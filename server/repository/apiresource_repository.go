package repository

import (
	"context"
	"fmt"
	"shadmin/domain"
	"shadmin/ent"
	"shadmin/ent/apiresource"
)

type entApiResourceRepository struct {
	client *ent.Client
}

func NewApiResourceRepository(client *ent.Client) domain.ApiResourceRepository {
	return &entApiResourceRepository{
		client: client,
	}
}

// convertEntApiResourceToDomain converts an ent ApiResource to domain ApiResource
func (arr *entApiResourceRepository) convertEntApiResourceToDomain(entApiResource *ent.ApiResource) *domain.ApiResource {
	if entApiResource == nil {
		return nil
	}

	return &domain.ApiResource{
		ID:        entApiResource.ID,
		Method:    entApiResource.Method,
		Path:      entApiResource.Path,
		Handler:   entApiResource.Handler,
		Module:    entApiResource.Module,
		IsPublic:  entApiResource.IsPublic,
		CreatedAt: entApiResource.CreatedAt,
		UpdatedAt: entApiResource.UpdatedAt,
	}
}

func (arr *entApiResourceRepository) FetchPaged(ctx context.Context, params domain.ApiResourceQueryParams) (*domain.ApiResourcePagedResult, error) {
	query := arr.client.ApiResource.Query()

	// Apply filters
	apiParams := params
	if apiParams.Method != "" {
		query = query.Where(apiresource.Method(apiParams.Method))
	}
	if apiParams.Module != "" {
		query = query.Where(apiresource.Module(apiParams.Module))
	}

	if apiParams.IsPublic != nil {
		query = query.Where(apiresource.IsPublic(*apiParams.IsPublic))
	}
	if apiParams.Keyword != "" {
		query = query.Where(
			apiresource.Or(
				apiresource.PathContains(apiParams.Keyword),
			),
		)
	}
	if apiParams.Path != "" {
		query = query.Where(apiresource.PathContains(apiParams.Path))
	}

	// Get total count
	total, err := query.Clone().Count(ctx)
	if err != nil {
		return nil, fmt.Errorf("failed to count API resources: %w", err)
	}

	// Apply pagination and ordering
	query = query.
		Order(ent.Asc(apiresource.FieldModule), ent.Asc(apiresource.FieldPath), ent.Asc(apiresource.FieldMethod)).
		Offset((params.GetPage() - 1) * params.GetPageSize()).
		Limit(params.GetPageSize())

	entApiResources, err := query.All(ctx)
	if err != nil {
		return nil, fmt.Errorf("failed to fetch paged API resources: %w", err)
	}

	apiResources := make([]*domain.ApiResource, len(entApiResources))
	for i, entApiResource := range entApiResources {
		apiResources[i] = arr.convertEntApiResourceToDomain(entApiResource)
	}

	return domain.NewApiResourcePagedResult(apiResources, total, params.GetPage(), params.GetPageSize()), nil
}
