package schema

import (
	"time"

	"entgo.io/ent"
	"entgo.io/ent/schema/edge"
	"entgo.io/ent/schema/field"
	"entgo.io/ent/schema/index"
	"github.com/rs/xid"
)

// Role holds the schema definition for the Role entity.
type Role struct {
	ent.Schema
}

// Fields of the Role.
func (Role) Fields() []ent.Field {
	return []ent.Field{
		field.String("id").
			MaxLen(20).
			NotEmpty().
			Unique().
			Immutable().
			DefaultFunc(func() string {
				return xid.New().String()
			}).
			Comment("角色ID"),
		field.String("name").
			MaxLen(100).
			NotEmpty().
			Comment("角色名称"),
		field.Int("sequence").
			Default(0).
			Comment("排序序号"),
		field.String("status").
			MaxLen(20).
			Default("active").
			Comment("角色状态 active/inactive"),
		field.Time("created_at").
			Default(time.Now).
			Immutable().
			Comment("创建时间"),
		field.Time("updated_at").
			Default(time.Now).
			UpdateDefault(time.Now).
			Comment("更新时间"),
	}
}

// Edges of the Role.
func (Role) Edges() []ent.Edge {
	return []ent.Edge{
		edge.From("users", User.Type).Ref("roles").Comment("角色用户"),
		edge.To("menus", Menu.Type).Comment("角色菜单权限"),
	}
}

// Indexes of the Role.
func (Role) Indexes() []ent.Index {
	return []ent.Index{
		// 角色名称唯一
		index.Fields("name").
			Unique(),
		// 状态索引
		index.Fields("status"),
		// 排序索引
		index.Fields("sequence"),
	}
}
