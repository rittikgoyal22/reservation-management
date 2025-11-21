package com.etd.reservation_management.dao;

import com.etd.reservation_management.entity.ReservationDocs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservationDocsRepo extends JpaRepository<ReservationDocs, Long> {

    @Query("SELECT rd FROM ReservationDocs rd where rd.reservation.id = :reservationId")
    ReservationDocs findByReservationId(Long reservationId);

}
