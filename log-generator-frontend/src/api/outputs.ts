import { apiGet, apiPut, apiPatch, apiDelete } from './client'
import type { Output, OutputDto, OutputTemplate, EpsHistoryVO } from '@/types/output'

export async function getOutputs(): Promise<OutputDto[]> {
  return apiGet<OutputDto[]>('/outputs')
}

export async function getOutput(id: number): Promise<OutputDto> {
  return apiGet<OutputDto>(`/outputs/${id}`)
}

export async function createOutput(output: Partial<Output>): Promise<OutputDto> {
  return apiPut<OutputDto>('/outputs', output)
}

export async function updateOutput(output: Partial<Output>): Promise<OutputDto> {
  return apiPatch<OutputDto>('/outputs', output)
}

export async function deleteOutput(output: { id: number }): Promise<void> {
  return apiDelete('/outputs', output)
}

export async function startOutput(id: number): Promise<void> {
  return apiPatch('/outputs/start', { id })
}

export async function stopOutput(id: number): Promise<void> {
  return apiPatch('/outputs/stop', { id })
}

export async function stopClient(outputId: number, clientId: string): Promise<void> {
  return apiPatch(`/outputs/stop-client/${outputId}/${clientId}`)
}

export async function getProducerEps(id: number): Promise<EpsHistoryVO[]> {
  return apiGet<EpsHistoryVO[]>(`/outputs/eps/producer/${id}`)
}

export async function getProducerEpsByLogger(id: number, loggerId: number): Promise<EpsHistoryVO[]> {
  return apiGet<EpsHistoryVO[]>(`/outputs/eps/producer/${id}/${loggerId}`)
}

export async function getOutputTemplate(): Promise<Record<string, OutputTemplate>> {
  return apiGet<Record<string, OutputTemplate>>('/const/output/template')
}
