package com.etd.reservation_management.config;

import com.etd.reservation_management.dao.ReservationTypeRepo;
import com.etd.reservation_management.entity.ReservationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataInitializer implements ApplicationRunner {

    private final Logger logger = LoggerFactory.getLogger(DataInitializer.class);
    private final ReservationTypeRepo reservationTypeRepo;

    public DataInitializer(ReservationTypeRepo reservationTypeRepo) {
        this.reservationTypeRepo = reservationTypeRepo;
    }

    @Override
    public void run(ApplicationArguments args) {
        seedReservationTypes();
    }

    private void seedReservationTypes() {
        if (reservationTypeRepo.count() > 0) {
            logger.info("DataInitializer :: Reservation types already seeded, skipping.");
            return;
        }
        List<String> typeNames = List.of("Flight", "Train", "Bus", "Cab", "Hotel");
        typeNames.forEach(name ->
                reservationTypeRepo.save(ReservationType.builder().typeName(name).build())
        );
        logger.info("DataInitializer :: Seeded {} reservation types.", typeNames.size());
    }
}
