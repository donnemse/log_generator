import type { SparrowClient } from '@/types/output'
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'
import { Button } from '@/components/ui/button'
import { formatDate, formatEps } from '@/lib/utils'
import { XCircle } from 'lucide-react'
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

interface ClientTableProps {
  clients: SparrowClient[] | undefined
  onDisconnect: (clientId: string) => void
}

export function ClientTable({ clients, onDisconnect }: ClientTableProps) {
  if (!clients?.length) {
    return (
      <div className="flex h-24 items-center justify-center text-muted-foreground border rounded-md">
        No clients connected
      </div>
    )
  }

  return (
    <Table>
      <TableHeader>
        <TableRow>
          <TableHead>Client ID</TableHead>
          <TableHead>IP</TableHead>
          <TableHead>Connected At</TableHead>
          <TableHead>EPS</TableHead>
          <TableHead className="w-20">Actions</TableHead>
        </TableRow>
      </TableHeader>
      <TableBody>
        {clients.map((client) => (
          <TableRow key={client.id}>
            <TableCell className="font-mono text-sm">{client.id}</TableCell>
            <TableCell>{client.ip}</TableCell>
            <TableCell>{formatDate(client.connectedAt)}</TableCell>
            <TableCell>{formatEps(client.eps)}</TableCell>
            <TableCell>
              <AlertDialog>
                <AlertDialogTrigger asChild>
                  <Button variant="ghost" size="icon">
                    <XCircle className="h-4 w-4 text-destructive" />
                  </Button>
                </AlertDialogTrigger>
                <AlertDialogContent>
                  <AlertDialogHeader>
                    <AlertDialogTitle>Disconnect Client</AlertDialogTitle>
                    <AlertDialogDescription>
                      Are you sure you want to disconnect client "{client.id}"?
                    </AlertDialogDescription>
                  </AlertDialogHeader>
                  <AlertDialogFooter>
                    <AlertDialogCancel>Cancel</AlertDialogCancel>
                    <AlertDialogAction
                      onClick={() => onDisconnect(client.id)}
                      className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
                    >
                      Disconnect
                    </AlertDialogAction>
                  </AlertDialogFooter>
                </AlertDialogContent>
              </AlertDialog>
            </TableCell>
          </TableRow>
        ))}
      </TableBody>
    </Table>
  )
}
