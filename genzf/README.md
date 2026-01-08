# GenZF - Financial Market Dashboard

A modern React + TypeScript application for tracking financial markets, managing portfolios, and financial calculations.

## Run Locally

**Prerequisites:** Node.js

1. Install dependencies:
   ```bash
   npm install
   ```
2. Set the `GEMINI_API_KEY` in [.env.local](.env.local) to your Gemini API key (if needed)
3. Run the app:
   ```bash
   npm run dev
   ```
4. Open [http://localhost:3000](http://localhost:3000) in your browser

## Learning React with This Project

**New to React?** Check out the [TUTORIAL.md](./TUTORIAL.md) guide that explains React concepts using this codebase as examples.

### What You'll Learn

- ✅ React Router for navigation
- ✅ Context API for state management
- ✅ React Hooks (useState, useEffect, useRef)
- ✅ TypeScript integration
- ✅ API integration patterns
- ✅ Component composition
- ✅ Modern React patterns

### Learning Path

1. **Start Here**: Read [TUTORIAL.md](./TUTORIAL.md) - covers essential concepts
2. **Explore Code**: Look at `pages/Home.tsx` and `components/` folder
3. **Understand Routing**: Study `App.tsx` for route configuration
4. **State Management**: Check `context/AppContext.tsx`
5. **API Integration**: Review `services/` folder

## Project Structure

```
genzf/
├── components/       # Reusable UI components
├── pages/           # Page components (routes)
├── context/         # React Context providers
├── services/        # API and business logic
├── layouts/         # Layout components
├── types.ts         # TypeScript definitions
├── constants.ts     # Constants and translations
├── App.tsx          # Main app with routing
└── index.tsx       # Entry point
```

## Tech Stack

- **React 19** - UI library
- **TypeScript** - Type safety
- **Vite** - Build tool
- **React Router v7** - Routing
- **Recharts** - Charts
- **Lucide React** - Icons

## Features

- 📊 Real-time market data
- 💼 Portfolio tracking
- 🧮 Financial calculators
- 🌓 Dark mode
- 🌐 Multi-language (EN/VI)
- 📱 Responsive design

---

View your app in AI Studio: https://ai.studio/apps/drive/1dwOIQLCx-ztlnLZqoxROfDjnFmZ9-HpP
