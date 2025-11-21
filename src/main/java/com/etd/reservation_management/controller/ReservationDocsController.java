package com.etd.reservation_management.controller;

import com.etd.reservation_management.service.interfaces.ReservationDocsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/reservations")
public class ReservationDocsController {

    private final Logger logger = LoggerFactory.getLogger(ReservationDocsController.class);
    private final ReservationDocsService reservationDocsService;

    public ReservationDocsController(ReservationDocsService reservationDocsService) {
        this.reservationDocsService = reservationDocsService;
    }

    @GetMapping("/{reservationId}/download")
    public ResponseEntity<byte[]> getDocument(@PathVariable("reservationId") Long reservationId) {
        logger.info("Inside ReservationDocsController :: getDocument");
        byte[] pdfBytes = reservationDocsService.getDocument(reservationId);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

}
