package org.jenkinsci.plugins.yamkins;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
    public Response post(String resource, Map<String, String> params) {
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
        Map<String, String> map = new HashMap<String, String>();
        map.put("body", message);
        map.put("group_id", groupId);
        return post("messages.json", map);
    }

    /**
     * post a message in opengraph format to the group this service has been configured with
     * @param graphUrl the mandatory canonical URL of the OG object that will be used as its permanent ID in the graph
     * @param openGraphParams optional parameters
     * @param replyToMessageId The message ID this message is in reply to, can be null
     * @return  response from the web request
     */
    public Response postMessage(URL graphUrl, Map<OpenGraph, String> openGraphParams, String replyToMessageId) {
        Map<String, String> paramMap = new HashMap<String, String>();
        paramMap.put(OpenGraph.URL.toString(), graphUrl.toString());
        paramMap.put("group_id", groupId);

        Set<Map.Entry<OpenGraph, String>> entrySet = openGraphParams.entrySet();
        for(Map.Entry<OpenGraph, String> entry : entrySet) {
            paramMap.put(entry.getKey().toString(), entry.getValue());
        }

        if(replyToMessageId != null && !replyToMessageId.isEmpty())
            paramMap.put("replied_to_id", replyToMessageId);

        return post("messages.json", paramMap);
    }

}
