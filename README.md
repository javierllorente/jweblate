# jwl
A Java-based Weblate library

### Consume Weblate's REST resources easily
- Authenticate  
`TranslationProvider translationProvider = new Weblate();`  
`translationProvider.setApiUrl(new URI("https://l10.example.org/api/"));`  
`translationProvider.setAuthToken("yourToken");`  
`translationProvider.authenticate();`  
- Get resources  
`List<String> projects = translationProvider.getProjects();`  
`List<String> components = translationProvider.getComponents("project");`  
`List<String> translations = translationProvider.getTranslations("project", "component");`  
- Get translation file  
`String file = translationProvider.getFile("project", "component", "language");`  
- Submit changes  
`Map<String, String> results = translationProvider.submit("project", "component", "language", "file");`  

Copyright (C) 2020-2022 Javier Llorente javier@opensuse.org