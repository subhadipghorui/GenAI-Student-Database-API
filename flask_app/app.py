from flask import Flask, request, jsonify
from flask_cors import CORS
from sqlalchemy import create_engine, text
import os
from openai import OpenAI
import json

app = Flask(__name__)

CORS(app)  # Enable CORS for all routes
# Database configuration
DATABASE_URL = os.getenv('DATABASE_URL', 'postgresql://postgres:postgres@localhost:5432/mydb')
engine = create_engine(DATABASE_URL)

# OpenAI configuration
GROK_API_KEY = os.getenv('GROK_API_KEY', 'your_openai_api_key_here')
GROK_BASE_URL= os.getenv('GROK_BASE_URL', 'https://api.x.ai/v1')
LLM_MODEL = os.getenv('LLM_MODEL', '"grok-4-1-fast-reasoning"')

client = OpenAI(
    api_key=GROK_API_KEY,  # Replace with your actual Grok API key
    base_url=GROK_BASE_URL
)

# Database schema for AI context
SCHEMA_INFO = """
    Database Schema:

    1. classes table: id, name, status
    2. students table: id, first_name, last_name, gender, dob, age, class_id
    3. subjects table: id, name, class_id, description
    4. test_marks table: id, name, class_id, student_id, subject_id, marks, total_marks

    Relationships:
    - Student belongs to one Class (students.class_id -> classes.id)
    - Subject belongs to one Class (subjects.class_id -> classes.id)
    - TestMarks belongs to Student, Class, and Subject

    """


CUSTOM_INSTRUCTIONS = f"""
    Exceptions:
    - When querying subjects: Subject names in the database include the class name as a suffix (e.g., "Math Five", "Physics Six"). If the user asks about a subject without specifying the class (e.g., "Math"), use LIKE pattern matching to find all matching subjects (e.g., WHERE name LIKE 'Math%').

    - Always include related data in SELECT: When a query involves relationships (e.g., students and their marks), include relevant fields from all related tables. For example, if asking about "Student with highest marks in Math", return student columns (first_name, last_name, etc.), marks columns (marks, total_marks), and subject columns (subject name) in a single result set using JOINs.

    - Incase of gender for male use 'M' and for female use 'F' in the query.

    """
@app.route('/query', methods=['POST'])
def query():
    """
    Query endpoint to generate and execute SELECT queries from natural language
    Expected JSON: {"prompt": "Get all students in class Five"}
    """
    try:
        data = request.get_json()
        user_prompt = data.get('prompt', '')
        
        if not user_prompt:
            return jsonify({'error': 'Prompt is required'}), 400
        
        # Generate SQL query using OpenAI
        prompt = f"""
        {SCHEMA_INFO}
        {CUSTOM_INSTRUCTIONS}
        User Question: {user_prompt}

        Generate a PostgreSQL SELECT query to answer this question. Return ONLY the SQL query as a JSON object with a 'query' key.
        """
                
        response = client.chat.completions.create(
            model=LLM_MODEL,
            messages=[
                {"role": "system", "content": "You are a SQL expert. Generate only valid PostgreSQL SELECT queries. Return JSON format: {'query': 'SELECT ...'}."},
                {"role": "user", "content": prompt}
            ],
            response_format={"type": "json_object"}
        )
        
        response_data = json.loads(response.choices[0].message.content)
        sql_query = response_data.get('query', '') or response_data.get('sql', '')
        
        if not sql_query:
            return jsonify({'error': 'Failed to generate SQL query'}), 500
        
        print(f"Generated SQL Query: {sql_query}")
        
        # Validate that it's a SELECT query
        if not sql_query.strip().upper().startswith('SELECT'):
            return jsonify({'error': 'Only SELECT queries are allowed'}), 400
        
        # Execute the query
        with engine.connect() as conn:
            result = conn.execute(text(sql_query))
            rows = result.fetchall()
            columns = result.keys()
            
            # Convert to list of dictionaries
            result_data = [dict(zip(columns, row)) for row in rows]
            
            return jsonify({
                'status': 'success',
                'prompt': user_prompt,
                'generated_sql': sql_query,
                'data': result_data,
                'count': len(result_data)
            }), 200
            
    except Exception as e:
        return jsonify({'error': str(e)}), 500


@app.route('/updateDB', methods=['POST'])
def update_db():
    """
    Update endpoint to generate and execute UPDATE/INSERT/DELETE queries from natural language
    Expected JSON: {"prompt": "Update John's test marks to 88 in Math subject"}
    """
    try:
        data = request.get_json()
        user_prompt = data.get('prompt', '')
        
        if not user_prompt:
            return jsonify({'error': 'Prompt is required'}), 400
        
        # Generate SQL query using OpenAI
        prompt = f"""
        {SCHEMA_INFO}
        {CUSTOM_INSTRUCTIONS}

        User Question: {user_prompt}

        Generate a PostgreSQL UPDATE, INSERT, or DELETE query to answer this question. Return ONLY the SQL query as a JSON object with a 'query' key.
        """
                
        response = client.chat.completions.create(
            model=LLM_MODEL,
            messages=[
                {"role": "system", "content": "You are a SQL expert. Generate only valid PostgreSQL UPDATE, INSERT, or DELETE queries. Return JSON format: {'query': 'UPDATE ...'}."},
                {"role": "user", "content": prompt}
            ],
            response_format={"type": "json_object"}
        )
        
        response_data = json.loads(response.choices[0].message.content)
        sql_query = response_data.get('query', '') or response_data.get('sql', '')
        
        if not sql_query:
            return jsonify({'error': 'Failed to generate SQL query'}), 500
        
        print(f"Generated SQL Query: {sql_query}")
        
        # Validate that it's NOT a SELECT query
        if sql_query.strip().upper().startswith('SELECT'):
            return jsonify({'error': 'SELECT queries should use /query endpoint'}), 400
        
        # Execute UPDATE/INSERT/DELETE query
        with engine.connect() as conn:
            result = conn.execute(text(sql_query))
            conn.commit()
            
            return jsonify({
                'status': 'success',
                'prompt': user_prompt,
                'generated_sql': sql_query,
                'rows_affected': result.rowcount
            }), 200
            
    except Exception as e:
        return jsonify({'error': str(e)}), 500

@app.route('/health', methods=['GET'])
def health():
    """Health check endpoint"""
    return jsonify({'status': 'healthy'}), 200


@app.route('/', methods=['GET'])
def index():
    """Health check endpoint"""
    return jsonify({'status': 'Flask app is running on port 5000'}), 200


if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0', port=5000)
