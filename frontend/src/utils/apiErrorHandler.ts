import type { AxiosError } from "axios";
import { useAuthStore } from "@/store/auth";
import { toast } from "sonner";

/**
 * Handles authentication/session errors (401, 403) globally.
 * Use for global query/mutation error handling (e.g., in main.tsx).
 */
export function handleAuthError(
  error: unknown,
  router?: { navigate: (opts: { to: string }) => void }
) {
  const { status } = parseApiError(error);
  if (status && [401, 403].includes(status)) {
    useAuthStore.getState().logout();
    toast.error("Session expired. Please log in again.");
    if (router) {
      router.navigate({ to: "/login" });
    } else {
      window.location.href = "/login";
    }
  }
}

// Normalized error shape
export interface NormalizedApiError {
  status?: number;
  detail?: string;
}

/**
 * Normalizes any error (Axios, fetch, ApiError, etc.) to a consistent shape.
 */
export function parseApiError(err: unknown): NormalizedApiError {
  // Axios error
  if (err && typeof err === "object" && "isAxiosError" in err) {
    const axiosErr = err as AxiosError<any>;
    return {
      status: axiosErr.response?.status,
      detail:
        axiosErr.response?.data?.detail ||
        axiosErr.response?.data?.message ||
        axiosErr.message,
    };
  }
  // New client error shape (if any, adapt as needed)
  if (err && typeof err === "object" && "status" in err && "body" in err) {
    const body = (err as any).body;
    return {
      status: (err as any).status,
      detail:
        Array.isArray(body?.detail) && body.detail.length > 0
          ? body.detail[0].msg
          : body?.detail || (err as any).message,
    };
  }
  // Generic JS error
  if (err && typeof err === "object" && "message" in err) {
    return { detail: (err as any).message };
  }
  return { detail: "Something went wrong." };
}

/**
 * Generic API error handler for UI feedback.
 * Handles ApiError, AxiosError, fetch errors, and unknown error shapes.
 */
export function handleError(err: unknown) {
  const { detail } = parseApiError(err);
  toast.error(detail || "Something went wrong.");
}
