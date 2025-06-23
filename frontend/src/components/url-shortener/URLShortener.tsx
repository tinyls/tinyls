import { useEffect, useState } from "react"

import { ArrowRight } from "lucide-react"
import { Button } from "../ui/button"
import { Link } from "@tanstack/react-router"
import React from "react"
import { ShortenedUrlResult } from "./ShortenedUrlResult"
import { URLShortenerForm } from "./URLShortenerForm"
import { toast } from "sonner"
import { useAuthStore } from "@/store/auth"
import { useUrlShortenerMutation } from "@/hooks/useUrlShortener"

export default function URLShortener({
	showQr = true,
	onSuccess,
}: {
	showQr?: boolean
	onSuccess?: (shortUrl: string, data: any) => void
	qrPosition?: "right" | "bottom"
} = {}) {
	const [url, setUrl] = useState("")
	const [shortenedUrl, setShortenedUrl] = useState("")
	const { shortenUrl, isPending, data } = useUrlShortenerMutation()

	const isLoggedIn = useAuthStore((s) => s.isLoggedIn)

	const handleSubmit = (e: React.FormEvent) => {
		e.preventDefault()
		if (!url) {
			toast.error("Please enter a URL")
			return
		}
		shortenUrl(url)
	}

	// Handle mutation success
	useEffect(() => {
		if (data?.shortCode) {
			const shortUrl = `${window.location.origin}/${data.shortCode}`
			setShortenedUrl(shortUrl)
			onSuccess?.(shortUrl, data)
		}
	}, [data, onSuccess])

	return (
		<div className="w-full space-y-4">
			<URLShortenerForm
				url={url}
				onUrlChange={setUrl}
				onSubmit={handleSubmit}
				loading={isPending}
			/>
			{shortenedUrl && (
				<ShortenedUrlResult
					shortenedUrl={shortenedUrl}
					onCopy={() => {
						navigator.clipboard.writeText(shortenedUrl)
						toast.success("Copied to clipboard")
					}}
					showQr={showQr}
				/>
			)}
			{shortenedUrl && !isLoggedIn && (
				<div className="py-6">
					<h3 className="text-start mb-1 text-xl md:text-2xl font-semibold">
						Want to manage your links?
					</h3>
					<p className="text-start mb-3 text-muted-foreground">
						Create an account to track, analyze, and manage all your shortened
						URLs in one place.
					</p>
					<div className="flex flex-col space-y-2 justify-start">
						<Button asChild className="w-min">
							<Link to="/register">
								Create Account <ArrowRight className="ml-2 h-4 w-4" />
							</Link>
						</Button>
						<p className="text-sm text-muted-foreground text-start">
							Already have an account?{" "}
							<Link to="/login" className="underline">
								Login
							</Link>
						</p>
					</div>
				</div>
			)}
		</div>
	)
}
