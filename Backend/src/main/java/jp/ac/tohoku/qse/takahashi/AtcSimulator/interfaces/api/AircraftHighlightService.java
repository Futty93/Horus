package jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.api;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.config.globals.GlobalVariables;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.Aircraft;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.AircraftBase;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.AircraftRepository;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftHighlight.CallsignExtructStatus;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Callsign.Callsign;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.dto.AircragtHighlightDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/highlight")
public class AircraftHighlightService {
    private final AircraftRepository aircraftRepository;

    public AircraftHighlightService(AircraftRepository aircraftRepository) {
        this.aircraftRepository = aircraftRepository;
    }

    @RequestMapping(path = "/", method = RequestMethod.POST)
    public ResponseEntity<List<String>> highlightAircraft(@RequestParam String callsignStatus, @RequestBody List<AircragtHighlightDto> aircragtHighlightDtos) {
//        登録されている航空機をすべて取得し、ハイライトをリセットする
        List<AircraftBase> aircraftBases = aircraftRepository.findAll().stream()
                .filter(aircraft -> aircraft instanceof AircraftBase)
                .map(aircraft -> (AircraftBase) aircraft)
                .toList();

        for (AircraftBase aircraftBase : aircraftBases) {
            aircraftBase.highlight = aircraftBase.highlight.changeHighlightRank(0);
        }

        // callsignStatusがnullの場合、リクエストが不正であるとして400を返す
        if (callsignStatus == null) {
            return ResponseEntity.badRequest().body(new ArrayList<>());
        }

        // callsignStatusが"NO_VALUE"の場合、CallsignExtructStatusをNO_VALUEに設定し、リクエストが成功したことを示す200を返す
        if (callsignStatus.equals("NO_VALUE")) {
            GlobalVariables.callsignExtructStatus = CallsignExtructStatus.NO_VALUE;
            return ResponseEntity.ok(Collections.singletonList("Callsign extraction status to be reset"));
        }

        // callsignStatusが"FAILURE"の場合、CallsignExtructStatusをFAILUREに設定し、リクエストが成功したことを示す200を返す
        if (callsignStatus.equals("FAILURE")) {
            GlobalVariables.callsignExtructStatus = CallsignExtructStatus.FAILURE;
            return ResponseEntity.ok(Collections.singletonList("Callsign extraction failed"));
        }

        // callsignStatusが"SUCCESS"の場合、CallsignExtructStatusをSUCCESSに設定し、リクエストが成功したことを示す200を返す
        List<String> highlightAircrafts = new ArrayList<>();
        List<String> notExistAircrafts = new ArrayList<>();

        for (AircragtHighlightDto aircragtHighlightDto : aircragtHighlightDtos) {
            if (aircraftRepository.isAircraftExist(new Callsign(aircragtHighlightDto.callsign))) {
                AircraftBase highlightedAircraft = (AircraftBase) aircraftRepository.findByCallsign(new Callsign(aircragtHighlightDto.callsign));
                highlightedAircraft.highlight = highlightedAircraft.highlight.changeHighlightRank(aircragtHighlightDto.rank);
                highlightAircrafts.add("Aircraft highlighted: " + aircragtHighlightDto.callsign);
            } else {
                notExistAircrafts.add("Aircraft is not exists: " + aircragtHighlightDto.callsign);
            }
        }

        if (highlightAircrafts.isEmpty()) {
            GlobalVariables.callsignExtructStatus = CallsignExtructStatus.FAILURE;
            return ResponseEntity.badRequest().body(notExistAircrafts);
        }

        GlobalVariables.callsignExtructStatus = CallsignExtructStatus.SUCCESS;
        return ResponseEntity.ok(highlightAircrafts);
    }
}
