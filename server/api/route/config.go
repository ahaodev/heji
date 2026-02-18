package route

import (
	"fmt"
	"github.com/gin-gonic/gin"
)

// ServerConfig holds server configuration settings
type ServerConfig struct {
	TrustedProxies     []string
	MaxMultipartMemory int64
	RedirectSlash      bool
}

// DefaultServerConfig returns default server configuration
func DefaultServerConfig() *ServerConfig {
	return &ServerConfig{
		TrustedProxies:     []string{"127.0.0.1"},
		MaxMultipartMemory: 1000 << 20, // 1000 MB
		RedirectSlash:      true,
	}
}

// Apply applies the server configuration to the gin engine
func (c *ServerConfig) Apply(engine *gin.Engine) error {
	if err := engine.SetTrustedProxies(c.TrustedProxies); err != nil {
		return fmt.Errorf("failed to set trusted proxies: %w", err)
	}

	engine.MaxMultipartMemory = c.MaxMultipartMemory
	engine.RedirectTrailingSlash = c.RedirectSlash

	return nil
}
