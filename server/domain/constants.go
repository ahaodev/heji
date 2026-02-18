package domain

// 用户状态定义
const (
	UserStatusActive    = "active"    // 活跃用户
	UserStatusInactive  = "inactive"  // 非活跃用户
	UserStatusInvited   = "invited"   // 已邀请但未激活
	UserStatusSuspended = "suspended" // 已暂停
)

// 所有可用用户状态列表
var AllUserStatuses = []string{
	UserStatusActive,
	UserStatusInactive,
	UserStatusInvited,
	UserStatusSuspended,
}

// 固定菜单定义
type MenuItem struct {
	ID            string `json:"id"`
	Name          string `json:"name"`
	Path          string `json:"path"`
	Icon          string `json:"icon"`
	Component     string `json:"component"`
	Sort          int    `json:"sort"`
	Visible       bool   `json:"visible"`
	RequiresAuth  bool   `json:"requiresAuth"`
	RequiresAdmin bool   `json:"requiresAdmin"`
}
