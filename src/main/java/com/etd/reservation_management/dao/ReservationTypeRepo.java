package com.etd.reservation_management.dao;

import com.etd.reservation_management.entity.ReservationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservationTypeRepo extends JpaRepository<ReservationType, Long> {

}
