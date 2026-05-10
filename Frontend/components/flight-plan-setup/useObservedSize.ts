import { useEffect, useState, type RefObject } from "react";

const DEFAULT_W = 640;
const DEFAULT_H = 480;

/**
 * Subscribes to the element size via ResizeObserver (browser / DOM external sync).
 * See https://ja.react.dev/learn/you-might-not-need-an-effect — this is a valid sync case.
 */
export function useObservedSize(containerRef: RefObject<HTMLElement | null>): {
  w: number;
  h: number;
} {
  const [disp, setDisp] = useState({ w: DEFAULT_W, h: DEFAULT_H });

  useEffect(() => {
    const el = containerRef.current;
    if (!el || typeof ResizeObserver === "undefined") return;
    const ro = new ResizeObserver((entries) => {
      const cr = entries[0]?.contentRect;
      if (!cr) return;
      const nw = Math.floor(cr.width);
      const nh = Math.floor(cr.height);
      if (nw >= 1 && nh >= 1) {
        setDisp((prev) =>
          prev.w !== nw || prev.h !== nh ? { w: nw, h: nh } : prev
        );
      }
    });
    ro.observe(el);
    return () => ro.disconnect();
    // eslint-disable-next-line react-hooks/exhaustive-deps -- containerRef identity is stable; subscribe once on mount
  }, []);

  return disp;
}
