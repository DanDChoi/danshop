"use client";

import { useEffect, useState } from "react";
import { subscribeToasts, type Toast } from "./toast-store";

export function useToasts(): Toast[] {
  const [toasts, setToasts] = useState<Toast[]>([]);

  useEffect(() => subscribeToasts(setToasts), []);

  return toasts;
}
