package com.basit.cz.notification.api;

import com.basit.cz.notification.service.NotificationService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;

@Path("/notifications")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class NotificationResource {

    @Inject
    NotificationService notificationService;

    @POST
    public Response create(CreateNotificationRequest request) {
        NotificationResponse resp = notificationService.createAndSend(request);
        return Response.status(Response.Status.ACCEPTED).entity(resp).build();
    }

    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") UUID id) {
        NotificationResponse resp = notificationService.getById(id);
        if (resp == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(resp).build();
    }

    @GET
    @Path("/by-user/{userId}")
    public List<NotificationResponse> getByUser(@PathParam("userId") String userId,
                                                @QueryParam("limit") @DefaultValue("50") int limit) {
        return notificationService.getByUserId(userId, limit);
    }
}
