#!/bin/bash

# AI Analysis API Test Script
# This script demonstrates how to use the AI analysis endpoints

BASE_URL="http://localhost:8080/api/v1"

echo "=========================================="
echo "AI Analysis API Demo"
echo "=========================================="
echo ""

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 1. Test Bill Classification
echo -e "${BLUE}1. Testing Bill Classification${NC}"
echo "Request: Classify a restaurant bill (肯德基)"
echo ""

curl -X POST "${BASE_URL}/ai/classify" \
  -H "Content-Type: application/json" \
  -d '{
    "description": "肯德基 午餐",
    "amount": 45.5,
    "merchant": "肯德基餐厅"
  }' | jq '.'

echo ""
echo "---"
echo ""

# 2. Test Transportation Classification
echo -e "${BLUE}2. Testing Transportation Classification${NC}"
echo "Request: Classify a taxi bill (滴滴)"
echo ""

curl -X POST "${BASE_URL}/ai/classify" \
  -H "Content-Type: application/json" \
  -d '{
    "description": "滴滴出行 打车到机场",
    "amount": 85.5,
    "merchant": "滴滴出行"
  }' | jq '.'

echo ""
echo "---"
echo ""

# 3. Test Salary Income Classification
echo -e "${BLUE}3. Testing Income Classification${NC}"
echo "Request: Classify salary income"
echo ""

curl -X POST "${BASE_URL}/ai/classify" \
  -H "Content-Type: application/json" \
  -d '{
    "description": "公司工资发放",
    "amount": 8000.0,
    "merchant": "公司财务"
  }' | jq '.'

echo ""
echo "---"
echo ""

# 4. Test English Keywords
echo -e "${BLUE}4. Testing English Keywords${NC}"
echo "Request: Classify Starbucks coffee"
echo ""

curl -X POST "${BASE_URL}/ai/classify" \
  -H "Content-Type: application/json" \
  -d '{
    "description": "Starbucks coffee and cake",
    "amount": 65.0,
    "merchant": "Starbucks"
  }' | jq '.'

echo ""
echo "---"
echo ""

# 5. Test Shopping Classification
echo -e "${BLUE}5. Testing Shopping Classification${NC}"
echo "Request: Classify online shopping"
echo ""

curl -X POST "${BASE_URL}/ai/classify" \
  -H "Content-Type: application/json" \
  -d '{
    "description": "淘宝购物 买了几件衣服",
    "amount": 299.0,
    "merchant": "淘宝"
  }' | jq '.'

echo ""
echo "---"
echo ""

# Note: The following endpoints require a valid book_id with existing data
echo -e "${BLUE}Note:${NC} The following endpoints require a valid book_id with bills data"
echo ""

# Example book_id (you need to replace with a real one)
BOOK_ID="507f1f77bcf86cd799439011"
CURRENT_MONTH=$(date +%Y-%m)

# 6. Test Expense Prediction
echo -e "${BLUE}6. Testing Expense Prediction${NC}"
echo "Request: Predict expenses for next month"
echo "Note: Replace BOOK_ID with a real book ID that has historical bills"
echo ""
echo "Command: curl \"${BASE_URL}/ai/predict?book_id=${BOOK_ID}&period=${CURRENT_MONTH}\""
echo ""

# Uncomment to test with real data:
# curl "${BASE_URL}/ai/predict?book_id=${BOOK_ID}&period=${CURRENT_MONTH}" | jq '.'

echo "---"
echo ""

# 7. Test Financial Analysis
echo -e "${BLUE}7. Testing Financial Analysis${NC}"
echo "Request: Analyze financial health"
echo "Note: Replace BOOK_ID and dates with real data"
echo ""

START_DATE=$(date -d "1 month ago" +%Y-%m-01)
END_DATE=$(date +%Y-%m-%d)

echo "Command:"
echo "curl -X POST \"${BASE_URL}/ai/analyze\" \\"
echo "  -H \"Content-Type: application/json\" \\"
echo "  -d '{\"book_id\": \"${BOOK_ID}\", \"start_time\": \"${START_DATE}\", \"end_time\": \"${END_DATE}\"}'"
echo ""

# Uncomment to test with real data:
# curl -X POST "${BASE_URL}/ai/analyze" \
#   -H "Content-Type: application/json" \
#   -d "{
#     \"book_id\": \"${BOOK_ID}\",
#     \"start_time\": \"${START_DATE}\",
#     \"end_time\": \"${END_DATE}\"
#   }" | jq '.'

echo "---"
echo ""

# 8. Test Pattern Detection
echo -e "${BLUE}8. Testing Spending Pattern Detection${NC}"
echo "Request: Detect spending patterns"
echo "Note: Replace BOOK_ID with a real book ID"
echo ""
echo "Command: curl \"${BASE_URL}/ai/patterns?book_id=${BOOK_ID}\""
echo ""

# Uncomment to test with real data:
# curl "${BASE_URL}/ai/patterns?book_id=${BOOK_ID}" | jq '.'

echo "=========================================="
echo -e "${GREEN}Demo Complete!${NC}"
echo "=========================================="
echo ""
echo "To test endpoints 6-8, you need to:"
echo "1. Start the server: go run main.go"
echo "2. Create a book and add some bills"
echo "3. Replace BOOK_ID in this script with your actual book ID"
echo "4. Uncomment the curl commands for endpoints 6-8"
echo ""
echo "For more information, see:"
echo "- API Documentation: server/docs/AI_ANALYSIS_API.md"
echo "- Feature Guide (中文): server/docs/AI_FEATURES_CN.md"
