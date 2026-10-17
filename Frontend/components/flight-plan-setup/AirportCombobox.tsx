"use client";

import React, { useId, useMemo, useState } from "react";
import {
  airportLabel,
  filterAirports,
  type AirportOption,
} from "./airportOption";

interface AirportComboboxProps {
  label: string;
  airports: readonly AirportOption[];
  value: string;
  onChange: (icaoCode: string) => void;
}

export function AirportCombobox({
  label,
  airports,
  value,
  onChange,
}: AirportComboboxProps) {
  const listId = useId();
  const selected = airports.find((airport) => airport.icaoCode === value);
  const [query, setQuery] = useState("");
  const [editing, setEditing] = useState(false);
  const matches = useMemo(
    () => filterAirports(airports, query),
    [airports, query]
  );

  return (
    <label className="block">
      <span className="mb-1 block text-xs text-[#6b6256]">{label}</span>
      <input
        type="text"
        role="combobox"
        aria-expanded={editing}
        aria-controls={listId}
        aria-autocomplete="list"
        value={editing ? query : selected ? airportLabel(selected) : ""}
        placeholder={
          airports.length === 0 ? "Loading airports…" : "ICAO, IATA, or name"
        }
        onChange={(event) => {
          setQuery(event.target.value);
          setEditing(true);
          if (value) onChange("");
        }}
        onFocus={() => {
          setQuery("");
          setEditing(true);
        }}
        onKeyDown={(event) => {
          if (event.key !== "Enter" || matches.length === 0) return;
          event.preventDefault();
          onChange(matches[0].icaoCode);
          setQuery("");
          setEditing(false);
        }}
        onBlur={() => {
          window.setTimeout(() => setEditing(false), 120);
        }}
        className="w-full rounded-xl border border-[#e4d8c4] bg-white px-3 py-2 font-[family-name:var(--font-geist-mono)] text-sm text-[#1c1915] outline-none focus:border-[#c2410c]"
        autoComplete="off"
      />
      {editing && (
        <ul
          id={listId}
          role="listbox"
          className="mt-1 max-h-48 overflow-y-auto rounded-xl border border-[#e4d8c4] bg-white text-sm shadow-sm"
        >
          {matches.length === 0 ? (
            <li className="px-3 py-2 text-xs text-[#8a7d6b]">
              No matching airport
            </li>
          ) : (
            matches.map((airport) => (
              <li
                key={airport.icaoCode}
                role="option"
                aria-selected={airport.icaoCode === value}
              >
                <button
                  type="button"
                  className="w-full px-3 py-2 text-left hover:bg-[#f4efe4]"
                  onMouseDown={(event) => {
                    event.preventDefault();
                    onChange(airport.icaoCode);
                    setQuery("");
                    setEditing(false);
                  }}
                >
                  <span className="font-[family-name:var(--font-geist-mono)] text-[#1c1915]">
                    {airport.icaoCode}
                  </span>
                  {airport.name && (
                    <span className="ml-2 text-[#6b6256]">{airport.name}</span>
                  )}
                  {airport.iataCode && (
                    <span className="ml-1 text-[#8a7d6b]">
                      ({airport.iataCode})
                    </span>
                  )}
                </button>
              </li>
            ))
          )}
        </ul>
      )}
    </label>
  );
}
