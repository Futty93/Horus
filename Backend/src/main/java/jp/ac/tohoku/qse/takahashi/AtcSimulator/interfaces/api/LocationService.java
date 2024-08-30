package jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.api;


import jp.ac.tohoku.qse.takahashi.AtcSimulator.application.AircraftRadarService;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.AircraftRepository;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Callsign.Callsign;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Callsign.Company;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Callsign.FlightNumber;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/aircraft")
public class LocationService {
    private final AircraftRadarService aircraftRadarService;
    private final AircraftRepository aircraftRepository;

    public LocationService(AircraftRadarService aircraftRadarService, AircraftRepository aircraftRepository) {
        this.aircraftRadarService = aircraftRadarService;
        this.aircraftRepository = aircraftRepository;
    }

    @RequestMapping(path = "/location/all", method = RequestMethod.GET)
    public String getAllAircraftLocation() {
        return aircraftRadarService.getAllAircraftLocation();
    }

    @RequestMapping(path = "/location", method = RequestMethod.GET)
    public ResponseEntity<String> getAircraftLocation(String callsign) {
        if (aircraftRepository.isAircraftExist(new Callsign(callsign))) {
            return ResponseEntity.ok(aircraftRadarService.getAircraftLocation(new Callsign(callsign)));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Aircraft with callsign " + callsign + " does not exist.");
        }
    }
}
