package jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.api;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.infrastructure.fix.AtsRouteFixPositionRepository;

@RestController
@RequestMapping("/ats")
public class AtsRouteService {
    private final AtsRouteFixPositionRepository atsRouteFixPositionRepository;

    public AtsRouteService(AtsRouteFixPositionRepository atsRouteFixPositionRepository) {
        this.atsRouteFixPositionRepository = atsRouteFixPositionRepository;
    }

    @RequestMapping(path = "/route/all", method = RequestMethod.GET)
    public String getAtsRouteInfo() {
        return atsRouteFixPositionRepository.getRouteInfo();
    }
}
