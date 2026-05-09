import { findNewIdsInSet } from "./violationDiff";

describe("findNewIdsInSet", () => {
  it("returns ids only in current", () => {
    const prev = new Set(["a", "b"]);
    const cur = new Set(["b", "c", "d"]);
    expect(findNewIdsInSet(prev, cur).sort()).toEqual(["c", "d"]);
  });

  it("returns empty when nothing new", () => {
    expect(findNewIdsInSet(new Set(["x"]), new Set(["x"]))).toEqual([]);
  });
});
