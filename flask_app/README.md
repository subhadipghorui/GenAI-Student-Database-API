# Flask API Project

Simple Flask API with query and update endpoints for database operations.

## Setup

1. Install dependencies:
```bash
pip install -r requirements.txt
```

2. Set up environment variables (optional):
```bash
cp .env.example .env
# Edit .env with your database credentials
```

3. Run the application:
```bash
python app.py
```

The API will be available at `http://localhost:5000`

## API Endpoints

### 1. Query Endpoint (SELECT)
**POST** `/query`

Execute SELECT queries to retrieve data.

**Request:**
```json
{
  "sql": "SELECT * FROM students"
}
```

**Response:**
```json
{
  "status": "success",
  "data": [...],
  "count": 5
}
```

### 2. Update Endpoint (INSERT/UPDATE/DELETE)
**POST** `/update`

Execute modification queries (INSERT, UPDATE, DELETE).

**Request:**
```json
{
  "sql": "UPDATE students SET age = 20 WHERE id = 1"
}
```

**Response:**
```json
{
  "status": "success",
  "rows_affected": 1
}
```

### 3. Health Check
**GET** `/health`

Check if the API is running.

**Response:**
```json
{
  "status": "healthy"
}
```

## Testing with curl

### Query:
```bash
curl -X POST http://localhost:5000/query \
  -H "Content-Type: application/json" \
  -d '{"prompt": "Get all students in class Five"}'
```

### Update:
```bash
curl -X POST http://localhost:5000/update \
  -H "Content-Type: application/json" \
  -d '{"prompt": "Update John's marks in Math to 55"}'
```
