package schema

import (
	"time"

	"entgo.io/ent"
	"entgo.io/ent/schema/edge"
	"entgo.io/ent/schema/field"
	"entgo.io/ent/schema/index"
	"github.com/rs/xid"
)

// DictItem holds the schema definition for the DictItem entity.
type DictItem struct {
	ent.Schema
}

// Fields of the DictItem.
func (DictItem) Fields() []ent.Field {
	return []ent.Field{
		field.String("id").
			MaxLen(20).
			NotEmpty().
			Unique().
			Immutable().
			DefaultFunc(func() string {
				return xid.New().String()
			}).
			Comment("字典项ID"),
		field.String("type_id").
			MaxLen(20).
			NotEmpty().
			Comment("字典类型ID"),
		field.String("label").
			MaxLen(200).
			NotEmpty().
			Comment("字典项显示标签"),
		field.String("value").
			MaxLen(200).
			NotEmpty().
			Comment("字典项值"),
		field.Int("sort").
			Default(0).
			Comment("排序序号，默认0"),
		field.Bool("is_default").
			Default(false).
			Comment("是否为默认项，同一类型下最多一个默认项"),
		field.Enum("status").
			Values("active", "inactive").
			Default("active").
			Comment("字典项状态：active-启用，inactive-禁用"),
		field.String("color").
			MaxLen(50).
			Optional().
			Comment("颜色标识"),
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

// Edges of the DictItem.
func (DictItem) Edges() []ent.Edge {
	return []ent.Edge{
		edge.From("type", DictType.Type).
			Ref("items").
			Field("type_id").
			Required().
			Unique().
			Comment("所属字典类型"),
	}
}

// Indexes of the DictItem.
func (DictItem) Indexes() []ent.Index {
	return []ent.Index{
		// 同一类型下 (type_id, value) 唯一
		index.Fields("type_id", "value").
			Unique(),
		// 同一类型下 (type_id, label) 唯一，避免显示混淆
		index.Fields("type_id", "label").
			Unique(),
		// 类型ID索引
		index.Fields("type_id"),
		// 状态索引
		index.Fields("status"),
		// 排序索引
		index.Fields("sort"),
		// 默认项索引
		index.Fields("type_id", "is_default"),
	}
}
