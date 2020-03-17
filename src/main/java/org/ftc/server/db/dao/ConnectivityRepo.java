package org.ftc.server.db.dao;

import org.ftc.server.db.domain.Connectivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConnectivityRepo extends JpaRepository<Connectivity, Long> {
    Optional<Connectivity> findByUser1IdAndUser2Id(UUID uuid, UUID uuid1);

    @Query(
        "FROM Connectivity " +
        "JOIN ConfirmedCase ON user1UUID = userUUID OR user2UUID = userUUID " +
        "WHERE (user1UUID = :userUUID OR user2UUID = :userUUID) "
    )
    List<Connectivity> connectivitiesWithConfirmedCases(@Param("userUUID") UUID userUUID);
}
