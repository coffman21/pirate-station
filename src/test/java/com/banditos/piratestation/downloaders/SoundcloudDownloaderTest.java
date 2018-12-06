package com.banditos.piratestation.downloaders;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;

@RunWith(SpringJUnit4ClassRunner.class)
public class SoundcloudDownloaderTest {

    Logger logger = LogManager.getLogger();

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

}