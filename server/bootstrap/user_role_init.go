package bootstrap

import (
	"context"
	"log"
	"shadmin/ent/role"
)

// InitDefaultUserRole 初始化默认 "user" 角色（权限由 web 端管理）
func InitDefaultUserRole(app *Application) {
	ctx := context.Background()

	// 检查 "user" 角色是否已存在
	_, err := app.DB.Role.Query().
		Where(role.NameEQ("user")).
		Only(ctx)
	if err == nil {
		log.Println("user role already exists")
		return
	}

	// 创建 "user" 角色
	userRole, err := app.DB.Role.Create().
		SetName("user").
		SetStatus("active").
		SetSequence(10).
		Save(ctx)
	if err != nil {
		log.Printf("create user role failed: %v", err)
		return
	}

	log.Printf("user role created: %s", userRole.ID)
}
