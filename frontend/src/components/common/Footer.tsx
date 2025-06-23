import { SOCIAL_GITHUB_URL, SOCIAL_INSTAGRAM_URL, SOCIAL_LINKEDIN_URL, SOCIAL_PERSONAL_URL } from "@/lib/config"

import { Button } from "../ui/button"
import GithubIcon from "../icons/github-icon"
import InstagramIcon from "../icons/instagram-icon"
import LinkedInIcon from "../icons/linkedin-icon"
import { useMediaQuery } from "@/hooks/use-media-query"

const Footer = () => {
	const isMobile = !useMediaQuery("(min-width: 768px)")

	if (isMobile) {
		return (
			<footer className="px-5 md:px-0 flex justify-center border-t py-4 font-mono tracking-tight">
				<div className="container flex flex-col items-start md:items-center justify-between gap-4 md:flex-row h-5">
					<p className="text-center text-sm text-muted-foreground">
						&copy; {new Date().getFullYear()} tinyls.com. All rights reserved.
					</p>
				</div>
			</footer>
		)
	}

	return (
		<footer className="px-5 md:px-0 flex justify-center border-t py-4 font-mono tracking-tight">
			<div className="container flex flex-col md:flex-row items-start md:items-center justify-between gap-2 md:gap-4 h-[52px] md:h-5">
				<p className="text-sm text-muted-foreground h-5">
					&copy; {new Date().getFullYear()} tinyls.com | All rights reserved.
				</p>
				<div className="flex flex-row items-center gap-2 h-5 self-end md:self-auto">
					<p className="text-sm text-muted-foreground">
						Built with ❤️ by{" "}
						<a href={SOCIAL_PERSONAL_URL} className="hover:underline" target="_blank" rel="noopener noreferrer">
							mounish
						</a>
					</p>
					<div className="flex items-center gap-1">
						<Button variant="ghost" size="icon" asChild>
							<a href={SOCIAL_GITHUB_URL} target="_blank" rel="noopener noreferrer">
								<GithubIcon className="h-4 w-4" />
							</a>
						</Button>
						<Button variant="ghost" size="icon" asChild>
							<a href={SOCIAL_LINKEDIN_URL} target="_blank" rel="noopener noreferrer">
								<LinkedInIcon className="h-4 w-4" />
							</a>
						</Button>
						<Button variant="ghost" size="icon" asChild>
							<a href={SOCIAL_INSTAGRAM_URL} target="_blank" rel="noopener noreferrer">
								<InstagramIcon className="h-4 w-4" />
							</a>
						</Button>
					</div>
				</div>
			</div>
		</footer>
	)
}

export default Footer
