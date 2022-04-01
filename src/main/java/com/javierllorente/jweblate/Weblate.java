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
package com.javierllorente.jweblate;

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

    @Override
    public URI getApiUrl() {
        return http.getApiURI();
    }

    @Override
    public void setApiUrl(URI apiUri) {
        http.setApiURI(apiUri);
    }
    
    @Override
   public String getAuthToken() {
       return http.getAuthToken();
   }
   
    @Override
   public void setAuthToken(String authToken) {
       http.setAuthToken(authToken);
   }

    @Override
    public boolean isAuthenticated() {
        return http.isAuthenticated();
    }
    
    @Override
    public void authenticate()
            throws ClientErrorException, ServerErrorException, ProcessingException {
        http.authenticate();
    }
    
    @Override
    public void logout() {
        http.setAuthToken("");
        http.setAuthenticated(false);
    }
    
    @Override
    public void setUserAgent(String userAgent) {
        http.setUserAgent(userAgent);
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

    @Override
    public List<String> getProjects() 
            throws ClientErrorException, ServerErrorException, ProcessingException {
        return getElements("projects/", "slug");
    }

    @Override
    public List<String> getComponents(String project) 
            throws ClientErrorException, ServerErrorException, ProcessingException {
        return getElements("projects/" + project + "/components/", "slug");
    }

    @Override
    public List<String> getTranslations(String project, String component)
            throws ClientErrorException, ServerErrorException, ProcessingException {
        return getElements("components/" + project + "/" + component + "/translations/", 
                "language_code");
    }
    
    @Override
    public String getFileFormat(String project, String component, String language)
            throws ClientErrorException, ServerErrorException, ProcessingException {
        String resource = "translations/" + project + "/" + component
                + "/" + language + "/";
        JsonObject jsonObject = http.get(resource);
        
        JsonObject componentObject = jsonObject.getJsonObject("component");
        return componentObject.getString("file_format");
    }

    @Override
    public String getFile(String project, String component, String language)
            throws ClientErrorException, ServerErrorException, ProcessingException {
        String resource = "translations/" + project + "/" + component
                + "/" + language + "/file/";
        return http.getText(resource);
    }

    @Override
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
