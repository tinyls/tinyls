import Logo from "@/components/ui/logo"
import { SheetClose } from "@/components/ui/sheet.tsx"
import { mainNavLinks } from "@/config/navigation"
import { cn } from "@/lib/utils"
import { useAuthStore } from "@/store/auth"
import { Link } from "@tanstack/react-router"
import { useLocation } from "@tanstack/react-router"

interface MainNavProps {
	isMobile?: boolean
}

const MainNav = ({ isMobile = false }: MainNavProps) => {
	const pathname = useLocation({
		select: (location) => location.pathname,
	})
	const isLoggedIn = useAuthStore((s) => s.isLoggedIn)

	const filteredNavLinks = mainNavLinks.filter(
		(link) =>
			(!link.requiresAuth || isLoggedIn) && (!isMobile || !link.hideInMobile),
	)

	if (isMobile) {
		return (
			<div className="flex flex-col h-full gap-4">
				<nav className="flex flex-col gap-2">
					{filteredNavLinks.map((link) => (
						<SheetClose asChild key={link.to}>
							<Link
								to={link.to}
								search={link.search}
								className={cn(
									"text-sm font-mono font-medium transition-colors hover:text-primary py-1",
									pathname === link.to
										? "text-primary"
										: "text-muted-foreground",
								)}
							>
								{link.label}
							</Link>
						</SheetClose>
					))}
				</nav>
			</div>
		)
	}

	return (
		<div className="flex gap-6 md:gap-10">
			<Link to="/" className="flex items-center space-x-2">
				<Logo width={24} height={24} />
				<span className="tracking-tight font-bold sm:inline-block">tinyls</span>
			</Link>
			<nav className="flex gap-6 items-center">
				{filteredNavLinks.map((link) => (
					<Link
						key={link.to}
						to={link.to}
						search={link.search}
						className={cn(
							"text-sm font-mono font-medium transition-colors hover:text-primary",
							pathname === link.to ? "text-primary" : "text-muted-foreground",
						)}
					>
						{link.label}
					</Link>
				))}
			</nav>
		</div>
	)
}

export default MainNav
