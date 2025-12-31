package aro.ki.pnj.service;

import aro.ki.pnj.entity.Interaction;
import aro.ki.pnj.entity.Memory;
import aro.ki.pnj.entity.Npc;
import aro.ki.pnj.model.InteractionType;
import aro.ki.pnj.repository.InteractionRepository;
import aro.ki.pnj.repository.MemoryRepository;
import aro.ki.pnj.repository.RelationshipRepository;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class ContextBuilderService {

    @Inject
    MemoryRepository memoryRepository;

    @Inject
    InteractionRepository interactionRepository;

    @Inject
    RelationshipRepository relationshipRepository;

    @Inject
    ChainOfThoughtParser cotParser;

    @ConfigProperty(name = "npc.memory.top-memories-limit", defaultValue = "5")
    int topMemoriesLimit;

    @ConfigProperty(name = "npc.memory.conversation-history-limit", defaultValue = "10")
    int conversationHistoryLimit;

    /**
     * Builds the complete system message including NPC role + relevant memories
     */
    public String buildSystemMessage(Npc npc, String characterName) {
        StringBuilder systemMessage = new StringBuilder();
        systemMessage.append("You are ").append(npc.name).append(".\n\n");

        // Add backstory if present
        if (npc.backstory != null && !npc.backstory.isBlank()) {
            systemMessage.append("BACKSTORY:\n").append(npc.backstory).append("\n\n");
        }

        // Add personality if present
        if (npc.personality != null && !npc.personality.isBlank()) {
            systemMessage.append("PERSONALITY:\n").append(npc.personality).append("\n\n");
        }

        // Add occupation, goals, fears
        if (npc.occupation != null && !npc.occupation.isBlank()) {
            systemMessage.append("OCCUPATION: ").append(npc.occupation).append("\n");
        }
        if (npc.goals != null && !npc.goals.isBlank()) {
            systemMessage.append("GOALS: ").append(npc.goals).append("\n");
        }
        if (npc.fears != null && !npc.fears.isBlank()) {
            systemMessage.append("FEARS: ").append(npc.fears).append("\n");
        }
        systemMessage.append("\n");

        // Relationship context
        relationshipRepository.findByNpcAndCharacter(npc.id, characterName)
            .ifPresent(rel -> {
                systemMessage.append("YOUR RELATIONSHIP WITH ").append(characterName).append(":\n");
                systemMessage.append("Status: ").append(rel.currentStatus).append("\n");
                systemMessage.append("Affinity: ").append(rel.affinity).append("/100\n");
                systemMessage.append("Trust: ").append(rel.trustLevel).append("/100\n");
                systemMessage.append("You've met them ").append(rel.totalInteractions)
                            .append(" time").append(rel.totalInteractions == 1 ? "" : "s").append(".\n\n");
            });

        // Important memories
        List<Memory> memories = memoryRepository.findTopMemoriesForCharacter(
            npc.id, characterName, topMemoriesLimit);
        if (!memories.isEmpty()) {
            systemMessage.append("IMPORTANT MEMORIES ABOUT ").append(characterName).append(":\n");
            for (Memory mem : memories) {
                systemMessage.append("- ").append(mem.memoryText);
                if (mem.emotionalContext != null && !mem.emotionalContext.isBlank()) {
                    systemMessage.append(" (").append(mem.emotionalContext).append(")");
                }
                systemMessage.append("\n");
            }
            systemMessage.append("\n");
        }

        // Instructions
        systemMessage.append("INSTRUCTIONS:\n");
        systemMessage.append("- Respond in character based on your personality and backstory\n");
        systemMessage.append("- Remember your relationship with ").append(characterName).append("\n");
        systemMessage.append("- Reference past events when relevant\n");
        systemMessage.append("- Keep responses concise (2-3 sentences typically)\n");
        systemMessage.append("- Show emotions appropriate to the situation\n");
        systemMessage.append("- Use the same language as ").append(characterName).append("\n");

        // Chain of Thought instructions
        systemMessage.append(cotParser.buildCoTInstructions());

        return systemMessage.toString();
    }

    /**
     * Builds conversation history for chat memory
     */
    public List<ChatMessage> buildConversationHistory(Long npcId, String characterName, int limit) {
        List<Interaction> recentInteractions = interactionRepository
            .findRecentByNpcAndCharacter(npcId, characterName, limit);

        List<ChatMessage> messages = new ArrayList<>();

        for (Interaction interaction : recentInteractions) {
            if (interaction.type == InteractionType.PARLER) {
                // User message
                messages.add(new UserMessage(interaction.input));
                // AI response
                if (interaction.response != null && !interaction.response.isBlank()) {
                    messages.add(new AiMessage(interaction.response));
                }
            } else {
                // For actions, format as system context
                messages.add(new SystemMessage("EVENT: " + interaction.input));
                if (interaction.response != null && !interaction.response.isBlank()) {
                    messages.add(new AiMessage(interaction.response));
                }
            }
        }

        return messages;
    }
}
