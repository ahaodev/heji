package repository

import (
	"context"
	"heji-server/domain"
	"heji-server/mongo"
	"time"

	"go.mongodb.org/mongo-driver/bson"
	"go.mongodb.org/mongo-driver/bson/primitive"
)

type aiAnalysisRepository struct {
	database   mongo.Database
	collection string
	billColl   string
}

// NewAIAnalysisRepository creates a new AI analysis repository
func NewAIAnalysisRepository(db mongo.Database, collection string) domain.AIAnalysisRepository {
	return &aiAnalysisRepository{
		database:   db,
		collection: collection,
		billColl:   domain.CollBill,
	}
}

func (r *aiAnalysisRepository) SavePrediction(ctx context.Context, prediction *domain.ExpensePrediction) error {
	collection := r.database.Collection(domain.CollAIPrediction)
	
	if prediction.ID.IsZero() {
		prediction.ID = primitive.NewObjectID()
	}
	prediction.CreatedAt = time.Now().Unix()

	_, err := collection.InsertOne(ctx, prediction)
	return err
}

func (r *aiAnalysisRepository) GetLatestPrediction(ctx context.Context, bookID string, period string) (*domain.ExpensePrediction, error) {
	collection := r.database.Collection(domain.CollAIPrediction)
	
	bookObjID, err := primitive.ObjectIDFromHex(bookID)
	if err != nil {
		return nil, err
	}

	filter := bson.M{
		"book_id": bookObjID,
		"period":  period,
	}
	
	var prediction domain.ExpensePrediction
	err = collection.FindOne(ctx, filter).Decode(&prediction)
	if err != nil {
		return nil, err
	}

	return &prediction, nil
}

func (r *aiAnalysisRepository) GetBillsForAnalysis(ctx context.Context, bookID string, startTime, endTime string) ([]domain.Bill, error) {
	collection := r.database.Collection(r.billColl)
	
	bookObjID, err := primitive.ObjectIDFromHex(bookID)
	if err != nil {
		return nil, err
	}

	filter := bson.M{
		"book_id": bookObjID,
	}

	if startTime != "" && endTime != "" {
		filter["time"] = bson.M{
			"$gte": startTime,
			"$lte": endTime,
		}
	}

	cursor, err := collection.Find(ctx, filter)
	if err != nil {
		return nil, err
	}
	defer cursor.Close(ctx)

	var bills []domain.Bill
	if err = cursor.All(ctx, &bills); err != nil {
		return nil, err
	}

	return bills, nil
}

func (r *aiAnalysisRepository) GetCategoryStatistics(ctx context.Context, bookID string, startTime, endTime string) (map[string]domain.CategorySpending, error) {
	collection := r.database.Collection(r.billColl)
	
	bookObjID, err := primitive.ObjectIDFromHex(bookID)
	if err != nil {
		return nil, err
	}

	matchStage := bson.M{
		"book_id": bookObjID,
	}

	if startTime != "" && endTime != "" {
		matchStage["time"] = bson.M{
			"$gte": startTime,
			"$lte": endTime,
		}
	}

	pipeline := []bson.M{
		{"$match": matchStage},
		{
			"$group": bson.M{
				"_id": "$category",
				"total": bson.M{
					"$sum": bson.M{"$toDouble": "$money"},
				},
				"count": bson.M{"$sum": 1},
			},
		},
		{
			"$sort": bson.M{"total": -1},
		},
	}

	cursor, err := collection.Aggregate(ctx, pipeline)
	if err != nil {
		return nil, err
	}
	defer cursor.Close(ctx)

	type AggResult struct {
		ID    string  `bson:"_id"`
		Total float64 `bson:"total"`
		Count int     `bson:"count"`
	}

	var results []AggResult
	if err = cursor.All(ctx, &results); err != nil {
		return nil, err
	}

	// Calculate total for percentages
	var grandTotal float64
	for _, r := range results {
		grandTotal += r.Total
	}

	stats := make(map[string]domain.CategorySpending)
	for _, r := range results {
		percent := 0.0
		if grandTotal > 0 {
			percent = (r.Total / grandTotal) * 100
		}
		
		stats[r.ID] = domain.CategorySpending{
			Category: r.ID,
			Amount:   r.Total,
			Count:    r.Count,
			Percent:  percent,
		}
	}

	return stats, nil
}
