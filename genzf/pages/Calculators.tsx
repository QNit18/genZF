
import React, { useState } from 'react';
import { useApp } from '../context/AppContext';
import { Card } from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { ProfitCalculator } from '../components/ProfitCalculator';

// --- Tax Calculator Component ---
const TaxCalculator: React.FC = () => {
  const { t } = useApp();
  const [income, setIncome] = useState<string>('');
  const [result, setResult] = useState<{tax: number, net: number} | null>(null);

  const calculateTax = () => {
    // Simple progressive tax logic mock (Vietnam based logic for example)
    const i = parseFloat(income) || 0;
    let tax = 0;
    // Mock logic: 5% < 5M, 10% < 10M, 20% > 10M
    if (i > 10000000) tax = (i - 10000000) * 0.2 + 250000 + 250000;
    else if (i > 5000000) tax = (i - 5000000) * 0.1 + 250000;
    else tax = i * 0.05;

    // Ensure no negative tax for simplification
    if (i < 11000000) tax = 0; // Standard deduction mock

    setResult({ tax, net: i - tax });
  };

  return (
    <div className="max-w-xl mx-auto space-y-6">
      <div className="space-y-2">
        <label className="block text-sm font-medium text-slate-700 dark:text-slate-300">{t('calc.tax.income')}</label>
        <div className="flex gap-2">
          <input 
            type="number" 
            value={income}
            onChange={e => setIncome(e.target.value)}
            className="flex-1 p-3 rounded-lg border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-900 outline-none focus:ring-2 focus:ring-purple-500 text-base md:text-sm"
            placeholder="e.g. 20000000"
          />
          <Button onClick={calculateTax}>{t('calc.tax.calculate')}</Button>
        </div>
        <p className="text-xs text-slate-400">{t('calc.tax.disclaimer')}</p>
      </div>

      {result && (
        <div className="grid grid-cols-2 gap-4">
          <Card className="text-center">
            <p className="text-xs text-slate-500 mb-1">{t('calc.tax.est_tax')}</p>
            <p className="text-xl font-bold text-red-500">{result.tax.toLocaleString()} ₫</p>
          </Card>
          <Card className="text-center">
            <p className="text-xs text-slate-500 mb-1">{t('calc.tax.net_income')}</p>
            <p className="text-xl font-bold text-emerald-500">{result.net.toLocaleString()} ₫</p>
          </Card>
        </div>
      )}
    </div>
  );
};

export const Calculators: React.FC<{ type?: 'profit' | 'tax' }> = ({ type = 'profit' }) => {
  const [activeTab, setActiveTab] = useState<'profit' | 'tax'>(type);
  const { t } = useApp();

  return (
    <div className="max-w-4xl mx-auto">
      <div className="flex justify-center mb-8">
        <div className="bg-slate-100 dark:bg-slate-900 p-1 rounded-xl inline-flex">
          <button 
            onClick={() => setActiveTab('profit')}
            className={`px-6 py-2 rounded-lg text-sm font-medium transition-all ${activeTab === 'profit' ? 'bg-white dark:bg-slate-800 shadow-sm text-blue-600 dark:text-blue-400' : 'text-slate-500'}`}
          >
            {t('calc.profit.title')}
          </button>
          <button 
            onClick={() => setActiveTab('tax')}
            className={`px-6 py-2 rounded-lg text-sm font-medium transition-all ${activeTab === 'tax' ? 'bg-white dark:bg-slate-800 shadow-sm text-blue-600 dark:text-blue-400' : 'text-slate-500'}`}
          >
            {t('calc.tax.title')}
          </button>
        </div>
      </div>

      <div className="animate-in fade-in slide-in-from-bottom-4 duration-300">
        {activeTab === 'profit' ? <ProfitCalculator /> : <TaxCalculator />}
      </div>
    </div>
  );
};
