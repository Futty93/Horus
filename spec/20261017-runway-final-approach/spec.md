# 滑走路と最終進入（Phase 1-4）

## メタデータ

- **Status**: Done
- **Date**: 2026-10-17
- **親**: [terminal-radar-gaps](../20261017-terminal-radar-gaps/spec.md)

## 概要

`runways.json` の滑走路をレーダーに描画し、最終進入延長線上の intercept 点へ Direct To した後、進入コース針路へ合流する（進入許可・復行は別スライス）。

## 完了条件

- [x] `GET /ats/runways` と Frontend 描画（実線 RWY・点線延長）
- [x] `POST /api/aircraft/{callsign}/final-approach` で intercept→course
