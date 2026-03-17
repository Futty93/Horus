"use client";

import React, { useState, useCallback } from "react";
import type { ScenarioAircraft, InitialPositionDto } from "@/types/scenario";

interface AddAircraftFormProps {
  existingCallsigns: string[];
  airportPositions: Map<string, { latitude: number; longitude: number }>;
  onSubmit: (aircraft: ScenarioAircraft) => void;
  onCancel: () => void;
  onStatus?: (msg: string | null) => void;
}

const DEFAULT_POSITION: InitialPositionDto = {
  latitude: 34.5,
  longitude: 138.5,
  altitude: 31000,
  heading: 90,
  groundSpeed: 450,
  verticalSpeed: 0,
};

export function AddAircraftForm({
  existingCallsigns,
  airportPositions,
  onSubmit,
  onCancel,
  onStatus,
}: AddAircraftFormProps) {
  const [callsign, setCallsign] = useState("");
  const [departureAirport, setDepartureAirport] = useState("");
  const [arrivalAirport, setArrivalAirport] = useState("");
  const [lat, setLat] = useState(DEFAULT_POSITION.latitude.toString());
  const [lon, setLon] = useState(DEFAULT_POSITION.longitude.toString());
  const [altitude, setAltitude] = useState(
    DEFAULT_POSITION.altitude.toString()
  );
  const [heading, setHeading] = useState(DEFAULT_POSITION.heading.toString());
  const [groundSpeed, setGroundSpeed] = useState(
    DEFAULT_POSITION.groundSpeed.toString()
  );
  const [verticalSpeed, setVerticalSpeed] = useState(
    DEFAULT_POSITION.verticalSpeed.toString()
  );

  const applyDeparturePosition = useCallback(() => {
    const icao = departureAirport.trim().toUpperCase();
    if (icao.length !== 4) return;
    const pos = airportPositions.get(icao);
    if (pos) {
      setLat(pos.latitude.toString());
      setLon(pos.longitude.toString());
      onStatus?.(`Set position from ${icao}`);
    }
  }, [departureAirport, airportPositions, onStatus]);

  const handleSubmit = useCallback(
    (e: React.FormEvent) => {
      e.preventDefault();
      const cs = callsign.trim().toUpperCase();
      const dep = departureAirport.trim().toUpperCase();
      const arr = arrivalAirport.trim().toUpperCase();

      if (!cs || !dep || !arr) {
        onStatus?.("Error: Callsign, Origin, and Dest are required");
        return;
      }
      if (dep.length !== 4 || arr.length !== 4) {
        onStatus?.("Error: Origin and Dest must be 4-character ICAO codes");
        return;
      }
      if (existingCallsigns.includes(cs)) {
        onStatus?.(`Error: Callsign ${cs} already exists`);
        return;
      }

      const aircraft: ScenarioAircraft = {
        flightPlan: {
          callsign: cs,
          departureAirport: dep,
          arrivalAirport: arr,
          cruiseAltitude: 31000,
          cruiseSpeed: 450,
          route: [],
        },
        initialPosition: {
          latitude: Number(lat) || DEFAULT_POSITION.latitude,
          longitude: Number(lon) || DEFAULT_POSITION.longitude,
          altitude: Number(altitude) || DEFAULT_POSITION.altitude,
          heading: Number(heading) || DEFAULT_POSITION.heading,
          groundSpeed: Number(groundSpeed) || DEFAULT_POSITION.groundSpeed,
          verticalSpeed:
            Number(verticalSpeed) ?? DEFAULT_POSITION.verticalSpeed,
        },
      };
      onSubmit(aircraft);
      onStatus?.(`Added ${cs}`);
    },
    [
      callsign,
      departureAirport,
      arrivalAirport,
      lat,
      lon,
      altitude,
      heading,
      groundSpeed,
      verticalSpeed,
      existingCallsigns,
      onSubmit,
      onStatus,
    ]
  );

  return (
    <form
      onSubmit={handleSubmit}
      className="border border-atc-border rounded-lg p-4 bg-atc-surface space-y-3"
    >
      <h3 className="font-mono text-sm font-bold text-atc-text">
        Add Aircraft
      </h3>
      <div className="grid grid-cols-2 gap-3 text-sm">
        <label>
          <span className="block text-atc-text-muted text-xs mb-1">
            Callsign
          </span>
          <input
            type="text"
            value={callsign}
            onChange={(e) => setCallsign(e.target.value.toUpperCase())}
            placeholder="JAL101"
            maxLength={8}
            className="w-full px-3 py-1.5 bg-atc-bg border border-atc-border rounded
                       text-atc-text font-mono focus:outline-none focus:border-atc-accent"
          />
        </label>
        <label>
          <span className="block text-atc-text-muted text-xs mb-1">Origin</span>
          <input
            type="text"
            value={departureAirport}
            onChange={(e) =>
              setDepartureAirport(e.target.value.toUpperCase().slice(0, 4))
            }
            placeholder="RJTT"
            maxLength={4}
            className="w-full px-3 py-1.5 bg-atc-bg border border-atc-border rounded
                       text-atc-text font-mono focus:outline-none focus:border-atc-accent"
          />
        </label>
        <label>
          <span className="block text-atc-text-muted text-xs mb-1">Dest</span>
          <input
            type="text"
            value={arrivalAirport}
            onChange={(e) =>
              setArrivalAirport(e.target.value.toUpperCase().slice(0, 4))
            }
            placeholder="RJAA"
            maxLength={4}
            className="w-full px-3 py-1.5 bg-atc-bg border border-atc-border rounded
                       text-atc-text font-mono focus:outline-none focus:border-atc-accent"
          />
        </label>
      </div>
      <div className="space-y-2">
        <span className="block text-atc-text-muted text-xs">
          Initial Position
        </span>
        <div className="grid grid-cols-3 gap-2 text-sm">
          <input
            type="number"
            value={lat}
            onChange={(e) => setLat(e.target.value)}
            step="any"
            placeholder="Lat"
            className="px-2 py-1.5 bg-atc-bg border border-atc-border rounded text-atc-text
                       focus:outline-none focus:border-atc-accent"
          />
          <input
            type="number"
            value={lon}
            onChange={(e) => setLon(e.target.value)}
            step="any"
            placeholder="Lon"
            className="px-2 py-1.5 bg-atc-bg border border-atc-border rounded text-atc-text
                       focus:outline-none focus:border-atc-accent"
          />
          <button
            type="button"
            onClick={applyDeparturePosition}
            className="px-2 py-1.5 text-xs bg-atc-surface border border-atc-border rounded
                       text-atc-text hover:border-atc-accent"
          >
            From Origin
          </button>
        </div>
        <div className="grid grid-cols-3 gap-2 text-sm">
          <input
            type="number"
            value={altitude}
            onChange={(e) => setAltitude(e.target.value)}
            placeholder="Alt (ft)"
            className="px-2 py-1.5 bg-atc-bg border border-atc-border rounded text-atc-text
                       focus:outline-none focus:border-atc-accent"
          />
          <input
            type="number"
            value={heading}
            onChange={(e) => setHeading(e.target.value)}
            min={0}
            max={359}
            placeholder="Hdg"
            className="px-2 py-1.5 bg-atc-bg border border-atc-border rounded text-atc-text
                       focus:outline-none focus:border-atc-accent"
          />
          <input
            type="number"
            value={groundSpeed}
            onChange={(e) => setGroundSpeed(e.target.value)}
            placeholder="GS"
            className="px-2 py-1.5 bg-atc-bg border border-atc-border rounded text-atc-text
                       focus:outline-none focus:border-atc-accent"
          />
        </div>
        <input
          type="number"
          value={verticalSpeed}
          onChange={(e) => setVerticalSpeed(e.target.value)}
          placeholder="VS"
          className="w-24 px-2 py-1.5 bg-atc-bg border border-atc-border rounded text-atc-text text-sm
                     focus:outline-none focus:border-atc-accent"
        />
      </div>
      <div className="flex gap-2">
        <button
          type="submit"
          className="px-3 py-1.5 text-xs font-bold bg-atc-accent text-white rounded
                     hover:opacity-90"
        >
          Add
        </button>
        <button
          type="button"
          onClick={onCancel}
          className="px-3 py-1.5 text-xs font-bold bg-atc-surface border border-atc-border rounded
                     text-atc-text hover:border-atc-accent"
        >
          Cancel
        </button>
      </div>
    </form>
  );
}
