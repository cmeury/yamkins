package org.jenkinsci.plugins.yamkins;

import javax.ws.rs.core.Response;
import java.util.Date;

/**
 * Testing the service class manually.
 */
public class Playground {

    public static void main(String args[]) {
        YammerService service = new YammerService("APITOKEN", "GROUPID");
        Response response = service.postMessage("Hello World! Time is " + new Date().toString());
        System.out.println(response.getStatus());
        System.out.println(response.readEntity(String.class));
    }
}
