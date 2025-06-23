import * as React from "react";
import { Loader2 } from "lucide-react";
import { cn } from "@/lib/utils";

export interface SpinnerProps extends React.HTMLAttributes<HTMLDivElement> {
  size?: "sm" | "md" | "lg" | "xl" | "2xl";
  variant?: "default" | "primary" | "secondary";
}

const Spinner = React.forwardRef<HTMLDivElement, SpinnerProps>(
  ({ className, size = "md", variant = "default", ...props }, ref) => {
    return (
      <div
        ref={ref}
        role="status"
        className={cn("flex items-center justify-center", className)}
        {...props}
      >
        <Loader2
          className={cn(
            "animate-spin",
            {
              "h-4 w-4": size === "sm",
              "h-6 w-6": size === "md",
              "h-8 w-8": size === "lg",
              "h-10 w-10": size === "xl",
              "h-12 w-12": size === "2xl",
            },
            {
              "text-muted-foreground": variant === "default",
              "text-primary": variant === "primary",
              "text-secondary": variant === "secondary",
            }
          )}
        />
        <span className="sr-only">Loading...</span>
      </div>
    );
  }
);
Spinner.displayName = "Spinner";

export { Spinner };
