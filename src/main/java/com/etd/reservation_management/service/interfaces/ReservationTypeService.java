package com.etd.reservation_management.service.interfaces;

import com.etd.reservation_management.dto.ReservationTypeResponseDTO;

import java.util.List;

public interface ReservationTypeService {

    List<ReservationTypeResponseDTO> getAllReservationTypes();

}
