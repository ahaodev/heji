package bootstrap

import (
	"context"
	"fmt"
	"log"
	"shadmin/domain"
	"shadmin/ent"
	"shadmin/ent/apiresource"
	"shadmin/ent/menu"
	"shadmin/internal/constants"
	"strings"
	"time"

	"github.com/gin-gonic/gin"
)

// InitApiResources 初始化API资源数据 - 删除重建，保持ID一致维持菜单关联
func InitApiResources(app *Application) {
	log.Println("🔍 Scanning and rebuilding API resources...")

	ctx := context.Background()

	// 先获取现有菜单-API资源关联关系
	menuApiResourceAssociations, err := getMenuApiResourceAssociations(ctx, app.DB)
	if err != nil {
		log.Printf("❌ Failed to get menu-API resource associations: %v", err)
		return
	}
	log.Printf("💾 Saved %d menu-API resource associations", len(menuApiResourceAssociations))

	// 先清空所有API资源
	deleted, err := app.DB.ApiResource.Delete().Exec(ctx)
	if err != nil {
		log.Printf("❌ Failed to clear existing API resources: %v", err)
		return
	}
	log.Printf("🗑️ Cleared %d existing API resources", deleted)

	// 扫描路由获取API资源
	discoveredResources := scanGinRoutes(app.ApiEngine)
	totalScanned := len(discoveredResources)

	// 重新创建所有资源（ID保持一致）
	created, err := bulkCreateApiResources(ctx, app.DB, discoveredResources)
	if err != nil {
		log.Printf("❌ Failed to create API resources: %v", err)
		return
	}

	// 恢复菜单-API资源关联关系
	restored, err := restoreMenuApiResourceAssociations(ctx, app.DB, menuApiResourceAssociations)
	if err != nil {
		log.Printf("❌ Failed to restore menu-API resource associations: %v", err)
	} else {
		log.Printf("🔗 Restored %d menu-API resource associations", restored)
	}

	log.Printf("✅ API resources rebuilt successfully:")
	log.Printf("   - Total scanned: %d", totalScanned)
	log.Printf("   - Created: %d", created)
}

// scanGinRoutes 扫描Gin路由获取API资源
func scanGinRoutes(ginEngine *gin.Engine) []*domain.ApiResource {
	var resources []*domain.ApiResource

	// 获取所有路由
	routes := ginEngine.Routes()

	for _, route := range routes {
		// 跳过不需要存储的路由
		if shouldSkipRoute(route.Path) {
			continue
		}

		// 从路径提取模块名
		module := extractModuleFromPath(route.Path)

		// 提取处理器名称
		handlerName := route.Handler

		// 判断是否为公开端点
		isPublic := isPublicEndpoint(route.Path)

		resource := &domain.ApiResource{
			Method:   route.Method,
			Path:     route.Path,
			Handler:  handlerName,
			Module:   module,
			IsPublic: isPublic,
		}

		resources = append(resources, resource)
	}

	return resources
}

// bulkCreateApiResources 批量创建API资源
func bulkCreateApiResources(ctx context.Context, client *ent.Client, apiResources []*domain.ApiResource) (int, error) {
	if len(apiResources) == 0 {
		return 0, nil
	}

	tx, err := client.Tx(ctx)
	if err != nil {
		return 0, fmt.Errorf("failed to start transaction: %w", err)
	}
	defer tx.Rollback()

	now := time.Now()
	createdCount := 0

	for _, apiResource := range apiResources {
		// 验证必要字段
		if apiResource.Method == "" {
			log.Printf("WARN: Skipping API resource with empty method, path: %s", apiResource.Path)
			continue
		}
		if apiResource.Path == "" {
			log.Printf("WARN: Skipping API resource with empty path, method: %s", apiResource.Method)
			continue
		}

		// 生成稳定的ID（method:path组合）
		apiResource.ID = domain.GenerateApiResourceID(apiResource.Method, apiResource.Path)
		apiResource.CreatedAt = now
		apiResource.UpdatedAt = now

		_, err = tx.ApiResource.Create().
			SetID(apiResource.ID).
			SetMethod(apiResource.Method).
			SetPath(apiResource.Path).
			SetHandler(apiResource.Handler).
			SetModule(apiResource.Module).
			SetIsPublic(apiResource.IsPublic).
			SetCreatedAt(apiResource.CreatedAt).
			SetUpdatedAt(apiResource.UpdatedAt).
			Save(ctx)
		if err != nil {
			return 0, fmt.Errorf("failed to create API resource %s (method: %s, path: %s): %w", apiResource.ID, apiResource.Method, apiResource.Path, err)
		}
		createdCount++
	}

	if err := tx.Commit(); err != nil {
		return 0, fmt.Errorf("failed to commit transaction: %w", err)
	}

	return createdCount, nil
}

// shouldSkipRoute 判断是否应该跳过该路由
func shouldSkipRoute(path string) bool {
	allSkipPaths := constants.GetAPIPathsToSkip()

	for _, skipPath := range allSkipPaths {
		if strings.HasPrefix(path, skipPath) {
			return true
		}
	}

	return false
}

// extractModuleFromPath 从API路径中提取模块名
func extractModuleFromPath(path string) string {
	// 移除前导斜杠并按斜杠分割
	parts := strings.Split(strings.Trim(path, "/"), "/")

	if len(parts) >= 3 && parts[0] == "api" && parts[1] == "v1" {
		// 从 /api/v1/module/... 提取模块名
		return parts[2]
	}

	if len(parts) >= 1 {
		return parts[0]
	}

	return "unknown"
}

// isPublicEndpoint 判断端点是否为公开端点
func isPublicEndpoint(path string) bool {
	// 合并公开API路径和Swagger路径
	publicPaths := append(constants.PublicAPIPaths, constants.SwaggerAPIPaths...)

	for _, publicPath := range publicPaths {
		if strings.HasPrefix(path, publicPath) {
			return true
		}
	}

	return false
}

// MenuApiResourceAssociation 菜单-API资源关联关系
type MenuApiResourceAssociation struct {
	MenuID        string
	ApiResourceID string
}

// getMenuApiResourceAssociations 获取所有菜单-API资源关联关系
func getMenuApiResourceAssociations(ctx context.Context, client *ent.Client) ([]MenuApiResourceAssociation, error) {
	var associations []MenuApiResourceAssociation

	// 查询所有菜单及其关联的API资源
	menus, err := client.Menu.Query().
		WithAPIResources().
		All(ctx)
	if err != nil {
		return nil, fmt.Errorf("failed to query menus with API resources: %w", err)
	}

	for _, menu := range menus {
		for _, apiResource := range menu.Edges.APIResources {
			associations = append(associations, MenuApiResourceAssociation{
				MenuID:        menu.ID,
				ApiResourceID: apiResource.ID,
			})
		}
	}

	return associations, nil
}

// restoreMenuApiResourceAssociations 恢复菜单-API资源关联关系
func restoreMenuApiResourceAssociations(ctx context.Context, client *ent.Client, associations []MenuApiResourceAssociation) (int, error) {
	if len(associations) == 0 {
		return 0, nil
	}

	tx, err := client.Tx(ctx)
	if err != nil {
		return 0, fmt.Errorf("failed to start transaction: %w", err)
	}
	defer tx.Rollback()

	restoredCount := 0

	// 按菜单分组关联关系，批量更新
	menuAssociations := make(map[string][]string)
	for _, assoc := range associations {
		menuAssociations[assoc.MenuID] = append(menuAssociations[assoc.MenuID], assoc.ApiResourceID)
	}

	for menuID, apiResourceIDs := range menuAssociations {
		// 检查菜单是否存在
		menuExists, err := tx.Menu.Query().
			Where(menu.IDEQ(menuID)).
			Exist(ctx)
		if err != nil {
			log.Printf("WARN: Failed to check menu existence %s: %v", menuID, err)
			continue
		}
		if !menuExists {
			log.Printf("WARN: Menu %s no longer exists, skipping association restore", menuID)
			continue
		}

		// 检查API资源是否存在，只添加存在的资源
		var existingApiResourceIDs []string
		for _, apiResourceID := range apiResourceIDs {
			exists, err := tx.ApiResource.Query().
				Where(apiresource.IDEQ(apiResourceID)).
				Exist(ctx)
			if err != nil {
				log.Printf("WARN: Failed to check API resource existence %s: %v", apiResourceID, err)
				continue
			}
			if exists {
				existingApiResourceIDs = append(existingApiResourceIDs, apiResourceID)
			} else {
				log.Printf("WARN: API resource %s no longer exists, skipping", apiResourceID)
			}
		}

		if len(existingApiResourceIDs) == 0 {
			continue
		}

		// 添加关联关系
		_, err = tx.Menu.UpdateOneID(menuID).
			AddAPIResourceIDs(existingApiResourceIDs...).
			Save(ctx)
		if err != nil {
			log.Printf("WARN: Failed to restore associations for menu %s: %v", menuID, err)
			continue
		}

		restoredCount += len(existingApiResourceIDs)
	}

	if err := tx.Commit(); err != nil {
		return 0, fmt.Errorf("failed to commit transaction: %w", err)
	}

	return restoredCount, nil
}
