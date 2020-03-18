package org.ftc.server.db.domain;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import lombok.Data;

@Data
@Entity
public class Connectivity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private long id;

    @Column(nullable = false)
    private UUID user1Id;

    @Column(nullable = false)
    private UUID user2Id;

    @UpdateTimestamp
    private LocalDateTime lastUpdate;

    @Max(1)
    @Min(0)
    @Column(nullable = false)
    @ColumnDefault("0")
    private double factor;
}
