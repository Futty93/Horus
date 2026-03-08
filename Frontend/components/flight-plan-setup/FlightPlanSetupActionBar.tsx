"use client";

import React, { useRef } from "react";

interface FlightPlanSetupActionBarProps {
  onLoadTemplate: () => void;
  onLoadTemplateAndSuggest?: () => void;
  onImportJson: (json: string) => void;
  onExportJson: () => void;
  onStartWithThis: () => void;
  status: string | null;
  starting: boolean;
  loadingSuggest?: boolean;
  hasAircraft: boolean;
}

export function FlightPlanSetupActionBar({
  onLoadTemplate,
  onLoadTemplateAndSuggest,
  onImportJson,
  onExportJson,
  onStartWithThis,
  status,
  starting,
  loadingSuggest,
  hasAircraft,
}: FlightPlanSetupActionBarProps) {
  const fileInputRef = useRef<HTMLInputElement>(null);

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;
    const reader = new FileReader();
    reader.onload = () => {
      onImportJson(reader.result as string);
    };
    reader.readAsText(file);
    e.target.value = "";
  };

  return (
    <div className="space-y-2">
      <div className="flex flex-wrap gap-3">
        <button
          type="button"
          onClick={onLoadTemplate}
          disabled={loadingSuggest}
          className="px-4 py-2 text-sm font-bold bg-atc-surface border border-atc-border rounded
                     hover:border-atc-accent text-atc-text disabled:opacity-50"
        >
          Load Haneda Template (28)
        </button>
        {onLoadTemplateAndSuggest && (
          <button
            type="button"
            onClick={onLoadTemplateAndSuggest}
            disabled={loadingSuggest}
            className="px-4 py-2 text-sm font-bold bg-atc-accent text-white rounded
                       hover:opacity-90 disabled:opacity-50"
          >
            {loadingSuggest ? "Loading routes..." : "Load & Suggest Routes"}
          </button>
        )}
        <button
          type="button"
          onClick={() => fileInputRef.current?.click()}
          className="px-4 py-2 text-sm font-bold bg-atc-surface border border-atc-border rounded
                     hover:border-atc-accent text-atc-text"
        >
          Import JSON
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
          className="px-4 py-2 text-sm font-bold bg-atc-surface border border-atc-border rounded
                     hover:border-atc-accent text-atc-text disabled:opacity-50"
        >
          Export JSON
        </button>
        <button
          type="button"
          onClick={onStartWithThis}
          disabled={!hasAircraft || starting}
          className="px-4 py-2 text-sm font-bold bg-atc-accent text-white rounded
                     hover:opacity-90 disabled:opacity-50"
        >
          これで始める
        </button>
      </div>
      {status && (
        <p
          className={`text-sm ${
            status.startsWith("Error") ? "text-atc-danger" : "text-atc-accent"
          }`}
        >
          {status}
        </p>
      )}
    </div>
  );
}
