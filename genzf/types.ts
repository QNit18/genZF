
export type Language = 'en' | 'vi';
export type Theme = 'light' | 'dark';

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

export interface PortfolioItem {
  id: number;
  symbol: string;
  quantity: number;
  avgCost: number;
  currentPrice: number;
  pl: number;
}

export interface User {
  id: string;
  name: string;
  email: string;
  role: 'guest' | 'user';
}

export interface IncomeSplit {
  needs: number;
  wants: number;
  invest: number; // Savings/Invest
}

export type TranslationKeys = 
  // Nav & Common
  | 'nav.home' | 'nav.markets' | 'nav.calculators' | 'nav.portfolio' | 'nav.income_split' 
  | 'nav.login' | 'nav.logout' | 'nav.register'
  | 'common.loading' | 'common.error' | 'common.save' | 'common.submit' | 'common.theme' | 'common.cancel'
  | 'common.disclaimer' | 'common.updated' | 'common.login_required' | 'common.back'
  | 'common.search_placeholder' | 'common.clear_filters' | 'common.no_data'

  // Home
  | 'home.snapshot' | 'home.quick_actions' | 'home.intro'
  | 'home.feat.profit' | 'home.feat.profit_desc'
  | 'home.feat.tax' | 'home.feat.tax_desc'
  | 'home.feat.markets' | 'home.feat.markets_desc'
  | 'home.feat.portfolio' | 'home.feat.portfolio_guest' | 'home.feat.portfolio_user'

  // Markets
  | 'market.subtitle' | 'market.filter_all' | 'market.filter_crypto' | 'market.filter_forex'
  | 'market.filter_commodity' | 'market.filter_etf' 
  | 'market.sort_change_desc' | 'market.sort_change_asc' | 'market.sort_price_desc' 
  | 'market.sort_price_asc' | 'market.sort_name' 
  | 'market.status_title' | 'market.status_desc' | 'market.open' | 'market.high' | 'market.low' | 'market.vol'
  | 'market.tab_chart' | 'market.tab_overview' | 'market.tab_calc' | 'market.not_found'

  // Calculators
  | 'calc.profit.title' | 'calc.profit.buy_price' | 'calc.profit.sell_price' | 'calc.profit.qty' | 'calc.profit.result' | 'calc.profit.breakeven'
  | 'calc.tax.title' | 'calc.tax.income' | 'calc.tax.calculate' | 'calc.tax.est_tax' | 'calc.tax.net_income' | 'calc.tax.disclaimer'

  // Portfolio
  | 'port.title' | 'port.welcome' | 'port.add_trans' | 'port.allocation' | 'port.net_worth' | 'port.all_time'
  | 'port.table.asset' | 'port.table.qty' | 'port.table.avg' | 'port.table.cur' | 'port.table.pl' | 'port.table.action'
  | 'port.modal.title' | 'port.modal.symbol' | 'port.modal.qty' | 'port.modal.price' | 'port.modal.add'
  | 'port.close' | 'port.remove_confirm'
  | 'port.verified_asset' | 'port.spot_market' | 'port.valuation_realtime' | 'port.growth_perf' | 'port.pl_perf'
  | 'port.profit_status' | 'port.loss_status' | 'port.strategy_title' | 'port.strategy_desc' | 'port.explore_markets'
  | 'port.active_positions' | 'port.total_assets' | 'port.leader' | 'port.strategy_insight' | 'port.live_tracking' | 'port.assets_label'

  // Income Split
  | 'split.title' | 'split.subtitle' | 'split.income' | 'split.unallocated'
  | 'split.needs' | 'split.needs_desc'
  | 'split.wants' | 'split.wants_desc'
  | 'split.save' | 'split.save_desc'
  | 'split.reset' | 'split.save_plan' | 'split.success'
  | 'split.visualizer' | 'split.fully_allocated' | 'split.pro_tip' | 'split.pro_tip_desc'

  // Auth
  | 'auth.email' | 'auth.password' | 'auth.forgot_password' | 'auth.remember_me' 
  | 'auth.login_title' | 'auth.login_subtitle' | 'auth.register_title' | 'auth.register_subtitle' 
  | 'auth.name' | 'auth.confirm_password' | 'auth.have_account' | 'auth.no_account' | 'auth.reset_password' | 'auth.back_to_login' | 'auth.sent_title' | 'auth.sent_desc';
