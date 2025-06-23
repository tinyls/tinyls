import { Card, CardContent, CardFooter, CardHeader } from "@/components/ui/card"
import { Copy, ExternalLink, QrCode, Share2 } from "lucide-react"
import {
	Dialog,
	DialogContent,
	DialogDescription,
	DialogHeader,
	DialogTitle,
} from "@/components/ui/dialog"

import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { QRCode } from "@/components/ui/qr-code"
import { useState } from "react"

function ShortenedUrlBlock({ shortenedUrl }: { shortenedUrl: string }) {
	return (
		<div className="space-y-4 w-full">
			<div className="flex items-center space-x-2">
				<Input value={shortenedUrl} readOnly className="font-medium flex-1" />
			</div>
		</div>
	)
}

// TODO: fix the share functionality
// TODO: toggling QR code is making the whole page sizing change
export function ShortenedUrlResult({
	shortenedUrl,
	onCopy,
	showQr = true,
}: {
	shortenedUrl: string
	onCopy: () => void
	showQr?: boolean
	qrPosition?: "right" | "bottom"
}) {
	const [qrVisible, setQrVisible] = useState(showQr)
	const [shareOpen, setShareOpen] = useState(false)

	// Share handlers
	const shareUrl = encodeURIComponent(shortenedUrl)
	const shareText = encodeURIComponent(
		`Check out this short URL: ${shortenedUrl}`,
	)
	const whatsappUrl = `https://wa.me/?text=${shareText}`
	const instagramUrl = `https://www.instagram.com/?url=${shareUrl}`
	const mailtoUrl = `mailto:?subject=Check%20out%20this%20short%20URL&body=${shareText}`

	const handleShare = () => {
		if (navigator.share) {
			navigator.share({ url: shortenedUrl, text: "Check out this short URL!" })
		} else {
			setShareOpen(true)
		}
	}

	return (
		<>
			<Card className="overflow-hidden">
				<CardHeader>
					<h3 className="text-lg font-medium">Your shortened URL</h3>
				</CardHeader>
				<CardContent>
					<div>
						<div>
							<ShortenedUrlBlock shortenedUrl={shortenedUrl} />
						</div>
					</div>
				</CardContent>
				<CardFooter>
					<div className="flex flex-row items-start gap-2 overflow-x-auto">
						<Button
							size="sm"
							variant="default"
							onClick={onCopy}
							title="Copy to clipboard"
						>
							<Copy className="h-4 w-4" /> Copy
						</Button>
						<Button
							size="sm"
							variant={qrVisible ? "secondary" : "outline"}
							onClick={() => setQrVisible((v) => !v)}
							title="Show QR code"
							aria-pressed={qrVisible}
							type="button"
						>
							<QrCode className="h-4 w-4" /> QR Code
						</Button>
						<Button
							size="sm"
							variant="outline"
							onClick={handleShare}
							title="Share"
							type="button"
						>
							<Share2 className="h-4 w-4" /> Share
						</Button>
						<Button size="sm" variant="outline" asChild title="Visit URL">
							<a
								href={shortenedUrl}
								target="_blank"
								rel="noopener noreferrer"
								className="flex items-center gap-1"
							>
								<ExternalLink className="h-4 w-4" /> Visit
							</a>
						</Button>
					</div>
				</CardFooter>
			</Card>
			{qrVisible && (
				<Card className="overflow-hidden transition-all duration-300">
					<CardHeader>
						<h3 className="text-lg font-medium">
							QR Code for your shortened URL
						</h3>
					</CardHeader>
					<CardContent>
						<div className="flex flex-col items-center justify-center gap-4">
							<QRCode size={128} value={shortenedUrl} />
						</div>
					</CardContent>
				</Card>
			)}

			<Dialog open={shareOpen} onOpenChange={setShareOpen}>
				<DialogContent>
					<DialogHeader>
						<DialogTitle>Share this URL</DialogTitle>
						<DialogDescription>Share your shortened URL via:</DialogDescription>
					</DialogHeader>
					<div className="flex flex-col gap-4 py-2">
						<a
							href={whatsappUrl}
							target="_blank"
							rel="noopener noreferrer"
							className="w-full"
						>
							<Button variant="outline" className="w-full">
								WhatsApp
							</Button>
						</a>
						<a
							href={instagramUrl}
							target="_blank"
							rel="noopener noreferrer"
							className="w-full"
						>
							<Button variant="outline" className="w-full">
								Instagram
							</Button>
						</a>
						<a href={mailtoUrl} className="w-full">
							<Button variant="outline" className="w-full">
								Email
							</Button>
						</a>
					</div>
				</DialogContent>
			</Dialog>
		</>
	)
}
