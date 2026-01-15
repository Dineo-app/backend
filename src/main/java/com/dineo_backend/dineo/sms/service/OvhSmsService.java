package com.dineo_backend.dineo.sms.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Date;

/**
 * Service for sending SMS via OVH API
 * Implements OVH API signature authentication
 */
@Service
public class OvhSmsService {

    private static final Logger logger = LoggerFactory.getLogger(OvhSmsService.class);

    @Value("${ovh.sms.application.key}")
    private String applicationKey;

    @Value("${ovh.sms.application.secret}")
    private String applicationSecret;

    @Value("${ovh.sms.consumer.key}")
    private String consumerKey;

    @Value("${ovh.sms.service.name}")
    private String serviceName;

    @Value("${ovh.sms.api.endpoint}")
    private String apiEndpoint;

    @Value("${ovh.sms.sender:}")
    private String defaultSender;

    /**
     * Send SMS to a single phone number
     * 
     * @param phoneNumber recipient phone number (format: +33612345678)
     * @param message SMS message content
     * @return response from OVH API
     * @throws Exception if SMS sending fails
     */
    public String sendSms(String phoneNumber, String message) throws Exception {
        return sendSms(new String[]{phoneNumber}, message, defaultSender, false, false);
    }

    /**
     * Send SMS with full configuration
     * 
     * @param phoneNumbers array of recipient phone numbers
     * @param message SMS message content
     * @param sender custom sender name (e.g., "MIAMLOCOM") or null to use default
     * @param senderForResponse use short number for responses (France only) - ignored if sender is specified
     * @param noStopClause disable STOP clause (only for non-commercial SMS)
     * @return response from OVH API
     * @throws Exception if SMS sending fails
     */
    public String sendSms(String[] phoneNumbers, String message, String sender, boolean senderForResponse, boolean noStopClause) throws Exception {
        logger.info("📱 Attempting to send SMS to {} recipient(s) from sender: {}", phoneNumbers.length, sender != null && !sender.isEmpty() ? sender : "default");
        
        String method = "POST";
        String queryPath = "/sms/" + serviceName + "/jobs";
        URL queryUrl = new URL(apiEndpoint + queryPath);
        
        // Build JSON body
        StringBuilder receiversJson = new StringBuilder("[");
        for (int i = 0; i < phoneNumbers.length; i++) {
            receiversJson.append("\"").append(phoneNumbers[i]).append("\"");
            if (i < phoneNumbers.length - 1) {
                receiversJson.append(",");
            }
        }
        receiversJson.append("]");
        
        // Build body with optional sender
        String body;
        if (sender != null && !sender.isEmpty()) {
            body = String.format(
                "{\"receivers\":%s,\"message\":\"%s\",\"sender\":\"%s\",\"priority\":\"high\",\"noStopClause\":%s}",
                receiversJson.toString(),
                escapeJson(message),
                sender,
                noStopClause
            );
        } else {
            body = String.format(
                "{\"receivers\":%s,\"message\":\"%s\",\"priority\":\"high\",\"senderForResponse\":%s,\"noStopClause\":%s}",
                receiversJson.toString(),
                escapeJson(message),
                senderForResponse,
                noStopClause
            );
        }
        
        logger.debug("📤 SMS request body: {}", body);
        
        // Calculate timestamp
        long timestamp = new Date().getTime() / 1000;
        
        // Create signature
        String toSign = applicationSecret + "+" + consumerKey + "+" + method + "+" + queryUrl + "+" + body + "+" + timestamp;
        String signature = "$1$" + hashSHA1(toSign);
        
        logger.debug("🔐 Generated signature for timestamp: {}", timestamp);
        
        // Prepare HTTP request
        HttpURLConnection connection = (HttpURLConnection) queryUrl.openConnection();
        connection.setRequestMethod(method);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("X-Ovh-Application", applicationKey);
        connection.setRequestProperty("X-Ovh-Consumer", consumerKey);
        connection.setRequestProperty("X-Ovh-Signature", signature);
        connection.setRequestProperty("X-Ovh-Timestamp", String.valueOf(timestamp));
        connection.setDoOutput(true);
        
        // Send request body
        try (DataOutputStream wr = new DataOutputStream(connection.getOutputStream())) {
            wr.writeBytes(body);
            wr.flush();
        }
        
        // Read response
        int responseCode = connection.getResponseCode();
        logger.info("📥 OVH API response code: {}", responseCode);
        
        BufferedReader reader;
        if (responseCode == 200) {
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        } else {
            reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
        }
        
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();
        
        String responseBody = response.toString();
        
        if (responseCode == 200) {
            logger.info("✅ SMS sent successfully: {}", responseBody);
            return responseBody;
        } else {
            logger.error("❌ SMS sending failed with code {}: {}", responseCode, responseBody);
            throw new Exception("OVH SMS API error (HTTP " + responseCode + "): " + responseBody);
        }
    }

    /**
     * Get list of SMS service names
     * 
     * @return JSON array of service names
     * @throws Exception if API call fails
     */
    public String getSmsAccounts() throws Exception {
        logger.info("🔍 Fetching SMS accounts");
        
        String method = "GET";
        String queryPath = "/sms/";
        URL queryUrl = new URL(apiEndpoint + queryPath);
        String body = "";
        
        long timestamp = new Date().getTime() / 1000;
        
        // Create signature
        String toSign = applicationSecret + "+" + consumerKey + "+" + method + "+" + queryUrl + "+" + body + "+" + timestamp;
        String signature = "$1$" + hashSHA1(toSign);
        
        // Prepare HTTP request
        HttpURLConnection connection = (HttpURLConnection) queryUrl.openConnection();
        connection.setRequestMethod(method);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("X-Ovh-Application", applicationKey);
        connection.setRequestProperty("X-Ovh-Consumer", consumerKey);
        connection.setRequestProperty("X-Ovh-Signature", signature);
        connection.setRequestProperty("X-Ovh-Timestamp", String.valueOf(timestamp));
        
        // Read response
        int responseCode = connection.getResponseCode();
        BufferedReader reader;
        if (responseCode == 200) {
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        } else {
            reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
        }
        
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();
        
        String responseBody = response.toString();
        logger.info("📋 SMS accounts: {}", responseBody);
        
        return responseBody;
    }

    /**
     * Calculate SHA-1 hash for OVH signature
     * 
     * @param text text to hash
     * @return hexadecimal SHA-1 hash
     * @throws Exception if hashing fails
     */
    private String hashSHA1(String text) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] hash = md.digest(text.getBytes(StandardCharsets.ISO_8859_1));
        return convertToHex(hash);
    }

    /**
     * Convert byte array to hexadecimal string
     * 
     * @param data byte array
     * @return hexadecimal string
     */
    private String convertToHex(byte[] data) {
        StringBuilder buf = new StringBuilder();
        for (byte b : data) {
            int halfbyte = (b >>> 4) & 0x0F;
            int twoHalfs = 0;
            do {
                if ((0 <= halfbyte) && (halfbyte <= 9)) {
                    buf.append((char) ('0' + halfbyte));
                } else {
                    buf.append((char) ('a' + (halfbyte - 10)));
                }
                halfbyte = b & 0x0F;
            } while (twoHalfs++ < 1);
        }
        return buf.toString();
    }

    /**
     * Escape special characters for JSON
     * 
     * @param text text to escape
     * @return escaped text
     */
    private String escapeJson(String text) {
        return text.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }
}
