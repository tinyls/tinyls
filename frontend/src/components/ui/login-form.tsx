import * as z from "zod"

import {
	Form,
	FormControl,
	FormField,
	FormItem,
	FormLabel,
	FormMessage,
} from "@/components/ui/form"
import { Link, useNavigate } from "@tanstack/react-router"
import { Eye, EyeOff } from "lucide-react"

import { GoogleIcon } from "@/components/icons/google-icon"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Separator } from "@/components/ui/separator"
import { useLoginMutation } from "@/hooks/useAuth"
import { zodResolver } from "@hookform/resolvers/zod"
import { useState } from "react"
import { useForm } from "react-hook-form"
import { toast } from "sonner"
import GithubIcon from "../icons/github-icon"

const formSchema = z.object({
	email: z.string().email({
		message: "Please enter a valid email address",
	}),
	password: z.string().min(8, {
		message: "Password must be at least 8 characters",
	}),
})

type LoginStep = "email" | "password"

// TODO: add forgot password button (nudge user to click it after user enters wrong password)
export default function LoginForm() {
	const [showPassword, setShowPassword] = useState(false)
	const [loginStep, setLoginStep] = useState<LoginStep>("email")
	const navigate = useNavigate()

	const form = useForm<z.infer<typeof formSchema>>({
		resolver: zodResolver(formSchema),
		defaultValues: {
			email: "",
			password: "",
		},
		mode: "onTouched",
		reValidateMode: "onChange",
	})

	// Watch the email field value and its error state
	const email = form.watch("email")
	const emailError = form.formState.errors.email

	const { loginWithPassword, loginWithOAuth2, isPending } = useLoginMutation(
		() => {
			toast.success("Signed in successfully")
			navigate({ to: "/dashboard" })
		},
		(err: any) => {
			if (err.message === "Invalid email or password") {
				form.setError("email", {})
				form.setError("password", {
					type: "manual",
					message: "Invalid email or password. Please try again.",
				})
				toast.error("Invalid email or password. Please try again.")
			} else {
				toast.error("Login failed. Please try again.")
			}
		},
	)

	const handleOAuth2Login = async (provider: "google" | "github") => {
		let loadingToastId: string | number | undefined = undefined
		try {
			const isMobile =
				/Mobi|Android/i.test(navigator.userAgent) ||
				window.matchMedia("(pointer:coarse)").matches

			if (isMobile) {
				loadingToastId = toast.loading("Redirecting to login...")
			}

			await loginWithOAuth2(provider)
			if (loadingToastId !== undefined) toast.dismiss(loadingToastId)
			toast.success("Successfully logged in!")
			navigate({ to: "/dashboard" })
		} catch (error) {
			if (loadingToastId !== undefined) toast.dismiss(loadingToastId)
			if (error instanceof Error) {
				if (error.message === "Popup closed by user") {
					toast.error("Login cancelled")
				} else {
					toast.error("Failed to login with OAuth2")
				}
			} else {
				toast.error("An unexpected error occurred")
			}
		}
	}

	const handleContinue = async () => {
		// Trigger email validation
		const isEmailValid = await form.trigger("email")

		if (isEmailValid) {
			setLoginStep("password")
		}
	}

	const handleBack = () => {
		setLoginStep("email")
	}

	function onSubmit(values: z.infer<typeof formSchema>) {
		form.clearErrors()
		loginWithPassword({ email: values.email, password: values.password })
	}

	return (
		<Form {...form}>
			<form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
				<FormField
					control={form.control}
					name="email"
					render={({ field }) => (
						<FormItem>
							<FormLabel>Email</FormLabel>
							<FormControl>
								<Input placeholder="you@example.com" {...field} />
							</FormControl>
							<FormMessage className="pt-1" />
						</FormItem>
					)}
				/>

				{loginStep === "password" && (
					<>
						<FormField
							control={form.control}
							name="password"
							render={({ field }) => (
								<FormItem>
									<FormLabel>Password</FormLabel>
									<FormControl>
										<div className="relative">
											<Input
												type={showPassword ? "text" : "password"}
												placeholder="••••••••"
												{...field}
											/>
											<button
												type="button"
												tabIndex={-1}
												className="absolute right-2 top-1/2 px-1 -translate-y-1/2 text-muted-foreground"
												onClick={() => setShowPassword((v) => !v)}
												aria-label={
													showPassword ? "Hide password" : "Show password"
												}
												style={{
													background: "none",
													border: "none",
													margin: 0,
												}}
											>
												{showPassword ? (
													<EyeOff className="h-5 w-5" />
												) : (
													<Eye className="h-5 w-5" />
												)}
											</button>
										</div>
									</FormControl>
									<FormMessage className="pt-1" />
								</FormItem>
							)}
						/>
						<Button type="submit" className="w-full" disabled={isPending}>
							{isPending ? "Signing in..." : "Sign in"}
						</Button>
						<Button
							type="button"
							variant="ghost"
							className="w-full"
							onClick={handleBack}
						>
							Back
						</Button>
					</>
				)}

				{loginStep === "email" && (
					<>
						<Button
							type="button"
							className="w-full"
							onClick={handleContinue}
							disabled={!email || !!emailError}
						>
							Continue
						</Button>

						<div className="relative mb-6 mt-4">
							<div className="absolute inset-0 flex items-center">
								<Separator className="w-full" />
							</div>
							<div className="relative flex justify-center text-sm lowercase">
								<span className="bg-background px-2 text-muted-foreground">
									Or continue with
								</span>
							</div>
						</div>

						<div className="grid grid-cols-2 gap-4">
							<Button
								variant="outline"
								type="button"
								onClick={() => handleOAuth2Login("google")}
								disabled={isPending}
								className="flex items-center justify-center gap-2"
							>
								<GoogleIcon className="h-4 w-4" />
								Google
							</Button>
							<Button
								variant="outline"
								type="button"
								onClick={() => handleOAuth2Login("github")}
								disabled={isPending}
								className="flex items-center justify-center gap-2"
							>
								<GithubIcon className="h-4 w-4" />
								GitHub
							</Button>
						</div>
					</>
				)}
			</form>

			<div className="text-center text-sm">
				Don't have an account?{" "}
				<Link
					to="/register"
					className="underline underline-offset-4 hover:text-primary"
				>
					Register
				</Link>
			</div>
		</Form>
	)
}
