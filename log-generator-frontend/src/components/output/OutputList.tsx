import { cn } from '@/lib/utils'
import type { OutputDto } from '@/types/output'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { ScrollArea } from '@/components/ui/scroll-area'
import { Skeleton } from '@/components/ui/skeleton'
import { Play, Square, Trash2, FileText, Send, Server } from 'lucide-react'
import { formatEps } from '@/lib/utils'
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
  AlertDialogTrigger,
} from '@/components/ui/alert-dialog'

const typeIcons = {
  sparrow: Send,
  file: FileText,
  kafka: Server,
}

interface OutputListProps {
  outputs: OutputDto[] | undefined
  selectedId: number | null
  onSelect: (id: number) => void
  onStart: (id: number) => void
  onStop: (id: number) => void
  onDelete: (id: number) => void
  isLoading: boolean
}

export function OutputList({
  outputs,
  selectedId,
  onSelect,
  onStart,
  onStop,
  onDelete,
  isLoading,
}: OutputListProps) {
  if (isLoading) {
    return (
      <div className="space-y-2 p-2">
        {[...Array(5)].map((_, i) => (
          <Skeleton key={i} className="h-20 w-full" />
        ))}
      </div>
    )
  }

  if (!outputs?.length) {
    return (
      <div className="flex h-40 items-center justify-center text-muted-foreground">
        No outputs found
      </div>
    )
  }

  return (
    <ScrollArea className="h-[calc(100vh-12rem)]">
      <div className="space-y-2 p-2">
        {outputs.map((output) => {
          const Icon = typeIcons[output.type] || Server
          return (
            <div
              key={output.id}
              className={cn(
                'group cursor-pointer rounded-lg border p-3 transition-colors',
                selectedId === output.id
                  ? 'border-primary bg-primary/10'
                  : 'hover:bg-accent'
              )}
              onClick={() => onSelect(output.id)}
            >
              <div className="flex items-start justify-between">
                <div className="space-y-1">
                  <div className="flex items-center gap-2">
                    <Icon className="h-4 w-4" />
                    <span className="font-medium">{output.name}</span>
                    <Badge variant={output.status === 1 ? 'success' : 'secondary'}>
                      {output.status === 1 ? 'Running' : 'Stopped'}
                    </Badge>
                  </div>
                  <div className="text-sm text-muted-foreground">
                    Type: {output.type} | IP: {output.ip || 'N/A'}
                  </div>
                  {output.currentQueueSize !== undefined && (
                    <div className="text-sm text-muted-foreground">
                      Queue: {output.currentQueueSize} / {output.max_queue_size}
                    </div>
                  )}
                  {output.eps?.current !== undefined && (
                    <div className="text-sm text-muted-foreground">
                      EPS: {formatEps(output.eps.current)}
                    </div>
                  )}
                </div>
                <div className="flex gap-1 opacity-0 transition-opacity group-hover:opacity-100">
                  {output.status === 1 ? (
                    <Button
                      variant="ghost"
                      size="icon"
                      onClick={(e) => {
                        e.stopPropagation()
                        onStop(output.id)
                      }}
                    >
                      <Square className="h-4 w-4" />
                    </Button>
                  ) : (
                    <Button
                      variant="ghost"
                      size="icon"
                      onClick={(e) => {
                        e.stopPropagation()
                        onStart(output.id)
                      }}
                    >
                      <Play className="h-4 w-4" />
                    </Button>
                  )}
                  <AlertDialog>
                    <AlertDialogTrigger asChild>
                      <Button
                        variant="ghost"
                        size="icon"
                        onClick={(e) => e.stopPropagation()}
                      >
                        <Trash2 className="h-4 w-4 text-destructive" />
                      </Button>
                    </AlertDialogTrigger>
                    <AlertDialogContent onClick={(e) => e.stopPropagation()}>
                      <AlertDialogHeader>
                        <AlertDialogTitle>Delete Output</AlertDialogTitle>
                        <AlertDialogDescription>
                          Are you sure you want to delete "{output.name}"? This action
                          cannot be undone.
                        </AlertDialogDescription>
                      </AlertDialogHeader>
                      <AlertDialogFooter>
                        <AlertDialogCancel>Cancel</AlertDialogCancel>
                        <AlertDialogAction
                          onClick={() => onDelete(output.id)}
                          className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
                        >
                          Delete
                        </AlertDialogAction>
                      </AlertDialogFooter>
                    </AlertDialogContent>
                  </AlertDialog>
                </div>
              </div>
            </div>
          )
        })}
      </div>
    </ScrollArea>
  )
}
