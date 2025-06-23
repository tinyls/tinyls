import * as z from "zod"

import { Check, X } from "lucide-react"
import { Eye, EyeOff } from "lucide-react"
import {
	Form,
	FormControl,
	FormField,
	FormItem,
	FormLabel,
	FormMessage,
} from "@/components/ui/form"
import { Link, useNavigate } from "@tanstack/react-router"
import { Suspense, lazy, useState } from "react"
import { useLoginMutation, useRegisterMutation } from "@/hooks/useAuth"

import { Button } from "@/components/ui/button"
import { Checkbox } from "@/components/ui/checkbox"
import { Input } from "@/components/ui/input"
import { toast } from "sonner"
import { useForm } from "react-hook-form"
import { zodResolver } from "@hookform/resolvers/zod"

// TODO: add terms and conditions route

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
		email: z.string().email({
			message: "Please enter a valid email address",
		}),
		password: z
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
		full_name: z.string().min(2, {
			message: "Name must be at least 2 characters",
		}),
		acceptTerms: z.boolean().refine((val) => val === true, {
			message: "You must accept the terms and conditions",
		}),
	})
	.refine((data) => data.password === data.confirmPassword, {
		message: "Passwords do not match",
		path: ["confirmPassword"],
	})

export default function RegisterForm() {
	const [showPassword, setShowPassword] = useState(false)
	const [showConfirmPassword, setShowConfirmPassword] = useState(false)
	const [emailExists, setEmailExists] = useState(false)
	const navigate = useNavigate()

	const form = useForm<z.infer<typeof formSchema>>({
		resolver: zodResolver(formSchema),
		defaultValues: {
			email: "",
			password: "",
			confirmPassword: "",
			full_name: "",
			acceptTerms: false,
		},
		mode: "onTouched",
		reValidateMode: "onChange",
	})

	const loginMutation = useLoginMutation(
		() => {
			toast.success("Registration successful")
			navigate({ to: "/dashboard" })
		},
		(err: any) => {
			if (err.message === "Invalid username or password") {
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

	const registerMutation = useRegisterMutation()

	async function onSubmit(values: z.infer<typeof formSchema>) {
		form.clearErrors()
		setEmailExists(false)
		registerMutation.mutate(
			{
				data: {
					email: values.email,
					password: values.password,
					name: values.full_name,
				},
			},
			{
				onSuccess: () => {
					loginMutation.loginWithPassword({
						email: values.email,
						password: values.password,
					})
				},
				onError: (err: any) => {
					if (
						err?.response?.status === 400 &&
						err?.response?.data?.detail?.toLowerCase().includes("password")
					) {
						form.setError("password", {
							type: "manual",
							message: err.response.data.detail,
						})
					} else if (
						err?.response?.status === 409 &&
						err?.response?.data?.message
							?.toLowerCase()
							.includes("email already exists")
					) {
						form.setError("email", {
							type: "manual",
							message: "A user with this email already exists.",
						})
						setEmailExists(true)
					} else {
						toast.error("Registration failed. Please try again.")
					}
				},
			},
		)
	}

	const password = form.watch("password")
	const passwordRequirements = PASSWORD_REQUIREMENTS.map((req) => ({
		...req,
		met:
			req.id === "length"
				? password.length >= 8
				: req.id === "uppercase"
					? hasUpperCase(password)
					: req.id === "lowercase"
						? hasLowerCase(password)
						: req.id === "digit"
							? hasDigit(password)
							: req.id === "special"
								? hasSpecialChar(password)
								: false,
	}))

	return (
		<Form {...form}>
			<form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
				<FormField
					control={form.control}
					name="full_name"
					render={({ field }) => (
						<FormItem>
							<FormLabel>Name</FormLabel>
							<FormControl>
								<Input placeholder="Your name" {...field} />
							</FormControl>
							<FormMessage />
						</FormItem>
					)}
				/>
				<FormField
					control={form.control}
					name="email"
					render={({ field }) => (
						<FormItem>
							<FormLabel>Email</FormLabel>
							<FormControl>
								<Input
									placeholder="you@example.com"
									{...field}
									onChange={(e) => {
										field.onChange(e)
										setEmailExists(false)
									}}
								/>
							</FormControl>
							<FormMessage />
							{emailExists && (
								<p className="text-sm">
									Do you want to{" "}
									<Link to="/login" className="underline text-primary">
										Login
									</Link>{" "}
									instead?
								</p>
							)}
						</FormItem>
					)}
				/>
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
										className="absolute right-2 top-1/2 -translate-y-1/2 px-1 text-muted-foreground"
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
							{password && (
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
											password={password}
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
							<FormLabel>Confirm Password</FormLabel>
							<FormControl>
								<div className="relative">
									<Input
										type={showConfirmPassword ? "text" : "password"}
										placeholder="Confirm your password"
										{...field}
									/>
									<button
										type="button"
										tabIndex={-1}
										className="absolute right-2 top-1/2 -translate-y-1/2 px-1 text-muted-foreground"
										onClick={() => setShowConfirmPassword((v) => !v)}
										aria-label={
											showConfirmPassword ? "Hide password" : "Show password"
										}
										style={{ background: "none", border: "none", margin: 0 }}
									>
										{showConfirmPassword ? (
											<EyeOff className="h-5 w-5" />
										) : (
											<Eye className="h-5 w-5" />
										)}
									</button>
								</div>
							</FormControl>
							<FormMessage />
						</FormItem>
					)}
				/>
				<FormField
					control={form.control}
					name="acceptTerms"
					render={({ field }) => (
						<FormItem>
							<div className="flex items-start gap-3">
								<FormControl>
									<Checkbox
										checked={field.value}
										onCheckedChange={field.onChange}
									/>
								</FormControl>
								<div className="grid gap-2">
									<FormLabel>Accept terms and conditions</FormLabel>
									<p className="text-muted-foreground text-sm">
										By clicking this checkbox, you agree to the terms and
										conditions.
									</p>
								</div>
							</div>
							<FormMessage />
						</FormItem>
					)}
				/>
				<Button
					type="submit"
					className="w-full"
					disabled={
						registerMutation.isPending ||
						!form.formState.isValid ||
						!passwordRequirements.every((req) => req.met) ||
						!form.watch("acceptTerms")
					}
				>
					{registerMutation.isPending ? "Signing up..." : "Sign up"}
				</Button>
			</form>
			<div className="text-center text-sm">
				Already have an account?{" "}
				<Link
					to="/login"
					className="underline underline-offset-4 hover:text-primary"
				>
					Login
				</Link>
			</div>
		</Form>
	)
}
