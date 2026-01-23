import { useState } from 'react'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { RefreshCw } from 'lucide-react'
import type { ImportFromModel } from '@/types/logger'

interface ImportModalProps {
  open: boolean
  onOpenChange: (open: boolean) => void
  onImport: (model: ImportFromModel) => void
  isImporting: boolean
}

export function ImportModal({
  open,
  onOpenChange,
  onImport,
  isImporting,
}: ImportModalProps) {
  const [zookeeperUrl, setZookeeperUrl] = useState('')
  const [engineId, setEngineId] = useState('')
  const [ip, setIp] = useState('')

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    onImport({
      zookeeper_url: zookeeperUrl,
      engine_id: engineId,
      ip,
    })
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-[425px]">
        <form onSubmit={handleSubmit}>
          <DialogHeader>
            <DialogTitle>Import from Zookeeper</DialogTitle>
            <DialogDescription>
              Import logger configuration from a Sparrow engine model in Zookeeper.
            </DialogDescription>
          </DialogHeader>
          <div className="grid gap-4 py-4">
            <div className="space-y-2">
              <Label htmlFor="zookeeper-url">Zookeeper URL</Label>
              <Input
                id="zookeeper-url"
                value={zookeeperUrl}
                onChange={(e) => setZookeeperUrl(e.target.value)}
                placeholder="localhost:2181"
                required
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="engine-id">Engine ID</Label>
              <Input
                id="engine-id"
                value={engineId}
                onChange={(e) => setEngineId(e.target.value)}
                placeholder="engine-001"
                required
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="import-ip">IP Address</Label>
              <Input
                id="import-ip"
                value={ip}
                onChange={(e) => setIp(e.target.value)}
                placeholder="192.168.1.100"
              />
            </div>
          </div>
          <DialogFooter>
            <Button
              type="button"
              variant="outline"
              onClick={() => onOpenChange(false)}
            >
              Cancel
            </Button>
            <Button type="submit" disabled={isImporting}>
              {isImporting && <RefreshCw className="mr-2 h-4 w-4 animate-spin" />}
              Import
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}
