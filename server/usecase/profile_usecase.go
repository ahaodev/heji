package usecase

import (
	"context"
	"shadmin/domain"
	"time"

	"golang.org/x/crypto/bcrypt"
)

type profileUsecase struct {
	profileRepo domain.ProfileRepository
	timeout     time.Duration
}

func NewProfileUsecase(profileRepo domain.ProfileRepository, timeout time.Duration) domain.ProfileUsecase {
	return &profileUsecase{
		profileRepo: profileRepo,
		timeout:     timeout,
	}
}

func (pu *profileUsecase) GetProfile(c context.Context, userID string) (*domain.Profile, error) {
	ctx, cancel := context.WithTimeout(c, pu.timeout)
	defer cancel()

	profile, err := pu.profileRepo.GetByID(ctx, userID)
	if err != nil {
		return nil, err
	}

	return profile, nil
}

func (pu *profileUsecase) UpdateProfile(c context.Context, userID string, updateData domain.ProfileUpdate) error {
	ctx, cancel := context.WithTimeout(c, pu.timeout)
	defer cancel()

	return pu.profileRepo.UpdateProfile(ctx, userID, updateData)
}

func (pu *profileUsecase) UpdatePassword(c context.Context, userID string, passwordUpdate domain.PasswordUpdate) error {
	ctx, cancel := context.WithTimeout(c, pu.timeout)
	defer cancel()

	// Get current password hash
	currentHash, err := pu.profileRepo.GetPasswordHash(ctx, userID)
	if err != nil {
		return err
	}

	// Verify current password
	if err := bcrypt.CompareHashAndPassword([]byte(currentHash), []byte(passwordUpdate.CurrentPassword)); err != nil {
		return domain.ErrInvalidPassword
	}

	// Hash new password
	hashedPassword, err := bcrypt.GenerateFromPassword([]byte(passwordUpdate.NewPassword), bcrypt.DefaultCost)
	if err != nil {
		return err
	}

	// Update password
	return pu.profileRepo.UpdatePassword(ctx, userID, string(hashedPassword))
}
