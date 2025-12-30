package tn.cyberious.compta.oauth2.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.cyberious.compta.oauth2.entity.Role;

@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {

  Optional<Role> findByName(String name);

  boolean existsByName(String name);
}
