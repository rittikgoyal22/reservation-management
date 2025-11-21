package com.etd.reservation_management.controller;

import com.etd.reservation_management.dto.ReservationTypeResponseDTO;
import com.etd.reservation_management.service.interfaces.ReservationTypeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/reservations/types")
public class ReservationTypeController {

    private final Logger logger = LoggerFactory.getLogger(ReservationTypeController.class);
    private final ReservationTypeService reservationTypeService;


    public ReservationTypeController(ReservationTypeService reservationTypeService) {
        this.reservationTypeService = reservationTypeService;
    }

    @GetMapping()
    public ResponseEntity<List<ReservationTypeResponseDTO>> getAllReservationTypes() {
        logger.info("Inside ReservationTypeController :: getAllReservationTypes");
        List<ReservationTypeResponseDTO> reservationTypes = reservationTypeService.getAllReservationTypes();
        return ResponseEntity.ok(reservationTypes);
    }

}
