package com.dineo_backend.dineo.contact.service;

import com.dineo_backend.dineo.contact.dto.ContactMessageResponse;
import com.dineo_backend.dineo.contact.dto.CreateContactRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.UUID;

public interface ContactMessageService {

    ContactMessageResponse createMessage(CreateContactRequest request);

    Page<ContactMessageResponse> getAllMessages(Boolean isViewed, LocalDate startDate, LocalDate endDate, Pageable pageable);

    ContactMessageResponse getMessageById(UUID id);

    long getUnviewedCount();
}
