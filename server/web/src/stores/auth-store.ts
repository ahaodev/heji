import {create} from 'zustand'
import {getCookie, removeCookie, setCookie} from '@/lib/cookies'
import {getProfile} from '@/services/profileApi'
import {type User} from '@/types/user'
import {ACCESS_TOKEN} from "@/types/constants.ts"
import {menuService} from '@/services/menu-service'


interface AuthUser {
    accountNo: string
    email: string
    role: string[]
    exp: number
}

interface UserPermissions {
    permissions: string[][]
    roles: string[]
    menus: string[]
    is_admin: boolean
}


interface AuthState {
    auth: {
        user: AuthUser | null
        setUser: (user: AuthUser | null) => void
        profile: User | null
        setProfile: (profile: User | null) => void
        isLoadingProfile: boolean
        fetchProfile: () => Promise<void>
        accessToken: string
        setAccessToken: (accessToken: string) => void
        resetAccessToken: () => void
        permissions: UserPermissions | null
        setPermissions: (permissions: UserPermissions | null) => void
        reset: () => void
        clearSidebarCache: () => void
        hasPermission: (permission: string) => boolean
        hasRole: (role: string) => boolean
        canAccessMenu: (menuId: string) => boolean
    }
}

// Helper function to decode JWT payload
function decodeJwt(token: string): any | null {
    try {
        const parts = token.split('.')
        if (parts.length < 2) return null
        const base64Url = parts[1]
        // base64url -> base64
        const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/')
        // pad base64 string
        const pad = base64.length % 4
        const padded = base64 + (pad ? '='.repeat(4 - pad) : '')
        const json = decodeURIComponent(
            atob(padded)
                .split('')
                .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
                .join(''),
        )
        return JSON.parse(json)
    } catch (e) {
        console.error('Failed to decode JWT', e)
        return null
    }
}

export const useAuthStore = create<AuthState>()((set, get) => {
    const cookieState = getCookie(ACCESS_TOKEN)
    const initToken = cookieState || ''


    // Initialize user from lightweight JWT token (identity only)
    let initUser: AuthUser | null = null
    if (initToken) {
        const tokenPayload = decodeJwt(initToken)
        if (tokenPayload) {
            initUser = {
                accountNo: tokenPayload.name ?? tokenPayload.username ?? tokenPayload.id ?? '',
                email: tokenPayload.email ?? '',
                role: tokenPayload.role ?? ['authenticated_user'], // 基础角色标识
                exp: typeof tokenPayload.exp === 'number' ? tokenPayload.exp : Date.now() + 24 * 60 * 60,
            }
        }
    }

    return {
        auth: {
            user: initUser,
            setUser: (user) =>
                set((state) => ({...state, auth: {...state.auth, user}})),
            profile: null,
            setProfile: (profile) =>
                set((state) => ({...state, auth: {...state.auth, profile}})),
            isLoadingProfile: false,
            fetchProfile: async () => {
                const currentState = get()
                
                // Prevent duplicate calls if already loading or already have profile
                if (currentState.auth.isLoadingProfile || currentState.auth.profile) {
                    return
                }

                try {
                    set((state) => ({...state, auth: {...state.auth, isLoadingProfile: true}}))
                    const profile = await getProfile()
                    set((state) => ({
                        ...state, 
                        auth: {
                            ...state.auth, 
                            profile, 
                            isLoadingProfile: false
                        }
                    }))
                } catch (error: any) {
                    console.error('Failed to fetch profile:', error)
                    set((state) => ({...state, auth: {...state.auth, isLoadingProfile: false}}))
                    
                    // Sign out if user not found (404)
                    if (error?.response?.status === 404) {
                        console.log('User not found (404), signing out...')
                        get().auth.reset()
                    }
                }
            },
            accessToken: initToken,
            setAccessToken: (accessToken) =>
                set((state) => {
                    setCookie(ACCESS_TOKEN, accessToken)
                    localStorage.setItem(ACCESS_TOKEN, accessToken)
                    return {...state, auth: {...state.auth, accessToken}}
                }),
            resetAccessToken: () =>
                set((state) => {
                    removeCookie(ACCESS_TOKEN)
                    return {...state, auth: {...state.auth, accessToken: ''}}
                }),
            permissions: null,
            setPermissions: (permissions) =>
                set((state) => ({...state, auth: {...state.auth, permissions}})),
            reset: () =>
                set((state) => {
                    // 清理所有token相关的cookies和localStorage
                    removeCookie(ACCESS_TOKEN)
                    removeCookie('refreshToken')
                    localStorage.removeItem(ACCESS_TOKEN)
                    localStorage.removeItem('refreshToken')
                    localStorage.removeItem('userProfile')
                    localStorage.removeItem('userPermissions')
                    localStorage.removeItem('lastLoginTime')
                    
                    // 清除侧边栏缓存
                    try {
                        menuService.clearCache()
                        console.log('Menu cache cleared during auth reset')
                    } catch (error) {
                        console.warn('Failed to clear menu cache:', error)
                    }
                    
                    return {
                        ...state,
                        auth: {
                            ...state.auth,
                            user: null,
                            profile: null,
                            isLoadingProfile: false,
                            accessToken: '',
                            permissions: null
                        },
                    }
                }),
            clearSidebarCache: () => {
                // 清除侧边栏相关缓存的辅助方法
                try {
                    menuService.clearCache()
                    console.log('Menu cache cleared via clearSidebarCache()')
                } catch (error) {
                    console.warn('Failed to clear menu cache:', error)
                }
            },
            hasPermission: (permission: string) => {
                const {permissions} = get().auth
                if (!permissions) return false

                // Check if user is admin
                if (permissions.is_admin) return true

                // Check specific permissions
                return permissions.permissions.some(p =>
                    p.includes(permission) || p.includes('*')
                )
            },
            hasRole: (role: string) => {
                const {permissions} = get().auth
                if (!permissions) return false
                return permissions.roles.includes(role)
            },
            canAccessMenu: (menuId: string) => {
                const {permissions} = get().auth
                if (!permissions) return false

                // Admin can access all menus
                if (permissions.is_admin) return true

                // Check if menu is in allowed menus
                return permissions.menus.includes(menuId)
            },
        },
    }
})
