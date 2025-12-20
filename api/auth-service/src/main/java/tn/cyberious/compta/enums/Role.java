package tn.cyberious.compta.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Enum représentant les différents rôles utilisateurs dans le système */
@Getter
@RequiredArgsConstructor
public enum Role {
  /** Administrateur système avec tous les droits */
  ADMIN("ADMIN", "Administrateur système avec tous les droits"),

  /** Comptable pouvant gérer plusieurs sociétés */
  COMPTABLE("COMPTABLE", "Comptable pouvant gérer plusieurs sociétés"),

  /** Utilisateur de type société pouvant avoir plusieurs sociétés */
  SOCIETE("SOCIETE", "Utilisateur de type société pouvant avoir plusieurs sociétés"),

  /** Employé appartenant à une société */
  EMPLOYEE("EMPLOYEE", "Employé appartenant à une société");

  private final String name;
  private final String description;

  /** Récupère le nom du rôle avec le préfixe ROLE_ pour Spring Security */
  public String getAuthority() {
    return "ROLE_" + name;
  }

  /** Trouve un rôle par son nom */
  public static Role fromName(String name) {
    for (Role role : values()) {
      if (role.name.equalsIgnoreCase(name) || role.name().equalsIgnoreCase(name)) {
        return role;
      }
    }
    throw new IllegalArgumentException("Unknown role: " + name);
  }

  @Override
  public String toString() {
    return name;
  }
}
