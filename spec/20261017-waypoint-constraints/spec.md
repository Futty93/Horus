# ウェイポイント高度・速度制限（Phase 1-2）

## メタデータ

- **Status**: Done
- **Date**: 2026-10-17
- **親**: [terminal-radar-gaps](../20261017-terminal-radar-gaps/spec.md)

## 概要

フライトプランウェイポイントの `AltitudeConstraint` を操縦目標と通過判定に反映する。

## 完了条件

- [x] `WaypointAltitudeTargetResolver` で AT / AT_OR_ABOVE / AT_OR_BELOW を目標高度に反映
- [x] 通過時に制約を満たさない場合はインデックスを進めない
