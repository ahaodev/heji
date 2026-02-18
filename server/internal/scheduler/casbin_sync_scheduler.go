package scheduler

import (
	"context"
	"fmt"
	"log"
	"shadmin/internal/casbin"
	"sync"
	"time"
)

// CasbinSyncScheduler casbin同步定时任务调度器
type CasbinSyncScheduler struct {
	syncService *casbin.SyncService
	interval    time.Duration
	running     bool
	stopChan    chan struct{}
	wg          sync.WaitGroup
	mutex       sync.RWMutex
}

// NewCasbinSyncScheduler 创建新的casbin同步调度器
func NewCasbinSyncScheduler(syncService *casbin.SyncService, interval time.Duration) *CasbinSyncScheduler {
	return &CasbinSyncScheduler{
		syncService: syncService,
		interval:    interval,
		stopChan:    make(chan struct{}),
	}
}

// Start 启动定时同步任务
func (s *CasbinSyncScheduler) Start(ctx context.Context) {
	s.mutex.Lock()
	defer s.mutex.Unlock()

	if s.running {
		log.Printf("WARN: Casbin同步调度器已经在运行")
		return
	}

	s.running = true
	s.wg.Add(1)

	go s.run(ctx)

	log.Printf("INFO: Casbin同步调度器已启动，同步间隔: %v", s.interval)
}

// Stop 停止定时同步任务
func (s *CasbinSyncScheduler) Stop() {
	s.mutex.Lock()
	defer s.mutex.Unlock()

	if !s.running {
		return
	}

	close(s.stopChan)
	s.wg.Wait()
	s.running = false

	log.Printf("INFO: Casbin同步调度器已停止")
}

// IsRunning 检查调度器是否在运行
func (s *CasbinSyncScheduler) IsRunning() bool {
	s.mutex.RLock()
	defer s.mutex.RUnlock()
	return s.running
}

// run 执行定时同步任务的主循环
func (s *CasbinSyncScheduler) run(ctx context.Context) {
	defer s.wg.Done()

	ticker := time.NewTicker(s.interval)
	defer ticker.Stop()

	log.Printf("INFO: Casbin同步定时任务开始运行")

	for {
		select {
		case <-s.stopChan:
			log.Printf("INFO: 收到停止信号，退出Casbin同步定时任务")
			return

		case <-ctx.Done():
			log.Printf("INFO: 上下文已取消，退出Casbin同步定时任务")
			return

		case <-ticker.C:
			s.performSync(ctx)
		}
	}
}

// performSync 执行一次同步操作
func (s *CasbinSyncScheduler) performSync(ctx context.Context) {
	startTime := time.Now()

	log.Printf("DEBUG: 开始执行Casbin定时同步")

	// 使用带超时的上下文，避免单次同步时间过长
	syncCtx, cancel := context.WithTimeout(ctx, 30*time.Second)
	defer cancel()

	err := s.syncService.SyncFromDatabase(syncCtx)
	if err != nil {
		log.Printf("ERROR: Casbin定时同步失败: %v", err)
		return
	}

	duration := time.Since(startTime)
	log.Printf("DEBUG: Casbin定时同步完成，耗时: %v", duration)

	// 可选：获取并记录同步统计
	if stats, err := s.syncService.GetSyncStats(syncCtx); err == nil {
		if !stats.IsHealthy() {
			log.Printf("WARN: Casbin同步状态不健康 - Roles: %d, Policies: %d",
				stats.CasbinRoles, stats.CasbinPolicies)
		}
	}
}

// TriggerSync 手动触发一次同步
func (s *CasbinSyncScheduler) TriggerSync(ctx context.Context) error {
	if !s.IsRunning() {
		return fmt.Errorf("调度器未运行")
	}

	log.Printf("INFO: 手动触发Casbin同步")
	s.performSync(ctx)
	return nil
}

// GetStatus 获取调度器状态信息
func (s *CasbinSyncScheduler) GetStatus() SchedulerStatus {
	s.mutex.RLock()
	defer s.mutex.RUnlock()

	return SchedulerStatus{
		Running:  s.running,
		Interval: s.interval,
	}
}

// SchedulerStatus 调度器状态
type SchedulerStatus struct {
	Running  bool          `json:"running"`
	Interval time.Duration `json:"interval"`
}

// SetInterval 更新同步间隔（需要重启调度器才能生效）
func (s *CasbinSyncScheduler) SetInterval(interval time.Duration) {
	s.mutex.Lock()
	defer s.mutex.Unlock()

	s.interval = interval
	log.Printf("INFO: Casbin同步间隔已更新为: %v（重启调度器后生效）", interval)
}
