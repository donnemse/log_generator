import { useState } from 'react'
import { Header } from '@/components/layout/Header'
import { HistoryTable } from '@/components/history/HistoryTable'
import { DiffModal } from '@/components/history/DiffModal'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Skeleton } from '@/components/ui/skeleton'
import { useHistory } from '@/hooks/useHistory'
import { useLogger } from '@/hooks/useLoggers'
import type { HistoryDto } from '@/types/history'

export function HistoryPage() {
  const [page, setPage] = useState(0)
  const [selectedHistory, setSelectedHistory] = useState<HistoryDto | null>(null)
  const [diffOpen, setDiffOpen] = useState(false)

  const { data: historyData, isLoading } = useHistory(page)
  const { data: currentLogger } = useLogger(selectedHistory?.logger_id ?? 0)

  const handleViewDiff = (history: HistoryDto) => {
    setSelectedHistory(history)
    setDiffOpen(true)
  }

  return (
    <div className="flex h-screen flex-col">
      <Header title="History" />

      <div className="flex-1 overflow-auto p-4">
        <Card>
          <CardHeader>
            <CardTitle>Change History</CardTitle>
          </CardHeader>
          <CardContent>
            {isLoading ? (
              <div className="space-y-2">
                {[...Array(10)].map((_, i) => (
                  <Skeleton key={i} className="h-12 w-full" />
                ))}
              </div>
            ) : historyData ? (
              <HistoryTable
                data={historyData.content}
                pageIndex={page}
                pageCount={historyData.totalPages}
                onPageChange={setPage}
                onViewDiff={handleViewDiff}
              />
            ) : (
              <div className="flex h-40 items-center justify-center text-muted-foreground">
                No history records found
              </div>
            )}
          </CardContent>
        </Card>
      </div>

      <DiffModal
        open={diffOpen}
        onOpenChange={setDiffOpen}
        history={selectedHistory}
        currentYaml={currentLogger?.yaml_str ?? null}
      />
    </div>
  )
}
