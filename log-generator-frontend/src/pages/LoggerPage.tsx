import { useState } from 'react'
import { Header } from '@/components/layout/Header'
import { LoggerList } from '@/components/logger/LoggerList'
import { LoggerEditor } from '@/components/logger/LoggerEditor'
import { ImportModal } from '@/components/logger/ImportModal'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Plus, Download } from 'lucide-react'
import {
  useLoggers,
  useLogger,
  useCreateLogger,
  useUpdateLogger,
  useDeleteLogger,
  useStartLogger,
  useStopLogger,
  useGenerateSampleLogs,
  useImportFromZookeeper,
} from '@/hooks/useLoggers'
import type { LoggerDto, ImportFromModel } from '@/types/logger'

export function LoggerPage() {
  const [selectedId, setSelectedId] = useState<number | null>(null)
  const [isNew, setIsNew] = useState(false)
  const [importOpen, setImportOpen] = useState(false)
  const [sampleLogs, setSampleLogs] = useState<Record<string, unknown>[] | null>(null)

  const { data: loggers, isLoading } = useLoggers()
  const { data: selectedLogger } = useLogger(selectedId ?? 0)

  const createMutation = useCreateLogger()
  const updateMutation = useUpdateLogger()
  const deleteMutation = useDeleteLogger()
  const startMutation = useStartLogger()
  const stopMutation = useStopLogger()
  const sampleMutation = useGenerateSampleLogs()
  const importMutation = useImportFromZookeeper()

  const handleSelect = (id: number) => {
    setSelectedId(id)
    setIsNew(false)
    setSampleLogs(null)
  }

  const handleNew = () => {
    setSelectedId(null)
    setIsNew(true)
    setSampleLogs(null)
  }

  const handleSave = (logger: Partial<LoggerDto>) => {
    if (logger.id) {
      updateMutation.mutate(logger, {
        onSuccess: () => {
          setIsNew(false)
        },
      })
    } else {
      createMutation.mutate(logger, {
        onSuccess: (data) => {
          setSelectedId(data.id)
          setIsNew(false)
        },
      })
    }
  }

  const handleGenerateSample = (yaml: string) => {
    sampleMutation.mutate(yaml, {
      onSuccess: (data) => {
        setSampleLogs(data)
      },
    })
  }

  const handleDelete = (id: number) => {
    deleteMutation.mutate(id, {
      onSuccess: () => {
        if (selectedId === id) {
          setSelectedId(null)
        }
      },
    })
  }

  const handleImport = (model: ImportFromModel) => {
    importMutation.mutate(model, {
      onSuccess: (data) => {
        setImportOpen(false)
        setSelectedId(data.id)
        setIsNew(false)
      },
    })
  }

  return (
    <div className="flex h-screen flex-col">
      <Header
        title="Logger Management"
        action={
          <div className="flex gap-2">
            <Button variant="outline" size="sm" onClick={() => setImportOpen(true)}>
              <Download className="mr-2 h-4 w-4" />
              Import
            </Button>
            <Button size="sm" onClick={handleNew}>
              <Plus className="mr-2 h-4 w-4" />
              New Logger
            </Button>
          </div>
        }
      />

      <div className="flex flex-1 gap-4 overflow-hidden p-4">
        <Card className="w-80 shrink-0">
          <CardHeader className="pb-2">
            <CardTitle className="text-base">Loggers</CardTitle>
          </CardHeader>
          <CardContent className="p-0">
            <LoggerList
              loggers={loggers}
              selectedId={selectedId}
              onSelect={handleSelect}
              onStart={(id) => startMutation.mutate(id)}
              onStop={(id) => stopMutation.mutate(id)}
              onDelete={handleDelete}
              isLoading={isLoading}
            />
          </CardContent>
        </Card>

        <div className="flex-1 overflow-hidden">
          {(selectedId || isNew) ? (
            <LoggerEditor
              logger={selectedLogger ?? null}
              onSave={handleSave}
              onGenerateSample={handleGenerateSample}
              sampleLogs={sampleLogs}
              isSaving={createMutation.isPending || updateMutation.isPending}
              isGenerating={sampleMutation.isPending}
              isNew={isNew}
            />
          ) : (
            <Card className="flex h-full items-center justify-center">
              <div className="text-center text-muted-foreground">
                <p>Select a logger from the list or create a new one</p>
              </div>
            </Card>
          )}
        </div>
      </div>

      <ImportModal
        open={importOpen}
        onOpenChange={setImportOpen}
        onImport={handleImport}
        isImporting={importMutation.isPending}
      />
    </div>
  )
}
