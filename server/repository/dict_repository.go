package repository

import (
	"context"
	"shadmin/domain"
	"shadmin/ent"
	"shadmin/ent/dictitem"
	"shadmin/ent/dicttype"
	"time"
)

// Helper functions for status conversion
func domainStatusToDictTypeStatus(status string) dicttype.Status {
	switch status {
	case "active":
		return dicttype.StatusActive
	case "inactive":
		return dicttype.StatusInactive
	default:
		return dicttype.StatusActive
	}
}

func dictTypeStatusToDomainStatus(status dicttype.Status) string {
	return string(status)
}

func domainStatusToDictItemStatus(status string) dictitem.Status {
	switch status {
	case "active":
		return dictitem.StatusActive
	case "inactive":
		return dictitem.StatusInactive
	default:
		return dictitem.StatusActive
	}
}

func dictItemStatusToDomainStatus(status dictitem.Status) string {
	return string(status)
}

type entDictRepository struct {
	client *ent.Client
}

func NewDictRepository(client *ent.Client) domain.DictRepository {
	return &entDictRepository{
		client: client,
	}
}

// Helper function to convert ent DictType to domain DictType
func (dr *entDictRepository) convertEntDictTypeToDomain(entType *ent.DictType) *domain.DictType {
	if entType == nil {
		return nil
	}

	return &domain.DictType{
		ID:        entType.ID,
		Code:      entType.Code,
		Name:      entType.Name,
		Status:    dictTypeStatusToDomainStatus(entType.Status),
		Remark:    entType.Remark,
		CreatedAt: entType.CreatedAt,
		UpdatedAt: entType.UpdatedAt,
	}
}

// Helper function to convert ent DictItem to domain DictItem
func (dr *entDictRepository) convertEntDictItemToDomain(entItem *ent.DictItem) *domain.DictItem {
	if entItem == nil {
		return nil
	}

	return &domain.DictItem{
		ID:        entItem.ID,
		TypeID:    entItem.TypeID,
		Label:     entItem.Label,
		Value:     entItem.Value,
		Sort:      entItem.Sort,
		IsDefault: entItem.IsDefault,
		Status:    dictItemStatusToDomainStatus(entItem.Status),
		Color:     entItem.Color,
		Remark:    entItem.Remark,
		CreatedAt: entItem.CreatedAt,
		UpdatedAt: entItem.UpdatedAt,
	}
}

// 字典类型相关实现

func (dr *entDictRepository) CreateType(ctx context.Context, dictType *domain.DictType) error {
	// 检查code是否已存在
	exists, err := dr.client.DictType.Query().
		Where(dicttype.Code(dictType.Code)).
		Exist(ctx)
	if err != nil {
		return err
	}
	if exists {
		return domain.ErrDictTypeCodeExists
	}

	now := time.Now()
	dictType.CreatedAt = now
	dictType.UpdatedAt = now

	created, err := dr.client.DictType.
		Create().
		SetCode(dictType.Code).
		SetName(dictType.Name).
		SetStatus(domainStatusToDictTypeStatus(dictType.Status)).
		SetNillableRemark(&dictType.Remark).
		SetCreatedAt(dictType.CreatedAt).
		SetUpdatedAt(dictType.UpdatedAt).
		Save(ctx)

	if err != nil {
		return err
	}

	dictType.ID = created.ID
	return nil
}

func (dr *entDictRepository) GetTypeByID(ctx context.Context, id string) (*domain.DictType, error) {
	entType, err := dr.client.DictType.
		Query().
		Where(dicttype.ID(id)).
		First(ctx)

	if err != nil {
		if ent.IsNotFound(err) {
			return nil, domain.ErrDictTypeNotFound
		}
		return nil, err
	}

	return dr.convertEntDictTypeToDomain(entType), nil
}

func (dr *entDictRepository) GetTypeByCode(ctx context.Context, code string) (*domain.DictType, error) {
	entType, err := dr.client.DictType.
		Query().
		Where(dicttype.Code(code)).
		First(ctx)

	if err != nil {
		if ent.IsNotFound(err) {
			return nil, domain.ErrDictTypeNotFound
		}
		return nil, err
	}

	return dr.convertEntDictTypeToDomain(entType), nil
}

func (dr *entDictRepository) FetchTypes(ctx context.Context, params domain.DictTypeQueryParams) (*domain.PagedResult[*domain.DictType], error) {
	query := dr.client.DictType.Query()

	// 应用过滤条件
	if params.Code != "" {
		query = query.Where(dicttype.CodeContains(params.Code))
	}
	if params.Name != "" {
		query = query.Where(dicttype.NameContains(params.Name))
	}
	if params.Status != "" {
		query = query.Where(dicttype.StatusEQ(domainStatusToDictTypeStatus(params.Status)))
	}
	if params.Search != "" {
		query = query.Where(
			dicttype.Or(
				dicttype.CodeContains(params.Search),
				dicttype.NameContains(params.Search),
			),
		)
	}

	// 获取总数
	total, err := query.Clone().Count(ctx)
	if err != nil {
		return nil, err
	}

	// 应用排序
	if params.SortBy != "" {
		switch params.SortBy {
		case "code":
			if params.Order == "desc" {
				query = query.Order(ent.Desc(dicttype.FieldCode))
			} else {
				query = query.Order(ent.Asc(dicttype.FieldCode))
			}
		case "name":
			if params.Order == "desc" {
				query = query.Order(ent.Desc(dicttype.FieldName))
			} else {
				query = query.Order(ent.Asc(dicttype.FieldName))
			}
		default:
			if params.Order == "desc" {
				query = query.Order(ent.Desc(dicttype.FieldCreatedAt))
			} else {
				query = query.Order(ent.Asc(dicttype.FieldCreatedAt))
			}
		}
	} else {
		query = query.Order(ent.Desc(dicttype.FieldCreatedAt))
	}

	// 应用分页
	var entTypes []*ent.DictType
	if params.Page > 0 && params.PageSize > 0 {
		offset := (params.Page - 1) * params.PageSize
		entTypes, err = query.Offset(offset).Limit(params.PageSize).All(ctx)
	} else {
		entTypes, err = query.All(ctx)
	}
	if err != nil {
		return nil, err
	}

	// 转换为domain对象
	var result []*domain.DictType
	for _, entType := range entTypes {
		result = append(result, dr.convertEntDictTypeToDomain(entType))
	}

	return domain.NewPagedResult(result, total, params.Page, params.PageSize), nil
}

func (dr *entDictRepository) UpdateType(ctx context.Context, id string, updates domain.UpdateDictTypeRequest) error {
	// 检查记录是否存在
	exists, err := dr.client.DictType.Query().Where(dicttype.ID(id)).Exist(ctx)
	if err != nil {
		return err
	}
	if !exists {
		return domain.ErrDictTypeNotFound
	}

	updateQuery := dr.client.DictType.UpdateOneID(id)

	if updates.Code != nil {
		// 检查新code是否已被其他记录使用
		if exists, err := dr.client.DictType.Query().
			Where(dicttype.And(dicttype.Code(*updates.Code), dicttype.Not(dicttype.ID(id)))).
			Exist(ctx); err != nil {
			return err
		} else if exists {
			return domain.ErrDictTypeCodeExists
		}
		updateQuery = updateQuery.SetCode(*updates.Code)
	}
	if updates.Name != nil {
		updateQuery = updateQuery.SetName(*updates.Name)
	}
	if updates.Status != nil {
		updateQuery = updateQuery.SetStatus(domainStatusToDictTypeStatus(*updates.Status))
	}
	if updates.Remark != nil {
		updateQuery = updateQuery.SetRemark(*updates.Remark)
	}

	_, err = updateQuery.Save(ctx)
	return err
}

func (dr *entDictRepository) DeleteType(ctx context.Context, id string) error {
	// 检查是否有关联的字典项
	hasItems, err := dr.client.DictItem.Query().
		Where(dictitem.TypeID(id)).
		Exist(ctx)
	if err != nil {
		return err
	}
	if hasItems {
		return domain.ErrDictTypeHasItems
	}

	// 删除字典类型
	err = dr.client.DictType.DeleteOneID(id).Exec(ctx)
	if err != nil {
		if ent.IsNotFound(err) {
			return domain.ErrDictTypeNotFound
		}
		return err
	}

	return nil
}

// 字典项相关实现

func (dr *entDictRepository) CreateItem(ctx context.Context, dictItem *domain.DictItem) error {
	// 开启事务
	tx, err := dr.client.Tx(ctx)
	if err != nil {
		return err
	}
	defer func() {
		if v := recover(); v != nil {
			_ = tx.Rollback()
			panic(v)
		}
	}()

	// 检查字典类型是否存在
	typeExists, err := tx.DictType.Query().Where(dicttype.ID(dictItem.TypeID)).Exist(ctx)
	if err != nil {
		return tx.Rollback()
	}
	if !typeExists {
		_ = tx.Rollback()
		return domain.ErrDictTypeNotFound
	}

	// 检查同一类型下value是否已存在
	valueExists, err := tx.DictItem.Query().
		Where(dictitem.And(dictitem.TypeID(dictItem.TypeID), dictitem.Value(dictItem.Value))).
		Exist(ctx)
	if err != nil {
		return tx.Rollback()
	}
	if valueExists {
		_ = tx.Rollback()
		return domain.ErrDictItemValueExists
	}

	// 如果设置为默认项，需要清除同类型的其他默认项
	if dictItem.IsDefault {
		_, err = tx.DictItem.Update().
			Where(dictitem.And(dictitem.TypeID(dictItem.TypeID), dictitem.IsDefault(true))).
			SetIsDefault(false).
			Save(ctx)
		if err != nil {
			return tx.Rollback()
		}
	}

	now := time.Now()
	dictItem.CreatedAt = now
	dictItem.UpdatedAt = now

	created, err := tx.DictItem.
		Create().
		SetTypeID(dictItem.TypeID).
		SetLabel(dictItem.Label).
		SetValue(dictItem.Value).
		SetSort(dictItem.Sort).
		SetIsDefault(dictItem.IsDefault).
		SetStatus(domainStatusToDictItemStatus(dictItem.Status)).
		SetNillableColor(&dictItem.Color).
		SetNillableRemark(&dictItem.Remark).
		SetCreatedAt(dictItem.CreatedAt).
		SetUpdatedAt(dictItem.UpdatedAt).
		Save(ctx)

	if err != nil {
		return tx.Rollback()
	}

	dictItem.ID = created.ID
	return tx.Commit()
}

func (dr *entDictRepository) GetItemByID(ctx context.Context, id string) (*domain.DictItem, error) {
	entItem, err := dr.client.DictItem.
		Query().
		Where(dictitem.ID(id)).
		First(ctx)

	if err != nil {
		if ent.IsNotFound(err) {
			return nil, domain.ErrDictItemNotFound
		}
		return nil, err
	}

	return dr.convertEntDictItemToDomain(entItem), nil
}

func (dr *entDictRepository) FetchItems(ctx context.Context, params domain.DictItemQueryParams) (*domain.PagedResult[*domain.DictItem], error) {
	query := dr.client.DictItem.Query()

	// 应用过滤条件
	if params.TypeID != "" {
		query = query.Where(dictitem.TypeID(params.TypeID))
	} else if params.TypeCode != "" {
		// 通过TypeCode查询，需要先获取TypeID
		dictType, err := dr.client.DictType.Query().
			Where(dicttype.Code(params.TypeCode)).
			First(ctx)
		if err != nil {
			if ent.IsNotFound(err) {
				// 如果字典类型不存在，返回空结果
				return domain.NewPagedResult([]*domain.DictItem{}, 0, params.Page, params.PageSize), nil
			}
			return nil, err
		}
		query = query.Where(dictitem.TypeID(dictType.ID))
	}

	if params.Label != "" {
		query = query.Where(dictitem.LabelContains(params.Label))
	}
	if params.Value != "" {
		query = query.Where(dictitem.ValueContains(params.Value))
	}
	if params.Status != "" {
		query = query.Where(dictitem.StatusEQ(domainStatusToDictItemStatus(params.Status)))
	}
	if params.Search != "" {
		query = query.Where(
			dictitem.Or(
				dictitem.LabelContains(params.Search),
				dictitem.ValueContains(params.Search),
			),
		)
	}

	// 获取总数
	total, err := query.Clone().Count(ctx)
	if err != nil {
		return nil, err
	}

	// 应用排序
	if params.SortBy != "" {
		switch params.SortBy {
		case "label":
			if params.Order == "desc" {
				query = query.Order(ent.Desc(dictitem.FieldLabel))
			} else {
				query = query.Order(ent.Asc(dictitem.FieldLabel))
			}
		case "value":
			if params.Order == "desc" {
				query = query.Order(ent.Desc(dictitem.FieldValue))
			} else {
				query = query.Order(ent.Asc(dictitem.FieldValue))
			}
		case "sort":
			if params.Order == "desc" {
				query = query.Order(ent.Desc(dictitem.FieldSort))
			} else {
				query = query.Order(ent.Asc(dictitem.FieldSort))
			}
		default:
			if params.Order == "desc" {
				query = query.Order(ent.Desc(dictitem.FieldCreatedAt))
			} else {
				query = query.Order(ent.Asc(dictitem.FieldCreatedAt))
			}
		}
	} else {
		// 默认按sort正序，然后按创建时间倒序
		query = query.Order(ent.Asc(dictitem.FieldSort), ent.Desc(dictitem.FieldCreatedAt))
	}

	// 应用分页
	var entItems []*ent.DictItem
	if params.Page > 0 && params.PageSize > 0 {
		offset := (params.Page - 1) * params.PageSize
		entItems, err = query.Offset(offset).Limit(params.PageSize).All(ctx)
	} else {
		entItems, err = query.All(ctx)
	}
	if err != nil {
		return nil, err
	}

	// 转换为domain对象
	var result []*domain.DictItem
	for _, entItem := range entItems {
		result = append(result, dr.convertEntDictItemToDomain(entItem))
	}

	return domain.NewPagedResult(result, total, params.Page, params.PageSize), nil
}

func (dr *entDictRepository) UpdateItem(ctx context.Context, id string, updates domain.UpdateDictItemRequest) error {
	// 开启事务
	tx, err := dr.client.Tx(ctx)
	if err != nil {
		return err
	}
	defer func() {
		if v := recover(); v != nil {
			_ = tx.Rollback()
			panic(v)
		}
	}()

	// 获取当前记录
	currentItem, err := tx.DictItem.Query().Where(dictitem.ID(id)).First(ctx)
	if err != nil {
		if ent.IsNotFound(err) {
			_ = tx.Rollback()
			return domain.ErrDictItemNotFound
		}
		return tx.Rollback()
	}

	updateQuery := tx.DictItem.UpdateOneID(id)

	// 检查value唯一性
	if updates.Value != nil && *updates.Value != currentItem.Value {
		valueExists, err := tx.DictItem.Query().
			Where(dictitem.And(
				dictitem.TypeID(currentItem.TypeID),
				dictitem.Value(*updates.Value),
				dictitem.Not(dictitem.ID(id)),
			)).
			Exist(ctx)
		if err != nil {
			return tx.Rollback()
		}
		if valueExists {
			_ = tx.Rollback()
			return domain.ErrDictItemValueExists
		}
		updateQuery = updateQuery.SetValue(*updates.Value)
	}

	if updates.Label != nil {
		updateQuery = updateQuery.SetLabel(*updates.Label)
	}
	if updates.Sort != nil {
		updateQuery = updateQuery.SetSort(*updates.Sort)
	}
	if updates.Status != nil {
		updateQuery = updateQuery.SetStatus(domainStatusToDictItemStatus(*updates.Status))
	}
	if updates.Color != nil {
		updateQuery = updateQuery.SetColor(*updates.Color)
	}
	if updates.Remark != nil {
		updateQuery = updateQuery.SetRemark(*updates.Remark)
	}

	// 处理默认项逻辑
	if updates.IsDefault != nil {
		if *updates.IsDefault && !currentItem.IsDefault {
			// 设置为默认项，需要清除同类型的其他默认项
			_, err = tx.DictItem.Update().
				Where(dictitem.And(
					dictitem.TypeID(currentItem.TypeID),
					dictitem.IsDefault(true),
					dictitem.Not(dictitem.ID(id)),
				)).
				SetIsDefault(false).
				Save(ctx)
			if err != nil {
				return tx.Rollback()
			}
		}
		updateQuery = updateQuery.SetIsDefault(*updates.IsDefault)
	}

	_, err = updateQuery.Save(ctx)
	if err != nil {
		return tx.Rollback()
	}

	return tx.Commit()
}

func (dr *entDictRepository) DeleteItem(ctx context.Context, id string) error {
	err := dr.client.DictItem.DeleteOneID(id).Exec(ctx)
	if err != nil {
		if ent.IsNotFound(err) {
			return domain.ErrDictItemNotFound
		}
		return err
	}
	return nil
}

// 便捷查询实现

func (dr *entDictRepository) GetItemsByTypeCode(ctx context.Context, typeCode string, status string) ([]*domain.DictItem, error) {
	// 先获取字典类型
	dictType, err := dr.client.DictType.Query().
		Where(dicttype.Code(typeCode)).
		First(ctx)
	if err != nil {
		if ent.IsNotFound(err) {
			return []*domain.DictItem{}, nil // 返回空切片而不是错误
		}
		return nil, err
	}

	query := dr.client.DictItem.Query().Where(dictitem.TypeID(dictType.ID))

	// 应用状态过滤（默认只返回active状态）
	if status == "" {
		status = "active"
	}
	query = query.Where(dictitem.StatusEQ(domainStatusToDictItemStatus(status)))

	// 按sort正序，然后按创建时间倒序
	entItems, err := query.Order(ent.Asc(dictitem.FieldSort), ent.Desc(dictitem.FieldCreatedAt)).All(ctx)
	if err != nil {
		return nil, err
	}

	var result []*domain.DictItem
	for _, entItem := range entItems {
		result = append(result, dr.convertEntDictItemToDomain(entItem))
	}

	return result, nil
}
