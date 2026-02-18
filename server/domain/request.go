package domain

// QueryParams 通用查询参数，包含分页信息、租户信息和用户权限信息
type QueryParams struct {
	IsAdmin  bool   `json:"-" form:"-"` // 是否管理员，不从请求参数获取，由中间件设置
	Page     int    `json:"page" form:"page"`
	PageSize int    `json:"page_size" form:"page_size"`
	SortBy   string `json:"sort_by" form:"sort_by"`
	Order    string `json:"order" form:"order"` // asc, desc
}

// SystemQueryParams 系统级查询参数，用于管理员跨租户操作
type SystemQueryParams struct {
	Page     int    `json:"page" form:"page"`
	PageSize int    `json:"page_size" form:"page_size"`
	SortBy   string `json:"sort_by" form:"sort_by"`
	Order    string `json:"order" form:"order"` // asc, desc
}

// GetPage returns the page number, defaulting to 1 if not set
func (qp *QueryParams) GetPage() int {
	if qp.Page <= 0 {
		return 1
	}
	return qp.Page
}

// GetPageSize returns the page size, defaulting to 10 if not set
func (qp *QueryParams) GetPageSize() int {
	if qp.PageSize <= 0 {
		return 10
	}
	if qp.PageSize > 10000 {
		return 10000
	}
	return qp.PageSize
}

// ValidateQueryParams 验证和设置默认分页参数以及租户权限
func ValidateQueryParams(params *QueryParams) error {
	// 验证分页参数
	if params.Page <= 0 {
		params.Page = 1
	}
	if params.PageSize <= 0 {
		params.PageSize = 10
	}
	if params.PageSize > 10000 {
		params.PageSize = 10000
	}
	return nil
}
