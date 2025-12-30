package aro.ki.pnj.entity;

import aro.ki.pnj.model.InteractionType;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "interactions", indexes = {
    @Index(name = "idx_npc_character", columnList = "npc_id, character_name"),
    @Index(name = "idx_npc_timestamp", columnList = "npc_id, timestamp")
})
public class Interaction extends PanacheEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "npc_id", nullable = false)
    public Npc npc;

    @Column(nullable = false, name = "character_name")
    public String characterName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public InteractionType type;

    @Column(length = 2000, nullable = false)
    public String input;

    @Column(length = 4000)
    public String response;

    @Column(nullable = false)
    public LocalDateTime timestamp;

    @Column(length = 500)
    public String context;

    @Column(name = "is_memory_worthy")
    public Boolean isMemoryWorthy = false;

    @Column(name = "relationship_impact")
    public Integer relationshipImpact = 0;

    @PrePersist
    public void prePersist() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }
}
