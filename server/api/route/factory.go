package route

import (
	"shadmin/api/controller"
	bootstrap "shadmin/bootstrap"
	"shadmin/internal"
	"shadmin/internal/casbin"
	"shadmin/internal/sync"
	"shadmin/internal/tokenservice"
	"shadmin/repository"
	"shadmin/usecase"
	"time"

	"shadmin/ent"

	"github.com/gin-gonic/gin"
)

// ControllerFactory creates and manages controller instances
type ControllerFactory struct {
	app         *bootstrap.Application
	timeout     time.Duration
	db          *ent.Client
	syncService *sync.SyncService
}

// NewControllerFactory creates a new controller factory
func NewControllerFactory(app *bootstrap.Application, timeout time.Duration, db *ent.Client, syncService *sync.SyncService) *ControllerFactory {
	return &ControllerFactory{
		app:         app,
		timeout:     timeout,
		db:          db,
		syncService: syncService,
	}
}

// CreateAuthController creates an authentication controller
func (f *ControllerFactory) CreateAuthController(casManager casbin.Manager) *controller.AuthController {
	ur := repository.NewUserRepository(f.db, casManager)
	rr := repository.NewRoleRepository(f.db)
	ts := tokenservice.NewTokenService()

	// 创建LoginLog相关的repository和usecase
	loginLogRepository := repository.NewLoginLogRepository(f.db)
	loginLogUseCase := usecase.NewLoginLogUsecase(loginLogRepository, f.timeout)

	return &controller.AuthController{
		LoginUsecase:    usecase.NewLoginUsecase(ur, f.timeout),
		RegisterUsecase: usecase.NewRegisterUsecase(ur, f.timeout),
		LoginLogUsecase: loginLogUseCase,
		RoleRepository:  rr,
		Env:             f.app.Env,
		SecurityManager: internal.NewLoginSecurityManager(),
		TokenService:    ts,
		CasManager:      casManager,
	}
}

// CreateProfileController creates a profile controller
func (f *ControllerFactory) CreateProfileController() *controller.ProfileController {
	pr := repository.NewProfileRepository(f.db)

	return &controller.ProfileController{
		ProfileUsecase: usecase.NewProfileUsecase(pr, f.timeout),
		Env:            f.app.Env,
	}
}

// CreateResourceController creates a resource controller
func (f *ControllerFactory) CreateResourceController() *controller.ResourceController {
	menuRepository := repository.NewMenuRepository(f.db)
	roleRepository := repository.NewRoleRepository(f.db)
	userRepository := repository.NewUserRepository(f.db, f.app.CasManager)

	return &controller.ResourceController{
		MenuRepository: menuRepository,
		RoleRepository: roleRepository,
		UserRepository: userRepository,
		Env:            f.app.Env,
	}
}

// CreateUserController creates a user controller
func (f *ControllerFactory) CreateUserController() *controller.UserController {
	ur := repository.NewUserRepository(f.db, f.app.CasManager)
	rr := repository.NewRoleRepository(f.db)

	return &controller.UserController{
		UserUsecase: usecase.NewUserUsecase(f.db, ur, rr, f.timeout),
		Env:         f.app.Env,
	}
}

// CreateRoleController creates a role controller
func (f *ControllerFactory) CreateRoleController() *controller.RoleController {
	roleRepository := repository.NewRoleRepository(f.db)
	userRepository := repository.NewUserRepository(f.db, f.app.CasManager)
	menuRepository := repository.NewMenuRepository(f.db)
	roleUseCase := usecase.NewRoleUsecase(f.db, roleRepository, f.timeout)

	return &controller.RoleController{
		CasManager:     f.app.CasManager,
		Env:            f.app.Env,
		RoleUseCase:    roleUseCase,
		UserRepository: userRepository,
		RoleRepository: roleRepository,
		MenuRepository: menuRepository,
	}
}

// CreateMenuController creates a menu controller
func (f *ControllerFactory) CreateMenuController() *controller.MenuController {
	menuRepository := repository.NewMenuRepository(f.db)
	menuUsecase := usecase.NewMenuUsecase(f.db, menuRepository, f.timeout)

	return &controller.MenuController{
		MenuUseCase: menuUsecase,
		Env:         f.app.Env,
	}
}

// CreateApiResourceController creates an API resource controller
func (f *ControllerFactory) CreateApiResourceController(engine *gin.Engine) *controller.ApiResourceController {
	apiResourceRepository := repository.NewApiResourceRepository(f.db)
	apiResourceUseCase := usecase.NewApiResourceUsecase(apiResourceRepository, engine, f.timeout)

	return &controller.ApiResourceController{
		ApiResourceUseCase: apiResourceUseCase,
	}
}

// CreateLoginLogController creates a login log controller
func (f *ControllerFactory) CreateLoginLogController() *controller.LoginLogController {
	loginLogRepository := repository.NewLoginLogRepository(f.db)
	loginLogUseCase := usecase.NewLoginLogUsecase(loginLogRepository, f.timeout)

	return &controller.LoginLogController{
		LoginLogUsecase: loginLogUseCase,
		Env:             f.app.Env,
	}
}

// CreateDictController creates a dictionary controller
func (f *ControllerFactory) CreateDictController() *controller.DictController {
	dictRepository := repository.NewDictRepository(f.db)
	dictUseCase := usecase.NewDictUsecase(f.db, dictRepository, f.timeout)

	return &controller.DictController{
		DictUseCase: dictUseCase,
	}
}

// CreateBookController creates a book controller
func (f *ControllerFactory) CreateBookController() *controller.BookController {
	bookRepo := repository.NewBookRepository(f.db)
	return &controller.BookController{
		BookUsecase: usecase.NewBookUsecase(bookRepo, f.timeout),
		SyncService: f.syncService,
	}
}

// CreateBillController creates a bill controller
func (f *ControllerFactory) CreateBillController() *controller.BillController {
	billRepo := repository.NewBillRepository(f.db)
	userRepo := repository.NewUserRepository(f.db, f.app.CasManager)
	return &controller.BillController{
		BillUsecase:    usecase.NewBillUsecase(billRepo, f.timeout),
		UserRepository: userRepo,
		SyncService:    f.syncService,
	}
}

// CreateCategoryController creates a category controller
func (f *ControllerFactory) CreateCategoryController() *controller.CategoryController {
	categoryRepo := repository.NewCategoryRepository(f.db)
	return &controller.CategoryController{
		CategoryUsecase: usecase.NewCategoryUsecase(categoryRepo, f.timeout),
	}
}

// CreateImageController creates an image controller
func (f *ControllerFactory) CreateImageController() *controller.ImageController {
	imageRepo := repository.NewBillImageRepository(f.db)
	return &controller.ImageController{
		ImageUsecase: usecase.NewBillImageUsecase(imageRepo, f.app.FileStorage, f.timeout),
		FileRepo:     f.app.FileStorage,
	}
}

// CreateSyncController creates a sync controller
func (f *ControllerFactory) CreateSyncController() *controller.SyncController {
	syncRepo := repository.NewSyncRepository(f.db)
	return &controller.SyncController{
		SyncRepo: syncRepo,
	}
}
