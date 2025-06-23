import { API_URL } from "@/lib/config"
import Axios from "axios"
import type { AxiosRequestConfig, AxiosError } from "axios"

export const AXIOS_INSTANCE = Axios.create({
	baseURL: API_URL,
	// ...other default configs
})

AXIOS_INSTANCE.interceptors.request.use((config) => {
	const token = localStorage.getItem("access_token")
	if (token) {
		config.headers.Authorization = `Bearer ${token}`
	}
	return config
})

// Global error handling
AXIOS_INSTANCE.interceptors.response.use(
	(response) => response,
	(error) => {
		if (error.response?.status === 401) {
			// handle unauthorized, e.g., redirect to login
		}
		return Promise.reject(error)
	},
)

export const customInstance = <T>(
	config: AxiosRequestConfig,
	options?: AxiosRequestConfig,
): Promise<T> => {
	const source = Axios.CancelToken.source()
	const promise = AXIOS_INSTANCE({
		...config,
		...options,
		cancelToken: source.token,
	}).then(({ data }) => data)

	// @ts-ignore
	promise.cancel = () => {
		source.cancel("Query was cancelled")
	}

	return promise
}

export type ErrorType<Error> = AxiosError<Error>
export type BodyType<BodyData> = BodyData
