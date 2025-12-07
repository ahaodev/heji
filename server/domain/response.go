package domain

const (
	SuccessCode = 0
	FailCode    = 1
)

type Response struct {
	Code int         `json:"code"`
	Msg  string      `json:"msg"`
	Data interface{} `json:"data,omitempty"`
}

type ErrorResponse struct {
	Message string `json:"message"`
}

func RespError(msg string) Response {
	return Response{Code: FailCode, Msg: msg, Data: nil}
}

func RespSuccess(data interface{}) Response {
	return Response{Code: SuccessCode, Msg: "ok", Data: data}
}
