package schema

import (
	"time"

	"github.com/rs/xid"

	"entgo.io/ent"
	"entgo.io/ent/schema/field"
	"entgo.io/ent/schema/index"
)

// LoginLog holds the schema definition for the LoginLog entity.
type LoginLog struct {
	ent.Schema
}

// Fields of the LoginLog.
func (LoginLog) Fields() []ent.Field {
	return []ent.Field{
		field.String("id").
			DefaultFunc(func() string {
				return xid.New().String()
			}),
		field.String("username").
			MaxLen(100).
			Comment("登录账号"),
		field.String("login_ip").
			MaxLen(45).
			Comment("登录IP地址"),
		field.String("user_agent").
			MaxLen(500).
			Comment("用户代理字符串"),
		field.String("browser").
			MaxLen(100).
			Optional().
			Comment("浏览器类型"),
		field.String("os").
			MaxLen(100).
			Optional().
			Comment("操作系统"),
		field.String("device").
			MaxLen(100).
			Optional().
			Comment("设备类型"),
		field.Enum("status").
			Values("success", "failed").
			Comment("登录状态：success-成功，failed-失败"),
		field.String("failure_reason").
			MaxLen(255).
			Optional().
			Comment("失败原因"),
		field.Time("login_time").
			Default(time.Now).
			Comment("登录时间"),
	}
}

// Edges of the LoginLog.
func (LoginLog) Edges() []ent.Edge {
	return nil
}

// Indexes of the LoginLog.
func (LoginLog) Indexes() []ent.Index {
	return []ent.Index{
		index.Fields("username"),
		index.Fields("login_ip"),
		index.Fields("status"),
		index.Fields("login_time"),
	}
}
