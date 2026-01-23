import Editor, { DiffEditor, type OnMount, type OnChange } from '@monaco-editor/react'
import { useUIStore } from '@/stores/uiStore'

interface CodeEditorProps {
  value: string
  onChange?: (value: string | undefined) => void
  language?: string
  readOnly?: boolean
  height?: string | number
  onMount?: OnMount
}

export function CodeEditor({
  value,
  onChange,
  language = 'yaml',
  readOnly = false,
  height = '400px',
  onMount,
}: CodeEditorProps) {
  const { theme } = useUIStore()

  const handleChange: OnChange = (newValue) => {
    onChange?.(newValue)
  }

  return (
    <Editor
      height={height}
      language={language}
      value={value}
      onChange={handleChange}
      onMount={onMount}
      theme={theme === 'dark' ? 'vs-dark' : 'light'}
      options={{
        readOnly,
        minimap: { enabled: false },
        fontSize: 13,
        lineNumbers: 'on',
        scrollBeyondLastLine: false,
        automaticLayout: true,
        tabSize: 2,
        wordWrap: 'on',
        folding: true,
        renderLineHighlight: 'line',
        scrollbar: {
          vertical: 'auto',
          horizontal: 'auto',
        },
      }}
    />
  )
}

interface DiffViewerProps {
  original: string
  modified: string
  language?: string
  height?: string | number
}

export function DiffViewer({
  original,
  modified,
  language = 'yaml',
  height = '400px',
}: DiffViewerProps) {
  const { theme } = useUIStore()

  return (
    <DiffEditor
      height={height}
      language={language}
      original={original}
      modified={modified}
      theme={theme === 'dark' ? 'vs-dark' : 'light'}
      options={{
        readOnly: true,
        minimap: { enabled: false },
        fontSize: 13,
        scrollBeyondLastLine: false,
        automaticLayout: true,
        renderSideBySide: true,
      }}
    />
  )
}
