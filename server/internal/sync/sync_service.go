package sync

import (
	"context"
	"encoding/json"
	"fmt"
	"shadmin/domain"
	"shadmin/pkg"
	"time"

	mqtt "github.com/eclipse/paho.mqtt.golang"
)

// 下行通知主题模板：服务端 → 客户端
const (
	TopicNotify = "heji/notify/%s/%s" // heji/notify/{userId}/{entity}
)

// SyncMessage 推送通知消息体（与客户端 SyncMessage 对应）
type SyncMessage struct {
	ID        string `json:"id"`
	Type      string `json:"type"`
	BookID    string `json:"book_id"`
	Content   string `json:"content"`
	Timestamp int64  `json:"timestamp"`
}

// SyncService v2 同步服务 — 纯推送模式
// 不订阅任何 MQTT 主题，仅在 Controller CRUD 后调用 NotifyBookMembers 推送通知
type SyncService struct {
	mqttClient mqtt.Client
	bookRepo   domain.BookRepository
}

// NewSyncService creates a new sync service
func NewSyncService(mqttClient mqtt.Client, bookRepo domain.BookRepository) *SyncService {
	return &SyncService{
		mqttClient: mqttClient,
		bookRepo:   bookRepo,
	}
}

// NotifyBookMembers 向账本所有成员（排除操作者）推送变更通知
func (s *SyncService) NotifyBookMembers(bookID string, senderID string, msgType string, entity string, content string) {
	members, err := s.getBookMembers(bookID)
	if err != nil {
		pkg.Log.Errorf("Failed to get book members for %s: %v", bookID, err)
		return
	}

	msg := &SyncMessage{
		Type:      msgType,
		BookID:    bookID,
		Content:   content,
		Timestamp: time.Now().UnixMilli(),
	}

	for _, uid := range members {
		if uid != senderID {
			s.publishNotify(uid, entity, msg)
		}
	}
}

// publishNotify 发布通知到指定用户
func (s *SyncService) publishNotify(userID, entity string, msg *SyncMessage) {
	topic := fmt.Sprintf(TopicNotify, userID, entity)
	data, err := json.Marshal(msg)
	if err != nil {
		pkg.Log.Errorf("Failed to marshal notify message: %v", err)
		return
	}
	token := s.mqttClient.Publish(topic, 1, false, data)
	token.Wait()
	if token.Error() != nil {
		pkg.Log.Errorf("Failed to publish to %s: %v", topic, token.Error())
		return
	}
	pkg.Log.Debugf("Notify published to %s: type=%s", topic, msg.Type)
}

// getBookMembers 查询账本成员列表
func (s *SyncService) getBookMembers(bookID string) ([]string, error) {
	ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
	defer cancel()
	book, err := s.bookRepo.GetByID(ctx, bookID)
	if err != nil {
		return nil, fmt.Errorf("failed to get book %s: %w", bookID, err)
	}
	return book.Members, nil
}
