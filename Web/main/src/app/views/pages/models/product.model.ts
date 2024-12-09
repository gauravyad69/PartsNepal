export interface BaseModel {
    id: string;
    lastUpdated: number;
    version: number;
}

export interface ProductModel extends BaseModel {
    basic: BasicProductInfo;
    details: DetailedProductInfo;
}

export interface BasicProductInfo extends IProductInfo {
    inventory: InventoryInfo;
    pricing: PricingInfo;
}

export interface IProductInfo {
    productId: number;
    productSKU: string;
    productName: string;
    categoryId: number;
}

export interface DetailedProductInfo {
    productId: number;
    description: string;
    addDate: number;
    features: Features;
    delivery: DeliveryInfo;
    warranty: WarrantyInfo;
}

export interface InventoryInfo {
    stock: number;
    mainImage: string;
    isAvailable: boolean;
}

export interface PricingInfo {
    regularPrice: Money;
    salePrice?: Money;
    discount?: Discount;
    isOnSale: boolean;
}

export interface Money {
    amount: number;
    currency?: string;
}

export interface Discount {
    percentage?: number;
    amount?: number;
}

export interface Features {
    highlights: string[];
    images: ProductImage[];
    reviews: Reviews;
}

export interface ProductImage {
    url: string;
    alt: string;
    isPrimary: boolean;
    order: number;
}

export interface Reviews {
    items: Review[];
    summary: ReviewSummary;
    averageRating: number;
}

export interface Review extends BaseModel {
    userId: string;
    rating: number;
    comment: string;
}

export interface ReviewSummary {
    averageRating: number;
    totalCount: number;
    distribution: { [key: number]: number };
}

export interface DeliveryInfo {
    options: DeliveryOption[];
    estimatedDays: number;
    shippingCost: Money;
}

export enum DeliveryOption {
    STORE_PICKUP = 'STORE_PICKUP',
    STANDARD_DELIVERY = 'STANDARD_DELIVERY',
    EXPRESS_DELIVERY = 'EXPRESS_DELIVERY',
    INTERNATIONAL_SHIPPING = 'INTERNATIONAL_SHIPPING'
}

export interface WarrantyInfo {
    isReturnable: boolean;
    returnPeriodDays: number;
    warrantyMonths: number;
    terms: string[];
}

export interface BasicProductView extends BaseModel {
    basic: BasicProductInfo;
} 