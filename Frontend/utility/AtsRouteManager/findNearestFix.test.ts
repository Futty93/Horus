import { findNearestFixName } from "./findNearestFix";
import type { Waypoint } from "./RouteInterfaces/Waypoint";
import type { RadioNavigationAid } from "./RouteInterfaces/RadioNavigationAid";

describe("findNearestFixName", () => {
  const waypoints: Waypoint[] = [
    { name: "ABENO", latitude: 35.5, longitude: 139.5, type: "waypoint" },
    { name: "OMOTE", latitude: 36.0, longitude: 140.0, type: "waypoint" },
    { name: "FAR", latitude: 40.0, longitude: 145.0, type: "waypoint" },
  ];

  const radioNavAids: RadioNavigationAid[] = [
    {
      name: "VOR1",
      id: "vor1",
      type: "radioNavigationAid",
      latitude: 35.5,
      longitude: 139.5,
      frequency: "114.0",
    },
  ];

  it.each([
    [
      "click near ABENO",
      { latitude: 35.51, longitude: 139.51 },
      waypoints,
      [],
      "ABENO",
    ],
    ["empty arrays", { latitude: 35.5, longitude: 139.5 }, [], [], ""],
    [
      "prefers radioNavAid when distances equal",
      { latitude: 35.5, longitude: 139.5 },
      waypoints,
      radioNavAids,
      "VOR1",
    ],
    [
      "returns nearest when multiple waypoints",
      { latitude: 35.8, longitude: 139.8 },
      waypoints,
      [],
      "OMOTE",
    ],
  ] as const)(
    "returns expected name when %s",
    (_, geo, wps, aids, expected) => {
      expect(findNearestFixName(geo, wps, aids)).toBe(expected);
    }
  );
});
