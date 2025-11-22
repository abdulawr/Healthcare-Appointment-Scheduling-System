package com.basit.cz.notification.novu;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/v1/events")
@RegisterRestClient(configKey = "novu-api")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface NovuClient {

    @POST
    @Path("/trigger")
    NovuTriggerResponse triggerEvent(
            @HeaderParam("Authorization") String apiKey,
            NovuTriggerRequest request
    );
}
