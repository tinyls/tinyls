import {
  useDeleteAccount,
  useUpdateProfile,
} from "@/api/client/user-controller/user-controller";
import {
  useGetCurrentUser,
  useUpdatePassword,
} from "@/api/client/auth-controller/auth-controller";

import type { HTTPValidationError } from "@/api/schemas/apiError";
import type { UserResponseDTO } from "@/api/schemas";
import { handleError } from "@/utils/apiErrorHandler";
import { toast } from "sonner";
import { useAuthStore } from "@/store/auth";
import { useNavigate } from "@tanstack/react-router";

// Query for current user
export function useCurrentUserQuery() {
  return useGetCurrentUser<UserResponseDTO, HTTPValidationError>({
    query: { queryKey: ["me"] },
  });
}

// Profile update mutation
export function useUpdateProfileMutation(
  userQuery: ReturnType<typeof useCurrentUserQuery>
) {
  return useUpdateProfile<HTTPValidationError, unknown>({
    mutation: {
      onSuccess: async () => {
        toast.success("Profile updated successfully");
        await useAuthStore.getState().refreshUser();
        userQuery.refetch();
      },
      onError: handleError,
    },
  });
}

// Password update mutation
export function useUpdatePasswordMutation() {
  return useUpdatePassword<HTTPValidationError, unknown>({
    mutation: {
      onSuccess: () => toast.success("Password updated successfully"),
      onError: handleError,
    },
  });
}

// Delete account mutation
export function useDeleteAccountMutation() {
  const navigate = useNavigate();
  return useDeleteAccount<HTTPValidationError, unknown>({
    mutation: {
      onSuccess: () => {
        toast.success("Account deleted. Goodbye!");
        useAuthStore.getState().logout();
        navigate({ to: "/" });
      },
      onError: handleError,
    },
  });
}

// Convenience hook to compose all user settings hooks
export function useUserSettings() {
  const userQuery = useCurrentUserQuery();
  const updateProfile = useUpdateProfileMutation(userQuery);
  const updatePassword = useUpdatePasswordMutation();
  const deleteMutation = useDeleteAccountMutation();
  return {
    userQuery,
    updateProfile,
    updatePassword,
    deleteMutation,
    isAnyPending:
      updateProfile.isPending ||
      updatePassword.isPending ||
      deleteMutation.isPending,
  };
}
