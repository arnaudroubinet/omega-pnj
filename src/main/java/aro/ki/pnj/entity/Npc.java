package aro.ki.pnj.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "npcs")
public class Npc extends PanacheEntity {

    @Column(nullable = false, unique = true)
    public String npcId;  // e.g., "merchant_bob", "guard_alice"

    @Column(nullable = false)
    public String name;

    @Column(length = 2000)
    public String backstory;

    @Column(length = 1000)
    public String personality;

    @Column(length = 500)
    public String occupation;

    @Column(length = 500)
    public String goals;

    @Column(length = 500)
    public String fears;

    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;

    @OneToMany(mappedBy = "npc", cascade = CascadeType.ALL)
    public List<Interaction> interactions = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
