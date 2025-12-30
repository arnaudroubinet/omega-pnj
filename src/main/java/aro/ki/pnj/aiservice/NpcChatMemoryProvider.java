package aro.ki.pnj.aiservice;

import aro.ki.pnj.service.ContextBuilderService;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@ApplicationScoped
public class NpcChatMemoryProvider implements Supplier<ChatMemoryProvider> {

    @Inject
    ContextBuilderService contextBuilder;

    @ConfigProperty(name = "npc.memory.chat-window-size", defaultValue = "20")
    int chatWindowSize;

    private final Map<Object, ChatMemory> memories = new ConcurrentHashMap<>();

    @Override
    public ChatMemoryProvider get() {
        return memoryId -> {
            return memories.computeIfAbsent(memoryId, k -> {
                // Create a MessageWindowChatMemory with configurable window
                return MessageWindowChatMemory.withMaxMessages(chatWindowSize);
            });
        };
    }

    /**
     * Populate memory with historical context
     */
    public void populateMemory(Object memoryId, Long npcId, String characterName) {
        ChatMemory memory = memories.get(memoryId);
        if (memory != null && memory.messages().isEmpty()) {
            // Only populate if memory is empty (first request in session)
            List<ChatMessage> history = contextBuilder.buildConversationHistory(
                npcId, characterName, 10);
            history.forEach(memory::add);
        }
    }

    /**
     * Clear memory for a specific ID
     */
    public void clearMemory(Object memoryId) {
        memories.remove(memoryId);
    }

    /**
     * Clear all memories (useful for testing or memory management)
     */
    public void clearAllMemories() {
        memories.clear();
    }
}
