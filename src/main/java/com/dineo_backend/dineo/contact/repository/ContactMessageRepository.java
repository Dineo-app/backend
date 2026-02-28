package com.dineo_backend.dineo.contact.repository;

import com.dineo_backend.dineo.contact.entity.ContactMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ContactMessageRepository extends JpaRepository<ContactMessage, UUID>, JpaSpecificationExecutor<ContactMessage> {

    Page<ContactMessage> findByIsViewed(Boolean isViewed, Pageable pageable);

    long countByIsViewed(Boolean isViewed);
}
