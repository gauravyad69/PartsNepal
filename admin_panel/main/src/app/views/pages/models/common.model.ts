export interface Money {
    amount: number;
    currency?: string;
}

export interface Discount {
    amount: number;
    type: 'PERCENTAGE' | 'FIXED';
    description?: string;
}

export interface BaseModel {
    id: string;
    lastUpdated: number;
    version: number;
}
export const formatMoney = (money: Money): string => {
    return `Rs. ${money.amount.toFixed(2)}`;
};