package cmd

import (
	"shadmin/api"
	bootstrap "shadmin/bootstrap"
	"shadmin/pkg"
)

// 构建时注入的版本信息
var (
	version = "dev"
	commit  = "unknown"
	date    = "unknown"
)

func Run() {
	// 记录版本信息
	pkg.Log.Infof("starting - Version: %s, Commit: %s, Built: %s", version, commit, date)

	app := bootstrap.App()
	app.Version = version
	defer app.CloseDBConnection()

	// 先设定路由
	api.SetupRoutes(app)

	// 扫描路由入库
	bootstrap.InitApiResources(app)

	// 初始化默认管理员用户（包含菜单初始化）
	bootstrap.InitDefaultAdmin(app)

	// 初始化默认用户角色（heji 客户端 API 权限）
	bootstrap.InitDefaultUserRole(app)

	// 初始化字典数据
	bootstrap.InitDictData(app)

	// 初始化完成后，执行一次全量同步并注册Hook
	bootstrap.InitCasbinHooks(app)

	err := api.Run(app)
	if err != nil {
		pkg.Log.Error(err)
	}
}
