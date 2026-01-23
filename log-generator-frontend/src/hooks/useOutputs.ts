import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import {
  getOutputs,
  getOutput,
  createOutput,
  updateOutput,
  deleteOutput,
  startOutput,
  stopOutput,
  stopClient,
  getProducerEps,
  getOutputTemplate,
} from '@/api/outputs'
import type { Output } from '@/types/output'
import { toast } from 'sonner'

export const outputKeys = {
  all: ['outputs'] as const,
  lists: () => [...outputKeys.all, 'list'] as const,
  list: () => [...outputKeys.lists()] as const,
  details: () => [...outputKeys.all, 'detail'] as const,
  detail: (id: number) => [...outputKeys.details(), id] as const,
  eps: (id: number) => [...outputKeys.all, 'eps', id] as const,
  template: () => [...outputKeys.all, 'template'] as const,
}

export function useOutputs() {
  return useQuery({
    queryKey: outputKeys.list(),
    queryFn: getOutputs,
    refetchInterval: 3000,
  })
}

export function useOutput(id: number) {
  return useQuery({
    queryKey: outputKeys.detail(id),
    queryFn: () => getOutput(id),
    enabled: id > 0,
  })
}

export function useOutputTemplate() {
  return useQuery({
    queryKey: outputKeys.template(),
    queryFn: getOutputTemplate,
    staleTime: Infinity,
  })
}

export function useProducerEps(id: number) {
  return useQuery({
    queryKey: outputKeys.eps(id),
    queryFn: () => getProducerEps(id),
    enabled: id > 0,
    refetchInterval: 3000,
  })
}

export function useCreateOutput() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (output: Partial<Output>) => createOutput(output),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: outputKeys.lists() })
      toast.success('Output created successfully')
    },
    onError: (error: Error) => {
      toast.error(`Failed to create output: ${error.message}`)
    },
  })
}

export function useUpdateOutput() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (output: Partial<Output>) => updateOutput(output),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: outputKeys.lists() })
      if (variables.id) {
        queryClient.invalidateQueries({ queryKey: outputKeys.detail(variables.id) })
      }
      toast.success('Output updated successfully')
    },
    onError: (error: Error) => {
      toast.error(`Failed to update output: ${error.message}`)
    },
  })
}

export function useDeleteOutput() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (id: number) => deleteOutput({ id }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: outputKeys.lists() })
      toast.success('Output deleted successfully')
    },
    onError: (error: Error) => {
      toast.error(`Failed to delete output: ${error.message}`)
    },
  })
}

export function useStartOutput() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (id: number) => startOutput(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: outputKeys.lists() })
      toast.success('Output started')
    },
    onError: (error: Error) => {
      toast.error(`Failed to start output: ${error.message}`)
    },
  })
}

export function useStopOutput() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (id: number) => stopOutput(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: outputKeys.lists() })
      toast.success('Output stopped')
    },
    onError: (error: Error) => {
      toast.error(`Failed to stop output: ${error.message}`)
    },
  })
}

export function useStopClient() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({ outputId, clientId }: { outputId: number; clientId: string }) =>
      stopClient(outputId, clientId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: outputKeys.lists() })
      toast.success('Client disconnected')
    },
    onError: (error: Error) => {
      toast.error(`Failed to disconnect client: ${error.message}`)
    },
  })
}
