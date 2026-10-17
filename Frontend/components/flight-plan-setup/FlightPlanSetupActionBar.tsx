"use client";

import React, { useRef } from "react";

interface FlightPlanSetupActionBarProps {
  onLoadTemplate: () => void;
  onLoadTemplateAndSuggest?: () => void;
  onImportJson: (json: string) => void;
  onExportJson: () => void;
  loadingSuggest?: boolean;
  hasAircraft: boolean;
  dataLoadWarning?: string | null;
}

export function FlightPlanSetupActionBar({
  onLoadTemplate,
  onLoadTemplateAndSuggest,
  onImportJson,
  onExportJson,
  loadingSuggest,
  hasAircraft,
  dataLoadWarning,
}: FlightPlanSetupActionBarProps) {
  const fileInputRef = useRef<HTMLInputElement>(null);

  const handleFileChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    if (!file) return;
    const reader = new FileReader();
    reader.onload = () => {
      onImportJson(reader.result as string);
    };
    reader.readAsText(file);
    event.target.value = "";
  };

  const toolClass =
    "text-sm text-[#6b6256] underline-offset-4 hover:text-[#1c1915] hover:underline disabled:opacity-40";

  return (
    <div className="space-y-2">
      <div className="flex flex-wrap gap-x-4 gap-y-1">
        <button
          type="button"
          onClick={onLoadTemplate}
          disabled={loadingSuggest}
          className={toolClass}
        >
          Haneda sample
        </button>
        {onLoadTemplateAndSuggest && (
          <button
            type="button"
            onClick={onLoadTemplateAndSuggest}
            disabled={loadingSuggest}
            className={toolClass}
          >
            {loadingSuggest ? "Suggesting routes…" : "Sample with routes"}
          </button>
        )}
        <button
          type="button"
          onClick={() => fileInputRef.current?.click()}
          className={toolClass}
        >
          Import
        </button>
        <input
          ref={fileInputRef}
          type="file"
          accept=".json,application/json"
          onChange={handleFileChange}
          className="hidden"
        />
        <button
          type="button"
          onClick={onExportJson}
          disabled={!hasAircraft}
          className={toolClass}
        >
          Export
        </button>
      </div>
      {dataLoadWarning && (
        <p className="text-sm whitespace-pre-line text-[#b45309]">
          {dataLoadWarning}
        </p>
      )}
    </div>
  );
}
