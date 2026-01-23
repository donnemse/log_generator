import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import {
  getLoggers,
  getLogger,
  createLogger,
  updateLogger,
  deleteLogger,
  startLogger,
  stopLogger,
  generateSampleLogs,
  importFromZookeeper,
} from '@/api/loggers'
import type { Logger, ImportFromModel } from '@/types/logger'
import { toast } from 'sonner'

export const loggerKeys = {
  all: ['loggers'] as const,
  lists: () => [...loggerKeys.all, 'list'] as const,
  list: () => [...loggerKeys.lists()] as const,
  details: () => [...loggerKeys.all, 'detail'] as const,
  detail: (id: number) => [...loggerKeys.details(), id] as const,
}

export function useLoggers() {
  return useQuery({
    queryKey: loggerKeys.list(),
    queryFn: getLoggers,
    refetchInterval: 3000,
  })
}

export function useLogger(id: number) {
  return useQuery({
    queryKey: loggerKeys.detail(id),
    queryFn: () => getLogger(id),
    enabled: id > 0,
  })
}

export function useCreateLogger() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (logger: Partial<Logger>) => createLogger(logger),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: loggerKeys.lists() })
      toast.success('Logger created successfully')
    },
    onError: (error: Error) => {
      toast.error(`Failed to create logger: ${error.message}`)
    },
  })
}

export function useUpdateLogger() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (logger: Partial<Logger>) => updateLogger(logger),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: loggerKeys.lists() })
      if (variables.id) {
        queryClient.invalidateQueries({ queryKey: loggerKeys.detail(variables.id) })
      }
      toast.success('Logger updated successfully')
    },
    onError: (error: Error) => {
      toast.error(`Failed to update logger: ${error.message}`)
    },
  })
}

export function useDeleteLogger() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (id: number) => deleteLogger({ id }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: loggerKeys.lists() })
      toast.success('Logger deleted successfully')
    },
    onError: (error: Error) => {
      toast.error(`Failed to delete logger: ${error.message}`)
    },
  })
}

export function useStartLogger() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (id: number) => startLogger(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: loggerKeys.lists() })
      toast.success('Logger started')
    },
    onError: (error: Error) => {
      toast.error(`Failed to start logger: ${error.message}`)
    },
  })
}

export function useStopLogger() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (id: number) => stopLogger(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: loggerKeys.lists() })
      toast.success('Logger stopped')
    },
    onError: (error: Error) => {
      toast.error(`Failed to stop logger: ${error.message}`)
    },
  })
}

export function useGenerateSampleLogs() {
  return useMutation({
    mutationFn: (yamlStr: string) => generateSampleLogs(yamlStr),
    onError: (error: Error) => {
      toast.error(`Failed to generate sample logs: ${error.message}`)
    },
  })
}

export function useImportFromZookeeper() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (model: ImportFromModel) => importFromZookeeper(model),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: loggerKeys.lists() })
      toast.success('Logger imported successfully')
    },
    onError: (error: Error) => {
      toast.error(`Failed to import logger: ${error.message}`)
    },
  })
}
