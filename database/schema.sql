-- Create Database
CREATE DATABASE IF NOT EXISTS skyweave;
USE skyweave;

-- 1. Airport Table
DROP TABLE IF EXISTS Flight;
DROP TABLE IF EXISTS Airport;
DROP TABLE IF EXISTS Admin;

CREATE TABLE Airport (
    id INT AUTO_INCREMENT PRIMARY KEY,
    airport_code VARCHAR(10) NOT NULL UNIQUE,
    airport_name VARCHAR(100) NOT NULL,
    city VARCHAR(100) NOT NULL,
    country VARCHAR(100) NOT NULL,
    INDEX (airport_code)
);

-- 2. Flight Table
CREATE TABLE Flight (
    id INT AUTO_INCREMENT PRIMARY KEY,
    flight_number VARCHAR(20) NOT NULL UNIQUE,
    airline VARCHAR(100) NOT NULL,
    source_airport VARCHAR(10) NOT NULL,
    destination_airport VARCHAR(10) NOT NULL,
    distance INT NOT NULL,     -- in km
    duration INT NOT NULL,     -- in minutes
    cost INT NOT NULL,         -- in INR
    status VARCHAR(20) NOT NULL DEFAULT 'Available', -- Available, Delayed, Cancelled
    FOREIGN KEY (source_airport) REFERENCES Airport(airport_code) ON DELETE CASCADE,
    FOREIGN KEY (destination_airport) REFERENCES Airport(airport_code) ON DELETE CASCADE
);

-- 3. Admin Table
CREATE TABLE Admin (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
);

-- Seed Admin Table
-- Password is 'admin123' hashed using bcrypt ($2a$10$tZ91176QkE/K1.hUfCg71uxW5a/JskKzPzJb0l/B7sXWwVn/NqB0O)
INSERT INTO Admin (username, password) VALUES 
('admin', '$2a$10$tZ91176QkE/K1.hUfCg71uxW5a/JskKzPzJb0l/B7sXWwVn/NqB0O');

-- Seed Airport Table (20 Airports)
INSERT INTO Airport (airport_code, airport_name, city, country) VALUES
('DEL', 'Indira Gandhi International Airport', 'Delhi', 'India'),
('BOM', 'Chhatrapati Shivaji Maharaj Airport', 'Mumbai', 'India'),
('BLR', 'Kempegowda International Airport', 'Bengaluru', 'India'),
('MAA', 'Chennai International Airport', 'Chennai', 'India'),
('HYD', 'Rajiv Gandhi International Airport', 'Hyderabad', 'India'),
('CCU', 'Netaji Subhash Chandra Bose Airport', 'Kolkata', 'India'),
('GOI', 'Dabolim Airport', 'Goa', 'India'),
('PNQ', 'Pune Airport', 'Pune', 'India'),
('DXB', 'Dubai International Airport', 'Dubai', 'UAE'),
('LHR', 'Heathrow Airport', 'London', 'UK'),
('JFK', 'John F. Kennedy International Airport', 'New York', 'USA'),
('SIN', 'Changi Airport', 'Singapore', 'Singapore'),
('HND', 'Haneda Airport', 'Tokyo', 'Japan'),
('SYD', 'Sydney Airport', 'Sydney', 'Australia'),
('CDG', 'Charles de Gaulle Airport', 'Paris', 'France'),
('FRA', 'Frankfurt Airport', 'Frankfurt', 'Germany'),
('YYZ', 'Toronto Pearson Airport', 'Toronto', 'Canada'),
('SFO', 'San Francisco International Airport', 'San Francisco', 'USA'),
('COK', 'Cochin International Airport', 'Kochi', 'India'),
('AMD', 'Sardar Vallabhbhai Patel Airport', 'Ahmedabad', 'India');

-- Seed Flight Table (50 Flights across major airlines)
INSERT INTO Flight (flight_number, airline, source_airport, destination_airport, distance, duration, cost, status) VALUES
('AI-101', 'Air India', 'DEL', 'BOM', 1150, 130, 5500, 'Available'),
('6E-201', 'IndiGo', 'DEL', 'BOM', 1150, 125, 4800, 'Available'),
('UK-801', 'Vistara', 'DEL', 'BLR', 1740, 160, 7200, 'Available'),
('6E-305', 'IndiGo', 'DEL', 'BLR', 1740, 165, 6000, 'Available'),
('AI-403', 'Air India', 'BOM', 'BLR', 840, 95, 4500, 'Available'),
('SG-151', 'SpiceJet', 'BOM', 'GOI', 440, 65, 3200, 'Available'),
('6E-412', 'IndiGo', 'DEL', 'HYD', 1260, 130, 5200, 'Delayed'),
('UK-710', 'Vistara', 'DEL', 'CCU', 1310, 135, 5900, 'Available'),
('AI-201', 'Air India', 'CCU', 'MAA', 1370, 145, 6200, 'Available'),
('6E-551', 'IndiGo', 'MAA', 'BLR', 270, 50, 2500, 'Available'),
('SG-221', 'SpiceJet', 'HYD', 'MAA', 510, 75, 3400, 'Available'),
('AI-501', 'Air India', 'BOM', 'PNQ', 120, 45, 2200, 'Cancelled'),
('6E-188', 'IndiGo', 'PNQ', 'DEL', 1160, 130, 5100, 'Available'),
('AI-312', 'Air India', 'COK', 'BLR', 370, 60, 3100, 'Available'),
('6E-671', 'IndiGo', 'AMD', 'DEL', 760, 90, 4000, 'Available'),
('EK-501', 'Emirates', 'BOM', 'DXB', 1930, 185, 18000, 'Available'),
('EK-503', 'Emirates', 'DEL', 'DXB', 2200, 210, 19500, 'Available'),
('QR-551', 'Qatar Airways', 'DXB', 'LHR', 5470, 470, 45000, 'Available'),
('BA-142', 'British Airways', 'DEL', 'LHR', 6710, 560, 58000, 'Available'),
('BA-112', 'British Airways', 'LHR', 'JFK', 5570, 480, 62000, 'Available'),
('UA-101', 'United Airlines', 'SFO', 'JFK', 4150, 320, 24000, 'Available'),
('AI-102', 'Air India', 'JFK', 'LHR', 5570, 470, 51000, 'Available'),
('SQ-402', 'Singapore Airlines', 'SIN', 'SYD', 6300, 460, 48000, 'Available'),
('SQ-421', 'Singapore Airlines', 'DEL', 'SIN', 4150, 340, 28000, 'Available'),
('6E-901', 'IndiGo', 'CCU', 'SIN', 2900, 250, 15000, 'Available'),
('EK-312', 'Emirates', 'DXB', 'HND', 7930, 600, 72000, 'Available'),
('SQ-638', 'Singapore Airlines', 'SIN', 'HND', 5300, 410, 46000, 'Available'),
('LH-761', 'Lufthansa', 'DEL', 'FRA', 6120, 510, 56000, 'Available'),
('AF-342', 'Air France', 'CDG', 'JFK', 5840, 490, 53000, 'Available'),
('BA-308', 'British Airways', 'LHR', 'CDG', 350, 70, 9500, 'Available'),
('LH-901', 'Lufthansa', 'FRA', 'LHR', 630, 95, 11000, 'Available'),
('AC-072', 'Air Canada', 'YYZ', 'LHR', 5700, 440, 49000, 'Available'),
('AC-080', 'Air Canada', 'SFO', 'YYZ', 3630, 290, 21000, 'Available'),
('AI-121', 'Air India', 'DEL', 'FRA', 6120, 520, 52000, 'Available'),
('EK-201', 'Emirates', 'DXB', 'JFK', 11000, 840, 95000, 'Available'),
('QR-203', 'Qatar Airways', 'HYD', 'DXB', 2550, 230, 21000, 'Available'),
('6E-921', 'IndiGo', 'BOM', 'COK', 1060, 110, 4600, 'Available'),
('AI-602', 'Air India', 'BLR', 'MAA', 270, 55, 2800, 'Available'),
('SG-405', 'SpiceJet', 'BLR', 'HYD', 500, 70, 3300, 'Available'),
('UK-981', 'Vistara', 'BOM', 'CCU', 1660, 155, 6800, 'Available'),
('6E-789', 'IndiGo', 'CCU', 'GOI', 1790, 180, 7500, 'Available'),
('SG-923', 'SpiceJet', 'GOI', 'PNQ', 340, 60, 2900, 'Available'),
('AI-711', 'Air India', 'DEL', 'AMD', 760, 95, 4200, 'Available'),
('6E-456', 'IndiGo', 'AMD', 'BOM', 440, 65, 3100, 'Available'),
('EK-406', 'Emirates', 'DXB', 'SIN', 5850, 450, 39000, 'Available'),
('SQ-211', 'Singapore Airlines', 'SIN', 'SFO', 13600, 890, 110000, 'Available'),
('QF-002', 'Qantas', 'SYD', 'HND', 7800, 580, 68000, 'Available'),
('AI-173', 'Air India', 'DEL', 'SFO', 12340, 960, 92000, 'Available'),
('AF-382', 'Air France', 'CDG', 'FRA', 450, 75, 9000, 'Available'),
('LH-401', 'Lufthansa', 'FRA', 'JFK', 6200, 500, 59000, 'Available'),
('QF-081', 'Qantas', 'SYD', 'SIN', 6300, 470, 45000, 'Available');
