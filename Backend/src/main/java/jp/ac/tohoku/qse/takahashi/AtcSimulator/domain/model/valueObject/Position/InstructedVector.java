package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position;

public class InstructedVector {
    private int instructedHeading;
    private int instructedAltitude;
    private int instructedGroundSpeed;

    public InstructedVector(int instructedHeading, int instructedAltitude, int instructedGroundSpeed) {
        this.instructedHeading = instructedHeading;
        this.instructedAltitude = instructedAltitude;
        this.instructedGroundSpeed = instructedGroundSpeed;
    }

    public int getInstructedHeading() {
        return this.instructedHeading;
    }

    public int getInstructedAltitude() {
        return this.instructedAltitude;
    }

    public int getInstructedGroundSpeed() {
        return this.instructedGroundSpeed;
    }

    public void setInstruction(InstructedVector instructedVector) {
        this.instructedHeading = instructedVector.instructedHeading;
        this.instructedAltitude = instructedVector.instructedAltitude;
        this.instructedGroundSpeed = instructedVector.instructedGroundSpeed;
    }
}
