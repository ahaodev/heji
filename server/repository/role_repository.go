package repository

import (
	"context"
	"fmt"
	"shadmin/domain"
	"shadmin/ent"
	"shadmin/ent/role"
	"time"
)

type entRoleRepository struct {
	client *ent.Client
}

func (rr *entRoleRepository) Assign(c context.Context, uid, rid string) error {
	return rr.client.User.UpdateOneID(uid).AddRoleIDs(rid).Exec(c)
}

func (rr *entRoleRepository) Revoke(c context.Context, uid, rid string) error {
	return rr.client.User.UpdateOneID(uid).RemoveRoleIDs(rid).Exec(c)
}

func NewRoleRepository(client *ent.Client) domain.RoleRepository {
	return &entRoleRepository{
		client: client,
	}
}

// convertEntRoleToDomain converts an ent Role to domain Role
func (rr *entRoleRepository) convertEntRoleToDomain(entRole *ent.Role) *domain.Role {
	if entRole == nil {
		return nil
	}

	role := &domain.Role{
		ID:        entRole.ID,
		Name:      entRole.Name,
		Sequence:  entRole.Sequence,
		Status:    entRole.Status,
		CreatedAt: entRole.CreatedAt,
		UpdatedAt: entRole.UpdatedAt,
	}

	// Extract menu IDs from edges if available
	if entRole.Edges.Menus != nil {
		menuIDs := make([]string, len(entRole.Edges.Menus))
		for i, menu := range entRole.Edges.Menus {
			menuIDs[i] = menu.ID
		}
		role.MenusIds = menuIDs
	}

	return role
}

func (rr *entRoleRepository) Create(c context.Context, role *domain.Role) error {
	now := time.Now()
	role.CreatedAt = now
	role.UpdatedAt = now

	builder := rr.client.Role.Create().
		SetName(role.Name).
		SetSequence(role.Sequence).
		SetStatus(role.Status).
		AddMenuIDs(role.MenusIds...).
		SetCreatedAt(role.CreatedAt).
		SetUpdatedAt(role.UpdatedAt)

	created, err := builder.Save(c)
	if err != nil {
		return fmt.Errorf("failed to create role: %w", err)
	}

	role.ID = created.ID
	return nil
}

func (rr *entRoleRepository) Fetch(c context.Context) ([]*domain.Role, error) {
	entRoles, err := rr.client.Role.Query().
		WithMenus().
		Order(ent.Asc(role.FieldSequence)).
		All(c)
	if err != nil {
		return nil, fmt.Errorf("failed to fetch roles: %w", err)
	}

	roles := make([]*domain.Role, len(entRoles))
	for i, entRole := range entRoles {
		roles[i] = rr.convertEntRoleToDomain(entRole)
	}

	return roles, nil
}

func (rr *entRoleRepository) FetchPaged(c context.Context, params domain.QueryParams) (*domain.RolePagedResult, error) {
	query := rr.client.Role.Query()

	// Get total count
	total, err := query.Clone().Count(c)
	if err != nil {
		return nil, fmt.Errorf("failed to count roles: %w", err)
	}

	// Apply pagination and ordering
	query = query.
		WithMenus().
		Order(ent.Asc(role.FieldSequence)).
		Offset((params.Page - 1) * params.PageSize).
		Limit(params.PageSize)

	entRoles, err := query.All(c)
	if err != nil {
		return nil, fmt.Errorf("failed to fetch paged roles: %w", err)
	}

	roles := make([]*domain.Role, len(entRoles))
	for i, entRole := range entRoles {
		roles[i] = rr.convertEntRoleToDomain(entRole)
	}

	return domain.NewPagedResult(roles, total, params.Page, params.PageSize), nil
}

func (rr *entRoleRepository) GetByID(c context.Context, id string) (*domain.Role, error) {
	entRole, err := rr.client.Role.Query().
		Where(role.ID(id)).
		WithMenus().
		Only(c)
	if err != nil {
		if ent.IsNotFound(err) {
			return nil, fmt.Errorf("role not found")
		}
		return nil, fmt.Errorf("failed to get role by ID: %w", err)
	}

	return rr.convertEntRoleToDomain(entRole), nil
}

func (rr *entRoleRepository) GetByName(c context.Context, name string) (*domain.Role, error) {
	entRole, err := rr.client.Role.Query().
		Where(role.Name(name)).
		WithMenus().
		Only(c)
	if err != nil {
		if ent.IsNotFound(err) {
			return nil, fmt.Errorf("role not found")
		}
		return nil, fmt.Errorf("failed to get role by name: %w", err)
	}

	return rr.convertEntRoleToDomain(entRole), nil
}

func (rr *entRoleRepository) Update(c context.Context, role *domain.Role) error {
	role.UpdatedAt = time.Now()

	builder := rr.client.Role.UpdateOneID(role.ID).
		SetName(role.Name).
		SetSequence(role.Sequence).
		SetStatus(role.Status).
		SetUpdatedAt(role.UpdatedAt).
		ClearMenus().
		AddMenuIDs(role.MenusIds...)

	_, err := builder.Save(c)
	if err != nil {
		return fmt.Errorf("failed to update role: %w", err)
	}

	return nil
}

func (rr *entRoleRepository) Delete(c context.Context, id string) error {
	err := rr.client.Role.DeleteOneID(id).Exec(c)
	if err != nil {
		if ent.IsNotFound(err) {
			return fmt.Errorf("role not found")
		}
		return fmt.Errorf("failed to delete role: %w", err)
	}

	return nil
}

func (rr *entRoleRepository) GetAllRoleNames(c context.Context) ([]string, error) {
	entRoles, err := rr.client.Role.Query().
		Where(role.Status("active")).
		Select(role.FieldName).
		All(c)
	if err != nil {
		return nil, fmt.Errorf("failed to fetch all role names: %w", err)
	}

	names := make([]string, len(entRoles))
	for i, entRole := range entRoles {
		names[i] = entRole.Name
	}

	return names, nil
}
