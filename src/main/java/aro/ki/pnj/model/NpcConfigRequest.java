package aro.ki.pnj.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class NpcConfigRequest {

    @NotBlank(message = "npcId is required")
    @Size(max = 100, message = "npcId must be less than 100 characters")
    public String npcId;

    @NotBlank(message = "name is required")
    @Size(max = 200, message = "name must be less than 200 characters")
    public String name;

    @Size(max = 2000, message = "backstory must be less than 2000 characters")
    public String backstory;

    @Size(max = 1000, message = "personality must be less than 1000 characters")
    public String personality;

    @Size(max = 500, message = "occupation must be less than 500 characters")
    public String occupation;

    @Size(max = 500, message = "goals must be less than 500 characters")
    public String goals;

    @Size(max = 500, message = "fears must be less than 500 characters")
    public String fears;
}
