export interface Output {
  id: number
  name: string
  ip: string
  type: OutputType
  created: number
  last_modified: number
  info: OutputInfo
  max_queue_size: number
  status: number
}

export type OutputType = 'sparrow' | 'file' | 'kafka'

export interface OutputInfo {
  [key: string]: unknown
}

export interface SparrowInfo extends OutputInfo {
  port: number
  ssl: boolean
  cert?: string
  key?: string
}

export interface FileInfo extends OutputInfo {
  path: string
  filename: string
  rotation: string
  max_size?: number
}

export interface KafkaInfo extends OutputInfo {
  brokers: string
  topic: string
  compression?: string
}

export interface OutputDto extends Output {
  currentQueueSize?: number
  clients?: SparrowClient[]
  eps?: EpsData
}

export interface SparrowClient {
  id: string
  ip: string
  connectedAt: number
  eps: number
}

export interface EpsData {
  current: number
  history: EpsHistoryVO[]
}

export interface EpsHistoryVO {
  timestamp: number
  eps: number
}

export interface OutputTemplate {
  label: string
  placeholder?: unknown
  type: 'text' | 'select' | 'number' | 'checkbox'
  options?: string[]
  info?: Record<string, OutputTemplate>
}
