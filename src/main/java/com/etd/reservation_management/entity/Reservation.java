package com.etd.reservation_management.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Entity
@Table(name = "reservations")
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "reservation_done_by_employee_id")
    private Long reservationDoneByEmployeeId;

    @Column(name = "travel_request_id")
    private Long travelRequestId;

    @ManyToOne
    @JoinColumn(name = "reservation_type_id", referencedColumnName = "type_id")
    private ReservationType reservationType;

    @Column(name = "created_on")
    private Date createdOn;

    @Column(name = "reservation_done_with_entity")
    private String reservationDoneWithEntity;

    @Column(name = "reservation_date")
    private Date reservationDate;

    @Column(name = "amount")
    private Long amount;

    @Column(name = "confirmation_id")
    private String confirmationId;

    @Column(name = "remarks")
    private String remarks;

    @OneToOne(mappedBy = "reservation")
    private ReservationDocs reservationDocs;

}
