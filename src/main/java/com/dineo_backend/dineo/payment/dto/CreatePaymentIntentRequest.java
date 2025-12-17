package com.dineo_backend.dineo.payment.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class CreatePaymentIntentRequest {
    
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;
    
    @NotNull(message = "Currency is required")
    private String currency;
    
    private String description;
    
    // Order items for reference
    private List<OrderItem> items;
    
    public CreatePaymentIntentRequest() {}
    
    public CreatePaymentIntentRequest(BigDecimal amount, String currency, String description, List<OrderItem> items) {
        this.amount = amount;
        this.currency = currency;
        this.description = description;
        this.items = items;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public String getCurrency() {
        return currency;
    }
    
    public void setCurrency(String currency) {
        this.currency = currency;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public List<OrderItem> getItems() {
        return items;
    }
    
    public void setItems(List<OrderItem> items) {
        this.items = items;
    }
    
    public static class OrderItem {
        private UUID platId;
        private String platName;
        private Integer quantity;
        private BigDecimal price;
        private UUID chefId;
        
        public OrderItem() {}
        
        public OrderItem(UUID platId, String platName, Integer quantity, BigDecimal price, UUID chefId) {
            this.platId = platId;
            this.platName = platName;
            this.quantity = quantity;
            this.price = price;
            this.chefId = chefId;
        }
        
        public UUID getPlatId() {
            return platId;
        }
        
        public void setPlatId(UUID platId) {
            this.platId = platId;
        }
        
        public String getPlatName() {
            return platName;
        }
        
        public void setPlatName(String platName) {
            this.platName = platName;
        }
        
        public Integer getQuantity() {
            return quantity;
        }
        
        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }
        
        public BigDecimal getPrice() {
            return price;
        }
        
        public void setPrice(BigDecimal price) {
            this.price = price;
        }
        
        public UUID getChefId() {
            return chefId;
        }
        
        public void setChefId(UUID chefId) {
            this.chefId = chefId;
        }
    }
}
