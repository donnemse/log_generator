import { type ClassValue, clsx } from "clsx"
import { twMerge } from "tailwind-merge"

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs))
}

export function formatDate(timestamp: number): string {
  return new Date(timestamp).toLocaleString()
}

export function formatEps(eps: number): string {
  if (eps >= 1000000) {
    return `${(eps / 1000000).toFixed(2)}M`
  }
  if (eps >= 1000) {
    return `${(eps / 1000).toFixed(2)}K`
  }
  return eps.toFixed(2)
}
