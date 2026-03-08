package jp.ac.tohoku.qse.takahashi.AtcSimulator.application;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.exception.AircraftNotFoundException;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.Aircraft;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.AircraftBase;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.AircraftRepository;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Callsign.Callsign;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.types.commercial.CommercialAircraft;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Conflict.RiskAssessment;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.dto.AircraftLocationDto;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.shared.utility.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * Use case: get all aircraft locations with risk assessment for radar display.
 */
public class GetAllAircraftLocationsWithRiskUseCase {

    private final AircraftRepository aircraftRepository;
    private final ConflictAlertService conflictAlertService;

    public GetAllAircraftLocationsWithRiskUseCase(AircraftRepository aircraftRepository,
                                                  ConflictAlertService conflictAlertService) {
        this.aircraftRepository = aircraftRepository;
        this.conflictAlertService = conflictAlertService;
    }

    public List<AircraftLocationDto> execute() {
        List<Aircraft> allAircraft = aircraftRepository.findAll();
        Map<String, RiskAssessment> allConflicts = conflictAlertService.getAllConflictAlerts();

        return allAircraft.stream()
                .map(aircraft -> toDto(aircraft, allConflicts, allAircraft))
                .toList();
    }

    public AircraftLocationDto execute(String callsign) {
        if (!aircraftRepository.isAircraftExist(new Callsign(callsign))) {
            throw new AircraftNotFoundException(callsign);
        }
        return execute().stream()
                .filter(dto -> dto.callsign().equals(callsign))
                .findFirst()
                .orElseThrow();
    }

    private AircraftLocationDto toDto(Aircraft aircraft, Map<String, RiskAssessment> allConflicts,
                                      List<Aircraft> allAircraft) {
        if (!(aircraft instanceof AircraftBase base)) {
            return fallbackDto(aircraft, 0.0);
        }

        double riskLevel = calculateMaxRiskForAircraft(
                base.getCallsign().toString(), allConflicts, allAircraft);

        String originIata = "";
        String originIcao = "";
        String destinationIata = "";
        String destinationIcao = "";
        String eta = "";

        if (aircraft instanceof CommercialAircraft commercial) {
            originIata = commercial.getOriginIata();
            originIcao = commercial.getOriginIcao();
            destinationIata = commercial.getDestinationIata();
            destinationIcao = commercial.getDestinationIcao();
            eta = commercial.getEta();
        }

        return new AircraftLocationDto(
                base.getCallsign().toString(),
                new AircraftLocationDto.PositionDto(
                        base.getAircraftPosition().latitude.toDouble(),
                        base.getAircraftPosition().longitude.toDouble(),
                        base.getAircraftPosition().altitude.toDouble()
                ),
                new AircraftLocationDto.VectorDto(
                        base.getAircraftVector().heading.toDouble(),
                        base.getAircraftVector().groundSpeed.toDouble(),
                        base.getAircraftVector().verticalSpeed.toDouble()
                ),
                new AircraftLocationDto.InstructedVectorDto(
                        base.getInstructedVector().instructedHeading.toDouble(),
                        base.getInstructedVector().instructedGroundSpeed.toDouble(),
                        base.getInstructedVector().instructedAltitude.toDouble()
                ),
                base.getCharacteristics().getCategory().name(),
                base.getAircraftType().toString(),
                originIata,
                originIcao,
                destinationIata,
                destinationIcao,
                eta,
                riskLevel
        );
    }

    private AircraftLocationDto fallbackDto(Aircraft aircraft, double riskLevel) {
        return new AircraftLocationDto(
                aircraft.getCallsign().toString(),
                new AircraftLocationDto.PositionDto(0, 0, 0),
                new AircraftLocationDto.VectorDto(0, 0, 0),
                new AircraftLocationDto.InstructedVectorDto(0, 0, 0),
                "UNKNOWN",
                "UNKNOWN",
                "", "", "", "", "",
                riskLevel
        );
    }

    private double calculateMaxRiskForAircraft(String targetCallsign,
                                              Map<String, RiskAssessment> allConflicts,
                                              List<Aircraft> allAircraft) {
        double maxRisk = 0.0;
        for (Aircraft other : allAircraft) {
            if (!other.getCallsign().toString().equals(targetCallsign)) {
                String pairId = StringUtils.generatePairId(targetCallsign, other.getCallsign().toString());
                RiskAssessment risk = allConflicts.get(pairId);
                if (risk != null && risk.getRiskLevel() > maxRisk) {
                    maxRisk = risk.getRiskLevel();
                }
            }
        }
        return maxRisk;
    }
}
