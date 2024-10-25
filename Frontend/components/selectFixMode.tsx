import { useSelectFixMode } from "@/context/selectFixModeContext";
import React from "react";

const SelectFixMode = () => {
  const { isSelectFixMode, setIsSelectFixMode } = useSelectFixMode();

  return (
    <div className="mt-5">
      <div className="text-center mb-3">
        <p id="selectedFixName" className="text-xl font-bold text-green-400">No fixes selected</p>
      </div>
      {!isSelectFixMode.selectFixMode ? (
        // isSelectFixMode が false の場合
        <button
          className="w-full bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded"
          onClick={() => {
            console.log("Direct to Fix 押されたよ");
            const callsignElement = document.getElementById("callsign") as HTMLParagraphElement;
            const selectedFixNameElement = document.getElementById("selectedFixName") as HTMLParagraphElement;
            if (callsignElement.innerText.length <= 1) {
              console.error("Callsign is empty");
              selectedFixNameElement.innerText = "Select aircraft first";
              return;
            }
            setIsSelectFixMode({ selectFixMode: true });
          }}
        >
          Direct to Fix
        </button>
      ) : (
        // isSelectFixMode が true の場合
          <div className="flex flex-col space-y-2">
            <button
              className="bg-green-500 hover:bg-green-700 text-white font-bold py-2 px-4 rounded"
              onClick={async () => {
                console.log("Confirm 押されたよ");
                const callsignElement = document.getElementById("callsign") as HTMLParagraphElement;
                const selectedFixNameElement = document.getElementById("selectedFixName") as HTMLParagraphElement;

                if (callsignElement.innerText.length <= 1) {
                  console.error("Callsign is empty");
                } else if (callsignElement) {
                  const callsign = callsignElement.innerText;
                  console.log("Callsign:", callsign);
                } else {
                  console.error("Callsign element not found");
                }

                const callsign = callsignElement.innerText;
                const selectedFixName = selectedFixNameElement.innerText;
                try {
                  const response = await fetch(
                    `http://localhost:8080/api/aircraft/control/${callsign}/direct/${selectedFixName}`,
                    {
                      method: "POST",
                      headers: {
                        "Content-Type": "application/json",
                      },
                    }
                  );
            
                  if (response.ok) {
                    console.log(`Aircraft ${callsign} controlled successfully.`);
                  } else {
                    console.error(`Failed to control aircraft ${callsign}. Status:`, response.status);
                  }
                } catch (error) {
                  console.error("Error occurred while controlling aircraft:", error);
                }

                selectedFixNameElement.innerText = "No fixes selected";
                setIsSelectFixMode({ selectFixMode: false });
              }}
            >
              Confirm
            </button>
            <button
              className="bg-red-500 hover:bg-red-700 text-white font-bold py-2 px-4 rounded"
              onClick={() => {
                console.log("Cancel 押されたよ");
                const selectedFixNameElement = document.getElementById("selectedFixName") as HTMLParagraphElement;

                selectedFixNameElement.innerText = "No fixes selected";
                setIsSelectFixMode({ selectFixMode: false });
              }}
            >
              Cancel
            </button>
          </div>
      )}
    </div>
  );
};

export default SelectFixMode;