-- Sample data for H2 Database
-- Note: Users are now created programmatically in AdminUserInitialization.java
-- This ensures proper password encoding and role assignment

-- Sample chemicals data
INSERT INTO chemicals (name, cas_no, lot_no, producer, storage, toxic_state, responsible, order_date, weight)
VALUES
-- Safe chemicals
('Sodium Chloride', '7647-14-5', 'LOT001', 'ChemCorp', 'Room A-101', false, 'John Doe', '2024-01-15', '500g'),
('Distilled Water', '7732-18-5', 'LOT009', 'AquaCorp', 'Room A-101', false, 'John Doe', '2024-01-10', '5L'),
('Ethanol', '64-17-5', 'LOT003', 'SolventCorp', 'Room A-102', false, 'Bob Johnson', '2024-02-01', '2L'),
('Acetone', '67-64-1', 'LOT005', 'SolventPro', 'Room C-301', false, 'John Doe', '2024-02-15', '1L'),
('Calcium Carbonate', '471-34-1', 'LOT010', 'MineralCorp', 'Room A-103', false, 'Jane Smith', '2024-01-25', '1kg'),
('Glucose', '50-99-7', 'LOT011', 'BioCorp', 'Room A-104', false, 'Bob Johnson', '2024-02-05', '250g'),

-- Toxic chemicals
('Hydrochloric Acid', '7647-01-0', 'LOT002', 'AcidCorp', 'Room B-201', true, 'Jane Smith', '2024-01-20', '1L'),
('Sulfuric Acid', '7664-93-9', 'LOT004', 'AcidCorp', 'Room B-202', true, 'Jane Smith', '2024-02-10', '500ml'),
('Benzene', '71-43-2', 'LOT006', 'OrganicCorp', 'Room B-203', true, 'Bob Johnson', '2024-02-20', '250ml'),
('Methanol', '67-56-1', 'LOT007', 'SolventCorp', 'Room A-103', true, 'Jane Smith', '2024-02-25', '1L'),
('Sodium Hydroxide', '1310-73-2', 'LOT008', 'BaseCorp', 'Room B-204', true, 'John Doe', '2024-03-01', '1kg'),
('Formaldehyde', '50-00-0', 'LOT012', 'PreserveCorp', 'Room B-205', true, 'Jane Smith', '2024-03-05', '500ml'),
('Toluene', '108-88-3', 'LOT013', 'OrganicCorp', 'Room B-206', true, 'Bob Johnson', '2024-03-10', '1L'),
('Ammonia', '7664-41-7', 'LOT014', 'GasCorp', 'Room B-207', true, 'Jane Smith', '2024-03-15', '2L');