package jp.ac.tohoku.qse.takahashi.AtcSimulator;

import static org.assertj.core.api.Assertions.assertThat;

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
import org.springframework.http.*;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.config.globals.GlobalVariables;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.AircraftRepository;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Callsign.Callsign;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FlightPlanApiIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private AircraftRepository aircraftRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    private String baseUrl() {
        return "http://localhost:" + port;
    }

    @BeforeEach
    void setUp() {
        GlobalVariables.isSimulationRunning = false;
    }

    @AfterEach
    void tearDown() {
        GlobalVariables.isSimulationRunning = false;
        try {
            var a = aircraftRepository.findByCallsign(new Callsign("FPAPI01"));
            aircraftRepository.remove(a);
        } catch (Exception ignored) {
        }
    }

    @Test
    @DisplayName("POST /api/aircraft/spawn-with-flightplan spawns aircraft with flight plan")
    void spawnWithFlightPlan_createsAircraft() {
        var flightPlan = Map.of(
                "callsign", "FPAPI01",
                "aircraftType", "B738",
                "departureAirport", "RJTT",
                "arrivalAirport", "RJOO",
                "cruiseAltitude", 35000,
                "cruiseSpeed", 450,
                "route", List.of(
                        Map.of("fix", "ABENO", "action", "CONTINUE"),
                        Map.of("fix", "MAIKO", "action", "CONTINUE")
                )
        );
        var initialPosition = Map.of(
                "latitude", 35.0,
                "longitude", 139.0,
                "altitude", 5000,
                "heading", 90,
                "groundSpeed", 250,
                "verticalSpeed", 0
        );
        var body = Map.of("flightPlan", flightPlan, "initialPosition", initialPosition);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl() + "/api/aircraft/spawn-with-flightplan",
                body,
                Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsEntry("success", true);
        assertThat(response.getBody()).containsEntry("callsign", "FPAPI01");
        assertThat(aircraftRepository.isAircraftExist(new Callsign("FPAPI01"))).isTrue();
    }

    @Test
    @DisplayName("GET /api/aircraft/{callsign}/flightplan returns flight plan status")
    void getFlightPlan_returnsStatus() {
        var flightPlan = Map.of(
                "callsign", "FPAPI01",
                "cruiseAltitude", 35000,
                "cruiseSpeed", 450,
                "route", List.of(
                        Map.of("fix", "ABENO", "action", "CONTINUE")
                )
        );
        var initialPosition = Map.of(
                "latitude", 35.0, "longitude", 139.0, "altitude", 5000,
                "heading", 90, "groundSpeed", 250, "verticalSpeed", 0
        );
        restTemplate.postForEntity(baseUrl() + "/api/aircraft/spawn-with-flightplan",
                Map.of("flightPlan", flightPlan, "initialPosition", initialPosition), Map.class);

        ResponseEntity<Map> response = restTemplate.getForEntity(
                baseUrl() + "/api/aircraft/FPAPI01/flightplan",
                Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsKey("callsign");
        assertThat(response.getBody()).containsKey("navigationMode");
        assertThat(response.getBody()).containsKey("remainingWaypoints");
    }

    @Test
    @DisplayName("POST /api/aircraft/{callsign}/direct-to applies direct to")
    void directTo_appliesInstruction() {
        var flightPlan = Map.of(
                "callsign", "FPAPI01",
                "cruiseAltitude", 35000,
                "cruiseSpeed", 450,
                "route", List.of(Map.of("fix", "ABENO", "action", "CONTINUE"))
        );
        var initialPosition = Map.of(
                "latitude", 35.0, "longitude", 139.0, "altitude", 5000,
                "heading", 90, "groundSpeed", 250, "verticalSpeed", 0
        );
        restTemplate.postForEntity(baseUrl() + "/api/aircraft/spawn-with-flightplan",
                Map.of("flightPlan", flightPlan, "initialPosition", initialPosition), Map.class);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl() + "/api/aircraft/FPAPI01/direct-to",
                Map.of("fixName", "ABENO", "resumeFlightPlan", false),
                Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsEntry("success", true);
        assertThat(response.getBody()).containsEntry("targetFix", "ABENO");
    }

    @Test
    @DisplayName("POST /api/aircraft/{callsign}/resume-navigation applies resume")
    void resumeNavigation_appliesInstruction() {
        var flightPlan = Map.of(
                "callsign", "FPAPI01",
                "cruiseAltitude", 35000,
                "cruiseSpeed", 450,
                "route", List.of(Map.of("fix", "ABENO", "action", "CONTINUE"))
        );
        var initialPosition = Map.of(
                "latitude", 35.0, "longitude", 139.0, "altitude", 5000,
                "heading", 90, "groundSpeed", 250, "verticalSpeed", 0
        );
        restTemplate.postForEntity(baseUrl() + "/api/aircraft/spawn-with-flightplan",
                Map.of("flightPlan", flightPlan, "initialPosition", initialPosition), Map.class);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl() + "/api/aircraft/FPAPI01/resume-navigation",
                null,
                Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsEntry("success", true);
        assertThat(response.getBody()).containsEntry("navigationMode", "FLIGHT_PLAN");
    }
}
