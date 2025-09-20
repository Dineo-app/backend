package com.dineo_backend.dineo.config;

public final class AppConstants {
    // Messages de succès
    public static final String USER_REGISTERED_SUCCESS = "Utilisateur enregistré avec succès.";
    public static final String USER_LOGIN_SUCCESS = "Connexion réussie.";

    // Messages d'erreur
    public static final String USER_ALREADY_EXISTS = "L'utilisateur existe déjà.";
    public static final String INVALID_CREDENTIALS = "Email ou mot de passe invalide.";
    public static final String INVALID_USER_DATA = "Les données utilisateur ne sont pas valides.";
    public static final String ROLE_NOT_FOUND = "Rôle par défaut introuvable.";
    public static final String INTERNAL_ERROR = "Une erreur interne s'est produite.";

    private AppConstants() {
        throw new UnsupportedOperationException("Utility class");
    }
}
