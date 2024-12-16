export interface BaseModel {
    id: string;
    lastUpdated: number;
    version: number;
}

export interface ProductModel {
    basic: BasicProductInfo;
    details: DetailedProductInfo;
}

export interface BasicProductInfo {
    productId: number;
    productSKU: string;
    productName: string;
    categoryId: number;
    inventory: InventoryInfo;
    pricing: PricingInfo;
}

export interface DetailedProductInfo {
    productId: number;
    description: string;
    // addDate: number;
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
}

export interface Money {
    amount: number;
    currency?: string;
}

export enum DiscountType {
    PERCENTAGE = 'PERCENTAGE',
    FIXED_AMOUNT = 'FIXED_AMOUNT'
}

export interface Discount {
    amount: Money;
    type: DiscountType;
    description?: string;
    startDate?: number;
    endDate?: number;
}

export enum CustomerType {
    INDIVIDUAL = 'INDIVIDUAL',
    BUSINESS = 'BUSINESS'
}

export enum PaymentStatus {
    PENDING = 'PENDING',
    INITIATED = 'INITIATED',
    COMPLETED = 'COMPLETED',
    FAILED = 'FAILED',
    REFUNDED = 'REFUNDED',
    ON_HOLD = 'ON_HOLD'
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
    items: Review[] | null;
    summary: ReviewSummary | null;
}

export interface Review extends BaseModel {
    userId: string;
    rating: number;
    comment: string;
}

export interface ReviewSummary {
    averageRating: number;
    totalCount: number;
    distribution: Record<number, number>;
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