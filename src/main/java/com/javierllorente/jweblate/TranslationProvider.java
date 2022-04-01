/*
 * Copyright (C) 2022 Javier Llorente <javier@opensuse.org>
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

import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.ServerErrorException;
import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 *
 * @author javier
 */
public interface TranslationProvider {

    public URI getApiUrl();

    public void setApiUrl(URI apiUri);

    public String getAuthToken();

    public void setAuthToken(String authToken);

    public boolean isAuthenticated();

    public void authenticate()
            throws ClientErrorException, ServerErrorException, ProcessingException;

    public void logout();
    
    public void setUserAgent(String userAgent);

    public List<String> getProjects()
            throws ClientErrorException, ServerErrorException, ProcessingException;

    public List<String> getComponents(String project)
            throws ClientErrorException, ServerErrorException, ProcessingException;

    public List<String> getTranslations(String project, String component)
            throws ClientErrorException, ServerErrorException, ProcessingException;

    public String getFileFormat(String project, String component, String language)
            throws ClientErrorException, ServerErrorException, ProcessingException;

    public String getFile(String project, String component, String language)
            throws ClientErrorException, ServerErrorException, ProcessingException;

    public Map<String, String> submit(String project, String component, String language, String file)
            throws ClientErrorException, ServerErrorException, ProcessingException;

}
