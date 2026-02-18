package repository

import (
	"context"
	"shadmin/domain"
	"shadmin/ent"
	"shadmin/ent/user"
)

type entProfileRepository struct {
	client *ent.Client
}

func NewProfileRepository(client *ent.Client) domain.ProfileRepository {
	return &entProfileRepository{
		client: client,
	}
}

func (pr *entProfileRepository) GetByID(c context.Context, id string) (*domain.Profile, error) {
	u, err := pr.client.User.
		Query().
		Where(user.ID(id)).
		First(c)

	if err != nil {
		return nil, err
	}

	// Convert ent.User to domain.Profile
	profile := &domain.Profile{
		ID:        u.ID,
		Username:  u.Username,
		Email:     u.Email,
		Phone:     u.Phone,
		IsAdmin:   u.IsAdmin,
		Status:    string(u.Status),
		CreatedAt: u.CreatedAt,
		UpdatedAt: u.UpdatedAt,
	}

	return profile, nil
}

func (pr *entProfileRepository) UpdateProfile(c context.Context, userID string, updateData domain.ProfileUpdate) error {
	updateQuery := pr.client.User.
		UpdateOneID(userID)

	if updateData.Name != "" {
		updateQuery = updateQuery.SetUsername(updateData.Name)
	}
	if updateData.Avatar != "" {
		updateQuery = updateQuery.SetAvatar(updateData.Avatar)
	}

	_, err := updateQuery.Save(c)
	return err
}

func (pr *entProfileRepository) UpdatePassword(c context.Context, userID, hashedPassword string) error {
	_, err := pr.client.User.
		UpdateOneID(userID).
		SetPassword(hashedPassword).
		Save(c)
	return err
}

func (pr *entProfileRepository) GetPasswordHash(c context.Context, userID string) (string, error) {
	u, err := pr.client.User.
		Query().
		Where(user.ID(userID)).
		Select(user.FieldPassword).
		First(c)
	if err != nil {
		return "", err
	}
	return u.Password, nil
}
