package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position;

public class AircraftVector {
    private int heading;
    private int groundSpeed;
    private int verticalSpeed;

    public AircraftVector(int heading, int groundSpeed, int verticalSpeed) {
        this.heading = heading;
        this.groundSpeed = groundSpeed;
        this.verticalSpeed = verticalSpeed;
    }

    public int getHeading() {
        return this.heading;
    }

    public int getGroundSpeed() {
        return this.groundSpeed;
    }

    public int getVerticalSpeed() {
        return this.verticalSpeed;
    }

    public void setHeading(int heading) {
        this.heading = heading;
    }

    public void setGroundSpeed(int groundSpeed) {
        this.groundSpeed = groundSpeed;
    }

    public void setVerticalSpeed(int verticalSpeed) {
        this.verticalSpeed = verticalSpeed;
    }
}