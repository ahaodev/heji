package schema

import (
	"time"

	"github.com/rs/xid"

	"entgo.io/ent"
	"entgo.io/ent/schema/edge"
	"entgo.io/ent/schema/field"
	"entgo.io/ent/schema/index"
)

// Book holds the schema definition for the Book entity.
type Book struct {
	ent.Schema
}

// Fields of the Book.
func (Book) Fields() []ent.Field {
	return []ent.Field{
		field.String("id").
			DefaultFunc(func() string {
				return xid.New().String()
			}),
		field.String("name").
			MaxLen(100).
			Comment("账本名称"),
		field.String("type").
			Optional().
			Comment("账本类型"),
		field.String("banner").
			Optional().
			Comment("封面图片URL"),
		field.String("crt_user_id").
			Comment("创建人ID"),
		field.Bool("is_initial").
			Default(false).
			Comment("是否为初始账本"),
		field.Time("deleted_at").
			Optional().
			Nillable().
			Comment("软删除时间，nil 表示未删除"),
		field.Time("created_at").
			Default(time.Now),
		field.Time("updated_at").
			Default(time.Now).
			UpdateDefault(time.Now),
	}
}

// Edges of the Book.
func (Book) Edges() []ent.Edge {
	return []ent.Edge{
		edge.To("bills", Bill.Type).Comment("账本下的账单"),
		edge.To("categories", Category.Type).Comment("账本下的分类"),
		edge.From("members", User.Type).Ref("books").Comment("账本成员"),
	}
}

// Indexes of the Book.
func (Book) Indexes() []ent.Index {
	return []ent.Index{
		index.Fields("crt_user_id"),
		index.Fields("name", "crt_user_id"),
		index.Fields("updated_at"),
	}
}
