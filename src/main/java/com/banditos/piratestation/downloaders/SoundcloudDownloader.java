package com.banditos.piratestation.downloaders;

import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;

@Service
public class SoundcloudDownloader implements Downloader {

    @Value("${key.soundcloud}")
    private String KEY;
    private WebClient webClientApi;

    @PostConstruct
    public void init() {
        webClientApi = WebClient
                .builder()
                .baseUrl("https://api.soundcloud.com")
                .build();
    }

    @Override
    public byte[] download(String trackId) {

        Mono<byte[]> mono = webClientApi.get()
                .uri(builder -> builder
                        .queryParam("client_id", KEY)
                        .path("tracks/" + trackId + "/stream")
                        .build())
                .exchange()
                .flatMap(resp -> {
                    if (resp.statusCode().is4xxClientError()) {
                        Mono.error(new WebClientResponseException(
                                HttpStatus.UNAUTHORIZED.value(),
                                "Unauthorized",
                                null, null, null
                        ));
                    }
                    return resp
                            .bodyToMono(Void.class).then(webClientApi
                            .get().uri(resp.headers().header("Location").get(0))
                            .exchange()
                            .flatMap(r -> r.bodyToMono(ByteArrayResource.class)));
                })
                .map(ByteArrayResource::getByteArray);;

        return mono.block();
    }

    public Gson search(MultiValueMap<String, String> params) {
        Mono<Gson> mono = webClientApi.get()
                .uri(builder -> builder
                        .queryParam("client_id", KEY)
                        .queryParams(params)
                        .path("tracks")
                        .build())
                .exchange()
                .flatMap(resp -> resp.bodyToMono(Gson.class));
        return mono.block();
    }
}
