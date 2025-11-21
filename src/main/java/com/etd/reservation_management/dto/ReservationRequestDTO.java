package com.etd.reservation_management.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReservationRequestDTO {

    private Long reservationDoneByEmployeeId;

    private Long travelRequestId;

    private String reservationDoneWithEntity;

    private Date reservationDate;

    private Long amount;

    private String confirmationId;

    private String remarks;

    private Long reservationTypeId;

}
