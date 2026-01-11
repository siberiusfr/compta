package tn.cyberious.compta.authz.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Rôles disponibles pour les utilisateurs d'une societe cliente.
 */
@Getter
@RequiredArgsConstructor
public enum SocieteRole {
    MANAGER("MANAGER", "Responsable de la societe"),
    FINANCE("FINANCE", "Responsable financier"),
    VIEWER("VIEWER", "Consultation seule");

    private final String code;
    private final String description;

    public static SocieteRole fromCode(String code) {
        for (SocieteRole role : values()) {
            if (role.code.equalsIgnoreCase(code)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Rôle societe inconnu: " + code);
    }
}
