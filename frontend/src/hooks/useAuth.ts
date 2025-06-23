import type {
	AuthenticateUser200,
	LoginRequest,
	RegisterRequest,
} from "@/api/schemas"
import { handleError, parseApiError } from "@/utils/apiErrorHandler"

import { API_URL } from "@/lib/config"
import type { HTTPValidationError } from "@/api/schemas/apiError"
import { toast } from "sonner"
import { useAuthStore } from "@/store/auth"
import { useAuthenticateUser } from "@/api/client/auth-controller/auth-controller"
import { useRegisterUser } from "@/api/client/auth-controller/auth-controller"

// Helper function to open OAuth popup with proper sizing and mobile detection
function openOAuthPopup(provider: "google" | "github"): Window | null {
	const url = `${API_URL}/oauth2/authorize/${provider}`

	// Detect mobile using both user agent and pointer type
	const isMobile =
		/Mobi|Android/i.test(navigator.userAgent) ||
		window.matchMedia("(pointer:coarse)").matches

	if (isMobile) {
		// On mobile, open in a new tab
		const mobilePopup = window.open(url, "_blank")
		return mobilePopup
	}

	// Desktop: compute 80% of available size, but cap to a max
	const availW = window.screen.availWidth
	const availH = window.screen.availHeight
	const popupW = Math.min(800, Math.floor(availW * 0.8))
	const popupH = Math.min(700, Math.floor(availH * 0.8))

	// Center the popup
	const left = (availW - popupW) / 2 + window.screenX
	const top = (availH - popupH) / 2 + window.screenY

	// Configure popup features
	const opts = [
		`width=${popupW}`,
		`height=${popupH}`,
		`left=${left}`,
		`top=${top}`,
		"menubar=no",
		"toolbar=no",
		"location=no",
		"resizable=yes",
		"scrollbars=yes",
	].join(",")

	return window.open(url, "OAuth2 Login", opts)
}

// Ergonomic login hook for username/password (future extensible for OAuth2)
export function useLoginMutation(
	onSuccess?: () => void,
	onError?: (err: any) => void,
) {
	const login = useAuthStore((s) => s.login)
	const mutation = useAuthenticateUser<HTTPValidationError, unknown>({
		mutation: {
			mutationFn: async ({ data }: { data: LoginRequest }) => {
				try {
					const response: AuthenticateUser200 = await (
						await import("@/api/client/auth-controller/auth-controller")
					).authenticateUser(data)
					// Assuming the response contains a token field
					await login(response.token as string)
					return response
				} catch (err: any) {
					if (err?.response?.status === 401) {
						throw new Error("Invalid email or password")
					}
					throw err
				}
			},
			onSuccess: () => {
				if (onSuccess) onSuccess()
			},
			onError: (err: any) => {
				if (onError) {
					onError(err)
				} else {
					if (err.message === "Invalid email or password") {
						toast.error("Invalid email or password")
					} else {
						toast.error("Login failed. Please try again.")
					}
				}
			},
		},
	})

	// Ergonomic mutate function for email/password login
	const loginWithPassword = (params: {
		email: string
		password: string
	}) => {
		mutation.mutate({ data: params })
	}

	// OAuth2 login function
	const loginWithOAuth2 = (provider: "google" | "github") => {
		return new Promise<void>((resolve, reject) => {
			// 1) Listen first…
			function handleMessage(event: MessageEvent) {
				if (event.origin !== window.location.origin) return
				const token = event.data?.token as string | undefined
				if (!token) return
				window.removeEventListener("message", handleMessage)
				clearInterval(checkPopup)

				// 2) normal login(token) and resolve
				login(token)
					.then(() => resolve())
					.catch((err) => reject(err))

				// 3) close popup if still open
				if (popup && !popup.closed) popup.close()
			}
			window.addEventListener("message", handleMessage)

			// 4) open the popup with improved sizing
			const popup = openOAuthPopup(provider)

			// If we're on mobile, popup will be null and we're already redirected
			if (!popup) return

			// 5) optional: detect if user manually closed popup
			const checkPopup = setInterval(() => {
				if (!popup || popup.closed) {
					clearInterval(checkPopup)
					window.removeEventListener("message", handleMessage)
					reject(new Error("Popup closed by user"))
				}
			}, 500)
		})
	}

	return {
		...mutation,
		loginWithPassword,
		loginWithOAuth2,
	}
}

// Ergonomic register hook
export function useRegisterMutation(onSuccess?: () => void) {
	const mutation = useRegisterUser<HTTPValidationError, unknown>({
		mutation: {
			mutationFn: async ({ data }: { data: RegisterRequest }) => {
				try {
					const registeredUser = await (
						await import("@/api/client/auth-controller/auth-controller")
					).registerUser(data)
					return registeredUser
				} catch (err: any) {
					throw err
				}
			},
			onSuccess: async () => {
				await useAuthStore.getState().refreshUser()
				if (onSuccess) onSuccess()
			},
			onError: (err: any) => {
				const { status, detail } = parseApiError(err)
				if (
					status === 400 &&
					typeof detail === "string" &&
					detail.toLowerCase().includes("already exists")
				) {
					toast.error("A user with this email already exists.")
					return
				}
				handleError(err)
			},
		},
	})

	// Ergonomic mutate function for registration
	const register = (user: RegisterRequest) => {
		mutation.mutate({ data: user })
	}

	return {
		...mutation,
		register,
	}
}

export function useCurrentUser() {
	return useAuthStore((s) => s.user)
}

export function useIsLoggedIn() {
	return useAuthStore((s) => s.isLoggedIn)
}

export function useLogout() {
	return useAuthStore((s) => s.logout)
}
