import Link from "next/link";

export function FlightPlanSetupNav() {
  return (
    <nav className="flex gap-4 pt-4 border-t border-atc-border">
      <Link href="/" className="text-sm text-atc-accent hover:underline">
        ← Back to Home
      </Link>
      <Link
        href="/controller"
        className="text-sm text-atc-accent hover:underline"
      >
        Go to Controller
      </Link>
      <Link
        href="/operator"
        className="text-sm text-atc-accent hover:underline"
      >
        Go to Operator
      </Link>
    </nav>
  );
}
