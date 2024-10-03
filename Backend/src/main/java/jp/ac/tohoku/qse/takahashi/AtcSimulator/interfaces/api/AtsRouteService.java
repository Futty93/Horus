package jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.api;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.fix.AtsRouteRepository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ats")
public class AtsRouteService {
    private final AtsRouteRepository atsRouteRepository;

    public AtsRouteService(AtsRouteRepository atsRouteRepository) {
        this.atsRouteRepository = atsRouteRepository;
    }

    @RequestMapping(path = "/route/all", method = RequestMethod.GET)
    public String getAtsRouteInfo() {
        return atsRouteRepository.toString();
    }
}
