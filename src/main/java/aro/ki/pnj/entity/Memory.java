package aro.ki.pnj.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "memories", indexes = {
    @Index(name = "idx_npc_character_importance", columnList = "npc_id, character_name, importance")
})
public class Memory extends PanacheEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "npc_id", nullable = false)
    public Npc npc;

    @Column(nullable = false, name = "character_name")
    public String characterName;

    @Column(length = 1000, nullable = false, name = "memory_text")
    public String memoryText;

    @Column(nullable = false)
    public Integer importance;

    @Column(nullable = false, name = "created_at")
    public LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interaction_id")
    public Interaction sourceInteraction;

    @Column(length = 200, name = "emotional_context")
    public String emotionalContext;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
