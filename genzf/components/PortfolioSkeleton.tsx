import React from 'react';
import { Card } from './ui/Card';
import { Skeleton } from './ui/Skeleton';

export const PortfolioSkeleton: React.FC = () => {
  return (
    <div className="space-y-6 animate-pulse">
      {/* Header */}
      <div className="flex flex-col md:flex-row gap-4 items-start md:items-center justify-between">
        <div className="space-y-3">
          <Skeleton className="h-8 w-48" />
          <Skeleton className="h-4 w-32" />
        </div>
        <Skeleton className="h-10 w-40" />
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {/* Chart Card */}
        <Card className="col-span-1 md:col-span-2 h-[300px] flex flex-col">
            <Skeleton className="h-6 w-32 mb-6" />
            <div className="flex-1 flex items-center justify-center gap-10">
              <Skeleton className="h-48 w-48 rounded-full shrink-0" />
              <div className="space-y-4 hidden sm:block">
                 <div className="flex items-center gap-2"><Skeleton className="w-3 h-3 rounded-full" /><Skeleton className="h-4 w-24" /></div>
                 <div className="flex items-center gap-2"><Skeleton className="w-3 h-3 rounded-full" /><Skeleton className="h-4 w-24" /></div>
                 <div className="flex items-center gap-2"><Skeleton className="w-3 h-3 rounded-full" /><Skeleton className="h-4 w-24" /></div>
              </div>
            </div>
        </Card>

        {/* Net Worth Card */}
        <Card className="flex flex-col justify-center gap-4 h-[300px] md:h-auto">
             <Skeleton className="h-4 w-24" />
             <Skeleton className="h-10 w-full max-w-[200px]" />
             <Skeleton className="h-8 w-32 rounded-lg" />
        </Card>
      </div>

      {/* Table Skeleton */}
      <div className="rounded-xl border border-slate-200 dark:border-slate-800 overflow-hidden bg-white dark:bg-slate-900">
        <div className="bg-slate-50 dark:bg-slate-950 p-4 border-b border-slate-200 dark:border-slate-800 grid grid-cols-6 gap-4">
           <Skeleton className="h-4 w-16 col-span-1" />
           <Skeleton className="h-4 w-16 col-span-1 justify-self-end" />
           <Skeleton className="h-4 w-16 col-span-1 justify-self-end" />
           <Skeleton className="h-4 w-16 col-span-1 justify-self-end" />
           <Skeleton className="h-4 w-16 col-span-1 justify-self-end" />
           <Skeleton className="h-4 w-16 col-span-1 justify-self-end" />
        </div>
        <div className="divide-y divide-slate-100 dark:divide-slate-800">
           {[1, 2, 3, 4, 5].map(i => (
             <div key={i} className="p-4 grid grid-cols-6 gap-4 items-center">
                <div className="col-span-1">
                   <Skeleton className="h-5 w-12" />
                </div>
                <div className="col-span-1 justify-self-end">
                    <Skeleton className="h-5 w-10" />
                </div>
                <div className="col-span-1 justify-self-end">
                    <Skeleton className="h-5 w-20" />
                </div>
                <div className="col-span-1 justify-self-end">
                    <Skeleton className="h-5 w-20" />
                </div>
                <div className="col-span-1 justify-self-end">
                    <Skeleton className="h-5 w-16" />
                </div>
                <div className="col-span-1 justify-self-end">
                    <Skeleton className="h-8 w-8 rounded-full" />
                </div>
             </div>
           ))}
        </div>
      </div>
    </div>
  );
};