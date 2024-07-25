package jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces;


import jp.ac.tohoku.qse.takahashi.AtcSimulator.application.AircraftLocationService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/aircraft")
public class LocationService {

    private final AircraftLocationService aircraftLocationService;

    public LocationService(AircraftLocationService aircraftLocationService) {
        this.aircraftLocationService = aircraftLocationService;
    }


    @RequestMapping(path = "/location/all", method = RequestMethod.GET)
    public String getAllAircraftLocation() {
        return aircraftLocationService.getAllAircraftLocation();
    }

    @RequestMapping(path = "/location", method = RequestMethod.GET)
    public String getAircraftLocation(String id) {
        int id_num = Integer.parseInt(id);
        return aircraftLocationService.getAircraftLocation(id_num);
    }
}
