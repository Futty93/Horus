package jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CreateAircraftServiceTest {

    @LocalServerPort
    private int port;

    @Autowired
    WebTestClient client;

    @Test
    void shouldCreateAircraft() {
        String json = "{\"altitude\":0,\"latitude\":0,\"longitude\":0,\"companyName\":\"ANA\",\"flightNumber\":\"001\"}";
        client.post()
                .uri("http://localhost:" + port + "/aircraft/create?altitude=0&latitude=0&longitude=0&companyName=ANA&flightNumber=001")
                .exchange()
                .expectStatus().isOk();
    }
}
