import LoginForm from "@/components/ui/login-form"
import { createFileRoute, redirect } from "@tanstack/react-router"
import { useAuthStore } from "@/store/auth"

export const Route = createFileRoute("/login")({
	component: LoginComponent,
	beforeLoad: async () => {
		if (useAuthStore.getState().isLoggedIn) {
			throw redirect({
				to: "/",
			})
		}
	},
})

function LoginComponent() {
	return (
		<div
			className="flex flex-col items-center justify-center"
			style={{ minHeight: "inherit" }}
		>
			<div className="container flex w-screen px-5 md:px-0">
				<div className="mx-auto flex w-full flex-col justify-center space-y-6 sm:w-[350px] md:w-[380px]">
					<div className="flex flex-col space-y-2 text-center">
						<h1 className="text-2xl font-semibold tracking-tight">
							Welcome back
						</h1>
						<p className="text-sm text-muted-foreground">
							Enter your credentials to sign in to your account
						</p>
					</div>
					<LoginForm />
				</div>
			</div>
		</div>
	)
}
