import { Outlet, createRootRoute } from "@tanstack/react-router"

import AuthHydrator from "@/components/common/AuthHydrator"
import Footer from "@/components/common/Footer.tsx"
import Header from "../components/common/Header"
import { Toaster } from "@/components/ui/sonner"

export const Route = createRootRoute({
	component: () => (
		<>
			<AuthHydrator />
			<Header />
			<main className="flex-1 min-h-[calc(100vh-7.375rem)]">
				<Outlet />
				<Toaster />
			</main>
			{/* <TanStackRouterDevtools /> */}
			<Footer />
		</>
	),
})
