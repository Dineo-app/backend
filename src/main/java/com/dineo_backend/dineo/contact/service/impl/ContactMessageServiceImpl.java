package com.dineo_backend.dineo.contact.service.impl;

import com.dineo_backend.dineo.contact.dto.ContactMessageResponse;
import com.dineo_backend.dineo.contact.dto.CreateContactRequest;
import com.dineo_backend.dineo.contact.entity.ContactMessage;
import com.dineo_backend.dineo.contact.repository.ContactMessageRepository;
import com.dineo_backend.dineo.contact.service.ContactMessageService;
import jakarta.persistence.criteria.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ContactMessageServiceImpl implements ContactMessageService {

    private static final Logger logger = LoggerFactory.getLogger(ContactMessageServiceImpl.class);

    private final ContactMessageRepository repository;

    public ContactMessageServiceImpl(ContactMessageRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public ContactMessageResponse createMessage(CreateContactRequest request) {
        logger.info("Creating new contact message from: {}", request.getEmail());

        ContactMessage msg = new ContactMessage();
        msg.setFullName(request.getFullName());
        msg.setEmail(request.getEmail());
        msg.setSubject(request.getSubject());
        msg.setMessage(request.getMessage());
        msg.setPhone(request.getPhone());
        msg.setIsViewed(false);

        ContactMessage saved = repository.save(msg);
        logger.info("Contact message created with ID: {}", saved.getId());

        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ContactMessageResponse> getAllMessages(Boolean isViewed, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        logger.info("Fetching contact messages - isViewed: {}, startDate: {}, endDate: {}", isViewed, startDate, endDate);

        Specification<ContactMessage> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (isViewed != null) {
                predicates.add(cb.equal(root.get("isViewed"), isViewed));
            }
            if (startDate != null) {
                LocalDateTime start = startDate.atStartOfDay();
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), start));
            }
            if (endDate != null) {
                LocalDateTime end = endDate.atTime(23, 59, 59);
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), end));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<ContactMessage> messages = repository.findAll(spec, pageable);
        logger.info("Found {} contact messages", messages.getTotalElements());
        return messages.map(this::mapToResponse);
    }

    @Override
    @Transactional
    public ContactMessageResponse getMessageById(UUID id) {
        logger.info("Fetching contact message with ID: {}", id);
        ContactMessage msg = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Message non trouvé"));

        if (!msg.getIsViewed()) {
            msg.setIsViewed(true);
            repository.save(msg);
            logger.info("Contact message {} marked as viewed", id);
        }
        return mapToResponse(msg);
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnviewedCount() {
        return repository.countByIsViewed(false);
    }

    private ContactMessageResponse mapToResponse(ContactMessage msg) {
        ContactMessageResponse r = new ContactMessageResponse();
        r.setId(msg.getId());
        r.setFullName(msg.getFullName());
        r.setEmail(msg.getEmail());
        r.setSubject(msg.getSubject());
        r.setMessage(msg.getMessage());
        r.setPhone(msg.getPhone());
        r.setIsViewed(msg.getIsViewed());
        r.setCreatedAt(msg.getCreatedAt());
        r.setUpdatedAt(msg.getUpdatedAt());
        return r;
    }
}
