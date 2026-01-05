import React, { createContext, useContext, useEffect, useState } from 'react';
import { Language, Theme, User, TranslationKeys } from '../types';
import { TRANSLATIONS } from '../constants';

interface AppState {
  theme: Theme;
  toggleTheme: () => void;
  language: Language;
  setLanguage: (lang: Language) => void;
  user: User | null;
  login: (email: string, password: string) => Promise<boolean>;
  logout: () => void;
  t: (key: TranslationKeys) => string;
}

const AppContext = createContext<AppState | undefined>(undefined);

export const AppProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  // Theme State
  const [theme, setTheme] = useState<Theme>(() => {
    const saved = localStorage.getItem('genzf-theme');
    return (saved as Theme) || (window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light');
  });

  // Language State
  const [language, setLanguageState] = useState<Language>(() => {
    const saved = localStorage.getItem('genzf-lang');
    return (saved as Language) || 'vi';
  });

  // Auth State
  const [user, setUser] = useState<User | null>(null);

  // Effects
  useEffect(() => {
    localStorage.setItem('genzf-theme', theme);
    const root = window.document.documentElement;
    root.classList.remove('light', 'dark');
    root.classList.add(theme);
  }, [theme]);

  useEffect(() => {
    localStorage.setItem('genzf-lang', language);
  }, [language]);

  const toggleTheme = () => setTheme(prev => prev === 'light' ? 'dark' : 'light');
  const setLanguage = (lang: Language) => setLanguageState(lang);

  const login = async (email: string, password: string): Promise<boolean> => {
    // Mock network delay
    await new Promise(resolve => setTimeout(resolve, 800));

    // Hardcoded credentials for "fake" account
    if (email === 'admin' && password === 'admin') {
      setUser({ 
        id: 'u1', 
        name: 'Admin User', 
        email: 'admin@genzf.com', 
        role: 'user' 
      });
      return true;
    }

    return false;
  };

  const logout = () => {
    setUser(null);
  };

  const t = (key: TranslationKeys): string => {
    return TRANSLATIONS[language][key] || key;
  };

  return (
    <AppContext.Provider value={{ theme, toggleTheme, language, setLanguage, user, login, logout, t }}>
      {children}
    </AppContext.Provider>
  );
};

export const useApp = () => {
  const context = useContext(AppContext);
  if (!context) throw new Error("useApp must be used within AppProvider");
  return context;
};