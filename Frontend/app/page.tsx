import React from "react";
import Image from "next/image";
import RadarCanvas from "@/component/radarCanvas";

export default function Home() {
  return (
    <div>
      <h1>Welcome to the Home Page</h1>
      <a href="/operator">Go to Operator Page</a>
    </div>
  );
}
