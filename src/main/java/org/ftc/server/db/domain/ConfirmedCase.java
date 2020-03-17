package org.ftc.server.db.domain;

import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Data;

@Data
@Entity
public class ConfirmedCase {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private long id;

    @CreationTimestamp
    private LocalDateTime created;

    @Column
    private UUID userUUID;
}
