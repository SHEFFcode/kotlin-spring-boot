-- PostgreSQL script to create todos database and seed data
-- Run this directly in PostgreSQL, not through Spring Boot

-- Create todos database (run this first as a separate command)
-- CREATE DATABASE todos;

-- Connect to the todos database before running the rest

-- Create todos table
CREATE TABLE IF NOT EXISTS todos (
 id          BIGSERIAL PRIMARY KEY,
 title       VARCHAR(255) NOT NULL,
 description TEXT,
 completed   BOOLEAN DEFAULT FALSE NOT NULL,
 created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
 updated_at  TIMESTAMP
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_todos_completed ON todos(completed);
CREATE INDEX IF NOT EXISTS idx_todos_created_at ON todos(created_at);

-- Insert sample todos (only if table is empty)
INSERT INTO todos (title, description, completed, created_at, updated_at)
SELECT title, description, completed, created_at, updated_at FROM (
  VALUES
      ('Setup Development Environment',
       'Install Java, Kotlin, and IntelliJ IDEA. Configure project structure.',
       true,
       CURRENT_TIMESTAMP - INTERVAL '6 days',
       CURRENT_TIMESTAMP - INTERVAL '5 days'),

      ('Learn Kotlin Basics',
       'Study Kotlin syntax, data classes, and null safety features.',
       true,
       CURRENT_TIMESTAMP - INTERVAL '5 days',
       CURRENT_TIMESTAMP - INTERVAL '4 days'),

      ('Setup Spring Boot Project',
       'Initialize Spring Boot project with required dependencies.',
       true,
       CURRENT_TIMESTAMP - INTERVAL '4 days',
       CURRENT_TIMESTAMP - INTERVAL '3 days'),

      ('Implement REST API Endpoints',
       'Create CRUD operations for todo management with proper HTTP status codes.',
       false,
       CURRENT_TIMESTAMP - INTERVAL '3 days',
       NULL),

      ('Add Input Validation',
       'Implement request validation using Jakarta Bean Validation annotations.',
       false,
       CURRENT_TIMESTAMP - INTERVAL '2 days',
       NULL),

      ('Write Unit Tests',
       'Create comprehensive test suite covering service and controller layers.',
       false,
       CURRENT_TIMESTAMP - INTERVAL '1 day',
       NULL),

      ('Add Exception Handling',
       'Implement global exception handler and custom error responses.',
       false,
       CURRENT_TIMESTAMP,
       NULL),

      ('Setup Database Migration',
       'Configure Flyway or Liquibase for database schema versioning.',
       false,
       CURRENT_TIMESTAMP + INTERVAL '2 hours',
       NULL),

      ('Add API Documentation',
       'Integrate Swagger/OpenAPI for automatic API documentation.',
       false,
       CURRENT_TIMESTAMP + INTERVAL '4 hours',
       NULL),

      ('Deploy to Production',
       'Deploy application to cloud platform with monitoring and alerting.',
       false,
       CURRENT_TIMESTAMP + INTERVAL '6 hours',
       NULL)
) AS sample_data(title, description, completed, created_at, updated_at)
WHERE NOT EXISTS (SELECT 1 FROM todos);

-- Verify the data was inserted
SELECT COUNT(*) as total_todos FROM todos;
SELECT COUNT(*) as completed_todos FROM todos WHERE completed = true;
SELECT COUNT(*) as pending_todos FROM todos WHERE completed = false;