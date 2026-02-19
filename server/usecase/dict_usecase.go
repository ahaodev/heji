package usecase

import (
	"context"
	"fmt"
	"shadmin/domain"
	"shadmin/ent"
	"time"
)

type dictUsecase struct {
	client         *ent.Client
	dictRepository domain.DictRepository
	contextTimeout time.Duration
}

func NewDictUsecase(client *ent.Client, dictRepository domain.DictRepository, timeout time.Duration) domain.DictUseCase {
	return &dictUsecase{
		client:         client,
		dictRepository: dictRepository,
		contextTimeout: timeout,
	}
}

// 字典类型相关业务逻辑

func (du *dictUsecase) CreateDictType(ctx context.Context, request *domain.CreateDictTypeRequest) (*domain.DictType, error) {
	ctx, cancel := context.WithTimeout(ctx, du.contextTimeout)
	defer cancel()

	// 设置默认状态
	if request.Status == "" {
		request.Status = "active"
	}

	// 验证状态值
	if request.Status != "active" && request.Status != "inactive" {
		return nil, fmt.Errorf("invalid status: must be 'active' or 'inactive'")
	}

	// 创建域模型
	dictType := &domain.DictType{
		Code:   request.Code,
		Name:   request.Name,
		Status: request.Status,
		Remark: request.Remark,
	}

	// 调用repository创建
	err := du.dictRepository.CreateType(ctx, dictType)
	if err != nil {
		return nil, err
	}

	return dictType, nil
}

func (du *dictUsecase) GetDictTypeByID(ctx context.Context, id string) (*domain.DictType, error) {
	ctx, cancel := context.WithTimeout(ctx, du.contextTimeout)
	defer cancel()

	return du.dictRepository.GetTypeByID(ctx, id)
}

func (du *dictUsecase) GetDictTypeByCode(ctx context.Context, code string) (*domain.DictType, error) {
	ctx, cancel := context.WithTimeout(ctx, du.contextTimeout)
	defer cancel()

	return du.dictRepository.GetTypeByCode(ctx, code)
}

func (du *dictUsecase) ListDictTypes(ctx context.Context, params domain.DictTypeQueryParams) (*domain.PagedResult[*domain.DictType], error) {
	ctx, cancel := context.WithTimeout(ctx, du.contextTimeout)
	defer cancel()

	// 验证和设置默认分页参数
	_ = domain.ValidateQueryParams(&params.QueryParams)

	return du.dictRepository.FetchTypes(ctx, params)
}

func (du *dictUsecase) UpdateDictType(ctx context.Context, id string, updates domain.UpdateDictTypeRequest) error {
	ctx, cancel := context.WithTimeout(ctx, du.contextTimeout)
	defer cancel()

	// 验证状态值（如果提供）
	if updates.Status != nil {
		if *updates.Status != "active" && *updates.Status != "inactive" {
			return fmt.Errorf("invalid status: must be 'active' or 'inactive'")
		}
	}

	return du.dictRepository.UpdateType(ctx, id, updates)
}

func (du *dictUsecase) DeleteDictType(ctx context.Context, id string) error {
	ctx, cancel := context.WithTimeout(ctx, du.contextTimeout)
	defer cancel()

	return du.dictRepository.DeleteType(ctx, id)
}

// 字典项相关业务逻辑

func (du *dictUsecase) CreateDictItem(ctx context.Context, request *domain.CreateDictItemRequest) (*domain.DictItem, error) {
	ctx, cancel := context.WithTimeout(ctx, du.contextTimeout)
	defer cancel()

	// 设置默认状态
	if request.Status == "" {
		request.Status = "active"
	}

	// 验证状态值
	if request.Status != "active" && request.Status != "inactive" {
		return nil, fmt.Errorf("invalid status: must be 'active' or 'inactive'")
	}

	// 验证字典类型是否存在
	_, err := du.dictRepository.GetTypeByID(ctx, request.TypeID)
	if err != nil {
		if err == domain.ErrDictTypeNotFound {
			return nil, fmt.Errorf("dictionary type not found")
		}
		return nil, err
	}

	// 创建域模型
	dictItem := &domain.DictItem{
		TypeID:    request.TypeID,
		Label:     request.Label,
		Value:     request.Value,
		Sort:      request.Sort,
		IsDefault: request.IsDefault,
		Status:    request.Status,
		Color:     request.Color,
		Remark:    request.Remark,
	}

	// 调用repository创建
	err = du.dictRepository.CreateItem(ctx, dictItem)
	if err != nil {
		return nil, err
	}

	return dictItem, nil
}

func (du *dictUsecase) GetDictItemByID(ctx context.Context, id string) (*domain.DictItem, error) {
	ctx, cancel := context.WithTimeout(ctx, du.contextTimeout)
	defer cancel()

	return du.dictRepository.GetItemByID(ctx, id)
}

func (du *dictUsecase) ListDictItems(ctx context.Context, params domain.DictItemQueryParams) (*domain.PagedResult[*domain.DictItem], error) {
	ctx, cancel := context.WithTimeout(ctx, du.contextTimeout)
	defer cancel()

	// 验证和设置默认分页参数
	_ = domain.ValidateQueryParams(&params.QueryParams)

	return du.dictRepository.FetchItems(ctx, params)
}

func (du *dictUsecase) UpdateDictItem(ctx context.Context, id string, updates domain.UpdateDictItemRequest) error {
	ctx, cancel := context.WithTimeout(ctx, du.contextTimeout)
	defer cancel()

	// 验证状态值（如果提供）
	if updates.Status != nil {
		if *updates.Status != "active" && *updates.Status != "inactive" {
			return fmt.Errorf("invalid status: must be 'active' or 'inactive'")
		}
	}

	return du.dictRepository.UpdateItem(ctx, id, updates)
}

func (du *dictUsecase) DeleteDictItem(ctx context.Context, id string) error {
	ctx, cancel := context.WithTimeout(ctx, du.contextTimeout)
	defer cancel()

	return du.dictRepository.DeleteItem(ctx, id)
}

// 便捷查询

func (du *dictUsecase) GetActiveItemsByTypeCode(ctx context.Context, typeCode string) ([]*domain.DictItem, error) {
	ctx, cancel := context.WithTimeout(ctx, du.contextTimeout)
	defer cancel()

	return du.dictRepository.GetItemsByTypeCode(ctx, typeCode, "active")
}
