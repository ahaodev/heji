package repository

import (
	"context"
	"shadmin/domain"
	"shadmin/ent"
	"shadmin/ent/menu"
)

type entMenuRepository struct {
	client *ent.Client
}

func NewMenuRepository(client *ent.Client) domain.MenuRepository {
	return &entMenuRepository{
		client: client,
	}
}

// GetMenuTree retrieves menu tree structure
func (mr *entMenuRepository) GetMenuTree(ctx context.Context) ([]domain.MenuTreeNode, error) {
	// Query all active menus, ordered by sequence, include API resources
	menus, err := mr.client.Menu.
		Query().
		Where(menu.Status(domain.MenuStatusActive)).
		WithAPIResources().
		Order(menu.BySequence()).
		All(ctx)

	if err != nil {
		return nil, err
	}

	// Convert ent.Menu to domain.MenuTreeNode
	var allNodes []domain.MenuTreeNode
	for _, m := range menus {
		// Extract API resource IDs
		var apiResourceIDs []string
		for _, apiResource := range m.Edges.APIResources {
			apiResourceIDs = append(apiResourceIDs, apiResource.ID)
		}

		node := domain.MenuTreeNode{
			ID:          m.ID,
			Name:        m.Name,
			Sequence:    m.Sequence,
			Type:        m.Type,
			Path:        stringToPtr(m.Path),
			Icon:        m.Icon,
			Component:   stringToPtr(m.Component),
			RouteName:   stringToPtr(m.RouteName),
			Query:       stringToPtr(m.Query),
			IsFrame:     m.IsFrame,
			Visible:     m.Visible,
			Permissions: stringToPtr(m.Permissions),
			Status:      m.Status,
			ParentID:    m.ParentID,
			Children:    []domain.MenuTreeNode{},
		}
		allNodes = append(allNodes, node)
	}

	// Build tree structure
	return buildMenuTree(allNodes), nil
}

// buildMenuTree constructs hierarchical tree structure from flat menu list
func buildMenuTree(nodes []domain.MenuTreeNode) []domain.MenuTreeNode {
	// Step 1: Find L1 directories (nodes without ParentID)
	var rootNodes []domain.MenuTreeNode
	for _, node := range nodes {
		if node.ParentID == nil || *node.ParentID == "" {
			rootNodes = append(rootNodes, node)
		}
	}

	// Step 2: For each L1 directory, find its children through ParentID
	for i := range rootNodes {
		rootNodes[i].Children = findChildren(rootNodes[i].ID, nodes)
	}

	return rootNodes
}

// findChildren recursively finds all children for a given parent ID
func findChildren(parentID string, allNodes []domain.MenuTreeNode) []domain.MenuTreeNode {
	var children []domain.MenuTreeNode

	for _, node := range allNodes {
		if node.ParentID != nil && *node.ParentID == parentID {
			child := node
			// Recursively find children of this child
			child.Children = findChildren(child.ID, allNodes)
			children = append(children, child)
		}
	}

	return children
}

// GetMenus retrieves paginated menus with filtering
func (mr *entMenuRepository) GetMenus(ctx context.Context, params domain.MenuQueryParams) (*domain.PagedResult[domain.Menu], error) {
	query := mr.client.Menu.Query()

	// Get total count
	total, err := query.Clone().Count(ctx)
	if err != nil {
		return nil, err
	}

	// Apply pagination and ordering, and include API resources
	offset := (params.Page - 1) * params.PageSize
	menus, err := query.
		WithAPIResources().
		Order(menu.BySequence(), menu.ByCreatedAt()).
		Offset(offset).
		All(ctx)

	if err != nil {
		return nil, err
	}

	// Convert to domain menus
	var result []domain.Menu
	for _, m := range menus {
		// Extract API resource IDs
		var apiResourceIDs []string
		for _, apiResource := range m.Edges.APIResources {
			apiResourceIDs = append(apiResourceIDs, apiResource.ID)
		}

		result = append(result, domain.Menu{
			ID:           m.ID,
			Name:         m.Name,
			Sequence:     m.Sequence,
			Type:         m.Type,
			Path:         stringToPtr(m.Path),
			Icon:         m.Icon,
			Component:    stringToPtr(m.Component),
			RouteName:    stringToPtr(m.RouteName),
			Query:        stringToPtr(m.Query),
			IsFrame:      m.IsFrame,
			Visible:      m.Visible,
			Permissions:  stringToPtr(m.Permissions),
			Status:       m.Status,
			ParentID:     m.ParentID,
			ApiResources: apiResourceIDs,
			CreatedAt:    m.CreatedAt,
			UpdatedAt:    m.UpdatedAt,
		})
	}

	return domain.NewPagedResult(result, total, params.Page, params.PageSize), nil
}

// GetMenuByID retrieves a menu by its ID
func (mr *entMenuRepository) GetMenuByID(ctx context.Context, id string) (*domain.Menu, error) {
	m, err := mr.client.Menu.
		Query().
		Where(menu.ID(id)).
		WithAPIResources().
		First(ctx)

	if err != nil {
		return nil, err
	}

	// Extract API resource IDs
	var apiResourceIDs []string
	for _, apiResource := range m.Edges.APIResources {
		apiResourceIDs = append(apiResourceIDs, apiResource.ID)
	}

	return &domain.Menu{
		ID:           m.ID,
		Name:         m.Name,
		Sequence:     m.Sequence,
		Type:         m.Type,
		Path:         stringToPtr(m.Path),
		Icon:         m.Icon,
		Component:    stringToPtr(m.Component),
		RouteName:    stringToPtr(m.RouteName),
		Query:        stringToPtr(m.Query),
		IsFrame:      m.IsFrame,
		Visible:      m.Visible,
		Permissions:  stringToPtr(m.Permissions),
		Status:       m.Status,
		ParentID:     m.ParentID,
		CreatedAt:    m.CreatedAt,
		UpdatedAt:    m.UpdatedAt,
		ApiResources: apiResourceIDs,
	}, nil
}

// CreateMenu creates a new menu
func (mr *entMenuRepository) CreateMenu(ctx context.Context, req *domain.CreateMenuRequest) (*domain.Menu, error) {
	tx, err := mr.client.Tx(ctx)
	if err != nil {
		return nil, err
	}
	defer tx.Rollback()

	createQuery := tx.Menu.
		Create().
		SetName(req.Name).
		SetSequence(req.Sequence).
		SetType(req.Type).
		SetNillablePath(req.Path).
		SetIcon(req.Icon).
		SetNillableComponent(req.Component).
		SetNillableRouteName(req.RouteName).
		SetNillableQuery(req.Query).
		SetIsFrame(req.IsFrame).
		SetVisible(req.Visible).
		SetNillablePermissions(req.Permissions).
		SetStatus(req.Status).
		SetNillableParentID(req.ParentID)

	// Handle API resource associations
	if len(req.ApiResources) > 0 {
		createQuery = createQuery.AddAPIResourceIDs(req.ApiResources...)
	}

	created, err := createQuery.Save(ctx)
	if err != nil {
		return nil, err
	}

	if err := tx.Commit(); err != nil {
		return nil, err
	}

	// Fetch created menu with API resources
	return mr.GetMenuByID(ctx, created.ID)
}

// UpdateMenu updates an existing menu
func (mr *entMenuRepository) UpdateMenu(ctx context.Context, id string, req *domain.UpdateMenuRequest) (*domain.Menu, error) {
	tx, err := mr.client.Tx(ctx)
	if err != nil {
		return nil, err
	}
	defer tx.Rollback()

	updateQuery := tx.Menu.
		UpdateOneID(id).
		SetName(req.Name).
		SetSequence(req.Sequence).
		SetType(req.Type).
		SetNillablePath(req.Path).
		SetIcon(req.Icon).
		SetNillableComponent(req.Component).
		SetNillableRouteName(req.RouteName).
		SetNillableQuery(req.Query).
		SetIsFrame(req.IsFrame).
		SetVisible(req.Visible).
		SetNillablePermissions(req.Permissions).
		SetStatus(req.Status).
		SetNillableParentID(req.ParentID)

	// Handle API resource associations
	if len(req.ApiResources) > 0 {
		// Clear existing API resource associations
		updateQuery = updateQuery.ClearAPIResources()

		// Add new API resource associations
		var apiResourceIDs []string
		for _, apiResourceID := range req.ApiResources {
			apiResourceIDs = append(apiResourceIDs, apiResourceID)
		}
		updateQuery = updateQuery.AddAPIResourceIDs(apiResourceIDs...)
	} else {
		// If no API resources provided, clear all associations
		updateQuery = updateQuery.ClearAPIResources()
	}

	updated, err := updateQuery.Save(ctx)
	if err != nil {
		return nil, err
	}

	if err := tx.Commit(); err != nil {
		return nil, err
	}

	// Fetch updated menu with API resources
	return mr.GetMenuByID(ctx, updated.ID)
}

// GetChildrenMenus retrieves all direct children of a menu
func (mr *entMenuRepository) GetChildrenMenus(ctx context.Context, parentID string) ([]*domain.Menu, error) {
	menus, err := mr.client.Menu.
		Query().
		Where(menu.ParentID(parentID)).
		Order(menu.BySequence()).
		All(ctx)
	if err != nil {
		return nil, err
	}

	var result []*domain.Menu
	for _, m := range menus {
		result = append(result, &domain.Menu{
			ID:          m.ID,
			Name:        m.Name,
			Sequence:    m.Sequence,
			Type:        m.Type,
			Path:        stringToPtr(m.Path),
			Icon:        m.Icon,
			Component:   stringToPtr(m.Component),
			RouteName:   stringToPtr(m.RouteName),
			Query:       stringToPtr(m.Query),
			IsFrame:     m.IsFrame,
			Visible:     m.Visible,
			Permissions: stringToPtr(m.Permissions),
			Status:      m.Status,
			ParentID:    m.ParentID,
			CreatedAt:   m.CreatedAt,
			UpdatedAt:   m.UpdatedAt,
		})
	}

	return result, nil
}

// DeleteMenu deletes a menu (only the specified menu, not children)
func (mr *entMenuRepository) DeleteMenu(ctx context.Context, id string) error {
	// Delete the menu directly without checking children
	// The recursive deletion logic will be handled in the UseCase layer
	return mr.client.Menu.
		DeleteOneID(id).
		Exec(ctx)
}

// stringToPtr converts a string to *string, returning nil for empty strings
func stringToPtr(s string) *string {
	if s == "" {
		return nil
	}
	return &s
}
