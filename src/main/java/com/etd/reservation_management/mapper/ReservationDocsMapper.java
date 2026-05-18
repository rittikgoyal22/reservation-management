package com.etd.reservation_management.mapper;

import com.etd.reservation_management.entity.Reservation;
import com.etd.reservation_management.entity.ReservationDocs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ReservationDocsMapper {

    private final Logger logger = LoggerFactory.getLogger(ReservationDocsMapper.class);

    public ReservationDocs mapReservationDocsByReservationAndDocPath(Reservation savedReservation, String uniqueFileName) {
        logger.info("Inside ReservationDocsMapper :: mapReservationDocsByReservationAndDocPath");
        return ReservationDocs
                .builder()
                .reservation(savedReservation)
                .documentUrl(uniqueFileName)
                .build();
    }

}
