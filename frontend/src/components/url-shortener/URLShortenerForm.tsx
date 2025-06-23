import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import type React from "react"

// TODO: once the url is submitted, the input cannot be changed
export function URLShortenerForm({
	url,
	onUrlChange,
	onSubmit,
	loading,
}: {
	url: string
	onUrlChange: (v: string) => void
	onSubmit: (e: React.FormEvent) => void
	loading: boolean
}) {
	return (
		<form
			onSubmit={onSubmit}
			className="w-full flex flex-col space-y-4 md:space-y-0 md:flex-row md:space-x-2"
		>
			<Input
				type="url"
				placeholder="Enter your URL"
				value={url}
				onChange={(e) => onUrlChange(e.target.value)}
				className="flex-1"
				required
			/>
			<Button type="submit" disabled={loading}>
				{loading ? "Shortening..." : "Shorten"}
			</Button>
		</form>
	)
}
