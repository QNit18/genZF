
import React, { useRef, useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { TrendingUp, Calculator, PieChart, Wallet, ChevronLeft, ChevronRight } from 'lucide-react';
import { useApp } from '../context/AppContext';
import { Card } from '../components/ui/Card';
import { MarketCard } from '../components/MarketCard';
import { MarketCardSkeleton } from '../components/MarketCardSkeleton';
import { getHomeAssets } from '../services/assetService';
import { MarketItem } from '../types';

export const Home: React.FC = () => {
  const { t, user } = useApp();
  const navigate = useNavigate();
  const scrollContainerRef = useRef<HTMLDivElement>(null);
  
  // State to track scroll availability
  const [canScrollLeft, setCanScrollLeft] = useState(false);
  const [canScrollRight, setCanScrollRight] = useState(true);
  
  // State for market data
  const [marketData, setMarketData] = useState<MarketItem[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const features = [
    { 
      title: t('home.feat.profit'), 
      icon: <TrendingUp className="text-blue-500" />, 
      desc: t('home.feat.profit_desc'), 
      path: "/calculators/profit" 
    },
    { 
      title: t('home.feat.tax'), 
      icon: <Calculator className="text-purple-500" />, 
      desc: t('home.feat.tax_desc'), 
      path: "/calculators/tax" 
    },
    { 
      title: t('nav.markets'), 
      icon: <PieChart className="text-amber-500" />, 
      desc: t('home.feat.markets_desc'), 
      path: "/markets" 
    },
    { 
      title: t('nav.portfolio'), 
      icon: <Wallet className="text-emerald-500" />, 
      desc: user ? t('home.feat.portfolio_user') : t('home.feat.portfolio_guest'), 
      path: user ? "/portfolio" : "/auth/login" 
    },
  ];

  const checkScroll = () => {
    if (scrollContainerRef.current) {
      const { scrollLeft, scrollWidth, clientWidth } = scrollContainerRef.current;
      setCanScrollLeft(scrollLeft > 0);
      // Use a small buffer (1px) for floating point math safety
      setCanScrollRight(Math.ceil(scrollLeft + clientWidth) < scrollWidth);
    }
  };

  // Fetch home assets from API
  useEffect(() => {
    const fetchHomeAssets = async () => {
      setIsLoading(true);
      setError(null);
      try {
        const assets = await getHomeAssets();
        setMarketData(assets);
      } catch (err) {
        console.error('Failed to load home assets:', err);
        setError(t('common.error'));
        // Keep empty array on error, will show empty state
        setMarketData([]);
      } finally {
        setIsLoading(false);
      }
    };

    fetchHomeAssets();
  }, [t]);

  useEffect(() => {
    const container = scrollContainerRef.current;
    if (container) {
      // Check initially
      checkScroll();
      
      // Add listeners
      container.addEventListener('scroll', checkScroll);
      window.addEventListener('resize', checkScroll);
      
      return () => {
        container.removeEventListener('scroll', checkScroll);
        window.removeEventListener('resize', checkScroll);
      };
    }
  }, [marketData]); // Re-check scroll when data changes

  const scroll = (direction: 'left' | 'right') => {
    if (scrollContainerRef.current) {
      const scrollAmount = 320; // Approx one card width
      scrollContainerRef.current.scrollBy({
        left: direction === 'left' ? -scrollAmount : scrollAmount,
        behavior: 'smooth'
      });
    }
  };

  return (
    <div className="space-y-8 animate-in fade-in duration-500">
      {/* Header Section */}
      <section>
        <div className="flex items-center justify-between mb-4">
          <h2 className="text-lg font-bold flex items-center gap-2">
            <span className="w-2 h-2 rounded-full bg-red-500 animate-pulse"></span>
            {t('home.snapshot')}
          </h2>
          <div className="hidden md:flex gap-2">
            <button 
              onClick={() => scroll('left')} 
              disabled={!canScrollLeft}
              className={`p-1.5 rounded-full bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 text-slate-500 hover:text-blue-600 hover:border-blue-600 dark:hover:text-blue-400 transition-all shadow-sm ${!canScrollLeft ? 'opacity-0 pointer-events-none' : 'opacity-100'}`}
              aria-label="Scroll left"
            >
              <ChevronLeft size={18} />
            </button>
            <button 
              onClick={() => scroll('right')} 
              disabled={!canScrollRight}
              className={`p-1.5 rounded-full bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 text-slate-500 hover:text-blue-600 hover:border-blue-600 dark:hover:text-blue-400 transition-all shadow-sm ${!canScrollRight ? 'opacity-0 pointer-events-none' : 'opacity-100'}`}
              aria-label="Scroll right"
            >
              <ChevronRight size={18} />
            </button>
          </div>
        </div>
        
        <div 
          ref={scrollContainerRef}
          className="flex gap-4 overflow-x-auto pb-4 no-scrollbar snap-x scroll-smooth"
        >
          {isLoading ? (
            // Show skeleton loaders while loading
            Array.from({ length: 6 }).map((_, idx) => (
              <div key={`skeleton-${idx}`} className="min-w-[280px] md:min-w-[320px] snap-start">
                <MarketCardSkeleton />
              </div>
            ))
          ) : error ? (
            // Show error message
            <div className="w-full text-center py-8 text-slate-500">
              <p>{error}</p>
            </div>
          ) : marketData.length === 0 ? (
            // Show empty state
            <div className="w-full text-center py-8 text-slate-500">
              <p>{t('common.no_data')}</p>
            </div>
          ) : (
            // Show market cards
            marketData.map(item => (
              <div key={item.id} className="min-w-[280px] md:min-w-[320px] snap-start">
                <MarketCard item={item} />
              </div>
            ))
          )}
        </div>
      </section>

      {/* Quick Actions */}
      <section>
        <h2 className="text-lg font-bold mb-4">{t('home.quick_actions')}</h2>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {features.map((feat, idx) => (
            <Card 
              key={idx} 
              className="group hover:shadow-md transition-all cursor-pointer border-l-4 border-l-transparent hover:border-l-blue-500"
              onClick={() => navigate(feat.path)}
            >
              <div className="flex items-start gap-4">
                <div className="p-3 rounded-lg bg-slate-50 dark:bg-slate-800 group-hover:bg-blue-50 dark:group-hover:bg-blue-900/20 transition-colors">
                  {feat.icon}
                </div>
                <div>
                  <h3 className="font-semibold text-slate-900 dark:text-slate-100">{feat.title}</h3>
                  <p className="text-sm text-slate-500 mt-1">{feat.desc}</p>
                </div>
              </div>
            </Card>
          ))}
        </div>
      </section>

      {/* Intro/Trust */}
      <section className="bg-gradient-to-r from-slate-100 to-slate-50 dark:from-slate-900 dark:to-slate-900/50 rounded-xl p-6 text-center border border-slate-200 dark:border-slate-800">
        <p className="text-sm text-slate-600 dark:text-slate-400 max-w-2xl mx-auto">
          {t('home.intro')}
        </p>
      </section>
    </div>
  );
};
