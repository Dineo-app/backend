package com.dineo_backend.dineo.authentication.config;

public class AppConstants {
    
    // General Messages
    public static final String INTERNAL_ERROR = "Une erreur interne s'est produite";
    public static final String SUCCESS = "Opération réussie";
    public static final String INVALID_REQUEST = "Requête invalide";
    public static final String UNAUTHORIZED_ACCESS = "Accès non autorisé";
    public static final String FORBIDDEN_ACCESS = "Accès interdit";
    public static final String RESOURCE_NOT_FOUND = "Ressource non trouvée";
    public static final String INVALID_USER_DATA = "Données utilisateur invalides";
    public static final String USER_ALREADY_EXISTS = "L'utilisateur existe déjà";
    
    // Authentication Messages
    public static final String USER_REGISTERED_SUCCESS = "Utilisateur enregistré avec succès";
    public static final String USER_LOGIN_SUCCESS = "Connexion réussie";
    public static final String USER_LOGOUT_SUCCESS = "Déconnexion réussie";
    public static final String INVALID_CREDENTIALS = "Identifiants invalides";
    public static final String EMAIL_ALREADY_EXISTS = "Cette adresse e-mail existe déjà";
    public static final String USER_NOT_FOUND = "Utilisateur non trouvé";
    public static final String ACCOUNT_DISABLED = "Compte désactivé";
    public static final String ACCOUNT_NOT_VERIFIED = "Compte non vérifié";
    
    // Password Messages
    public static final String PASSWORD_UPDATED_SUCCESS = "Mot de passe mis à jour avec succès";
    public static final String PASSWORD_UPDATE_FAILED = "Échec de la mise à jour du mot de passe";
    public static final String CURRENT_PASSWORD_INCORRECT = "Le mot de passe actuel est incorrect";
    public static final String NEW_PASSWORD_SAME_AS_CURRENT = "Le nouveau mot de passe doit être différent de l'actuel";
    public static final String PASSWORD_MISMATCH = "Les mots de passe ne correspondent pas";
    public static final String PASSWORD_TOO_WEAK = "Le mot de passe est trop faible";
    public static final String PASSWORD_REQUIRED = "Le mot de passe est obligatoire";
    public static final String OLD_PASSWORD_REQUIRED = "L'ancien mot de passe est obligatoire";
    public static final String NEW_PASSWORD_REQUIRED = "Le nouveau mot de passe est obligatoire";
    public static final String CONFIRM_PASSWORD_REQUIRED = "La confirmation du mot de passe est obligatoire";
    
    // JWT Messages
    public static final String JWT_TOKEN_INVALID = "Token JWT invalide";
    public static final String JWT_TOKEN_EXPIRED = "Token JWT expiré";
    public static final String JWT_TOKEN_MISSING = "Token JWT manquant";
    public static final String JWT_TOKEN_MALFORMED = "Token JWT mal formé";
    
    // Validation Messages
    public static final String FIELD_REQUIRED = "Ce champ est obligatoire";
    public static final String EMAIL_INVALID = "Format d'e-mail invalide";
    public static final String PHONE_INVALID = "Format de téléphone invalide";
    public static final String NAME_TOO_SHORT = "Le nom doit contenir au moins 2 caractères";
    public static final String NAME_TOO_LONG = "Le nom ne peut pas dépasser 50 caractères";
    public static final String DESCRIPTION_TOO_LONG = "La description ne peut pas dépasser 2000 caractères";
    
    // English Messages
    public static final class EN {
        public static final String INTERNAL_ERROR = "Internal server error occurred";
        public static final String SUCCESS = "Operation successful";
        public static final String INVALID_REQUEST = "Invalid request";
        public static final String UNAUTHORIZED_ACCESS = "Unauthorized access";
        public static final String FORBIDDEN_ACCESS = "Forbidden access";
        public static final String RESOURCE_NOT_FOUND = "Resource not found";
        
        public static final String USER_REGISTERED_SUCCESS = "User registered successfully";
        public static final String USER_LOGIN_SUCCESS = "Login successful";
        public static final String USER_LOGOUT_SUCCESS = "Logout successful";
        public static final String INVALID_CREDENTIALS = "Invalid credentials";
        public static final String EMAIL_ALREADY_EXISTS = "Email already exists";
        public static final String USER_NOT_FOUND = "User not found";
        public static final String ACCOUNT_DISABLED = "Account disabled";
        public static final String ACCOUNT_NOT_VERIFIED = "Account not verified";
        
        public static final String PASSWORD_UPDATED_SUCCESS = "Password updated successfully";
        public static final String PASSWORD_UPDATE_FAILED = "Password update failed";
        public static final String CURRENT_PASSWORD_INCORRECT = "Current password is incorrect";
        public static final String NEW_PASSWORD_SAME_AS_CURRENT = "New password must be different from current";
        public static final String PASSWORD_MISMATCH = "Passwords do not match";
        public static final String PASSWORD_TOO_WEAK = "Password is too weak";
        public static final String PASSWORD_REQUIRED = "Password is required";
        public static final String OLD_PASSWORD_REQUIRED = "Old password is required";
        public static final String NEW_PASSWORD_REQUIRED = "New password is required";
        public static final String CONFIRM_PASSWORD_REQUIRED = "Confirm password is required";
        
        public static final String JWT_TOKEN_INVALID = "Invalid JWT token";
        public static final String JWT_TOKEN_EXPIRED = "JWT token expired";
        public static final String JWT_TOKEN_MISSING = "JWT token missing";
        public static final String JWT_TOKEN_MALFORMED = "Malformed JWT token";
        
        public static final String FIELD_REQUIRED = "This field is required";
        public static final String EMAIL_INVALID = "Invalid email format";
        public static final String PHONE_INVALID = "Invalid phone format";
        public static final String NAME_TOO_SHORT = "Name must be at least 2 characters";
        public static final String NAME_TOO_LONG = "Name cannot exceed 50 characters";
        public static final String DESCRIPTION_TOO_LONG = "Description cannot exceed 2000 characters";
    }
}
