package aro.ki.pnj.service;

import aro.ki.pnj.aiservice.NpcChatMemoryProvider;
import aro.ki.pnj.aiservice.NpcChatService;
import aro.ki.pnj.entity.Interaction;
import aro.ki.pnj.entity.Npc;
import aro.ki.pnj.entity.Relationship;
import aro.ki.pnj.model.ChainOfThoughtResponse;
import aro.ki.pnj.model.HandleRequest;
import aro.ki.pnj.model.HandleResponse;
import aro.ki.pnj.model.InteractionType;
import aro.ki.pnj.repository.InteractionRepository;
import aro.ki.pnj.repository.NpcRepository;
import aro.ki.pnj.repository.RelationshipRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

@ApplicationScoped
public class NpcHandlerService {

    private static final Logger log = LoggerFactory.getLogger(NpcHandlerService.class);

    @Inject
    NpcRepository npcRepository;

    @Inject
    InteractionRepository interactionRepository;

    @Inject
    RelationshipRepository relationshipRepository;

    @Inject
    MemoryService memoryService;

    @Inject
    ContextBuilderService contextBuilder;

    @Inject
    NpcChatService chatService;

    @Inject
    NpcChatMemoryProvider memoryProvider;

    @Inject
    ChainOfThoughtParser cotParser;

    @Transactional
    public HandleResponse handle(HandleRequest request) {
        log.debug("Handling interaction for NPC: {}, character: {}, type: {}",
            request.npcId, request.characterName, request.actionType);

        // 1. Find or validate NPC
        Npc npc = npcRepository.findByNpcId(request.npcId)
            .orElseThrow(() -> new NotFoundException("NPC not found: " + request.npcId));
        log.debug("NPC found: {}", npc.name);

        // 2. Get or create relationship
        Relationship relationship = relationshipRepository.getOrCreate(npc, request.characterName);
        log.debug("Relationship loaded: {} interactions, affinity: {}",
            relationship.totalInteractions, relationship.affinity);

        // 3. Build memory ID (unique per NPC + character)
        String memoryId = request.npcId + ":" + request.characterName;

        // 4. Populate chat memory with history if first interaction in session
        memoryProvider.populateMemory(memoryId, npc.id, request.characterName);
        log.debug("Chat memory populated for memoryId: {}", memoryId);

        // 5. Call AI service
        log.info("Calling AI service - NPC: {}, Message: '{}'", npc.name, request.message);
        String rawAiResponse;
        try {
            if (request.actionType == InteractionType.PARLER) {
                rawAiResponse = chatService.chat(memoryId, request.message);
            } else {
                String actionMessage = "A game event occurred: " + request.message;
                rawAiResponse = chatService.handleAction(memoryId, actionMessage);
            }
            log.debug("Raw AI response received: {} chars", rawAiResponse.length());
        } catch (Exception e) {
            log.error("Failed to get AI response for NPC {}: {}", npc.name, e.getMessage(), e);
            throw new RuntimeException("Failed to get AI response: " + e.getMessage(), e);
        }

        // 6. Parse Chain of Thought response
        ChainOfThoughtResponse cotResponse = cotParser.parse(rawAiResponse);
        log.info("CoT parsed - Thinking: {}, Response: '{}'",
            cotResponse.thinking != null ? cotResponse.thinking.substring(0, Math.min(80, cotResponse.thinking.length())) + "..." : "[none]",
            cotResponse.response.substring(0, Math.min(100, cotResponse.response.length())) + "...");

        // 7. Store interaction
        Interaction interaction = new Interaction();
        interaction.npc = npc;
        interaction.characterName = request.characterName;
        interaction.type = request.actionType;
        interaction.input = request.message;
        interaction.response = cotResponse.response;  // Only the visible response
        interaction.thinking = cotResponse.thinking;   // Internal thoughts
        interaction.timestamp = LocalDateTime.now();
        interaction.context = request.context;
        interaction.persist();

        // 7. Update relationship
        relationship.lastInteraction = LocalDateTime.now();
        relationship.totalInteractions++;
        updateRelationshipStatus(relationship);
        relationship.persist();

        // 8. Check if memory-worthy and create memory
        if (memoryService.isMemoryWorthy(request.message, cotResponse.response, request.actionType)) {
            interaction.isMemoryWorthy = true;
            memoryService.createMemory(npc, request.characterName, interaction);
        }

        // 9. Build response
        String emotionalState = memoryService.extractEmotion(cotResponse.response);
        HandleResponse response = new HandleResponse();
        response.npcResponse = cotResponse.response;
        response.thinking = cotResponse.thinking;  // Include internal thoughts for UI visualization
        response.emotionalState = emotionalState;
        response.timestamp = interaction.timestamp;
        response.relationshipChange = interaction.relationshipImpact;

        return response;
    }

    private void updateRelationshipStatus(Relationship relationship) {
        int interactions = relationship.totalInteractions;
        int affinity = relationship.affinity;

        if (interactions == 1) {
            relationship.currentStatus = "stranger";
        } else if (interactions < 5) {
            relationship.currentStatus = "acquaintance";
        } else if (affinity > 50) {
            relationship.currentStatus = "friend";
        } else if (affinity < -50) {
            relationship.currentStatus = "enemy";
        } else {
            relationship.currentStatus = "known";
        }
    }
}
