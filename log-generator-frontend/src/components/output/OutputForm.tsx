import { useState, useEffect, useMemo } from 'react'
import type { OutputDto, OutputType, OutputTemplate } from '@/types/output'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import { Switch } from '@/components/ui/switch'
import { ScrollArea } from '@/components/ui/scroll-area'
import { Save, RefreshCw } from 'lucide-react'
import { CodeEditor } from '@/components/ui/code-editor'

interface OutputFormProps {
  output: OutputDto | null
  template: Record<string, OutputTemplate> | undefined
  onSave: (output: Partial<OutputDto>) => void
  isSaving: boolean
  isNew?: boolean
}

export function OutputForm({
  output,
  template,
  onSave,
  isSaving,
  isNew = false,
}: OutputFormProps) {
  const [name, setName] = useState('')
  const [ip, setIp] = useState('')
  const [type, setType] = useState<OutputType>('sparrow')
  const [maxQueueSize, setMaxQueueSize] = useState('10000')
  const [info, setInfo] = useState<Record<string, unknown>>({})

  useEffect(() => {
    if (output) {
      setName(output.name || '')
      setIp(output.ip || '')
      setType(output.type || 'sparrow')
      setMaxQueueSize(String(output.max_queue_size || 10000))
      setInfo(output.info || {})
    } else if (isNew) {
      setName('')
      setIp('')
      setType('sparrow')
      setMaxQueueSize('10000')
      setInfo({})
    }
  }, [output, isNew])

  useEffect(() => {
    if (template && type) {
      const typeTemplate = template[type]
      if (typeTemplate?.info && !output) {
        const defaultInfo: Record<string, unknown> = {}
        Object.entries(typeTemplate.info).forEach(([key, fieldTemplate]) => {
          if (fieldTemplate.placeholder !== undefined) {
            defaultInfo[key] = fieldTemplate.placeholder
          }
        })
        setInfo(defaultInfo)
      }
    }
  }, [type, template, output])

  const handleSave = () => {
    const data: Partial<OutputDto> = {
      name,
      ip,
      type,
      max_queue_size: parseInt(maxQueueSize, 10),
      info,
    }
    if (output?.id) {
      data.id = output.id
    }
    onSave(data)
  }

  const handleInfoChange = (key: string, value: unknown) => {
    setInfo((prev) => ({ ...prev, [key]: value }))
  }

  const currentConfig = useMemo(() => {
    return {
      ...(output || {}),
      name,
      ip,
      type,
      max_queue_size: parseInt(maxQueueSize, 10) || 0,
      info,
    }
  }, [output, name, ip, type, maxQueueSize, info])

  const renderField = (key: string, fieldTemplate: OutputTemplate) => {
    const value = info[key]

    switch (fieldTemplate.type) {
      case 'checkbox':
        return (
          <div key={key} className="flex items-center justify-between">
            <Label htmlFor={key}>{fieldTemplate.label}</Label>
            <Switch
              id={key}
              checked={Boolean(value)}
              onCheckedChange={(checked) => handleInfoChange(key, checked)}
            />
          </div>
        )
      case 'select':
        return (
          <div key={key} className="space-y-2">
            <Label htmlFor={key}>{fieldTemplate.label}</Label>
            <Select
              value={String(value || '')}
              onValueChange={(v) => handleInfoChange(key, v)}
            >
              <SelectTrigger id={key}>
                <SelectValue placeholder={`Select ${fieldTemplate.label}`} />
              </SelectTrigger>
              <SelectContent>
                {fieldTemplate.options?.map((option) => (
                  <SelectItem key={option} value={option}>
                    {option}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
        )
      case 'number':
        return (
          <div key={key} className="space-y-2">
            <Label htmlFor={key}>{fieldTemplate.label}</Label>
            <Input
              id={key}
              type="number"
              value={String(value || '')}
              onChange={(e) => handleInfoChange(key, parseInt(e.target.value, 10))}
              placeholder={String(fieldTemplate.placeholder || '')}
            />
          </div>
        )
      default:
        return (
          <div key={key} className="space-y-2">
            <Label htmlFor={key}>{fieldTemplate.label}</Label>
            <Input
              id={key}
              value={String(value || '')}
              onChange={(e) => handleInfoChange(key, e.target.value)}
              placeholder={String(fieldTemplate.placeholder || '')}
            />
          </div>
        )
    }
  }

  const currentTemplate = template?.[type]

  return (
    <Card className="h-full flex flex-col">
      <CardHeader className="flex-row items-center justify-between space-y-0 pb-4">
        <CardTitle>{isNew ? 'New Output' : output?.name || 'Output Editor'}</CardTitle>
        <Button size="sm" onClick={handleSave} disabled={isSaving || !name}>
          {isSaving ? (
            <RefreshCw className="mr-2 h-4 w-4 animate-spin" />
          ) : (
            <Save className="mr-2 h-4 w-4" />
          )}
          Save
        </Button>
      </CardHeader>
      <CardContent className="flex-1 min-h-0">
        <ScrollArea className="h-full">
          <div className="space-y-4 pr-4">
            <div className="grid grid-cols-1 gap-4">
              <div className="space-y-2">
                <Label htmlFor="name">Name</Label>
                <Input
                  id="name"
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  placeholder="Output name"
                />
              </div>
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label htmlFor="type">Type</Label>
                <Select
                  value={type}
                  onValueChange={(v) => setType(v as OutputType)}
                  disabled={!!output?.id}
                >
                  <SelectTrigger id="type">
                    <SelectValue placeholder="Select type" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="sparrow">Sparrow</SelectItem>
                    <SelectItem value="file">File</SelectItem>
                    <SelectItem value="kafka">Kafka</SelectItem>
                  </SelectContent>
                </Select>
              </div>
              <div className="space-y-2">
                <Label htmlFor="maxQueueSize">Max Queue Size</Label>
                <Input
                  id="maxQueueSize"
                  type="number"
                  value={maxQueueSize}
                  onChange={(e) => setMaxQueueSize(e.target.value)}
                  placeholder="10000"
                />
              </div>
            </div>

            {currentTemplate?.info && (
              <div className="space-y-4 pt-4 border-t">
                <h3 className="font-medium">
                  {type.charAt(0).toUpperCase() + type.slice(1)} Configuration
                </h3>
                {Object.entries(currentTemplate.info).map(([key, fieldTemplate]) =>
                  renderField(key, fieldTemplate)
                )}
              </div>
            )}

            <div className="space-y-2 pt-4 border-t">
              <Label>Configuration JSON</Label>
              <div className="border rounded-md overflow-hidden">
                <CodeEditor
                  value={JSON.stringify(currentConfig, null, 2)}
                  language="json"
                  readOnly={true}
                  height="calc(100vh - 400px)"
                />
              </div>
            </div>
          </div>
        </ScrollArea>
      </CardContent>
    </Card>
  )
}
