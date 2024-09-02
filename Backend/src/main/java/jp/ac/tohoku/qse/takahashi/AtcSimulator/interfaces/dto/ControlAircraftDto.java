package jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.dto;

import jakarta.validation.constraints.NotBlank;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.AircraftBase;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes.Altitude;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes.GroundSpeed;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes.Heading;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.InstructedVector;

public record ControlAircraftDto(@NotBlank int instructedHeading, @NotBlank int instructedAltitude,
                                 @NotBlank int instructedGroundSpeed) {
    public ControlAircraftDto(int instructedHeading, int instructedAltitude, int instructedGroundSpeed) {
        this.instructedHeading = instructedHeading;
        this.instructedAltitude = instructedAltitude;
        this.instructedGroundSpeed = instructedGroundSpeed;
    }

    public void setInstruction(AircraftBase aircraftBase) {
        InstructedVector instructedVector = new InstructedVector(new Heading(this.instructedHeading), new Altitude(this.instructedAltitude), new GroundSpeed(this.instructedGroundSpeed));
        aircraftBase.setInstructedVector(instructedVector);
    }
}
