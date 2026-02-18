import axios from "axios";
import {ACCESS_TOKEN} from "@/types/constants.ts";

//const REFRESH_TOKEN = 'refresh_token';

const getApiBaseURL = () => {
    return `${window.location.protocol}//${window.location.host}`;
};

export const apiClient = axios.create({
    baseURL: getApiBaseURL(),
    headers: {
        'Content-Type': 'application/json',
    },
    timeout: 300000, // 300秒
});

// 动态设置 Authorization 头部
apiClient.interceptors.request.use((config: any) => {
// 从localStorage获取token
    const token = localStorage.getItem(ACCESS_TOKEN);
    if (token) {
        config.headers = config.headers || {};
        config.headers['Authorization'] = `Bearer ${token}`;
    } else {
        console.log('API Request: No token found, request will be unauthenticated');
    }
    return config;
}, (error: any) => {
    console.error('Request interceptor error:', error);
    return Promise.reject(error);
});

// Export as api for consistency with other service files
export const api = apiClient;
