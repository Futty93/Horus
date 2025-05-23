package jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.api;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.aggregate.airspace.AirspaceManagement;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.AircraftRepository;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.service.scenario.ScenarioService;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Callsign.Callsign;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.dto.CreateAircraftDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/aircraft")
public class CreateAircraftService {

    private final ScenarioService scenarioService;
    private final AircraftRepository aircraftRepository;

    public CreateAircraftService(ScenarioService scenarioService, AircraftRepository aircraftRepository) {
        this.scenarioService = scenarioService;
        this.aircraftRepository = aircraftRepository;
    }

    @Operation(
            summary = "Create multiple new aircraft",
            description = "This API creates multiple aircraft based on the provided DTO array. Aircraft that already exist are skipped.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CreateAircraftDto[].class),
                            examples = @ExampleObject(
                                    value = "[ { \"callsign\": \"SKY514\", \"latitude\": 34.99, \"longitude\": 139.613, \"altitude\": 27050, \"groundSpeed\": 400, \"verticalSpeed\": 0, \"heading\": 70, \"type\": \"B738\", \"originIata\": \"OKA\", \"originIcao\": \"ROAH\", \"destinationIata\": \"HND\", \"destinationIcao\": \"RJTT\", \"eta\": \"2024-09-11T12:55:00Z\" }, { \"callsign\": \"ANA123\", \"latitude\": 34.86, \"longitude\": 139.764, \"altitude\": 30000, \"groundSpeed\": 450, \"verticalSpeed\": 0, \"heading\": 60, \"type\": \"B777\", \"originIata\": \"ITM\", \"originIcao\": \"RJOO\", \"destinationIata\": \"NRT\", \"destinationIcao\": \"RJAA\", \"eta\": \"2024-09-11T14:10:00Z\" } ]"
                            )
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Aircraft created successfully",
                            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "[\"Aircraft created: SKY514\", \"Aircraft created: JAL123\"]"))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "All aircraft already exist",
                            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "All aircraft already exist."))
                    )
            }
    )
    @RequestMapping(path = "/create-multiple", method = RequestMethod.POST)
    public ResponseEntity<List<String>> createMultipleAircraft(@RequestBody List<CreateAircraftDto> createAircraftDtos) {
        List<String> createdAircrafts = new ArrayList<>();
        List<String> existingAircrafts = new ArrayList<>();

        for (CreateAircraftDto dto : createAircraftDtos) {
            if (!aircraftRepository.isAircraftExist(new Callsign(dto.callsign))) {
                scenarioService.spawnAircraft(dto);
                createdAircrafts.add("Aircraft created: " + dto.callsign);
            } else {
                existingAircrafts.add("Aircraft already exists: " + dto.callsign);
            }
        }

        if (createdAircrafts.isEmpty()) {
            return ResponseEntity.badRequest().body(existingAircrafts); // If all aircraft already exist, return 400
        }

        createdAircrafts.addAll(existingAircrafts); // Append existing aircraft info to the response
        return ResponseEntity.ok(createdAircrafts);
    }

    @Operation(
            summary = "Create sample aircraft for T09 sector conflict testing",
            description = "This API creates sample aircraft around T09 sector center (34.482, 138.614) for testing conflict detection system with various conflict scenarios.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Sample aircraft created successfully",
                            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "[\"Sample aircraft created: 28 aircraft around T09 sector\"]"))
                    )
            }
    )
    @RequestMapping(path = "/create-haneda-samples", method = RequestMethod.POST)
    public ResponseEntity<List<String>> createHanedaSampleAircraft() {
        List<String> results = new ArrayList<>();
        List<CreateAircraftDto> sampleAircraft = generateT09ConflictTestAircraft();

        int createdCount = 0;
        int existingCount = 0;

        for (CreateAircraftDto dto : sampleAircraft) {
            if (!aircraftRepository.isAircraftExist(new Callsign(dto.callsign))) {
                scenarioService.spawnAircraft(dto);
                createdCount++;
            } else {
                existingCount++;
            }
        }

        results.add("Sample aircraft created: " + createdCount + " new aircraft");
        if (existingCount > 0) {
            results.add("Skipped existing aircraft: " + existingCount);
        }
        results.add("Total aircraft around T09 sector: " + sampleAircraft.size());

        return ResponseEntity.ok(results);
    }

    /**
     * T09セクター中心でのコンフリクト検証用サンプル航空機データを生成
     * 中心座標: 34.482, 138.614 (400km範囲)
     */
    private List<CreateAircraftDto> generateT09ConflictTestAircraft() {
        List<CreateAircraftDto> aircraftList = new ArrayList<>();

        // T09セクター中心座標: 34.482, 138.614
        double centerLat = 34.482;
        double centerLon = 138.614;

        // サンプル航空機データ（28機） - 様々なコンフリクトシナリオ
        String[][] aircraftData = {
            // グループ1: 東西交差シナリオ（同高度での交差）
            {"JAL101", "34.482", "136.614", "31000", "450", "0", "90", "B777", "KIX", "RJBB", "NRT", "RJAA"},  // 西→東
            {"ANA102", "34.482", "140.614", "31000", "460", "0", "270", "B787", "NRT", "RJAA", "KIX", "RJBB"}, // 東→西
            {"SKY103", "34.532", "136.814", "31000", "440", "0", "85", "B737", "ITM", "RJOO", "HND", "RJTT"},  // 西→東（少し北）
            {"JAL104", "34.432", "140.414", "31000", "470", "0", "275", "B777", "HND", "RJTT", "ITM", "RJOO"}, // 東→西（少し南）

            // グループ2: 南北交差シナリオ（同高度での交差）
            {"ANA201", "32.482", "138.614", "32000", "420", "0", "0", "A320", "KOJ", "RJFK", "SDJ", "RJSM"},   // 南→北
            {"SKY202", "36.482", "138.614", "32000", "430", "0", "180", "B738", "SDJ", "RJSM", "KOJ", "RJFK"}, // 北→南
            {"JAL203", "32.682", "138.414", "32000", "410", "0", "10", "B787", "MYJ", "RJOM", "AOJ", "RJSA"},  // 南→北（少し東）
            {"ANA204", "36.282", "138.814", "32000", "440", "0", "190", "A321", "AOJ", "RJSA", "MYJ", "RJOM"}, // 北→南（少し西）

            // グループ3: 収束ポイントシナリオ（中心点に向かって収束）
            {"SKY301", "33.482", "137.614", "30000", "380", "0", "45", "B737", "FUK", "RJFF", "NRT", "RJAA"},  // 南西→中心
            {"JAL302", "35.482", "139.614", "30000", "390", "0", "225", "B777", "NRT", "RJAA", "FUK", "RJFF"}, // 北東→中心
            {"ANA303", "33.482", "139.614", "30000", "400", "0", "315", "A320", "NGO", "RJGG", "ITM", "RJOO"}, // 南東→中心
            {"SKY304", "35.482", "137.614", "30000", "410", "0", "135", "B738", "ITM", "RJOO", "NGO", "RJGG"}, // 北西→中心
            {"JAL305", "34.982", "138.114", "30000", "420", "0", "135", "B787", "TAK", "RJFM", "HND", "RJTT"}, // 北西→中心
            {"ANA306", "33.982", "139.114", "30000", "430", "0", "315", "A321", "HND", "RJTT", "TAK", "RJFM"}, // 南東→中心

            // グループ4: 追い越しシナリオ（同ルート、異なる速度）
            {"SKY401", "34.482", "137.114", "33000", "380", "0", "90", "B737", "HIJ", "RJOA", "NRT", "RJAA"},  // 遅い機
            {"JAL402", "34.482", "136.614", "33000", "480", "0", "90", "B777", "FUK", "RJFF", "NRT", "RJAA"},  // 速い機（後方から追い越し）
            {"ANA403", "34.232", "137.314", "33000", "390", "0", "75", "A320", "KMQ", "RJFK", "NGO", "RJGG"}, // 遅い機
            {"SKY404", "34.132", "136.814", "33000", "470", "0", "75", "B738", "MSJ", "RJOM", "NGO", "RJGG"}, // 速い機（後方から追い越し）

            // グループ5: 高度変更中の接近シナリオ
            {"JAL501", "34.782", "138.314", "29000", "420", "1000", "180", "B777", "SDJ", "RJSM", "FUK", "RJFF"}, // 上昇中
            {"ANA502", "34.182", "138.914", "34000", "430", "-1000", "0", "B787", "FUK", "RJFF", "SDJ", "RJSM"},  // 降下中
            {"SKY503", "34.682", "138.814", "31000", "410", "800", "200", "B737", "UKB", "RJBE", "OKA", "ROAH"}, // 上昇中
            {"JAL504", "34.282", "138.414", "35000", "440", "-800", "20", "B777", "OKA", "ROAH", "UKB", "RJBE"},  // 降下中

            // グループ6: 複雑な交差パターン（対角線交差）
            {"ANA601", "33.282", "137.414", "28000", "450", "0", "60", "A320", "AKJ", "RJEC", "ISG", "RJAG"},   // SW→NE
            {"SKY602", "35.682", "139.814", "28000", "460", "0", "240", "B738", "ISG", "RJAG", "AKJ", "RJEC"},  // NE→SW
            {"JAL603", "33.282", "139.814", "28000", "440", "0", "300", "B787", "MMY", "ROMD", "OGN", "RJSN"},  // SE→NW
            {"ANA604", "35.682", "137.414", "28000", "470", "0", "120", "A321", "OGN", "RJSN", "MMY", "ROMD"},  // NW→SE
            {"SKY605", "34.082", "137.814", "28000", "430", "0", "45", "B737", "NGO", "RJGG", "HND", "RJTT"},   // SW→NE（中央通過）
            {"JAL606", "34.882", "139.414", "28000", "420", "0", "225", "B777", "HND", "RJTT", "NGO", "RJGG"}   // NE→SW（中央通過）
        };

        for (String[] data : aircraftData) {
            CreateAircraftDto dto = new CreateAircraftDto(
                data[0],                                    // callsign
                Double.parseDouble(data[1]),                // latitude
                Double.parseDouble(data[2]),                // longitude
                Integer.parseInt(data[3]),                  // altitude
                Integer.parseInt(data[4]),                  // groundSpeed
                Integer.parseInt(data[5]),                  // verticalSpeed
                Integer.parseInt(data[6]),                  // heading
                data[7],                                    // type
                data[8],                                    // originIata
                data[9],                                    // originIcao
                data[10],                                   // destinationIata
                data[11],                                   // destinationIcao
                "2024-12-13T14:30:00Z"                     // eta
            );

            aircraftList.add(dto);
        }

        return aircraftList;
    }
}
