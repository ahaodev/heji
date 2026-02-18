package schema

import (
	"time"

	"entgo.io/ent"
	"entgo.io/ent/schema/edge"
	"entgo.io/ent/schema/field"
	"entgo.io/ent/schema/index"
	"github.com/rs/xid"
)

// DictType holds the schema definition for the DictType entity.
type DictType struct {
	ent.Schema
}

// Fields of the DictType.
func (DictType) Fields() []ent.Field {
	return []ent.Field{
		field.String("id").
			MaxLen(20).
			NotEmpty().
			Unique().
			Immutable().
			DefaultFunc(func() string {
				return xid.New().String()
			}).
			Comment("字典类型ID"),
		field.String("code").
			MaxLen(100).
			NotEmpty().
			Unique().
			Comment("字典类型编码，全局唯一"),
		field.String("name").
			MaxLen(200).
			NotEmpty().
			Comment("字典类型名称"),
		field.Enum("status").
			Values("active", "inactive").
			Default("active").
			Comment("字典类型状态：active-启用，inactive-禁用"),
		field.String("remark").
			MaxLen(500).
			Optional().
			Comment("备注"),
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

// Edges of the DictType.
func (DictType) Edges() []ent.Edge {
	return []ent.Edge{
		edge.To("items", DictItem.Type).Comment("字典类型下的字典项"),
	}
}

// Indexes of the DictType.
func (DictType) Indexes() []ent.Index {
	return []ent.Index{
		// 编码唯一索引
		index.Fields("code").
			Unique(),
		// 状态索引
		index.Fields("status"),
		// 名称索引（用于搜索）
		index.Fields("name"),
	}
}
