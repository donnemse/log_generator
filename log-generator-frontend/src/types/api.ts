export interface ApiResponse<T = unknown> {
  status: number
  msg: string
  data: T
}
