import { type UserStatus } from './user'

export interface Profile {
    id: string;
    username: string;
    email: string;
    phone?: string;
    avatar?: string;
    status: UserStatus;
    created_at: Date;
    updated_at: Date;
}

export interface ProfileUpdateRequest {
    name?: string;
    username?: string;
    email?: string;
    phone?: string;
    avatar?: string;
}

export interface PasswordUpdateRequest {
    current_password: string;
    new_password: string;
}