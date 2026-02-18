package repository

import (
	"context"
	"crypto/md5"
	"fmt"
	"io"
	"os"
	"path"
	"path/filepath"
	"strings"

	"shadmin/domain"
	"shadmin/pkg"
)

type diskFileRepository struct {
	basePath string
}

// validatePath 验证路径是否在basePath内，防止路径遍历攻击
func (dfr *diskFileRepository) validatePath(targetPath string) error {
	// 获取绝对路径
	absPath, err := filepath.Abs(targetPath)
	if err != nil {
		return fmt.Errorf("invalid path: %v", err)
	}
	absBase, err := filepath.Abs(dfr.basePath)
	if err != nil {
		return fmt.Errorf("invalid base path: %v", err)
	}
	// 确保目标路径在basePath内
	if !strings.HasPrefix(absPath, absBase+string(filepath.Separator)) && absPath != absBase {
		return fmt.Errorf("path traversal detected: access denied")
	}
	return nil
}

func NewDiskFileRepository(basePath string) domain.FileRepository {
	// 确保基础路径存在
	if err := os.MkdirAll(basePath, 0755); err != nil {
		panic(fmt.Sprintf("Failed to create base path %s: %v", basePath, err))
	}
	return &diskFileRepository{
		basePath: basePath,
	}
}

func (dfr *diskFileRepository) Upload(c context.Context, req *domain.UploadRequest) (*domain.UploadResult, error) {
	// 构建文件路径
	bucketPath := filepath.Join(dfr.basePath, req.Bucket)
	if err := os.MkdirAll(bucketPath, 0755); err != nil {
		return nil, fmt.Errorf("failed to create bucket directory: %v", err)
	}

	objectName := req.ObjectName
	if req.Prefix != "" {
		objectName = req.Prefix + "/" + req.ObjectName
		// 确保前缀目录存在
		prefixPath := filepath.Join(bucketPath, req.Prefix)
		if err := os.MkdirAll(prefixPath, 0755); err != nil {
			return nil, fmt.Errorf("failed to create prefix directory: %v", err)
		}
	}

	filePath := filepath.Join(bucketPath, objectName)

	// 验证路径安全性，防止路径遍历攻击
	if err := dfr.validatePath(filePath); err != nil {
		return nil, err
	}

	// 确保目标文件的目录存在
	if err := os.MkdirAll(filepath.Dir(filePath), 0755); err != nil {
		return nil, fmt.Errorf("failed to create file directory: %v", err)
	}

	// 创建目标文件
	file, err := os.Create(filePath)
	if err != nil {
		return nil, fmt.Errorf("failed to create file: %v", err)
	}
	defer file.Close()

	// 计算文件哈希和大小
	hasher := md5.New()
	writer := io.MultiWriter(file, hasher)

	// 复制文件内容
	size, err := io.Copy(writer, req.Reader)
	if err != nil {
		os.Remove(filePath) // 清理失败的文件
		return nil, fmt.Errorf("failed to write file: %v", err)
	}

	// 验证文件大小
	if req.Size > 0 && size != req.Size {
		os.Remove(filePath)
		return nil, fmt.Errorf("file size mismatch: expected %d, got %d", req.Size, size)
	}

	// 生成ETag（MD5哈希）
	etag := fmt.Sprintf("%x", hasher.Sum(nil))

	return &domain.UploadResult{
		Bucket:     req.Bucket,
		Key:        objectName,
		ETag:       etag,
		Size:       size,
		ObjectName: objectName,
	}, nil
}

func (dfr *diskFileRepository) Download(c context.Context, req *domain.DownloadRequest) (io.ReadCloser, error) {
	filePath := filepath.Join(dfr.basePath, req.Bucket, req.ObjectName)

	// 验证路径安全性，防止路径遍历攻击
	if err := dfr.validatePath(filePath); err != nil {
		return nil, err
	}

	file, err := os.Open(filePath)
	if err != nil {
		if os.IsNotExist(err) {
			return nil, fmt.Errorf("file not found: %s", req.ObjectName)
		}
		return nil, fmt.Errorf("failed to open file: %v", err)
	}

	return file, nil
}

func (dfr *diskFileRepository) Delete(c context.Context, bucket, objectName string) error {
	filePath := filepath.Join(dfr.basePath, bucket, objectName)

	// 验证路径安全性，防止路径遍历攻击
	if err := dfr.validatePath(filePath); err != nil {
		return err
	}

	if err := os.Remove(filePath); err != nil {
		if os.IsNotExist(err) {
			return nil // 文件不存在，认为删除成功
		}
		return fmt.Errorf("failed to delete file: %v", err)
	}

	// 尝试删除空的父目录
	dfr.removeEmptyDirs(filepath.Dir(filePath), filepath.Join(dfr.basePath, bucket))

	return nil
}

func (dfr *diskFileRepository) List(c context.Context, bucket, prefix string) ([]domain.FileInfo, error) {
	bucketPath := filepath.Join(dfr.basePath, bucket)

	// 验证路径安全性，防止路径遍历攻击
	if err := dfr.validatePath(bucketPath); err != nil {
		return nil, err
	}

	// 检查bucket目录是否存在
	if _, err := os.Stat(bucketPath); os.IsNotExist(err) {
		return []domain.FileInfo{}, nil
	}

	var files []domain.FileInfo
	searchPath := bucketPath
	if prefix != "" {
		searchPath = filepath.Join(bucketPath, prefix)
		// 验证搜索路径安全性
		if err := dfr.validatePath(searchPath); err != nil {
			return nil, err
		}
	}

	err := filepath.Walk(searchPath, func(fullPath string, info os.FileInfo, err error) error {
		if err != nil {
			return err
		}

		// 跳过基础路径本身
		if fullPath == searchPath {
			return nil
		}

		// 计算相对路径（相对于bucket）
		relPath, err := filepath.Rel(bucketPath, fullPath)
		if err != nil {
			return err
		}

		// 转换路径分隔符为统一的'/'（兼容Windows）
		relPath = filepath.ToSlash(relPath)

		// 如果是目录且不以'/'结尾，添加'/'
		if info.IsDir() && !strings.HasSuffix(relPath, "/") {
			relPath += "/"
		}

		fileType := "folder"
		if !info.IsDir() {
			fileType = pkg.GetFileType(relPath)
		}

		// 计算ETag（对于文件）
		var etag string
		if !info.IsDir() {
			if hash, err := dfr.calculateFileHash(fullPath); err == nil {
				etag = hash
			}
		}

		fileInfo := domain.FileInfo{
			Key:          relPath,
			Name:         info.Name(),
			LastModified: info.ModTime(),
			Size:         info.Size(),
			Type:         fileType,
			Path:         relPath,
			IsFolder:     info.IsDir(),
			ETag:         etag,
		}

		files = append(files, fileInfo)
		return nil
	})

	if err != nil {
		return nil, fmt.Errorf("failed to list files: %v", err)
	}

	return files, nil
}

func (dfr *diskFileRepository) GetObjectStat(c context.Context, bucket, objectName string) (*domain.FileInfo, error) {
	filePath := filepath.Join(dfr.basePath, bucket, objectName)

	// 验证路径安全性，防止路径遍历攻击
	if err := dfr.validatePath(filePath); err != nil {
		return nil, err
	}

	info, err := os.Stat(filePath)
	if err != nil {
		if os.IsNotExist(err) {
			return nil, fmt.Errorf("file not found: %s", objectName)
		}
		return nil, fmt.Errorf("failed to get file info: %v", err)
	}

	fileType := pkg.GetFileType(objectName)
	if info.IsDir() {
		fileType = "folder"
	}

	// 计算ETag
	var etag string
	if !info.IsDir() {
		if hash, err := dfr.calculateFileHash(filePath); err == nil {
			etag = hash
		}
	}

	// 检测内容类型
	contentType := "application/octet-stream"
	if !info.IsDir() {
		if ct := dfr.detectContentType(filePath); ct != "" {
			contentType = ct
		}
	}

	return &domain.FileInfo{
		Key:          objectName,
		Name:         path.Base(objectName),
		LastModified: info.ModTime(),
		Size:         info.Size(),
		Type:         fileType,
		Path:         objectName,
		IsFolder:     info.IsDir(),
		ETag:         etag,
		ContentType:  contentType,
	}, nil
}

// 计算文件的MD5哈希
func (dfr *diskFileRepository) calculateFileHash(filePath string) (string, error) {
	file, err := os.Open(filePath)
	if err != nil {
		return "", err
	}
	defer file.Close()

	hasher := md5.New()
	if _, err := io.Copy(hasher, file); err != nil {
		return "", err
	}

	return fmt.Sprintf("%x", hasher.Sum(nil)), nil
}

// 检测文件内容类型
func (dfr *diskFileRepository) detectContentType(filePath string) string {
	if err := dfr.validatePath(filePath); err != nil {
		return ""
	}
	file, err := os.Open(filePath)
	if err != nil {
		return ""
	}
	defer file.Close()

	// 读取前512字节用于检测内容类型
	buffer := make([]byte, 512)
	n, err := file.Read(buffer)
	if err != nil && err != io.EOF {
		return ""
	}

	return pkg.DetectContentType(buffer[:n], filepath.Ext(filePath))
}

// 删除空目录（递归）
func (dfr *diskFileRepository) removeEmptyDirs(dirPath, stopAt string) {
	if dirPath == stopAt || dirPath == dfr.basePath {
		return
	}

	// 检查目录是否为空
	entries, err := os.ReadDir(dirPath)
	if err != nil || len(entries) > 0 {
		return
	}

	// 删除空目录
	if err := os.Remove(dirPath); err == nil {
		// 递归删除父目录（如果也为空）
		dfr.removeEmptyDirs(filepath.Dir(dirPath), stopAt)
	}
}
