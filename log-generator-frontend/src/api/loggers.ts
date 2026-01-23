import { apiGet, apiPost, apiPut, apiPatch, apiDelete } from './client'
import type { Logger, LoggerDto, ImportFromModel } from '@/types/logger'

export async function getLoggers(): Promise<LoggerDto[]> {
  const data = await apiGet<Record<string, LoggerDto>>('/loggers')
  return Object.values(data)
}

export async function getLogger(id: number): Promise<LoggerDto> {
  return apiGet<LoggerDto>(`/loggers/${id}`)
}

export async function createLogger(logger: Partial<Logger>): Promise<LoggerDto> {
  return apiPut<LoggerDto>('/loggers', logger)
}

export async function updateLogger(logger: Partial<Logger>): Promise<LoggerDto> {
  return apiPatch<LoggerDto>('/loggers', logger)
}

export async function deleteLogger(logger: { id: number }): Promise<void> {
  return apiDelete('/loggers', logger)
}

export async function startLogger(id: number): Promise<void> {
  return apiPatch('/loggers/start', { id })
}

export async function stopLogger(id: number): Promise<void> {
  return apiPatch('/loggers/stop', { id })
}

export async function generateSampleLogs(yamlStr: string): Promise<Record<string, unknown>[]> {
  return apiPost<Record<string, unknown>[]>('/loggers/sample', { id: -1, yaml_str: yamlStr })
}

export async function importFromZookeeper(model: ImportFromModel): Promise<LoggerDto> {
  return apiPost<LoggerDto>('/loggers/import', model)
}
