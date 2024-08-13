package jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class LocationServiceTest {
    @LocalServerPort
    private int port;

    @Autowired
    WebTestClient client;

    @Test
    void shouldGetAllAircraftLocation() {
        client.get()
                .uri("http://localhost:" + port + "/aircraft/location/all")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void shouldGetAircraftLocation() {
        client.post()
                .uri("http://localhost:" + port + "/aircraft/create?altitude=0&latitude=0&longitude=0&companyName=ANA&flightNumber=001")
                .exchange()
                .expectStatus().isOk();
        client.get()
                .uri("http://localhost:" + port + "/aircraft/location?companyName=ANA&flightNumber=001")
                .exchange()
                .expectStatus().isOk();
    }
}
