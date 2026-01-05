
import React, { useState, useMemo } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { ArrowUpRight, ArrowDownRight, ArrowLeft, Clock, Activity, DollarSign, BarChart3, TrendingUp, Info } from 'lucide-react';
import { AreaChart, Area, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid } from 'recharts';
import { MOCK_MARKET_DATA } from '../constants';
import { Card } from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { ProfitCalculator } from '../components/ProfitCalculator';
import { useApp } from '../context/AppContext';

type TimeRange = '1D' | '1W' | '1M' | '1Y';

export const MarketDetail: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { t } = useApp();
  const [activeTab, setActiveTab] = useState<'overview' | 'chart' | 'calc'>('chart');
  const [timeRange, setTimeRange] = useState<TimeRange>('1W');

  const item = MOCK_MARKET_DATA.find(m => m.id === id);

  // Generate mock historical data
  const chartData = useMemo(() => {
    if (!item) return [];
    const points = timeRange === '1D' ? 24 : timeRange === '1W' ? 30 : 50;
    const base = item.price;
    const volatility = base * 0.02; // 2% vol
    
    return Array.from({ length: points }, (_, i) => {
      const randomChange = (Math.random() - 0.5) * volatility;
      return {
        date: `T-${points - i}`,
        price: base + randomChange * (i * 0.1) // drift
      };
    });
  }, [item, timeRange]);

  if (!item) {
    return (
      <div className="flex flex-col items-center justify-center min-h-[50vh] space-y-4">
        <div className="p-4 rounded-full bg-slate-100 dark:bg-slate-800">
           <Activity className="text-slate-400" size={32} />
        </div>
        <h2 className="text-xl font-bold text-slate-900 dark:text-white">{t('market.not_found')}</h2>
        <Button onClick={() => navigate('/markets')}>{t('market.filter_all')}</Button>
      </div>
    );
  }

  const isPositive = item.change >= 0;
  const themeColor = isPositive ? '#10b981' : '#ef4444';

  const CustomTooltip = ({ active, payload, label }: any) => {
    if (active && payload && payload.length) {
      return (
        <div className="bg-white/90 dark:bg-slate-900/90 border border-slate-200 dark:border-slate-700 p-3 rounded-xl shadow-xl backdrop-blur-md ring-1 ring-black/5">
          <p className="text-slate-500 dark:text-slate-400 text-xs font-medium mb-1 uppercase tracking-wider">{label || 'Price'}</p>
          <p className="text-slate-900 dark:text-white font-bold font-mono text-lg">
            ${payload[0].value.toLocaleString(undefined, {minimumFractionDigits: 2, maximumFractionDigits: 2})}
          </p>
        </div>
      );
    }
    return null;
  };

  return (
    <div className="max-w-5xl mx-auto space-y-6 animate-in fade-in slide-in-from-bottom-4 duration-500">
      
      {/* Top Navigation Bar */}
      <div className="flex items-center gap-2 text-sm text-slate-500 hover:text-slate-800 dark:hover:text-slate-200 transition-colors w-fit cursor-pointer group" onClick={() => navigate('/markets')}>
         <div className="p-1.5 rounded-full bg-slate-100 dark:bg-slate-800 group-hover:bg-slate-200 dark:group-hover:bg-slate-700 transition-colors">
            <ArrowLeft size={16} />
         </div>
         <span className="font-medium">{t('common.back')}</span>
      </div>

      {/* Main Header */}
      <div className="flex flex-col lg:flex-row justify-between items-start lg:items-end gap-6">
        <div className="space-y-1">
           <div className="flex items-center gap-3">
              <div className="w-10 h-10 rounded-xl bg-gradient-to-br from-slate-100 to-slate-200 dark:from-slate-800 dark:to-slate-900 flex items-center justify-center text-lg font-bold shadow-inner">
                  {item.symbol[0]}
              </div>
              <div>
                <h1 className="text-3xl font-bold text-slate-900 dark:text-white tracking-tight leading-none">{item.name}</h1>
                <span className="text-slate-500 dark:text-slate-400 font-medium">{item.symbol}</span>
              </div>
           </div>
           <div className="flex items-center gap-2 mt-2">
             <span className="px-2.5 py-0.5 rounded-md text-[10px] font-bold uppercase tracking-wider bg-slate-100 dark:bg-slate-800 text-slate-600 dark:text-slate-400 border border-slate-200 dark:border-slate-700">
               {item.type}
             </span>
             <span className="text-xs text-slate-400 flex items-center gap-1">
               <Clock size={12} /> {t('common.updated')} {item.lastUpdated}
             </span>
           </div>
        </div>
        
        <div className="text-left lg:text-right bg-slate-50 dark:bg-slate-900/50 p-4 lg:p-0 rounded-2xl lg:bg-transparent w-full lg:w-auto flex flex-row lg:flex-col justify-between items-center lg:items-end">
          <div>
            <div className="text-sm text-slate-500 font-medium mb-1 lg:hidden">Current Price</div>
            <div className="text-4xl font-bold font-mono tracking-tight text-slate-900 dark:text-white">
                ${item.price.toLocaleString()}
            </div>
          </div>
          <div className={`flex items-center gap-1.5 px-3 py-1.5 rounded-full font-bold text-sm shadow-sm backdrop-blur-sm mt-1
              ${isPositive 
                  ? 'bg-emerald-100/50 text-emerald-700 dark:bg-emerald-500/10 dark:text-emerald-400 ring-1 ring-emerald-500/20' 
                  : 'bg-red-100/50 text-red-700 dark:bg-red-500/10 dark:text-red-400 ring-1 ring-red-500/20'
              }`}>
            {isPositive ? <ArrowUpRight size={18} /> : <ArrowDownRight size={18} />}
            {Math.abs(item.change)}%
            <span className="opacity-60 font-normal ml-1">Today</span>
          </div>
        </div>
      </div>

      {/* Tabs */}
      <div className="border-b border-slate-200 dark:border-slate-800">
        <div className="flex gap-8 overflow-x-auto no-scrollbar">
            <button
                onClick={() => setActiveTab('chart')}
                className={`pb-4 text-sm font-semibold border-b-2 transition-all flex items-center gap-2 whitespace-nowrap
                    ${activeTab === 'chart' 
                        ? 'border-blue-600 text-blue-600 dark:text-blue-400' 
                        : 'border-transparent text-slate-500 hover:text-slate-700 dark:hover:text-slate-300'}`}
            >
                <BarChart3 size={18} className={activeTab === 'chart' ? "animate-pulse" : ""}/> {t('market.tab_chart')}
            </button>
            <button
                onClick={() => setActiveTab('overview')}
                className={`pb-4 text-sm font-semibold border-b-2 transition-all flex items-center gap-2 whitespace-nowrap
                    ${activeTab === 'overview' 
                        ? 'border-blue-600 text-blue-600 dark:text-blue-400' 
                        : 'border-transparent text-slate-500 hover:text-slate-700 dark:hover:text-slate-300'}`}
            >
                <Activity size={18}/> {t('market.tab_overview')}
            </button>
            <button
                onClick={() => setActiveTab('calc')}
                className={`pb-4 text-sm font-semibold border-b-2 transition-all flex items-center gap-2 whitespace-nowrap
                    ${activeTab === 'calc' 
                        ? 'border-blue-600 text-blue-600 dark:text-blue-400' 
                        : 'border-transparent text-slate-500 hover:text-slate-700 dark:hover:text-slate-300'}`}
            >
                <DollarSign size={18}/> {t('market.tab_calc')}
            </button>
        </div>
      </div>

      {/* Content Area */}
      <div className="min-h-[400px]">
        {activeTab === 'chart' && (
          <div className="space-y-4">
            {/* Chart Controls */}
            <div className="flex justify-end">
              <div className="bg-slate-100 dark:bg-slate-800/80 p-1 rounded-lg inline-flex ring-1 ring-slate-200 dark:ring-slate-700">
                {(['1D', '1W', '1M', '1Y'] as TimeRange[]).map(r => (
                  <button
                    key={r}
                    onClick={() => setTimeRange(r)}
                    className={`px-4 py-1.5 rounded-md text-xs font-bold transition-all ${
                        timeRange === r 
                        ? 'bg-white dark:bg-slate-700 shadow-sm text-slate-900 dark:text-white' 
                        : 'text-slate-500 hover:text-slate-700 dark:hover:text-slate-300'
                    }`}
                  >
                    {r}
                  </button>
                ))}
              </div>
            </div>
            
            {/* Chart Container */}
            <Card className="h-[450px] w-full p-1 border-0 shadow-xl bg-gradient-to-b from-white to-slate-50 dark:from-slate-900 dark:to-slate-950 ring-1 ring-slate-200 dark:ring-slate-800">
              <div className="h-full w-full p-2 sm:p-6">
                <ResponsiveContainer width="100%" height="100%">
                  <AreaChart data={chartData} margin={{ top: 10, right: 0, left: -20, bottom: 0 }}>
                    <defs>
                      <linearGradient id="colorPrice" x1="0" y1="0" x2="0" y2="1">
                        <stop offset="5%" stopColor={themeColor} stopOpacity={0.2}/>
                        <stop offset="95%" stopColor={themeColor} stopOpacity={0}/>
                      </linearGradient>
                    </defs>
                    <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#334155" opacity={0.08} />
                    <XAxis 
                        dataKey="date" 
                        hide 
                        axisLine={false} 
                        tickLine={false} 
                    />
                    <YAxis 
                        orientation="right" 
                        domain={['auto', 'auto']} 
                        tick={{fontSize: 11, fill: '#94a3b8', fontWeight: 500}} 
                        axisLine={false} 
                        tickLine={false}
                        tickFormatter={(val) => `$${val.toLocaleString()}`}
                        width={60}
                    />
                    <Tooltip content={<CustomTooltip />} cursor={{ stroke: themeColor, strokeWidth: 1, strokeDasharray: '4 4' }} />
                    <Area 
                      type="monotone" 
                      dataKey="price" 
                      stroke={themeColor} 
                      fillOpacity={1} 
                      fill="url(#colorPrice)" 
                      strokeWidth={2.5}
                      animationDuration={1500}
                    />
                  </AreaChart>
                </ResponsiveContainer>
              </div>
            </Card>
          </div>
        )}

        {activeTab === 'overview' && (
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4 md:gap-6">
             <Card className="bg-slate-50/50 dark:bg-slate-900/50 border-0 ring-1 ring-slate-200 dark:ring-slate-800">
                <div className="flex items-center gap-2 mb-2">
                    <div className="w-1 h-4 bg-blue-500 rounded-full"></div>
                    <span className="text-xs font-bold text-slate-500 uppercase tracking-wider">{t('market.open')}</span>
                </div>
                <div className="text-xl md:text-2xl font-mono font-semibold text-slate-900 dark:text-white">
                    ${(item.price * 0.99).toLocaleString()}
                </div>
             </Card>
             <Card className="bg-slate-50/50 dark:bg-slate-900/50 border-0 ring-1 ring-slate-200 dark:ring-slate-800">
                <div className="flex items-center gap-2 mb-2">
                    <div className="w-1 h-4 bg-emerald-500 rounded-full"></div>
                    <span className="text-xs font-bold text-slate-500 uppercase tracking-wider">{t('market.high')}</span>
                </div>
                <div className="text-xl md:text-2xl font-mono font-semibold text-slate-900 dark:text-white">
                    ${(item.price * 1.02).toLocaleString()}
                </div>
             </Card>
             <Card className="bg-slate-50/50 dark:bg-slate-900/50 border-0 ring-1 ring-slate-200 dark:ring-slate-800">
                <div className="flex items-center gap-2 mb-2">
                    <div className="w-1 h-4 bg-red-500 rounded-full"></div>
                    <span className="text-xs font-bold text-slate-500 uppercase tracking-wider">{t('market.low')}</span>
                </div>
                <div className="text-xl md:text-2xl font-mono font-semibold text-slate-900 dark:text-white">
                    ${(item.price * 0.98).toLocaleString()}
                </div>
             </Card>
             <Card className="bg-slate-50/50 dark:bg-slate-900/50 border-0 ring-1 ring-slate-200 dark:ring-slate-800">
                <div className="flex items-center gap-2 mb-2">
                    <div className="w-1 h-4 bg-purple-500 rounded-full"></div>
                    <span className="text-xs font-bold text-slate-500 uppercase tracking-wider">{t('market.vol')}</span>
                </div>
                <div className="text-xl md:text-2xl font-mono font-semibold text-slate-900 dark:text-white">
                    1.2M
                </div>
             </Card>
             
             <div className="col-span-2 md:col-span-4 mt-2">
                <div className="flex gap-4 p-4 rounded-xl bg-gradient-to-r from-blue-50 to-indigo-50 dark:from-slate-800 dark:to-slate-800/50 border border-blue-100 dark:border-slate-700">
                    <div className="p-2 bg-white dark:bg-slate-900 rounded-lg h-fit text-blue-600 dark:text-blue-400 shadow-sm">
                        <Info size={24} />
                    </div>
                    <div>
                        <h3 className="font-bold text-slate-900 dark:text-white mb-1">{t('market.status_title')}</h3>
                        <p className="text-sm text-slate-600 dark:text-slate-400 leading-relaxed max-w-2xl">{t('market.status_desc')}</p>
                    </div>
                </div>
             </div>
          </div>
        )}

        {activeTab === 'calc' && (
           <div className="max-w-3xl mx-auto py-4">
             <ProfitCalculator initialBuyPrice={item.price} symbol={item.symbol} />
           </div>
        )}
      </div>
    </div>
  );
};
