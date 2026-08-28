package com.carrental.service;

import com.carrental.dto.CarDto;
import com.carrental.entity.Car;
import com.carrental.enums.CarCategory;
import com.carrental.enums.CarStatus;
import com.carrental.enums.FuelType;
import com.carrental.enums.Transmission;

import java.math.BigDecimal;
import java.util.List;

public interface CarService {

    List<Car> getAllCars();

    Car getCarById(Long id);

    Car createCar(CarDto carDto);

    Car updateCar(Long id, CarDto carDto);

    void deleteCar(Long id);

    Car updateCarStatus(Long id, CarStatus status);

    List<Car> getAvailableCars();

    List<Car> filterCars(CarCategory category, CarStatus status, FuelType fuelType,
                         Transmission transmission, BigDecimal maxRate, String search);

    CarDto mapToDto(Car car);
}
