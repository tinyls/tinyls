import {
  useCreateUrl,
  useDeleteUrlByShortCode,
  useGetUrlsByUser,
} from "@/api/client/url-controller/url-controller";

import type { HTTPValidationError } from "@/api/schemas/apiError";
import type { UrlDTO } from "@/api/schemas";
import { handleError } from "@/utils/apiErrorHandler";
import { toast } from "sonner";
import { useQueryClient } from "@tanstack/react-query";

// Ergonomic mutation for shortening URLs
export function useUrlShortenerMutation() {
  const queryClient = useQueryClient();

  const mutation = useCreateUrl<HTTPValidationError, unknown>({
    mutation: {
      onSuccess: () => {
        queryClient.invalidateQueries({ queryKey: ["url-history"] });
        toast.success("URL shortened successfully");
      },
      onError: (err) => {
        handleError(err);
      },
    },
  });

  // Ergonomic mutate function for components
  const shortenUrl = (originalUrl: string) => {
    mutation.mutate({ data: { originalUrl } });
  };

  return {
    ...mutation,
    shortenUrl,
  };
}

// Query for URL history
export function useUrlHistoryQuery() {
  const query = useGetUrlsByUser<UrlDTO[], HTTPValidationError>({
    query: {
      queryKey: ["url-history"],
    },
  });
  return query;
}

// Mutation for deleting a URL
export function useDeleteUrlMutation() {
  const queryClient = useQueryClient();
  const mutation = useDeleteUrlByShortCode<HTTPValidationError, unknown>({
    mutation: {
      onSuccess: () => {
        queryClient.invalidateQueries({ queryKey: ["url-history"] });
        toast.success("URL deleted successfully");
      },
      onError: (err) => {
        handleError(err);
      },
    },
  });

  // Ergonomic mutate function for components
  const deleteUrl = (shortCode: string) => {
    mutation.mutate({ shortCode });
  };

  return {
    ...mutation,
    deleteUrl,
  };
}
