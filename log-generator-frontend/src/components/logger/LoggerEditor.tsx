import { useState, useEffect, useMemo } from 'react'
import type { LoggerDto } from '@/types/logger'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { CodeEditor } from '@/components/ui/code-editor'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { ScrollArea } from '@/components/ui/scroll-area'
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'
import { Play, Save, RefreshCw } from 'lucide-react'

const DEFAULT_YAML = `log:
  - '[<timestamp:yyyy-MM-dd HH:mm:ss>]'
  - ' '
  - <srcip>
  - ' '
  - <msg>
logtype: sample
raw: false
fields:
  srcip:
    type: ipv4
    format: public
  msg:
    type: pick
    values:
      - value: "Hello World"
        weight: 1
      - value: "Test Message"
        weight: 1
`

interface LoggerEditorProps {
  logger: LoggerDto | null
  onSave: (logger: Partial<LoggerDto>) => void
  onGenerateSample: (yaml: string) => void
  sampleLogs: Record<string, unknown>[] | null
  isSaving: boolean
  isGenerating: boolean
  isNew?: boolean
}

export function LoggerEditor({
  logger,
  onSave,
  onGenerateSample,
  sampleLogs,
  isSaving,
  isGenerating,
  isNew = false,
}: LoggerEditorProps) {
  const [name, setName] = useState('')
  const [eps, setEps] = useState('100')
  const [ip, setIp] = useState('')
  const [yamlStr, setYamlStr] = useState(DEFAULT_YAML)
  const [activeTab, setActiveTab] = useState('editor')

  useEffect(() => {
    if (logger) {
      setName(logger.name || '')
      setEps(logger.eps || '100')
      setIp(logger.ip || '')
      setYamlStr(logger.yaml_str || DEFAULT_YAML)
    } else if (isNew) {
      setName('')
      setEps('100')
      setIp('')
      setYamlStr(DEFAULT_YAML)
    }
  }, [logger, isNew])

  const columns = useMemo(() => {
    if (!sampleLogs?.length) return []
    const allKeys = new Set<string>()
    sampleLogs.forEach((log) => {
      Object.keys(log).forEach((key) => allKeys.add(key))
    })
    return Array.from(allKeys)
  }, [sampleLogs])

  const handleSave = () => {
    const data: Partial<LoggerDto> = {
      name,
      eps,
      ip,
      yaml_str: yamlStr,
    }
    if (logger?.id) {
      data.id = logger.id
    }
    onSave(data)
  }

  const handleGenerateSample = () => {
    onGenerateSample(yamlStr)
    setActiveTab('sample')
  }

  const formatCellValue = (value: unknown): string => {
    if (value === null || value === undefined) return ''
    if (typeof value === 'object') return JSON.stringify(value)
    return String(value)
  }

  return (
    <Card className="h-full">
      <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-4">
        <CardTitle>{isNew ? 'New Logger' : logger?.name || 'Logger Editor'}</CardTitle>
        <div className="flex gap-2">
          <Button
            variant="outline"
            size="sm"
            onClick={handleGenerateSample}
            disabled={isGenerating}
          >
            {isGenerating ? (
              <RefreshCw className="mr-2 h-4 w-4 animate-spin" />
            ) : (
              <Play className="mr-2 h-4 w-4" />
            )}
            Sample
          </Button>
          <Button size="sm" onClick={handleSave} disabled={isSaving || !name}>
            {isSaving ? (
              <RefreshCw className="mr-2 h-4 w-4 animate-spin" />
            ) : (
              <Save className="mr-2 h-4 w-4" />
            )}
            Save
          </Button>
        </div>
      </CardHeader>
      <CardContent>
        <div className="mb-4 grid grid-cols-2 gap-4">
          <div className="space-y-2">
            <Label htmlFor="name">Name</Label>
            <Input
              id="name"
              value={name}
              onChange={(e) => setName(e.target.value)}
              placeholder="Logger name"
            />
          </div>
          <div className="space-y-2">
            <Label htmlFor="eps">EPS</Label>
            <Input
              id="eps"
              value={eps}
              onChange={(e) => setEps(e.target.value)}
              placeholder="Events per second"
            />
          </div>
        </div>

        <Tabs value={activeTab} onValueChange={setActiveTab}>
          <TabsList>
            <TabsTrigger value="editor">YAML Editor</TabsTrigger>
            <TabsTrigger value="sample">Sample Output</TabsTrigger>
          </TabsList>
          <TabsContent value="editor" className="border rounded-md mt-2">
            <CodeEditor
              value={yamlStr}
              onChange={(value) => setYamlStr(value || '')}
              language="yaml"
              height="calc(100vh - 24rem)"
            />
          </TabsContent>
          <TabsContent value="sample" className="border rounded-md mt-2">
            <ScrollArea className="h-[calc(100vh-24rem)]">
              {sampleLogs?.length ? (
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead className="w-12">#</TableHead>
                      {columns.map((col) => (
                        <TableHead key={col} className="font-semibold">
                          {col}
                        </TableHead>
                      ))}
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {sampleLogs.map((log, index) => (
                      <TableRow key={index}>
                        <TableCell className="text-muted-foreground">
                          {index + 1}
                        </TableCell>
                        {columns.map((col) => (
                          <TableCell
                            key={col}
                            className="font-mono text-sm max-w-xs truncate"
                            title={formatCellValue(log[col])}
                          >
                            {formatCellValue(log[col])}
                          </TableCell>
                        ))}
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              ) : (
                <div className="flex h-40 items-center justify-center text-muted-foreground">
                  Click "Sample" to generate sample logs
                </div>
              )}
            </ScrollArea>
          </TabsContent>
        </Tabs>
      </CardContent>
    </Card>
  )
}
