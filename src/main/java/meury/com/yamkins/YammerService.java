package meury.com.yamkins;

import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Cedric
 * Date: 6/11/13
 * Time: 8:06 PM
 * To change this template use File | Settings | File Templates.
 *
 *
 * API calls are subject to rate limiting. Exceeding any rate limits will result in all endpoints returning a status code of 429 (Too Many Requests). Rate limits are per user per app. There are four rate limits:

 Autocomplete: 10 requests in 10 seconds.

 Messages: 10 requests in 30 seconds.

 Notifications: 10 requests in 30 seconds.

 All Other Resources: 10 requests in 10 seconds.
 */
public class YammerService {

    private static String BASE_URL = "https://www.yammer.com/api/v1";
    private String bearerToken;

    /**
     * Create a new Yammer REST API service.
     * @param apiToken long lived authorization token
     */
    public YammerService(String apiToken) {
        this.bearerToken = "Bearer " + apiToken;
    }

    /**
     * Do a POST request to the Yammer API endpoint v1.
     * @param resource resource path (for example "users/current.json")
     * @param params map of query parameters
     */
    public Response post(String resource, Map<String, Object> params) {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(BASE_URL).path(resource);
        for (String key : params.keySet()) {
            target = target.queryParam(key, params.get(key));
        }
        Invocation.Builder request = target.request(MediaType.APPLICATION_JSON_TYPE);
        request.header("Authorization", bearerToken);
        return request.post(Entity.entity("A string entity to be POSTed", MediaType.TEXT_PLAIN));
    }

    public static void main(String args[]) {
        YammerService service = new YammerService("");
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("body", "Hello World!");
        map.put("group_id", 1940994);
        Response post = service.post("messages.json", map);
        System.out.println(post.getStatus());
        System.out.println(post.readEntity(String.class));
    }
}
