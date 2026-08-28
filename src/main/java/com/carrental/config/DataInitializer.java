package com.carrental.config;

import com.carrental.dto.BookingRequest;
import com.carrental.entity.Car;
import com.carrental.entity.Customer;
import com.carrental.enums.*;
import com.carrental.repository.CarRepository;
import com.carrental.repository.CustomerRepository;
import com.carrental.service.BookingService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final CarRepository carRepository;
    private final CustomerRepository customerRepository;
    private final BookingService bookingService;

    public DataInitializer(CarRepository carRepository,
                           CustomerRepository customerRepository,
                           BookingService bookingService) {
        this.carRepository = carRepository;
        this.customerRepository = customerRepository;
        this.bookingService = bookingService;
    }

    @Override
    public void run(String... args) {
        if (carRepository.count() > 0) {
            return;
        }

        // 1. Seed Cars
        List<Car> cars = Arrays.asList(
                new Car("Tesla", "Model 3 Performance", 2024, CarCategory.ELECTRIC, new BigDecimal("85.00"),
                        FuelType.ELECTRIC, Transmission.AUTOMATIC, 5, "TSLA-301", 12400, CarStatus.AVAILABLE,
                        "https://images.unsplash.com/photo-1560958089-b8a1929cea89?auto=format&fit=crop&w=800&q=80",
                        "Autopilot, 315mi Range, Premium Audio, Wireless Charging, 0-60 in 3.1s"),

                new Car("BMW", "M340i xDrive", 2023, CarCategory.LUXURY, new BigDecimal("95.00"),
                        FuelType.PETROL, Transmission.AUTOMATIC, 5, "BMW-402", 18500, CarStatus.AVAILABLE,
                        "https://images.unsplash.com/photo-1555215695-3004980ad54e?auto=format&fit=crop&w=800&q=80",
                        "382 HP Turbo, Leather Seats, Heads-up Display, Harman Kardon, All-Wheel Drive"),

                new Car("Mercedes-Benz", "C300 Sedan", 2024, CarCategory.LUXURY, new BigDecimal("110.00"),
                        FuelType.HYBRID, Transmission.AUTOMATIC, 5, "MBZ-503", 8200, CarStatus.AVAILABLE,
                        "https://images.unsplash.com/photo-1618843479313-40f8afb4b4d8?auto=format&fit=crop&w=800&q=80",
                        "Mild Hybrid, Panoramic Sunroof, Burmester 3D Sound, Ambient Lighting, Driver Assist"),

                new Car("Ford", "Mustang GT 5.0", 2023, CarCategory.SPORTS, new BigDecimal("120.00"),
                        FuelType.PETROL, Transmission.AUTOMATIC, 4, "MUST-604", 15300, CarStatus.AVAILABLE,
                        "https://images.unsplash.com/photo-1584345604476-8ec5e12e42dd?auto=format&fit=crop&w=800&q=80",
                        "450 HP V8 Engine, Active Exhaust, Brembo Brakes, Track Apps, Recaro Seats"),

                new Car("Porsche", "911 Carrera S", 2024, CarCategory.SPORTS, new BigDecimal("250.00"),
                        FuelType.PETROL, Transmission.AUTOMATIC, 2, "PORS-705", 4100, CarStatus.AVAILABLE,
                        "https://images.unsplash.com/photo-1503376780353-7e6692767b70?auto=format&fit=crop&w=800&q=80",
                        "Twin-Turbo Boxer 6, Sport Chrono, PASM Suspension, Sport Exhaust, 443 HP"),

                new Car("Audi", "Q7 Quattro Prestige", 2023, CarCategory.SUV, new BigDecimal("130.00"),
                        FuelType.PETROL, Transmission.AUTOMATIC, 7, "AUDI-806", 21000, CarStatus.AVAILABLE,
                        "https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?auto=format&fit=crop&w=800&q=80",
                        "7 Seater, Adaptive Air Suspension, Matrix LED, Virtual Cockpit, Heated & Ventilated Seats"),

                new Car("Toyota", "Camry Hybrid XLE", 2024, CarCategory.SEDAN, new BigDecimal("55.00"),
                        FuelType.HYBRID, Transmission.AUTOMATIC, 5, "TOY-907", 9800, CarStatus.AVAILABLE,
                        "https://images.unsplash.com/photo-1621007947382-bb3c3994e3fb?auto=format&fit=crop&w=800&q=80",
                        "52 MPG Combined, Toyota Safety Sense 3.0, Apple CarPlay/Android Auto, JBL Audio"),

                new Car("Honda", "CR-V Sport Touring", 2023, CarCategory.SUV, new BigDecimal("65.00"),
                        FuelType.HYBRID, Transmission.AUTOMATIC, 5, "HND-108", 16400, CarStatus.AVAILABLE,
                        "https://images.unsplash.com/photo-1568844293986-8d0400bd4745?auto=format&fit=crop&w=800&q=80",
                        "AWD Hybrid, Hands-free Power Tailgate, Bose Audio, Honda Sensing, Heated Steering Wheel"),

                new Car("Hyundai", "Ioniq 5 Limited", 2024, CarCategory.ELECTRIC, new BigDecimal("75.00"),
                        FuelType.ELECTRIC, Transmission.AUTOMATIC, 5, "HYU-209", 11200, CarStatus.AVAILABLE,
                        "https://images.unsplash.com/photo-1593941707882-a5bba14938c7?auto=format&fit=crop&w=800&q=80",
                        "Ultra-Fast 800V Charging, Vision Roof, Augmented Reality HUD, V2L Power"),

                new Car("Chevrolet", "Tahoe Premier", 2023, CarCategory.SUV, new BigDecimal("140.00"),
                        FuelType.PETROL, Transmission.AUTOMATIC, 8, "CHEV-310", 28000, CarStatus.AVAILABLE,
                        "https://images.unsplash.com/photo-1533473359331-0135ef1b58bf?auto=format&fit=crop&w=800&q=80",
                        "8 Seater, Magnetic Ride Control, Rear-Seat Entertainment, Towing Package, 4WD")
        );

        carRepository.saveAll(cars);

        // 2. Seed Customers
        List<Customer> customers = Arrays.asList(
                new Customer("Siddesh", "Kumar", "siddesh.kumar@example.com", "+1 555-0192", "DL-9823145", "104 Silicon Valley Ave, San Jose, CA"),
                new Customer("Sarah", "Jenkins", "sarah.j@example.com", "+1 555-0143", "DL-7734129", "452 Ocean Drive, Miami, FL"),
                new Customer("Alex", "Morgan", "alex.morgan@example.com", "+1 555-0188", "DL-5542890", "789 Pine Street, Seattle, WA"),
                new Customer("Marcus", "Vance", "m.vance@example.com", "+1 555-0274", "DL-3382910", "12 Sunset Blvd, Los Angeles, CA")
        );

        customerRepository.saveAll(customers);

        // 3. Seed Sample Bookings
        try {
            Customer cust1 = customers.get(0);
            Customer cust2 = customers.get(1);
            Car car1 = cars.get(0); // Tesla
            Car car2 = cars.get(1); // BMW

            // Booking 1: Tesla
            BookingRequest req1 = new BookingRequest();
            req1.setCarId(car1.getId());
            req1.setCustomerId(cust1.getId());
            req1.setStartDate(LocalDate.now().plusDays(2));
            req1.setEndDate(LocalDate.now().plusDays(6));
            req1.setIncludeInsurance(true);
            req1.setPromoCode("SAVE10");
            req1.setPickupLocation("Downtown Airport Terminal");
            req1.setDropoffLocation("Downtown Airport Terminal");
            req1.setPaymentMethod(PaymentMethod.CREDIT_CARD);
            req1.setNotes("Customer requested child safety seat.");
            bookingService.createBooking(req1);

            // Booking 2: BMW
            BookingRequest req2 = new BookingRequest();
            req2.setCarId(car2.getId());
            req2.setCustomerId(cust2.getId());
            req2.setStartDate(LocalDate.now().plusDays(10));
            req2.setEndDate(LocalDate.now().plusDays(14));
            req2.setIncludeInsurance(false);
            req2.setPromoCode("DRIVE20");
            req2.setPickupLocation("Main City Branch");
            req2.setDropoffLocation("North Bay Station");
            req2.setPaymentMethod(PaymentMethod.UPI);
            req2.setNotes("Business executive rental.");
            bookingService.createBooking(req2);

        } catch (Exception e) {
            System.err.println("Note on seed bookings: " + e.getMessage());
        }
    }
}
