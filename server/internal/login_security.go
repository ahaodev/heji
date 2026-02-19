package internal

import (
	"sync"
	"time"
)

// LoginAttempt 记录登录尝试信息
type LoginAttempt struct {
	FailCount   int       // 失败次数
	LastAttempt time.Time // 最后尝试时间
	LockedUntil time.Time // 锁定到什么时候
}

// LoginSecurityManager 管理登录安全
type LoginSecurityManager struct {
	attempts map[string]*LoginAttempt
	mutex    sync.RWMutex

	MaxFailures   int           // 最大失败次数
	LockDuration  time.Duration // 锁定时间
	CleanInterval time.Duration // 清理过期记录的间隔
}

// NewLoginSecurityManager 创建新的登录安全管理器
func NewLoginSecurityManager() *LoginSecurityManager {
	manager := &LoginSecurityManager{
		attempts:      make(map[string]*LoginAttempt),
		MaxFailures:   3,               // 最大失败3次
		LockDuration:  time.Minute,     // 锁定1分钟
		CleanInterval: 5 * time.Minute, // 5分钟清理一次过期记录
	}

	// 启动定期清理
	go manager.startCleanup()

	return manager
}

// IsLocked 检查用户是否被锁定
func (lsm *LoginSecurityManager) IsLocked(username string) bool {
	lsm.mutex.RLock()
	defer lsm.mutex.RUnlock()

	attempt, exists := lsm.attempts[username]
	if !exists {
		return false
	}

	// 检查是否仍在锁定期内
	return time.Now().Before(attempt.LockedUntil)
}

// GetRemainingLockTime 获取剩余锁定时间
func (lsm *LoginSecurityManager) GetRemainingLockTime(username string) time.Duration {
	lsm.mutex.RLock()
	defer lsm.mutex.RUnlock()

	attempt, exists := lsm.attempts[username]
	if !exists {
		return 0
	}

	remaining := time.Until(attempt.LockedUntil)
	if remaining < 0 {
		return 0
	}

	return remaining
}

// RecordFailedAttempt 记录失败的登录尝试
func (lsm *LoginSecurityManager) RecordFailedAttempt(username string) {
	lsm.mutex.Lock()
	defer lsm.mutex.Unlock()

	now := time.Now()

	attempt, exists := lsm.attempts[username]
	if !exists {
		attempt = &LoginAttempt{}
		lsm.attempts[username] = attempt
	}

	// 如果上次失败超过锁定时间且曾经被锁定过，重置计数
	if !attempt.LockedUntil.IsZero() && now.After(attempt.LockedUntil) {
		attempt.FailCount = 0
		attempt.LockedUntil = time.Time{} // 重置锁定时间
	}

	attempt.FailCount++
	attempt.LastAttempt = now

	// 如果失败次数达到最大值，进行锁定
	if attempt.FailCount >= lsm.MaxFailures {
		attempt.LockedUntil = now.Add(lsm.LockDuration)
	}
}

// RecordSuccessfulLogin 记录成功的登录，清除失败记录
func (lsm *LoginSecurityManager) RecordSuccessfulLogin(username string) {
	lsm.mutex.Lock()
	defer lsm.mutex.Unlock()

	delete(lsm.attempts, username)
}

// GetFailedAttempts 获取失败尝试次数
func (lsm *LoginSecurityManager) GetFailedAttempts(username string) int {
	lsm.mutex.RLock()
	defer lsm.mutex.RUnlock()

	attempt, exists := lsm.attempts[username]
	if !exists {
		return 0
	}

	// 如果已过锁定时间且曾经被锁定过，返回0
	if !attempt.LockedUntil.IsZero() && time.Now().After(attempt.LockedUntil) {
		return 0
	}

	return attempt.FailCount
}

// startCleanup 定期清理过期的记录
func (lsm *LoginSecurityManager) startCleanup() {
	ticker := time.NewTicker(lsm.CleanInterval)
	defer ticker.Stop()

	for range ticker.C {
		lsm.cleanupExpiredRecords()
	}
}

// cleanupExpiredRecords 清理过期的记录
func (lsm *LoginSecurityManager) cleanupExpiredRecords() {
	lsm.mutex.Lock()
	defer lsm.mutex.Unlock()

	now := time.Now()
	for username, attempt := range lsm.attempts {
		// 如果锁定时间已过且最后尝试时间超过清理间隔，删除记录
		if now.After(attempt.LockedUntil) && now.Sub(attempt.LastAttempt) > lsm.CleanInterval {
			delete(lsm.attempts, username)
		}
	}
}
