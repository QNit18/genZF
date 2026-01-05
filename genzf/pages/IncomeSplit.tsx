
import React, { useState } from 'react';
import { useApp } from '../context/AppContext';
import { Card } from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { Info, RefreshCw, Save, CheckCircle2 } from 'lucide-react';

export const IncomeSplit: React.FC = () => {
  const { user, t } = useApp();
  const [income, setIncome] = useState<number>(20000000); // 20M VND default
  const [ratios, setRatios] = useState({ needs: 50, wants: 30, save: 20 });
  const [isSaved, setIsSaved] = useState(false);

  if (!user) return <div className="p-10 text-center">{t('common.login_required')}</div>;

  const handleSliderChange = (changedKey: keyof typeof ratios, val: number) => {
    const otherKeys = (Object.keys(ratios) as Array<keyof typeof ratios>).filter(k => k !== changedKey);
    const sumOthers = otherKeys.reduce((acc: number, key) => acc + ratios[key], 0);
    const maxAllowed = 100 - sumOthers;

    let newValue = Math.max(0, Math.min(maxAllowed, val));

    setRatios(prev => ({
        ...prev,
        [changedKey]: newValue
    }));
    setIsSaved(false);
  };

  const needsVal = (income * ratios.needs) / 100;
  const wantsVal = (income * ratios.wants) / 100;
  const saveVal = (income * ratios.save) / 100;
  
  const totalPercent = ratios.needs + ratios.wants + ratios.save;
  const unallocated = 100 - totalPercent;

  const maxNeeds = 100 - ratios.wants - ratios.save;
  const maxWants = 100 - ratios.needs - ratios.save;
  const maxSave = 100 - ratios.needs - ratios.wants;

  const formatVND = (num: number) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(num);

  const handleSave = () => {
    setIsSaved(true);
    setTimeout(() => setIsSaved(false), 3000);
  };

  return (
    <div className="max-w-3xl mx-auto space-y-8 animate-in fade-in slide-in-from-bottom-4 duration-700 pb-20">
      <div className="text-center space-y-2">
        <h1 className="text-3xl font-black text-slate-900 dark:text-white tracking-tight">{t('split.title')}</h1>
        <p className="text-slate-500 font-medium max-w-md mx-auto">{t('split.subtitle')}</p>
      </div>

      {/* Income Input Section */}
      <div className="flex justify-center">
        <div className="relative inline-block w-full max-w-sm group">
            <div className="absolute -inset-1 bg-gradient-to-r from-blue-500 to-indigo-500 rounded-2xl blur opacity-10 group-focus-within:opacity-25 transition-opacity duration-500"></div>
            <div className="relative flex flex-col items-center bg-white dark:bg-slate-900 p-6 rounded-2xl border border-slate-200 dark:border-slate-800 shadow-xl">
                <span className="text-xs font-black uppercase tracking-widest text-slate-400 mb-2">{t('split.income')}</span>
                <div className="flex items-center gap-2">
                    <span className="text-2xl font-black text-blue-500">₫</span>
                    <input 
                        type="number" 
                        value={income} 
                        onChange={(e) => setIncome(Number(e.target.value))}
                        className="w-full text-center text-4xl font-black bg-transparent outline-none text-slate-900 dark:text-white placeholder-slate-200"
                        placeholder="0"
                    />
                </div>
            </div>
        </div>
      </div>

      <div className="space-y-10 p-6 md:p-10 bg-white dark:bg-slate-900 rounded-[2.5rem] border border-slate-100 dark:border-slate-800 shadow-2xl shadow-slate-200/50 dark:shadow-none">
        {/* Visual Bar - Replaced Striped Unallocated with Clean Neutral Segment */}
        <div className="space-y-3">
            <div className="flex justify-between text-[10px] font-black uppercase tracking-widest text-slate-400 px-1">
                <span>{t('split.visualizer')}</span>
                <span className={unallocated > 0 ? "text-amber-500" : "text-emerald-500"}>
                    {unallocated > 0 ? `${unallocated}% ${t('split.unallocated')}` : t('split.fully_allocated')}
                </span>
            </div>
            <div className="relative w-full h-10 bg-slate-100 dark:bg-slate-800 rounded-2xl overflow-hidden flex shadow-inner">
                <div style={{ width: `${ratios.needs}%` }} className="bg-gradient-to-r from-blue-400 to-blue-500 h-full transition-all duration-500 flex items-center justify-center text-[10px] text-white font-black shadow-[inset_-2px_0_10px_rgba(0,0,0,0.1)]">{ratios.needs > 8 && t('split.needs')}</div>
                <div style={{ width: `${ratios.wants}%` }} className="bg-gradient-to-r from-purple-400 to-purple-500 h-full transition-all duration-500 flex items-center justify-center text-[10px] text-white font-black shadow-[inset_-2px_0_10px_rgba(0,0,0,0.1)]">{ratios.wants > 8 && t('split.wants')}</div>
                <div style={{ width: `${ratios.save}%` }} className="bg-gradient-to-r from-emerald-400 to-emerald-500 h-full transition-all duration-500 flex items-center justify-center text-[10px] text-white font-black shadow-[inset_-2px_0_10px_rgba(0,0,0,0.1)]">{ratios.save > 8 && t('split.save')}</div>
                {unallocated > 0 && (
                    <div style={{ width: `${unallocated}%` }} className="bg-slate-200 dark:bg-slate-700 h-full transition-all duration-500"></div>
                )}
            </div>
        </div>

        <div className="grid grid-cols-1 gap-12">
            {/* Needs */}
            <div className="space-y-5">
                <div className="flex justify-between items-center">
                    <div className="flex items-center gap-3">
                        <div className="w-10 h-10 rounded-2xl bg-blue-50 dark:bg-blue-900/30 flex items-center justify-center text-blue-600">
                             <Info size={18} />
                        </div>
                        <div>
                            <span className="text-blue-600 dark:text-blue-400 font-black tracking-tight">{t('split.needs')} ({ratios.needs}%)</span>
                            <p className="text-[10px] text-slate-400 font-bold uppercase tracking-wider leading-none mt-1">{t('split.needs_desc')}</p>
                        </div>
                    </div>
                    <span className="font-mono font-black text-lg text-slate-900 dark:text-white">
                        {formatVND(needsVal)}
                    </span>
                </div>
                <div className="px-1">
                    <input 
                        type="range" min="0" max={maxNeeds}
                        value={ratios.needs} 
                        onChange={(e) => handleSliderChange('needs', Number(e.target.value))}
                        className="w-full h-3 bg-slate-100 dark:bg-slate-800 rounded-full appearance-none cursor-pointer accent-blue-500 focus:outline-none focus:ring-4 focus:ring-blue-500/10"
                    />
                </div>
            </div>

            {/* Wants */}
            <div className="space-y-5">
                <div className="flex justify-between items-center">
                    <div className="flex items-center gap-3">
                        <div className="w-10 h-10 rounded-2xl bg-purple-50 dark:bg-purple-900/30 flex items-center justify-center text-purple-600">
                             <RefreshCw size={18} />
                        </div>
                        <div>
                            <span className="text-purple-600 dark:text-purple-400 font-black tracking-tight">{t('split.wants')} ({ratios.wants}%)</span>
                            <p className="text-[10px] text-slate-400 font-bold uppercase tracking-wider leading-none mt-1">{t('split.wants_desc')}</p>
                        </div>
                    </div>
                    <span className="font-mono font-black text-lg text-slate-900 dark:text-white">
                        {formatVND(wantsVal)}
                    </span>
                </div>
                <div className="px-1">
                    <input 
                        type="range" min="0" max={maxWants}
                        value={ratios.wants} 
                        onChange={(e) => handleSliderChange('wants', Number(e.target.value))}
                        className="w-full h-3 bg-slate-100 dark:bg-slate-800 rounded-full appearance-none cursor-pointer accent-purple-500 focus:outline-none focus:ring-4 focus:ring-purple-500/10"
                    />
                </div>
            </div>

            {/* Savings */}
            <div className="space-y-5">
                <div className="flex justify-between items-center">
                    <div className="flex items-center gap-3">
                        <div className="w-10 h-10 rounded-2xl bg-emerald-50 dark:bg-emerald-900/30 flex items-center justify-center text-emerald-600">
                             <Save size={18} />
                        </div>
                        <div>
                            <span className="text-emerald-600 dark:text-emerald-400 font-black tracking-tight">{t('split.save')} ({ratios.save}%)</span>
                            <p className="text-[10px] text-slate-400 font-bold uppercase tracking-wider leading-none mt-1">{t('split.save_desc')}</p>
                        </div>
                    </div>
                    <span className="font-mono font-black text-lg text-slate-900 dark:text-white">
                    {formatVND(saveVal)}
                    </span>
                </div>
                <div className="px-1">
                    <input 
                        type="range" min="0" max={maxSave}
                        value={ratios.save} 
                        onChange={(e) => handleSliderChange('save', Number(e.target.value))}
                        className="w-full h-3 bg-slate-100 dark:bg-slate-800 rounded-full appearance-none cursor-pointer accent-emerald-500 focus:outline-none focus:ring-4 focus:ring-emerald-500/10"
                    />
                </div>
            </div>
        </div>
      </div>
      
      <div className="flex flex-col sm:flex-row items-center justify-center gap-4 pt-4">
        <Button variant="ghost" className="font-bold text-slate-400" onClick={() => setRatios({ needs: 50, wants: 30, save: 20 })}>{t('split.reset')}</Button>
        <Button onClick={handleSave} className="font-black px-10 relative overflow-hidden group">
            <span className={`flex items-center gap-2 transition-transform duration-300 ${isSaved ? '-translate-y-12' : 'translate-y-0'}`}>
                <Save size={18} /> {t('split.save_plan')}
            </span>
            <span className={`absolute inset-0 flex items-center justify-center gap-2 transition-transform duration-300 ${isSaved ? 'translate-y-0' : 'translate-y-12'}`}>
                <CheckCircle2 size={18} /> {t('split.success')}
            </span>
        </Button>
      </div>

      {/* Smart Insight Box */}
      <div className="bg-blue-50/50 dark:bg-blue-900/10 border border-blue-100 dark:border-blue-900/30 p-6 rounded-[2rem] flex items-start gap-4">
         <div className="p-3 bg-white dark:bg-slate-800 rounded-2xl shadow-sm text-blue-500">
            <Info size={24} />
         </div>
         <div className="space-y-1">
            <h4 className="font-black text-slate-900 dark:text-slate-100 text-sm">{t('split.pro_tip')}</h4>
            <p className="text-xs text-slate-500 leading-relaxed">
                {t('split.pro_tip_desc')}
            </p>
         </div>
      </div>
    </div>
  );
};
