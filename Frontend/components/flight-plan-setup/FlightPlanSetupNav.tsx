import Link from "next/link";

export function FlightPlanSetupNav() {
  return (
    <nav className="flex gap-4 text-sm text-[#8a7d6b]">
      <Link href="/" className="hover:text-[#1c1915]">
        Home
      </Link>
      <Link href="/controller" className="hover:text-[#1c1915]">
        Controller
      </Link>
      <Link href="/operator" className="hover:text-[#1c1915]">
        Operator
      </Link>
    </nav>
  );
}
