export interface ApiError {
	status: string
	code: string
	message: string
	debugMessage: string
	timestamp: string
	subErrors?: ApiSubError[]
}

export interface ApiSubError {
	object: string
	field: string
	rejectedValue: any
	message: string
}

export type HTTPValidationError = ApiError
