package aro.ki.pnj.resource;

import aro.ki.pnj.entity.Memory;
import aro.ki.pnj.repository.MemoryRepository;
import aro.ki.pnj.repository.NpcRepository;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;
import java.util.Map;

@Path("/npc/memories")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "NPC Memories", description = "Manage NPC memories and important interactions")
public class MemoryResource {

    @Inject
    MemoryRepository memoryRepository;

    @Inject
    NpcRepository npcRepository;

    @GET
    @Path("/{npcId}")
    @Operation(
        summary = "Get all memories for an NPC",
        description = "Retrieve all stored memories for a specific NPC"
    )
    public Response getAllMemoriesForNpc(@PathParam("npcId") String npcId) {
        return npcRepository.findByNpcId(npcId)
            .map(npc -> {
                List<Memory> memories = memoryRepository.findByNpcId(npc.id);
                return Response.ok(memories).build();
            })
            .orElse(Response.status(Response.Status.NOT_FOUND)
                .entity(Map.of("error", "NPC not found: " + npcId))
                .build());
    }

    @GET
    @Path("/{npcId}/character/{characterName}")
    @Operation(
        summary = "Get memories about a specific character",
        description = "Retrieve all memories an NPC has about a specific character"
    )
    public Response getMemoriesAboutCharacter(
        @PathParam("npcId") String npcId,
        @PathParam("characterName") String characterName
    ) {
        return npcRepository.findByNpcId(npcId)
            .map(npc -> {
                List<Memory> memories = memoryRepository.findByNpcAndCharacter(npc.id, characterName);
                return Response.ok(memories).build();
            })
            .orElse(Response.status(Response.Status.NOT_FOUND)
                .entity(Map.of("error", "NPC not found: " + npcId))
                .build());
    }

    @GET
    @Path("/{npcId}/top/{limit}")
    @Operation(
        summary = "Get top memories for an NPC",
        description = "Retrieve the most important memories for an NPC, limited by count"
    )
    public Response getTopMemories(
        @PathParam("npcId") String npcId,
        @PathParam("limit") int limit
    ) {
        return npcRepository.findByNpcId(npcId)
            .map(npc -> {
                List<Memory> memories = memoryRepository.findTopMemories(npc.id, limit);
                return Response.ok(memories).build();
            })
            .orElse(Response.status(Response.Status.NOT_FOUND)
                .entity(Map.of("error", "NPC not found: " + npcId))
                .build());
    }

    @GET
    @Path("/memory/{memoryId}")
    @Operation(
        summary = "Get a specific memory",
        description = "Retrieve a single memory by its ID"
    )
    public Response getMemory(@PathParam("memoryId") Long memoryId) {
        Memory memory = memoryRepository.findById(memoryId);
        if (memory != null) {
            return Response.ok(memory).build();
        }
        return Response.status(Response.Status.NOT_FOUND)
            .entity(Map.of("error", "Memory not found: " + memoryId))
            .build();
    }

    @PUT
    @Path("/memory/{memoryId}")
    @Transactional
    @Operation(
        summary = "Update a memory",
        description = "Edit the text or emotional context of an existing memory"
    )
    public Response updateMemory(
        @PathParam("memoryId") Long memoryId,
        Map<String, String> updates
    ) {
        Memory memory = memoryRepository.findById(memoryId);
        if (memory == null) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(Map.of("error", "Memory not found: " + memoryId))
                .build();
        }

        if (updates.containsKey("memoryText")) {
            memory.memoryText = updates.get("memoryText");
        }
        if (updates.containsKey("emotionalContext")) {
            memory.emotionalContext = updates.get("emotionalContext");
        }

        memory.persist();
        return Response.ok(memory).build();
    }

    @DELETE
    @Path("/memory/{memoryId}")
    @Transactional
    @Operation(
        summary = "Delete a memory",
        description = "Remove a specific memory from the NPC's memory bank"
    )
    public Response deleteMemory(@PathParam("memoryId") Long memoryId) {
        Memory memory = memoryRepository.findById(memoryId);
        if (memory == null) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(Map.of("error", "Memory not found: " + memoryId))
                .build();
        }

        memory.delete();
        return Response.ok(Map.of("message", "Memory deleted successfully")).build();
    }

    @POST
    @Path("/{npcId}")
    @Transactional
    @Operation(
        summary = "Create a manual memory",
        description = "Manually create a memory for an NPC (useful for GM/admin)"
    )
    public Response createManualMemory(
        @PathParam("npcId") String npcId,
        Map<String, String> memoryData
    ) {
        return npcRepository.findByNpcId(npcId)
            .map(npc -> {
                Memory memory = new Memory();
                memory.npc = npc;
                memory.characterName = memoryData.get("characterName");
                memory.memoryText = memoryData.get("memoryText");
                memory.emotionalContext = memoryData.get("emotionalContext");
                memory.importance = 5; // Default importance for manual memories

                memory.persist();
                return Response.ok(memory).build();
            })
            .orElse(Response.status(Response.Status.NOT_FOUND)
                .entity(Map.of("error", "NPC not found: " + npcId))
                .build());
    }
}
