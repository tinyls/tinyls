import {
	DropdownMenu,
	DropdownMenuContent,
	DropdownMenuItem,
	DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"
import { Link, useLocation, useNavigate } from "@tanstack/react-router"
import { ChevronRight, LogOut, Settings } from "lucide-react"

import MainNav from "@/components/common/MainNav.tsx"
import { Button } from "@/components/ui/button.tsx"
import Logo from "@/components/ui/logo.tsx"
import { Separator } from "@/components/ui/separator.tsx"
import { UserNav } from "@/components/ui/user-nav.tsx"
import { settingsNavLinks } from "@/config/navigation"
import { cn } from "@/lib/utils"
import { useAuthStore } from "@/store/auth"
import { useState } from "react"
import { SheetClose } from "./sheet"

export function SideNav() {
	const { isLoggedIn, logout } = useAuthStore()
	const user = useAuthStore((s) => s.user)
	const navigate = useNavigate()
	const [isOpen, setIsOpen] = useState(false)

	const pathname = useLocation({
		select: (location) => location.pathname,
	})

	// Filter out password tab if user cannot change password
	const filteredNavLinks = settingsNavLinks.filter(
		(l) => l.id !== "password" || user?.canChangePassword,
	)

	const handleLogout = () => {
		logout()
		navigate({ to: "/" })
	}

	return (
		<>
			<Link to="/" className="flex items-center space-x-2">
				<Logo width={24} height={24} />
				<span className="tracking-tight font-bold sm:inline-block">tinyls</span>
			</Link>
			{!isLoggedIn ? (
				<>
					<MainNav isMobile />
					<UserNav isMobile />
				</>
			) : (
				<>
					<div className="flex flex-col justify-between gap-2 h-full">
						<Separator className="mb-2 mt-1" />
						<UserNav isMobile />
						<Separator className="my-2" />
						<MainNav isMobile />
						<DropdownMenu open={isOpen} onOpenChange={setIsOpen}>
							<DropdownMenuTrigger asChild>
								<Button variant="ghost" className="w-full justify-between">
									<div
										className={cn(
											"flex items-center text-sm font-mono font-medium transition-colors hover:text-primary",
											pathname === "/settings"
												? "text-primary"
												: "text-muted-foreground",
										)}
									>
										<Settings className="mr-2 h-4 w-4" />
										<span>Settings</span>
									</div>
									<ChevronRight
										className={cn(
											"h-4 w-4 text-muted-foreground transition-transform duration-200",
											isOpen && "-rotate-90",
										)}
									/>
								</Button>
							</DropdownMenuTrigger>
							<DropdownMenuContent
								align="start"
								className="w-full"
								sideOffset={8}
							>
								{filteredNavLinks.map((link) => (
									<DropdownMenuItem key={link.id} asChild>
										<SheetClose asChild>
											<Link
												to={link.to}
												search={link.search}
												className="flex items-center font-mono"
											>
												{link.icon && <link.icon className="mr-2 h-4 w-4" />}
												<span>{link.label}</span>
											</Link>
										</SheetClose>
									</DropdownMenuItem>
								))}
							</DropdownMenuContent>
						</DropdownMenu>
						<Button
							variant="ghost"
							onClick={handleLogout}
							className="w-full justify-start text-red-600"
						>
							<LogOut className="mr-2 h-4 w-4 tracking-normal" />
							<span>Logout</span>
						</Button>
					</div>
				</>
			)}
		</>
	)
}
