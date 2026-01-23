import { useQuery } from '@tanstack/react-query'
import { getHistory } from '@/api/history'

export const historyKeys = {
  all: ['history'] as const,
  lists: () => [...historyKeys.all, 'list'] as const,
  list: (page: number) => [...historyKeys.lists(), page] as const,
}

export function useHistory(page: number) {
  return useQuery({
    queryKey: historyKeys.list(page),
    queryFn: () => getHistory(page),
  })
}
