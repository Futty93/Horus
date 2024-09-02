package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes.Altitude;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes.GroundSpeed;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes.Heading;

public class InstructedVector {
    public final Heading instructedHeading;
    public final Altitude instructedAltitude;
    public final GroundSpeed instructedGroundSpeed;

    public InstructedVector(Heading instructedHeading, Altitude instructedAltitude, GroundSpeed instructedGroundSpeed) {
        this.instructedHeading = instructedHeading;
        this.instructedAltitude = instructedAltitude;
        this.instructedGroundSpeed = instructedGroundSpeed;
    }
}
