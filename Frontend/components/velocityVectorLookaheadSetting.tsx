"use client";

import React from "react";
import {
  useVelocityVectorLookahead,
  VELOCITY_VECTOR_LOOKAHEAD_MAX_MINUTES,
  VELOCITY_VECTOR_LOOKAHEAD_MIN_MINUTES,
  VELOCITY_VECTOR_LOOKAHEAD_STEP_MINUTES,
} from "@/context/velocityVectorLookaheadContext";

const VelocityVectorLookaheadSetting = ({
  embedded = false,
}: {
  embedded?: boolean;
}) => {
  const { durationMinutes, setDurationMinutes } = useVelocityVectorLookahead();

  const handleChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    setDurationMinutes(Number(event.target.value));
  };

  const content = (
    <div className="space-y-2">
      <div className="flex items-center justify-between gap-2">
        <label
          htmlFor="velocityVectorLookahead"
          className="font-bold text-atc-text font-mono tracking-wider text-xs shrink-0"
        >
          速度ベクトル予測時間:
        </label>
        <span className="text-atc-text font-mono text-xs tabular-nums">
          {durationMinutes} 分
        </span>
      </div>
      <input
        id="velocityVectorLookahead"
        type="range"
        min={VELOCITY_VECTOR_LOOKAHEAD_MIN_MINUTES}
        max={VELOCITY_VECTOR_LOOKAHEAD_MAX_MINUTES}
        step={VELOCITY_VECTOR_LOOKAHEAD_STEP_MINUTES}
        value={durationMinutes}
        onChange={handleChange}
        className="w-full h-1.5 bg-atc-surface-elevated rounded-full appearance-none cursor-pointer
                   accent-atc-accent
                   [&::-webkit-slider-thumb]:appearance-none [&::-webkit-slider-thumb]:w-3 [&::-webkit-slider-thumb]:h-3
                   [&::-webkit-slider-thumb]:rounded-full [&::-webkit-slider-thumb]:bg-atc-accent"
      />
      <div className="flex justify-between text-[10px] font-mono text-atc-text-muted">
        <span>{VELOCITY_VECTOR_LOOKAHEAD_MIN_MINUTES}分</span>
        <span>{VELOCITY_VECTOR_LOOKAHEAD_MAX_MINUTES}分</span>
      </div>
    </div>
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

export default VelocityVectorLookaheadSetting;
