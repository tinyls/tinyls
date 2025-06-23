import { createFileRoute, redirect } from "@tanstack/react-router"

import SettingsPanes from "@/components/settings/SettingsPanes.tsx"
import { useAuthStore } from "@/store/auth"

export const Route = createFileRoute("/settings")({
	validateSearch: (search: Record<string, unknown>) => {
		return {
			tab: search.tab as "profile" | "password" | "delete" | undefined,
		}
	},
	component: SettingsComponent,
	beforeLoad: async () => {
		const isLoggedIn = useAuthStore.getState().isLoggedIn
		if (!isLoggedIn) {
			throw redirect({ to: "/login" })
		}
	},
})

function SettingsComponent() {
	const { tab } = Route.useSearch()

	return (
		<div className="flex flex-col items-center justify-center">
			<div className="container py-10 px-5 md:px-0">
				<div className="mb-8">
					<h1 className="text-3xl font-bold tracking-tight">Settings</h1>
					<p className="text-muted-foreground">Manage your account</p>
				</div>
				<div className="flex gap-8">
					<SettingsPanes defaultTab={tab} />
				</div>
			</div>
		</div>
	)
}
