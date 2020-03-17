package org.ftc.server.db.dao;

import org.ftc.server.db.domain.ConfirmedCase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConfirmedCasesRepo extends JpaRepository<ConfirmedCase, Long> {
    Optional<ConfirmedCase> findByUserUUID(UUID userUUID);
}
