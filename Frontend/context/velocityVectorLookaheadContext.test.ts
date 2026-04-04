import {
  clampVelocityVectorLookaheadMinutes,
  VELOCITY_VECTOR_LOOKAHEAD_DEFAULT_MINUTES,
  VELOCITY_VECTOR_LOOKAHEAD_MAX_MINUTES,
  VELOCITY_VECTOR_LOOKAHEAD_MIN_MINUTES,
  VELOCITY_VECTOR_LOOKAHEAD_STEP_MINUTES,
} from "./velocityVectorLookaheadContext";

describe("clampVelocityVectorLookaheadMinutes", () => {
  it("clamps to min/max and snaps to step", () => {
    expect(clampVelocityVectorLookaheadMinutes(0)).toBe(
      VELOCITY_VECTOR_LOOKAHEAD_MIN_MINUTES
    );
    expect(clampVelocityVectorLookaheadMinutes(99)).toBe(
      VELOCITY_VECTOR_LOOKAHEAD_MAX_MINUTES
    );
    expect(clampVelocityVectorLookaheadMinutes(1.24)).toBe(1);
    expect(clampVelocityVectorLookaheadMinutes(1.26)).toBe(1.5);
  });

  it("leaves allowed values unchanged", () => {
    expect(clampVelocityVectorLookaheadMinutes(1)).toBe(
      VELOCITY_VECTOR_LOOKAHEAD_DEFAULT_MINUTES
    );
    expect(clampVelocityVectorLookaheadMinutes(10)).toBe(
      VELOCITY_VECTOR_LOOKAHEAD_MAX_MINUTES
    );
    expect(clampVelocityVectorLookaheadMinutes(0.5)).toBe(
      VELOCITY_VECTOR_LOOKAHEAD_MIN_MINUTES
    );
    expect(clampVelocityVectorLookaheadMinutes(7.5)).toBe(7.5);
  });

  it("preserves each allowed 0.5 minute step", () => {
    for (let i = 0; i < 20; i += 1) {
      const v =
        VELOCITY_VECTOR_LOOKAHEAD_MIN_MINUTES +
        i * VELOCITY_VECTOR_LOOKAHEAD_STEP_MINUTES;
      expect(clampVelocityVectorLookaheadMinutes(v)).toBeCloseTo(v, 6);
    }
  });
});
