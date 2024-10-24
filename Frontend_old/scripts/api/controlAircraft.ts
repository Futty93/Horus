export async function controlAircraft(callsign: string): Promise<void> {
  const inputAltitude: HTMLInputElement = document.getElementById('altitude') as HTMLInputElement;
  const inputHeading: HTMLInputElement = document.getElementById('heading') as HTMLInputElement;
  const inputGroundSpeed: HTMLInputElement = document.getElementById('groundSpeed') as HTMLInputElement;

  // Get the input values from the form
  const instructedAltitude = Number(inputAltitude.value);
  const instructedGroundSpeed = Number(inputGroundSpeed.value);
  const instructedHeading = Number(inputHeading.value);

  // Create the DTO object
  const controlAircraftDto = {
    instructedAltitude,
    instructedGroundSpeed,
    instructedHeading,
  };

  try {
    const response = await fetch(
      `http://localhost:8080/api/aircraft/control/${callsign}`,
      {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(controlAircraftDto),
      },
    );

    if (response.ok) {
      console.log(`Aircraft ${callsign} controlled successfully.`);
    } else {
      console.error(
        `Failed to control aircraft ${callsign}. Status:`,
        response.status,
      );
    }
  } catch (error) {
    console.error("Error occurred while controlling aircraft:", error);
  }
}