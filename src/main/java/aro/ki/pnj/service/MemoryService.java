package aro.ki.pnj.service;

import aro.ki.pnj.entity.Interaction;
import aro.ki.pnj.entity.Memory;
import aro.ki.pnj.entity.Npc;
import aro.ki.pnj.model.InteractionType;
import aro.ki.pnj.repository.MemoryRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;

@ApplicationScoped
public class MemoryService {

    @Inject
    MemoryRepository memoryRepository;

    /**
     * Determines if an interaction should become a memory
     */
    public boolean isMemoryWorthy(String input, String response, InteractionType type) {
        // Actions are always memory-worthy
        if (type == InteractionType.ACTION) {
            return true;
        }

        // For conversations, use heuristics:
        // - Length (longer conversations are more important)
        // - Keywords (quest, gift, attack, help, etc.)
        String combined = (input + " " + (response != null ? response : "")).toLowerCase();

        return combined.length() > 200 ||
               combined.contains("quest") ||
               combined.contains("quête") ||
               combined.contains("help") ||
               combined.contains("aide") ||
               combined.contains("gift") ||
               combined.contains("cadeau") ||
               combined.contains("attack") ||
               combined.contains("attaque") ||
               combined.contains("betray") ||
               combined.contains("trahir") ||
               combined.contains("promise") ||
               combined.contains("promesse");
    }

    /**
     * Creates a memory from an interaction
     */
    @Transactional
    public Memory createMemory(Npc npc, String characterName, Interaction interaction) {
        String memoryText = summarizeInteraction(interaction, characterName);

        Memory memory = new Memory();
        memory.npc = npc;
        memory.characterName = characterName;
        memory.memoryText = memoryText;
        memory.importance = calculateImportance(interaction);
        memory.createdAt = LocalDateTime.now();
        memory.sourceInteraction = interaction;
        memory.emotionalContext = extractEmotion(interaction.response);
        memory.persist();

        return memory;
    }

    private String summarizeInteraction(Interaction interaction, String characterName) {
        if (interaction.type == InteractionType.ACTION) {
            return interaction.input;  // Actions are already concise
        }
        // Simple summarization - could be enhanced with AI
        String input = truncate(interaction.input, 80);
        return characterName + " said: " + input;
    }

    private int calculateImportance(Interaction interaction) {
        if (interaction.type == InteractionType.ACTION) {
            return 8;  // Actions are highly important
        }
        // Simple heuristic based on length and keywords
        int importance = 5;
        String combined = (interaction.input + " " +
                          (interaction.response != null ? interaction.response : "")).toLowerCase();

        if (combined.contains("quest") || combined.contains("quête")) importance += 2;
        if (combined.contains("gift") || combined.contains("cadeau")) importance += 1;
        if (combined.contains("attack") || combined.contains("betray") ||
            combined.contains("attaque") || combined.contains("trahir")) importance += 3;

        return Math.min(importance, 10);
    }

    public String extractEmotion(String response) {
        if (response == null) return "neutral";

        // Simple keyword-based emotion detection
        String lower = response.toLowerCase();
        if (lower.contains("angry") || lower.contains("furious") ||
            lower.contains("colère") || lower.contains("furieux")) return "anger";
        if (lower.contains("happy") || lower.contains("glad") ||
            lower.contains("heureux") || lower.contains("content")) return "joy";
        if (lower.contains("sad") || lower.contains("sorry") ||
            lower.contains("triste") || lower.contains("désolé")) return "sadness";
        if (lower.contains("afraid") || lower.contains("scared") ||
            lower.contains("peur") || lower.contains("effrayé")) return "fear";
        if (lower.contains("thank") || lower.contains("grateful") ||
            lower.contains("merci") || lower.contains("reconnaissant")) return "gratitude";

        return "neutral";
    }

    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        return text.length() <= maxLength ? text : text.substring(0, maxLength) + "...";
    }
}
