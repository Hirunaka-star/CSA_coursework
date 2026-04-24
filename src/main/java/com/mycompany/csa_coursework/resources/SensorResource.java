package com.mycompany.csa_coursework.resources;

import com.mycompany.csa_coursework.DataStore;
import com.mycompany.csa_coursework.exceptions.LinkedResourceNotFoundException;
import com.mycompany.csa_coursework.models.Room;
import com.mycompany.csa_coursework.models.Sensor;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Path("/sensors")
public class SensorResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSensors(@QueryParam("type") String targetType) {
        List<Sensor> sensors = new ArrayList<>(DataStore.getInstance().getSensors().values());
        
        if (targetType != null && !targetType.isEmpty()) {
            sensors = sensors.stream()
                             .filter(s -> targetType.equalsIgnoreCase(s.getType()))
                             .collect(Collectors.toList());
        }
        
        return Response.ok(sensors).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createSensor(Sensor sensor) {
        if (sensor.getRoomId() == null || !DataStore.getInstance().getRooms().containsKey(sensor.getRoomId())) {
            throw new LinkedResourceNotFoundException("The specified room ID does not exist.");
        }
        
        if (sensor.getId() == null || sensor.getId().isEmpty()) {
            sensor.setId(UUID.randomUUID().toString());
        }
        
        DataStore.getInstance().getSensors().put(sensor.getId(), sensor);
        
        Room room = DataStore.getInstance().getRooms().get(sensor.getRoomId());
        room.getSensorIds().add(sensor.getId());
        
        return Response.status(Response.Status.CREATED).entity(sensor).build();
    }

    @Path("/{sensorId}/readings")
    public SensorReadingResource getReadings(@PathParam("sensorId") String sensorId) {
        return new SensorReadingResource(sensorId);
    }
}
