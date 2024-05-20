package edu.example.springmvcdemo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import java.net.URI;

@Component
public class RestClientConfig {

    @Value("${remote-server.url}")
    private String serverUrl;

    public RestClient getRestClient() {
        return RestClient.builder()
                .requestFactory(new HttpComponentsClientHttpRequestFactory())
                .messageConverters(converters -> converters.add(new MappingJackson2HttpMessageConverter()))
                .build();
    }

    public URI getUri(String path) {
        return URI.create(serverUrl + path);
    }
}
