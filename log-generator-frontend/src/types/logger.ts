export interface Logger {
  id: number
  name: string
  eps: string
  yaml_str: string
  ip: string
  created: number
  last_modified: number
  status: number
}

export interface LoggerDto extends Logger {
  detail?: LoggerDetail
  currentEps?: number
  totalCount?: number
}

export interface LoggerDetail {
  log: string
  logtype: string
  raw: boolean
  fields: FieldInfo[]
}

export interface FieldInfo {
  name: string
  type: string
  format?: string
  values?: FieldValue[]
}

export interface FieldValue {
  value: string
  weight?: number
}

export interface ImportFromModel {
  zookeeper_url: string
  engine_id: string
  ip: string
}

export interface SampleLogRequest {
  yaml_str: string
}
