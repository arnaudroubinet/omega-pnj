package aro.ki.pnj.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "relationships",
    uniqueConstraints = @UniqueConstraint(columnNames = {"npc_id", "character_name"}))
public class Relationship extends PanacheEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "npc_id", nullable = false)
    public Npc npc;

    @Column(nullable = false, name = "character_name")
    public String characterName;

    @Column(nullable = false)
    public Integer affinity = 0;  // -100 to +100

    @Column(nullable = false, name = "trust_level")
    public Integer trustLevel = 0;  // 0 to 100

    @Column(nullable = false, name = "fear_level")
    public Integer fearLevel = 0;  // 0 to 100

    @Column(name = "first_met")
    public LocalDateTime firstMet;

    @Column(name = "last_interaction")
    public LocalDateTime lastInteraction;

    @Column(nullable = false, name = "total_interactions")
    public Integer totalInteractions = 0;

    @Column(length = 500, name = "current_status")
    public String currentStatus = "stranger";

    @PrePersist
    public void prePersist() {
        if (firstMet == null) {
            firstMet = LocalDateTime.now();
        }
    }
}
