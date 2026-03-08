import React from "react";
import Link from "next/link";

export default function Home() {
  return (
    <div className="min-h-screen bg-atc-bg text-atc-text flex flex-col items-center justify-center p-8">
      <div className="max-w-2xl w-full space-y-10">
        <header className="text-center space-y-4">
          <h1 className="font-mono text-4xl sm:text-5xl font-bold tracking-[0.25em] text-atc-text">
            HORUS
          </h1>
          <p className="text-atc-text-muted text-sm sm:text-base leading-relaxed max-w-md mx-auto">
            ATC radar simulation for training. Controller speaks instructions;
            Operator reads back and sends them to move aircraft.
          </p>
        </header>

        <p className="text-center text-xs text-atc-text-muted font-mono tracking-wide">
          Flight plan creation and aircraft spawn: use Swagger (
          <code className="text-atc-text-muted opacity-80">/docs.html</code>).
        </p>

        <nav className="grid gap-4 sm:grid-cols-2">
          <Link
            href="/controller"
            className="group block rounded-lg p-6 bg-atc-surface border border-atc-border
                       transition-colors duration-200 animate-fade-in
                       hover:border-atc-accent focus:outline-none focus:ring-2 focus:ring-atc-accent focus:ring-offset-2 focus:ring-offset-atc-bg"
            style={{ animationDelay: "100ms" }}
          >
            <h2 className="font-mono text-lg font-bold tracking-wider text-atc-text mb-2">
              CONTROLLER (管制官役)
            </h2>
            <p className="text-sm text-atc-text-muted leading-relaxed">
              View radar and give verbal instructions. Record your issued
              instructions as memos. No direct aircraft control.
            </p>
            <span className="inline-block mt-3 text-xs font-mono text-atc-accent group-hover:text-atc-accent-hover transition-colors">
              Go to Controller →
            </span>
          </Link>

          <Link
            href="/operator"
            className="group block rounded-lg p-6 bg-atc-surface border border-atc-border
                       transition-colors duration-200 animate-fade-in
                       hover:border-atc-accent focus:outline-none focus:ring-2 focus:ring-atc-accent focus:ring-offset-2 focus:ring-offset-atc-bg"
            style={{ animationDelay: "200ms" }}
          >
            <h2 className="font-mono text-lg font-bold tracking-wider text-atc-text mb-2">
              OPERATOR (パイロット役)
            </h2>
            <p className="text-sm text-atc-text-muted leading-relaxed">
              Listen, read back, and input instructions. Send altitude, speed,
              heading, Direct To, and Resume to control aircraft.
            </p>
            <span className="inline-block mt-3 text-xs font-mono text-atc-accent group-hover:text-atc-accent-hover transition-colors">
              Go to Operator →
            </span>
          </Link>
        </nav>
      </div>
    </div>
  );
}
