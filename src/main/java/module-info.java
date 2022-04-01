module com.javierllorente.jweblate {
    requires java.base;
    requires java.logging;
    requires jakarta.ws.rs;
    requires jersey.common;
    requires jersey.client;
    requires jersey.media.multipart;
    requires jakarta.json;

    exports com.javierllorente.jweblate;
}