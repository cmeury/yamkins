package meury.com.yamkins;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

/**
 * Posts messages to Yammer groups via REST calls.
 */
public class YammerService {

    private static String BASE_URL = "https://www.yammer.com/api/v1";
    private String bearerToken;
    private String groupId;

    /**
     * Create a new Yammer REST API service.
     * @param apiToken long lived authorization token
     * @param groupId ID of the Yammer group
     */
    public YammerService(String apiToken, String groupId) {
        this.bearerToken = "Bearer " + apiToken;
        this.groupId = groupId;
    }

    /**
     * Do a POST request to the Yammer API endpoint v1.
     * @param resource resource path (for example "users/current.json")
     * @param params map of query parameters
     * @return response from the web request
     */
    public Response post(String resource, Map<String, Object> params) {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(BASE_URL).path(resource);
        for (String key : params.keySet()) {
            target = target.queryParam(key, params.get(key));
        }
        Invocation.Builder request = target.request(MediaType.APPLICATION_JSON_TYPE);
        request.header("Authorization", bearerToken);
        return request.post(null);   // no payload
    }

    /**
     * Post a message to the group this service has been configured with.
     * @param message plain text to post
     * @return response from the web request
     */
    public Response postMessage(String message) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("body", message);
        map.put("group_id", groupId);
        return post("messages.json", map);
    }

}
