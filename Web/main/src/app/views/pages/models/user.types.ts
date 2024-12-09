// Core value types
export type UserId = number;
export type Email = string | null;
export type PhoneNumber = string;

// Enums
export enum AccountType {
    PERSONAL = 'PERSONAL',
    BUSINESS = 'BUSINESS',
    ENTERPRISE = 'ENTERPRISE'
}

export enum AccountStatus {
    ACTIVE = 'ACTIVE',
    SUSPENDED = 'SUSPENDED',
    DEACTIVATED = 'DEACTIVATED',
    PENDING_VERIFICATION = 'PENDING_VERIFICATION'
}

// Main interfaces
export interface UserCredentials {
    hashedPassword: string | null;
    lastPasswordChange: number;
    mfaEnabled: boolean;
    mfaSecret: string | null;
}

export interface UserPreferences {
    language: string;
    timezone: string;
    marketingConsent: boolean;
    notificationSettings: NotificationSettings;
    customPreferences: Record<string, string>;
}

export interface NotificationSettings {
    emailNotifications: boolean;
    pushNotifications: boolean;
    smsNotifications: boolean;
}

export interface UserEngagement {
    totalTimeSpentMs: number;
    lastActive: number;
    engagementScore: number | null;
    loginHistory: number[];
}