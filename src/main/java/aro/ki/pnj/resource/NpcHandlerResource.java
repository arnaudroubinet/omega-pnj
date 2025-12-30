package aro.ki.pnj.resource;

import aro.ki.pnj.model.HandleRequest;
import aro.ki.pnj.model.HandleResponse;
import aro.ki.pnj.service.NpcHandlerService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.Map;

@Path("/npc/handle")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "NPC Interactions", description = "Handle conversations and actions with NPCs")
public class NpcHandlerResource {

    @Inject
    NpcHandlerService handlerService;

    @POST
    @Operation(
        summary = "Interact with an NPC",
        description = "Send a message (PARLER) or action (ACTION) to an NPC and receive a response"
    )
    public Response handle(@Valid HandleRequest request) {
        try {
            HandleResponse response = handlerService.handle(request);
            return Response.ok(response).build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                          .entity(Map.of("error", e.getMessage()))
                          .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity(Map.of("error", "An error occurred: " + e.getMessage()))
                          .build();
        }
    }
}
