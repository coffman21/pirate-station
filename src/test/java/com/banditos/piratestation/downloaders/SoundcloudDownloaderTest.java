package com.banditos.piratestation.downloaders;

import com.banditos.piratestation.downloaders.dto.SoundcloudSong;
import com.banditos.piratestation.downloaders.dto.SoundcloudUser;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
public class SoundcloudDownloaderTest {

    private Logger logger = LoggerFactory.getLogger(SoundcloudDownloaderTest.class);

    WebClient webClientApi;

    @Before
    public void init() {
        webClientApi = WebClient
                .builder()
                .baseUrl("https://api.soundcloud.com")
                .build();
    }
    @Test
    public void doWebClientRequest() {
        WebClient webClient = WebClient
                .builder()
                .baseUrl("https://example.com")
                .build();

        Mono<String> responseMono = webClient
                .get().uri("/").exchange().flatMap(r -> r.bodyToMono(String.class));

        logger.info(responseMono.block());
    }

    @Test
    public void downloadFile() throws IOException {
        WebClient webClientApi = WebClient
                .builder()
                .baseUrl("https://api.soundcloud.com")
                .build();

        Mono<byte[]> mono = webClientApi.get()
                .uri(builder -> builder
                        .queryParam("client_id", "WKcQQdEZw7Oi01KqtHWxeVSxNyRzgT8M")
                        .path("tracks/21792166/stream")
                        .build())
                .exchange()
                .flatMap(resp -> resp
                        .bodyToMono(Void.class).then(webClientApi
                                .get().uri(resp.headers().header("Location").get(0))
                                .exchange()
                                .flatMap(r -> r.bodyToMono(ByteArrayResource.class))))
                .map(ByteArrayResource::getByteArray);;


        FileUtils.writeByteArrayToFile(new File("new.mp3"), mono.block());
    }

    @Test
    public void gsonTest() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.put("q", Collections.singletonList("scary%20monsters"));

        Mono<String> mono = webClientApi.get()
                .uri(builder -> builder
                        .queryParam("client_id", "WKcQQdEZw7Oi01KqtHWxeVSxNyRzgT8M")
                        .queryParams(params)
                        .path("tracks")
                        .build())
                .exchange()
                .flatMap(resp -> resp.bodyToMono(String.class));
        String resp = mono.block();
        logger.info(resp);
        List<SoundcloudSong> serialized = new Gson()
                .fromJson(resp, new TypeToken<List<SoundcloudSong>>() {}.getType());
        logger.info(serialized.get(0).title);
    }
}