package aro.ki.pnj.service;

import aro.ki.pnj.aiservice.NpcChatMemoryProvider;
import aro.ki.pnj.aiservice.NpcChatService;
import aro.ki.pnj.entity.Interaction;
import aro.ki.pnj.entity.Npc;
import aro.ki.pnj.entity.Relationship;
import aro.ki.pnj.model.HandleRequest;
import aro.ki.pnj.model.HandleResponse;
import aro.ki.pnj.model.InteractionType;
import aro.ki.pnj.repository.InteractionRepository;
import aro.ki.pnj.repository.NpcRepository;
import aro.ki.pnj.repository.RelationshipRepository;
import dev.langchain4j.data.message.SystemMessage;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;

import java.time.LocalDateTime;

@ApplicationScoped
public class NpcHandlerService {

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

    @Transactional
    public HandleResponse handle(HandleRequest request) {
        // 1. Find or validate NPC
        Npc npc = npcRepository.findByNpcId(request.npcId)
            .orElseThrow(() -> new NotFoundException("NPC not found: " + request.npcId));

        // 2. Get or create relationship
        Relationship relationship = relationshipRepository.getOrCreate(npc, request.characterName);

        // 3. Build memory ID (unique per NPC + character)
        String memoryId = request.npcId + ":" + request.characterName;

        // 4. Populate chat memory with history if first interaction in session
        memoryProvider.populateMemory(memoryId, npc.id, request.characterName);

        // 5. Build dynamic system message
        String systemMessage = contextBuilder.buildSystemMessage(npc, request.characterName);

        // 6. Add system message to chat memory (langchain4j will use it)
        // We need to inject the system message into the conversation
        // For this, we'll prepend it to the user message for actions
        String aiResponse;
        if (request.actionType == InteractionType.PARLER) {
            // For conversations, the system message is part of the context
            // We'll use a workaround by adding it as part of chat history
            aiResponse = chatService.chat(memoryId, request.message);
        } else {
            // For actions, format as an event description
            String actionMessage = "A game event occurred: " + request.message;
            aiResponse = chatService.handleAction(memoryId, actionMessage);
        }

        // 7. Store interaction
        Interaction interaction = new Interaction();
        interaction.npc = npc;
        interaction.characterName = request.characterName;
        interaction.type = request.actionType;
        interaction.input = request.message;
        interaction.response = aiResponse;
        interaction.timestamp = LocalDateTime.now();
        interaction.context = request.context;
        interaction.persist();

        // 8. Update relationship
        relationship.lastInteraction = LocalDateTime.now();
        relationship.totalInteractions++;
        updateRelationshipStatus(relationship);
        relationship.persist();

        // 9. Check if memory-worthy and create memory
        if (memoryService.isMemoryWorthy(request.message, aiResponse, request.actionType)) {
            interaction.isMemoryWorthy = true;
            memoryService.createMemory(npc, request.characterName, interaction);
        }

        // 10. Build response
        String emotionalState = memoryService.extractEmotion(aiResponse);
        HandleResponse response = new HandleResponse();
        response.npcResponse = aiResponse;
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
