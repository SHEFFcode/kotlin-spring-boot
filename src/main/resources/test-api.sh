#!/bin/bash

BASE_URL="http://localhost:8080/api/todos"

echo "🚀 Testing Todo API..."
echo "================================"

echo "1. Getting all todos:"
curl -s -X GET "$BASE_URL" | jq .

echo -e "\n2. Creating a new todo:"
curl -s -X POST "$BASE_URL" \
  -H "Content-Type: application/json" \
  -d '{"title": "Test Todo", "description": "Created via script"}' | jq .

echo -e "\n3. Getting pending todos:"
curl -s -X GET "$BASE_URL/pending" | jq .

echo -e "\n4. Getting completed todos:"
curl -s -X GET "$BASE_URL/completed" | jq .

echo -e "\n5. Toggling todo completion (ID 1):"
curl -s -X PATCH "$BASE_URL/1/toggle" | jq .

echo -e "\n✅ API testing complete!"