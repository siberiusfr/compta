package tn.cyberious.compta.authz.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Rôles disponibles pour les utilisateurs d'une societe comptable (cabinet).
 */
@Getter
@RequiredArgsConstructor
public enum CabinetRole {
    MANAGER("MANAGER", "Responsable du cabinet"),
    COMPTABLE("COMPTABLE", "Comptable du cabinet"),
    ASSISTANT("ASSISTANT", "Assistant comptable");

    private final String code;
    private final String description;

    public static CabinetRole fromCode(String code) {
        for (CabinetRole role : values()) {
            if (role.code.equalsIgnoreCase(code)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Rôle cabinet inconnu: " + code);
    }
}
