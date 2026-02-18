package bootstrap

import (
	"context"
	"fmt"
	"log"
	"shadmin/ent"
	"shadmin/internal/casbin"
	"time"
)

// CasbinInitializer casbin初始化器
type CasbinInitializer struct {
	entClient   *ent.Client
	syncService *casbin.SyncService
	casManager  casbin.Manager
}

// NewCasbinInitializer 创建casbin初始化器
func NewCasbinInitializer(entClient *ent.Client, casManager casbin.Manager) *CasbinInitializer {
	syncService := casbin.NewSyncService(entClient, casManager)

	return &CasbinInitializer{
		entClient:   entClient,
		syncService: syncService,
		casManager:  casManager,
	}
}

// InitializeCasbin 初始化casbin系统
// 这个方法会在应用启动时调用，执行以下操作：
// 1. 初始化casbin管理器
// 2. 从数据库同步权限数据到casbin
// 3. 验证同步结果
func (ci *CasbinInitializer) InitializeCasbin(ctx context.Context) error {
	log.Printf("INFO: 开始初始化Casbin权限系统...")

	startTime := time.Now()

	// 1. 验证casbin管理器是否已初始化
	if !casbin.IsInitialized() {
		return fmt.Errorf("casbin管理器未初始化，请先调用casbin.Initialize()")
	}

	// 2. 从数据库同步权限数据到casbin
	log.Printf("INFO: 开始从数据库同步权限数据到Casbin...")
	if err := ci.syncService.SyncFromDatabase(ctx); err != nil {
		return fmt.Errorf("从数据库同步权限数据失败: %w", err)
	}

	// 3. 验证同步结果
	stats, err := ci.syncService.GetSyncStats(ctx)
	if err != nil {
		log.Printf("WARN: 获取同步统计失败: %v", err)
	} else {
		log.Printf("INFO: Casbin同步统计:")
		log.Printf("  - 数据库用户角色关系: %d", stats.DatabaseUserRoles)
		log.Printf("  - 数据库角色权限关系: %d", stats.DatabaseRolePermissions)
		log.Printf("  - Casbin角色映射: %d", stats.CasbinRoles)
		log.Printf("  - Casbin权限策略: %d", stats.CasbinPolicies)

		if !stats.IsHealthy() {
			log.Printf("WARN: Casbin同步状态可能不健康，请检查数据")
		}
	}

	duration := time.Since(startTime)
	log.Printf("INFO: Casbin权限系统初始化完成，耗时: %v", duration)

	return nil
}

// SyncFromDatabase 手动触发从数据库同步
func (ci *CasbinInitializer) SyncFromDatabase(ctx context.Context) error {
	return ci.syncService.SyncFromDatabase(ctx)
}

// GetSyncStats 获取同步统计信息
func (ci *CasbinInitializer) GetSyncStats(ctx context.Context) (*casbin.SyncStats, error) {
	return ci.syncService.GetSyncStats(ctx)
}

// GetSyncService 获取同步服务实例
func (ci *CasbinInitializer) GetSyncService() *casbin.SyncService {
	return ci.syncService
}

// ValidateCasbinData 验证casbin数据完整性
func (ci *CasbinInitializer) ValidateCasbinData(ctx context.Context) error {
	// 获取同步统计
	stats, err := ci.syncService.GetSyncStats(ctx)
	if err != nil {
		return fmt.Errorf("获取同步统计失败: %w", err)
	}

	// 检查是否有基础数据
	if stats.CasbinRoles == 0 {
		return fmt.Errorf("casbin中没有角色映射数据")
	}

	if stats.CasbinPolicies == 0 {
		return fmt.Errorf("casbin中没有权限策略数据")
	}

	log.Printf("INFO: Casbin数据验证通过")
	return nil
}

// HealthCheck 健康检查
func (ci *CasbinInitializer) HealthCheck(ctx context.Context) (bool, error) {
	stats, err := ci.syncService.GetSyncStats(ctx)
	if err != nil {
		return false, fmt.Errorf("获取同步统计失败: %w", err)
	}

	return stats.IsHealthy(), nil
}

// SetupHooks 注册 Ent Hook 实现自动同步
func (ci *CasbinInitializer) SetupHooks() {
	// 监听 User, Role, Menu 的变更，在事务提交后触发 Casbin 同步
	ci.entClient.Use(func(next ent.Mutator) ent.Mutator {
		return ent.MutateFunc(func(ctx context.Context, m ent.Mutation) (ent.Value, error) {
			// 1. 执行原始变更
			v, err := next.Mutate(ctx, m)
			if err != nil {
				return nil, err
			}

			// 2. 检查是否为权限相关表
			schemaType := m.Type()
			if schemaType == ent.TypeUser || schemaType == ent.TypeRole || schemaType == ent.TypeMenu {
				// 定义同步逻辑
				doSync := func() {
					go func() {
						// 稍微延迟确保数据库事务完全传播
						time.Sleep(100 * time.Millisecond)
						// 设置同步超时 1分钟
						syncCtx, cancel := context.WithTimeout(context.Background(), 1*time.Minute)
						defer cancel()

						log.Printf("INFO: 触发 Casbin 自动同步 (来源: %s 变更)", schemaType)
						if err := ci.syncService.SyncFromDatabase(syncCtx); err != nil {
							log.Printf("ERROR: Casbin 自动同步失败: %v", err)
						}
					}()
				}

				// 3. 检查是否在事务中
				if tx := ent.TxFromContext(ctx); tx != nil {
					// 在事务提交后触发
					tx.OnCommit(func(next ent.Committer) ent.Committer {
						return ent.CommitFunc(func(ctx context.Context, tx *ent.Tx) error {
							err := next.Commit(ctx, tx)
							if err == nil {
								doSync()
							}
							return err
						})
					})
				} else {
					// 非事务环境，直接触发
					doSync()
				}
			}

			return v, nil
		})
	})
}

// InitCasbinHooks 初始化Casbin Hooks
// 在应用启动完成后调用，执行一次全量同步并注册Hook
func InitCasbinHooks(app *Application) {
	ctx := context.Background()
	if err := app.CasbinInitializer.SyncFromDatabase(ctx); err != nil {
		log.Printf("ERROR: initial casbin sync failed: %v", err)
	}
	app.CasbinInitializer.SetupHooks()
}
