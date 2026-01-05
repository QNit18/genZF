
import React from 'react';
import { Outlet, Link } from 'react-router-dom';
import { useApp } from '../context/AppContext';
import { Sun, Moon, ArrowLeft } from 'lucide-react';

export const AuthLayout: React.FC = () => {
  const { theme, toggleTheme, t } = useApp();

  return (
    <div className="min-h-screen flex items-center justify-center bg-slate-50 dark:bg-slate-950 text-slate-900 dark:text-slate-100 transition-colors duration-200 p-4">
      
      <div className="w-full max-w-md bg-white dark:bg-slate-900 p-8 rounded-2xl shadow-xl border border-slate-200 dark:border-slate-800 relative">
        {/* Top Actions */}
        <div className="absolute top-6 right-6 flex items-center gap-4">
          <button onClick={toggleTheme} className="p-2 rounded-full hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors">
            {theme === 'light' ? <Moon size={20} /> : <Sun size={20} />}
          </button>
        </div>
        
        <div className="absolute top-6 left-6">
             <Link to="/" className="flex items-center gap-2 text-sm text-slate-500 hover:text-slate-900 dark:hover:text-slate-200 transition-colors">
                <ArrowLeft size={16}/> {t('nav.home')}
             </Link>
        </div>

        <div className="mt-8 space-y-8 animate-in slide-in-from-bottom-4 duration-500">
           {/* Logo */}
           <div className="flex justify-center mb-8">
              <div className="w-12 h-12 bg-blue-600 rounded-xl flex items-center justify-center text-white font-bold text-2xl shadow-lg shadow-blue-600/20">Z</div>
           </div>

           <Outlet />
        </div>
      </div>
    </div>
  );
};
