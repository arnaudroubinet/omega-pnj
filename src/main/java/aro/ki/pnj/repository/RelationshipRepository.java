package aro.ki.pnj.repository;

import aro.ki.pnj.entity.Npc;
import aro.ki.pnj.entity.Relationship;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@ApplicationScoped
public class RelationshipRepository implements PanacheRepository<Relationship> {

    public Optional<Relationship> findByNpcAndCharacter(Long npcId, String characterName) {
        return find("npc.id = ?1 and characterName = ?2", npcId, characterName)
               .firstResultOptional();
    }

    @Transactional
    public Relationship getOrCreate(Npc npc, String characterName) {
        return findByNpcAndCharacter(npc.id, characterName)
               .orElseGet(() -> createNew(npc, characterName));
    }

    private Relationship createNew(Npc npc, String characterName) {
        Relationship rel = new Relationship();
        rel.npc = npc;
        rel.characterName = characterName;
        rel.firstMet = LocalDateTime.now();
        rel.persist();
        return rel;
    }
}
