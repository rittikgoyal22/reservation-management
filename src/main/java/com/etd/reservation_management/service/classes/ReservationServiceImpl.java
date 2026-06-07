package com.etd.reservation_management.service.classes;

import com.etd.reservation_management.client.TravelPlannerClient;
import com.etd.reservation_management.dao.ReservationDocsRepo;
import com.etd.reservation_management.dao.ReservationRepo;
import com.etd.reservation_management.dao.ReservationTypeRepo;
import com.etd.reservation_management.dto.ReservationRequestDTO;
import com.etd.reservation_management.dto.ReservationResponseDTO;
import com.etd.reservation_management.entity.Reservation;
import com.etd.reservation_management.entity.ReservationDocs;
import com.etd.reservation_management.entity.ReservationType;
import com.etd.reservation_management.exception.BadRequestException;
import com.etd.reservation_management.exception.DocumentSizeLimitExceededException;
import com.etd.reservation_management.exception.IllegalArgumentException;
import com.etd.reservation_management.exception.IllegalStateException;
import com.etd.reservation_management.exception.NotFoundException;
import com.etd.reservation_management.mapper.ReservationDocsMapper;
import com.etd.reservation_management.mapper.ReservationMapper;
import com.etd.reservation_management.service.interfaces.ReservationService;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static com.etd.reservation_management.constant.AppConstant.AMOUNT;
import static com.etd.reservation_management.constant.AppConstant.AMOUNT_NOT_POSITIVE;
import static com.etd.reservation_management.constant.AppConstant.APPROVED;
import static com.etd.reservation_management.constant.AppConstant.APPROVED_BUDGET;
import static com.etd.reservation_management.constant.AppConstant.APPROVED_BUDGET_NOT_CALCULATED;
import static com.etd.reservation_management.constant.AppConstant.BUDGET_EXCEED;
import static com.etd.reservation_management.constant.AppConstant.BUS;
import static com.etd.reservation_management.constant.AppConstant.CAB;
import static com.etd.reservation_management.constant.AppConstant.FIFTEEN;
import static com.etd.reservation_management.constant.AppConstant.FIFTY;
import static com.etd.reservation_management.constant.AppConstant.FLIGHT;
import static com.etd.reservation_management.constant.AppConstant.FROM_DATE;
import static com.etd.reservation_management.constant.AppConstant.HOTEL;
import static com.etd.reservation_management.constant.AppConstant.HOTEL_RESERVATION_DATE_INVALID;
import static com.etd.reservation_management.constant.AppConstant.INVALID_PDF_FORMAT;
import static com.etd.reservation_management.constant.AppConstant.MODE_OF_TRAVEL;
import static com.etd.reservation_management.constant.AppConstant.PDF_FILE;
import static com.etd.reservation_management.constant.AppConstant.PDF_SIZE_EXCEED;
import static com.etd.reservation_management.constant.AppConstant.REQUEST_ID;
import static com.etd.reservation_management.constant.AppConstant.REQUEST_STATUS;
import static com.etd.reservation_management.constant.AppConstant.RESERVATION;
import static com.etd.reservation_management.constant.AppConstant.RESERVATIONS_NOT_FOUND;
import static com.etd.reservation_management.constant.AppConstant.RESERVATION_ALREADY_DONE;
import static com.etd.reservation_management.constant.AppConstant.RESERVATION_ID;
import static com.etd.reservation_management.constant.AppConstant.RESERVATION_NOT_FOUND;
import static com.etd.reservation_management.constant.AppConstant.RESERVATION_TYPE_ID;
import static com.etd.reservation_management.constant.AppConstant.RESERVATION_TYPE_NOT_FOUND;
import static com.etd.reservation_management.constant.AppConstant.THIRTY_FIVE;
import static com.etd.reservation_management.constant.AppConstant.TRAIN;
import static com.etd.reservation_management.constant.AppConstant.TRAVEL_REQUEST_ID;
import static com.etd.reservation_management.constant.AppConstant.TRAVEL_REQUEST_NOT_APPROVED;
import static com.etd.reservation_management.constant.AppConstant.TRAVEL_REQUEST_NOT_FOUND;
import static com.etd.reservation_management.constant.AppConstant.TRAVEL_RESERVATION_DATE_INVALID;
import static com.etd.reservation_management.constant.AppConstant.UNDERSCORE;

@Service
public class ReservationServiceImpl implements ReservationService {

    private final Logger logger = LoggerFactory.getLogger(ReservationServiceImpl.class);
    private final ReservationTypeRepo reservationTypeRepo;
    private final TravelPlannerClient travelPlannerClient;
    private final ReservationRepo reservationRepo;
    private final ReservationMapper reservationMapper;
    private final ReservationDocsRepo reservationDocsRepo;
    private final ReservationDocsMapper reservationDocsMapper;
    private final MessageSource messageSource;
    private final String uploadDir;

    public ReservationServiceImpl(ReservationTypeRepo reservationTypeRepo, TravelPlannerClient travelPlannerClient,
                                  ReservationRepo reservationRepo, ReservationMapper reservationMapper,
                                  ReservationDocsRepo reservationDocsRepo, ReservationDocsMapper reservationDocsMapper,
                                  MessageSource messageSource,
                                  @Value("${app.upload.dir}") String uploadDir) {
        this.reservationTypeRepo = reservationTypeRepo;
        this.travelPlannerClient = travelPlannerClient;
        this.reservationRepo = reservationRepo;
        this.reservationMapper = reservationMapper;
        this.reservationDocsRepo = reservationDocsRepo;
        this.reservationDocsMapper = reservationDocsMapper;
        this.messageSource = messageSource;
        this.uploadDir = uploadDir;
    }

    private static final long MAX_PDF_SIZE = 1048576;
    private static final Set<String> TRAVEL_MODES = Set.of(FLIGHT, BUS, TRAIN);
    private static final Set<Long> TRAVEL_MODES_IDS = Set.of(1L, 2L, 3L);

    @Override
    @Transactional
    public ReservationResponseDTO addReservation(ReservationRequestDTO requestDTO, MultipartFile pdfFile) {
        logger.info("Inside ReservationServiceImpl :: addReservation");
        validatePdfFile(pdfFile);
        if (requestDTO.getAmount() == null || requestDTO.getAmount() <= 0) {
            throw new BadRequestException(messageSource.getMessage(AMOUNT_NOT_POSITIVE, null, Locale.ENGLISH), AMOUNT);
        }
        ReservationType reservationType = reservationTypeRepo.findById(requestDTO.getReservationTypeId())
                .orElseThrow(() -> new IllegalArgumentException(
                        messageSource.getMessage(RESERVATION_TYPE_NOT_FOUND, new Object[]{requestDTO.getReservationTypeId()}, Locale.ENGLISH),
                        RESERVATION_TYPE_ID));
        ObjectNode travelRequest;
        try {
            travelRequest = travelPlannerClient.getTravelRequestDetailByTravelRequestId(requestDTO.getTravelRequestId());
        } catch (Exception e) {
            logger.warn("Failed to fetch travel request {}: {}", requestDTO.getTravelRequestId(), e.getMessage());
            throw new IllegalArgumentException(
                    messageSource.getMessage(TRAVEL_REQUEST_NOT_FOUND, new Object[]{requestDTO.getTravelRequestId()}, Locale.ENGLISH),
                    TRAVEL_REQUEST_ID);
        }
        if (ObjectUtils.isEmpty(travelRequest)) {
            throw new IllegalArgumentException(
                    messageSource.getMessage(TRAVEL_REQUEST_NOT_FOUND, new Object[]{requestDTO.getTravelRequestId()}, Locale.ENGLISH),
                    TRAVEL_REQUEST_ID);
        }
        if (!APPROVED.equals(travelRequest.get(REQUEST_STATUS).asText())) {
            throw new BadRequestException(
                    messageSource.getMessage(TRAVEL_REQUEST_NOT_APPROVED, new Object[]{requestDTO.getTravelRequestId()}, Locale.ENGLISH),
                    REQUEST_STATUS);
        }
        validateReservationDates(requestDTO, reservationType, travelRequest);
        List<Reservation> prevReservations = reservationRepo.findByTravelRequestId(travelRequest.get(REQUEST_ID).asLong());
        validateBusinessRules(requestDTO, travelRequest, prevReservations);
        Reservation reservation = reservationMapper.mapReservationRequestDtoToReservation(requestDTO, reservationType);
        Reservation savedReservation = reservationRepo.save(reservation);
        String uniqueFileName = savePdfFile(pdfFile);
        ReservationDocs reservationDocs = reservationDocsMapper.mapReservationDocsByReservationAndDocPath(savedReservation, uniqueFileName);
        reservationDocsRepo.save(reservationDocs);
        return reservationMapper.mapReservationToReservationResponseDTO(savedReservation);
    }

    private void validatePdfFile(MultipartFile pdfFile) {
        if (pdfFile.getSize() > MAX_PDF_SIZE) {
            throw new DocumentSizeLimitExceededException(messageSource.getMessage(PDF_SIZE_EXCEED, null, Locale.ENGLISH));
        }
        String contentType = pdfFile.getContentType();
        if (contentType == null || !contentType.equals("application/pdf")) {
            throw new BadRequestException(messageSource.getMessage(INVALID_PDF_FORMAT, null, Locale.ENGLISH), PDF_FILE);
        }
    }

    private void validateReservationDates(ReservationRequestDTO requestDTO, ReservationType reservationType, ObjectNode travelRequest) {
        Date travelRequestFromDate = Date.valueOf(travelRequest.get(FROM_DATE).asText().substring(0, 10));
        long diffInDays = Math.abs((travelRequestFromDate.getTime() - requestDTO.getReservationDate().getTime()) / (24 * 60 * 60 * 1000L));
        String typeName = reservationType.getTypeName();

        if ((TRAIN.equals(typeName) || BUS.equals(typeName)) && diffInDays != 1) {
            throw new IllegalArgumentException(messageSource.getMessage(TRAVEL_RESERVATION_DATE_INVALID, null, Locale.ENGLISH), FROM_DATE);
        }
        if (HOTEL.equals(typeName) && diffInDays != 0) {
            throw new IllegalArgumentException(messageSource.getMessage(HOTEL_RESERVATION_DATE_INVALID, null, Locale.ENGLISH), FROM_DATE);
        }
    }

    private void validateBusinessRules(ReservationRequestDTO requestDTO, ObjectNode travelRequest, List<Reservation> prevReservations) {
        List<String> prevReservationTypes = prevReservations.stream()
                .map(r -> r.getReservationType().getTypeName())
                .toList();

        if (prevReservationTypes.stream().anyMatch(TRAVEL_MODES::contains) && TRAVEL_MODES_IDS.contains(requestDTO.getReservationTypeId())) {
            throw new IllegalStateException(messageSource.getMessage(RESERVATION_ALREADY_DONE, new Object[]{MODE_OF_TRAVEL}, Locale.ENGLISH), RESERVATION);
        }
        if (prevReservationTypes.contains(CAB) && requestDTO.getReservationTypeId() == 4L) {
            throw new IllegalStateException(messageSource.getMessage(RESERVATION_ALREADY_DONE, new Object[]{CAB}, Locale.ENGLISH), RESERVATION);
        }
        if (prevReservationTypes.contains(HOTEL) && requestDTO.getReservationTypeId() == 5L) {
            throw new IllegalStateException(messageSource.getMessage(RESERVATION_ALREADY_DONE, new Object[]{HOTEL}, Locale.ENGLISH), RESERVATION);
        }

        if (travelRequest.get(APPROVED_BUDGET) == null || travelRequest.get(APPROVED_BUDGET).isNull()) {
            throw new BadRequestException(
                    messageSource.getMessage(APPROVED_BUDGET_NOT_CALCULATED, null, Locale.ENGLISH),
                    APPROVED_BUDGET);
        }
        long approvedBudget = travelRequest.get(APPROVED_BUDGET).asLong();
        long reservationsBudget = approvedBudget * 70 / 100;
        validateAmount(requestDTO, reservationsBudget);
    }

    private void validateAmount(ReservationRequestDTO requestDTO, long reservationsBudget) {
        Long requestedAmount = requestDTO.getAmount();

        if (TRAVEL_MODES_IDS.contains(requestDTO.getReservationTypeId()) && requestedAmount > (reservationsBudget * 35 / 100)) {
            throw new IllegalArgumentException(messageSource.getMessage(BUDGET_EXCEED, new Object[]{MODE_OF_TRAVEL, THIRTY_FIVE}, Locale.ENGLISH), AMOUNT);
        }
        if (requestDTO.getReservationTypeId() == 4L && requestedAmount > (reservationsBudget * 15 / 100)) {
            throw new IllegalArgumentException(messageSource.getMessage(BUDGET_EXCEED, new Object[]{CAB, FIFTEEN}, Locale.ENGLISH), AMOUNT);
        }
        if (requestDTO.getReservationTypeId() == 5L && requestedAmount > (reservationsBudget * 50 / 100)) {
            throw new IllegalArgumentException(messageSource.getMessage(BUDGET_EXCEED, new Object[]{HOTEL, FIFTY}, Locale.ENGLISH), AMOUNT);
        }
    }

    private String savePdfFile(MultipartFile pdfFile) {
        String originalFileName = pdfFile.getOriginalFilename();
        String uniqueFileName = System.currentTimeMillis() + UNDERSCORE + originalFileName;
        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            Path filePath = uploadPath.resolve(uniqueFileName);
            Files.write(filePath, pdfFile.getBytes());
        } catch (IOException e) {
            logger.error("Failed to save PDF file: {}", e.getMessage());
            throw new BadRequestException("Failed to save the uploaded PDF file.", PDF_FILE);
        }
        return uniqueFileName;
    }

    @Override
    public List<ReservationResponseDTO> getAllReservationsByTravelRequestId(Long travelRequestId) {
        logger.info("Inside ReservationServiceImpl :: getAllReservationsByTravelRequestId");
        List<Reservation> reservations = reservationRepo.findByTravelRequestId(travelRequestId);
        if (ObjectUtils.isEmpty(reservations)) {
            throw new NotFoundException(messageSource.getMessage(RESERVATIONS_NOT_FOUND, new Object[]{travelRequestId}, Locale.ENGLISH), TRAVEL_REQUEST_ID);
        }
        return reservationMapper.mapListOfReservationToListOfReservationResponseDTO(reservations);
    }

    @Override
    public ReservationResponseDTO getReservationById(Long reservationId) {
        logger.info("Inside ReservationServiceImpl :: getReservationById");
        Reservation reservation = reservationRepo.findById(reservationId).orElse(null);
        if (ObjectUtils.isEmpty(reservation)) {
            throw new NotFoundException(messageSource.getMessage(RESERVATION_NOT_FOUND, new Object[]{reservationId}, Locale.ENGLISH), RESERVATION_ID);
        }
        return reservationMapper.mapReservationToReservationResponseDTO(reservation);
    }
}
