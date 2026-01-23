import { cn } from '@/lib/utils'
import type { LoggerDto } from '@/types/logger'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { ScrollArea } from '@/components/ui/scroll-area'
import { Skeleton } from '@/components/ui/skeleton'
import { Play, Square, Trash2 } from 'lucide-react'
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

interface LoggerListProps {
  loggers: LoggerDto[] | undefined
  selectedId: number | null
  onSelect: (id: number) => void
  onStart: (id: number) => void
  onStop: (id: number) => void
  onDelete: (id: number) => void
  isLoading: boolean
}

export function LoggerList({
  loggers,
  selectedId,
  onSelect,
  onStart,
  onStop,
  onDelete,
  isLoading,
}: LoggerListProps) {
  if (isLoading) {
    return (
      <div className="space-y-2 p-2">
        {[...Array(5)].map((_, i) => (
          <Skeleton key={i} className="h-20 w-full" />
        ))}
      </div>
    )
  }

  if (!loggers?.length) {
    return (
      <div className="flex h-40 items-center justify-center text-muted-foreground">
        No loggers found
      </div>
    )
  }

  return (
    <ScrollArea className="h-[calc(100vh-12rem)]">
      <div className="space-y-2 p-2">
        {loggers.map((logger) => (
          <div
            key={logger.id}
            className={cn(
              'group cursor-pointer rounded-lg border p-3 transition-colors',
              selectedId === logger.id
                ? 'border-primary bg-primary/10'
                : 'hover:bg-accent'
            )}
            onClick={() => onSelect(logger.id)}
          >
            <div className="flex items-start justify-between">
              <div className="space-y-1">
                <div className="flex items-center gap-2">
                  <span className="font-medium">{logger.name}</span>
                  <Badge variant={logger.status === 1 ? 'success' : 'secondary'}>
                    {logger.status === 1 ? 'Running' : 'Stopped'}
                  </Badge>
                </div>
                <div className="text-sm text-muted-foreground">
                  EPS: {logger.eps} | IP: {logger.ip || 'N/A'}
                </div>
                {logger.currentEps !== undefined && (
                  <div className="text-sm text-muted-foreground">
                    Current EPS: {formatEps(logger.currentEps)}
                  </div>
                )}
              </div>
              <div className="flex gap-1 opacity-0 transition-opacity group-hover:opacity-100">
                {logger.status === 1 ? (
                  <Button
                    variant="ghost"
                    size="icon"
                    onClick={(e) => {
                      e.stopPropagation()
                      onStop(logger.id)
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
                      onStart(logger.id)
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
                      <AlertDialogTitle>Delete Logger</AlertDialogTitle>
                      <AlertDialogDescription>
                        Are you sure you want to delete "{logger.name}"? This action
                        cannot be undone.
                      </AlertDialogDescription>
                    </AlertDialogHeader>
                    <AlertDialogFooter>
                      <AlertDialogCancel>Cancel</AlertDialogCancel>
                      <AlertDialogAction
                        onClick={() => onDelete(logger.id)}
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
        ))}
      </div>
    </ScrollArea>
  )
}
