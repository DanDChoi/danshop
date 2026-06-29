package com.dan.danshop.domain.point.service;

import com.dan.danshop.domain.notification.dto.NotificationEvent;
import com.dan.danshop.domain.notification.service.NotificationService;
import com.dan.danshop.domain.point.dto.PointHistoryResponse;
import com.dan.danshop.domain.point.entity.PointHistory;
import com.dan.danshop.domain.point.entity.PointType;
import com.dan.danshop.domain.point.repository.PointHistoryRepository;
import com.dan.danshop.domain.user.entity.User;
import com.dan.danshop.domain.user.repository.UserRepository;
import com.dan.danshop.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static com.dan.danshop.global.exception.ErrorCode.USER_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class PointService {

    private final PointHistoryRepository pointHistoryRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    private static final BigDecimal EARN_RATE = new BigDecimal("0.01"); // 결제금액의 1% 적립

    @Transactional(readOnly = true)
    public long getBalance(String userId) {
        return userRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(USER_NOT_FOUND))
                .getPointBalance();
    }

    @Transactional(readOnly = true)
    public List<PointHistoryResponse> getHistory(String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(USER_NOT_FOUND));
        return pointHistoryRepository.findByUserOrderByCreatedAtDesc(user)
                .stream().map(PointHistoryResponse::from).toList();
    }

    // Called within OrderService transaction: deducts points and returns reduced payAmount
    public BigDecimal applyPoints(User user, Long usePoints, BigDecimal payAmount) {
        if (usePoints == null || usePoints <= 0) return payAmount;
        user.deductPoints(usePoints);
        return payAmount.subtract(BigDecimal.valueOf(usePoints)).max(BigDecimal.ZERO);
    }

    // Called within OrderService transaction: saves USE history after order is persisted
    public void recordUse(User user, Long orderId, long usePoints) {
        pointHistoryRepository.save(PointHistory.builder()
                .user(user).amount(usePoints).type(PointType.USE)
                .description("주문 #" + orderId + " 포인트 사용")
                .orderId(orderId).build());
    }

    // Called within OrderService transaction: credits 1% of payAmount and saves EARN history
    public void earnPoints(User user, Long orderId, BigDecimal payAmount) {
        long amount = payAmount.multiply(EARN_RATE).longValue();
        if (amount <= 0) return;
        user.addPoints(amount);
        pointHistoryRepository.save(PointHistory.builder()
                .user(user).amount(amount).type(PointType.EARN)
                .description("주문 #" + orderId + " 포인트 적립")
                .orderId(orderId).build());

        notificationService.send(user.getUserId(), new NotificationEvent(
                "POINT_EARNED",
                amount + " 포인트가 적립되었습니다.",
                amount
        ));
    }

    // Called within cancelOrder transaction: reverses EARN and USE histories for the order
    public void cancelOrderPoints(User user, Long orderId) {
        List<PointHistory> histories = pointHistoryRepository
                .findByOrderIdAndTypeIn(orderId, List.of(PointType.EARN, PointType.USE));

        for (PointHistory h : histories) {
            if (h.getType() == PointType.EARN) {
                user.forceDeductPoints(h.getAmount());
                pointHistoryRepository.save(PointHistory.builder()
                        .user(user).amount(h.getAmount()).type(PointType.EARN_CANCEL)
                        .description("주문 #" + orderId + " 포인트 적립 취소")
                        .orderId(orderId).build());
            } else {
                user.addPoints(h.getAmount());
                pointHistoryRepository.save(PointHistory.builder()
                        .user(user).amount(h.getAmount()).type(PointType.REFUND)
                        .description("주문 #" + orderId + " 포인트 사용 환불")
                        .orderId(orderId).build());
            }
        }
    }
}
