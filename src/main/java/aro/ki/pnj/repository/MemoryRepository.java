package aro.ki.pnj.repository;

import aro.ki.pnj.entity.Memory;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class MemoryRepository implements PanacheRepository<Memory> {

    public List<Memory> findTopMemoriesForCharacter(Long npcId, String characterName, int limit) {
        return find("npc.id = ?1 and characterName = ?2 order by importance desc, createdAt desc",
                    npcId, characterName)
               .range(0, limit - 1)
               .list();
    }

    public List<Memory> findAllMemoriesForCharacter(Long npcId, String characterName) {
        return find("npc.id = ?1 and characterName = ?2 order by createdAt desc",
                    npcId, characterName)
               .list();
    }

    public List<Memory> findByNpcId(Long npcId) {
        return find("npc.id = ?1 order by createdAt desc", npcId).list();
    }

    public List<Memory> findByNpcAndCharacter(Long npcId, String characterName) {
        return findAllMemoriesForCharacter(npcId, characterName);
    }

    public List<Memory> findTopMemories(Long npcId, int limit) {
        return find("npc.id = ?1 order by importance desc, createdAt desc", npcId)
               .range(0, limit - 1)
               .list();
    }
}
