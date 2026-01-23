import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
} from 'recharts'
import type { EpsHistoryVO } from '@/types/output'
import { formatEps } from '@/lib/utils'

interface EpsChartProps {
  data: EpsHistoryVO[] | undefined
  height?: number
}

export function EpsChart({ data, height = 200 }: EpsChartProps) {
  const chartData =
    data?.map((item) => ({
      time: new Date(item.timestamp).toLocaleTimeString(),
      eps: item.eps,
    })) || []

  if (!chartData.length) {
    return (
      <div
        className="flex items-center justify-center text-muted-foreground border rounded-md"
        style={{ height }}
      >
        No EPS data available
      </div>
    )
  }

  return (
    <ResponsiveContainer width="100%" height={height}>
      <LineChart data={chartData}>
        <CartesianGrid strokeDasharray="3 3" stroke="hsl(var(--border))" />
        <XAxis
          dataKey="time"
          tick={{ fontSize: 12, fill: 'hsl(var(--muted-foreground))' }}
          tickLine={{ stroke: 'hsl(var(--border))' }}
          axisLine={{ stroke: 'hsl(var(--border))' }}
        />
        <YAxis
          tickFormatter={(value) => formatEps(value)}
          tick={{ fontSize: 12, fill: 'hsl(var(--muted-foreground))' }}
          tickLine={{ stroke: 'hsl(var(--border))' }}
          axisLine={{ stroke: 'hsl(var(--border))' }}
        />
        <Tooltip
          contentStyle={{
            backgroundColor: 'hsl(var(--card))',
            border: '1px solid hsl(var(--border))',
            borderRadius: '8px',
          }}
          labelStyle={{ color: 'hsl(var(--foreground))' }}
          formatter={(value) => [formatEps(value as number), 'EPS']}
        />
        <Line
          type="monotone"
          dataKey="eps"
          stroke="hsl(var(--primary))"
          strokeWidth={2}
          dot={false}
          activeDot={{ r: 4 }}
        />
      </LineChart>
    </ResponsiveContainer>
  )
}
