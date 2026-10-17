# 指示対気速度（Phase 1-1）

## メタデータ

- **Status**: Done
- **Date**: 2026-10-17
- **親**: [terminal-radar-gaps](../20261017-terminal-radar-gaps/spec.md)

## 概要

管制指示の速度を地速（GS）だけでなく指示対気速度（IAS）として出せるようにする。風モデルが無い間は標準大気・無風で IAS→TAS→GS（=TAS）換算する。

## 完了条件

- [x] `ControlAircraftDto.instructedIndicatedAirspeed` が非 null のとき IAS モード
- [x] シミュレーション tick ごとに高度に応じて目標 GS を再計算（`IndicatedAirspeedConverter`）
- [x] Frontend で IAS/GS 切替（Controller `controlAircraft.tsx`）
- [x] 位置 API の `instructedVector.indicatedAirspeed` で IAS 目標を返す
