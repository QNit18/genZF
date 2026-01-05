
import React, { useState, useEffect } from 'react';
import { useApp } from '../context/AppContext';
import { Card } from './ui/Card';

interface ProfitCalculatorProps {
  initialBuyPrice?: number;
  initialSellPrice?: number;
  symbol?: string;
}

export const ProfitCalculator: React.FC<ProfitCalculatorProps> = ({ 
  initialBuyPrice, 
  initialSellPrice,
  symbol 
}) => {
  const { t } = useApp();
  const [buyPrice, setBuyPrice] = useState<string>(initialBuyPrice?.toString() || '');
  const [sellPrice, setSellPrice] = useState<string>(initialSellPrice?.toString() || '');
  const [qty, setQty] = useState<string>('');
  
  // Update state if props change (e.g. switching markets)
  useEffect(() => {
    if (initialBuyPrice) setBuyPrice(initialBuyPrice.toString());
    if (initialSellPrice) setSellPrice(initialSellPrice.toString());
  }, [initialBuyPrice, initialSellPrice]);

  const b = parseFloat(buyPrice) || 0;
  const s = parseFloat(sellPrice) || 0;
  const q = parseFloat(qty) || 0;
  
  const profit = (s - b) * q;
  const roi = b > 0 ? ((s - b) / b) * 100 : 0;
  const isGain = profit >= 0;

  // Use simple formatting or VND if assumed
  const formatCurrency = (val: number) => {
    // If it looks like a small number (Forex/US Stock), keep it generic. 
    // If it looks like a large number (VND), format as VND.
    // For this generic tool, we will just use locale string to handle commas.
    // However, if the user requested "Default Currency is VND", we should prioritize that.
    if (val > 1000) {
         return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val);
    }
    return val.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  };

  return (
    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
      <div className="space-y-4">
        <div>
          <label className="block text-sm font-medium mb-1 text-slate-700 dark:text-slate-300">
            {t('calc.profit.buy_price')} {symbol && `(${symbol})`}
          </label>
          <div className="relative">
             <input 
                type="number" 
                value={buyPrice} 
                onChange={e => setBuyPrice(e.target.value)}
                className="w-full p-2 pl-4 rounded-lg border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-900 focus:ring-2 focus:ring-blue-500 outline-none transition-all text-base md:text-sm"
                placeholder="0.00"
            />
          </div>
        </div>
        <div>
          <label className="block text-sm font-medium mb-1 text-slate-700 dark:text-slate-300">{t('calc.profit.sell_price')}</label>
          <input 
            type="number" 
            value={sellPrice} 
            onChange={e => setSellPrice(e.target.value)}
            className="w-full p-2 pl-4 rounded-lg border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-900 focus:ring-2 focus:ring-blue-500 outline-none transition-all text-base md:text-sm"
            placeholder="0.00"
          />
        </div>
        <div>
          <label className="block text-sm font-medium mb-1 text-slate-700 dark:text-slate-300">{t('calc.profit.qty')}</label>
          <input 
            type="number" 
            value={qty} 
            onChange={e => setQty(e.target.value)}
            className="w-full p-2 pl-4 rounded-lg border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-900 focus:ring-2 focus:ring-blue-500 outline-none transition-all text-base md:text-sm"
            placeholder="0"
          />
        </div>
      </div>

      <Card className="flex flex-col justify-center items-center bg-slate-50 dark:bg-slate-800/50">
        <h3 className="text-sm font-medium text-slate-500 mb-2">{t('calc.profit.result')}</h3>
        <div className={`text-4xl font-bold mb-2 ${isGain ? 'text-emerald-500' : 'text-red-500'}`}>
          {formatCurrency(profit)}
        </div>
        <div className={`text-lg font-medium px-3 py-1 rounded-full ${isGain ? 'bg-emerald-100 text-emerald-700 dark:bg-emerald-900/30 dark:text-emerald-400' : 'bg-red-100 text-red-700 dark:bg-red-900/30 dark:text-red-400'}`}>
          {roi.toFixed(2)}% ROI
        </div>
        <p className="text-xs text-slate-400 mt-4">{t('calc.profit.breakeven')}: {formatCurrency(b)}</p>
      </Card>
    </div>
  );
};
