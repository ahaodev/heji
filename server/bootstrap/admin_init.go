package bootstrap

import (
	"context"
	"log"
	"shadmin/ent/user"

	"entgo.io/ent/dialect/sql"
	"golang.org/x/crypto/bcrypt"
)

// InitDefaultAdmin initializes default admin user and permissions
func InitDefaultAdmin(app *Application) {
	ctx := context.Background()

	// Check if admin user exists
	adminExists, err := app.DB.User.Query().
		Where(user.UsernameEQ(app.Env.AdminUsername)).
		Exist(ctx)
	if err != nil {
		log.Printf("check admin user failed: %v", err)
		return
	}

	if adminExists {
		log.Println("admin user already exists")
		// Ensure admin role has menus bound
		checkAndBindMenusToExistingAdmin(app, ctx)
		return
	}

	adminRole, err := app.DB.Role.Create().
		SetName("admin").
		SetStatus("active").
		Save(ctx)
	if err != nil {
		log.Printf("create admin role failed: %v", err)
		return
	}
	// Create admin user
	hashedPassword, err := bcrypt.GenerateFromPassword([]byte(app.Env.AdminPassword), bcrypt.DefaultCost)
	if err != nil {
		log.Printf("hash password failed: %v", err)
		return
	}
	adminUser, err := app.DB.User.Create().
		SetUsername(app.Env.AdminUsername).
		SetEmail(app.Env.AdminEmail).
		SetPassword(string(hashedPassword)).
		SetStatus(user.StatusActive).
		SetIsAdmin(true).
		AddRoles(adminRole).
		Save(ctx)
	if err != nil {
		log.Printf("create admin user failed: %v", err)
		return
	}

	// Grant super policy
	_, err = app.CasManager.AddPolicy(adminRole.ID, "*", "*")
	if err != nil {
		log.Printf("add policy failed: %v", err)
	}

	// Bind role to user
	_, err = app.CasManager.AddRoleForUser(adminUser.ID, adminRole.ID)
	if err != nil {
		log.Printf("assign role failed: %v", err)
	}

	// Save policy
	if err := app.CasManager.SavePolicy(); err != nil {
		log.Printf("save policy failed: %v", err)
	}

	// Init menus and bind to admin
	initMenu(app, adminRole.ID)

	log.Printf("admin user created: %s", app.Env.AdminUsername)
}

// bindAllMenusToAdminRole binds all menus to the admin role if not already bound
func bindAllMenusToAdminRole(app *Application, adminRoleID string, ctx context.Context) {
	// Check existing bindings
	existingBindings, err := app.DB.Role.Query().
		Where(func(s *sql.Selector) {
			s.Where(sql.EQ("id", adminRoleID))
		}).
		QueryMenus().
		Count(ctx)
	if err != nil {
		log.Printf("check role-menu binding failed: %v", err)
		return
	}

	if existingBindings > 0 {
		log.Printf("admin role already bound menus (%d)", existingBindings)
		return
	}

	// Fetch all menus
	allMenus, err := app.DB.Menu.Query().All(ctx)
	if err != nil {
		log.Printf("fetch all menus failed: %v", err)
		return
	}

	if len(allMenus) == 0 {
		log.Println("no menu found, skip bind")
		return
	}

	// Bind all menus to admin role
	adminRole, err := app.DB.Role.Get(ctx, adminRoleID)
	if err != nil {
		log.Printf("get admin role failed: %v", err)
		return
	}

	_, err = adminRole.Update().AddMenus(allMenus...).Save(ctx)
	if err != nil {
		log.Printf("bind menus to admin role failed: %v", err)
		return
	}

	log.Printf("successfully bound %d menus to admin role", len(allMenus))
}

// checkAndBindMenusToExistingAdmin ensures admin role has menus bound
func checkAndBindMenusToExistingAdmin(app *Application, ctx context.Context) {
	// Get admin role
	adminRole, err := app.DB.Role.Query().
		Where(func(s *sql.Selector) {
			s.Where(sql.EQ("name", "admin"))
		}).
		First(ctx)
	if err != nil {
		log.Printf("get admin role failed: %v", err)
		return
	}

	bindAllMenusToAdminRole(app, adminRole.ID, ctx)
}

func initMenu(app *Application, roleID string) {
	ctx := context.Background()
	// Check if menus already exist
	existingMenus, err := app.DB.Menu.Query().Count(ctx)
	if err != nil {
		log.Printf("check menu data failed: %v", err)
		return
	}
	if existingMenus > 0 {
		log.Println("menu data already exists, skip init")
		return
	}
	// Level 1 menus
	_, _ = app.DB.Menu.Create().SetName("仪表盘").SetSequence(0).SetType("menu").SetPath("/").SetIcon("Home").SetIsFrame(false).SetVisible("show").SetStatus("active").Save(ctx)
	system, _ := app.DB.Menu.Create().SetName("系统管理").SetSequence(0).SetType("menu").SetPath("/").SetIcon("Settings").SetIsFrame(false).SetVisible("show").SetStatus("active").Save(ctx)
	// Level 2 menus
	menuMgmt, _ := app.DB.Menu.Create().SetParentID(system.ID).SetName("菜单管理").SetSequence(1).SetType("menu").SetPath("/system/menu").SetIcon("Menu").SetIsFrame(false).SetVisible("show").SetStatus("active").Save(ctx)
	roleMgmt, _ := app.DB.Menu.Create().SetParentID(system.ID).SetName("角色管理").SetSequence(2).SetType("menu").SetPath("/system/role").SetIcon("UserCheck").SetIsFrame(false).SetVisible("show").SetStatus("active").Save(ctx)
	userMgmt, _ := app.DB.Menu.Create().SetParentID(system.ID).SetName("用户管理").SetSequence(3).SetType("menu").SetPath("/system/user").SetIcon("Users").SetIsFrame(false).SetVisible("show").SetStatus("active").Save(ctx)
	apiMapping, _ := app.DB.Menu.Create().SetParentID(system.ID).SetName("API资源").SetSequence(4).SetType("menu").SetPath("/system/api-resources").SetIcon("Code2").SetIsFrame(false).SetVisible("show").SetStatus("active").Save(ctx)
	loginLogs, _ := app.DB.Menu.Create().SetParentID(system.ID).SetName("登录日志").SetSequence(5).SetType("menu").SetPath("/system/login-logs").SetIcon("Layers3").SetIsFrame(false).SetVisible("show").SetStatus("active").Save(ctx)
	dictMgmt, _ := app.DB.Menu.Create().SetParentID(system.ID).SetName("字典管理").SetSequence(6).SetType("menu").SetPath("/system/dict").SetIcon("BookMarked").SetIsFrame(false).SetVisible("show").SetStatus("active").Save(ctx)
	// Menu management buttons
	_, _ = app.DB.Menu.Create().SetParentID(menuMgmt.ID).SetName("创建菜单").SetSequence(0).SetType("button").SetPath("").SetIcon("PlusCircle").SetIsFrame(false).SetVisible("show").SetPermissions("system:menu:add").AddAPIResourceIDs("POST:/api/v1/system/menu").SetStatus("active").Save(ctx)
	_, _ = app.DB.Menu.Create().SetParentID(menuMgmt.ID).SetName("编辑菜单").SetSequence(0).SetType("button").SetPath("").SetIcon("Edit").SetIsFrame(false).SetVisible("show").SetPermissions("system:menu:edit").AddAPIResourceIDs("PUT:/api/v1/system/menu/:id").SetStatus("active").Save(ctx)
	_, _ = app.DB.Menu.Create().SetParentID(menuMgmt.ID).SetName("删除菜单").SetSequence(0).SetType("button").SetPath("").SetIcon("Trash2").SetIsFrame(false).SetVisible("show").SetPermissions("system:menu:delete").AddAPIResourceIDs("DELETE:/api/v1/system/menu/:id").SetStatus("active").Save(ctx)
	// Role management buttons
	_, _ = app.DB.Menu.Create().SetParentID(roleMgmt.ID).SetName("创建角色").SetSequence(0).SetType("button").SetPath("").SetIcon("Plus").SetIsFrame(false).SetVisible("show").SetPermissions("system:role:add").AddAPIResourceIDs("POST:/api/v1/system/role").SetStatus("active").Save(ctx)
	_, _ = app.DB.Menu.Create().SetParentID(roleMgmt.ID).SetName("删除角色").SetSequence(0).SetType("button").SetPath("").SetIcon("Trash2").SetIsFrame(false).SetVisible("show").SetPermissions("system:role:delete").AddAPIResourceIDs("DELETE:/api/v1/system/role/:id").SetStatus("active").Save(ctx)
	_, _ = app.DB.Menu.Create().SetParentID(roleMgmt.ID).SetName("编辑角色").SetSequence(0).SetType("button").SetPath("").SetIcon("Edit").SetIsFrame(false).SetVisible("show").SetPermissions("system:role:edit").AddAPIResourceIDs("PUT:/api/v1/system/role/:id").SetStatus("active").Save(ctx)
	// User management buttons
	_, _ = app.DB.Menu.Create().SetParentID(userMgmt.ID).SetName("创建用户").SetSequence(0).SetType("button").SetPath("").SetIcon("PlusCircle").SetIsFrame(false).SetVisible("show").SetPermissions("system:user:add").AddAPIResourceIDs("POST:/api/v1/system/user/").SetStatus("active").Save(ctx)
	_, _ = app.DB.Menu.Create().SetParentID(userMgmt.ID).SetName("邀请用户").SetSequence(0).SetType("button").SetPath("").SetIcon("Phone").SetIsFrame(false).SetVisible("show").SetPermissions("system:user:invite").AddAPIResourceIDs("POST:/api/v1/system/user/invite").SetStatus("active").Save(ctx)
	_, _ = app.DB.Menu.Create().SetParentID(userMgmt.ID).SetName("删除用户").SetSequence(0).SetType("button").SetPath("").SetIcon("Trash").SetIsFrame(false).SetVisible("show").SetPermissions("system:user:delete").AddAPIResourceIDs("DELETE:/api/v1/system/user/:id").SetStatus("active").Save(ctx)
	_, _ = app.DB.Menu.Create().SetParentID(userMgmt.ID).SetName("编辑用户").SetSequence(0).SetType("button").SetPath("").SetIcon("Edit").SetIsFrame(false).SetVisible("show").SetPermissions("system:user:edit").AddAPIResourceIDs("PUT:/api/v1/system/user/:id").SetStatus("active").Save(ctx)
	// Other buttons
	_, _ = app.DB.Menu.Create().SetParentID(apiMapping.ID).SetName("API扫描").SetSequence(0).SetType("button").SetPath("").SetIcon("ScanLine").SetIsFrame(false).SetVisible("show").SetPermissions("system:api:scan").AddAPIResourceIDs("POST:/api/v1/system/api-resources/scan").SetStatus("active").Save(ctx)
	_, _ = app.DB.Menu.Create().SetParentID(loginLogs.ID).SetName("清空日志").SetSequence(0).SetType("button").SetPath("").SetIcon("Trash2").SetIsFrame(false).SetVisible("show").SetPermissions("system:login_logs:clean").AddAPIResourceIDs("DELETE:/api/v1/system/login-logs").SetStatus("active").Save(ctx)
	// Dictionary buttons (types & items)
	_, _ = app.DB.Menu.Create().SetParentID(dictMgmt.ID).SetName("创建字典类型").SetSequence(0).SetType("button").SetPath("").SetIcon("PlusCircle").SetIsFrame(false).SetVisible("show").SetPermissions("system:dict:add_type").AddAPIResourceIDs("POST:/api/v1/system/dict/types").SetStatus("active").Save(ctx)
	_, _ = app.DB.Menu.Create().SetParentID(dictMgmt.ID).SetName("编辑字典类型").SetSequence(0).SetType("button").SetPath("").SetIcon("Edit").SetIsFrame(false).SetVisible("show").SetPermissions("system:dict:edit_type").AddAPIResourceIDs("PUT:/api/v1/system/dict/types/:id").SetStatus("active").Save(ctx)
	_, _ = app.DB.Menu.Create().SetParentID(dictMgmt.ID).SetName("删除字典类型").SetSequence(0).SetType("button").SetPath("").SetIcon("Trash2").SetIsFrame(false).SetVisible("show").SetPermissions("system:dict:delete_type").AddAPIResourceIDs("DELETE:/api/v1/system/dict/types/:id").SetStatus("active").Save(ctx)
	_, _ = app.DB.Menu.Create().SetParentID(dictMgmt.ID).SetName("创建字典项").SetSequence(0).SetType("button").SetPath("").SetIcon("Plus").SetIsFrame(false).SetVisible("show").SetPermissions("system:dict:add_item").AddAPIResourceIDs("POST:/api/v1/system/dict/items").SetStatus("active").Save(ctx)
	_, _ = app.DB.Menu.Create().SetParentID(dictMgmt.ID).SetName("编辑字典项").SetSequence(0).SetType("button").SetPath("").SetIcon("Edit").SetIsFrame(false).SetVisible("show").SetPermissions("system:dict:edit_item").AddAPIResourceIDs("PUT:/api/v1/system/dict/items/:id").SetStatus("active").Save(ctx)
	_, _ = app.DB.Menu.Create().SetParentID(dictMgmt.ID).SetName("删除字典项").SetSequence(0).SetType("button").SetPath("").SetIcon("Trash").SetIsFrame(false).SetVisible("show").SetPermissions("system:dict:delete_item").AddAPIResourceIDs("DELETE:/api/v1/system/dict/items/:id").SetStatus("active").Save(ctx)

	log.Println("menu data initialized")

	// Bind all menus to the role if provided
	if roleID != "" {
		bindAllMenusToAdminRole(app, roleID, ctx)
	}
}
