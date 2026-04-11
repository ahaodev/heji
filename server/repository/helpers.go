package repository

import (
	"shadmin/ent"

	"entgo.io/ent/dialect/sql"
)

// ApplySorting returns an order function based on sort parameters and a field mapping.
// fieldMap maps user-facing sort field names (e.g. "username") to Ent field constants.
// If sortBy is not found in fieldMap, defaultField is used.
// Returns func(*sql.Selector) so it's compatible with all entity-specific OrderOption types.
func ApplySorting(sortBy, order string, fieldMap map[string]string, defaultField string) func(*sql.Selector) {
	field := defaultField
	if mapped, ok := fieldMap[sortBy]; ok {
		field = mapped
	}
	if order == "desc" {
		return ent.Desc(field)
	}
	return ent.Asc(field)
}
