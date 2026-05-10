import type { InitialPositionDto } from "@/types/scenario";

export type InitialPositionFormStrings = {
  lat: string;
  lon: string;
  altitude: string;
  heading: string;
  groundSpeed: string;
  verticalSpeed: string;
};

export function positionToFormStrings(
  p: InitialPositionDto
): InitialPositionFormStrings {
  return {
    lat: p.latitude.toString(),
    lon: p.longitude.toString(),
    altitude: p.altitude.toString(),
    heading: p.heading.toString(),
    groundSpeed: p.groundSpeed.toString(),
    verticalSpeed: p.verticalSpeed.toString(),
  };
}
