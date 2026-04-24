package com.mycompany.csa_coursework;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    
    public static final String BASE_URI = "http://localhost:8080/";

    public static HttpServer startServer() {
        final ResourceConfig rc = ResourceConfig.forApplicationClass(JakartaRestConfiguration.class)
                                                .packages("com.mycompany.csa_coursework");
                                                
        System.out.println("Starting Embedded GlassFish/Grizzly Server...");
        return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc);
    }

    public static void main(String[] args) {
        try {
            final HttpServer server = startServer();
            System.out.println(String.format("Jersey Embedded app started!\nThe API is running at: "
                    + "%sapi/v1/\nHit Ctrl-C to stop it...", BASE_URI));
                    
            Thread.currentThread().join();
        } catch (InterruptedException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
