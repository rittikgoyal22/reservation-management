package com.etd.reservation_management.service.interfaces;

import com.etd.reservation_management.dto.ReservationRequestDTO;
import com.etd.reservation_management.dto.ReservationResponseDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface ReservationService {

    ReservationResponseDTO addReservation(ReservationRequestDTO reservationRequestDTO, MultipartFile pdfFile);

    List<ReservationResponseDTO> getAllReservationsByTravelRequestId(Long travelRequestId);

    ReservationResponseDTO getReservationById(Long reservationId);

}
