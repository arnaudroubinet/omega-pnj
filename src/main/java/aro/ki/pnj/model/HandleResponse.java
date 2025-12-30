package aro.ki.pnj.model;

import java.time.LocalDateTime;

public class HandleResponse {

    public String npcResponse;
    public String emotionalState;
    public Integer relationshipChange;
    public LocalDateTime timestamp;

    public HandleResponse() {
    }

    public HandleResponse(String npcResponse, String emotionalState, Integer relationshipChange, LocalDateTime timestamp) {
        this.npcResponse = npcResponse;
        this.emotionalState = emotionalState;
        this.relationshipChange = relationshipChange;
        this.timestamp = timestamp;
    }
}
