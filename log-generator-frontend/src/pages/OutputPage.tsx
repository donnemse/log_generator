import { useState } from 'react'
import { Header } from '@/components/layout/Header'
import { OutputList } from '@/components/output/OutputList'
import { OutputForm } from '@/components/output/OutputForm'
import { EpsChart } from '@/components/output/EpsChart'
import { ClientTable } from '@/components/output/ClientTable'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { Plus } from 'lucide-react'
import {
  useOutputs,
  useOutput,
  useOutputTemplate,
  useProducerEps,
  useCreateOutput,
  useUpdateOutput,
  useDeleteOutput,
  useStartOutput,
  useStopOutput,
  useStopClient,
} from '@/hooks/useOutputs'
import type { OutputDto } from '@/types/output'

export function OutputPage() {
  const [selectedId, setSelectedId] = useState<number | null>(null)
  const [isNew, setIsNew] = useState(false)
  const [activeTab, setActiveTab] = useState('settings')

  const { data: outputs, isLoading } = useOutputs()
  const { data: selectedOutput } = useOutput(selectedId ?? 0)
  const { data: template } = useOutputTemplate()
  const { data: epsData } = useProducerEps(selectedId ?? 0)

  const createMutation = useCreateOutput()
  const updateMutation = useUpdateOutput()
  const deleteMutation = useDeleteOutput()
  const startMutation = useStartOutput()
  const stopMutation = useStopOutput()
  const stopClientMutation = useStopClient()

  const handleSelect = (id: number) => {
    setSelectedId(id)
    setIsNew(false)
    setActiveTab('settings')
  }

  const handleNew = () => {
    setSelectedId(null)
    setIsNew(true)
    setActiveTab('settings')
  }

  const handleSave = (output: Partial<OutputDto>) => {
    if (output.id) {
      updateMutation.mutate(output, {
        onSuccess: () => {
          setIsNew(false)
        },
      })
    } else {
      createMutation.mutate(output, {
        onSuccess: (data) => {
          setSelectedId(data.id)
          setIsNew(false)
        },
      })
    }
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

  const handleDisconnectClient = (clientId: string) => {
    if (selectedId) {
      stopClientMutation.mutate({ outputId: selectedId, clientId })
    }
  }

  return (
    <div className="flex h-screen flex-col">
      <Header
        title="Output Management"
        action={
          <Button size="sm" onClick={handleNew}>
            <Plus className="mr-2 h-4 w-4" />
            New Output
          </Button>
        }
      />

      <div className="flex flex-1 gap-4 overflow-hidden p-4">
        <Card className="w-80 shrink-0">
          <CardHeader className="pb-2">
            <CardTitle className="text-base">Outputs</CardTitle>
          </CardHeader>
          <CardContent className="p-0">
            <OutputList
              outputs={outputs}
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
            <Tabs
              value={activeTab}
              onValueChange={setActiveTab}
              className="h-full flex flex-col"
            >
              <TabsList className="w-fit">
                <TabsTrigger value="settings">Settings</TabsTrigger>
                {selectedId && !isNew && (
                  <>
                    <TabsTrigger value="monitor">Monitor</TabsTrigger>
                    {selectedOutput?.type === 'sparrow' && (
                      <TabsTrigger value="clients">Clients</TabsTrigger>
                    )}
                  </>
                )}
              </TabsList>

              <TabsContent value="settings" className="flex-1 mt-4">
                <OutputForm
                  output={selectedOutput ?? null}
                  template={template}
                  onSave={handleSave}
                  isSaving={createMutation.isPending || updateMutation.isPending}
                  isNew={isNew}
                />
              </TabsContent>

              {selectedId && !isNew && (
                <>
                  <TabsContent value="monitor" className="flex-1 mt-4">
                    <Card className="h-full">
                      <CardHeader>
                        <CardTitle>EPS Monitor</CardTitle>
                      </CardHeader>
                      <CardContent>
                        <EpsChart data={epsData} height={300} />
                        <div className="mt-4 grid grid-cols-3 gap-4">
                          <Card>
                            <CardContent className="pt-6">
                              <div className="text-2xl font-bold">
                                {selectedOutput?.eps?.current?.toFixed(2) || '0.00'}
                              </div>
                              <div className="text-sm text-muted-foreground">
                                Current EPS
                              </div>
                            </CardContent>
                          </Card>
                          <Card>
                            <CardContent className="pt-6">
                              <div className="text-2xl font-bold">
                                {selectedOutput?.currentQueueSize || 0}
                              </div>
                              <div className="text-sm text-muted-foreground">
                                Queue Size
                              </div>
                            </CardContent>
                          </Card>
                          <Card>
                            <CardContent className="pt-6">
                              <div className="text-2xl font-bold">
                                {selectedOutput?.clients?.length || 0}
                              </div>
                              <div className="text-sm text-muted-foreground">
                                Connected Clients
                              </div>
                            </CardContent>
                          </Card>
                        </div>
                      </CardContent>
                    </Card>
                  </TabsContent>

                  {selectedOutput?.type === 'sparrow' && (
                    <TabsContent value="clients" className="flex-1 mt-4">
                      <Card className="h-full">
                        <CardHeader>
                          <CardTitle>Connected Clients</CardTitle>
                        </CardHeader>
                        <CardContent>
                          <ClientTable
                            clients={selectedOutput?.clients}
                            onDisconnect={handleDisconnectClient}
                          />
                        </CardContent>
                      </Card>
                    </TabsContent>
                  )}
                </>
              )}
            </Tabs>
          ) : (
            <Card className="flex h-full items-center justify-center">
              <div className="text-center text-muted-foreground">
                <p>Select an output from the list or create a new one</p>
              </div>
            </Card>
          )}
        </div>
      </div>
    </div>
  )
}
