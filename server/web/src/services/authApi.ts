import {type ApiResponse} from "@/types/api.ts";
import {apiClient} from "@/services/config.ts";
import {type User} from "@/features/system/users/data/schema.ts";
import {type Profile} from "@/types/profile.ts";

// 登录请求类型
export interface LoginRequest {
    username: string;
    password: string;
}

// 登录响应类型
export interface LoginResponse {
    user: User;
    accessToken: string;
    refreshToken: string;
}

// 刷新令牌响应类型
export interface RefreshTokenResponse {
    accessToken: string;
    refreshToken: string;
}

// 用户登录
export async function login(credentials: LoginRequest): Promise<ApiResponse<LoginResponse>> {
    const resp = await apiClient.post("/api/v1/auth/login", credentials);
    return resp.data;
}

// 刷新访问令牌
export async function refreshToken(refreshToken: string): Promise<ApiResponse<RefreshTokenResponse>> {
    const resp = await apiClient.post("/api/v1/auth/refresh", {
        refresh_token: refreshToken
    });
    return resp.data;
}

// 登出
export async function logout(): Promise<ApiResponse<void>> {
    // 尝试获取refresh token用于更完整的登出处理
    const refreshToken = document.cookie
        .split('; ')
        .find(row => row.startsWith('refreshToken='))
        ?.split('=')[1] || localStorage.getItem('refreshToken');
    
    const requestBody = refreshToken ? { refresh_token: refreshToken } : {};
    
    const resp = await apiClient.post("/api/v1/auth/logout", requestBody);
    return resp.data;
}

// 验证当前令牌是否有效
export async function validateToken(): Promise<ApiResponse<Profile>> {
    const resp = await apiClient.get("/api/v1/profile");
    return resp.data;
}