package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.fix;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.FixPosition;

import java.util.Optional;

/**
 * Repository for looking up fix positions by name.
 * Implementation lives in infrastructure layer.
 */
public interface FixPositionRepository {
    Optional<FixPosition> findFixPositionByName(String fixName);
}
