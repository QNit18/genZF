
import React, { useState, useEffect, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { PieChart, Pie, Cell, Tooltip, ResponsiveContainer, Sector } from 'recharts';
import { Plus, X, TrendingUp, Info, PieChart as PieChartIcon, Wallet, History, LineChart as Sparkles, Trash2, AlertTriangle, Loader2 } from 'lucide-react';
import { useApp } from '../context/AppContext';
import { Card } from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { Modal } from '../components/ui/Modal';
import { Input } from '../components/ui/Input';
import { PortfolioItem } from '../types';
import { PortfolioSkeleton } from '../components/PortfolioSkeleton';

const FRIENDLY_COLORS = [
  '#818CF8', // Indigo
  '#34D399', // Emerald
  '#F87171', // Red
  '#FB923C', // Orange
  '#A78BFA', // Violet
  '#22D3EE'  // Cyan
];

const INITIAL_HOLDINGS: PortfolioItem[] = [
  { id: 1, symbol: 'FPT', quantity: 1000, avgCost: 85000, currentPrice: 96000, pl: 11000000 },
  { id: 2, symbol: 'HPG', quantity: 2000, avgCost: 25000, currentPrice: 28500, pl: 7000000 },
  { id: 3, symbol: 'VNM', quantity: 500, avgCost: 72000, currentPrice: 68000, pl: -2000000 },
];

const renderActiveShape = (props: any) => {
  const { cx, cy, innerRadius, outerRadius, startAngle, endAngle, fill } = props;
  return (
    <g>
      <Sector
        cx={cx}
        cy={cy}
        innerRadius={innerRadius - 4}
        outerRadius={outerRadius + 8}
        startAngle={startAngle}
        endAngle={endAngle}
        fill={fill}
        style={{ filter: 'drop-shadow(0px 10px 20px rgba(0,0,0,0.15))' }}
      />
    </g>
  );
};

export const Portfolio: React.FC = () => {
  const { user, t, theme } = useApp();
  const navigate = useNavigate();
  const [holdings, setHoldings] = useState<PortfolioItem[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isModalOpen, setIsModalOpen] = useState(false);
  
  // Deletion States
  const [deleteConfirmId, setDeleteConfirmId] = useState<number | null>(null);
  const [isDeleting, setIsDeleting] = useState(false);
  
  const [activeIndex, setActiveIndex] = useState<number | null>(null);
  const [formData, setFormData] = useState({ symbol: '', quantity: '', price: '' });
  const [isSaving, setIsSaving] = useState(false);

  useEffect(() => {
    const fetchPortfolio = async () => {
      setIsLoading(true);
      await new Promise(resolve => setTimeout(resolve, 1000));
      setHoldings(INITIAL_HOLDINGS);
      setIsLoading(false);
    };

    if (user) fetchPortfolio();
  }, [user]);

  const chartData = useMemo(() => {
    const total = holdings.reduce((acc, h) => acc + (h.quantity * h.currentPrice), 0);
    return holdings.map((h, index) => ({
      name: h.symbol,
      value: h.quantity * h.currentPrice,
      color: FRIENDLY_COLORS[index % FRIENDLY_COLORS.length],
      percent: total > 0 ? ((h.quantity * h.currentPrice) / total) * 100 : 0
    })).sort((a, b) => b.value - a.value);
  }, [holdings]);

  const currentTotalValue = holdings.reduce((acc, h) => acc + (h.quantity * h.currentPrice), 0);
  const totalPL = holdings.reduce((acc, h) => acc + ((h.currentPrice - h.avgCost) * h.quantity), 0);
  const plPercent = currentTotalValue > 0 ? (totalPL / (currentTotalValue - totalPL)) * 100 : 0;

  const formatVND = (num: number) => {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(num);
  };

  const handleAddTransaction = (e: React.FormEvent) => {
    e.preventDefault();
    setIsSaving(true);
    setTimeout(() => {
        const qty = parseFloat(formData.quantity);
        const price = parseFloat(formData.price);
        const currentPrice = price * (1 + (Math.random() * 0.1 - 0.05));
        const newItem: PortfolioItem = {
            id: Date.now(),
            symbol: formData.symbol.toUpperCase(),
            quantity: qty,
            avgCost: price,
            currentPrice: currentPrice,
            pl: (currentPrice - price) * qty
        };
        setHoldings(prev => [...prev, newItem]);
        setIsSaving(false);
        setIsModalOpen(false);
        setFormData({ symbol: '', quantity: '', price: '' });
    }, 800);
  };

  const handleConfirmDelete = async () => {
    if (deleteConfirmId === null) return;
    setIsDeleting(true);
    
    // Simulate API call to delete asset
    await new Promise(resolve => setTimeout(resolve, 1000));
    
    setHoldings(prev => prev.filter(h => h.id !== deleteConfirmId));
    setIsDeleting(false);
    setDeleteConfirmId(null);
  };

  if (!user) return <div className="p-10 text-center font-bold text-slate-500">{t('common.login_required')}</div>;
  if (isLoading) return <PortfolioSkeleton />;

  const deletingItem = holdings.find(h => h.id === deleteConfirmId);

  return (
    <div className="max-w-6xl mx-auto space-y-8 md:space-y-10 animate-in fade-in slide-in-from-bottom-2 duration-700 pb-20 px-2 md:px-0">
      
      {/* Portfolio Header */}
      <div className="flex flex-col sm:flex-row gap-6 items-start sm:items-center justify-between px-2">
        <div className="space-y-1">
          <h1 className="text-3xl md:text-4xl font-black text-slate-900 dark:text-white tracking-tighter">{t('port.title')}</h1>
          <p className="text-slate-400 font-bold flex items-center gap-2 text-xs md:text-sm uppercase tracking-widest">
            <span className="w-1.5 h-1.5 rounded-full bg-blue-500 shadow-[0_0_8px_rgba(59,130,246,0.5)]"></span>
            {user.name}'s Financial Hub
          </p>
        </div>
        <Button onClick={() => setIsModalOpen(true)} className="rounded-2xl px-6 py-2.5 md:py-3 shadow-xl shadow-blue-500/10 active:scale-95 transition-all w-full sm:w-auto">
          <Plus size={20} className="mr-2" /> {t('port.add_trans')}
        </Button>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 md:gap-8 items-stretch">
        
        {/* Asset Allocation Card */}
        <Card className="lg:col-span-7 p-6 md:p-10 relative overflow-hidden rounded-[2rem] md:rounded-[2.5rem] border-slate-100 dark:border-slate-800 shadow-2xl shadow-slate-200/50 dark:shadow-none flex flex-col transition-all">
            <div className="flex items-center gap-4 mb-8 md:mb-10">
                <div className="w-10 h-10 md:w-12 md:h-12 rounded-xl md:rounded-2xl bg-indigo-50 dark:bg-indigo-900/30 flex items-center justify-center text-indigo-600 transition-colors">
                    <PieChartIcon size={20} className="md:size-6" />
                </div>
                <div>
                    <h3 className="font-black text-slate-900 dark:text-white tracking-tight text-lg md:text-xl">{t('port.allocation')}</h3>
                    <p className="text-[10px] text-slate-400 font-black uppercase tracking-[0.2em]">{t('port.strategy_insight')}</p>
                </div>
            </div>
            
            <div className="flex flex-col md:flex-row items-center justify-center gap-8 md:gap-10 flex-1">
              <div className="h-56 w-56 md:h-64 md:w-64 relative shrink-0">
                <ResponsiveContainer width="100%" height="100%">
                  <PieChart onMouseLeave={() => setActiveIndex(null)}>
                    <Pie
                      activeIndex={activeIndex === null ? undefined : activeIndex}
                      activeShape={renderActiveShape}
                      data={chartData}
                      innerRadius={70}
                      outerRadius={90}
                      paddingAngle={6}
                      dataKey="value"
                      stroke="none"
                      onMouseEnter={(_, index) => setActiveIndex(index)}
                      animationDuration={1500}
                    >
                      {chartData.map((entry, index) => (
                        <Cell key={`cell-${index}`} fill={entry.color} />
                      ))}
                    </Pie>
                    <Tooltip content={<div className="hidden" />} />
                  </PieChart>
                </ResponsiveContainer>
                
                <div className="absolute inset-0 flex flex-col items-center justify-center pointer-events-none">
                  {activeIndex !== null ? (
                    <div className="animate-in fade-in zoom-in-95 duration-200 text-center">
                        <p className="text-[10px] text-slate-400 font-black uppercase tracking-widest leading-none">{chartData[activeIndex].name}</p>
                        <p className="text-2xl md:text-3xl font-black text-slate-900 dark:text-white tracking-tighter mt-1">
                            {chartData[activeIndex].percent.toFixed(0)}%
                        </p>
                    </div>
                  ) : (
                    <div className="text-center">
                      <p className="text-[10px] text-slate-400 font-black uppercase tracking-widest leading-none mb-1">{t('port.total_assets')}</p>
                      <p className="text-4xl md:text-5xl font-black text-slate-900 dark:text-white tracking-tighter leading-none">
                          {holdings.length}
                      </p>
                      <p className="text-[10px] text-slate-400 font-black uppercase tracking-widest mt-2">{t('port.assets_label')}</p>
                    </div>
                  )}
                </div>
              </div>

              <div className="w-full max-w-xs grid grid-cols-1 gap-2.5 md:gap-3">
                  {chartData.map((d, i) => (
                      <button 
                        key={d.name} 
                        onMouseEnter={() => setActiveIndex(i)}
                        onMouseLeave={() => setActiveIndex(null)}
                        className={`
                            flex items-center justify-between p-3.5 md:p-4 rounded-2xl md:rounded-[1.5rem] transition-all duration-300 border
                            ${activeIndex === i 
                                ? 'bg-white dark:bg-slate-800 border-indigo-500 shadow-xl md:-translate-y-1' 
                                : 'bg-slate-50 dark:bg-slate-800/20 border-transparent hover:border-slate-200 dark:hover:border-slate-700'
                            }
                        `}
                      >
                          <div className="flex items-center gap-3 overflow-hidden">
                              <div className="w-2.5 h-2.5 rounded-full shrink-0" style={{backgroundColor: d.color, boxShadow: `0 0 10px ${d.color}66`}}></div>
                              <div className="text-left overflow-hidden">
                                <p className="font-black text-slate-900 dark:text-slate-100 text-sm tracking-tight leading-none mb-1">{d.name}</p>
                                <p className="text-[10px] text-slate-400 font-black font-mono tracking-tighter">
                                    {formatVND(d.value)}
                                </p>
                              </div>
                          </div>
                          <span className="text-[10px] font-black text-white bg-slate-900 dark:bg-slate-700 px-2 py-1 rounded-lg transition-colors">
                            {d.percent.toFixed(1)}%
                          </span>
                      </button>
                  ))}
              </div>
            </div>
        </Card>

        {/* Total Net Worth Card */}
        <Card className={`
          lg:col-span-5 flex flex-col p-6 md:p-10 relative overflow-hidden rounded-[2rem] md:rounded-[2.5rem] shadow-2xl transition-all duration-500 group
          ${theme === 'dark' 
            ? 'bg-[#0a0f1e] !border-0 text-white' 
            : 'bg-white border-slate-100 text-slate-900'
          }
        `}>
             <div className="absolute -right-20 -top-20 w-64 h-64 bg-blue-600/10 dark:bg-blue-600/20 rounded-full blur-[100px] group-hover:bg-blue-600/20 transition-colors duration-700"></div>
             <div className="absolute -left-20 -bottom-20 w-48 h-48 bg-indigo-500/5 dark:bg-indigo-500/10 rounded-full blur-[80px]"></div>

             <div className="relative z-10 flex flex-col h-full space-y-8 md:space-y-12">
                <div>
                    <div className={`${theme === 'dark' ? 'text-blue-400/80' : 'text-blue-600/80'} text-[10px] font-black uppercase tracking-[0.3em] mb-3 flex items-center gap-3`}>
                        <TrendingUp size={16} /> {t('port.net_worth').toUpperCase()}
                    </div>
                    <div className={`text-3xl md:text-5xl font-black tracking-tighter leading-none ${theme === 'dark' ? 'text-white' : 'text-slate-900'} transition-colors`}>
                        {formatVND(currentTotalValue)}
                    </div>
                </div>

                <div className={`pt-8 md:pt-10 border-t ${theme === 'dark' ? 'border-white/5' : 'border-slate-100'} space-y-6 transition-colors`}>
                    <div>
                        <div className="text-slate-500 text-[10px] font-black uppercase tracking-widest mb-3">{t('port.pl_perf')}</div>
                        <div className={`text-2xl md:text-3xl font-black tracking-tight ${totalPL >= 0 ? 'text-emerald-500' : 'text-rose-500'}`}>
                            {totalPL >= 0 ? '+' : ''}{formatVND(totalPL)}
                        </div>
                    </div>
                    
                    <div className="flex items-center justify-between">
                         <div className={`inline-flex items-center px-4 py-1.5 rounded-full text-[10px] md:text-xs font-black shadow-sm ${totalPL >= 0 ? 'bg-emerald-500/10 text-emerald-500 border border-emerald-500/20' : 'bg-rose-500/10 text-rose-500 border border-rose-500/20'}`}>
                            {totalPL >= 0 ? t('port.profit_status') : t('port.loss_status')} {plPercent.toFixed(2)}%
                        </div>
                        <button className={`w-10 h-10 md:w-12 md:h-12 rounded-[1.25rem] flex items-center justify-center transition-all border backdrop-blur-md ${theme === 'dark' ? 'bg-white/5 text-white/40 border-white/5 hover:bg-white/10' : 'bg-slate-50 text-slate-400 border-slate-100 hover:bg-slate-100'}`}>
                            <Info size={20} />
                        </button>
                    </div>
                </div>
             </div>
        </Card>
      </div>

      {/* Asset Positions List */}
      <div className="space-y-6">
        <div className="flex items-center justify-between px-2 md:px-4">
            <h3 className="font-black text-slate-900 dark:text-white text-2xl md:text-3xl tracking-tighter flex items-center gap-3 md:gap-4">
                {t('port.table.asset')} Positions
                <div className="p-2 bg-white dark:bg-slate-800 rounded-xl md:rounded-2xl border border-slate-100 dark:border-slate-700 shadow-sm">
                   <Sparkles size={16} className="md:size-[18px] text-slate-400" />
                </div>
            </h3>
        </div>

        <div className="overflow-hidden rounded-[2rem] md:rounded-[2.5rem] border border-slate-100 dark:border-slate-800 bg-white dark:bg-slate-900 shadow-2xl shadow-slate-200/50 dark:shadow-none">
            <div className="hidden sm:block overflow-x-auto no-scrollbar">
                <table className="w-full text-left border-collapse">
                <thead>
                    <tr className="bg-slate-50/50 dark:bg-slate-950/50 border-b border-slate-100 dark:border-slate-800">
                        <th className="px-10 py-8 text-[11px] font-black text-slate-400 uppercase tracking-[0.2em]">{t('port.table.asset')}</th>
                        <th className="px-10 py-8 text-[11px] font-black text-slate-400 uppercase tracking-[0.2em] text-right">{t('port.table.qty')}</th>
                        <th className="px-10 py-8 text-[11px] font-black text-slate-400 uppercase tracking-[0.2em] text-right">{t('port.table.avg')}</th>
                        <th className="px-10 py-8 text-[11px] font-black text-slate-400 uppercase tracking-[0.2em] text-right">{t('port.table.cur')}</th>
                        <th className="px-10 py-8 text-[11px] font-black text-slate-400 uppercase tracking-[0.2em] text-right">{t('port.table.pl')}</th>
                        <th className="px-10 py-8 text-[11px] font-black text-slate-400 uppercase tracking-[0.2em] text-center">ACTION</th>
                    </tr>
                </thead>
                <tbody className="divide-y divide-slate-50 dark:divide-slate-800/40">
                    {holdings.length === 0 ? (
                        <tr><td colSpan={6} className="px-10 py-20 text-center text-slate-400 font-bold italic">{t('common.no_data')}</td></tr>
                    ) : (
                        holdings
                        .sort((a,b) => (b.quantity * b.currentPrice) - (a.quantity * a.currentPrice))
                        .map((h) => {
                            const itemPL = (h.currentPrice - h.avgCost) * h.quantity;
                            const isLoss = itemPL < 0;
                            return (
                            <tr key={h.id} className="hover:bg-slate-50/50 dark:hover:bg-slate-800/20 transition-all group">
                                <td className="px-10 py-10">
                                    <div className="flex items-center gap-6">
                                        <div className="w-14 h-14 rounded-[1.5rem] bg-blue-50 dark:bg-blue-900/30 flex items-center justify-center font-black text-blue-600 text-xl shadow-inner border border-blue-100/50 dark:border-blue-800/50 transition-transform group-hover:scale-110">
                                            {h.symbol[0]}
                                        </div>
                                        <div>
                                            <p className="font-black text-slate-900 dark:text-white text-lg tracking-tighter leading-none mb-1">{h.symbol}</p>
                                            <p className="text-[10px] text-slate-400 font-black uppercase tracking-widest opacity-80">{t('port.verified_asset')}</p>
                                        </div>
                                    </div>
                                </td>
                                <td className="px-10 py-10 text-right font-mono font-black text-slate-900 dark:text-white text-base">
                                    {h.quantity.toLocaleString()}
                                </td>
                                <td className="px-10 py-10 text-right font-mono text-sm text-slate-400 font-bold">
                                    {formatVND(h.avgCost)}
                                </td>
                                <td className="px-10 py-10 text-right font-mono font-black text-slate-900 dark:text-white text-base">
                                    {formatVND(h.currentPrice)}
                                </td>
                                <td className="px-10 py-10 text-right">
                                    <div className={`inline-flex items-center px-4 py-2 rounded-2xl text-xs font-black font-mono shadow-sm
                                        ${isLoss 
                                            ? 'text-rose-600 bg-rose-50 dark:bg-rose-900/20' 
                                            : 'text-emerald-600 bg-emerald-50 dark:bg-emerald-900/20'
                                        }`}>
                                        {itemPL > 0 ? '+' : ''}{formatVND(itemPL)}
                                    </div>
                                </td>
                                <td className="px-10 py-10 text-center">
                                    <button 
                                        onClick={() => setDeleteConfirmId(h.id)}
                                        className="w-10 h-10 flex items-center justify-center mx-auto text-slate-300 hover:text-rose-500 hover:bg-rose-50 dark:hover:bg-rose-950 rounded-2xl transition-all"
                                    >
                                        <Trash2 size={20} />
                                    </button>
                                </td>
                            </tr>
                            );
                        })
                    )}
                </tbody>
                </table>
            </div>

            <div className="sm:hidden flex flex-col divide-y divide-slate-50 dark:divide-slate-800/50">
                {holdings.length === 0 ? (
                    <div className="p-10 text-center text-slate-400 font-bold italic">{t('common.no_data')}</div>
                ) : (
                    holdings.map((h) => {
                        const itemPL = (h.currentPrice - h.avgCost) * h.quantity;
                        const isLoss = itemPL < 0;
                        return (
                            <div key={h.id} className="p-6 space-y-4">
                                <div className="flex items-center justify-between">
                                    <div className="flex items-center gap-4">
                                        <div className="w-10 h-10 rounded-xl bg-blue-50 dark:bg-blue-900/30 flex items-center justify-center font-black text-blue-600">
                                            {h.symbol[0]}
                                        </div>
                                        <div>
                                            <p className="font-black text-slate-900 dark:text-white tracking-tight leading-none mb-1">{h.symbol}</p>
                                            <p className="text-[10px] text-slate-400 font-black uppercase tracking-widest">{h.quantity.toLocaleString()} Units</p>
                                        </div>
                                    </div>
                                    <button onClick={() => setDeleteConfirmId(h.id)} className="p-2 text-slate-300">
                                        <Trash2 size={18} />
                                    </button>
                                </div>
                                <div className="grid grid-cols-2 gap-4">
                                    <div>
                                        <p className="text-[9px] text-slate-400 font-black uppercase tracking-widest mb-1">{t('port.table.cur').toUpperCase()}</p>
                                        <p className="font-mono font-black text-slate-900 dark:text-white text-sm">{formatVND(h.currentPrice)}</p>
                                    </div>
                                    <div className="text-right">
                                        <p className="text-[9px] text-slate-400 font-black uppercase tracking-widest mb-1">{t('port.table.pl').toUpperCase()}</p>
                                        <p className={`font-mono font-black text-sm ${isLoss ? 'text-rose-500' : 'text-emerald-500'}`}>
                                            {itemPL > 0 ? '+' : ''}{formatVND(itemPL)}
                                        </p>
                                    </div>
                                </div>
                            </div>
                        );
                    })
                )}
            </div>
        </div>
      </div>

      {/* Add Transaction Modal */}
      <Modal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} title={t('port.modal.title')}>
        <form onSubmit={handleAddTransaction} className="space-y-6">
            <Input label={t('port.modal.symbol')} placeholder="e.g. BTC, VNM" value={formData.symbol} onChange={e => setFormData({...formData, symbol: e.target.value})} required className="rounded-2xl font-black h-14 px-6" />
            <div className="grid grid-cols-2 gap-5">
                <Input label={t('port.modal.qty')} type="number" placeholder="0" value={formData.quantity} onChange={e => setFormData({...formData, quantity: e.target.value})} required min="0" step="any" className="rounded-2xl font-mono font-bold h-14 px-6" />
                <Input label={t('port.modal.price')} type="number" placeholder="0" value={formData.price} onChange={e => setFormData({...formData, price: e.target.value})} required min="0" className="rounded-2xl font-mono font-bold h-14 px-6" />
            </div>
            <div className="flex flex-col sm:flex-row justify-end gap-3 pt-8 border-t dark:border-slate-800 mt-4">
                <Button type="button" variant="ghost" onClick={() => setIsModalOpen(false)} className="font-bold rounded-2xl px-8 h-12 w-full sm:w-auto">{t('common.cancel')}</Button>
                <Button type="submit" disabled={isSaving} className="font-black px-12 h-12 rounded-2xl shadow-xl shadow-blue-500/20 active:scale-95 transition-all w-full sm:w-auto">
                    {isSaving ? t('common.loading') : t('port.modal.add')}
                </Button>
            </div>
        </form>
      </Modal>

      {/* Concise Delete Confirmation Modal */}
      <Modal isOpen={deleteConfirmId !== null} onClose={() => !isDeleting && setDeleteConfirmId(null)} title={t('port.close')}>
        <div className="space-y-5">
            <div className="flex items-center gap-4 p-4 bg-rose-50 dark:bg-rose-950/30 rounded-xl border border-rose-100 dark:border-rose-900/50">
                <div className="w-10 h-10 rounded-lg bg-white dark:bg-slate-900 flex items-center justify-center text-rose-500 shrink-0 shadow-sm">
                    <AlertTriangle size={20} />
                </div>
                <div className="space-y-0.5">
                    <h4 className="font-black text-slate-900 dark:text-white text-sm">{t('port.remove_confirm')}</h4>
                    <p className="text-xs text-slate-500 font-medium">
                        {deletingItem?.symbol} - {deletingItem?.quantity.toLocaleString()} units
                    </p>
                </div>
            </div>
            
            <div className="flex gap-3">
                <Button 
                    variant="ghost" 
                    onClick={() => setDeleteConfirmId(null)} 
                    disabled={isDeleting}
                    className="flex-1 font-bold rounded-xl h-12"
                >
                    {t('common.cancel')}
                </Button>
                <Button 
                    onClick={handleConfirmDelete} 
                    disabled={isDeleting}
                    className="flex-1 font-black h-12 rounded-xl bg-rose-500 hover:bg-rose-600 text-white shadow-lg shadow-rose-500/10 active:scale-95 transition-all"
                >
                    {isDeleting ? <Loader2 size={18} className="animate-spin" /> : t('common.submit')}
                </Button>
            </div>
        </div>
      </Modal>
    </div>
  );
};
