# jwl
A Java-based Weblate library

### Consume Weblate's REST resources easily
- Authenticate  
`Weblate weblate = new Weblate();`  
`weblate.setApiUrl(new URI("https://l10.example.org/api/"));`  
`weblate.setAuthToken("yourToken");`  
`weblate.authenticate();`  
- Get resources  
`List<String> projects = weblate.getProjects();`  
`List<String> components = weblate.getComponents("project");`  
`List<String> translations = weblate.getTranslations("project", "component");`  
- Get translation file  
`String file = weblate.getFile("project", "component", "language");`  
- Submit changes  
`Map<String, String> results = weblate.submit("project", "component", "language", "file");`  

Copyright (C) 2020-2022 Javier Llorente javier@opensuse.org