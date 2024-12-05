package jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.api;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.dto.FlightPlanDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/flight-plan")
public class FlightPlanService {

   @RequestMapping(path = "/submit", method= RequestMethod.POST)
    public ResponseEntity<String> submitFlightPlan(@RequestBody FlightPlanDto flightPlanDto) {
        return ResponseEntity.ok("Flight plan submitted successfully");
   }
}
