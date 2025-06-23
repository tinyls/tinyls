import URLShortener from "@/components/url-shortener/URLShortener"
import { createFileRoute } from "@tanstack/react-router"

export const Route = createFileRoute("/")({
	component: App,
})

// TODO: sometimes when the backend is not available and user already signed in before, the usernav shows user avatar and logout but not the name.
function App() {
	return (
		<div
			className="flex flex-col items-center justify-center"
			style={{ minHeight: "inherit" }}
		>
			<div className="container max-w-4xl px-5 md:px-4 py-16 md:py-24 lg:py-32">
				<div className="flex flex-col items-center text-center">
					<h1 className="text-3xl font-bold tracking-tighter sm:text-4xl md:text-5xl lg:text-6xl">
						Shorten Your Links
					</h1>
					<p className="mx-auto mt-4 max-w-[700px] text-lg text-muted-foreground md:text-xl">
						Create short, memorable links and QR codes in seconds.
					</p>
					<div className="mt-8 md:mt-12 w-full max-w-2xl">
						<URLShortener showQr={false} />
					</div>
				</div>
			</div>
		</div>
	)
}
