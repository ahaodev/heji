package casbin

import (
	"shadmin/ent"
	"sync"
)

var (
	globalManager Manager
	globalOnce    sync.Once
	isInitialized bool
)

// Initialize 初始化全局Casbin管理器
// 必须在应用启动时调用一次
func Initialize(entClient *ent.Client) error {
	var err error
	globalOnce.Do(func() {
		globalManager = NewCasManager(entClient)
		isInitialized = true
	})
	return err
}

// InitializeWithLogger 使用自定义日志器初始化全局Casbin管理器
func InitializeWithLogger(entClient *ent.Client, logger Logger) error {
	var err error
	globalOnce.Do(func() {
		globalManager = NewCasManagerWithLogger(entClient, logger)
		isInitialized = true
	})
	return err
}

// GetManager 获取全局Casbin管理器实例
// 如果未初始化会panic，确保在Initialize()之后调用
func GetManager() Manager {
	if !isInitialized {
		panic("casbin manager not initialized, call casbin.Initialize() first")
	}
	return globalManager
}

// MustGetManager 安全获取全局Casbin管理器实例
// 返回manager和是否已初始化的状态
func MustGetManager() (Manager, bool) {
	return globalManager, isInitialized
}

// IsInitialized 检查是否已初始化
func IsInitialized() bool {
	return isInitialized
}

// ========== 全局便捷方法 ==========
// 以下方法直接调用全局管理器，简化使用

// CheckPermission 全局权限检查
func CheckPermission(wrappedUser, object, action string) (bool, error) {
	return GetManager().CheckPermission(wrappedUser, object, action)
}

// AddPolicy 全局添加策略
func AddPolicy(wrappedRole, object, action string) (bool, error) {
	return GetManager().AddPolicy(wrappedRole, object, action)
}

// RemovePolicy 全局移除策略
func RemovePolicy(wrappedRole, object, action string) (bool, error) {
	return GetManager().RemovePolicy(wrappedRole, object, action)
}

// AddRoleForUser 全局为用户添加角色
func AddRoleForUser(wrappedUser, wrappedRole string) (bool, error) {
	return GetManager().AddRoleForUser(wrappedUser, wrappedRole)
}

// DeleteRoleForUser 全局删除用户角色
func DeleteRoleForUser(wrappedUser, wrappedRole string) (bool, error) {
	return GetManager().DeleteRoleForUser(wrappedUser, wrappedRole)
}

// GetRolesForUser 全局获取用户角色
func GetRolesForUser(wrappedUser string) []string {
	return GetManager().GetRolesForUser(wrappedUser)
}

// SavePolicy 全局保存策略
func SavePolicy() error {
	return GetManager().SavePolicy()
}

// LoadPolicy 全局加载策略
func LoadPolicy() error {
	return GetManager().LoadPolicy()
}
