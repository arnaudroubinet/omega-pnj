package aro.ki.pnj.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class HandleRequest {

    @NotBlank(message = "npcId is required")
    public String npcId;

    @NotBlank(message = "characterName is required")
    public String characterName;

    @NotNull(message = "actionType is required")
    public InteractionType actionType;

    @NotBlank(message = "message is required")
    public String message;

    public String context;  // Optional: location, current state, etc.
}
