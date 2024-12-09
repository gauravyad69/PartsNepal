import { AccountType, Email, PhoneNumber, UserId, UserPreferences, AccountStatus, UserCredentials, UserEngagement } from './user.types';

export interface UserModel {
    userId: UserId;
    username: string;
    email: Email;
    firstName: string;
    lastName: string;
    phoneNumber: PhoneNumber;
    accountType: AccountType;
    createdAt: number;
    updatedAt: number;
}

export class UserModelValidator {
    static validate(user: UserModel): boolean {
        if (!user.username?.trim()) return false;
        if (!user.firstName?.trim()) return false;
        if (!user.lastName?.trim()) return false;
        if (user.email && !this.isValidEmail(user.email)) return false;
        if (!this.isValidPhone(user.phoneNumber)) return false;
        return true;
    }

    static isValidEmail(email: string): boolean {
        return /^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/.test(email);
    }

    static isValidPhone(phone: string): boolean {
        return /^\+?[1-9]\d{1,14}$/.test(phone);
    }
}


// import { UserModel } from './UserModel';

export interface ReviewRef {
    reviewId: string;
    createdAt: number;
}

export interface OrderRef {
    orderId: string;
    amount: number;
    createdAt: number;
}

export interface UserReviews {
    totalReviews: number;
    averageRating: number | null;
    reviewHistory: ReviewRef[];
}

export interface UserOrders {
    totalOrders: number;
    totalSpent: number;
    orderHistory: OrderRef[];
}

export interface FullUserDetails {
    user: UserModel;
    credentials: UserCredentials;
    preferences: UserPreferences;
    engagement: UserEngagement;
    accountStatus: AccountStatus;
    reviews: UserReviews;
    orders: UserOrders;
    lastModifiedBy: UserId | null;
    lastModifiedAt: number;
}