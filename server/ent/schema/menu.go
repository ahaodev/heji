package schema

import (
	"time"

	"entgo.io/ent"
	"entgo.io/ent/schema/edge"
	"entgo.io/ent/schema/field"
	"entgo.io/ent/schema/index"
	"github.com/rs/xid"
)

// Menu holds the schema definition for the Menu entity.
type Menu struct {
	ent.Schema
}

// Fields of the Menu.
func (Menu) Fields() []ent.Field {
	return []ent.Field{
		field.String("id").
			MaxLen(32).
			NotEmpty().
			Unique().
			Immutable().
			DefaultFunc(func() string { return xid.New().String() }).
			Comment("菜单ID"),
		field.String("name").
			MaxLen(100).
			NotEmpty().
			Comment("菜单名称"),
		field.Int("sequence").
			Default(0).
			Comment("排序序号"),
		field.String("type").
			MaxLen(20).
			Default("menu").
			Comment("菜单类型 menu/button"),
		field.String("path").
			MaxLen(100).
			Optional().
			Comment("路由/路径"),
		field.String("icon").
			MaxLen(100).
			Default("").
			Comment("菜单图标"),
		field.String("component").
			MaxLen(100).
			Optional().
			Comment("组件路径"),
		field.String("route_name").
			MaxLen(50).
			Optional().
			Comment("路由名称"),
		field.String("query").
			MaxLen(200).
			Optional().
			Comment("路由参数"),
		field.Bool("is_frame").
			Default(false).
			Comment("是否外链(true是 false否)"),
		field.String("visible").
			MaxLen(10).
			Default("show").
			Comment("显示状态(show显示 hide隐藏)"),
		field.String("permissions").
			MaxLen(100).
			Optional().
			Comment("权限标识"),
		field.String("status").
			MaxLen(20).
			Default("active").
			Comment("状态 active/inactive"),
		field.String("parent_id").
			MaxLen(20).
			Optional().
			Nillable().
			Comment("父菜单ID"),
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

// Edges of the Menu.
func (Menu) Edges() []ent.Edge {
	return []ent.Edge{
		// 自引用父(唯一) -> 子(多)
		edge.From("parent", Menu.Type).
			Ref("children").
			Field("parent_id").
			Unique().
			Comment("父级菜单"),
		edge.To("children", Menu.Type).
			Comment("子级菜单"),
		edge.To("api_resources", ApiResource.Type).Comment("路由资源"),
		edge.From("roles", Role.Type).Ref("menus").Comment("有权限的角色"),
	}
}

// Indexes of the Menu.
func (Menu) Indexes() []ent.Index {
	return []ent.Index{
		index.Fields("status"),
		index.Fields("type"),
		index.Fields("parent_id"),
		index.Fields("sequence"),
		index.Fields("parent_id", "sequence"),
		index.Fields("visible"),
		index.Fields("is_frame"),
		index.Fields("permissions"),
	}
}
