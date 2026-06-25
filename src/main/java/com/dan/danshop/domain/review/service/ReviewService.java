package com.dan.danshop.domain.review.service;

import com.dan.danshop.domain.order.entity.OrderStatus;
import com.dan.danshop.domain.order.repository.OrderItemRepository;
import com.dan.danshop.domain.product.entity.Product;
import com.dan.danshop.domain.product.repository.ProductRepository;
import com.dan.danshop.domain.review.dto.CreateReviewRequest;
import com.dan.danshop.domain.review.dto.ProductReviewResponse;
import com.dan.danshop.domain.review.dto.ReviewResponse;
import com.dan.danshop.domain.review.dto.UpdateReviewRequest;
import com.dan.danshop.domain.review.entity.Review;
import com.dan.danshop.domain.review.repository.ReviewRepository;
import com.dan.danshop.domain.user.entity.User;
import com.dan.danshop.domain.user.repository.UserRepository;
import com.dan.danshop.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.dan.danshop.global.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;

    @Transactional
    public Long createReview(Long productId, CreateReviewRequest request) {
        if (request.getRating() < 1 || request.getRating() > 5) throw new BusinessException(INVALID_RATING);

        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUserId(userId).orElseThrow(() -> new BusinessException(USER_NOT_FOUND));
        Product product = productRepository.findById(productId).orElseThrow(() -> new BusinessException(PRODUCT_NOT_FOUND));

        if (!orderItemRepository.existsByOrder_UserAndProductAndOrder_StatusNot(user, product, OrderStatus.CANCELLED)) {
            throw new BusinessException(NOT_PURCHASED_PRODUCT);
        }
        if (reviewRepository.existsByUserAndProduct(user, product)) {
            throw new BusinessException(DUPLICATE_REVIEW);
        }

        Review review = reviewRepository.save(Review.builder()
                .user(user).product(product)
                .rating(request.getRating()).content(request.getContent())
                .build());

        updateProductAvgRating(product);
        return review.getId();
    }

    @Transactional(readOnly = true)
    public ProductReviewResponse getReviews(Long productId) {
        Product product = productRepository.findById(productId).orElseThrow(() -> new BusinessException(PRODUCT_NOT_FOUND));
        List<ReviewResponse> reviews = reviewRepository.findByProductOrderByCreatedAtDesc(product)
                .stream().map(ReviewResponse::from).toList();
        return new ProductReviewResponse(product.getAvgRating(), reviews.size(), reviews);
    }

    @Transactional
    public void updateReview(Long reviewId, UpdateReviewRequest request) {
        if (request.getRating() < 1 || request.getRating() > 5) throw new BusinessException(INVALID_RATING);

        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        Review review = reviewRepository.findById(reviewId).orElseThrow(() -> new BusinessException(REVIEW_NOT_FOUND));

        if (!review.getUser().getUserId().equals(userId)) throw new BusinessException(NOT_REVIEW_OWNER);

        review.update(request.getRating(), request.getContent());
        updateProductAvgRating(review.getProduct());
    }

    @Transactional
    public void deleteReview(Long reviewId) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        Review review = reviewRepository.findById(reviewId).orElseThrow(() -> new BusinessException(REVIEW_NOT_FOUND));

        if (!review.getUser().getUserId().equals(userId)) throw new BusinessException(NOT_REVIEW_OWNER);

        Product product = review.getProduct();
        reviewRepository.delete(review);
        updateProductAvgRating(product);
    }

    private void updateProductAvgRating(Product product) {
        Double avg = reviewRepository.findAvgRatingByProduct(product);
        product.updateAvgRating(avg);
    }
}
