
## Gen AI Student-Class-Subject-TestMarks Project with Grok


## Overview

- In backend REST api development to view resource we need to create LIST and VIEW API for each resource i.e. 4x2= 8 api's for 4 resource and if we add more then we need develope those also mannually hard coding.

- Inside all those api we are just changing the SQL queries to fetch data from different tables. 

- For any kind of aggregate oparation we need to create those functions mannually.

<b>LMM Comes to picture</b>

- With AI LMM models we can now solve that problem by generating dynamic SQL queries on the fly and excute those to db and get ideal data.

## Example Prompt

 a. Profile Information of Student name John. Expecting class info and its subjects and marks if exist

 b. Class marks report for Student name John

 c. Get all students of Subject Physic

## Goals

Create a prompt API where we will ask question and it will query the DB and return response as json


## Project Architecture
#### Student Schema -

```
first_name
last_name
gender
dob
age
class_id
```

#### Class Schema -

```
name
status

```

#### Subject Schema -

```
name
class_id
description

```

#### Test Marks Schema - 

```
name
class_id
student_id
subject_id
marks
totalMarks
```


### Relationship -

1. Stundent Belongs to one Class
2. One Class has multiple Subjects
3. One Class has multiple Students
4. One Student has marks for every subject


## Getting started

1. Create a python virtual environment ``` python -m venv venv ```

2. activate ```source venv/bin/active ```

3. Install uv ``` pip install uv ```

4. Install jupyter notebook ```uv pip install uv ```

5. Install dependency ```uv pip install -r requirements.txt```

6. Install VSCode Jupyter NoteBook extensions

7. copy ```.env.example``` to ```.env```. Enter Database creds and API KEY for Grok

### Database Setup

1. Creat database name ```gen_ai_db``` in postgres db
2. If you have docker use ```docker/docker-compose.yaml``` file to spine up a postgres instace



