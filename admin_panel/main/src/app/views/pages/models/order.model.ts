import { 
    OrderStatus, 
    OrderSource, 
    PaymentMethod, 
    ShippingMethod,
    CustomerType,
    PaymentStatus 
} from './order.types';
import { Money, Discount, BaseModel } from './common.model';
export interface LineItem {
    id?: string;
    productId: number;
    name: string;
    quantity: number;
    unitPrice: Money;
    totalPrice?: Money;
    discount?: Discount;
    imageUrl?: string;
    lastModified?: number;
}

export interface CustomerInfo {
    id: number;
    name: string;
    type: CustomerType;
}

export interface PaymentInfo {
    method: PaymentMethod;
    status: PaymentStatus;
    transactionId?: string;
    paidAmount?: Money;
    paidDate?: number;
}

export interface RecipientInfo {
    name: string;
    phone: string;
}

export interface ShippingAddress {
    street: string;
    city: string;
    province: string;
    landmark?: string;
    district: string;
    ward: number;
    recipient: RecipientInfo;
}

export interface ShippingDetails {
    address: ShippingAddress;
    method: ShippingMethod;
    cost: Money;
}

export interface OrderSummary {
    subtotal: Money;
    discount?: Money;
    shippingCost: Money;
    tax?: Money;
    total: Money;
}

export interface TrackingEvent {
    status: OrderStatus;
    timestamp: number;
    location?: string;
    description?: string;
    updatedBy: string;
}

export interface OrderTracking {
    events: TrackingEvent[];
    currentStatus: OrderStatus;
    lastUpdated: number;
}

export interface OrderModel extends BaseModel {
    orderNumber: string;
    items: LineItem[];
    customer: CustomerInfo;
    payment: PaymentInfo;
    shippingDetails: ShippingDetails;
    summary: OrderSummary;
    status: OrderStatus;
    tracking: OrderTracking;
    notes?: string;
    source: OrderSource;
    orderDate: number;
}

export interface CreateOrderRequest {
    items: LineItem[];
    paymentMethod: PaymentMethod;
    shippingDetails: ShippingDetails;
    notes?: string;
    source: OrderSource;
}

export interface UpdateOrderStatusRequest {
    status: OrderStatus;
    updatedBy: string;
    note?: string;
}