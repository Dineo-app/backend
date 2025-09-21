package com.dineo_backend.dineo.authentication.enums;

/**
 * Enum representing the different roles available in the application
 */
public enum Role {
    CUSTOMER("Customer"),
    PROVIDER("Provider"), 
    ADMIN("Administrator");
    
    private final String displayName;
    
    Role(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    @Override
    public String toString() {
        return this.name();
    }
}