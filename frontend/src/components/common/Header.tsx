import { Link, useNavigate } from "@tanstack/react-router"
import { Menu, User } from "lucide-react"
import { Sheet, SheetContent, SheetTrigger } from "@/components/ui/sheet"

import { Button } from "@/components/ui/button"
import Logo from "@/components/ui/logo"
import MainNav from "./MainNav"
import { ModeToggle } from "../ui/mode-toggle"
import { SideNav } from "../ui/side-nav"
import { UserNav } from "../ui/user-nav"
import { useAuthStore } from "@/store/auth.ts"
import { useMediaQuery } from "@/hooks/use-media-query"

export default function Header() {
	const { isLoggedIn } = useAuthStore()
	const isMobile = !useMediaQuery("(min-width: 768px)")
	const navigate = useNavigate()
	return (
		<header className="px-5 md:px-0 flex justify-center sticky top-0 z-50 w-full border-b bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60">
			<div className="container flex h-16 items-center justify-between">
				{/* Left side - Logo and Mobile Menu */}
				<div className="flex items-center gap-4 md:hidden">
					{/* Mobile Menu */}
					<Sheet>
						<SheetTrigger asChild>
							<Button variant="outline" size="icon" className="md:hidden">
								<Menu className="h-[1.2rem] w-[1.2rem]" />
								<span className="sr-only">Toggle menu</span>
							</Button>
						</SheetTrigger>
						<SheetContent side="left" className="p-5 w-[300px] sm:w-[400px]">
							<SideNav />
						</SheetContent>
					</Sheet>

					<Link to="/" className="flex items-center space-x-2">
						<Logo width={24} height={24} />
						<span className="tracking-tight font-bold sm:inline-block">
							tinyls
						</span>
					</Link>
				</div>

				{/* Desktop Navigation */}
				<div className="hidden md:flex">
					<MainNav />
				</div>

				{/* Right side items */}
				<div className="flex items-center gap-4">
					<ModeToggle />
					{/* Show login icon on mobile if not logged in */}
					{!isLoggedIn && isMobile && (
						<Button
							variant="secondary"
							size="icon"
							aria-label="Login"
							onClick={() => navigate({ to: "/login" })}
						>
							<User className="h-4 w-4" />
						</Button>
					)}
					<div className="hidden md:flex">{<UserNav />}</div>
					{isLoggedIn && <div className="md:hidden flex">{<UserNav />}</div>}
				</div>
			</div>
		</header>
	)
}
