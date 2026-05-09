/** Elements present in `current` but not in `previous` (STCA pairId diff for polling alerts). */
export function findNewIdsInSet(
  previous: ReadonlySet<string>,
  current: ReadonlySet<string>
): string[] {
  const out: string[] = [];
  for (const id of current) {
    if (!previous.has(id)) {
      out.push(id);
    }
  }
  return out;
}
