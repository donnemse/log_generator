import type { HistoryDto } from '@/types/history'
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'
import { DiffViewer, CodeEditor } from '@/components/ui/code-editor'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { Badge } from '@/components/ui/badge'
import { formatDate } from '@/lib/utils'

interface DiffModalProps {
  open: boolean
  onOpenChange: (open: boolean) => void
  history: HistoryDto | null
  currentYaml: string | null
}

export function DiffModal({
  open,
  onOpenChange,
  history,
  currentYaml,
}: DiffModalProps) {
  if (!history) return null

  const showDiff = currentYaml && history.yaml_str && currentYaml !== history.yaml_str

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-4xl max-h-[90vh]">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2">
            History - {history.logger_name}
            <Badge variant="outline">{history.action}</Badge>
          </DialogTitle>
          <div className="text-sm text-muted-foreground">
            {formatDate(history.created)}
          </div>
        </DialogHeader>

        <Tabs defaultValue={showDiff ? 'diff' : 'yaml'}>
          <TabsList>
            <TabsTrigger value="yaml">YAML</TabsTrigger>
            {showDiff && <TabsTrigger value="diff">Diff</TabsTrigger>}
          </TabsList>

          <TabsContent value="yaml" className="mt-4">
            <div className="border rounded-md">
              <CodeEditor
                value={history.yaml_str || '(No YAML content)'}
                language="yaml"
                readOnly
                height="60vh"
              />
            </div>
          </TabsContent>

          {showDiff && (
            <TabsContent value="diff" className="mt-4">
              <div className="border rounded-md">
                <div className="flex border-b text-sm">
                  <div className="flex-1 p-2 bg-destructive/10 text-center">
                    History (Old)
                  </div>
                  <div className="flex-1 p-2 bg-[hsl(var(--success))]/10 text-center">
                    Current (New)
                  </div>
                </div>
                <DiffViewer
                  original={history.yaml_str}
                  modified={currentYaml}
                  language="yaml"
                  height="55vh"
                />
              </div>
            </TabsContent>
          )}
        </Tabs>
      </DialogContent>
    </Dialog>
  )
}
