package com.etd.reservation_management.mapper;

import com.etd.reservation_management.dto.ReservationTypeResponseDTO;
import com.etd.reservation_management.entity.ReservationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ReservationTypeMapper {

    private final Logger logger = LoggerFactory.getLogger(ReservationTypeMapper.class);

    public List<ReservationTypeResponseDTO> mapReservationTypeToReservationTypeResponseDTO(List<ReservationType> reservationTypes) {
        logger.info("Inside ReservationTypeMapper :: Mapping ReservationType entities to ReservationTypeResponseDTOs");
        return reservationTypes.stream()
                .map(reservationType ->
                        ReservationTypeResponseDTO
                                .builder()
                                .id(reservationType.getTypeId())
                                .typeName(reservationType.getTypeName())
                                .build())
                .toList();
    }

}
