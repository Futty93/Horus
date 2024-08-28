package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Callsign.Callsign;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.AircraftPosition;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.AircraftVector;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.InstructedVector;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Type.AircraftType;

public class CommercialAircraft extends AircraftBase implements Aircraft {
    private final String originIata;
    private final String originIcao;
    private final String destinationIata;
    private final String destinationIcao;
    private final String eta; //Estimated Time of Arrival in ISO 8601 format

    //Constructor
    public CommercialAircraft(Callsign callsign, AircraftType aircraftType, AircraftPosition aircraftPosition, AircraftVector aircraftVector, String originIata, String originIcao, String destinationIata, String destinationIcao, String eta) {
        super(callsign, aircraftType, aircraftPosition, aircraftVector);
        this.originIata = originIata;
        this.originIcao = originIcao;
        this.destinationIata = destinationIata;
        this.destinationIcao = destinationIcao;
        this.eta = eta;
    }

    /**
     * aircraftVector から 航空機の次の位置を計算する
     * 現在の位置と速度から次の位置を計算し、設定する
     */
    public void calculateNextAircraftPosition() {
        AircraftPosition currentAircraftPosition = this.getAircraftPosition();
        AircraftVector currentAircraftVector = this.getAircraftVector();
        double latitude = currentAircraftPosition.getLatitude() + currentAircraftVector.getGroundSpeed() * Math.cos(Math.toRadians(currentAircraftVector.getHeading()));
        double longitude = currentAircraftPosition.getLongitude() + currentAircraftVector.getGroundSpeed() * Math.sin(Math.toRadians(currentAircraftVector.getHeading()));
        int altitude = (int) (currentAircraftPosition.getAltitude() + currentAircraftVector.getVerticalSpeed());
        this.setAircraftPosition(new AircraftPosition(latitude, longitude, altitude));
    }

    /**
     * instructedVector から 航空機の次のaircraftVectorを計算する
     * 指示された高度と現在の高度を比較し、垂直速度を設定する
     */
    public void calculateNextAircraftVector() {
        InstructedVector instructedVector = this.getInstructedVector();

        // 現在の高度と指示された高度を比較
        int currentAltitude = (int) this.getAircraftPosition().getAltitude();
        int instructedAltitude = instructedVector.getInstructedAltitude();
        int verticalSpeed;

        if (instructedAltitude > currentAltitude) {
            // 指示された高度の方が高い場合
            verticalSpeed = 10;
        } else if (instructedAltitude < currentAltitude) {
            // 指示された高度の方が低い場合
            verticalSpeed = -10;
        } else {
            // 高度が同じ場合
            verticalSpeed = 0;
        }

        // 新しいAircraftVectorを設定
        this.setAircraftVector(new AircraftVector(instructedVector.getInstructedHeading(), instructedVector.getInstructedGroundSpeed(), verticalSpeed));
    }



    @Override
    public String toString() {
        AircraftPosition aircraftPosition = this.getAircraftPosition();
        AircraftVector aircraftVector = this.getAircraftVector();

        return "CommercialAircraft{" +
                "callsign=" + this.getCallsign() +
                ", position={" +
                "latitude=" + aircraftPosition.getLatitude() +
                ", longitude=" + aircraftPosition.getLongitude() +
                ", altitude=" + aircraftPosition.getAltitude() +
                "}, vector={" +
                "heading=" + aircraftVector.getHeading() +
                ", groundSpeed=" + aircraftVector.getGroundSpeed() +
                ", verticalSpeed=" + aircraftVector.getVerticalSpeed() +
                "}, type=" + this.getAircraftType() +
                ", originIata=" + this.originIata +
                ", originIcao=" + this.originIcao +
                ", destinationIata=" + this.destinationIata +
                ", destinationIcao=" + this.destinationIcao +
                ", eta=" + this.eta +
                '}';
    }
}
