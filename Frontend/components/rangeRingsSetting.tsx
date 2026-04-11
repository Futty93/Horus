"use client";
import React from "react";
import {
  useRangeRingsSetting,
  RANGE_RINGS_INTERVAL_OPTIONS,
} from "@/context/rangeRingsSettingContext";

const RangeRingsSetting = ({ embedded = false }: { embedded?: boolean }) => {
  const { rangeRingsSetting, setRangeRingsSetting } = useRangeRingsSetting();

  const content = (
    <>
      <div className="flex items-center justify-between mb-2">
        <label className="font-bold text-atc-text font-mono tracking-wider text-xs">
          レンジリング:
        </label>
        <label className="flex items-center gap-2 cursor-pointer">
          <input
            type="checkbox"
            checked={rangeRingsSetting.enabled}
            onChange={(e) =>
              setRangeRingsSetting((prev) => ({
                ...prev,
                enabled: e.target.checked,
              }))
            }
            className="rounded border-atc-border bg-atc-surface-elevated text-atc-accent focus:ring-atc-accent"
          />
          <span className="text-atc-text text-xs">表示</span>
        </label>
      </div>
      {rangeRingsSetting.enabled && (
        <div className="flex items-center justify-between">
          <label
            htmlFor="rangeRingsInterval"
            className="text-atc-text-muted font-mono text-xs"
          >
            間隔:
          </label>
          <select
            id="rangeRingsInterval"
            value={rangeRingsSetting.intervalNm}
            onChange={(e) =>
              setRangeRingsSetting((prev) => ({
                ...prev,
                intervalNm: Number(e.target.value),
              }))
            }
            className="px-2 py-1 bg-atc-surface-elevated border border-atc-border rounded
                       text-atc-text text-center font-mono text-xs
                       focus:outline-none focus:border-atc-accent"
          >
            {RANGE_RINGS_INTERVAL_OPTIONS.map((nm) => (
              <option key={nm} value={nm}>
                {nm} NM
              </option>
            ))}
          </select>
        </div>
      )}
    </>
  );

  if (embedded) {
    return content;
  }
  return (
    <div className="bg-atc-surface border border-atc-border rounded-lg p-3 mb-3">
      {content}
    </div>
  );
};

export default RangeRingsSetting;
