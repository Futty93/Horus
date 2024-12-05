package jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.api;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.application.atsRoute.AtsRouteServiceImpl;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/ats")
public class AtsRouteService {
    private final AtsRouteServiceImpl atsRouteService = new AtsRouteServiceImpl();

    public AtsRouteService(AtsRouteServiceImpl atsRouteService) throws IOException {
    }

    @RequestMapping(path = "/route/all", method = RequestMethod.GET)
    public String getAtsRouteInfo() {
        return atsRouteService.toString();
    }
}
