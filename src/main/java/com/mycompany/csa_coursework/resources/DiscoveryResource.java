package com.mycompany.csa_coursework.resources;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

@Path("/")
public class DiscoveryResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDiscoveryInfo() {
        Map<String, Object> discoveryInfo = new HashMap<>();
        discoveryInfo.put("version", "v1");
        discoveryInfo.put("contact", "admin@smartcampus.university.edu");
        
        Map<String, String> endpoints = new HashMap<>();
        endpoints.put("rooms", "/api/v1/rooms");
        endpoints.put("sensors", "/api/v1/sensors");
        discoveryInfo.put("endpoints", endpoints);
        
        return Response.ok(discoveryInfo).build();
    }
}
