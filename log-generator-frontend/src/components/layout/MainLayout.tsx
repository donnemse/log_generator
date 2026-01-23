import { Outlet } from 'react-router-dom'
import { cn } from '@/lib/utils'
import { useUIStore } from '@/stores/uiStore'
import { Sidebar } from './Sidebar'
import { TooltipProvider } from '@/components/ui/tooltip'

export function MainLayout() {
  const { sidebarCollapsed, theme } = useUIStore()

  return (
    <TooltipProvider>
      <div className={cn(theme === 'light' && 'light')}>
        <Sidebar />
        <main
          className={cn(
            'min-h-screen transition-all duration-300',
            sidebarCollapsed ? 'ml-16' : 'ml-64'
          )}
        >
          <Outlet />
        </main>
      </div>
    </TooltipProvider>
  )
}
