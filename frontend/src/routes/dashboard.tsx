import { createFileRoute, redirect } from "@tanstack/react-router"

import URLHistory from "@/components/url-shortener/URLHistory"
import URLShortener from "@/components/url-shortener/URLShortener"
import { useAuthStore } from "@/store/auth"

export const Route = createFileRoute("/dashboard")({
	component: DashboardComponent,
	beforeLoad: async () => {
		const isLoggedIn = useAuthStore.getState().isLoggedIn

		if (!isLoggedIn) {
			throw redirect({
				to: "/login",
			})
		}
	},
})

function DashboardComponent() {
	const { user } = useAuthStore()
	return (
		<div className="flex flex-col items-center justify-center px-5 md:px-0">
			<div className="container py-10">
				<div className="mb-8">
					<h1 className="text-3xl font-bold tracking-tight">
						Welcome {user?.name || user?.email}
					</h1>
					<p className="text-muted-foreground pt-2">
						Create, manage and track all your shortened URLs
					</p>
				</div>
				<div className="flex flex-col space-y-8 md:grid md:grid-cols-3 md:gap-4">
					<div className="col-span-1">
						<div className="mb-4">
							<h1 className="text-2xl font-bold tracking-tight">
								Shorten a URL
							</h1>
						</div>
						<URLShortener showQr={true} />
					</div>
					<div className="col-span-2">
						<div className="mb-4">
							<h1 className="text-2xl font-bold tracking-tight">Your URLs</h1>
						</div>
						<URLHistory />
					</div>
				</div>
			</div>
		</div>
	)
}
