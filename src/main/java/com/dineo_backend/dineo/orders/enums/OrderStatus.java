package com.dineo_backend.dineo.orders.enums;

/**
 * Enum representing different order statuses
 * Each status has a French label for user-friendly display
 */
public enum OrderStatus {
    PENDING("En attente"),
    CONFIRMED("Confirmée"),
    PREPARING("En préparation"),
    READY("Prête"),
    COMPLETED("Terminée"),
    CANCELLED("Annulée"),
    REJECTED("Rejetée");

    private final String label;

    OrderStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }

    /**
     * English translations for international use
     */
    public static class EN {
        public static final String PENDING = "Pending";
        public static final String CONFIRMED = "Confirmed";
        public static final String PREPARING = "Preparing";
        public static final String READY = "Ready";
        public static final String COMPLETED = "Completed";
        public static final String CANCELLED = "Cancelled";
        public static final String REJECTED = "Rejected";
    }

    /**
     * Get English label for the status
     * @return English label
     */
    public String getEnglishLabel() {
        switch (this) {
            case PENDING: return EN.PENDING;
            case CONFIRMED: return EN.CONFIRMED;
            case PREPARING: return EN.PREPARING;
            case READY: return EN.READY;
            case COMPLETED: return EN.COMPLETED;
            case CANCELLED: return EN.CANCELLED;
            case REJECTED: return EN.REJECTED;
            default: return this.name();
        }
    }
}