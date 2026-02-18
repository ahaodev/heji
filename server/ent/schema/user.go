package schema

import (
	"time"

	"github.com/rs/xid"

	"entgo.io/ent"
	"entgo.io/ent/schema/edge"
	"entgo.io/ent/schema/field"
	"entgo.io/ent/schema/index"
)

// User holds the schema definition for the User entity.
type User struct {
	ent.Schema
}

// Fields of the User.
func (User) Fields() []ent.Field {
	return []ent.Field{
		field.String("id").
			DefaultFunc(func() string {
				return xid.New().String()
			}),
		field.String("username").
			MaxLen(32).
			Unique(),
		field.String("email").
			MaxLen(100).
			Unique(),
		field.String("phone").
			MaxLen(20).
			Optional().
			Comment("用户手机号码"),
		field.Bool("is_admin").
			Default(false).
			Comment("是否为管理员"),
		field.Enum("status").
			Values("active", "inactive", "invited", "suspended").
			Default("active").
			Comment("用户状态：active-活跃，inactive-非活跃，invited-已邀请，suspended-已暂停"),
		field.Time("invited_at").
			Optional().
			Comment("邀请时间"),
		field.String("invited_by").
			Optional().
			Comment("邀请人ID"),
		field.String("avatar").
			MaxLen(255).
			Optional(),
		field.String("password").
			Sensitive().
			MaxLen(128),
		field.Time("created_at").
			Default(time.Now),
		field.Time("updated_at").
			Default(time.Now).
			UpdateDefault(time.Now),
	}
}

// Edges of the User.
func (User) Edges() []ent.Edge {
	return []ent.Edge{
		edge.To("roles", Role.Type).Comment("用户角色关系"),
		edge.To("books", Book.Type).Comment("用户加入的账本"),
	}
}

// Indexes of the User.
func (User) Indexes() []ent.Index {
	return []ent.Index{
		index.Fields("username"),
		index.Fields("email"),
		index.Fields("phone"),
		index.Fields("status"),
	}
}
