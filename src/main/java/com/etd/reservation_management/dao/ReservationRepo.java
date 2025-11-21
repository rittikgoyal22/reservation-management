package com.etd.reservation_management.dao;

import com.etd.reservation_management.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservationRepo extends JpaRepository<Reservation, Long> {

    List<Reservation> findByTravelRequestId(Long travelRequestId);

}
