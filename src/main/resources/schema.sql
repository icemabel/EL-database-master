-- H2 Database Schema for Chemical Database
-- Drop tables if they exist (for clean setup)
DROP TABLE IF EXISTS chemicals;
DROP TABLE IF EXISTS user_profile;
DROP TABLE IF EXISTS study_list;

-- Create user_profile table
CREATE TABLE user_profile (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    email VARCHAR(255) UNIQUE,
    phone_number VARCHAR(20),
    position VARCHAR(100),
    duration INTEGER DEFAULT 0,
    role VARCHAR(20) DEFAULT 'USER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create chemicals table with QR code fields
CREATE TABLE chemicals (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) UNIQUE NOT NULL,
    cas_no VARCHAR(50),
    lot_no VARCHAR(50),
    producer VARCHAR(255),
    storage VARCHAR(255) NOT NULL,
    quantity INTEGER,
    toxic_state BOOLEAN,
    responsible VARCHAR(255),
    order_date DATE,
    weight VARCHAR(50),
    qr_code VARCHAR(36) UNIQUE,
    qr_code_image LONGVARBINARY,
    qr_code_generated_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255)
);

CREATE TABLE study_list (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    study_code VARCHAR(100) UNIQUE NOT NULL,
    document_codes VARCHAR(500),
    material_type VARCHAR(100),
    study_level VARCHAR(10),
    risk_level VARCHAR(50),
    info VARCHAR(1000),
    number_of_samples VARCHAR(200),
    object_of_study VARCHAR(500),
    responsible_person VARCHAR(100),
    status VARCHAR(50),
    qr_code VARCHAR(36) UNIQUE,
    qr_code_image LONGVARBINARY,
    qr_code_generated_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255)
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_study_list_study_code ON study_list(study_code);
CREATE INDEX IF NOT EXISTS idx_study_list_material_type ON study_list(material_type);
CREATE INDEX IF NOT EXISTS idx_study_list_responsible_person ON study_list(responsible_person);
CREATE INDEX IF NOT EXISTS idx_study_list_status ON study_list(status);
CREATE INDEX IF NOT EXISTS idx_study_list_qr_code ON study_list(qr_code);
CREATE INDEX IF NOT EXISTS idx_study_list_study_level ON study_list(study_level);
CREATE INDEX IF NOT EXISTS idx_study_list_risk_level ON study_list(risk_level);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_chemicals_name ON chemicals(name);
CREATE INDEX IF NOT EXISTS idx_chemicals_cas_no ON chemicals(cas_no);
CREATE INDEX IF NOT EXISTS idx_chemicals_storage ON chemicals(storage);
CREATE INDEX IF NOT EXISTS idx_chemicals_responsible ON chemicals(responsible);
CREATE INDEX IF NOT EXISTS idx_chemicals_toxic_state ON chemicals(toxic_state);
CREATE INDEX IF NOT EXISTS idx_chemicals_qr_code ON chemicals(qr_code);
CREATE INDEX IF NOT EXISTS idx_chemicals_order_date ON chemicals(order_date);
CREATE INDEX IF NOT EXISTS idx_user_profile_username ON user_profile(username);
CREATE INDEX IF NOT EXISTS idx_user_profile_email ON user_profile(email);
CREATE INDEX IF NOT EXISTS idx_user_profile_position ON user_profile(position);

