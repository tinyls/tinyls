import "./logo.css"

import { useEffect, useRef, useState } from "react"

interface LogoProps {
	width?: number | string
	height?: number | string
	className?: string
}

const Logo = ({ width = 192, height = 192, className = "" }: LogoProps) => {
	const [isActive, setIsActive] = useState(true)
	const timeoutRef = useRef<number | null>(null)

	useEffect(() => {
		const animateLogo = () => {
			setIsActive(false)

			// Reset animation after completion
			const resetTimeout = window.setTimeout(() => {
				setIsActive(true)
			}, 800) // Animation duration

			return resetTimeout
		}

		// Initial animation
		const initialReset = animateLogo()

		// Set up interval for recurring animation every 10 seconds
		const intervalId = window.setInterval(() => {
			if (timeoutRef.current !== null) {
				window.clearTimeout(timeoutRef.current)
			}
			timeoutRef.current = animateLogo()
		}, 10000)

		return () => {
			window.clearInterval(intervalId)
			window.clearTimeout(initialReset)
			if (timeoutRef.current !== null) {
				window.clearTimeout(timeoutRef.current)
			}
		}
	}, [])

	return (
		<div className={`logo-container ${className}`} style={{ width, height }}>
			<svg
				xmlns="http://www.w3.org/2000/svg"
				width="100%"
				height="100%"
				fill="none"
				stroke="currentColor"
				strokeWidth="2"
				strokeLinecap="round"
				strokeLinejoin="round"
				className={`lucide lucide-link-icon lucide-link ${isActive ? "active" : ""}`}
				viewBox="0 0 192 192"
			>
				<defs>
					<linearGradient
						gradientUnits="userSpaceOnUse"
						x1="8.028"
						y1="8.995"
						x2="8.028"
						y2="21.934"
						id="gradient-1"
						gradientTransform="matrix(8.002409, 0, 0, 8.002409, -0.028872, -0.035379)"
					>
						<stop
							offset="0"
							style={{ stopColor: "color(a98-rgb 0.375 0 0.533)" }}
						></stop>
						<stop
							offset="1"
							style={{ stopColor: "color(a98-rgb 0.269 0 0.40863)" }}
						></stop>
					</linearGradient>
				</defs>
				<path
					d="M 79.995 103.995 C 94.594 123.512 123.102 125.553 140.333 108.316 L 164.341 84.31 C 185.738 62.155 175.129 25.143 145.244 17.689 C 131.855 14.351 117.689 18.146 107.763 27.732 L 94 41.416"
					style={{
						paintOrder: "fill",
						strokeWidth: "16px",
						stroke: "color(a98-rgb 0.375 0 0.533)",
					}}
					className="svg-link-1"
				></path>
				<path
					d="M 112.005 87.991 C 97.406 68.473 68.898 66.432 51.667 83.669 L 27.659 107.676 C 6.262 129.831 16.87 166.842 46.756 174.296 C 60.146 177.635 74.311 173.839 84.237 164.253 L 97.921 150.57"
					style={{
						paintOrder: "fill",
						stroke: "url(#gradient-1)",
						strokeWidth: "16px",
					}}
					className="svg-link-2"
				></path>
			</svg>
		</div>
	)
}

export default Logo
