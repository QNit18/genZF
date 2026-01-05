
import React, { useState, useEffect, useMemo } from 'react';
import { Search, Filter, ArrowUpDown, X } from 'lucide-react';
import { MarketCard } from '../components/MarketCard';
import { MarketCardSkeleton } from '../components/MarketCardSkeleton';
import { MOCK_MARKET_DATA } from '../constants';
import { useApp } from '../context/AppContext';

type SortOption = 'name-asc' | 'price-desc' | 'price-asc' | 'change-desc' | 'change-asc';
type FilterCategory = 'all' | 'crypto' | 'forex' | 'commodity' | 'etf';

export const Markets: React.FC = () => {
  const { t } = useApp();
  const [searchTerm, setSearchTerm] = useState('');
  const [category, setCategory] = useState<FilterCategory>('all');
  const [sortBy, setSortBy] = useState<SortOption>('change-desc');
  const [isLoading, setIsLoading] = useState(true);

  // Simulate network fetch
  useEffect(() => {
    const timer = setTimeout(() => {
      setIsLoading(false);
    }, 1200); // 1.2s simulated delay
    return () => clearTimeout(timer);
  }, []);

  const categories: { id: FilterCategory; label: string }[] = [
    { id: 'all', label: t('market.filter_all') },
    { id: 'crypto', label: t('market.filter_crypto') },
    { id: 'forex', label: t('market.filter_forex') },
    { id: 'commodity', label: t('market.filter_commodity') },
    { id: 'etf', label: t('market.filter_etf') },
  ];

  const filteredData = useMemo(() => {
    let data = [...MOCK_MARKET_DATA];

    // Filter by Category
    if (category !== 'all') {
      data = data.filter(item => item.type === category);
    }

    // Filter by Search
    if (searchTerm) {
      const lower = searchTerm.toLowerCase();
      data = data.filter(item => 
        item.name.toLowerCase().includes(lower) || 
        item.symbol.toLowerCase().includes(lower)
      );
    }

    // Sort
    data.sort((a, b) => {
      switch (sortBy) {
        case 'price-desc': return b.price - a.price;
        case 'price-asc': return a.price - b.price;
        case 'change-desc': return b.change - a.change;
        case 'change-asc': return a.change - b.change;
        case 'name-asc': return a.name.localeCompare(b.name);
        default: return 0;
      }
    });

    return data;
  }, [category, searchTerm, sortBy]);

  return (
    <div className="space-y-6 animate-in fade-in duration-300">
      <div className="flex flex-col md:flex-row justify-between items-end md:items-center gap-4">
        <div>
           <h1 className="text-2xl font-bold text-slate-900 dark:text-white">{t('nav.markets')}</h1>
           <p className="text-slate-500">{t('market.subtitle')}</p>
        </div>
      </div>

      {/* Controls */}
      <div className="flex flex-col lg:flex-row gap-4 bg-white dark:bg-slate-900 p-4 rounded-xl border border-slate-200 dark:border-slate-800 shadow-sm">
        {/* Search */}
        <div className="relative flex-1">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" size={18} />
          <input 
            type="text" 
            placeholder={t('common.search_placeholder')}
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            disabled={isLoading}
            className="w-full pl-10 pr-10 py-2 rounded-lg bg-slate-50 dark:bg-slate-950 border border-slate-200 dark:border-slate-800 focus:outline-none focus:ring-2 focus:ring-blue-500 transition-all disabled:opacity-50"
          />
          {searchTerm && (
            <button 
              onClick={() => setSearchTerm('')}
              className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600 dark:hover:text-slate-200 p-1 rounded-full hover:bg-slate-200 dark:hover:bg-slate-800 transition-colors"
            >
              <X size={14} />
            </button>
          )}
        </div>

        {/* Filters */}
        <div className="flex items-center gap-2 shrink-0">
          <Filter size={18} className="text-slate-400 mr-1" />
          
          <div className="md:hidden w-full min-w-[150px]">
            <select
              value={category}
              onChange={(e) => setCategory(e.target.value as FilterCategory)}
              disabled={isLoading}
              className="w-full px-3 py-2 rounded-lg bg-slate-50 dark:bg-slate-950 border border-slate-200 dark:border-slate-800 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 disabled:opacity-50"
            >
              {categories.map(cat => (
                <option key={cat.id} value={cat.id}>
                  {cat.label}
                </option>
              ))}
            </select>
          </div>

          <div className="hidden md:flex items-center gap-2 overflow-x-auto no-scrollbar">
            {categories.map(cat => (
              <button
                key={cat.id}
                onClick={() => setCategory(cat.id)}
                disabled={isLoading}
                className={`px-4 py-2 rounded-full text-sm font-medium whitespace-nowrap transition-colors disabled:opacity-50 ${
                  category === cat.id 
                  ? 'bg-blue-600 text-white shadow-md' 
                  : 'bg-slate-100 dark:bg-slate-800 text-slate-600 dark:text-slate-300 hover:bg-slate-200 dark:hover:bg-slate-700'
                }`}
              >
                {cat.label}
              </button>
            ))}
          </div>
        </div>

        {/* Sort */}
        <div className="flex items-center gap-2 shrink-0">
          <ArrowUpDown size={18} className="text-slate-400" />
          <select 
            value={sortBy}
            onChange={(e) => setSortBy(e.target.value as SortOption)}
            disabled={isLoading}
            className="px-3 py-2 rounded-lg bg-slate-50 dark:bg-slate-950 border border-slate-200 dark:border-slate-800 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 disabled:opacity-50"
          >
            <option value="change-desc">{t('market.sort_change_desc')}</option>
            <option value="change-asc">{t('market.sort_change_asc')}</option>
            <option value="price-desc">{t('market.sort_price_desc')}</option>
            <option value="price-asc">{t('market.sort_price_asc')}</option>
            <option value="name-asc">{t('market.sort_name')}</option>
          </select>
        </div>
      </div>

      {/* Market List - Vertical Stack */}
      {!isLoading && filteredData.length === 0 ? (
        // Empty State
        <div className="text-center py-20 bg-slate-50 dark:bg-slate-900/50 rounded-xl border border-dashed border-slate-300 dark:border-slate-700">
          <p className="text-slate-500 font-medium">{t('common.no_data')}</p>
          <button 
            onClick={() => { setSearchTerm(''); setCategory('all'); }}
            className="text-blue-600 hover:underline text-sm mt-2"
          >
            {t('common.clear_filters')}
          </button>
        </div>
      ) : (
        <div className="grid grid-cols-1 gap-4">
          {isLoading ? (
            // Render Skeletons
            Array.from({ length: 8 }).map((_, idx) => (
              <MarketCardSkeleton key={`skeleton-${idx}`} />
            ))
          ) : (
            // Render Data
            filteredData.map(item => (
               <MarketCard key={item.id} item={item} />
            ))
          )}
        </div>
      )}
    </div>
  );
};
