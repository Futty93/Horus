package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.fix;

import java.util.Optional;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.FixPosition;

/**
 * Repository for looking up fix positions by name.
 * Implementation lives in infrastructure layer.
 */
public interface FixPositionRepository {
    Optional<FixPosition> findFixPositionByName(String fixName);
}
