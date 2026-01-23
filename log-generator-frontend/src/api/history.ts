import { apiGet } from './client'
import type { HistoryDto, PageResponse } from '@/types/history'

export async function getHistory(page: number): Promise<PageResponse<HistoryDto>> {
  return apiGet<PageResponse<HistoryDto>>(`/history/${page}`)
}
