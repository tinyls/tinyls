import { createFileRoute } from "@tanstack/react-router";
import { useEffect } from "react";
import { useSearch } from "@tanstack/react-router";

export function OAuth2Callback() {
  const { token } = useSearch({ from: "/oauth2-callback" });

  useEffect(() => {
    if (token && window.opener) {
      window.opener.postMessage({ token }, window.location.origin);
    }
    window.close();
  }, [token]);

  return <div
  className="flex flex-col items-center justify-center"
  style={{ minHeight: "inherit" }}
  >
    <div className="container flex w-screen">
      <div className="mx-auto flex w-full flex-col justify-center space-y-6 sm:w-[350px]">
        <div className="flex flex-col space-y-2 text-center">
          <p className="text-sm text-muted-foreground">
            Logging you in ...
          </p>
        </div>
      </div>
    </div>
  </div>;
}

export const Route = createFileRoute("/oauth2-callback")({
  validateSearch: (s: Record<string, unknown>) => ({ token: s.token as string | undefined }),
  component: OAuth2Callback,
}); 