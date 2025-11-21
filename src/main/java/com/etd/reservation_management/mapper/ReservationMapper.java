package com.etd.reservation_management.mapper;

import com.etd.reservation_management.dto.ReservationRequestDTO;
import com.etd.reservation_management.dto.ReservationResponseDTO;
import com.etd.reservation_management.entity.Reservation;
import com.etd.reservation_management.entity.ReservationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
public class ReservationMapper {

    Logger logger = LoggerFactory.getLogger(ReservationMapper.class);

    public List<ReservationResponseDTO> mapListOfReservationToListOfReservationResponseDTO(List<Reservation> reservations) {
        logger.info("Inside ReservationMapper :: mapListOfReservationToListOfReservationResponseDTO");
        return reservations.stream().map(this::mapReservationToReservationResponseDTO).toList();
    }

    public ReservationResponseDTO mapReservationToReservationResponseDTO(Reservation reservation) {
        logger.info("Inside ReservationMapper :: mapReservationToReservationResponseDTO");
        return ReservationResponseDTO
                .builder()
                .id(reservation.getId())
                .reservationDate(reservation.getReservationDate())
                .reservationTypeName(reservation.getReservationType().getTypeName())
                .reservationDoneByEmployeeId(reservation.getReservationDoneByEmployeeId())
                .reservationDoneWithEntity(reservation.getReservationDoneWithEntity())
                .amount(reservation.getAmount())
                .confirmationId(reservation.getConfirmationId())
                .createdOn(reservation.getCreatedOn())
                .remarks(reservation.getRemarks())
                .travelRequestId(reservation.getTravelRequestId())
                .build();
    }

    public Reservation mapReservationRequestDtoToReservation(ReservationRequestDTO reservationRequestDTO, ReservationType reservationType) {
        logger.info("Inside ReservationMapper :: mapReservationRequestDtoToReservation");
        return Reservation
                .builder()
                .reservationDoneByEmployeeId(reservationRequestDTO.getReservationDoneByEmployeeId())
                .travelRequestId(reservationRequestDTO.getTravelRequestId())
                .reservationType(reservationType)
                .createdOn(new Date())
                .reservationDoneWithEntity(reservationRequestDTO.getReservationDoneWithEntity())
                .reservationDate(reservationRequestDTO.getReservationDate())
                .amount(reservationRequestDTO.getAmount())
                .confirmationId(reservationRequestDTO.getConfirmationId())
                .remarks(reservationRequestDTO.getRemarks())
                .build();
    }

}
