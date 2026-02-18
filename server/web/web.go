package web

import (
	"embed"
	"strings"

	"github.com/gin-contrib/static"
	"github.com/gin-gonic/gin"
)

//go:embed dist/*
var Static embed.FS

func Register(r *gin.Engine) {
	fs, err := static.EmbedFolder(Static, "dist")
	if err != nil {
		panic("静态文件映射错误,请检查前端是否构建" + err.Error())
	}
	r.Use(static.Serve("/", fs))

	// SPA fallback - 处理前端路由，所有非API路由都返回index.html
	r.NoRoute(func(c *gin.Context) {
		// 如果请求路径以/api开头，返回404（API路由）
		// 如果请求路径以/share开头，返回404（分享路由）
		// 如果请求路径以/swagger开头，返回404（文档路由）
		path := c.Request.URL.Path
		if strings.HasPrefix(path, "/api") ||
			strings.HasPrefix(path, "/share/") ||
			strings.HasPrefix(path, "/swagger") ||
			strings.HasPrefix(path, "/client-access") {
			c.JSON(404, gin.H{"error": "API endpoint not found"})
			return
		}

		// 其他路径返回index.html，让前端路由处理
		c.Header("Content-Type", "text/html")
		indexHTML, err := Static.ReadFile("dist/index.html")
		if err != nil {
			c.JSON(500, gin.H{"error": "Failed to load SPA"})
			return
		}
		c.Data(200, "text/html; charset=utf-8", indexHTML)
	})
}
