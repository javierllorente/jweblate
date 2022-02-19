/*
 * Copyright (C) 2022 Javier Llorente <javier@opensuse.org>
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
