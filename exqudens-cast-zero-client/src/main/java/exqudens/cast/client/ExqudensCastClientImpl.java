package exqudens.cast.client;

import java.net.HttpURLConnection;
import java.util.Map.Entry;
import java.util.Objects;

import com.fasterxml.jackson.databind.ObjectMapper;

import exqudens.cast.api.model.graph.Graph;
import exqudens.cast.api.util.HttpClient;

class ExqudensCastClientImpl implements ExqudensCastClient {

    private final String baseUrl;
    private final Integer connectTimeout;

    ExqudensCastClientImpl(String host, Integer port, String name, Integer connectTimeout) {
        super();
        this.baseUrl = host + (port != null ? ":" + port : "") + (name != null ? "/" + name : "");
        this.connectTimeout = connectTimeout != null ? connectTimeout : 1000;
    }

    @Override
    public Graph apiGraphOrder(Graph graph) {
        try {
            HttpClient httpClient = createHttpClient();
            ObjectMapper mapper = new ObjectMapper();
            String requestJson = mapper.writeValueAsString(graph);
            Entry<Integer, String> responseEntry = httpClient.requestWithBody(
                baseUrl + "/api/graph/order",
                requestJson
            );
            String responseJson = checkAndGetBody(responseEntry);
            Graph response = null;
            if (responseJson != null && !responseJson.isEmpty()) {
                response = mapper.readValue(responseJson, Graph.class);
            }
            return response;
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw e;
        } catch (Throwable e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private HttpClient createHttpClient() {
        try {
            return HttpClient.builder().requestMethod(HttpClient.POST).contentType(
                HttpClient.APPLICATION_JSON_CHARSET_UTF_8
            ).connectTimeout(connectTimeout).build();
        } catch (RuntimeException e) {
            throw e;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private String checkAndGetBody(Entry<Integer, String> responseEntry) {
        try {
            Objects.requireNonNull(responseEntry, "'responseEntry' is null");
            Integer responseCode = responseEntry.getKey();
            Objects.requireNonNull(responseCode, "'responseCode' is null");
            String responseBody = responseEntry.getValue();
            if (!responseCode.equals(HttpURLConnection.HTTP_OK)) {
                throw new IllegalStateException(responseBody);
            }
            return responseBody;
        } catch (RuntimeException e) {
            throw e;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

}
