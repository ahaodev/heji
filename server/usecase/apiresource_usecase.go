package usecase

import (
	"context"
	"shadmin/domain"
	"time"

	"github.com/gin-gonic/gin"
)

type apiResourceUsecase struct {
	apiResourceRepository domain.ApiResourceRepository
	ginEngine             *gin.Engine
	contextTimeout        time.Duration
}

func NewApiResourceUsecase(apiResourceRepository domain.ApiResourceRepository, ginEngine *gin.Engine, timeout time.Duration) domain.ApiResourceUseCase {
	return &apiResourceUsecase{
		apiResourceRepository: apiResourceRepository,
		ginEngine:             ginEngine,
		contextTimeout:        timeout,
	}
}

func (aru *apiResourceUsecase) FetchPaged(c context.Context, params domain.ApiResourceQueryParams) (*domain.ApiResourcePagedResult, error) {
	ctx, cancel := context.WithTimeout(c, aru.contextTimeout)
	defer cancel()

	return aru.apiResourceRepository.FetchPaged(ctx, params)
}
