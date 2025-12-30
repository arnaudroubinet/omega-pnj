package aro.ki.pnj.repository;

import aro.ki.pnj.entity.Npc;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;

@ApplicationScoped
public class NpcRepository implements PanacheRepository<Npc> {

    public Optional<Npc> findByNpcId(String npcId) {
        return find("npcId", npcId).firstResultOptional();
    }
}
