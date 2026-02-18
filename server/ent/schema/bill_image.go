package schema

import (
	"github.com/rs/xid"

	"entgo.io/ent"
	"entgo.io/ent/schema/edge"
	"entgo.io/ent/schema/field"
	"entgo.io/ent/schema/index"
)

// BillImage holds the schema definition for the BillImage entity.
type BillImage struct {
	ent.Schema
}

// Fields of the BillImage.
func (BillImage) Fields() []ent.Field {
	return []ent.Field{
		field.String("id").
			DefaultFunc(func() string {
				return xid.New().String()
			}),
		field.String("bill_id").
			Comment("所属账单ID"),
		field.String("key").
			Comment("文件存储key"),
		field.String("ext").
			Optional().
			Comment("文件扩展名"),
		field.Int64("size").
			Optional().
			Default(0).
			Comment("文件大小"),
		field.String("md5").
			Optional().
			Comment("文件MD5"),
	}
}

// Edges of the BillImage.
func (BillImage) Edges() []ent.Edge {
	return []ent.Edge{
		edge.From("bill", Bill.Type).
			Ref("bill_images").
			Unique().
			Required().
			Field("bill_id"),
	}
}

// Indexes of the BillImage.
func (BillImage) Indexes() []ent.Index {
	return []ent.Index{
		index.Fields("bill_id"),
	}
}
