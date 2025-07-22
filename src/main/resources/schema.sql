-- Create database

-- Use the database

-- Create user_profile table
CREATE TABLE user_profile (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    email VARCHAR(255) UNIQUE,
    phone_number VARCHAR(20),
    position VARCHAR(100),
    duration INTEGER
);

-- Create chemicals table with QR code fields
CREATE TABLE chemicals (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) UNIQUE NOT NULL,
    cas_no VARCHAR(50),
    lot_no VARCHAR(50),
    producer VARCHAR(255),
    storage VARCHAR(255),
    toxic_state BOOLEAN,
    responsible VARCHAR(255),
    order_date DATE,
    weight VARCHAR(50),
    qr_code VARCHAR(36) UNIQUE,
    qr_code_image BYTEA,
    qr_code_generated_at TIMESTAMP,
    created_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_at TIMESTAMP,
    updated_by VARCHAR(255)
);

-- Create indexes for better performance
CREATE INDEX idx_chemicals_name ON chemicals(name);
CREATE INDEX idx_chemicals_cas_no ON chemicals(cas_no);
CREATE INDEX idx_chemicals_storage ON chemicals(storage);
CREATE INDEX idx_chemicals_responsible ON chemicals(responsible);
CREATE INDEX idx_chemicals_toxic_state ON chemicals(toxic_state);
CREATE INDEX idx_chemicals_qr_code ON chemicals(qr_code);
CREATE INDEX idx_user_profile_username ON user_profile(username);

-- Insert sample data for testing
INSERT INTO user_profile (username, password, first_name, last_name, email, phone_number, position, duration)
VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iYqiSfFGjDyOspeI7lYz1dU3/2Z6', 'Admin', 'User', 'admin@example.com', '123-456-7890', 'Administrator', 0);
-- Password is 'password' encoded with BCrypt

-- Sample chemicals data
INSERT INTO chemicals (name, cas_no, lot_no, producer, storage, toxic_state, responsible, order_date, weight)
VALUES
('Sodium Chloride', '7647-14-5', 'LOT001', 'ChemCorp', 'Room A-101', false, 'John Doe', '2024-01-15', '500g'),
('Hydrochloric Acid', '7647-01-0', 'LOT002', 'AcidCorp', 'Room B-201', true, 'Jane Smith', '2024-01-20', '1L'),
('Ethanol', '64-17-5', 'LOT003', 'SolventCorp', 'Room A-102', false, 'Bob Johnson', '2024-02-01', '2L'),
('Sulfuric Acid', '7664-93-9', 'LOT004', 'AcidCorp', 'Room B-202', true, 'Alice Brown', '2024-02-10', '500ml'),
('Acetone', '67-64-1', 'LOT005', 'SolventPro', 'Room C-301', false, 'Charlie Wilson', '2024-02-15', '1L');