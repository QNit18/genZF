
import React, { useState, useEffect } from 'react';
import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { Sun, Moon, Menu, X, User as UserIcon } from 'lucide-react';
import { useApp } from '../context/AppContext';
import { Button } from './ui/Button';

export const Layout: React.FC = () => {
  const { theme, toggleTheme, language, setLanguage, user, logout, t } = useApp();
  const [isMenuOpen, setIsMenuOpen] = useState(false);
  const navigate = useNavigate();

  // FIX: Sync PWA Status Bar Color with Theme
  useEffect(() => {
    const metaThemeColor = document.querySelector("meta[name=theme-color]");
    if (metaThemeColor) {
      // Use #ffffff for light mode (matches header) and #020617 for dark mode (matches slate-950)
      metaThemeColor.setAttribute("content", theme === 'light' ? '#ffffff' : '#020617');
    }
  }, [theme]);

  const handleAuth = () => {
    if (user) {
      logout();
      navigate('/');
    } else {
      navigate('/auth/login');
    }
  };

  const navClass = ({ isActive }: { isActive: boolean }) => 
    `text-sm font-medium transition-colors hover:text-blue-600 dark:hover:text-blue-400 ${isActive ? 'text-blue-600 dark:text-blue-400' : 'text-slate-600 dark:text-slate-400'}`;

  return (
    <div className="min-h-screen flex flex-col bg-slate-50 dark:bg-slate-950 text-slate-900 dark:text-slate-100 transition-colors duration-200">
      <header className="sticky top-0 z-50 w-full border-b border-slate-200 dark:border-slate-800 bg-white/80 dark:bg-slate-950/80 backdrop-blur">
        <div className="container mx-auto px-4 h-16 flex items-center justify-between">
          {/* Logo */}
          <div className="flex items-center gap-2">
            <div className="w-8 h-8 bg-blue-600 rounded-lg flex items-center justify-center text-white font-bold">Z</div>
            <span className="text-xl font-bold bg-clip-text text-transparent bg-gradient-to-r from-blue-600 to-indigo-600">GenZF</span>
          </div>

          {/* Desktop Nav */}
          <nav className="hidden md:flex items-center gap-6">
            <NavLink to="/" className={navClass}>{t('nav.home')}</NavLink>
            <NavLink to="/markets" className={navClass}>{t('nav.markets')}</NavLink>
            <NavLink to="/calculators" className={navClass}>{t('nav.calculators')}</NavLink>
            {user && (
               <>
                <NavLink to="/portfolio" className={navClass}>{t('nav.portfolio')}</NavLink>
                <NavLink to="/income-split" className={navClass}>{t('nav.income_split')}</NavLink>
               </>
            )}
          </nav>

          {/* Right Actions */}
          <div className="hidden md:flex items-center gap-3">
            <button onClick={toggleTheme} className="p-2 rounded-full hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors">
              {theme === 'light' ? <Moon size={20} /> : <Sun size={20} />}
            </button>
            
            <div className="flex items-center border border-slate-200 dark:border-slate-700 rounded-lg overflow-hidden">
              <button 
                onClick={() => setLanguage('vi')}
                className={`px-3 py-1 text-xs font-medium ${language === 'vi' ? 'bg-slate-100 dark:bg-slate-800 text-blue-600' : 'text-slate-500'}`}
              >
                VI
              </button>
              <button 
                onClick={() => setLanguage('en')}
                className={`px-3 py-1 text-xs font-medium ${language === 'en' ? 'bg-slate-100 dark:bg-slate-800 text-blue-600' : 'text-slate-500'}`}
              >
                EN
              </button>
            </div>

            <Button size="sm" variant={user ? 'secondary' : 'primary'} onClick={handleAuth}>
              {user ? (
                <span className="flex items-center gap-2"><UserIcon size={16}/> {t('nav.logout')}</span>
              ) : t('nav.login')}
            </Button>
          </div>

          {/* Mobile Menu Toggle */}
          <button className="md:hidden p-2" onClick={() => setIsMenuOpen(!isMenuOpen)}>
            {isMenuOpen ? <X size={24} /> : <Menu size={24} />}
          </button>
        </div>

        {/* Mobile Menu */}
        {isMenuOpen && (
          <div className="md:hidden border-t border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-950 p-4 space-y-4 animate-in slide-in-from-top-2">
            <nav className="flex flex-col gap-4">
              <NavLink to="/" onClick={() => setIsMenuOpen(false)} className={navClass}>{t('nav.home')}</NavLink>
              <NavLink to="/markets" onClick={() => setIsMenuOpen(false)} className={navClass}>{t('nav.markets')}</NavLink>
              <NavLink to="/calculators" onClick={() => setIsMenuOpen(false)} className={navClass}>{t('nav.calculators')}</NavLink>
              {user && (
                <>
                  <NavLink to="/portfolio" onClick={() => setIsMenuOpen(false)} className={navClass}>{t('nav.portfolio')}</NavLink>
                  <NavLink to="/income-split" onClick={() => setIsMenuOpen(false)} className={navClass}>{t('nav.income_split')}</NavLink>
                </>
              )}
            </nav>
            <div className="flex items-center justify-between pt-4 border-t border-slate-100 dark:border-slate-800">
               <button onClick={toggleTheme} className="flex items-center gap-2 text-sm text-slate-600 dark:text-slate-400">
                  {theme === 'light' ? <Moon size={18}/> : <Sun size={18}/>} {t('common.theme')}
               </button>
               <div className="flex gap-2">
                 <button onClick={() => setLanguage('vi')} className={language === 'vi' ? 'text-blue-600 font-bold' : 'text-slate-500'}>VI</button>
                 <span className="text-slate-300">|</span>
                 <button onClick={() => setLanguage('en')} className={language === 'en' ? 'text-blue-600 font-bold' : 'text-slate-500'}>EN</button>
               </div>
            </div>
            <Button fullWidth onClick={() => { handleAuth(); setIsMenuOpen(false); }}>
              {user ? t('nav.logout') : t('nav.login')}
            </Button>
          </div>
        )}
      </header>

      <main className="flex-1 container mx-auto px-4 py-6 md:py-8">
        <Outlet />
      </main>

      <footer className="border-t border-slate-200 dark:border-slate-800 py-8 bg-white dark:bg-slate-950">
        <div className="container mx-auto px-4 text-center text-sm text-slate-500">
           <p className="mb-2">GenZF © 2024. All rights reserved.</p>
           <p className="text-xs text-slate-400">{t('common.disclaimer')}</p>
        </div>
      </footer>
    </div>
  );
};
