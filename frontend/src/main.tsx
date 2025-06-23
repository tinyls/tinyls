import "./styles.css";

import {
  MutationCache,
  QueryCache,
  QueryClient,
  QueryClientProvider,
} from "@tanstack/react-query";
import { RouterProvider, createRouter } from "@tanstack/react-router";

import ReactDOM from "react-dom/client";
import { StrictMode } from "react";
import { ThemeProvider } from "./components/ui/theme-provider.tsx";
import reportWebVitals from "./reportWebVitals.ts";
// Import the generated route tree
import { routeTree } from "./routeTree.gen";

import { handleAuthError } from "@/utils/apiErrorHandler";

// Create a client
const queryClient = new QueryClient({
  queryCache: new QueryCache({
    onError: (error) => handleAuthError(error, router),
  }),
  mutationCache: new MutationCache({
    onError: (error) => handleAuthError(error, router),
  }),
});

// Create a new router instance
const router = createRouter({
  routeTree,
  context: {},
  defaultPreload: "intent",
  scrollRestoration: true,
  defaultStructuralSharing: true,
  defaultPreloadStaleTime: 0,
});

// Register the router instance for type safety
declare module "@tanstack/react-router" {
  interface Register {
    router: typeof router;
  }
}

// Render the app
const rootElement = document.getElementById("app");
if (rootElement && !rootElement.innerHTML) {
  const root = ReactDOM.createRoot(rootElement);
  root.render(
    <StrictMode>
      <ThemeProvider>
        <QueryClientProvider client={queryClient}>
          <RouterProvider router={router} />
        </QueryClientProvider>
      </ThemeProvider>
    </StrictMode>
  );
}

// If you want to start measuring performance in your app, pass a function
// to log results (for example: reportWebVitals(console.log))
// or send to an analytics endpoint. Learn more: https://bit.ly/CRA-vitals
reportWebVitals();
