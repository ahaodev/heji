package bootstrap

import (
	"context"
	"log"

	"shadmin/ent/dictitem"
	"shadmin/ent/dicttype"
)

// InitDictData seeds initial dictionary data if empty
func InitDictData(app *Application) {
	ctx := context.Background()

	// Check if dictionary types already exist
	existingDictTypes, err := app.DB.DictType.Query().Count(ctx)
	if err != nil {
		log.Printf("check dict type data failed: %v", err)
		return
	}

	if existingDictTypes > 0 {
		log.Println("dictionary data already exists, skip init")
		return
	}

	// User status dictionary
	userStatusType, err := app.DB.DictType.Create().
		SetCode("user_status").
		SetName("User Status").
		SetStatus(dicttype.StatusActive).
		SetRemark("System user status enum").
		Save(ctx)
	if err != nil {
		log.Printf("create dict type 'user_status' failed: %v", err)
		return
	}

	userStatusItems := []struct {
		label     string
		value     string
		isDefault bool
		remark    string
	}{
		{"Active", "active", true, "Active user"},
		{"Inactive", "inactive", false, "Inactive user"},
		{"Locked", "locked", false, "Locked user"},
	}
	for i, item := range userStatusItems {
		_, err = app.DB.DictItem.Create().
			SetTypeID(userStatusType.ID).
			SetLabel(item.label).
			SetValue(item.value).
			SetSort(i).
			SetIsDefault(item.isDefault).
			SetStatus(dictitem.StatusActive).
			SetRemark(item.remark).
			Save(ctx)
		if err != nil {
			log.Printf("create user_status item failed: %v", err)
			continue
		}
	}

	log.Println("dictionary data initialized")
}
