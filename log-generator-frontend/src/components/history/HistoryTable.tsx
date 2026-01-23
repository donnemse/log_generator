import { type ColumnDef } from '@tanstack/react-table'
import type { HistoryDto } from '@/types/history'
import { DataTable } from '@/components/ui/data-table'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { formatDate } from '@/lib/utils'
import { Eye } from 'lucide-react'

const actionColors: Record<string, 'default' | 'success' | 'warning' | 'destructive'> = {
  CREATE: 'success',
  UPDATE: 'default',
  DELETE: 'destructive',
  START: 'success',
  STOP: 'warning',
}

interface HistoryTableProps {
  data: HistoryDto[]
  pageIndex: number
  pageCount: number
  onPageChange: (pageIndex: number) => void
  onViewDiff: (history: HistoryDto) => void
}

export function HistoryTable({
  data,
  pageIndex,
  pageCount,
  onPageChange,
  onViewDiff,
}: HistoryTableProps) {
  const columns: ColumnDef<HistoryDto>[] = [
    {
      accessorKey: 'id',
      header: 'ID',
      cell: ({ row }) => <span className="font-mono">{row.original.id}</span>,
    },
    {
      accessorKey: 'logger_name',
      header: 'Logger',
    },
    {
      accessorKey: 'action',
      header: 'Action',
      cell: ({ row }) => (
        <Badge variant={actionColors[row.original.action] || 'default'}>
          {row.original.action}
        </Badge>
      ),
    },
    {
      accessorKey: 'created',
      header: 'Date',
      cell: ({ row }) => formatDate(row.original.created),
    },
    {
      id: 'actions',
      header: 'Actions',
      cell: ({ row }) => (
        <Button
          variant="ghost"
          size="sm"
          onClick={() => onViewDiff(row.original)}
          disabled={!row.original.yaml_str}
        >
          <Eye className="mr-2 h-4 w-4" />
          View
        </Button>
      ),
    },
  ]

  return (
    <DataTable
      columns={columns}
      data={data}
      pagination={{ pageIndex, pageSize: 20 }}
      pageCount={pageCount}
      onPaginationChange={(pagination) => {
        if (pagination.pageIndex !== pageIndex) {
          onPageChange(pagination.pageIndex)
        }
      }}
      manualPagination
    />
  )
}
