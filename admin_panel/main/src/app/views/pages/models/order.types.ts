export enum OrderStatus {
    PENDING_PAYMENT = 'PENDING_PAYMENT',
    PAYMENT_CONFIRMED = 'PAYMENT_CONFIRMED',
    PROCESSING = 'PROCESSING',
    SHIPPED = 'SHIPPED',
    DELIVERED = 'DELIVERED',
    CANCELLED = 'CANCELLED'
}

export enum OrderSource {
    MOBILE_APP = 'MOBILE_APP',
    WEB = 'WEB',
    POS = 'POS'
}

export enum PaymentMethod {
    CASH_ON_DELIVERY = 'CASH_ON_DELIVERY',
    KHALTI = 'KHALTI',
    ESEWA = 'ESEWA'
}

export enum ShippingMethod {
    STANDARD = 'STANDARD',
    EXPRESS = 'EXPRESS'
}

export enum CustomerType {
    REGULAR = 'REGULAR',
    BUSINESS = 'BUSINESS'
}

export enum PaymentStatus {
    PENDING = 'PENDING',
    COMPLETED = 'COMPLETED',
    FAILED = 'FAILED'
}