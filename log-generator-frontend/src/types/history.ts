export interface History {
  id: number
  logger_id: number
  logger_name: string
  yaml_str: string
  action: HistoryAction
  created: number
}

export type HistoryAction = 'CREATE' | 'UPDATE' | 'DELETE' | 'START' | 'STOP'

export interface HistoryDto extends History {
  formattedDate?: string
}

export interface PageResponse<T> {
  content: T[]
  totalPages: number
  totalElements: number
  size: number
  number: number
  first: boolean
  last: boolean
  empty: boolean
}
