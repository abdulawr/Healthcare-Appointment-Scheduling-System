package com.basit.cz.controller;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("api/patient/")
public class PatientController {

    @GET
    @Path("register")
    @Produces(MediaType.TEXT_PLAIN)
    public String registerPatient() {
        return "The service is working";
    }
}
