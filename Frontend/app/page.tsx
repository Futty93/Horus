import React from "react";
import Image from "next/image";
import RadarCanvas from "@/components/radarCanvas";

export default function Home() {
  return (
    <div>
      <h1>Welcome to the Home Page</h1>
      <a href="/operator">Go to Operator Page</a><br/>
      <a href="/controller">Go to Controller Page</a>
    </div>
  );
}
