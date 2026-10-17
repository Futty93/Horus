"use client";

import React, { useCallback, useState } from "react";
import { AirportCombobox } from "./AirportCombobox";
import type { AirportOption } from "./airportOption";

export type NewAircraftDraft = {
  callsign: string;
  departureAirport: string;
  arrivalAirport: string;
};

interface AddAircraftFormProps {
  existingCallsigns: string[];
  airports: readonly AirportOption[];
  onSubmit: (draft: NewAircraftDraft) => void;
  onCancel?: () => void;
  onStatus?: (msg: string | null) => void;
}

export function AddAircraftForm({
  existingCallsigns,
  airports,
  onSubmit,
  onCancel,
  onStatus,
}: AddAircraftFormProps) {
  const [callsign, setCallsign] = useState("");
  const [departureAirport, setDepartureAirport] = useState("");
  const [arrivalAirport, setArrivalAirport] = useState("");

  const handleSubmit = useCallback(
    (event: React.FormEvent) => {
      event.preventDefault();
      const cs = callsign.trim().toUpperCase();
      if (!cs || !departureAirport || !arrivalAirport) {
        onStatus?.("Error: Callsign, origin, and destination are required");
        return;
      }
      if (departureAirport === arrivalAirport) {
        onStatus?.("Error: Origin and destination must differ");
        return;
      }
      if (existingCallsigns.includes(cs)) {
        onStatus?.(`Error: Callsign ${cs} already exists`);
        return;
      }
      onSubmit({
        callsign: cs,
        departureAirport,
        arrivalAirport,
      });
    },
    [
      arrivalAirport,
      callsign,
      departureAirport,
      existingCallsigns,
      onStatus,
      onSubmit,
    ]
  );

  return (
    <form onSubmit={handleSubmit} className="space-y-3">
      <div>
        <h2 className="text-xs font-semibold uppercase tracking-[0.16em] text-[#8a7d6b]">
          New flight
        </h2>
        <p className="mt-1 text-sm text-[#6b6256]">
          It starts at the origin airport. A route is filled in as soon as you
          place it.
        </p>
      </div>
      <label className="block">
        <span className="mb-1 block text-xs text-[#6b6256]">Callsign</span>
        <input
          type="text"
          value={callsign}
          onChange={(event) => setCallsign(event.target.value.toUpperCase())}
          placeholder="JAL101"
          maxLength={8}
          className="w-full rounded-xl border border-[#e4d8c4] bg-white px-3 py-2 font-[family-name:var(--font-geist-mono)] text-sm text-[#1c1915] outline-none focus:border-[#c2410c]"
        />
      </label>
      <AirportCombobox
        label="Origin"
        airports={airports}
        value={departureAirport}
        onChange={setDepartureAirport}
      />
      <AirportCombobox
        label="Destination"
        airports={airports}
        value={arrivalAirport}
        onChange={setArrivalAirport}
      />
      <div className="flex gap-2">
        <button
          type="submit"
          className="rounded-full bg-[#1c1915] px-4 py-2 text-sm font-semibold text-[#f4efe4] hover:bg-[#3f3830]"
        >
          Place on the chart
        </button>
        {onCancel && (
          <button
            type="button"
            onClick={onCancel}
            className="px-3 py-2 text-sm text-[#6b6256] hover:text-[#1c1915]"
          >
            Cancel
          </button>
        )}
      </div>
    </form>
  );
}
