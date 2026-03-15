# Horus 実装計画

- **Date**: 2026-03-11
- **Status**: Planning

---

## 凡例

- **優先度**: 🔴 高 / 🟡 中 / 🟢 低
- **難易度**: ★☆☆ 低 / ★★☆ 中 / ★★★ 高

---

## フェーズ 1 — シナリオ作成・開始フロー（最重要基盤）

現状 Swagger 頼みのスポーン/シナリオ登録を GUI で完結させる。研究・教育利用の入口として最優先。

| # | タスク | 優先度 | 難易度 | Issue | 備考 |
|---|--------|--------|--------|-------|------|
| 1-1 | `POST /api/scenario/load` バックエンド実装 | 🔴 | ★★☆ | [#44](https://github.com/Futty93/Horus/issues/44) | 複数機を一括スポーン。spec 20260308-flight-plan-setup-page で未着手 |
| 1-2 | フライトプラン設定ページ — JSON エクスポート/インポート | 🔴 | ★☆☆ | [#45](https://github.com/Futty93/Horus/issues/45) | フロントのみ。ブラウザ download API |
| 1-3 | フライトプラン設定ページ — 「これで始める」ボタン | 🔴 | ★★☆ | [#46](https://github.com/Futty93/Horus/issues/46) | 1-1 完了後。シナリオ送信 → シミュレーション開始 → Controller 遷移 |
| 1-4 | フライトプラン設定ページ — 航空機テーブル編集（追加/削除/初期位置） | 🔴 | ★★☆ | [#47](https://github.com/Futty93/Horus/issues/47) | 現状 OdGroupList は表示のみ |
| 1-5 | シミュレーション開始画面での JSON アップロード起動 | 🟡 | ★☆☆ | [#48](https://github.com/Futty93/Horus/issues/48) | トップページまたは別エントリ |

---

## フェーズ 2 — レーダー表示の強化

管制業務の基本となる視覚情報を充実させる。実装コストが低いわりに訓練品質への貢献が大きい。

| # | タスク | 優先度 | 難易度 | Issue | 備考 |
|---|--------|--------|--------|-------|------|
| 2-1 | 履歴ドット（過去位置の軌跡）表示 | 🔴 | ★☆☆ | [#49](https://github.com/Futty93/Horus/issues/49) | Canvas に過去 N 点を保持して描画 |
| 2-2 | レンジリング（距離環）表示 | 🔴 | ★☆☆ | [#50](https://github.com/Futty93/Horus/issues/50) | 中心から同心円。表示 NM 数は設定可能に |
| 2-3 | データブロック（ラベル）に表示項目を追加 | 🟡 | ★☆☆ | [#51](https://github.com/Futty93/Horus/issues/51) | スクオーク・機種・ETA など選択式 |
| 2-4 | 速度ベクトル線の長さ（時間）調整 | 🟡 | ★☆☆ | [#52](https://github.com/Futty93/Horus/issues/52) | 設定パネルに追加 |
| 2-5 | セクター境界線の表示 | 🟡 | ★★☆ | [#53](https://github.com/Futty93/Horus/issues/53) | 担当空域を JSON で定義して描画 |
| 2-6 | 指示メモをレーダーラベル隣に表示 | 🟡 | ★★☆ | [#54](https://github.com/Futty93/Horus/issues/54) | spec 20260308-ui-ux-improvement の Out of Scope 項目 |

---

## フェーズ 3 — 管制指示の拡充

現状は高度/針路/速度のみ。実際の管制業務に近づけるための指示種別追加。

| # | タスク | 優先度 | 難易度 | Issue | 備考 |
|---|--------|--------|--------|-------|------|
| 3-1 | スクオーク（SSRコード）割り当て・表示 | 🔴 | ★☆☆ | [#55](https://github.com/Futty93/Horus/issues/55) | 値オブジェクト追加 + API 1本 |
| 3-2 | ホールディング指示（`WaypointAction.HOLD` 実装） | 🔴 | ★★★ | [#56](https://github.com/Futty93/Horus/issues/56) | 楕円軌道の飛行計算が必要 |
| 3-3 | 高度制限の管制官による設定・変更指示 | 🟡 | ★★☆ | [#57](https://github.com/Futty93/Horus/issues/57) | `AltitudeConstraint` は定義済み。UI と API 追加 |
| 3-4 | ハンドオフ機能（`WaypointAction.HANDOFF` 実装） | 🟡 | ★★★ | [#58](https://github.com/Futty93/Horus/issues/58) | 単一空域内での移管表示から着手 |
| 3-5 | Mach 数での速度指示 | 🟢 | ★☆☆ | [#59](https://github.com/Futty93/Horus/issues/59) | 変換式のみ。高高度訓練向け |

---

## フェーズ 4 — コンフリクト検出・安全機能の UI 強化

バックエンドの検出ロジックは実装済み。フロントエンドへの反映が不足。

| # | タスク | 優先度 | 難易度 | Issue | 備考 |
|---|--------|--------|--------|-------|------|
| 4-1 | STCA 警告のレーダー上での視覚的強調（点滅・色変更） | 🔴 | ★☆☆ | [#60](https://github.com/Futty93/Horus/issues/60) | `riskLevel` は既に取得済み。描画ロジックのみ |
| 4-2 | コンフリクトペアの間隔数値表示 | 🟡 | ★★☆ | [#61](https://github.com/Futty93/Horus/issues/61) | 水平 NM・垂直 ft をラベルに表示 |
| 4-3 | 管制間隔の自動チェック（5NM/1000ft）の明示的な違反通知 | 🟡 | ★★☆ | [#62](https://github.com/Futty93/Horus/issues/62) | `ConflictAlertService` の統計を UI に接続 |
| 4-4 | MSAW（最低安全高度警告）— 地形データなしの簡易版 | 🟢 | ★★☆ | [#63](https://github.com/Futty93/Horus/issues/63) | 固定の最低高度を下回ったら警告 |

---

## フェーズ 5 — 訓練・評価機能

研究・教育用途としての差別化機能。フェーズ 1〜4 完了後に着手推奨。

| # | タスク | 優先度 | 難易度 | Issue | 備考 |
|---|--------|--------|--------|-------|------|
| 5-1 | シミュレーション速度変更（早送り/スロー） | 🔴 | ★★☆ | [#64](https://github.com/Futty93/Horus/issues/64) | バックエンドの `REFRESH_RATE` を動的変更 |
| 5-2 | セッション記録（イベントログ） | 🟡 | ★★☆ | [#65](https://github.com/Futty93/Horus/issues/65) | 指示・コンフリクト発生をタイムスタンプ付きで記録 |
| 5-3 | 採点・評価レポート（間隔違反カウント等） | 🟡 | ★★★ | [#66](https://github.com/Futty93/Horus/issues/66) | 5-2 のログを集計して表示 |
| 5-4 | 難易度設定（トラフィック量・自動スポーン間隔） | 🟢 | ★★☆ | [#67](https://github.com/Futty93/Horus/issues/67) | 定期スポーンのスケジューラー追加 |
| 5-5 | 緊急事態シナリオ（エンジン故障等のフラグ） | 🟢 | ★★★ | [#68](https://github.com/Futty93/Horus/issues/68) | 航空機状態に緊急フラグ追加 + UI |

---

## フェーズ 6 — 気象・環境

飛行計算の現実性向上。研究用途での需要が高い場合に実施。

| # | タスク | 優先度 | 難易度 | Issue | 備考 |
|---|--------|--------|--------|-------|------|
| 6-1 | 風向・風速の設定と対地速度への反映 | 🟡 | ★★☆ | [#69](https://github.com/Futty93/Horus/issues/69) | `FixedWingFlightBehavior` の位置計算に風ベクトル追加 |
| 6-2 | ATIS 情報の表示（風・QNH・使用滑走路） | 🟡 | ★☆☆ | [#71](https://github.com/Futty93/Horus/issues/71) | 静的設定値をレーダー画面に表示するだけでも有用 |
| 6-3 | 気象レーダー表示（降水域） | 🟢 | ★★★ | [#70](https://github.com/Futty93/Horus/issues/70) | 外部データ連携が必要 |

---

## フェーズ 7 — マルチユーザー・リアルタイム通信

現状のポーリング方式を WebSocket に移行。大規模訓練シナリオで必要になる。

| # | タスク | 優先度 | 難易度 | Issue | 備考 |
|---|--------|--------|--------|-------|------|
| 7-1 | WebSocket によるリアルタイム位置配信 | 🟡 | ★★★ | [#73](https://github.com/Futty93/Horus/issues/73) | Spring WebSocket + Next.js クライアント |
| 7-2 | 複数セクターの同時管制（マルチプレイヤー） | 🟢 | ★★★ | [#72](https://github.com/Futty93/Horus/issues/72) | 7-1 完了後。セッション管理が必要 |

---

## 技術的負債（並行して対応）

| # | タスク | 優先度 | 難易度 | Issue | 備考 |
|---|--------|--------|--------|-------|------|
| T-1 | `ConflictAlertService` の DTO 化（backend-redesign 6.6 未着手） | 🟡 | ★☆☆ | [#74](https://github.com/Futty93/Horus/issues/74) | 小さいが残タスク |
| T-2 | API クラス名統一（`*Service` → `*Controller`）（backend-redesign 7.3 保留） | 🟢 | ★★☆ | [#75](https://github.com/Futty93/Horus/issues/75) | 影響範囲大のため慎重に |
| T-3 | テスト網羅（単体・統合）— flight-plan spec 6.1〜6.3 未着手 | 🟡 | ★★☆ | [#76](https://github.com/Futty93/Horus/issues/76) | サンプルシナリオ JSON 作成も含む |

---

## 推奨着手順序

```
フェーズ1（シナリオ作成）
  → フェーズ2（レーダー表示）＋フェーズ4（STCA UI）  ← 並行可
    → フェーズ3（管制指示拡充）
      → フェーズ5（訓練・評価）
        → フェーズ6（気象）＋フェーズ7（WebSocket）  ← 並行可
```
