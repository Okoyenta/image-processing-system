-- Create database
CREATE DATABASE image;

-- Connect to the database (run this separately if needed in psql)
-- \c image

-- Create table
CREATE TABLE image_upload (
    id BIGSERIAL PRIMARY KEY,
    original_name VARCHAR(255),
    stored_name VARCHAR(255),
    file_path TEXT,
    processed_path TEXT,
    file_size BIGINT,
    mime_type VARCHAR(100),
    status VARCHAR(50),
    width INTEGER,
    height INTEGER,
    error_message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);



