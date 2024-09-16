package jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.api;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.AircraftBase;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.AircraftRepository;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Callsign.Callsign;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.dto.AircragtHighlightDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/highlight")
public class AircraftHighlightService {
    private final AircraftRepository aircraftRepository;

    public AircraftHighlightService(AircraftRepository aircraftRepository) {
        this.aircraftRepository = aircraftRepository;
    }

    @RequestMapping(path = "/", method = RequestMethod.POST)
    public ResponseEntity<List<String>> highlightAircraft(@RequestBody List<AircragtHighlightDto> aircragtHighlightDtos) {
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
            return ResponseEntity.badRequest().body(notExistAircrafts);
        }

        return ResponseEntity.ok(highlightAircrafts);
    }
}
