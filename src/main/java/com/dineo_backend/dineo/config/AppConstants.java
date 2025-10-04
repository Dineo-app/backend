package com.dineo_backend.dineo.config;

public final class AppConstants {
    // Messages de succès
    public static final String USER_REGISTERED_SUCCESS = "Utilisateur enregistré avec succès.";
    public static final String USER_LOGIN_SUCCESS = "Connexion réussie.";
    public static final String PASSWORD_UPDATED_SUCCESS = "Mot de passe mis à jour avec succès.";
    
    // Messages de succès pour les commandes
    public static final String ORDER_CREATED_SUCCESS = "Commande créée avec succès.";
    public static final String ORDER_RETRIEVED_SUCCESS = "Commande récupérée avec succès.";
    public static final String ORDERS_RETRIEVED_SUCCESS = "Commandes récupérées avec succès.";
    public static final String ORDER_STATUS_UPDATED_SUCCESS = "Statut de la commande mis à jour avec succès.";
    public static final String ORDER_UPDATED_SUCCESS = "Commande mise à jour avec succès.";
    public static final String ORDER_CANCELLED_SUCCESS = "Commande annulée avec succès.";
    public static final String STATISTICS_RETRIEVED_SUCCESS = "Statistiques récupérées avec succès.";
    public static final String CHECK_COMPLETED_SUCCESS = "Vérification terminée avec succès.";

    // Messages d'erreur
    public static final String USER_ALREADY_EXISTS = "L'utilisateur existe déjà.";
    public static final String INVALID_CREDENTIALS = "Email ou mot de passe invalide.";
    public static final String INVALID_USER_DATA = "Les données utilisateur ne sont pas valides.";
    public static final String ROLE_NOT_FOUND = "Rôle par défaut introuvable.";
    public static final String INTERNAL_ERROR = "Une erreur interne s'est produite.";
    public static final String PASSWORD_UPDATE_FAILED = "Échec de la mise à jour du mot de passe.";
    public static final String INVALID_TOKEN = "Token invalide.";
    public static final String USER_NOT_FOUND = "Utilisateur non trouvé.";
    public static final String CURRENT_PASSWORD_INCORRECT = "Mot de passe actuel incorrect.";
    
    // Messages d'erreur pour les commandes
    public static final String ORDER_CREATION_FAILED = "Échec de la création de la commande.";
    public static final String ORDER_NOT_FOUND = "Commande non trouvée.";
    public static final String ORDER_ACCESS_DENIED = "Accès à la commande refusé.";
    public static final String ORDER_UPDATE_FAILED = "Échec de la mise à jour de la commande.";

    private AppConstants() {
        throw new UnsupportedOperationException("Utility class");
    }
}
