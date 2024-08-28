package jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.api;


import jp.ac.tohoku.qse.takahashi.AtcSimulator.application.AircraftRadarService;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Callsign.Callsign;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Callsign.Company;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Callsign.FlightNumber;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/aircraft")
public class LocationService {

    private final AircraftRadarService aircraftRadarService;

    public LocationService(AircraftRadarService aircraftRadarService) {
        this.aircraftRadarService = aircraftRadarService;
    }


    @RequestMapping(path = "/location/all", method = RequestMethod.GET)
    public String getAllAircraftLocation() {
        return aircraftRadarService.getAllAircraftLocation();
    }

    @RequestMapping(path = "/location", method = RequestMethod.GET)
    public String getAircraftLocation(String companyName,String flightNumber) {
        Callsign callsign = new Callsign(new Company(companyName),new FlightNumber(flightNumber));
        return aircraftRadarService.getAircraftLocation(callsign);
    }

}
