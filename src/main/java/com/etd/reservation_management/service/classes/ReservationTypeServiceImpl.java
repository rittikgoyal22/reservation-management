package com.etd.reservation_management.service.classes;

import com.etd.reservation_management.dao.ReservationTypeRepo;
import com.etd.reservation_management.dto.ReservationTypeResponseDTO;
import com.etd.reservation_management.entity.ReservationType;
import com.etd.reservation_management.mapper.ReservationTypeMapper;
import com.etd.reservation_management.service.interfaces.ReservationTypeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReservationTypeServiceImpl implements ReservationTypeService {

    private final Logger logger = LoggerFactory.getLogger(ReservationTypeServiceImpl.class);
    private final ReservationTypeRepo reservationTypeRepo;
    private final ReservationTypeMapper reservationTypeMapper;

    public ReservationTypeServiceImpl(ReservationTypeRepo reservationTypeRepo, ReservationTypeMapper reservationTypeMapper) {
        this.reservationTypeRepo = reservationTypeRepo;
        this.reservationTypeMapper = reservationTypeMapper;
    }

    @Override
    public List<ReservationTypeResponseDTO> getAllReservationTypes() {
        logger.info("Inside ReservationTypeServiceImpl :: Fetching all reservation types from the database");
        List<ReservationType> reservationTypes = reservationTypeRepo.findAll();
        return reservationTypeMapper.mapReservationTypeToReservationTypeResponseDTO(reservationTypes);
    }

}
