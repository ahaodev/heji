package bootstrap

import (
	"errors"
	"fmt"
	"log"
	"os"
	"shadmin/pkg"
	"strings"

	"github.com/spf13/viper"
)

type Env struct {
	AppEnv         string `mapstructure:"APP_ENV"`
	ContextTimeout int    `mapstructure:"CONTEXT_TIMEOUT"`
	Port           string `mapstructure:"PORT"`

	// Database configuration
	DBType string `mapstructure:"DB_TYPE"` // "sqlite" or "postgres"
	DBDSN  string `mapstructure:"DB_DSN"`  // PostgreSQL connection string
	// 令牌配置
	AccessTokenExpiryHour  int    `mapstructure:"ACCESS_TOKEN_EXPIRY_HOUR"`
	RefreshTokenExpiryHour int    `mapstructure:"REFRESH_TOKEN_EXPIRY_HOUR"`
	AccessTokenSecret      string `mapstructure:"ACCESS_TOKEN_SECRET"`
	RefreshTokenSecret     string `mapstructure:"REFRESH_TOKEN_SECRET"`

	// 管理员配置
	AdminUsername string `mapstructure:"ADMIN_USERNAME"`
	AdminPassword string `mapstructure:"ADMIN_PASSWORD"`
	AdminEmail    string `mapstructure:"ADMIN_EMAIL"`

	// 存储配置
	StorageType     string `mapstructure:"STORAGE_TYPE"`      // "disk" 或 "minio"
	StorageBasePath string `mapstructure:"STORAGE_BASE_PATH"` // 本地存储基础路径

	// S3/MinIO 配置
	S3Address   string `mapstructure:"S3_ADDRESS"`
	S3AccessKey string `mapstructure:"S3_ACCESS_KEY"`
	S3SecretKey string `mapstructure:"S3_SECRET_KEY"`
	S3Bucket    string `mapstructure:"S3_BUCKET"`
	S3Token     string `mapstructure:"S3_TOKEN"`

	// MQTT 配置
	MQTTAddress string `mapstructure:"MQTT_ADDRESS"`
	MQTTTCPPort string `mapstructure:"MQTT_TCP_PORT"`
	MQTTWSPort  string `mapstructure:"MQTT_WS_PORT"`
}

func setDefaults() {
	defaults := map[string]interface{}{
		// 基础配置
		"APP_ENV":         "development",
		"CONTEXT_TIMEOUT": 60,
		"PORT":            ":55667",

		// 数据库配置
		"DB_TYPE": "sqlite",
		"DB_DSN":  "",

		// 令牌配置
		"ACCESS_TOKEN_EXPIRY_HOUR":  3,
		"REFRESH_TOKEN_EXPIRY_HOUR": 24,
		"ACCESS_TOKEN_SECRET":       "default-access-secret",
		"REFRESH_TOKEN_SECRET":      "default-refresh-secret",

		// 管理员配置
		"ADMIN_USERNAME": "admin",
		"ADMIN_PASSWORD": "123",
		"ADMIN_EMAIL":    "admin@gmail.com",

		// 存储配置
		"STORAGE_TYPE":      "disk",
		"STORAGE_BASE_PATH": "./uploads",

		// S3/MinIO 配置
		"S3_ADDRESS":    "192.168.8.6:9000",
		"S3_ACCESS_KEY": "IjJm2N3ZZTYjt8C9WkJf",
		"S3_SECRET_KEY": "eIuV0i4ChbLqx54g9rhsZDRTC2LE1xEcnIAnAw1C",
		"S3_BUCKET":     "shadmin",
		"S3_TOKEN":      "",

		// MQTT 配置
		"MQTT_ADDRESS":  "192.168.8.6",
		"MQTT_TCP_PORT": "51883",
		"MQTT_WS_PORT":  "59001",
	}

	for key, value := range defaults {
		viper.SetDefault(key, value)
	}
}

func generateEnvFile() error {
	configSections := []struct {
		title string
		keys  []string
	}{
		{
			title: "# 基础配置",
			keys:  []string{"APP_ENV", "CONTEXT_TIMEOUT", "PORT"},
		},
		{
			title: "# 数据库配置",
			keys:  []string{"DB_TYPE", "DB_DSN"},
		},
		{
			title: "# 令牌配置",
			keys:  []string{"ACCESS_TOKEN_EXPIRY_HOUR", "REFRESH_TOKEN_EXPIRY_HOUR", "ACCESS_TOKEN_SECRET", "REFRESH_TOKEN_SECRET"},
		},
		{
			title: "# 管理员配置",
			keys:  []string{"ADMIN_USERNAME", "ADMIN_PASSWORD", "ADMIN_EMAIL"},
		},
		{
			title: "# 存储配置",
			keys:  []string{"STORAGE_TYPE", "STORAGE_BASE_PATH"},
		},
		{
			title: "# S3/MinIO 配置",
			keys:  []string{"S3_ADDRESS", "S3_ACCESS_KEY", "S3_SECRET_KEY", "S3_BUCKET", "S3_TOKEN"},
		},
		{
			title: "# MQTT 配置",
			keys:  []string{"MQTT_ADDRESS", "MQTT_TCP_PORT", "MQTT_WS_PORT"},
		},
	}

	var content strings.Builder
	for _, section := range configSections {
		content.WriteString(section.title + "\n")
		for _, key := range section.keys {
			content.WriteString(fmt.Sprintf("%s=%v\n", key, viper.Get(key)))
		}
		content.WriteString("\n")
	}

	return os.WriteFile(".env", []byte(content.String()), 0644)
}

func loadConfig() (*Env, error) {
	viper.SetConfigFile(".env")
	if err := viper.ReadInConfig(); err != nil {
		return nil, fmt.Errorf("读取配置文件失败: %w", err)
	}

	var env Env
	if err := viper.Unmarshal(&env); err != nil {
		return nil, fmt.Errorf("解析配置失败: %w", err)
	}

	return &env, nil
}

func (e *Env) validate() error {
	var errs []string

	if e.AppEnv != "development" && e.AppEnv != "production" && e.AppEnv != "testing" {
		errs = append(errs, "APP_ENV必须是development、production或testing之一")
	}

	if e.ContextTimeout <= 0 {
		errs = append(errs, "CONTEXT_TIMEOUT必须大于0")
	}

	if e.Port == "" {
		errs = append(errs, "PORT不能为空")
	}

	if e.DBType != "sqlite" && e.DBType != "postgres" && e.DBType != "mysql" {
		errs = append(errs, "DB_TYPE必须是sqlite、postgres或mysql")
	}

	if (e.DBType == "postgres" || e.DBType == "mysql") && e.DBDSN == "" {
		errs = append(errs, "使用PostgreSQL或MySQL时必须提供DB_DSN")
	}

	if e.AccessTokenExpiryHour <= 0 {
		errs = append(errs, "ACCESS_TOKEN_EXPIRY_HOUR必须大于0")
	}

	if e.RefreshTokenExpiryHour <= 0 {
		errs = append(errs, "REFRESH_TOKEN_EXPIRY_HOUR必须大于0")
	}

	if len(e.AccessTokenSecret) < 16 {
		errs = append(errs, "ACCESS_TOKEN_SECRET长度不能少于16位")
	}

	if len(e.RefreshTokenSecret) < 16 {
		errs = append(errs, "REFRESH_TOKEN_SECRET长度不能少于16位")
	}

	if e.AdminUsername == "" {
		errs = append(errs, "ADMIN_USERNAME不能为空")
	}

	if len(e.AdminPassword) < 3 {
		errs = append(errs, "ADMIN_PASSWORD长度不能少于3位")
	}

	if e.StorageType != "disk" && e.StorageType != "minio" {
		errs = append(errs, "STORAGE_TYPE必须是disk或minio")
	}

	if e.StorageType == "disk" && e.StorageBasePath == "" {
		errs = append(errs, "使用本地存储时必须提供STORAGE_BASE_PATH")
	}

	if e.StorageType == "minio" {
		if e.S3Address == "" {
			errs = append(errs, "使用MinIO时S3_ADDRESS不能为空")
		}
		if e.S3AccessKey == "" {
			errs = append(errs, "使用MinIO时S3_ACCESS_KEY不能为空")
		}
		if e.S3SecretKey == "" {
			errs = append(errs, "使用MinIO时S3_SECRET_KEY不能为空")
		}
		if e.S3Bucket == "" {
			errs = append(errs, "使用MinIO时S3_BUCKET不能为空")
		}
	}

	if len(errs) > 0 {
		return errors.New(strings.Join(errs, "; "))
	}

	return nil
}

func NewEnv() *Env {
	setDefaults()

	if _, err := os.Stat(".env"); err != nil {
		pkg.Log.Println("没有找到 .env 文件，使用默认配置并写入.env")
		if err := generateEnvFile(); err != nil {
			pkg.Log.Error("写入.env文件失败: " + err.Error())
			return &Env{}
		}
	}

	env, err := loadConfig()
	if err != nil {
		pkg.Log.Error(err.Error())
		return &Env{}
	}

	if err := env.validate(); err != nil {
		pkg.Log.Error("配置验证失败: " + err.Error())
	}

	if env.AppEnv == "development" {
		log.Println("The App is running in development env")
	}

	return env
}
