package api

import (
	"log"
	"shadmin/api/route"
	bootstrap "shadmin/bootstrap"
	"time"
)

// SetupRoutes 设置所有路由
func SetupRoutes(app *bootstrap.Application) {
	timeout := time.Duration(app.Env.ContextTimeout) * time.Second

	// 设置路由
	if err := route.Setup(app, timeout, app.ApiEngine); err != nil {
		log.Printf("Failed to setup routes: %v", err)
	}
}

func Run(app *bootstrap.Application) error {
	// 启动服务器
	port := []string{app.Env.Port}
	err := app.ApiEngine.Run(port...)
	return err
}
