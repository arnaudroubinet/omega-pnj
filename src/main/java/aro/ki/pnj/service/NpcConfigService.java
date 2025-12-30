package aro.ki.pnj.service;

import aro.ki.pnj.entity.Npc;
import aro.ki.pnj.model.NpcConfigRequest;
import aro.ki.pnj.repository.NpcRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class NpcConfigService {

    @Inject
    NpcRepository npcRepository;

    @Transactional
    public Npc createOrUpdateNpc(NpcConfigRequest request) {
        Npc npc = npcRepository.findByNpcId(request.npcId)
            .orElseGet(() -> {
                Npc newNpc = new Npc();
                newNpc.npcId = request.npcId;
                newNpc.createdAt = LocalDateTime.now();
                return newNpc;
            });

        npc.name = request.name;
        npc.backstory = request.backstory;
        npc.personality = request.personality;
        npc.occupation = request.occupation;
        npc.goals = request.goals;
        npc.fears = request.fears;
        npc.updatedAt = LocalDateTime.now();

        npc.persist();
        return npc;
    }

    public List<Npc> getAllNpcs() {
        return npcRepository.listAll();
    }

    public Optional<Npc> getNpc(String npcId) {
        return npcRepository.findByNpcId(npcId);
    }

    @Transactional
    public boolean deleteNpc(String npcId) {
        Optional<Npc> npc = npcRepository.findByNpcId(npcId);
        if (npc.isPresent()) {
            npc.get().delete();
            return true;
        }
        return false;
    }
}
