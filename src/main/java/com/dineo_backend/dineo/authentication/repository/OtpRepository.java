package com.dineo_backend.dineo.authentication.repository;

import com.dineo_backend.dineo.authentication.model.Otp;
import com.dineo_backend.dineo.authentication.model.Otp.OtpType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OtpRepository extends JpaRepository<Otp, UUID> {
    
    /**
     * Find the most recent non-verified OTP for a phone number and type
     */
    Optional<Otp> findFirstByPhoneAndTypeAndIsVerifiedFalseOrderByCreatedAtDesc(
        String phone, OtpType type
    );
    
    /**
     * Find the most recent non-verified OTP for an email and type
     */
    Optional<Otp> findFirstByEmailAndTypeAndIsVerifiedFalseOrderByCreatedAtDesc(
        String email, OtpType type
    );
    
    /**
     * Find OTP by code and phone (for verification)
     */
    Optional<Otp> findByCodeAndPhoneAndIsVerifiedFalse(String code, String phone);
    
    /**
     * Find OTP by code and email (for verification)
     */
    Optional<Otp> findByCodeAndEmailAndIsVerifiedFalse(String code, String email);
    
    /**
     * Delete all expired OTPs (cleanup job)
     */
    @Modifying
    @Query("DELETE FROM Otp o WHERE o.expiresAt < :now")
    void deleteExpiredOtps(@Param("now") LocalDateTime now);
    
    /**
     * Delete all OTPs for a specific phone number (after successful verification)
     */
    @Modifying
    @Query("DELETE FROM Otp o WHERE o.phone = :phone")
    void deleteAllByPhone(@Param("phone") String phone);
    
    /**
     * Delete all OTPs for a specific email (after successful verification)
     */
    @Modifying
    @Query("DELETE FROM Otp o WHERE o.email = :email")
    void deleteAllByEmail(@Param("email") String email);
}
