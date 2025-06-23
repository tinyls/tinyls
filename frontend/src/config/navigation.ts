import {
	Key,
	LayoutDashboard,
	LogOut,
	Settings,
	Trash2,
	User,
} from "lucide-react"

import type { LucideIcon } from "lucide-react"

// Types
export type SettingsPage = "profile" | "password" | "delete"

export interface NavLink {
	to: string
	label: string
	icon?: LucideIcon
	requiresAuth?: boolean
	search?: Record<string, string>
	hideInMobile?: boolean
}

export interface SettingsNavLink extends NavLink {
	id: SettingsPage
}

// Main Navigation Links
export const mainNavLinks: NavLink[] = [
	{ to: "/", label: "Home" },
	{ to: "/dashboard", label: "Dashboard", requiresAuth: true },
	{
		to: "/settings",
		label: "Settings",
		requiresAuth: true,
		hideInMobile: true,
	},
]

// Auth Navigation Links
export const authNavLinks: NavLink[] = [
	{ to: "/login", label: "Login" },
	{ to: "/register", label: "Register" },
]

// Settings Navigation Links
export const settingsNavLinks: SettingsNavLink[] = [
	{
		id: "profile",
		to: "/settings",
		label: "Profile",
		icon: User,
		search: { tab: "profile" },
	},
	{
		id: "password",
		to: "/settings",
		label: "Password",
		icon: Key,
		search: { tab: "password" },
	},
	{
		id: "delete",
		to: "/settings",
		label: "Delete Account",
		icon: Trash2,
		search: { tab: "delete" },
	},
]

// User Menu Links
export const userMenuLinks: NavLink[] = [
	{
		to: "/dashboard",
		label: "Dashboard",
		requiresAuth: true,
		icon: LayoutDashboard,
	},
	{ to: "/settings", label: "Settings", requiresAuth: true, icon: Settings },
]

// Logout Link
export const logoutLink: NavLink = {
	to: "/",
	label: "Logout",
	icon: LogOut,
	requiresAuth: true,
}
