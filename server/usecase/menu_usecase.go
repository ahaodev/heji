package usecase

import (
	"context"
	"fmt"
	"log"
	"shadmin/domain"
	"shadmin/ent"
	"time"
)

type menuUsecase struct {
	client         *ent.Client
	menuRepository domain.MenuRepository
	contextTimeout time.Duration
}

func NewMenuUsecase(client *ent.Client, menuRepository domain.MenuRepository, timeout time.Duration) domain.MenuUseCase {
	return &menuUsecase{
		client:         client,
		menuRepository: menuRepository,
		contextTimeout: timeout,
	}
}

// Query operations
func (mu *menuUsecase) GetMenuTree(ctx context.Context) ([]domain.MenuTreeNode, error) {
	ctx, cancel := context.WithTimeout(ctx, mu.contextTimeout)
	defer cancel()

	return mu.menuRepository.GetMenuTree(ctx)
}
func (mu *menuUsecase) GetMenus(ctx context.Context, params domain.MenuQueryParams) (*domain.PagedResult[domain.Menu], error) {
	ctx, cancel := context.WithTimeout(ctx, mu.contextTimeout)
	defer cancel()

	// Validate query parameters
	_ = domain.ValidateMenuQueryParams(&params)

	return mu.menuRepository.GetMenus(ctx, params)
}

func (mu *menuUsecase) GetMenuByID(ctx context.Context, id string) (*domain.Menu, error) {
	ctx, cancel := context.WithTimeout(ctx, mu.contextTimeout)
	defer cancel()

	return mu.menuRepository.GetMenuByID(ctx, id)
}

// CRUD operations
func (mu *menuUsecase) CreateMenu(ctx context.Context, req *domain.CreateMenuRequest) (*domain.Menu, error) {
	ctx, cancel := context.WithTimeout(ctx, mu.contextTimeout)
	defer cancel()

	// Validate menu type
	validTypes := []string{domain.MenuTypeMenu, domain.MenuTypeButton}
	isValidType := false
	for _, validType := range validTypes {
		if req.Type == validType {
			isValidType = true
			break
		}
	}
	if !isValidType {
		return nil, domain.ErrInvalidMenuType
	}

	// Set default status if not provided
	if req.Status == "" {
		req.Status = domain.MenuStatusActive
	}

	// Validate status
	if req.Status != domain.MenuStatusActive && req.Status != domain.MenuStatusInactive {
		return nil, domain.ErrInvalidMenuStatus
	}

	// Create menu in repository
	createdMenu, err := mu.menuRepository.CreateMenu(ctx, req)
	if err != nil {
		return nil, err
	}

	// If menu has API resources and gets assigned to roles later,
	// the role assignment process will handle Casbin policy creation
	// No need to sync policies here as no roles are assigned yet

	return createdMenu, nil
}

func (mu *menuUsecase) UpdateMenu(ctx context.Context, id string, req *domain.UpdateMenuRequest) (*domain.Menu, error) {
	ctx, cancel := context.WithTimeout(ctx, mu.contextTimeout)
	defer cancel()

	// Validate menu type
	validTypes := []string{domain.MenuTypeMenu, domain.MenuTypeButton}
	isValidType := false
	for _, validType := range validTypes {
		if req.Type == validType {
			isValidType = true
			break
		}
	}
	if !isValidType {
		return nil, domain.ErrInvalidMenuType
	}

	// Validate status
	if req.Status != domain.MenuStatusActive && req.Status != domain.MenuStatusInactive {
		return nil, domain.ErrInvalidMenuStatus
	}

	// Update menu in repository
	updatedMenu, err := mu.menuRepository.UpdateMenu(ctx, id, req)
	if err != nil {
		return nil, err
	}

	// 注意: casbin权限同步由定时任务自动处理，无需在此手动操作
	log.Printf("INFO: Successfully updated menu %s", updatedMenu.ID)

	return updatedMenu, nil
}

func (mu *menuUsecase) DeleteMenu(ctx context.Context, id string) error {
	ctx, cancel := context.WithTimeout(ctx, mu.contextTimeout)
	defer cancel()

	// 1. 检查菜单是否存在并获取菜单信息
	menu, err := mu.menuRepository.GetMenuByID(ctx, id)
	if err != nil {
		return fmt.Errorf("menu not found: %w", err)
	}

	log.Printf("INFO: Starting deletion process for menu %s (ID: %s, Type: %s)",
		menu.Name, id, menu.Type)

	// 2. 在事务中递归删除菜单及其子菜单
	tx, err := mu.client.Tx(ctx)
	if err != nil {
		return fmt.Errorf("failed to begin transaction: %w", err)
	}
	defer func() { _ = tx.Rollback() }()

	// 递归删除菜单及其所有子菜单
	if err := mu.deleteMenuRecursively(ctx, id); err != nil {
		return fmt.Errorf("failed to delete menu recursively: %w", err)
	}

	// 提交事务
	if err := tx.Commit(); err != nil {
		return fmt.Errorf("failed to commit transaction: %w", err)
	}

	log.Printf("INFO: Successfully deleted menu %s and all its children", menu.Name)
	return nil
}

// deleteMenuRecursively 递归删除菜单及其所有子菜单
func (mu *menuUsecase) deleteMenuRecursively(ctx context.Context, menuID string) error {
	// 1. 获取当前菜单信息
	menu, err := mu.menuRepository.GetMenuByID(ctx, menuID)
	if err != nil {
		// 菜单不存在，可能已被删除，直接返回
		log.Printf("WARN: Menu %s not found during recursive deletion", menuID)
		return nil
	}

	// 2. 获取所有直接子菜单
	children, err := mu.menuRepository.GetChildrenMenus(ctx, menuID)
	if err != nil {
		return fmt.Errorf("failed to get children of menu %s: %w", menuID, err)
	}

	// 3. 递归删除所有子菜单
	for _, child := range children {
		log.Printf("INFO: Recursively deleting child menu %s (ID: %s) of parent %s",
			child.Name, child.ID, menu.Name)

		if err := mu.deleteMenuRecursively(ctx, child.ID); err != nil {
			return fmt.Errorf("failed to delete child menu %s: %w", child.ID, err)
		}
	}

	// 4. 删除当前菜单
	log.Printf("INFO: Deleting menu %s (ID: %s)", menu.Name, menuID)
	if err := mu.menuRepository.DeleteMenu(ctx, menuID); err != nil {
		return fmt.Errorf("failed to delete menu %s from database: %w", menuID, err)
	}

	// 注意: casbin权限同步由定时任务自动处理，无需在此手动清理
	log.Printf("INFO: Successfully deleted menu %s from database", menu.Name)

	return nil
}
