"use client";
import React from "react";
import CollapsiblePanel from "@/components/ui/collapsiblePanel";
import {
  useRouteInfoDisplaySetting,
  DisplaySettings,
} from "@/context/routeInfoDisplaySettingContext";

const SETTING_LABELS: Record<keyof DisplaySettings, string> = {
  waypointName: "Wpt Name",
  waypointPoint: "Wpt Point",
  radioNavigationAidsName: "Nav Name",
  radioNavigationAidsPoint: "Nav Point",
  atsLowerRoute: "ATS Lower",
  rnavRoute: "RNAV",
};

const RouteInfoDisplaySetting = () => {
  const { isDisplaying, setRouteInfoDisplaySetting } =
    useRouteInfoDisplaySetting();

  const activeItems = (Object.keys(isDisplaying) as (keyof DisplaySettings)[])
    .filter((k) => isDisplaying[k])
    .map((k) => SETTING_LABELS[k]);
  const summary = activeItems.length > 0 ? activeItems.join(", ") : "なし";

  const handleCheckboxChange = (settingKey: keyof DisplaySettings) => {
    return (event: React.ChangeEvent<HTMLInputElement>) => {
      const isChecked = event.target.checked;
      setRouteInfoDisplaySetting((prevSettings) => ({
        ...prevSettings,
        [settingKey]: isChecked,
      }));
    };
  };

  const CustomCheckbox = ({
    id,
    checked,
    onChange,
    label,
  }: {
    id: string;
    checked: boolean;
    onChange: (event: React.ChangeEvent<HTMLInputElement>) => void;
    label: string;
  }) => (
    <label
      htmlFor={id}
      className="flex items-center group cursor-pointer py-0.5"
    >
      <div className="relative">
        <input
          id={id}
          type="checkbox"
          checked={checked}
          onChange={onChange}
          className="sr-only"
        />
        <div
          className={`w-4 h-4 rounded border-2 transition-colors duration-200
                        ${
                          checked
                            ? "bg-atc-accent border-atc-accent"
                            : "bg-atc-surface-elevated border-atc-border hover:border-atc-text-muted"
                        }`}
        >
          {checked && (
            <div className="absolute inset-0 flex items-center justify-center">
              <svg
                className="w-2.5 h-2.5 text-white"
                fill="currentColor"
                viewBox="0 0 20 20"
              >
                <path
                  fillRule="evenodd"
                  d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z"
                  clipRule="evenodd"
                />
              </svg>
            </div>
          )}
        </div>
      </div>
      <span
        className={`ml-2 text-xs font-mono
                       ${checked ? "text-atc-text" : "text-atc-text-muted"}`}
      >
        {label}
      </span>
    </label>
  );

  return (
    <CollapsiblePanel
      id="routeInfoDisplaySetting"
      title="ROUTE DISPLAY SETTINGS"
      summary={summary}
    >
      <div className="space-y-2">
        {[
          {
            label: "Waypoint",
            settings: [
              { key: "waypointName" as keyof DisplaySettings, label: "Name" },
              { key: "waypointPoint" as keyof DisplaySettings, label: "Point" },
            ],
          },
          {
            label: "Radio Navigation AIDs",
            settings: [
              {
                key: "radioNavigationAidsName" as keyof DisplaySettings,
                label: "Name",
              },
              {
                key: "radioNavigationAidsPoint" as keyof DisplaySettings,
                label: "Point",
              },
            ],
          },
        ].map(({ label, settings }) => (
          <div
            key={label}
            className="border border-atc-border rounded p-2 bg-atc-surface-elevated"
          >
            <h4 className="text-atc-text-muted font-semibold font-mono tracking-wide mb-1 text-xs">
              {label}
            </h4>
            <div className="ml-2 space-y-0.5">
              {settings.map(({ key, label: settingLabel }) => (
                <CustomCheckbox
                  key={key}
                  id={key}
                  checked={isDisplaying[key]}
                  onChange={handleCheckboxChange(key)}
                  label={settingLabel}
                />
              ))}
            </div>
          </div>
        ))}

        <div className="space-y-0.5">
          <CustomCheckbox
            id="atsLowerRoute"
            checked={isDisplaying.atsLowerRoute}
            onChange={handleCheckboxChange("atsLowerRoute")}
            label="ATS Lower Route"
          />
          <CustomCheckbox
            id="rnavRoute"
            checked={isDisplaying.rnavRoute}
            onChange={handleCheckboxChange("rnavRoute")}
            label="RNAV Route"
          />
        </div>
      </div>
    </CollapsiblePanel>
  );
};

export default RouteInfoDisplaySetting;
