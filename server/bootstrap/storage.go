package bootstrap

import (
	"fmt"
	"strings"

	"shadmin/domain"
	"shadmin/repository"

	"github.com/minio/minio-go/v7"
	"github.com/minio/minio-go/v7/pkg/credentials"
)

type StorageConfig struct {
	MinioClient *minio.Client
	FileStorage domain.FileRepository
}

func InitStorage(env *Env) *StorageConfig {
	config := &StorageConfig{}

	storageType := strings.ToLower(env.StorageType)
	switch storageType {
	case "minio":
		config.initMinioStorage(env)
	case "disk":
		config.initDiskStorage(env)
	default:
		config.initDefaultStorage(env)
	}

	return config
}

func (sc *StorageConfig) initMinioStorage(env *Env) {
	minioClient, err := minio.New(env.S3Address, &minio.Options{
		Creds:  credentials.NewStaticV4(env.S3AccessKey, env.S3SecretKey, env.S3Token),
		Secure: false,
	})
	if err != nil {
		panic("Failed to connect to MinIO: " + err.Error())
	}

	sc.MinioClient = minioClient
	sc.FileStorage = repository.NewFileRepository(minioClient)
	fmt.Println("文件存储: 使用 MinIO 对象存储")
}

func (sc *StorageConfig) initDiskStorage(env *Env) {
	sc.FileStorage = repository.NewDiskFileRepository(env.StorageBasePath)
	fmt.Printf("文件存储: 使用本地磁盘存储 (%s)\n", env.StorageBasePath)
}

func (sc *StorageConfig) initDefaultStorage(env *Env) {
	sc.FileStorage = repository.NewDiskFileRepository(env.StorageBasePath)
	fmt.Printf("文件存储: 使用默认本地磁盘存储 (%s)\n", env.StorageBasePath)
}
