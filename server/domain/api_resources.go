package domain

import (
	"context"
	"strings"
	"time"
)

// ApiResource represents an API resource entity
type ApiResource struct {
	ID        string    `json:"id"` // format: "method:path"
	Method    string    `json:"method"`
	Path      string    `json:"path"`
	Handler   string    `json:"handler"`
	Module    string    `json:"module"`
	IsPublic  bool      `json:"is_public"`
	CreatedAt time.Time `json:"created_at"`
	UpdatedAt time.Time `json:"updated_at"`
}

// ApiResourceKey represents the composite key for API resource
type ApiResourceKey struct {
	Method string `json:"method"`
	Path   string `json:"path"`
}

// GenerateApiResourceID creates a stable ID from method and path
func GenerateApiResourceID(method, path string) string {
	return method + ":" + path
}

// ParseApiResourceID extracts method and path from ID
func ParseApiResourceID(id string) (method, path string) {
	parts := strings.SplitN(id, ":", 2)
	if len(parts) == 2 {
		return parts[0], parts[1]
	}
	return "", ""
}

// ApiResourcePagedResult represents paged API resource results
type ApiResourcePagedResult struct {
	Items      []*ApiResource `json:"items"`
	Total      int            `json:"total"`
	Page       int            `json:"page"`
	PageSize   int            `json:"page_size"`
	TotalPages int            `json:"total_pages"`
}

// ScanResult represents the result of API resource scanning
type ScanResult struct {
	TotalScanned int      `json:"total_scanned"`
	NewResources int      `json:"new_resources"`
	Updated      int      `json:"updated"`
	Deprecated   int      `json:"deprecated"`
	Errors       []string `json:"errors,omitempty"`
}

type ApiResourceUseCase interface {
	FetchPaged(ctx context.Context, params ApiResourceQueryParams) (*ApiResourcePagedResult, error)
}

// ApiResourceRepository defines the interface for API resource repository
type ApiResourceRepository interface {
	FetchPaged(ctx context.Context, params ApiResourceQueryParams) (*ApiResourcePagedResult, error)
}

// ApiResourceQueryParams represents query parameters for API resource
type ApiResourceQueryParams struct {
	QueryParams
	Method   string `json:"method"`
	Module   string `json:"module"`
	Status   string `json:"status"`
	IsPublic *bool  `json:"is_public"`
	Keyword  string `json:"keyword"`
	Path     string `json:"path"`
}

// NewApiResourcePagedResult creates a new paged result for API resources
func NewApiResourcePagedResult(items []*ApiResource, total, page, pageSize int) *ApiResourcePagedResult {
	totalPages := (total + pageSize - 1) / pageSize
	return &ApiResourcePagedResult{
		Items:      items,
		Total:      total,
		Page:       page,
		PageSize:   pageSize,
		TotalPages: totalPages,
	}
}
