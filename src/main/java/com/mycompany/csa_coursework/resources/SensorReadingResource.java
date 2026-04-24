package com.mycompany.csa_coursework.resources;

import com.mycompany.csa_coursework.DataStore;
import com.mycompany.csa_coursework.exceptions.SensorUnavailableException;
import com.mycompany.csa_coursework.models.Sensor;
import com.mycompany.csa_coursework.models.SensorReading;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Produces(MediaType.APPLICATION_JSON)
public class SensorReadingResource {
    
    private String sensorId;

    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    @GET
    public Response getReadings() {
        if (!DataStore.getInstance().getSensors().containsKey(sensorId)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        
        List<SensorReading> readings = DataStore.getInstance().getSensorReadings().getOrDefault(sensorId, new ArrayList<>());
        return Response.ok(readings).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createReading(SensorReading reading) {
        Sensor sensor = DataStore.getInstance().getSensors().get(sensorId);
        
        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        
        if ("MAINTENANCE".equalsIgnoreCase(sensor.getStatus()) || "OFFLINE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException("Sensor is currently disconnected or in maintenance and cannot accept new readings.");
        }
        
        if (reading.getId() == null || reading.getId().isEmpty()) {
            reading.setId(UUID.randomUUID().toString());
        }
        
        if (reading.getTimestamp() == 0) {
            reading.setTimestamp(System.currentTimeMillis());
        }
        
        List<SensorReading> list = DataStore.getInstance().getSensorReadings()
                .computeIfAbsent(sensorId, k -> Collections.synchronizedList(new ArrayList<>()));
        list.add(reading);
        
        sensor.setCurrentValue(reading.getValue());
        
        return Response.status(Response.Status.CREATED).entity(reading).build();
    }
}
