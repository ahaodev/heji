package schema

import (
	"fmt"
	"time"

	"entgo.io/ent"
	"entgo.io/ent/schema/edge"
	"entgo.io/ent/schema/field"
	"entgo.io/ent/schema/index"
)

// ApiResource holds the schema definition for the ApiResource entity.
type ApiResource struct {
	ent.Schema
}

// Fields of the ApiResource.
func (ApiResource) Fields() []ent.Field {
	return []ent.Field{
		field.String("id").
			MaxLen(300).
			Immutable().
			Comment("复合主键：method:path格式，例如 GET:/api/v1/users"),
		field.String("method").
			MaxLen(10).
			NotEmpty().
			Validate(func(s string) error {
				validMethods := []string{"GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS"}
				for _, method := range validMethods {
					if s == method {
						return nil
					}
				}
				return fmt.Errorf("invalid HTTP method: %s", s)
			}).
			Comment("HTTP方法：GET, POST, PUT, DELETE等"),
		field.String("path").
			MaxLen(255).
			NotEmpty().
			Comment("API路径，例如 /api/v1/users"),
		field.String("handler").
			MaxLen(255).
			Comment("处理函数名称"),
		field.String("module").
			MaxLen(50).
			Optional().
			Comment("模块分组"),
		field.Bool("is_public").
			Default(false).
			Comment("是否为公开接口，无需权限验证"),
		field.Time("created_at").
			Default(time.Now),
		field.Time("updated_at").
			Default(time.Now).
			UpdateDefault(time.Now),
	}
}

// Edges of the ApiResource.
func (ApiResource) Edges() []ent.Edge {
	return []ent.Edge{
		edge.From("permissions", Menu.Type).
			Ref("api_resources"),
	}
}

// Indexes of the ApiResource.
func (ApiResource) Indexes() []ent.Index {
	return []ent.Index{
		index.Fields("method", "path").
			Unique(),
		index.Fields("module"),
		index.Fields("is_public"),
	}
}
