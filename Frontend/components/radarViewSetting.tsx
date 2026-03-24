"use client";
import React, { useState } from "react";
import CollapsiblePanel from "@/components/ui/collapsiblePanel";
import SectorSelector from "@/components/sectorSelector";
import DisplayRangeSetting from "@/components/displayRangeSetting";
import RangeRingsSetting from "@/components/rangeRingsSetting";
import { useDisplayRange } from "@/context/displayRangeContext";
import { useRangeRingsSetting } from "@/context/rangeRingsSettingContext";

const RadarViewSetting = () => {
  const [selectedSector, setSelectedSector] = useState("T09");
  const { displayRange } = useDisplayRange();
  const { rangeRingsSetting } = useRangeRingsSetting();

  const rangeSummary = rangeRingsSetting.enabled
    ? `${rangeRingsSetting.intervalNm}NM`
    : "オフ";
  const summary = `${selectedSector}, ${displayRange.range}km, レンジ${rangeSummary}`;

  return (
    <CollapsiblePanel title="レーダー表示" summary={summary}>
      <div className="space-y-3">
        <SectorSelector
          embedded
          value={selectedSector}
          onChange={setSelectedSector}
        />
        <DisplayRangeSetting embedded />
        <RangeRingsSetting embedded />
      </div>
    </CollapsiblePanel>
  );
};

export default RadarViewSetting;
