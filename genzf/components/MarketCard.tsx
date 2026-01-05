
import React from 'react';
import { useNavigate } from 'react-router-dom';
import { ArrowUpRight, ArrowDownRight } from 'lucide-react';
import { LineChart, Line, ResponsiveContainer } from 'recharts';
import { Card } from './ui/Card';
import { MarketItem } from '../types';
import { useApp } from '../context/AppContext';

interface MarketCardProps {
  item: MarketItem;
}

export const MarketCard: React.FC<MarketCardProps> = ({ item }) => {
  const navigate = useNavigate();
  const { t } = useApp();
  const isPositive = item.change >= 0;
  // Prepare data for recharts
  const data = item.data.map((val, idx) => ({ i: idx, val }));

  return (
    <Card 
      onClick={() => navigate(`/markets/${item.id}`)}
      className="hover:border-blue-300 dark:hover:border-blue-700 transition-colors cursor-pointer group h-full flex flex-col justify-between"
    >
      <div className="flex justify-between items-start mb-2">
        <div>
          <h3 className="font-semibold text-slate-900 dark:text-slate-100">{item.name}</h3>
          <p className="text-xs text-slate-500">{item.symbol}</p>
        </div>
        <span className={`flex items-center text-sm font-medium ${isPositive ? 'text-emerald-600' : 'text-red-500'}`}>
          {isPositive ? <ArrowUpRight size={16} /> : <ArrowDownRight size={16} />}
          {Math.abs(item.change)}%
        </span>
      </div>
      
      <div className="flex items-end justify-between">
        <div className="text-xl font-bold tracking-tight">
          {item.price.toLocaleString()} <span className="text-xs font-normal text-slate-400">USD</span>
        </div>
        <div className="w-20 h-10">
          <ResponsiveContainer width="100%" height="100%">
            <LineChart data={data}>
              <Line 
                type="monotone" 
                dataKey="val" 
                stroke={isPositive ? '#10b981' : '#ef4444'} 
                strokeWidth={2} 
                dot={false} 
              />
            </LineChart>
          </ResponsiveContainer>
        </div>
      </div>
      <p className="text-[10px] text-slate-400 mt-2 text-right">{t('common.updated')} {item.lastUpdated}</p>
    </Card>
  );
};
