import { appendFix, moveItem, removeAt } from "./routeEdits";

describe("routeEdits", () => {
  it("moves an item earlier and later", () => {
    expect(moveItem(["A", "B", "C"], 2, 0)).toEqual(["C", "A", "B"]);
    expect(moveItem(["A", "B", "C"], 0, 1)).toEqual(["B", "A", "C"]);
  });

  it("leaves the list unchanged for an invalid move", () => {
    expect(moveItem(["A"], 0, 0)).toEqual(["A"]);
    expect(moveItem(["A"], -1, 0)).toEqual(["A"]);
  });

  it("removes by index", () => {
    expect(removeAt(["A", "B"], 0)).toEqual(["B"]);
  });

  it("rejects a blank or duplicate fix", () => {
    expect(appendFix(["KOITO"], "  ", new Set())).toEqual({
      ok: false,
      reason: "empty",
    });
    expect(appendFix(["KOITO"], "koito", new Set())).toEqual({
      ok: false,
      reason: "duplicate",
    });
    expect(appendFix([], "RJTT", new Set(["RJTT"]))).toEqual({
      ok: false,
      reason: "duplicate",
    });
  });

  it("appends a normalized fix name", () => {
    expect(appendFix(["KOITO"], " bokjo ", new Set(["RJTT"]))).toEqual({
      ok: true,
      waypoints: ["KOITO", "BOKJO"],
    });
  });
});
