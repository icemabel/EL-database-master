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
CREATE INDEX idx_user_profile_email ON user_profile(email);
CREATE INDEX idx_user_profile_position ON user_profile(position);
CREATE INDEX IF NOT EXISTS idx_user_profile_role ON user_profile(role);

ALTER TABLE user_profile ADD COLUMN IF NOT EXISTS role VARCHAR(10) DEFAULT 'USER';

-- Update existing users with appropriate roles
UPDATE user_profile SET role = 'ADMIN' WHERE username = 'admin';
UPDATE user_profile SET role = 'USER' WHERE username IN ('labtech1', 'scientist1', 'safety1');

-- Add a few more sample users for testing
INSERT INTO user_profile (username, password, first_name, last_name, email, phone_number, position, duration, role)
VALUES
('user1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iYqiSfFGjDyOspeI7lYz1dU3/2Z6', 'Alice', 'Johnson', 'alice.johnson@example.com', '123-456-7894', 'Research Assistant', 2, 'USER'),
('user2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iYqiSfFGjDyOspeI7lYz1dU3/2Z6', 'Bob', 'Smith', 'bob.smith@example.com', '123-456-7895', 'Lab Technician', 3, 'USER');

-- Create index for role column
CREATE INDEX IF NOT EXISTS idx_user_profile_role ON user_profile(role);

-- Insert default admin user (password is 'password' encoded with BCrypt)
INSERT INTO user_profile (username, password, first_name, last_name, email, phone_number, position, duration)
VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iYqiSfFGjDyOspeI7lYz1dU3/2Z6', 'Admin', 'User', 'admin@example.com', '123-456-7890', '', 0);

-- Insert sample regular users
INSERT INTO user_profile (username, password, first_name, last_name, email, phone_number, position, duration)
VALUES
('labtech1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iYqiSfFGjDyOspeI7lYz1dU3/2Z6', 'John', 'Doe', 'john.doe@example.com', '123-456-7891', '', 0),
('scientist1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iYqiSfFGjDyOspeI7lYz1dU3/2Z6', 'Jane', 'Smith', 'jane.smith@example.com', '123-456-7892', '', 0),
('safety1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iYqiSfFGjDyOspeI7lYz1dU3/2Z6', 'Bob', 'Johnson', 'bob.johnson@example.com', '123-456-7893', '', 0);

-- Sample chemicals data
INSERT INTO chemicals (name, cas_no, lot_no, producer, storage, toxic_state, responsible, order_date, weight)
VALUES
('Sodium Chloride', '7647-14-5', 'LOT001', 'ChemCorp', 'Room A-101', false, 'John Doe', '2024-01-15', '500g'),
('Hydrochloric Acid', '7647-01-0', 'LOT002', 'AcidCorp', 'Room B-201', true, 'Jane Smith', '2024-01-20', '1L'),
('Ethanol', '64-17-5', 'LOT003', 'SolventCorp', 'Room A-102', false, 'Bob Johnson', '2024-02-01', '2L'),
('Sulfuric Acid', '7664-93-9', 'LOT004', 'AcidCorp', 'Room B-202', true, 'Jane Smith', '2024-02-10', '500ml'),
('Acetone', '67-64-1', 'LOT005', 'SolventPro', 'Room C-301', false, 'John Doe', '2024-02-15', '1L'),
('Benzene', '71-43-2', 'LOT006', 'OrganicCorp', 'Room B-203', true, 'Bob Johnson', '2024-02-20', '250ml'),
('Methanol', '67-56-1', 'LOT007', 'SolventCorp', 'Room A-103', true, 'Jane Smith', '2024-02-25', '1L'),
('Sodium Hydroxide', '1310-73-2', 'LOT008', 'BaseCorp', 'Room B-204', true, 'John Doe', '2024-03-01', '1kg');