package com.carrental.controller;

import com.carrental.dto.BookingResponse;
import com.carrental.enums.BookingStatus;
import com.carrental.service.BookingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingService bookingService;

    @Test
    @DisplayName("GET /api/bookings should return 200 and list of bookings")
    void testGetAllBookings() throws Exception {
        BookingResponse response = new BookingResponse();
        response.setId(1L);
        response.setBookingReference("BK-123456");
        response.setCarMake("Tesla");
        response.setCarModel("Model 3");
        response.setStatus(BookingStatus.CONFIRMED);
        response.setTotalAmount(new BigDecimal("300.00"));

        when(bookingService.getAllBookings()).thenReturn(Collections.singletonList(response));

        mockMvc.perform(get("/api/bookings")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].bookingReference").value("BK-123456"))
                .andExpect(jsonPath("$.data[0].carMake").value("Tesla"));
    }

    @Test
    @DisplayName("GET /api/bookings/{id} should return booking details")
    void testGetBookingById() throws Exception {
        BookingResponse response = new BookingResponse();
        response.setId(1L);
        response.setBookingReference("BK-998877");
        response.setStartDate(LocalDate.now().plusDays(1));
        response.setEndDate(LocalDate.now().plusDays(3));
        response.setStatus(BookingStatus.ACTIVE);

        when(bookingService.getBookingById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/bookings/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.bookingReference").value("BK-998877"));
    }
}
