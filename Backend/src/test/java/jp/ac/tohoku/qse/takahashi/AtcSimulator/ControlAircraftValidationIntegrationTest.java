package jp.ac.tohoku.qse.takahashi.AtcSimulator;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ControlAircraftValidationIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String baseUrl() {
        return "http://localhost:" + port;
    }

    private void spawn(String callsign) {
        var flightPlan = Map.of(
                "callsign", callsign,
                "cruiseAltitude", 35000,
                "cruiseSpeed", 450,
                "route", List.of(Map.of("fix", "ABENO", "action", "CONTINUE")));
        var initialPosition = Map.of(
                "latitude", 35.0,
                "longitude", 139.0,
                "altitude", 5000,
                "heading", 90,
                "groundSpeed", 250,
                "verticalSpeed", 0);
        restTemplate.postForEntity(
                baseUrl() + "/api/aircraft/spawn-with-flightplan",
                Map.of("flightPlan", flightPlan, "initialPosition", initialPosition),
                Map.class);
    }

    @Test
    void rejectsInvalidIndicatedAirspeed() {
        spawn("CTLVAL01");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String body = """
                {"instructedHeading":90,"instructedAltitude":5000,"instructedGroundSpeed":250,"instructedIndicatedAirspeed":0}
                """;
        ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl() + "/api/aircraft/control/CTLVAL01",
                new HttpEntity<>(body, headers),
                Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void acceptsNullIndicatedAirspeedForGroundSpeedMode() {
        spawn("CTLVAL02");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String body = """
                {"instructedHeading":90,"instructedAltitude":5000,"instructedGroundSpeed":250,"instructedIndicatedAirspeed":null}
                """;
        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl() + "/api/aircraft/control/CTLVAL02",
                new HttpEntity<>(body, headers),
                String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
