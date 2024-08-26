package jp.ac.tohoku.qse.takahashi.AtcSimulator.application;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.Aircraft;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Callsign.Callsign;

import java.util.List;

/**
 * Service interface for managing aircraft radar data.
 */
public interface AircraftRadarService {
    /**
     * Retrieves the current state of all aircraft within the airspace.
     *
     * @return a list of all aircraft currently in the airspace.
     */
    List<Aircraft> getAllAircraft();

    /**
     * Retrieves the state of a specific aircraft identified by its callsign.
     *
     * @param callsign the callsign of the aircraft.
     * @return the aircraft with the given callsign, or null if not found.
     */
    Aircraft getAircraftByCallsign(String callsign);

    /**
     * Updates the state of a specific aircraft. This method should be used to apply
     * control instructions or update the aircraft's position based on new radar data.
     *
     * @param callsign the callsign of the aircraft.
     * @param updatedAircraft the updated state of the aircraft.
     * @return true if the update was successful, false otherwise.
     */
    boolean updateAircraft(String callsign, Aircraft updatedAircraft);

    /**
     * Adds a new aircraft to the radar system. This method should be used when a new aircraft
     * enters the airspace and needs to be tracked.
     *
     * @param newAircraft the aircraft to be added.
     * @return true if the addition was successful, false otherwise.
     */
    boolean addAircraft(Aircraft newAircraft);

    /**
     * Removes an aircraft from the radar system. This method should be used when an aircraft
     * leaves the airspace or is no longer being tracked.
     *
     * @param callsign the callsign of the aircraft to be removed.
     * @return true if the removal was successful, false otherwise.
     */
    boolean removeAircraft(String callsign);
}