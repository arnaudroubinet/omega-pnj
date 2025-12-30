package aro.ki.pnj.model;

import aro.ki.pnj.entity.Npc;

import java.time.LocalDateTime;

public class NpcConfigResponse {

    public Long id;
    public String npcId;
    public String name;
    public String backstory;
    public String personality;
    public String occupation;
    public String goals;
    public String fears;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;

    public static NpcConfigResponse from(Npc npc) {
        NpcConfigResponse response = new NpcConfigResponse();
        response.id = npc.id;
        response.npcId = npc.npcId;
        response.name = npc.name;
        response.backstory = npc.backstory;
        response.personality = npc.personality;
        response.occupation = npc.occupation;
        response.goals = npc.goals;
        response.fears = npc.fears;
        response.createdAt = npc.createdAt;
        response.updatedAt = npc.updatedAt;
        return response;
    }
}
