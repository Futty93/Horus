"use client";
import React from "react";
import CollapsiblePanel from "@/components/ui/collapsiblePanel";
import { useDataBlockDisplaySetting } from "@/context/dataBlockDisplaySettingContext";

type DataBlockDisplaySettingPanelProps = {
  /** Operator 画面では管制クリアランスメモ行は描画しない（spec: 管制側の状況認識用）。 */
  variant?: "controller" | "operator";
};

const DataBlockDisplaySetting = ({
  variant = "controller",
}: DataBlockDisplaySettingPanelProps) => {
  const { dataBlockDisplaySetting, setDataBlockDisplaySetting } =
    useDataBlockDisplaySetting();

  const activeItems: string[] = [];
  if (dataBlockDisplaySetting.aircraftType) activeItems.push("機種");
  if (dataBlockDisplaySetting.eta) activeItems.push("ETA");
  if (variant === "controller" && dataBlockDisplaySetting.atcClearanceMemo)
    activeItems.push("管制メモ");
  if (dataBlockDisplaySetting.squawk) activeItems.push("スクオーク");

  const summary =
    activeItems.length > 0 ? activeItems.join(", ") : "追加項目なし";

  return (
    <CollapsiblePanel title="データブロック" summary={summary}>
      <div className="space-y-2">
        <label className="flex items-center gap-2 cursor-pointer">
          <input
            type="checkbox"
            checked={dataBlockDisplaySetting.aircraftType}
            onChange={(e) =>
              setDataBlockDisplaySetting((prev) => ({
                ...prev,
                aircraftType: e.target.checked,
              }))
            }
            className="rounded border-atc-border bg-atc-surface-elevated text-atc-accent focus:ring-atc-accent"
          />
          <span className="text-atc-text text-xs">機種</span>
        </label>
        <label className="flex items-center gap-2 cursor-pointer">
          <input
            type="checkbox"
            checked={dataBlockDisplaySetting.eta}
            onChange={(e) =>
              setDataBlockDisplaySetting((prev) => ({
                ...prev,
                eta: e.target.checked,
              }))
            }
            className="rounded border-atc-border bg-atc-surface-elevated text-atc-accent focus:ring-atc-accent"
          />
          <span className="text-atc-text text-xs">ETA</span>
        </label>
        {variant === "controller" && (
          <label className="flex items-center gap-2 cursor-pointer">
            <input
              type="checkbox"
              checked={dataBlockDisplaySetting.atcClearanceMemo}
              onChange={(e) =>
                setDataBlockDisplaySetting((prev) => ({
                  ...prev,
                  atcClearanceMemo: e.target.checked,
                }))
              }
              className="rounded border-atc-border bg-atc-surface-elevated text-atc-accent focus:ring-atc-accent"
            />
            <span className="text-atc-text text-xs">
              管制クリアランスメモ行
            </span>
          </label>
        )}
        <label className="flex items-center gap-2 cursor-pointer">
          <input
            type="checkbox"
            checked={dataBlockDisplaySetting.squawk}
            onChange={(e) =>
              setDataBlockDisplaySetting((prev) => ({
                ...prev,
                squawk: e.target.checked,
              }))
            }
            className="rounded border-atc-border bg-atc-surface-elevated text-atc-accent focus:ring-atc-accent"
          />
          <span className="text-atc-text text-xs">スクオーク</span>
          <span className="text-atc-text-muted text-xs">(3-1で有効化予定)</span>
        </label>
      </div>
    </CollapsiblePanel>
  );
};

export default DataBlockDisplaySetting;
