/*
 * Copyright 2015 Acme Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.acme.ecards;

import com.acme.ecards.support.hk2.HK2Feature;
import com.acme.ecards.support.jackson.Jackson2Feature;
import java.io.IOException;
import static java.lang.Runtime.getRuntime;
import static java.lang.System.exit;
import java.net.URI;
import static java.net.URI.create;
import static javax.ws.rs.core.UriBuilder.fromUri;
import javax.ws.rs.core.UriBuilderException;
import org.glassfish.grizzly.http.server.HttpServer;
import static org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory.createHttpServer;
import org.glassfish.jersey.server.ResourceConfig;
import static org.glassfish.jersey.server.ServerProperties.BV_DISABLE_VALIDATE_ON_EXECUTABLE_OVERRIDE_CHECK;
import static org.glassfish.jersey.server.ServerProperties.BV_SEND_ERROR_IN_RESPONSE;
import static org.glassfish.jersey.server.ServerProperties.RESPONSE_SET_STATUS_OVER_SEND_ERROR;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Sharmarke Aden (saden1)
 */
public class Main {

    private static final Logger LOG = LoggerFactory.getLogger("main");

    // Base URI the Grizzly HTTP server will listen on
    public static final String BASE_URI = "http://localhost:1979";

    boolean isDevMode = false;

    public Main(boolean isDevMode) {
        this.isDevMode = isDevMode;
    }

    public static void main(String[] args) throws Exception {
        new Main(false).startServer(args);
    }

    /**
     * @param args the command line arguments
     */
    public void startServer(String[] args) {
        LOG.info("Configuring Server");
        ResourceConfig config = new ResourceConfig()
                .setApplicationName("eCards")
                .packages("com.acme.ecards.resource")
                .register(HK2Feature.class)
                .register(Jackson2Feature.class)
                .property(BV_SEND_ERROR_IN_RESPONSE, true)
                .property(BV_DISABLE_VALIDATE_ON_EXECUTABLE_OVERRIDE_CHECK, true)
                .property(RESPONSE_SET_STATUS_OVER_SEND_ERROR, true);

        URI uri;

        //if we are in test mode then use the first available port otherwise
        //use the port specified in the base uri.
        if (isDevMode) {
            uri = fromUri(BASE_URI).port(0).build();
        } else {
            uri = create(BASE_URI);
        }

        LOG.info("Creating Server");
        //create an instance of grizzly http server
        final HttpServer server = createHttpServer(uri, config);

        try {
            LOG.info("Starting Server");
            //start the grizzly http server
            server.start();

            //for display purpose get the port used to start the server. this
            //is necessary in case we're using a random port
            int port = server.getListeners().stream().findFirst().get().getPort();
            LOG.info("Server Started at => {}", fromUri(BASE_URI).port(port).build());

            //if we are not in test mode then register a shutdown hook and join
            //the main thread to prevent the server from shutting down.
        }
        catch (IllegalArgumentException | UriBuilderException | IOException e) {
            LOG.error("Server could not be started", e);
            exit(1);
        }

        if (isDevMode) {
            //for dev mode simply shut down the server.
            if (server.isStarted()) {
                server.shutdown();
            }
        } else {
            //for production mode wait for SIGINT signal and let the shutdown
            //hook take care of shutting the server down.

            //create a shutdow hook thread to stop the server.
            Thread shutdownHook = new Thread(() -> {
                LOG.info("Stopping Server");
                if (server.isStarted()) {
                    server.shutdown();
                }
            });

            // register shutdown hook
            getRuntime().addShutdownHook(shutdownHook);
            System.out.println("Press CTRL^C to exit...");

            try {
                Thread.currentThread().join();
            }
            catch (InterruptedException e) {
                //expected exception. NO-OP.
            }
        }

    }

}
