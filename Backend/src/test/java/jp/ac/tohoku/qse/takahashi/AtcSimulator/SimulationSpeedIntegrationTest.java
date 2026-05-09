package jp.ac.tohoku.qse.takahashi.AtcSimulator;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.databind.ObjectMapper;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.config.SimulationTickScheduler;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.config.SimulationTiming;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.config.globals.GlobalVariables;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.AircraftRepository;

/**
 * Integration tests for simulation speed API using the same single-aircraft payload as
 * {@code docs/test-data/scenario-load-minimal.json} (mirrored under test classpath).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SimulationSpeedIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private AircraftRepository aircraftRepository;

    @Autowired
    private SimulationTiming simulationTiming;

    @Autowired
    private SimulationTickScheduler simulationTickScheduler;

    @Autowired
    private ObjectMapper objectMapper;

    private String baseUrl() {
        return "http://localhost:" + port;
    }

    @BeforeEach
    void setUp() {
        GlobalVariables.isSimulationRunning = false;
        simulationTiming.setSpeedMultiplier(1.0);
        simulationTickScheduler.reschedule();
    }

    @AfterEach
    void tearDown() throws Exception {
        GlobalVariables.isSimulationRunning = false;
        simulationTiming.setSpeedMultiplier(1.0);
        simulationTickScheduler.reschedule();
        aircraftRepository.clear();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> loadMinimalScenarioMap() throws Exception {
        ClassPathResource resource = new ClassPathResource("fixtures/scenario-load-minimal.json");
        try (InputStream in = resource.getInputStream()) {
            return objectMapper.readValue(in, Map.class);
        }
    }

    @SuppressWarnings("unchecked")
    private double longitudeOfScload01() {
        ResponseEntity<List> response = restTemplate.getForEntity(
                baseUrl() + "/aircraft/location/all", List.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<Map<String, Object>> rows = response.getBody();
        assertThat(rows).isNotEmpty();
        for (Map<String, Object> row : rows) {
            if ("SCLOAD01".equals(row.get("callsign"))) {
                Map<String, Object> pos = (Map<String, Object>) row.get("position");
                return ((Number) pos.get("longitude")).doubleValue();
            }
        }
        throw new AssertionError("SCLOAD01 not found");
    }

    @Test
    @DisplayName("GET /simulation/speed returns default 1x and 1000 ms tick")
    void getSpeed_default() {
        ResponseEntity<Map> response = restTemplate.getForEntity(
                baseUrl() + "/simulation/speed", Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsEntry("speedMultiplier", 1.0);
        assertThat(response.getBody()).containsEntry("tickIntervalWallMs", 1000);
    }

    @Test
    @DisplayName("PUT /simulation/speed rejects non-preset multiplier")
    void putSpeed_invalid_returns400() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>("{\"speedMultiplier\":3}", headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl() + "/simulation/speed", HttpMethod.PUT, entity, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsKey("message");
    }

    @Test
    @DisplayName("PUT /simulation/speed accepts preset and GET reflects tick interval")
    void putSpeed_valid_reschedules() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>("{\"speedMultiplier\":2}", headers);

        ResponseEntity<Void> putResponse = restTemplate.exchange(
                baseUrl() + "/simulation/speed", HttpMethod.PUT, entity, Void.class);
        assertThat(putResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<Map> getResponse = restTemplate.getForEntity(
                baseUrl() + "/simulation/speed", Map.class);
        assertThat(getResponse.getBody()).containsEntry("speedMultiplier", 2.0);
        assertThat(getResponse.getBody()).containsEntry("tickIntervalWallMs", 500);
    }

    @Test
    @DisplayName("GET /simulation/status includes speed fields")
    void status_includesSpeed() {
        ResponseEntity<Map> response = restTemplate.getForEntity(
                baseUrl() + "/simulation/status", Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsKey("isSimulationRunning");
        assertThat(response.getBody()).containsKey("speedMultiplier");
        assertThat(response.getBody()).containsKey("tickIntervalWallMs");
    }

    @Test
    @DisplayName("With scenario-load-minimal aircraft, faster wall speed advances position over wall time")
    void minimalScenario_aircraftMovesAt4x() throws Exception {
        Map<String, Object> scenario = loadMinimalScenarioMap();
        ResponseEntity<Map> loadResponse = restTemplate.postForEntity(
                baseUrl() + "/api/scenario/load", scenario, Map.class);
        assertThat(loadResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        restTemplate.exchange(
                baseUrl() + "/simulation/speed",
                HttpMethod.PUT,
                new HttpEntity<>("{\"speedMultiplier\":4}", headers),
                Void.class);

        double lonBefore = longitudeOfScload01();

        restTemplate.postForEntity(baseUrl() + "/simulation/start", null, Void.class);

        Thread.sleep(1300);

        restTemplate.postForEntity(baseUrl() + "/simulation/pause", null, Void.class);

        double lonAfter = longitudeOfScload01();

        assertThat(lonAfter).isGreaterThan(lonBefore);
    }
}
