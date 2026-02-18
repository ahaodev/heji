package constants

// API path constants for permission and database management

// PublicAPIPaths defines API paths that are completely public (no authentication required)
var PublicAPIPaths = []string{
	"/api/v1/auth",
	"/api/v1/health",
}

// BasicProtectedAPIPaths defines API paths that require JWT authentication but no API permission check
var BasicProtectedAPIPaths = []string{
	"/api/v1/profile",
	"/api/v1/resources",
}

// SwaggerAPIPaths defines API paths for documentation that should be excluded
var SwaggerAPIPaths = []string{
	"/swagger",
	"/docs",
	"/api-docs",
	"/openapi",
}

// GetAPIPathsToSkip returns all API paths that should be skipped from api-resources table
func GetAPIPathsToSkip() []string {
	var allSkipPaths []string
	allSkipPaths = append(allSkipPaths, PublicAPIPaths...)
	allSkipPaths = append(allSkipPaths, BasicProtectedAPIPaths...)
	allSkipPaths = append(allSkipPaths, SwaggerAPIPaths...)
	return allSkipPaths
}

// GetAPIPathsToSkipPermissionCheck returns API paths that should skip Casbin permission check
func GetAPIPathsToSkipPermissionCheck() []string {
	return BasicProtectedAPIPaths
}
