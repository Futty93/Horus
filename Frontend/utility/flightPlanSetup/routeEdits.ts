export function moveItem<T>(
  items: readonly T[],
  from: number,
  to: number
): T[] {
  if (
    from === to ||
    from < 0 ||
    to < 0 ||
    from >= items.length ||
    to >= items.length
  ) {
    return [...items];
  }
  const next = [...items];
  const [item] = next.splice(from, 1);
  next.splice(to, 0, item);
  return next;
}

export function removeAt<T>(items: readonly T[], index: number): T[] {
  if (index < 0 || index >= items.length) return [...items];
  return items.filter((_, i) => i !== index);
}

export function appendFix(
  waypoints: readonly string[],
  fixName: string,
  blocked: ReadonlySet<string>
):
  | { ok: true; waypoints: string[] }
  | { ok: false; reason: "empty" | "duplicate" } {
  const name = fixName.trim().toUpperCase();
  if (!name) return { ok: false, reason: "empty" };
  if (blocked.has(name) || waypoints.some((w) => w.toUpperCase() === name)) {
    return { ok: false, reason: "duplicate" };
  }
  return { ok: true, waypoints: [...waypoints, name] };
}
