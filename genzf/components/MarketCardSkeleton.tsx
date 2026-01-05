import React from 'react';
import { Card } from './ui/Card';
import { Skeleton } from './ui/Skeleton';

export const MarketCardSkeleton: React.FC = () => {
  return (
    <Card className="h-full flex flex-col justify-between gap-4">
      {/* Header: Name/Symbol + % Change Badge */}
      <div className="flex justify-between items-start">
        <div className="space-y-2">
          <Skeleton className="h-5 w-24" /> {/* Name */}
          <Skeleton className="h-3 w-14" /> {/* Symbol */}
        </div>
        <Skeleton className="h-5 w-16 rounded-full" /> {/* Percent Change */}
      </div>
      
      {/* Bottom: Price + Chart placeholder */}
      <div className="flex items-end justify-between mt-2">
        <Skeleton className="h-8 w-32" /> {/* Price */}
        <Skeleton className="h-10 w-20" /> {/* Sparkline Chart */}
      </div>
      
      {/* Footer: Updated text */}
      <div className="flex justify-end mt-1">
        <Skeleton className="h-2 w-20" />
      </div>
    </Card>
  );
};