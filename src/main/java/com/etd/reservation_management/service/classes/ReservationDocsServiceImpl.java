package com.etd.reservation_management.service.classes;

import com.etd.reservation_management.dao.ReservationDocsRepo;
import com.etd.reservation_management.entity.ReservationDocs;
import com.etd.reservation_management.exception.NotFoundException;
import com.etd.reservation_management.service.interfaces.ReservationDocsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

import static com.etd.reservation_management.constant.AppConstant.DOCUMENT_NOT_FOUND;
import static com.etd.reservation_management.constant.AppConstant.RESERVATION_ID;
import static com.etd.reservation_management.constant.AppConstant.STATIC;

@Service
public class ReservationDocsServiceImpl implements ReservationDocsService {

    private final Logger logger = LoggerFactory.getLogger(ReservationDocsServiceImpl.class);
    private final ReservationDocsRepo reservationDocsRepo;
    private final MessageSource messageSource;

    public ReservationDocsServiceImpl(ReservationDocsRepo reservationDocsRepo, MessageSource messageSource) {
        this.reservationDocsRepo = reservationDocsRepo;
        this.messageSource = messageSource;
    }

    @Override
    public byte[] getDocument(Long reservationId) {
        logger.info("Inside ReservationDocsServiceImpl :: getDocument");
        ReservationDocs reservationDocs = reservationDocsRepo.findByReservationId(reservationId);

        if (ObjectUtils.isEmpty(reservationDocs) || ObjectUtils.isEmpty(reservationDocs.getDocumentUrl())) {
            throw new NotFoundException(messageSource.getMessage(DOCUMENT_NOT_FOUND, new Object[]{reservationId}, Locale.ENGLISH), RESERVATION_ID);
        }
        String uploadDir = "";
        try {
            uploadDir = new ClassPathResource(STATIC).getFile().getAbsolutePath();
        } catch (IOException e) {
            logger.error("Failed to save PDF file", e);
        }

        Path path = Paths.get(uploadDir, reservationDocs.getDocumentUrl());
        try {
            return Files.readAllBytes(path);
        } catch (IOException e) {
            logger.error("Failed to read document for reservation {}: {}", reservationId, e.getMessage());
            return new byte[0];
        }
    }

}
