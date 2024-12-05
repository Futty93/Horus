package jp.ac.tohoku.qse.takahashi.AtcSimulator.application.atsRoute;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.fix.AtsRouteRepository;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.FixPosition;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Optional;

@Service
public class AtsRouteServiceImpl {

    private final AtsRouteRepository atsRouteRepository = new AtsRouteRepository();

    public AtsRouteServiceImpl() throws IOException {
    }

    public String toString() {
        return atsRouteRepository.toString();
    }

    public Optional<FixPosition> findFixPositionByName(String fixName) {
        return atsRouteRepository.findFixPositionByName(fixName);
    }
}
