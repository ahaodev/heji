package domain

import (
	"context"
	"io"
	"time"
)

type FileInfo struct {
	Key          string    `json:"key"`
	Name         string    `json:"name"`
	LastModified time.Time `json:"last_modified"`
	Size         int64     `json:"size"`
	Type         string    `json:"type"`
	Path         string    `json:"path"`
	IsFolder     bool      `json:"is_folder"`
	ETag         string    `json:"etag,omitempty"`
	ContentType  string    `json:"content_type,omitempty"`
}

type UploadRequest struct {
	Bucket      string    `json:"bucket"`
	ObjectName  string    `json:"object_name"`
	Prefix      string    `json:"prefix,omitempty"`
	Reader      io.Reader `json:"-"`
	Size        int64     `json:"size"`
	ContentType string    `json:"content_type"`
}

type UploadResult struct {
	Bucket     string `json:"bucket"`
	Key        string `json:"key"`
	ETag       string `json:"etag"`
	Size       int64  `json:"size"`
	ObjectName string `json:"object_name"`
}

type DownloadRequest struct {
	Bucket     string `json:"bucket"`
	ObjectName string `json:"object_name"`
}

type FileRepository interface {
	Upload(c context.Context, req *UploadRequest) (*UploadResult, error)
	Download(c context.Context, req *DownloadRequest) (io.ReadCloser, error)
	Delete(c context.Context, bucket, objectName string) error
	List(c context.Context, bucket, prefix string) ([]FileInfo, error)
	GetObjectStat(c context.Context, bucket, objectName string) (*FileInfo, error)
}

type FileUsecase interface {
	Upload(c context.Context, req *UploadRequest) (*UploadResult, error)
	Download(c context.Context, req *DownloadRequest) (io.ReadCloser, error)
	Delete(c context.Context, bucket, objectName string) error
	List(c context.Context, bucket, prefix string) ([]FileInfo, error)
	GetObjectStat(c context.Context, bucket, objectName string) (*FileInfo, error)
}
