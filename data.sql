-- =========================================================
-- Car Rental System - Initial Sample Data
-- =========================================================

USE car_rental_db;

-- 1. Insert Cars
INSERT INTO cars (make, model, model_year, category, daily_rate, fuel_type, transmission, seats, registration_number, mileage, status, image_url, features)
VALUES 
('Tesla', 'Model 3 Performance', 2024, 'ELECTRIC', 85.00, 'ELECTRIC', 'AUTOMATIC', 5, 'TSLA-301', 12400, 'AVAILABLE', 'https://images.unsplash.com/photo-1560958089-b8a1929cea89?auto=format&fit=crop&w=800&q=80', 'Autopilot, 315mi Range, Premium Audio, Wireless Charging'),
('BMW', 'M340i xDrive', 2023, 'LUXURY', 95.00, 'PETROL', 'AUTOMATIC', 5, 'BMW-402', 18500, 'AVAILABLE', 'https://images.unsplash.com/photo-1555215695-3004980ad54e?auto=format&fit=crop&w=800&q=80', '382 HP Turbo, Leather Seats, Heads-up Display, All-Wheel Drive'),
('Mercedes-Benz', 'C300 Sedan', 2024, 'LUXURY', 110.00, 'HYBRID', 'AUTOMATIC', 5, 'MBZ-503', 8200, 'AVAILABLE', 'https://images.unsplash.com/photo-1618843479313-40f8afb4b4d8?auto=format&fit=crop&w=800&q=80', 'Panoramic Sunroof, Burmester 3D Sound, Ambient Lighting'),
('Ford', 'Mustang GT 5.0', 2023, 'SPORTS', 120.00, 'PETROL', 'AUTOMATIC', 4, 'MUST-604', 15300, 'AVAILABLE', 'https://images.unsplash.com/photo-1584345604476-8ec5e12e42dd?auto=format&fit=crop&w=800&q=80', '450 HP V8 Engine, Active Exhaust, Brembo Brakes, Recaro Seats'),
('Porsche', '911 Carrera S', 2024, 'SPORTS', 250.00, 'PETROL', 'AUTOMATIC', 2, 'PORS-705', 4100, 'AVAILABLE', 'https://images.unsplash.com/photo-1503376780353-7e6692767b70?auto=format&fit=crop&w=800&q=80', 'Twin-Turbo Boxer 6, Sport Chrono, PASM Suspension, 443 HP'),
('Audi', 'Q7 Quattro Prestige', 2023, 'SUV', 130.00, 'PETROL', 'AUTOMATIC', 7, 'AUDI-806', 21000, 'AVAILABLE', 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?auto=format&fit=crop&w=800&q=80', '7 Seater, Adaptive Air Suspension, Matrix LED, Virtual Cockpit'),
('Toyota', 'Camry Hybrid XLE', 2024, 'SEDAN', 55.00, 'HYBRID', 'AUTOMATIC', 5, 'TOY-907', 9800, 'AVAILABLE', 'https://images.unsplash.com/photo-1621007947382-bb3c3994e3fb?auto=format&fit=crop&w=800&q=80', '52 MPG Combined, Toyota Safety Sense 3.0, Apple CarPlay'),
('Honda', 'CR-V Sport Touring', 2023, 'SUV', 65.00, 'HYBRID', 'AUTOMATIC', 5, 'HND-108', 16400, 'AVAILABLE', 'https://images.unsplash.com/photo-1568844293986-8d0400bd4745?auto=format&fit=crop&w=800&q=80', 'AWD Hybrid, Power Tailgate, Bose Audio, Heated Steering Wheel');

-- 2. Insert Customers
INSERT INTO customers (first_name, last_name, email, phone, driver_license_number, address)
VALUES
('Siddesh', 'Kumar', 'siddesh.kumar@example.com', '+1 555-0192', 'DL-9823145', '104 Silicon Valley Ave, San Jose, CA'),
('Sarah', 'Jenkins', 'sarah.j@example.com', '+1 555-0143', 'DL-7734129', '452 Ocean Drive, Miami, FL'),
('Alex', 'Morgan', 'alex.morgan@example.com', '+1 555-0188', 'DL-5542890', '789 Pine Street, Seattle, WA');
