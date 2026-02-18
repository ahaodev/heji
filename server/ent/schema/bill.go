package schema

import (
	"time"

	"github.com/rs/xid"

	"entgo.io/ent"
	"entgo.io/ent/schema/edge"
	"entgo.io/ent/schema/field"
	"entgo.io/ent/schema/index"
)

// Bill holds the schema definition for the Bill entity.
type Bill struct {
	ent.Schema
}

// Fields of the Bill.
func (Bill) Fields() []ent.Field {
	return []ent.Field{
		field.String("id").
			DefaultFunc(func() string {
				return xid.New().String()
			}),
		field.String("book_id").
			Comment("所属账本ID"),
		field.Int64("money").
			Comment("金额(分)"),
		field.Int("type").
			Comment("收入(1)/支出(-1)"),
		field.String("category").
			Optional().
			Comment("分类名称"),
		field.String("crt_user").
			Comment("创建人"),
		field.String("time").
			Comment("账单日期"),
		field.String("remark").
			Optional().
			Comment("备注"),
		field.JSON("images", []string{}).
			Optional().
			Comment("票据图片列表"),
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

// Edges of the Bill.
func (Bill) Edges() []ent.Edge {
	return []ent.Edge{
		edge.From("book", Book.Type).
			Ref("bills").
			Unique().
			Required().
			Field("book_id"),
		edge.To("bill_images", BillImage.Type).Comment("账单关联的图片"),
	}
}

// Indexes of the Bill.
func (Bill) Indexes() []ent.Index {
	return []ent.Index{
		index.Fields("book_id"),
		index.Fields("time"),
		index.Fields("type"),
		index.Fields("crt_user"),
		index.Fields("book_id", "updated_at"),
	}
}
