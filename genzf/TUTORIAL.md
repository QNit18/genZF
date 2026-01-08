# React Tutorial: Learning from GenZF Project

This tutorial covers the essential React concepts you need to understand this project. We'll focus on practical examples from the codebase.

## Table of Contents

1. [Project Structure](#1-project-structure)
2. [React Fundamentals](#2-react-fundamentals)
3. [React Router](#3-react-router)
4. [Context API](#4-context-api)
5. [React Hooks](#5-react-hooks)
6. [TypeScript Basics](#6-typescript-basics)
7. [API Integration](#7-api-integration)
8. [Component Patterns](#8-component-patterns)

---

## 1. Project Structure

### Folder Organization

```
genzf/
├── components/       # Reusable UI components
├── pages/           # Page components (routes)
├── context/         # React Context providers
├── services/        # API and business logic
├── layouts/         # Layout components
├── types.ts         # TypeScript type definitions
├── constants.ts     # Constants and translations
├── App.tsx          # Main app with routing
└── index.tsx        # Entry point
```

### Key Technologies

- **Vite**: Build tool (faster than Create React App)
- **React 19**: UI library
- **TypeScript**: Type safety
- **React Router v7**: Client-side routing
- **Recharts**: Chart library

---

## 2. React Fundamentals

### Entry Point: `index.tsx`

```1:15:genzf/index.tsx
import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App';

const rootElement = document.getElementById('root');
if (!rootElement) {
  throw new Error("Could not find root element to mount to");
}

const root = ReactDOM.createRoot(rootElement);
root.render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
);
```

**Key Concepts:**
- `createRoot()`: React 19 way to render (replaces `ReactDOM.render`)
- `StrictMode`: Helps find bugs during development
- Component tree starts with `<App />`

### Functional Components

All components in this project are **functional components** (not class components):

```10:18:genzf/components/ui/Card.tsx
export const Card: React.FC<CardProps> = ({ children, className = '', onClick }) => {
  return (
    <div 
      onClick={onClick}
      className={`bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-xl shadow-sm p-4 ${className}`}
    >
      {children}
    </div>
  );
};
```

**What to know:**
- `React.FC<Props>`: TypeScript type for functional component
- Props are destructured in function parameters
- Return JSX (looks like HTML but it's JavaScript)

---

## 3. React Router

### Router Setup: `App.tsx`

```78:86:genzf/App.tsx
const App: React.FC = () => {
  return (
    <AppProvider>
      <HashRouter>
        <AppRoutes />
      </HashRouter>
    </AppProvider>
  );
};
```

**Why HashRouter?**
- Uses `#` in URL (e.g., `/#/markets`)
- Works better for static hosting
- Alternative: `BrowserRouter` (needs server config)

### Route Configuration

```34:76:genzf/App.tsx
const AppRoutes: React.FC = () => {
  return (
    <Routes>
      {/* Main Application Layout */}
      <Route path="/" element={<Layout />}>
        <Route index element={<Home />} />
        <Route path="markets" element={<Markets />} />
        <Route path="markets/:id" element={<MarketDetail />} />
        <Route path="calculators">
            <Route index element={<Navigate to="profit" replace />} />
            <Route path="profit" element={<Calculators type="profit" />} />
            <Route path="tax" element={<Calculators type="tax" />} />
        </Route>
        <Route 
            path="portfolio" 
            element={
                <ProtectedRoute>
                    <Portfolio />
                </ProtectedRoute>
            } 
        />
        <Route 
            path="income-split" 
            element={
                <ProtectedRoute>
                    <IncomeSplit />
                </ProtectedRoute>
            } 
        />
      </Route>

      {/* Authentication Layout */}
      <Route path="/auth" element={<GuestRoute><AuthLayout /></GuestRoute>}>
        <Route path="login" element={<Login />} />
        <Route path="register" element={<Register />} />
        <Route path="forgot-password" element={<ForgotPassword />} />
        <Route index element={<Navigate to="login" replace />} />
      </Route>

      <Route path="*" element={<Navigate to="/" />} />
    </Routes>
  );
};
```

**Key Concepts:**

1. **Nested Routes**: `<Route path="/" element={<Layout />}>` - Layout wraps child routes
2. **Route Parameters**: `path="markets/:id"` - `:id` is a parameter
3. **Index Route**: `index` - shown when parent path matches exactly
4. **Navigate**: Redirects to another route
5. **Wildcard**: `path="*"` - catches all unmatched routes

### Protected Routes

```21:25:genzf/App.tsx
const ProtectedRoute: React.FC<{ children: React.ReactNode }> = ({ children }) => {
    const { user } = useApp();
    if (!user) return <Navigate to="/auth/login" replace />;
    return <>{children}</>;
};
```

**How it works:**
- Checks if user is logged in
- Redirects to login if not authenticated
- Shows children if authenticated

### Navigation

```14:14:genzf/pages/Home.tsx
  const navigate = useNavigate();
```

```23:23:genzf/pages/Home.tsx
      onClick={() => navigate(`/markets/${item.id}`)}
```

**Usage:**
- `useNavigate()` hook returns navigation function
- `navigate('/path')` - programmatic navigation
- `navigate(-1)` - go back

---

## 4. Context API

### Context Setup: `context/AppContext.tsx`

```16:16:genzf/context/AppContext.tsx
const AppContext = createContext<AppState | undefined>(undefined);
```

**What is Context?**
- Way to share data across components without prop drilling
- Alternative to Redux for simpler apps

### Provider Component

```18:79:genzf/context/AppContext.tsx
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

  const toggleTheme = () => setTheme(prev => prev === 'light' ? 'dark' : 'dark');
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
```

**What's happening:**
1. **State Management**: Manages theme, language, and user state
2. **localStorage**: Persists theme/language preferences
3. **Effects**: Syncs theme to DOM, saves language
4. **Provider**: Wraps app and provides values to all children

### Using Context

```82:86:genzf/context/AppContext.tsx
export const useApp = () => {
  const context = useContext(AppContext);
  if (!context) throw new Error("useApp must be used within AppProvider");
  return context;
};
```

**Usage in components:**

```13:13:genzf/pages/Home.tsx
  const { t, user } = useApp();
```

**Benefits:**
- No prop drilling
- Access theme, language, user, translations anywhere
- Custom hook with error handling

---

## 5. React Hooks

### useState

```22:24:genzf/pages/Home.tsx
  const [marketData, setMarketData] = useState<MarketItem[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
```

**Pattern:**
- `[value, setValue] = useState(initialValue)`
- `setValue(newValue)` updates state
- Component re-renders when state changes

### useEffect

**Data Fetching:**

```63:81:genzf/pages/Home.tsx
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
```

**What to know:**
- Runs after component mounts
- `[t]` dependency array - re-runs if `t` changes
- Empty array `[]` = run once on mount
- No array = run on every render (usually wrong!)

**Event Listeners with Cleanup:**

```83:98:genzf/pages/Home.tsx
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
```

**Cleanup function:**
- Return function from useEffect
- Runs when component unmounts or dependencies change
- Prevents memory leaks

### useRef

```15:15:genzf/pages/Home.tsx
  const scrollContainerRef = useRef<HTMLDivElement>(null);
```

```140:140:genzf/pages/Home.tsx
          ref={scrollContainerRef}
```

**Purpose:**
- Access DOM elements directly
- Doesn't trigger re-renders (unlike state)
- Used for scroll, focus, measurements

### useNavigate

```14:14:genzf/pages/Home.tsx
  const navigate = useNavigate();
```

- From React Router
- Programmatic navigation
- `navigate('/path')` or `navigate(-1)` for back

---

## 6. TypeScript Basics

### Type Definitions: `types.ts`

```2:4:genzf/types.ts
export type Language = 'en' | 'vi';
export type Theme = 'light' | 'dark';
```

**Union Types:**
- `'en' | 'vi'` - can only be 'en' OR 'vi'
- TypeScript will error if you use other values

### Interfaces

```5:14:genzf/types.ts
export interface MarketItem {
  id: string;
  symbol: string;
  name: string;
  price: number;
  change: number; // Percentage
  type: 'crypto' | 'forex' | 'commodity' | 'etf';
  lastUpdated: string;
  data: number[]; // Sparkline data
}
```

**Usage in Components:**

```10:12:genzf/components/MarketCard.tsx
interface MarketCardProps {
  item: MarketItem;
}
```

**Benefits:**
- Autocomplete in IDE
- Catches errors before runtime
- Documents what props/components expect

### Type vs Interface

- `type`: For unions, intersections, aliases
- `interface`: For object shapes (can be extended)

Both work similarly, project uses both.

---

## 7. API Integration

### Service Layer Pattern

**Generic API Function: `services/api.ts`**

```11:38:genzf/services/api.ts
export async function fetchApi<T>(endpoint: string, options?: RequestInit): Promise<T> {
  const url = `${API_BASE_URL}${endpoint}`;
  
  try {
    const response = await fetch(url, {
      ...options,
      headers: {
        'Content-Type': 'application/json',
        ...options?.headers,
      },
    });

    if (!response.ok) {
      throw new Error(`API request failed: ${response.status} ${response.statusText}`);
    }

    const data: ApiBaseResponse<T> = await response.json();
    
    if (data.code !== 1000) {
      throw new Error(data.message || 'API request failed');
    }

    return data.result;
  } catch (error) {
    console.error('API Error:', error);
    throw error;
  }
}
```

**Key Points:**
- Generic `<T>` - works with any response type
- Centralized error handling
- Consistent API structure

**Service Function: `services/assetService.ts`**

```58:66:genzf/services/assetService.ts
export async function getHomeAssets(): Promise<MarketItem[]> {
  try {
    const assets: AssetResponse[] = await fetchApi<AssetResponse[]>('/assets/home');
    return assets.map(mapAssetToMarketItem);
  } catch (error) {
    console.error('Failed to fetch home assets:', error);
    throw error;
  }
}
```

**Data Transformation:**
- Backend format → Frontend format
- Maps `AssetResponse` to `MarketItem`
- Handles errors

**Using in Component:**

```63:81:genzf/pages/Home.tsx
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
```

**Pattern:**
1. Set loading state
2. Call API
3. Handle success/error
4. Update state
5. Always set loading to false

---

## 8. Component Patterns

### Presentational Component

```14:58:genzf/components/MarketCard.tsx
export const MarketCard: React.FC<MarketCardProps> = ({ item }) => {
  const navigate = useNavigate();
  const { t } = useApp();
  const isPositive = item.change >= 0;
  // Prepare data for recharts
  const data = item.data.map((val, idx) => ({ i: idx, val }));

  return (
    <Card 
      onClick={() => navigate(`/markets/${item.id}`)}
      className="hover:border-blue-300 dark:hover:border-blue-700 transition-colors cursor-pointer group h-full flex flex-col justify-between"
    >
      <div className="flex justify-between items-start mb-2">
        <div>
          <h3 className="font-semibold text-slate-900 dark:text-slate-100">{item.name}</h3>
          <p className="text-xs text-slate-500">{item.symbol}</p>
        </div>
        <span className={`flex items-center text-sm font-medium ${isPositive ? 'text-emerald-600' : 'text-red-500'}`}>
          {isPositive ? <ArrowUpRight size={16} /> : <ArrowDownRight size={16} />}
          {Math.abs(item.change)}%
        </span>
      </div>
      
      <div className="flex items-end justify-between">
        <div className="text-xl font-bold tracking-tight">
          {item.price.toLocaleString()} <span className="text-xs font-normal text-slate-400">USD</span>
        </div>
        <div className="w-20 h-10">
          <ResponsiveContainer width="100%" height="100%">
            <LineChart data={data}>
              <Line 
                type="monotone" 
                dataKey="val" 
                stroke={isPositive ? '#10b981' : '#ef4444'} 
                strokeWidth={2} 
                dot={false} 
              />
            </LineChart>
          </ResponsiveContainer>
        </div>
      </div>
      <p className="text-[10px] text-slate-400 mt-2 text-right">{t('common.updated')} {item.lastUpdated}</p>
    </Card>
  );
};
```

**Characteristics:**
- Receives data via props
- Focuses on presentation
- Reusable
- Uses hooks for context/navigation

### Conditional Rendering

```143:167:genzf/pages/Home.tsx
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
```

**Patterns:**
- Ternary: `condition ? true : false`
- Multiple conditions: nested ternaries
- List rendering: `.map()` with `key` prop

### List Rendering

```162:166:genzf/pages/Home.tsx
            marketData.map(item => (
              <div key={item.id} className="min-w-[280px] md:min-w-[320px] snap-start">
                <MarketCard item={item} />
              </div>
            ))
```

**Important:**
- Always use `key` prop
- `key` should be unique and stable
- Helps React identify which items changed

---

## Quick Reference

### Common Patterns

**1. Fetching Data:**
```typescript
useEffect(() => {
  const fetchData = async () => {
    setIsLoading(true);
    try {
      const data = await api.getData();
      setData(data);
    } catch (err) {
      setError(err.message);
    } finally {
      setIsLoading(false);
    }
  };
  fetchData();
}, []);
```

**2. Event Handler:**
```typescript
const handleClick = () => {
  navigate('/path');
};
```

**3. Conditional Class:**
```typescript
className={`base-class ${condition ? 'active' : 'inactive'}`}
```

**4. Access Context:**
```typescript
const { theme, user, t } = useApp();
```

---

## Next Steps

1. **Explore Components**: Look at `components/` folder
2. **Check Pages**: See how pages use hooks and routing
3. **Read Services**: Understand API integration
4. **Try Modifying**: Add a new feature following patterns

## Common Pitfalls

1. **Missing dependency in useEffect** - causes stale closures
2. **Forgetting key in lists** - React warnings
3. **Not handling loading/error states** - bad UX
4. **Prop drilling** - use Context instead
5. **Not cleaning up effects** - memory leaks

---

Happy Learning! 🚀

