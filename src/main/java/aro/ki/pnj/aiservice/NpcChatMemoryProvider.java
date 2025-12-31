package aro.ki.pnj.aiservice;

import aro.ki.pnj.entity.Npc;
import aro.ki.pnj.repository.NpcRepository;
import aro.ki.pnj.service.ContextBuilderService;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@ApplicationScoped
public class NpcChatMemoryProvider implements Supplier<ChatMemoryProvider> {

    private static final Logger log = LoggerFactory.getLogger(NpcChatMemoryProvider.class);

    @Inject
    ContextBuilderService contextBuilder;

    @Inject
    NpcRepository npcRepository;

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
     * Populate memory with historical context and system message
     */
    public void populateMemory(Object memoryId, Long npcId, String characterName) {
        ChatMemory memory = memories.get(memoryId);
        if (memory != null && memory.messages().isEmpty()) {
            // Only populate if memory is empty (first request in session)

            // 1. Add system message with NPC context
            Npc npc = npcRepository.findById(npcId);
            if (npc != null) {
                String systemMessageText = contextBuilder.buildSystemMessage(npc, characterName);
                memory.add(new SystemMessage(systemMessageText));
                log.debug("System message added to memory for {}: {} chars", memoryId, systemMessageText.length());
            }

            // 2. Add conversation history
            List<ChatMessage> history = contextBuilder.buildConversationHistory(
                npcId, characterName, 10);
            history.forEach(memory::add);
            log.debug("Loaded {} historical messages for {}", history.size(), memoryId);
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
