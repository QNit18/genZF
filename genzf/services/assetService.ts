import { fetchApi } from './api';
import { AssetResponse } from '../types';
import { MarketItem } from '../types';

/**
 * Maps backend AssetResponse to frontend MarketItem format
 */
function mapAssetToMarketItem(asset: AssetResponse): MarketItem {
  // Map category enum to frontend type
  const categoryMap: Record<string, 'crypto' | 'forex' | 'commodity' | 'etf'> = {
    'CRYPTO': 'crypto',
    'FOREX': 'forex',
    'COMMODITY': 'commodity',
  };

  // Generate sparkline data based on price history
  // Using open, low, high, and currentPrice to create a realistic trend
  // Create 5 data points showing price movement from open to current
  const priceRange = asset.high - asset.low;
  const midPoint = (asset.open + asset.currentPrice) / 2;
  const sparklineData = [
    asset.open,
    asset.low + (priceRange * 0.2), // Early low
    midPoint, // Mid point
    asset.high - (priceRange * 0.1), // Near high
    asset.currentPrice, // Current price
  ];

  // Format lastUpdated timestamp to readable format
  const formatTime = (isoString: string): string => {
    try {
      const date = new Date(isoString);
      return date.toLocaleTimeString('en-US', { 
        hour: '2-digit', 
        minute: '2-digit',
        hour12: true 
      });
    } catch {
      return 'N/A';
    }
  };

  return {
    id: asset.id,
    symbol: asset.symbol,
    name: asset.assetName,
    price: asset.currentPrice,
    change: asset.changePercentage,
    type: categoryMap[asset.category] || 'commodity',
    lastUpdated: formatTime(asset.lastUpdated),
    data: sparklineData,
  };
}

/**
 * Fetches home assets from the backend API
 */
export async function getHomeAssets(): Promise<MarketItem[]> {
  try {
    const assets: AssetResponse[] = await fetchApi<AssetResponse[]>('/assets/home');
    return assets.map(mapAssetToMarketItem);
  } catch (error) {
    console.error('Failed to fetch home assets:', error);
    throw error;
  }
}
