package schema

import (
	"github.com/rs/xid"

	"entgo.io/ent"
	"entgo.io/ent/schema/edge"
	"entgo.io/ent/schema/field"
	"entgo.io/ent/schema/index"
)

// Category holds the schema definition for the Category entity.
type Category struct {
	ent.Schema
}

// Fields of the Category.
func (Category) Fields() []ent.Field {
	return []ent.Field{
		field.String("id").
			DefaultFunc(func() string {
				return xid.New().String()
			}),
		field.String("book_id").
			Comment("所属账本ID"),
		field.String("name").
			Default("其他").
			Comment("分类名称"),
		field.Int("type").
			Comment("收入(1)/支出(-1)"),
		field.Int("level").
			Default(0).
			Comment("层级"),
		field.Int("index").
			Default(0).
			Comment("排序顺序"),
		field.String("parent_id").
			Optional().
			Comment("父分类ID"),
		field.String("create_user").
			Optional().
			Comment("创建人ID"),
	}
}

// Edges of the Category.
func (Category) Edges() []ent.Edge {
	return []ent.Edge{
		edge.From("book", Book.Type).
			Ref("categories").
			Unique().
			Required().
			Field("book_id"),
	}
}

// Indexes of the Category.
func (Category) Indexes() []ent.Index {
	return []ent.Index{
		index.Fields("book_id"),
		index.Fields("type"),
	}
}
