import { Metadata } from "next";

export const metadata: Metadata = {
  title: "Flight Plan Setup",
};

export default function FlightPlanSetupLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return <>{children}</>;
}
