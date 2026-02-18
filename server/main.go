package main

import (
	"shadmin/cmd"
	_ "shadmin/docs" // Import generated docs
)

// @title           ShadMIN API
// @version         1.0
// @description     admin dashboard system with RBAC
// @termsOfService  http://swagger.io/terms/

// @contact.name   API Support
// @contact.url    http://www.swagger.io/support
// @contact.email  support@swagger.io

// @license.name  MIT
// @license.url   https://opensource.org/licenses/MIT

// @host      localhost:55667
// @BasePath  /api/v1

// @securityDefinitions.apikey BearerAuth
// @in header
// @name Authorization
// @description Type "Bearer" followed by a space and JWT token.
//
//go:generate swag init
//go:generate go run -mod=mod entgo.io/ent/cmd/ent generate ./ent/schema
func main() {
	cmd.Run()
}
