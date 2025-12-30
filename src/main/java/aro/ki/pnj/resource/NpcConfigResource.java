package aro.ki.pnj.resource;

import aro.ki.pnj.entity.Npc;
import aro.ki.pnj.model.NpcConfigRequest;
import aro.ki.pnj.model.NpcConfigResponse;
import aro.ki.pnj.service.NpcConfigService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;
import java.util.stream.Collectors;

@Path("/npc/config")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "NPC Configuration", description = "Manage NPC personalities and configurations")
public class NpcConfigResource {

    @Inject
    NpcConfigService configService;

    @POST
    @Operation(summary = "Create or update an NPC", description = "Creates a new NPC or updates an existing one")
    public Response createOrUpdate(@Valid NpcConfigRequest request) {
        Npc npc = configService.createOrUpdateNpc(request);
        NpcConfigResponse response = NpcConfigResponse.from(npc);
        return Response.ok(response).build();
    }

    @GET
    @Operation(summary = "Get all NPCs", description = "Returns a list of all configured NPCs")
    public Response getAll() {
        List<NpcConfigResponse> npcs = configService.getAllNpcs()
            .stream()
            .map(NpcConfigResponse::from)
            .collect(Collectors.toList());
        return Response.ok(npcs).build();
    }

    @GET
    @Path("/{npcId}")
    @Operation(summary = "Get one NPC", description = "Returns a single NPC by its ID")
    public Response getOne(@PathParam("npcId") String npcId) {
        return configService.getNpc(npcId)
            .map(npc -> Response.ok(NpcConfigResponse.from(npc)).build())
            .orElse(Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"NPC not found: " + npcId + "\"}")
                    .build());
    }

    @DELETE
    @Path("/{npcId}")
    @Operation(summary = "Delete an NPC", description = "Deletes an NPC and all associated data")
    public Response delete(@PathParam("npcId") String npcId) {
        boolean deleted = configService.deleteNpc(npcId);
        if (deleted) {
            return Response.noContent().build();
        } else {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"NPC not found: " + npcId + "\"}")
                    .build();
        }
    }
}
