package com.etd.reservation_management.controller;

import com.etd.reservation_management.dto.ReservationRequestDTO;
import com.etd.reservation_management.dto.ReservationResponseDTO;
import com.etd.reservation_management.service.interfaces.ReservationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("api/reservations")
@CrossOrigin
public class ReservationController {

    private final Logger logger = LoggerFactory.getLogger(ReservationController.class);
    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService)
    {
        this.reservationService = reservationService;
    }

    @PostMapping(path = "/add", consumes = "multipart/form-data")
    public ResponseEntity<ReservationResponseDTO> addReservation(@RequestPart("reservationRequestDTO") ReservationRequestDTO reservationRequestDTO,
                                                                 @RequestPart("pdfFile") MultipartFile pdfFile) {
        logger.info("Inside Reservation Controller :: addReservation");
        ReservationResponseDTO reservation = reservationService.addReservation(reservationRequestDTO, pdfFile);
        return ResponseEntity.ok(reservation);
    }

    @GetMapping("/track/{travelRequestId}")
    public ResponseEntity<List<ReservationResponseDTO>> getAllReservationsByTravelRequestId(@PathVariable("travelRequestId") Long travelRequestId)
    {
        logger.info("Inside Reservation Controller :: getAllReservationsByTravelRequestId");
        List<ReservationResponseDTO> reservations = reservationService.getAllReservationsByTravelRequestId(travelRequestId);
        return ResponseEntity.ok(reservations);
    }

    @GetMapping("/{reservationId}")
    public ResponseEntity<ReservationResponseDTO> getReservationById(@PathVariable("reservationId") Long reservationId)
    {
        logger.info("Inside Reservation Controller :: getReservationsById");
        ReservationResponseDTO reservation = reservationService.getReservationById(reservationId);
        return ResponseEntity.ok(reservation);
    }

}
