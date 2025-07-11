package one.xis.http;

import com.google.gson.Gson;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor
class RestClientImpl implements RestClient {

    @Getter
    private final HttpClient httpClient;
    private final Gson gson;

    @Override
    public <R> R get(String url, Class<R> responseType) throws HttpClientException {
        HttpResponse response = httpClient.doGet(url, Map.of("Accept", "application/json"));
        if (response.getStatusCode() != 200) {
            throw new HttpClientException("Failed to fetch data: " + response.getStatusCode());
        }
        return gson.fromJson(response.getContent(), responseType);
    }

    @Override
    public <R> R post(String url, Object requestBody, Class<R> responseType) throws HttpClientException {
        String jsonBody = gson.toJson(requestBody);
        HttpResponse response = httpClient.doPost(url, jsonBody, Map.of("Content-Type", "application/json", "Accept", "application/json"));
        if (response.getStatusCode() != 200) {
            throw new HttpClientException("Failed to post data: " + response.getStatusCode());
        }
        return gson.fromJson(response.getContent(), responseType);
    }


}
