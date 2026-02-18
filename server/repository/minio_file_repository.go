package repository

import (
	"context"
	"io"
	"path"
	"strings"

	"shadmin/domain"
	"shadmin/pkg"

	"github.com/minio/minio-go/v7"
)

type minioFileRepository struct {
	client *minio.Client
}

func NewFileRepository(client *minio.Client) domain.FileRepository {
	return &minioFileRepository{
		client: client,
	}
}

func (fr *minioFileRepository) Upload(c context.Context, req *domain.UploadRequest) (*domain.UploadResult, error) {
	// 构建对象名称
	objectName := req.ObjectName
	if req.Prefix != "" {
		objectName = req.Prefix + "/" + req.ObjectName
	}
	// 检查存储桶是否存在,如果不存在创建桶
	bucketExists, err := fr.client.BucketExists(c, req.Bucket)
	if err != nil {
		return nil, err
	}
	if !bucketExists {
		err = fr.client.MakeBucket(c, req.Bucket, minio.MakeBucketOptions{})
		if err != nil {
			return nil, err
		}
	}
	// 上传对象到 MinIO
	info, err := fr.client.PutObject(c, req.Bucket, objectName, req.Reader, req.Size, minio.PutObjectOptions{
		ContentType: req.ContentType,
	})
	if err != nil {
		return nil, err
	}

	return &domain.UploadResult{
		Bucket:     info.Bucket,
		Key:        info.Key,
		ETag:       info.ETag,
		Size:       info.Size,
		ObjectName: objectName,
	}, nil
}

func (fr *minioFileRepository) Download(c context.Context, req *domain.DownloadRequest) (io.ReadCloser, error) {
	options := minio.GetObjectOptions{}
	object, err := fr.client.GetObject(c, req.Bucket, req.ObjectName, options)
	if err != nil {
		return nil, err
	}
	return object, nil
}

func (fr *minioFileRepository) Delete(c context.Context, bucket, objectName string) error {
	return fr.client.RemoveObject(c, bucket, objectName, minio.RemoveObjectOptions{})
}

func (fr *minioFileRepository) List(c context.Context, bucket, prefix string) ([]domain.FileInfo, error) {
	options := minio.ListObjectsOptions{Prefix: prefix}
	objectsCh := fr.client.ListObjects(c, bucket, options)

	var files []domain.FileInfo
	for object := range objectsCh {
		if object.Err != nil {
			return nil, object.Err
		}

		// 如果对象大小为 0 且关键字以 "/" 结尾，则认为是伪目录，直接跳过
		if object.Size == 0 && strings.HasSuffix(object.Key, "/") {
			if prefix == object.Key {
				continue
			}
		}

		isFolder := object.Size == 0 && strings.HasSuffix(object.Key, "/")
		var fileType string
		if isFolder {
			fileType = "folder"
		} else {
			fileType = pkg.GetFileType(object.Key)
		}

		fileInfo := domain.FileInfo{
			Key:          object.Key,
			Name:         path.Base(object.Key),
			LastModified: object.LastModified,
			Size:         object.Size,
			Type:         fileType,
			Path:         object.Key,
			IsFolder:     isFolder,
			ETag:         object.ETag,
		}
		files = append(files, fileInfo)
	}

	return files, nil
}

func (fr *minioFileRepository) GetObjectStat(c context.Context, bucket, objectName string) (*domain.FileInfo, error) {
	stat, err := fr.client.StatObject(c, bucket, objectName, minio.StatObjectOptions{})
	if err != nil {
		return nil, err
	}

	return &domain.FileInfo{
		Key:          stat.Key,
		Name:         path.Base(stat.Key),
		LastModified: stat.LastModified,
		Size:         stat.Size,
		Type:         pkg.GetFileType(stat.Key),
		Path:         stat.Key,
		IsFolder:     false,
		ETag:         stat.ETag,
		ContentType:  stat.ContentType,
	}, nil
}
