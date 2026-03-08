import { getBackendBaseUrl, proxyToBackend } from "@/utility/api/backendProxy";

const originalFetch = global.fetch;
const originalEnv = process.env;

beforeEach(() => {
  process.env = { ...originalEnv };
});

afterEach(() => {
  global.fetch = originalFetch;
  process.env = originalEnv;
});

describe("getBackendBaseUrl", () => {
  it("returns URL from BACKEND_SERVER_IP and BACKEND_SERVER_PORT", () => {
    process.env.BACKEND_SERVER_IP = "192.168.1.100";
    process.env.BACKEND_SERVER_PORT = "9000";

    expect(getBackendBaseUrl()).toBe("http://192.168.1.100:9000");
  });

  it("returns localhost:8080 when env vars are unset", () => {
    delete process.env.BACKEND_SERVER_IP;
    delete process.env.BACKEND_SERVER_PORT;

    expect(getBackendBaseUrl()).toBe("http://localhost:8080");
  });
});

describe("proxyToBackend", () => {
  it("calls fetch with correct URL and forwards init", async () => {
    const mockFetch = jest.fn().mockResolvedValue({ ok: true });
    global.fetch = mockFetch as typeof fetch;
    process.env.BACKEND_SERVER_IP = "backend";
    process.env.BACKEND_SERVER_PORT = "8080";

    await proxyToBackend("/aircraft/location/all", {
      method: "GET",
      headers: { Accept: "application/json" },
    });

    expect(mockFetch).toHaveBeenCalledWith(
      "http://backend:8080/aircraft/location/all",
      expect.objectContaining({
        method: "GET",
        headers: { Accept: "application/json" },
      })
    );
  });
});
