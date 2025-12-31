package aro.ki.pnj.aiservice;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;

/**
 * AI Service for NPC conversations
 * Uses custom chat memory provider to maintain conversation history per NPC-character pair
 */
@RegisterAiService(
    chatMemoryProviderSupplier = NpcChatMemoryProvider.class
)
public interface NpcChatService {

    /**
     * Handle a conversation (PARLER action)
     * The system message is injected dynamically via the handler service
     *
     * @param memoryId Unique ID for this conversation (format: "npcId:characterName")
     * @param userMessage The player's message
     * @return The NPC's response
     */
    String chat(@MemoryId Object memoryId, @UserMessage String userMessage);

    /**
     * Handle an action (ACTION type)
     * The action description will be formatted with context by the handler service
     *
     * @param memoryId Unique ID for this conversation
     * @param actionDescription Description of what happened (with context)
     * @return The NPC's reaction
     */
    String handleAction(@MemoryId Object memoryId,
                       @UserMessage String actionDescription);
}
