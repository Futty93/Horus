# ターミナル 3海里・後方乱気流間隔（Phase 1-3）

## メタデータ

- **Status**: Done
- **Date**: 2026-10-17
- **Parent**: [terminal-radar-gaps](../20261017-terminal-radar-gaps/spec.md)

## 概要

レーダーサイト（暫定 RJTT）から 40NM 以内では水平 3NM、それ以外は 5NM。同一航路の後続機には訓練用 wake 海里表を適用する。

## 完了条件

- [x] `HorizontalSeparationRequirements` がペアごとの最小水平 NM を返す
- [x] `ConflictDetector` / `RiskAssessment.isSeparationViolation()` がペア基準を使用
