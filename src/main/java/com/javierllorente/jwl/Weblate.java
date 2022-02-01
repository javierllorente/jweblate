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

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.ServerErrorException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author javier
 */
public class Weblate implements TranslationProvider {

    private static final Logger logger = Logger.getLogger(Weblate.class.getName());
    private final WeblateHttp http;

    public Weblate() {
        http = new WeblateHttp();
    }

    public URI getApiUrl() {
        return http.getApiURI();
    }

    public void setApiUrl(URI apiUri) {
        http.setApiURI(apiUri);
    }
    
   public String getAuthToken() {
       return http.getAuthToken();
   }
   
   public void setAuthToken(String authToken) {
       http.setAuthToken(authToken);
   }

    public boolean isAuthenticated() {
        return http.isAuthenticated();
    }
    
    public void authenticate()
            throws ClientErrorException, ServerErrorException, ProcessingException {
        http.authenticate();
    }
    
    public void logout() {
        http.setAuthToken("");
        http.setAuthenticated(false);
    }
    
    private void get(String resource, String path, int page, List<String> elements) 
            throws ClientErrorException, ServerErrorException, ProcessingException {
        JsonObject jsonObject = http.get(resource, page);
        
        JsonArray results = jsonObject.getJsonArray("results");        
        for (JsonValue value : results) {
            elements.add(value.asJsonObject().getString(path));
            System.out.println(value.asJsonObject().getString(path));
        }
        
        if (jsonObject.containsKey("next") && !jsonObject.isNull("next")) {
            get(resource, path, ++page, elements);
        }        
    }
    
    private List<String> getElements(String resource, String path) 
            throws ClientErrorException, ServerErrorException, ProcessingException {
        List<String> elements = new ArrayList<>();
        get(resource, path, 1, elements);
        return elements;
    }

    public List<String> getProjects() 
            throws ClientErrorException, ServerErrorException, ProcessingException {
        return getElements("projects/", "slug");
    }

    public List<String> getComponents(String project) 
            throws ClientErrorException, ServerErrorException, ProcessingException {
        return getElements("projects/" + project + "/components/", "slug");
    }

    public List<String> getTranslations(String project, String component)
            throws ClientErrorException, ServerErrorException, ProcessingException {
        return getElements("components/" + project + "/" + component + "/translations/", 
                "language_code");
    }
    
    public String getFileFormat(String project, String component, String language)
            throws ClientErrorException, ServerErrorException, ProcessingException {
        String resource = "translations/" + project + "/" + component
                + "/" + language + "/";
        JsonObject jsonObject = http.get(resource);
        
        JsonObject componentObject = jsonObject.getJsonObject("component");
        return componentObject.getString("file_format");
    }

    public String getFile(String project, String component, String language)
            throws ClientErrorException, ServerErrorException, ProcessingException {
        String resource = "translations/" + project + "/" + component
                + "/" + language + "/file/";
        return http.getText(resource);
    }

    public Map<String, String> submit(String project, String component, String language, String file)
            throws ClientErrorException, ServerErrorException, ProcessingException {
        String resource = "translations/" + project + "/" + component
                + "/" + language + "/file/";
        JsonObject jsonObject = http.post(resource, file);
        logger.log(Level.INFO, "Response: {0}", jsonObject);
        
        if (!jsonObject.containsKey("result") || jsonObject.isNull("result")) {
            throw new ProcessingException("JsonObject does not contain 'result' key/key is null");
        }

        Map<String, String> result = new HashMap<>();
        for (Entry<String, JsonValue> entry : jsonObject.entrySet()) {
            result.put(entry.getKey(), entry.getValue().toString());            
        }
        
        return result;
    }
    
}
