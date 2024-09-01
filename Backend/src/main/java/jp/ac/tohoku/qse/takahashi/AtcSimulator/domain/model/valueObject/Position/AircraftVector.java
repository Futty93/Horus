package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes.GroundSpeed;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes.Heading;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes.VerticalSpeed;

public class AircraftVector {
    final Heading heading;
    final GroundSpeed groundSpeed;
    final VerticalSpeed verticalSpeed;

    public AircraftVector(int heading, int groundSpeed, int verticalSpeed) {
        this.heading = new Heading(heading);
        this.groundSpeed = new GroundSpeed(groundSpeed);
        this.verticalSpeed = new VerticalSpeed(verticalSpeed);
    }
}