/*
 * Copyright (C) 2020-2022 Javier Llorente <javier@opensuse.org>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.javierllorente.jwl;

import jakarta.json.JsonObject;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.AuthenticationException;
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
    private String username;
    private String password;
    private String authToken;
    boolean authenticated;

    public WeblateHttp() {
        authenticated = false;
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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
    
    public void authenticate() throws AuthenticationException, IOException {
        try (Response response = target.request()
                .header("User-Agent", UserAgent.FULL)
                .header("Authorization", authToken)
                .accept(MediaType.APPLICATION_JSON)
                .get()) {
            logger.info(getConnectionInfo(target.getUri(), "", response.getStatus()));
            
            authenticated = (response.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL);
            if (!authenticated) {
                throw new AuthenticationException(Integer.toString(response.getStatus()));
            }
        }
    }
    
    public JsonObject get(String resource, int page) throws IOException {
        Response response = target
                .path(resource)
                .queryParam("page", page)
                .request()
                .header("User-Agent", UserAgent.FULL)
                .header("Authorization", authToken)
                .accept(MediaType.APPLICATION_JSON)
                .get();
        logger.info(getConnectionInfo(target.getUri(), resource + "?page=" + page, response.getStatus()));
        
        if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
            throw new IOException(Integer.toString(response.getStatus()));
        }        
        response.bufferEntity();
        return response.readEntity(JsonObject.class);
    }
    
    public JsonObject get(String resource) throws IOException {
        Response response = target
                .path(resource)
                .request()
                .header("User-Agent", UserAgent.FULL)
                .header("Authorization", authToken)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .get();
        logger.info(getConnectionInfo(target.getUri(), resource, response.getStatus()));
        
        if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
            throw new IOException(Integer.toString(response.getStatus()));
        }
        response.bufferEntity();        
        return response.readEntity(JsonObject.class);        
    }
    
    public String getText(String resource) throws IOException {
        Response response = target
                .path(resource)
                .request()
                .header("User-Agent", UserAgent.FULL)
                .header("Authorization", authToken)
                .accept(MediaType.TEXT_PLAIN_TYPE)
                .get();
        logger.info(getConnectionInfo(target.getUri(), resource, response.getStatus()));
        
        if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
            throw new IOException(Integer.toString(response.getStatus()));
        }
        response.bufferEntity();        
        return response.readEntity(String.class);        
    }
       
    public JsonObject post(String resource, String strings) throws IOException {        
        FormDataContentDisposition fdcd = FormDataContentDisposition.name("file")
                .fileName("strings.po").build();

        MultiPart multiPartEntity = new FormDataMultiPart()
                .field("method", "translate")
                .field("conflicts", "replace-translated")
                .bodyPart(new FormDataBodyPart(fdcd, strings.getBytes(), MediaType.TEXT_PLAIN_TYPE));

        Response response = target
                .path(resource)
                .request()
                .header("User-Agent", UserAgent.FULL)
                .header("Authorization", authToken)
                .accept(MediaType.APPLICATION_JSON)
                .post(Entity.entity(multiPartEntity, multiPartEntity.getMediaType()));
        logger.info(getConnectionInfo(target.getUri(), resource, response.getStatus()));
                
        response.bufferEntity();
        return response.readEntity(JsonObject.class);
    } 
    
    private String getConnectionInfo(URI uri, String resource, int status) {
        return "URL: " + uri + resource + ", status: " + status;
    }
    
}
