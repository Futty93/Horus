package jp.ac.tohoku.qse.takahashi.AtcSimulator;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;
import java.util.HashMap;
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
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.AircraftBase;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.AircraftRepository;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Callsign.Callsign;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.FixPosition;

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
        aircraftRepository.clear();
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
    @DisplayName("POST /api/scenario/load succeeds with multiple aircraft")
    void loadScenario_successWithMultipleAircraft() {
        var ac1 = Map.of(
                "flightPlan", Map.of(
                        "callsign", "SCN01",
                        "departureAirport", "RJTT",
                        "arrivalAirport", "RJAA",
                        "cruiseAltitude", 35000,
                        "cruiseSpeed", 450,
                        "route", List.<Map<String, Object>>of()
                ),
                "initialPosition", Map.of(
                        "latitude", 35.0, "longitude", 139.0, "altitude", 5000,
                        "heading", 90, "groundSpeed", 250, "verticalSpeed", 0
                )
        );
        var ac2 = Map.of(
                "flightPlan", Map.of(
                        "callsign", "SCN02",
                        "departureAirport", "RJAA",
                        "arrivalAirport", "RJTT",
                        "cruiseAltitude", 36000,
                        "cruiseSpeed", 460,
                        "route", List.<Map<String, Object>>of()
                ),
                "initialPosition", Map.of(
                        "latitude", 35.01, "longitude", 139.01, "altitude", 5100,
                        "heading", 180, "groundSpeed", 260, "verticalSpeed", 0
                )
        );
        var scenario = Map.of(
                "scenarioName", "Test Scenario",
                "aircraft", List.of(ac1, ac2)
        );

        ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl() + "/api/scenario/load",
                scenario,
                Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsEntry("success", true);
        assertThat(response.getBody()).containsEntry("aircraftCount", 2);
        assertThat(response.getBody()).containsEntry("scenarioName", "Test Scenario");
        assertThat(aircraftRepository.isAircraftExist(new Callsign("SCN01"))).isTrue();
        assertThat(aircraftRepository.isAircraftExist(new Callsign("SCN02"))).isTrue();
    }

    @Test
    @DisplayName("POST /api/scenario/load does not start simulation; isSimulationRunning remains false")
    void loadScenario_doesNotStartSimulation() {
        var ac = Map.of(
                "flightPlan", Map.of(
                        "callsign", "NOSIM01",
                        "departureAirport", "RJTT",
                        "arrivalAirport", "RJAA",
                        "cruiseAltitude", 35000,
                        "cruiseSpeed", 450,
                        "route", List.<Map<String, Object>>of()
                ),
                "initialPosition", Map.of(
                        "latitude", 35.0, "longitude", 139.0, "altitude", 5000,
                        "heading", 90, "groundSpeed", 250, "verticalSpeed", 0
                )
        );
        var scenario = Map.of(
                "scenarioName", "No Auto-Start Test",
                "aircraft", List.of(ac)
        );

        restTemplate.postForEntity(baseUrl() + "/api/scenario/load", scenario, Map.class);

        ResponseEntity<Map> statusResponse = restTemplate.getForEntity(
                baseUrl() + "/simulation/status", Map.class);

        assertThat(statusResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(statusResponse.getBody()).containsEntry("isSimulationRunning", false);
    }

    @Test
    @DisplayName("POST /api/scenario/load returns 400 when aircraft array is empty")
    void loadScenario_returns400_whenAircraftEmpty() {
        var scenario = Map.of(
                "scenarioName", "Empty",
                "aircraft", List.of()
        );

        ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl() + "/api/scenario/load",
                scenario,
                Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsEntry("success", false);
    }

    @Test
    @DisplayName("POST /api/scenario/load returns 400 on duplicate callsign")
    void loadScenario_returns400_onDuplicateCallsign() {
        var ac = Map.of(
                "flightPlan", Map.of(
                        "callsign", "DUP01",
                        "departureAirport", "RJTT",
                        "arrivalAirport", "RJAA",
                        "cruiseAltitude", 35000,
                        "cruiseSpeed", 450,
                        "route", List.<Map<String, Object>>of()
                ),
                "initialPosition", Map.of(
                        "latitude", 35.0, "longitude", 139.0, "altitude", 5000,
                        "heading", 90, "groundSpeed", 250, "verticalSpeed", 0
                )
        );
        var scenario = Map.of(
                "scenarioName", "Duplicate Test",
                "aircraft", List.of(ac, ac)
        );

        ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl() + "/api/scenario/load",
                scenario,
                Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsEntry("success", false);
        assertThat(String.valueOf(response.getBody().get("message"))).contains("Duplicate callsign");
    }

    @Test
    @DisplayName("POST /api/scenario/load skips aircraft without initialPosition")
    void loadScenario_skipsAircraftWithoutInitialPosition() {
        var acValid = Map.of(
                "flightPlan", Map.of(
                        "callsign", "SKIPVALID",
                        "departureAirport", "RJTT",
                        "arrivalAirport", "RJAA",
                        "cruiseAltitude", 35000,
                        "cruiseSpeed", 450,
                        "route", List.<Map<String, Object>>of()
                ),
                "initialPosition", Map.of(
                        "latitude", 35.0, "longitude", 139.0, "altitude", 5000,
                        "heading", 90, "groundSpeed", 250, "verticalSpeed", 0
                )
        );
        // Map.of() rejects null; HashMap is required for initialPosition: null
        var acNoPos = new HashMap<String, Object>();
        acNoPos.put("flightPlan", Map.of(
                "callsign", "SKIPNULL",
                "departureAirport", "RJTT",
                "arrivalAirport", "RJAA",
                "cruiseAltitude", 35000,
                "cruiseSpeed", 450,
                "route", List.<Map<String, Object>>of()
        ));
        acNoPos.put("initialPosition", null);
        var scenario = Map.of(
                "scenarioName", "Skip Test",
                "aircraft", List.of(acValid, acNoPos)
        );

        ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl() + "/api/scenario/load",
                scenario,
                Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsEntry("success", true);
        assertThat(response.getBody()).containsEntry("aircraftCount", 1);
        assertThat(aircraftRepository.isAircraftExist(new Callsign("SKIPVALID"))).isTrue();
        assertThat(aircraftRepository.isAircraftExist(new Callsign("SKIPNULL"))).isFalse();
    }

    @Test
    @DisplayName("POST /api/scenario/load returns 400 when fix in route does not exist")
    void loadScenario_returns400_whenFixNotFound() {
        var ac = Map.of(
                "flightPlan", Map.of(
                        "callsign", "BADFIX01",
                        "departureAirport", "RJTT",
                        "arrivalAirport", "RJAA",
                        "cruiseAltitude", 35000,
                        "cruiseSpeed", 450,
                        "route", List.of(Map.of("fix", "NONEXISTENT_WAYPOINT_XYZ", "action", "CONTINUE"))
                ),
                "initialPosition", Map.of(
                        "latitude", 35.0, "longitude", 139.0, "altitude", 5000,
                        "heading", 90, "groundSpeed", 250, "verticalSpeed", 0
                )
        );
        var scenario = Map.of(
                "scenarioName", "Bad Fix Test",
                "aircraft", List.of(ac)
        );

        ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl() + "/api/scenario/load",
                scenario,
                Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsEntry("success", false);
        assertThat(String.valueOf(response.getBody().get("message"))).contains("Fix not found");
    }

    @Test
    @DisplayName("POST /api/aircraft/{callsign}/flightplan assigns flight plan to existing aircraft")
    void assignFlightPlan_toExistingAircraft() {
        var fpSpawn = Map.of(
                "callsign", "ASGN01",
                "cruiseAltitude", 35000,
                "cruiseSpeed", 450,
                "route", List.of(Map.of("fix", "ABENO", "action", "CONTINUE"))
        );
        var initialPosition = Map.of(
                "latitude", 35.0, "longitude", 139.0, "altitude", 5000,
                "heading", 90, "groundSpeed", 250, "verticalSpeed", 0
        );
        restTemplate.postForEntity(baseUrl() + "/api/aircraft/spawn-with-flightplan",
                Map.of("flightPlan", fpSpawn, "initialPosition", initialPosition), Map.class);

        var assignBody = Map.of(
                "callsign", "ASGN01",
                "aircraftType", "B738",
                "departureAirport", "RJTT",
                "arrivalAirport", "RJOO",
                "cruiseAltitude", 36000,
                "cruiseSpeed", 460,
                "route", List.of(
                        Map.of("fix", "ABENO", "action", "CONTINUE"),
                        Map.of("fix", "MAIKO", "action", "CONTINUE")
                )
        );
        ResponseEntity<Map> assignResp = restTemplate.postForEntity(
                baseUrl() + "/api/aircraft/ASGN01/flightplan",
                assignBody,
                Map.class
        );

        assertThat(assignResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(assignResp.getBody()).containsEntry("success", true);

        ResponseEntity<Map> getResp = restTemplate.getForEntity(
                baseUrl() + "/api/aircraft/ASGN01/flightplan", Map.class);
        assertThat(getResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        @SuppressWarnings("unchecked")
        List<String> remaining = (List<String>) getResp.getBody().get("remainingWaypoints");
        assertThat(remaining).as("route should include MAIKO after assign").contains("MAIKO");
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

    @Test
    @DisplayName("POST /api/aircraft/{callsign}/hold applies right-turn holding")
    void holdAtFix_appliesInstruction() {
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
                baseUrl() + "/api/aircraft/FPAPI01/hold",
                Map.of("fixName", "ABENO", "turnDirection", "RIGHT"),
                Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsEntry("success", true);
        assertThat(response.getBody()).containsEntry("navigationMode", "HOLDING");
        assertThat(response.getBody()).containsEntry("turnDirection", "RIGHT");
    }

    @Test
    @DisplayName("POST /api/aircraft/{callsign}/hold rejects unsupported turn direction")
    void holdAtFix_rejectsLeftTurn() {
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
                baseUrl() + "/api/aircraft/FPAPI01/hold",
                Map.of("fixName", "ABENO", "turnDirection", "LEFT"),
                Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsEntry("success", false);
    }

    @Test
    @DisplayName("same fix hold uses identical deterministic orientation")
    void holdAtFix_sameFixUsesDeterministicOrientation() {
        var flightPlanA = Map.of(
                "callsign", "HOLDA1",
                "cruiseAltitude", 35000,
                "cruiseSpeed", 450,
                "route", List.of(Map.of("fix", "ABENO", "action", "CONTINUE"))
        );
        var flightPlanB = Map.of(
                "callsign", "HOLDB1",
                "cruiseAltitude", 35000,
                "cruiseSpeed", 450,
                "route", List.of(Map.of("fix", "ABENO", "action", "CONTINUE"))
        );
        var initialPositionA = Map.of(
                "latitude", 35.0, "longitude", 139.0, "altitude", 5000,
                "heading", 30, "groundSpeed", 250, "verticalSpeed", 0
        );
        var initialPositionB = Map.of(
                "latitude", 34.6, "longitude", 138.6, "altitude", 5000,
                "heading", 280, "groundSpeed", 250, "verticalSpeed", 0
        );

        restTemplate.postForEntity(baseUrl() + "/api/aircraft/spawn-with-flightplan",
                Map.of("flightPlan", flightPlanA, "initialPosition", initialPositionA), Map.class);
        restTemplate.postForEntity(baseUrl() + "/api/aircraft/spawn-with-flightplan",
                Map.of("flightPlan", flightPlanB, "initialPosition", initialPositionB), Map.class);

        restTemplate.postForEntity(
                baseUrl() + "/api/aircraft/HOLDA1/hold",
                Map.of("fixName", "ABENO", "turnDirection", "RIGHT"),
                Map.class
        );
        restTemplate.postForEntity(
                baseUrl() + "/api/aircraft/HOLDB1/hold",
                Map.of("fixName", "ABENO", "turnDirection", "RIGHT"),
                Map.class
        );

        var aircraftA = (AircraftBase) aircraftRepository.findByCallsign(new Callsign("HOLDA1"));
        var aircraftB = (AircraftBase) aircraftRepository.findByCallsign(new Callsign("HOLDB1"));
        FixPosition outboundA = extractHoldingOutboundTarget(aircraftA);
        FixPosition outboundB = extractHoldingOutboundTarget(aircraftB);

        assertThat(outboundA.latitude.toDouble()).isEqualTo(outboundB.latitude.toDouble());
        assertThat(outboundA.longitude.toDouble()).isEqualTo(outboundB.longitude.toDouble());
    }

    private FixPosition extractHoldingOutboundTarget(AircraftBase aircraft) {
        try {
            Field field = AircraftBase.class.getDeclaredField("holdingOutboundTarget");
            field.setAccessible(true);
            return (FixPosition) field.get(aircraft);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new IllegalStateException("Failed to inspect holding outbound target", e);
        }
    }
}
