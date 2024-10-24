import { RoutePoint } from './RoutePoint';

export interface Route {
  name: string;
  description: string;
  points: RoutePoint[];
}