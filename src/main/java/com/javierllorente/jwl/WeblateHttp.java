/*
 * Copyright (C) 2020-2022 Javier Llorente <javier@opensuse.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.javierllorente.jwl;

import jakarta.json.JsonObject;
import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.ServerErrorException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;

/**
 *
 * @author javier
 */
public class WeblateHttp {

    private static final Logger logger = Logger.getLogger(WeblateHttp.class.getName());
    private Client client;
    private WebTarget target;
    private String authToken;
    boolean authenticated;
    private String userAgent;

    public WeblateHttp() {
        authenticated = false;
        userAgent = UserAgent.FULL;
        ClientConfig config = new ClientConfig();
        config.property(ClientProperties.CONNECT_TIMEOUT, 20000);
        config.property(ClientProperties.FOLLOW_REDIRECTS, true);
        client = ClientBuilder.newClient(config)
                .register(new LoggingFeature(logger,
                        Level.INFO,
                        LoggingFeature.Verbosity.HEADERS_ONLY,
                        8192))
                .register(MultiPartFeature.class);
    }

    public WeblateHttp(URI apiURI) {
        this();
        target = client.target(apiURI);
    }    

    public URI getApiURI() {
        return target.getUri();
    }

    public void setApiURI(URI apiURI) {
        target = client.target(apiURI);
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = "Token " + authToken;
    }
    
    public boolean isAuthenticated() {
        return authenticated;
    }

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }
    
    private void handleErrors(final Response response) 
            throws ServerErrorException, ClientErrorException {        
        switch (response.getStatusInfo().getFamily()) {
            case CLIENT_ERROR:
                throw new ClientErrorException(response);
            case SERVER_ERROR:
                throw new ServerErrorException(response);
        }
    }
    
    public void authenticate() 
            throws ClientErrorException, ServerErrorException, ProcessingException {        
        try (Response response = target.request()
                .header("User-Agent", userAgent)
                .header("Authorization", authToken)
                .accept(MediaType.APPLICATION_JSON)
                .get()) {
            logger.info(getConnectionInfo(target.getUri(), "", response.getStatus()));            
            authenticated = (response.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL);
            
            handleErrors(response);
        }
    }
    
    public JsonObject get(String resource, int page)
            throws ClientErrorException, ServerErrorException, ProcessingException {        
        Response response = target
                .path(resource)
                .queryParam("page", page)
                .request()
                .header("User-Agent", userAgent)
                .header("Authorization", authToken)
                .accept(MediaType.APPLICATION_JSON)
                .get();
        logger.info(getConnectionInfo(target.getUri(), resource + "?page=" + page, response.getStatus()));        
        handleErrors(response);
        
        response.bufferEntity();
        return response.readEntity(JsonObject.class);
    }
    
    public JsonObject get(String resource)
            throws ClientErrorException, ServerErrorException, ProcessingException {
        Response response = target
                .path(resource)
                .request()
                .header("User-Agent", userAgent)
                .header("Authorization", authToken)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .get();
        logger.info(getConnectionInfo(target.getUri(), resource, response.getStatus()));
        handleErrors(response);
        
        response.bufferEntity();        
        return response.readEntity(JsonObject.class);        
    }
    
    public String getText(String resource) 
            throws ClientErrorException, ServerErrorException, ProcessingException {
        Response response = target
                .path(resource)
                .request()
                .header("User-Agent", userAgent)
                .header("Authorization", authToken)
                .accept(MediaType.TEXT_PLAIN_TYPE)
                .get();
        logger.info(getConnectionInfo(target.getUri(), resource, response.getStatus()));
        handleErrors(response);
        
        response.bufferEntity();        
        return response.readEntity(String.class);        
    }
       
    public JsonObject post(String resource, String strings) 
            throws ClientErrorException, ServerErrorException, ProcessingException {
        FormDataContentDisposition fdcd = FormDataContentDisposition.name("file")
                .fileName("strings.po").build();

        MultiPart multiPartEntity = new FormDataMultiPart()
                .field("method", "translate")
                .field("conflicts", "replace-translated")
                .bodyPart(new FormDataBodyPart(fdcd, strings.getBytes(), MediaType.TEXT_PLAIN_TYPE));

        Response response = target
                .path(resource)
                .request()
                .header("User-Agent", userAgent)
                .header("Authorization", authToken)
                .accept(MediaType.APPLICATION_JSON)
                .post(Entity.entity(multiPartEntity, multiPartEntity.getMediaType()));
        logger.info(getConnectionInfo(target.getUri(), resource, response.getStatus()));
        handleErrors(response);
                
        response.bufferEntity();
        return response.readEntity(JsonObject.class);
    } 
    
    private String getConnectionInfo(URI uri, String resource, int status) {
        return "URL: " + uri + resource + ", status: " + status;
    }

    void setUserAgent(String userAgent) {
        this.userAgent += " " + userAgent;
    }
    
}
