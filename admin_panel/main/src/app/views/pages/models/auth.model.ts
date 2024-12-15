// Define the AccountType enum
export enum AccountType {
    PERSONAL = 'PERSONAL',
    BUSINESS = 'BUSINESS' // Add other account types as needed
}

// Define the RegisterRequest interface
export interface RegisterRequest {
    email?: string; // Optional property
    phoneNumber: string;
    password: string;
    firstName: string;
    lastName: string;
    username: string;
    accountType?: AccountType; // Optional property with a default value
}


// Define the LoginRequest interface
export interface LoginRequest {
    identifier: string; // email or phone
    password: string;
    isPhoneLogin: boolean; // true for phone login, false for email login
}

// Define the AuthResponse interface
export interface AuthResponse {
    token: string;
    user: number; // Assuming user is an integer ID
    expiresIn?: number; // Optional property with a default value of 3600 seconds
}