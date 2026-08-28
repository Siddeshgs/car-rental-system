package com.carrental.controller;

import com.carrental.dto.ApiResponse;
import com.carrental.dto.CarDto;
import com.carrental.entity.Car;
import com.carrental.enums.CarCategory;
import com.carrental.enums.CarStatus;
import com.carrental.enums.FuelType;
import com.carrental.enums.Transmission;
import com.carrental.service.CarService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cars")
@CrossOrigin(origins = "*")
@Tag(name = "Car Management", description = "Endpoints for managing the vehicle fleet")
public class CarController {

    private final CarService carService;

    public CarController(CarService carService) {
        this.carService = carService;
    }

    @GetMapping
    @Operation(summary = "Get all cars or filter by category, fuel, transmission, price, search")
    public ResponseEntity<ApiResponse<List<CarDto>>> getCars(
            @RequestParam(required = false) CarCategory category,
            @RequestParam(required = false) CarStatus status,
            @RequestParam(required = false) FuelType fuelType,
            @RequestParam(required = false) Transmission transmission,
            @RequestParam(required = false) BigDecimal maxRate,
            @RequestParam(required = false) String search) {

        List<Car> cars;
        if (category != null || status != null || fuelType != null || transmission != null || maxRate != null || (search != null && !search.trim().isEmpty())) {
            cars = carService.filterCars(category, status, fuelType, transmission, maxRate, search);
        } else {
            cars = carService.getAllCars();
        }

        List<CarDto> dtos = cars.stream().map(carService::mapToDto).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok("Cars fetched successfully", dtos));
    }

    @GetMapping("/available")
    @Operation(summary = "Get all currently available cars")
    public ResponseEntity<ApiResponse<List<CarDto>>> getAvailableCars() {
        List<CarDto> dtos = carService.getAvailableCars()
                .stream()
                .map(carService::mapToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok("Available cars retrieved", dtos));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get car by ID")
    public ResponseEntity<ApiResponse<CarDto>> getCarById(@PathVariable Long id) {
        Car car = carService.getCarById(id);
        return ResponseEntity.ok(ApiResponse.ok("Car details retrieved", carService.mapToDto(car)));
    }

    @PostMapping
    @Operation(summary = "Add a new car to the fleet")
    public ResponseEntity<ApiResponse<CarDto>> createCar(@Valid @RequestBody CarDto carDto) {
        Car created = carService.createCar(carDto);
        return new ResponseEntity<>(ApiResponse.ok("Car created successfully", carService.mapToDto(created)), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update car details")
    public ResponseEntity<ApiResponse<CarDto>> updateCar(@PathVariable Long id, @Valid @RequestBody CarDto carDto) {
        Car updated = carService.updateCar(id, carDto);
        return ResponseEntity.ok(ApiResponse.ok("Car updated successfully", carService.mapToDto(updated)));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update car status (AVAILABLE, RENTED, MAINTENANCE)")
    public ResponseEntity<ApiResponse<CarDto>> updateStatus(@PathVariable Long id, @RequestParam CarStatus status) {
        Car updated = carService.updateCarStatus(id, status);
        return ResponseEntity.ok(ApiResponse.ok("Car status updated to " + status, carService.mapToDto(updated)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a car from inventory")
    public ResponseEntity<ApiResponse<Void>> deleteCar(@PathVariable Long id) {
        carService.deleteCar(id);
        return ResponseEntity.ok(ApiResponse.ok("Car deleted successfully", null));
    }
}
