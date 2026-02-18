package domain

const (
	SuccessCode = 0
	FailCode    = 1
	NotFondCode = 404
)

type Response struct {
	Code int         `json:"code"`
	Msg  string      `json:"msg"`
	Data interface{} `json:"data,omitempty"`
}

func RespError(msg interface{}) Response {
	var errMsg string
	switch v := msg.(type) {
	case string:
		errMsg = v // 如果是 string，直接赋值
	case error:
		errMsg = v.Error() // 如果是 error 类型，调用其 Error() 方法
	default:
		errMsg = "Unknown error" // 如果是其他类型，返回默认错误信息
	}
	return Response{Code: FailCode, Msg: errMsg, Data: nil}
}

func RespSuccess(data interface{}) Response {
	return Response{Code: SuccessCode, Msg: "OK", Data: data}
}

// PagedResult 通用分页结果
type PagedResult[T any] struct {
	List       []T `json:"list"`
	Total      int `json:"total"`
	Page       int `json:"page"`
	PageSize   int `json:"page_size"`
	TotalPages int `json:"total_pages"`
}

// NewPagedResult 创建分页结果
func NewPagedResult[T any](data []T, total, page, pageSize int) *PagedResult[T] {
	totalPages := (total + pageSize - 1) / pageSize
	return &PagedResult[T]{
		List:       data,
		Total:      total,
		Page:       page,
		PageSize:   pageSize,
		TotalPages: totalPages,
	}
}
