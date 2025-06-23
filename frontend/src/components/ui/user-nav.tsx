import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { authNavLinks, logoutLink, userMenuLinks } from "@/config/navigation";
import { Link, useLocation, useNavigate } from "@tanstack/react-router";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "./dropdown-menu";

import { Button } from "@/components/ui/button";
import { cn } from "@/lib/utils";
import { useAuthStore } from "@/store/auth";
import { User } from "lucide-react";
import { SheetClose } from "./sheet";

interface UserNavProps {
  isMobile?: boolean;
}

export function UserNav({ isMobile = false }: UserNavProps) {
  const { user, isLoggedIn, logout } = useAuthStore();
  const navigate = useNavigate();
  const pathname = useLocation({
    select: (location) => location.pathname,
  });

  const handleLogout = () => {
    logout();
    navigate({ to: "/" });
  };

  if (!isLoggedIn && !isMobile) {
    return (
      <div className="flex items-center gap-2">
        <Button variant="ghost" size="sm" asChild>
          <Link to={authNavLinks[0].to}>Login</Link>
        </Button>
        <Button size="sm" asChild>
          <Link to={authNavLinks[1].to}>Register</Link>
        </Button>
      </div>
    );
  }

  if (!isLoggedIn && isMobile) {
    return (
      <div className="flex flex-col w-full gap-3">
        <SheetClose asChild>
          <Button variant="outline" asChild>
            <Link to={authNavLinks[0].to}>Login</Link>
          </Button>
        </SheetClose>
        <SheetClose asChild>
          <Button asChild>
            <Link to={authNavLinks[1].to}>Register</Link>
          </Button>
        </SheetClose>
      </div>
    );
  }

  if (isMobile) {
    return (
      <div className="flex flex-col gap-2">
        <div className="flex items-center gap-3">
          <Avatar className="h-8 w-8">
            <AvatarImage src={user?.avatarUrl} alt={user?.name} />
            <AvatarFallback>
              {user?.name?.charAt(0) || user?.email?.charAt(0) || (
                <User className="h-4 w-4" />
              )}
            </AvatarFallback>
          </Avatar>
          <div className="flex flex-col gap-1 items-start justify-center">
            <p className="text-sm font-medium leading-none">
              {user?.name || user?.email}
            </p>
            {user?.name && (
              <p className="text-xs leading-none text-muted-foreground">
                {user?.email}
              </p>
            )}
          </div>
        </div>
      </div>
    );
  }

  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <Button variant="ghost" className="relative h-8 w-8 rounded-full">
          <Avatar className="h-8 w-8">
            <AvatarImage src={user?.avatarUrl} alt={user?.name} />
            <AvatarFallback>
              {user?.name?.charAt(0) || user?.email?.charAt(0) || (
                <User className="h-4 w-4" />
              )}
            </AvatarFallback>
          </Avatar>
        </Button>
      </DropdownMenuTrigger>
      <DropdownMenuContent className="w-56" align="end" forceMount>
        <DropdownMenuLabel className="font-normal">
          <div className="flex flex-col space-y-1">
            <p className="text-sm font-medium leading-none">
              {user?.name || user?.email}
            </p>
            {user?.name && (
              <p className="text-xs leading-none text-muted-foreground">
                {user?.email}
              </p>
            )}
          </div>
        </DropdownMenuLabel>
        <DropdownMenuSeparator />
        {userMenuLinks.map((link) => (
          <DropdownMenuItem key={link.to} className="font-mono" asChild>
            <Link
              to={link.to}
              search={link.search}
              className={cn(
                "flex items-center transition-colors hover:text-primary",
                pathname === link.to ? "text-primary" : "text-muted-foreground"
              )}
            >
              {link.icon && <link.icon className="mr-2 h-4 w-4" />}
              <span>{link.label}</span>
            </Link>
          </DropdownMenuItem>
        ))}
        <DropdownMenuSeparator />
        <DropdownMenuItem
          onClick={handleLogout}
          className="text-red-600 font-mono"
        >
          {logoutLink.icon && (
            <logoutLink.icon className="mr-2 h-4 w-4 tracking-normal" />
          )}
          <span>{logoutLink.label}</span>
        </DropdownMenuItem>
      </DropdownMenuContent>
    </DropdownMenu>
  );
}
