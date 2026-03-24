"use client";
import React, { useState } from "react";

interface CollapsiblePanelProps {
  title: string;
  summary: string;
  children: React.ReactNode;
  id?: string;
  defaultExpanded?: boolean;
}

const CollapsiblePanel = ({
  title,
  summary,
  children,
  id,
  defaultExpanded = false,
}: CollapsiblePanelProps) => {
  const [expanded, setExpanded] = useState(defaultExpanded);
  const contentId = id ? `${id}-content` : undefined;

  return (
    <div className="bg-atc-surface border border-atc-border rounded-lg p-2 mb-2">
      <button
        type="button"
        onClick={() => setExpanded((e) => !e)}
        aria-expanded={expanded}
        aria-controls={contentId}
        className="w-full flex items-center justify-between text-left gap-2"
      >
        <span className="font-bold text-atc-text font-mono tracking-wider text-xs">
          {title}
        </span>
        <span className="text-atc-text-muted text-xs truncate flex-1 min-w-0">
          {summary}
        </span>
        <span className="text-atc-text-muted text-xs shrink-0">
          {expanded ? "▲" : "▼"}
        </span>
      </button>
      {expanded && (
        <div id={contentId} className="mt-2 pt-2 border-t border-atc-border">
          {children}
        </div>
      )}
    </div>
  );
};

export default CollapsiblePanel;
