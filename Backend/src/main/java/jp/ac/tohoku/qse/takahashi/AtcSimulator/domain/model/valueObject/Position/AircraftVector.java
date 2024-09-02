package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes.GroundSpeed;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes.Heading;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes.VerticalSpeed;

public class AircraftVector {
    public final Heading heading;
    public final GroundSpeed groundSpeed;
    public final VerticalSpeed verticalSpeed;

    public AircraftVector(Heading heading, GroundSpeed groundSpeed, VerticalSpeed verticalSpeed) {
        this.heading = heading;
        this.groundSpeed = groundSpeed;
        this.verticalSpeed = verticalSpeed;
    }
}