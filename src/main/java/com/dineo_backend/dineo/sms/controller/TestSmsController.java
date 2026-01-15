package com.dineo_backend.dineo.sms.controller;

import com.dineo_backend.dineo.shared.dto.ApiResponse;
import com.dineo_backend.dineo.sms.service.OvhSmsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Test controller for OVH SMS integration
 * WARNING: This is for testing only - implement proper authentication before production use
 */
@RestController
@RequestMapping("/api/v1/test/sms")
public class TestSmsController {

    private static final Logger logger = LoggerFactory.getLogger(TestSmsController.class);

    @Autowired
    private OvhSmsService ovhSmsService;

    /**
     * Test endpoint to send SMS
     * 
     * POST /api/v1/test/sms/send
     * Body: {
     *   "phoneNumber": "+33612345678",
     *   "message": "Test SMS from Dineo"
     * }
     * 
     * @param request SMS request with phone number and message
     * @return API response with result
     */
    @PostMapping("/send")
    public ResponseEntity<ApiResponse<String>> sendTestSms(@RequestBody SmsTestRequest request) {
        try {
            logger.info("📱 Test SMS request received for: {}", request.getPhoneNumber());
            
            // Validate input
            if (request.getPhoneNumber() == null || request.getPhoneNumber().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, "Phone number is required"));
            }
            
            if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, "Message is required"));
            }
            
            // Validate phone number format (basic check)
            String phone = request.getPhoneNumber().trim();
            if (!phone.startsWith("+")) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, "Phone number must start with + (e.g., +33612345678)"));
            }
            
            // Send SMS
            String result = ovhSmsService.sendSms(phone, request.getMessage());
            
            logger.info("✅ SMS sent successfully to {}", phone);
            
            return ResponseEntity.ok(
                ApiResponse.success("SMS sent successfully", result)
            );
            
        } catch (Exception e) {
            logger.error("❌ Error sending test SMS: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(500, "Failed to send SMS: " + e.getMessage()));
        }
    }

    /**
     * Get list of SMS accounts
     * 
     * GET /api/v1/test/sms/accounts
     * 
     * @return list of SMS service names
     */
    @GetMapping("/accounts")
    public ResponseEntity<ApiResponse<String>> getSmsAccounts() {
        try {
            logger.info("🔍 Fetching SMS accounts");
            
            String accounts = ovhSmsService.getSmsAccounts();
            
            return ResponseEntity.ok(
                ApiResponse.success("SMS accounts retrieved successfully", accounts)
            );
            
        } catch (Exception e) {
            logger.error("❌ Error fetching SMS accounts: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(500, "Failed to fetch SMS accounts: " + e.getMessage()));
        }
    }

    /**
     * Health check endpoint
     * 
     * GET /api/v1/test/sms/health
     * 
     * @return health status
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        return ResponseEntity.ok(
            ApiResponse.success("OVH SMS service is running", "OK")
        );
    }

    /**
     * DTO for SMS test request
     */
    public static class SmsTestRequest {
        private String phoneNumber;
        private String message;

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public void setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
