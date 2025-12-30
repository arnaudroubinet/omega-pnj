package aro.ki.pnj.repository;

import aro.ki.pnj.entity.Interaction;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Collections;
import java.util.List;

@ApplicationScoped
public class InteractionRepository implements PanacheRepository<Interaction> {

    public List<Interaction> findRecentByNpcAndCharacter(Long npcId, String characterName, int limit) {
        List<Interaction> interactions = find("npc.id = ?1 and characterName = ?2 order by timestamp desc",
                    npcId, characterName)
               .range(0, limit - 1)
               .list();
        Collections.reverse(interactions);  // Return in chronological order
        return interactions;
    }

    public List<Interaction> findMemoryWorthyByNpc(Long npcId) {
        return find("npc.id = ?1 and isMemoryWorthy = true order by timestamp desc", npcId)
               .list();
    }
}
