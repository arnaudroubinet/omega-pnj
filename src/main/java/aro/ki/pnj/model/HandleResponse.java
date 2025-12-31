package aro.ki.pnj.model;

import java.time.LocalDateTime;

public class HandleResponse {

    public String npcResponse;
    public String thinking;  // Chain of Thought - pensée interne du PNJ
    public String emotionalState;
    public Integer relationshipChange;
    public LocalDateTime timestamp;

    public HandleResponse() {
    }

    public HandleResponse(String npcResponse, String thinking, String emotionalState, Integer relationshipChange, LocalDateTime timestamp) {
        this.npcResponse = npcResponse;
        this.thinking = thinking;
        this.emotionalState = emotionalState;
        this.relationshipChange = relationshipChange;
        this.timestamp = timestamp;
    }
}
