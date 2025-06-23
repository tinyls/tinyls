import { type SettingsNavLink, settingsNavLinks } from "@/config/navigation"
import { useAuthStore } from "@/store/auth"

import DeleteAccountSettings from "@/components/settings/delete-settings"
import PasswordSettings from "@/components/settings/password-settings"
import ProfileSettings from "@/components/settings/profile-settings"
import { Button } from "@/components/ui/button"
import { Tabs, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { useMediaQuery } from "@/hooks/use-media-query"
import { cn } from "@/lib/utils"
import { Route as SettingsRoute } from "@/routes/settings"
import { useNavigate } from "@tanstack/react-router"

interface SettingsPanesProps {
	defaultTab?: SettingsNavLink["id"]
}

export default function SettingsPanes({
	defaultTab = "profile",
}: SettingsPanesProps) {
	const navigate = useNavigate()
	const { tab } = SettingsRoute.useSearch()
	const activePage = tab || defaultTab
	const isDesktop = useMediaQuery("(min-width: 768px)")
	const user = useAuthStore((s) => s.user)

	// Filter out password tab if user cannot change password
	const filteredNavLinks = settingsNavLinks.filter(
		(l) => l.id !== "password" || user?.canChangePassword,
	)

	const handlePageChange = (page: string) => {
		const link = filteredNavLinks.find((l) => l.id === page)
		if (link) {
			navigate({
				to: link.to,
				search: link.search,
			})
		}
	}

	return (
		<div className="flex w-full flex-col gap-4 md:flex-row md:gap-8">
			{/* Sidebar - Only visible on desktop */}
			{isDesktop && (
				<div className="w-64 shrink-0">
					<nav className="flex flex-col gap-1">
						{filteredNavLinks.map((page) => (
							<Button
								key={page.id}
								variant={activePage === page.id ? "secondary" : "ghost"}
								className={cn(
									"w-full justify-start gap-2",
									activePage === page.id && "bg-secondary",
								)}
								onClick={() => handlePageChange(page.id)}
							>
								{page.icon && <page.icon className="h-4 w-4" />}
								{page.label}
							</Button>
						))}
					</nav>
				</div>
			)}

			{/* Mobile Tab Bar */}
			{!isDesktop && (
				<div className="w-full mb-2">
					<Tabs
						value={activePage}
						onValueChange={handlePageChange}
						className="w-full"
					>
						<TabsList className="w-full justify-between">
							{filteredNavLinks.map((page) => (
								<TabsTrigger
									key={page.id}
									value={page.id}
									className="data-[state=active]:bg-white dark:data-[state=active]:bg-black flex-1 flex items-center justify-center gap-1.5 py-2"
								>
									{page.icon && <page.icon className="h-4 w-4" />}
									{page.label}
								</TabsTrigger>
							))}
						</TabsList>
					</Tabs>

					{/* Dropdown select for mobile navigation */}
					{/* <select
						className="block w-full rounded-md border border-input bg-background px-3 py-2 text-sm text-foreground focus:border-ring focus:outline-none focus:ring-2 focus:ring-ring"
						value={activePage}
						onChange={(e) => handlePageChange(e.target.value)}
					>
						{filteredNavLinks.map((page) => (
							<option key={page.id} value={page.id}>
								{page.label}
							</option>
						))}
					</select> */}
				</div>
			)}

			{/* Content */}
			<div className="flex-1">
				{activePage === "profile" && <ProfileSettings />}
				{activePage === "password" && <PasswordSettings />}
				{activePage === "delete" && <DeleteAccountSettings />}
			</div>
		</div>
	)
}
