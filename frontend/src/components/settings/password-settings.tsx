import * as z from "zod"

import {
	Card,
	CardContent,
	CardDescription,
	CardFooter,
	CardHeader,
	CardTitle,
} from "@/components/ui/card"
import { Check, Eye, EyeOff, X } from "lucide-react"
import {
	Form,
	FormControl,
	FormField,
	FormItem,
	FormLabel,
	FormMessage,
} from "@/components/ui/form"
import { Suspense, lazy, useState } from "react"

import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { useAuthStore } from "@/store/auth"
import { useForm } from "react-hook-form"
import { useNavigate } from "@tanstack/react-router"
import { useUpdatePasswordMutation } from "@/hooks/useUserSettings"
import { zodResolver } from "@hookform/resolvers/zod"

// Lazy load the password strength bar component
const PasswordStrengthBar = lazy(() => import("react-password-strength-bar"))

// Password requirements
const PASSWORD_REQUIREMENTS = [
	{ id: "length", label: "At least 8 characters" },
	{ id: "uppercase", label: "One uppercase letter" },
	{ id: "lowercase", label: "One lowercase letter" },
	{ id: "digit", label: "One number" },
	{ id: "special", label: "One special character" },
]

// Password validation functions
const hasUpperCase = (str: string) => /[A-Z]/.test(str)
const hasLowerCase = (str: string) => /[a-z]/.test(str)
const hasDigit = (str: string) => /[0-9]/.test(str)
const hasSpecialChar = (str: string) => /[!@#$%^&*(),.?":{}|<>]/.test(str)

const formSchema = z
	.object({
		currentPassword: z.string().min(1, {
			message: "Current password is required",
		}),
		newPassword: z
			.string()
			.min(8, {
				message: "Password must be at least 8 characters",
			})
			.refine(
				(password) => {
					return (
						password.length >= 8 &&
						hasUpperCase(password) &&
						hasLowerCase(password) &&
						hasDigit(password) &&
						hasSpecialChar(password)
					)
				},
				{
					message: "Password must meet all requirements",
				},
			),
		confirmPassword: z.string().min(8, {
			message: "Please confirm your password",
		}),
	})
	.refine((data) => data.newPassword === data.confirmPassword, {
		message: "Passwords do not match",
		path: ["confirmPassword"],
	})

export default function PasswordSettings() {
	const updatePassword = useUpdatePasswordMutation()
	const [showNewPassword, setShowNewPassword] = useState(false)
	const [showConfirmPassword, setShowConfirmPassword] = useState(false)
	const user = useAuthStore((s) => s.user)
	const navigate = useNavigate()

	const form = useForm<z.infer<typeof formSchema>>({
		resolver: zodResolver(formSchema),
		defaultValues: {
			currentPassword: "",
			newPassword: "",
			confirmPassword: "",
		},
		mode: "onTouched",
		reValidateMode: "onChange",
	})

	if (!user?.canChangePassword) {
		return (
			<Card>
				<CardHeader>
					<CardTitle>Password</CardTitle>
					<CardDescription>
						You cannot change your password because your account is managed by
						an external provider.
					</CardDescription>
				</CardHeader>
			</Card>
		)
	}

	const onSubmit = (values: z.infer<typeof formSchema>) => {
		updatePassword.mutate(
			{
				data: {
					currentPassword: values.currentPassword,
					newPassword: values.newPassword,
				},
			},
			{
				onSuccess: () => {
					// Clear the form before logging out
					form.reset()
					useAuthStore.getState().logout()
					navigate({ to: "/login" })
				},
				onError: (error: any) => {
					if (
						error?.response?.status === 400 &&
						error?.response?.data?.message === "Current password is incorrect"
					) {
						form.setError("currentPassword", {
							type: "manual",
							message: "Current password is incorrect. Please try again.",
						})
						// Clear only the current password field
						// form.setValue("currentPassword", "", { shouldValidate: true })
					} else {
						form.setError("root", {
							type: "manual",
							message: "Failed to update password. Please try again.",
						})
					}
				},
			},
		)
	}

	const newPassword = form.watch("newPassword")
	const passwordRequirements = PASSWORD_REQUIREMENTS.map((req) => ({
		...req,
		met:
			req.id === "length"
				? newPassword.length >= 8
				: req.id === "uppercase"
					? hasUpperCase(newPassword)
					: req.id === "lowercase"
						? hasLowerCase(newPassword)
						: req.id === "digit"
							? hasDigit(newPassword)
							: req.id === "special"
								? hasSpecialChar(newPassword)
								: false,
	}))

	return (
		<Card>
			<CardHeader>
				<CardTitle>Password</CardTitle>
				<CardDescription>
					Change your password here. After saving, you'll be logged out.
				</CardDescription>
			</CardHeader>
			<CardContent className="space-y-4">
				<Form {...form}>
					<form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
						<FormField
							control={form.control}
							name="currentPassword"
							render={({ field }) => (
								<FormItem>
									<FormLabel>Current Password</FormLabel>
									<FormControl>
										<Input
											type="password"
											placeholder="Current password"
											{...field}
											onChange={(e) => {
												field.onChange(e)
												// Clear the error when user starts typing again
												if (form.formState.errors.currentPassword) {
													form.clearErrors("currentPassword")
												}
											}}
										/>
									</FormControl>
									<FormMessage />
								</FormItem>
							)}
						/>
						<FormField
							control={form.control}
							name="newPassword"
							render={({ field }) => (
								<FormItem>
									<FormLabel>New Password</FormLabel>
									<FormControl>
										<div className="relative">
											<Input
												type={showNewPassword ? "text" : "password"}
												placeholder="New password"
												{...field}
											/>
											<Button
												type="button"
												variant="ghost"
												size="icon"
												className="absolute right-0 top-0 h-full px-3 py-2 hover:bg-transparent"
												onClick={() => setShowNewPassword(!showNewPassword)}
											>
												{showNewPassword ? (
													<EyeOff className="h-4 w-4 text-muted-foreground" />
												) : (
													<Eye className="h-4 w-4 text-muted-foreground" />
												)}
												<span className="sr-only">
													{showNewPassword ? "Hide password" : "Show password"}
												</span>
											</Button>
										</div>
									</FormControl>
									{newPassword && (
										<div className="mt-2 space-y-2">
											<Suspense
												fallback={
													<p className="text-sm text-muted-foreground">
														Loading password strength...
													</p>
												}
											>
												<PasswordStrengthBar
													scoreWordClassName="capitalize"
													className="mb-0"
													password={newPassword}
												/>
											</Suspense>
											<div className="space-y-1 text-sm">
												{passwordRequirements.map((req) => (
													<div key={req.id} className="flex items-center gap-1">
														{req.met ? (
															<Check className="h-4 w-4 text-green-500" />
														) : (
															<X className="h-4 w-4 text-red-500" />
														)}
														<span
															className={
																req.met ? "text-green-500" : "text-red-500"
															}
														>
															{req.label}
														</span>
													</div>
												))}
											</div>
										</div>
									)}
									<FormMessage />
								</FormItem>
							)}
						/>
						<FormField
							control={form.control}
							name="confirmPassword"
							render={({ field }) => (
								<FormItem>
									<FormLabel>Confirm New Password</FormLabel>
									<FormControl>
										<div className="relative">
											<Input
												type={showConfirmPassword ? "text" : "password"}
												placeholder="Confirm new password"
												{...field}
											/>
											<Button
												type="button"
												variant="ghost"
												size="icon"
												className="absolute right-0 top-0 h-full px-3 py-2 hover:bg-transparent"
												onClick={() =>
													setShowConfirmPassword(!showConfirmPassword)
												}
											>
												{showConfirmPassword ? (
													<EyeOff className="h-4 w-4 text-muted-foreground" />
												) : (
													<Eye className="h-4 w-4 text-muted-foreground" />
												)}
												<span className="sr-only">
													{showConfirmPassword
														? "Hide password"
														: "Show password"}
												</span>
											</Button>
										</div>
									</FormControl>
									<FormMessage />
								</FormItem>
							)}
						/>
						{form.formState.errors.root && (
							<p className="text-sm text-red-500 mt-2">
								{form.formState.errors.root.message}
							</p>
						)}
					</form>
				</Form>
			</CardContent>
			<CardFooter>
				<Button
					type="submit"
					disabled={
						updatePassword.isPending ||
						!form.formState.isValid ||
						!passwordRequirements.every((req) => req.met)
					}
				>
					{updatePassword.isPending ? "Saving..." : "Change Password"}
				</Button>
			</CardFooter>
		</Card>
	)
}
