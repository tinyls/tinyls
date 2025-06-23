import * as z from "zod"

import {
	Card,
	CardContent,
	CardDescription,
	CardFooter,
	CardHeader,
	CardTitle,
} from "@/components/ui/card"
import {
	Form,
	FormControl,
	FormField,
	FormItem,
	FormLabel,
	FormMessage,
} from "@/components/ui/form"
import {
	Tooltip,
	TooltipContent,
	TooltipProvider,
	TooltipTrigger,
} from "@/components/ui/tooltip"
import {
	useCurrentUserQuery,
	useUpdateProfileMutation,
} from "@/hooks/useUserSettings"

import type { ProfileUpdateRequest } from "@/api/schemas"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { zodResolver } from "@hookform/resolvers/zod"
import { Info } from "lucide-react"
import { useEffect } from "react"
import { useForm } from "react-hook-form"

const formSchema = z.object({
	name: z.string().min(2, {
		message: "Name must be at least 2 characters",
	}),
	email: z.string().email({
		message: "Please enter a valid email address",
	}),
})

export default function ProfileSettings() {
	const userQuery = useCurrentUserQuery()
	const updateProfile = useUpdateProfileMutation(userQuery)

	const form = useForm<z.infer<typeof formSchema>>({
		resolver: zodResolver(formSchema),
		defaultValues: {
			name: "",
			email: "",
		},
		mode: "onTouched",
		reValidateMode: "onChange",
	})

	useEffect(() => {
		if (userQuery.data) {
			form.reset({
				name: userQuery.data.name ?? "",
				email: userQuery.data.email ?? "",
			})
		}
	}, [userQuery.data, form])

	if (userQuery.isLoading) {
		return <div className="py-10 text-center">Loading...</div>
	}
	if (userQuery.isError || !userQuery.data) {
		return (
			<div className="py-10 text-center text-red-500">
				Failed to load user data.
			</div>
		)
	}

	const onSubmit = (values: z.infer<typeof formSchema>) => {
		// Always send both name and email, but only allow editing email for LOCAL users
		const isLocal = userQuery.data.provider === "LOCAL"
		const data: ProfileUpdateRequest = {
			name: values.name,
			email: isLocal ? values.email : (userQuery.data.email ?? ""),
		}
		updateProfile.mutate(
			{ data },
			{
				onError: (error: any) => {
					if (
						error?.response?.status === 400 &&
						error?.response?.data?.detail
							?.toLowerCase()
							.includes("already exists")
					) {
						form.setError("email", {
							type: "manual",
							message: "A user with this email already exists.",
						})
					} else {
						form.setError("root", {
							type: "manual",
							message: "Failed to update profile. Please try again.",
						})
					}
				},
			},
		)
	}

	return (
		<Card>
			<CardHeader>
				<CardTitle>Profile</CardTitle>
				<CardDescription>
					Make changes to your profile here. Click save when you're done.
				</CardDescription>
			</CardHeader>
			<CardContent className="space-y-4">
				<Form {...form}>
					<form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
						<FormField
							control={form.control}
							name="name"
							render={({ field }) => (
								<FormItem>
									<div className="flex items-center gap-2">
										<FormLabel>Full Name</FormLabel>
										{userQuery.data.provider !== "LOCAL" && (
											<TooltipProvider>
												<Tooltip>
													<TooltipTrigger asChild>
														<Info className="h-4 w-4 text-muted-foreground" />
													</TooltipTrigger>
													<TooltipContent>
														<p>
															Changing your name here will not affect your name
															in your {userQuery.data.provider} account.
														</p>
													</TooltipContent>
												</Tooltip>
											</TooltipProvider>
										)}
									</div>
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
									<div className="flex items-center gap-2">
										<FormLabel>Email</FormLabel>
										{userQuery.data.provider !== "LOCAL" && (
											<TooltipProvider>
												<Tooltip>
													<TooltipTrigger asChild>
														<Info className="h-4 w-4 text-muted-foreground" />
													</TooltipTrigger>
													<TooltipContent>
														<p>
															Your email is managed by your{" "}
															{userQuery.data.provider} account and cannot be
															changed here.
														</p>
													</TooltipContent>
												</Tooltip>
											</TooltipProvider>
										)}
									</div>
									<FormControl>
										{userQuery.data.provider === "LOCAL" ? (
											<Input type="email" placeholder="Your email" {...field} />
										) : (
											<div className="font-mono text-sm py-2 px-3 border rounded-md bg-muted text-muted-foreground select-text">
												{field.value}
											</div>
										)}
									</FormControl>
									<FormMessage />
								</FormItem>
							)}
						/>
						{form.formState.errors.root && (
							<p className="text-sm text-red-500">
								{form.formState.errors.root.message}
							</p>
						)}
					</form>
				</Form>
			</CardContent>
			<CardFooter>
				<Button
					type="submit"
					disabled={updateProfile.isPending || !form.formState.isValid}
				>
					{updateProfile.isPending ? "Saving..." : "Save Changes"}
				</Button>
			</CardFooter>
		</Card>
	)
}
