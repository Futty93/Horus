"use client";
import { useCenterCoordinate } from "@/context/centerCoordinateContext";
import { useState } from "react";

// Define the sector center coordinates in a separate constant
const sectorCenterCoordinates: { [key: string]: { latitude: number; longitude: number } } = {
  "T09": { latitude: 34.482 , longitude: 138.614 },
  "T10": { latitude: 33.041 , longitude: 139.456 },
  "T14": { latitude: 33.667 , longitude: 137.918 },
  "T25": { latitude: 34.549 , longitude: 136.966 },
  "T30": { latitude: 43.29444444444445 , longitude: 142.67916666666667 },
  "T31": { latitude: 41.85805555555555 , longitude: 140.1590277777778 },
  "T32": { latitude: 40.209722222222226 , longitude: 141.23722222222221 },
  "T33": { latitude: 38.474999999999994 , longitude: 138.8048611111111 },
  "T34": { latitude: 37.543055555555554 , longitude: 141.55124999999998 },
  "T35": { latitude: 36.84736111111111 , longitude: 139.40930555555553 },
  "T36": { latitude: 35.77388888888889 , longitude: 142.14041666666665 },
  "T38": { latitude: 35.827083333333334 , longitude: 139.15763888888887 },
  "T39": { latitude: 35.273472222222225 , longitude: 139.24930555555557 },
  "T45": { latitude: 34.30236111111111 , longitude: 135.53097222222223 },
  "T46": { latitude: 33.43291666666667 , longitude: 135.91680555555556 },
  "T92": { latitude: 38.30972222222222 , longitude: 141.12583333333333 },
  "T93": { latitude: 36.28833333333333 , longitude: 142.9763888888889 },
  "F01": { latitude: 42.377361111111114 , longitude: 141.8722222222222 },
  "F04": { latitude: 39.608194444444436 , longitude: 136.90722222222223 },
  "F05": { latitude: 35.63972222222222 , longitude: 138.83125 },
  "F07": { latitude: 36.840694444444445 , longitude: 135.81527777777777 },
  "F08": { latitude: 35.93208333333333 , longitude: 132.54180555555553 },
  "F09": { latitude: 33.681805555555556 , longitude: 134.32638888888889 },
  "F10": { latitude: 32.59625 , longitude: 134.68847222222223 },
  "F11": { latitude: 31.687083333333334 , longitude: 135.36013888888888 },
  "F12": { latitude: 33.909 , longitude: 131.057 },
  "F13": { latitude: 31.779722222222222 , longitude: 127.51916666666668 },
  "F14": { latitude: 31.530277777777776 , longitude: 130.745 },
  "F15": { latitude: 28.71666666666667 , longitude: 126.49805555555555 },
  "F16": { latitude: 28.601805555555558 , longitude: 129.5948611111111 },
  "F17": { latitude: 24.36763888888889 , longitude: 126.57083333333333 },
  "N43": { latitude: 36.96819444444444 , longitude: 137.32430555555555 },
  "N44": { latitude: 35.09638888888889 , longitude: 136.40041666666667 },
  "N47": { latitude: 35.17722222222223 , longitude: 135.38680555555555 },
  "N48": { latitude: 36.169444444444444 , longitude: 134.56944444444446 },
  "N49": { latitude: 34.59513888888889 , longitude: 134.17916666666667 },
  "N50": { latitude: 33.25541666666667 , longitude: 133.79736111111112 },
  "N51": { latitude: 34.531111111111116 , longitude: 131.56319444444443 },
  "N52": { latitude: 33.38305555555556 , longitude: 132.2573611111111 },
  "N53": { latitude: 33.26736111111111 , longitude: 129.81375000000003 },
  "N54": { latitude: 31.617222222222225 , longitude: 131.49763888888887 },
  "N55": { latitude: 28.23111111111111 , longitude: 128.70791666666668 },
  "A01": { latitude: 44.36263888888889 , longitude: 151.79333333333335 },
  "A02": { latitude: 42.24486111111111 , longitude: 154.02319444444444 },
  "A03": { latitude: 35.25888888888889 , longitude: 153.5966666666667 },
  "A04": { latitude: 27.654166666666665 , longitude: 143.64499999999998 },
  "A05": { latitude: 24.915694444444444 , longitude: 132.88125 }
};

interface SectorSelectorProps {
  onSectorChange: (coordinates: { latitude: number; longitude: number }) => void;
}

const SectorSelector = ({ onSectorChange }: SectorSelectorProps) => {
  const [selectedSector, setSelectedSector] = useState("T09"); // Default sector
  const { centerCoordinate, setCenterCoordinate } = useCenterCoordinate();

  // Update the selected sector and call the callback
  const handleSectorChange = (event: React.ChangeEvent<HTMLSelectElement>) => {
    const sector = event.target.value;
    setSelectedSector(sector);
    onSectorChange(sectorCenterCoordinates[sector]);
    setCenterCoordinate((prev) => ({
      ...prev,
      centerCoordinate: {
        latitude: sectorCenterCoordinates[sector].latitude,
        longitude: sectorCenterCoordinates[sector].longitude
      },
    }));
  };

  return (
    <div className="flex justify-between mb-5">
      <label htmlFor="selectSector" className="font-bold text-green-400 mr-2">担当セクター:</label>
      <select
        id="selectSector"
        value={selectedSector}
        onChange={handleSectorChange}
        className="p-2 rounded border border-green-400 bg-gray-900 text-white"
      >
        {Object.keys(sectorCenterCoordinates).map(sector => (
          <option key={sector} value={sector}>{sector}</option>
        ))}
      </select>
    </div>
  );
};

export default SectorSelector;