package jp.ac.tohoku.qse.takahashi.AtcSimulator;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.application.AircraftFactory;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.application.AircraftRadarService;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.application.ConflictAlertService;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.application.ScenarioService;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.config.globals.GlobalVariables;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.AircraftRepository;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Callsign.Callsign;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.dto.CreateAircraftDto;

/**
 * Backend redesign regression tests.
 * Verifies that key behaviors are preserved during refactoring.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BackendRedesignIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private ScenarioService scenarioService;

    @Autowired
    private AircraftRadarService aircraftRadarService;

    @Autowired
    private ConflictAlertService conflictAlertService;

    @Autowired
    private AircraftRepository aircraftRepository;

    private TestRestTemplate restTemplate = new TestRestTemplate();

    @BeforeEach
    void setUp() {
        GlobalVariables.isSimulationRunning = false;
    }

    @AfterEach
    void tearDown() {
        GlobalVariables.isSimulationRunning = false;
    }

    @Test
    @DisplayName("ScenarioService: spawnAircraft creates aircraft in repository")
    void scenarioService_spawnAircraft_createsAircraft() {
        CreateAircraftDto dto = new CreateAircraftDto(
                "TEST001", 35.0, 139.0, 35000, 450, 0, 90,
                "B738", "HND", "RJTT", "NRT", "RJAA", "2024-12-13T14:30:00Z"
        );

        scenarioService.spawnAircraft(AircraftFactory.createCommercialAircraft(dto));

        assertThat(aircraftRepository.isAircraftExist(new Callsign("TEST001"))).isTrue();
    }

    @Test
    @DisplayName("ConflictAlertService: getAllConflictAlerts returns map")
    void conflictAlertService_getAllConflictAlerts_returnsMap() {
        var result = conflictAlertService.getAllConflictAlerts();

        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("AircraftRadarService: getAllAircraftLocation returns string")
    void aircraftRadarService_getAllAircraftLocation_returnsString() {
        var result = aircraftRadarService.getAllAircraftLocation();

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("Location API: /aircraft/location/all returns JSON array")
    void locationApi_getAll_returnsJsonArray() {
        ResponseEntity<java.util.List> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/aircraft/location/all", java.util.List.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getHeaders().getContentType()).isNotNull();
        assertThat(response.getHeaders().getContentType().toString()).contains("application/json");
    }

    @Test
    @DisplayName("Simulation API: start returns 200")
    void simulationApi_start_returns200() {
        ResponseEntity<Void> response = restTemplate.postForEntity(
                "http://localhost:" + port + "/simulation/start", null, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("Simulation API: status returns isSimulationRunning")
    void simulationApi_status_returnsStatus() {
        restTemplate.postForEntity("http://localhost:" + port + "/simulation/start", null, Void.class);

        ResponseEntity<java.util.Map> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/simulation/status", java.util.Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsKey("isSimulationRunning");
    }

    @Test
    @DisplayName("Simulation API: pause returns 200")
    void simulationApi_pause_returns200() {
        restTemplate.postForEntity("http://localhost:" + port + "/simulation/start", null, Void.class);

        ResponseEntity<Void> response = restTemplate.postForEntity(
                "http://localhost:" + port + "/simulation/pause", null, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("FixPositionRepository: findFixPositionByName and Direct To work")
    void fixPositionRepository_directTo_works() {
        CreateAircraftDto dto = new CreateAircraftDto(
                "DIRECT01", 35.0, 139.0, 35000, 450, 0, 90,
                "B738", "HND", "RJTT", "NRT", "RJAA", "2024-12-13T14:30:00Z"
        );
        scenarioService.spawnAircraft(AircraftFactory.createCommercialAircraft(dto));
        scenarioService.directFixAircraft(new Callsign("DIRECT01"), "ABENO");
        assertThat(aircraftRepository.isAircraftExist(new Callsign("DIRECT01"))).isTrue();
    }

    @Test
    @DisplayName("ATS route API: /ats/route/all returns route info")
    void atsRouteApi_returnsRouteInfo() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/ats/route/all", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("waypoints");
    }

    @Test
    @DisplayName("ATS route suggest API: returns waypoints for known O/D")
    void atsRouteSuggest_returnsWaypoints() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/ats/route/suggest?origin=RJTT&destination=RJAA",
                String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("waypoints");
    }

    @Test
    @DisplayName("ATS route suggest API: returns 400 when params missing")
    void atsRouteSuggest_returns400WhenParamsMissing() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/ats/route/suggest?origin=RJTT",
                String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
