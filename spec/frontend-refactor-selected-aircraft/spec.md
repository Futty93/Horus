# フロントエンド 選択航空機状態リファクタリング 実装計画

## 概要

選択航空機（callsign）の状態を DOM ポーリング・`getElementById` から React Context に移行し、堅牢な状態管理に統一する。DEFERRED_ITEMS #6 に対応。

## 背景・課題

### 現状の流れ

1. **状態の書き込み元**: `RadarCanvas` で航空機クリック時、`changeDisplayAircraftInfo(aircraft)` が `document.getElementById("callsign")` 等に直接 DOM 書き込み
2. **状態の読み取り**: 複数コンポーネントが `getElementById("callsign")` で callsign を取得
   - **ポーリング**: `flightPlanControl.tsx`, `flightPlanDisplay.tsx` … 500ms 間隔で `getElementById` を実行
   - **オンデマンド**: `controlAircraft.tsx`, `selectFixMode.tsx`, `inputInfoArea.tsx` … ボタンクリック時に `getElementById` で取得

### 課題

| 課題 | 影響 |
|------|------|
| DOM 依存が fragile | `id="callsign"` の変更や要素削除で全コンポーネントが壊れる |
| ポーリングのオーバーヘッド | 500ms 間隔で不要な DOM アクセス・再レンダリング |
| React の状態管理から外れている | デバッグ・テストが困難 |
| コンポーネント間の暗黙の契約 | DOM 構造が「API」となり、型安全でない |

### 関連: DEFERRED_ITEMS #8（バックエンド）

スレッドセーフティはバックエンドの関心事。本リファクタリングとは独立だが、フロントから `direct-to` / `resume-navigation` を呼ぶ際のタイミングは現状のまま。バックエンド側で lock 設計を見直す必要がある場合は別計画で対応。

---

## 方針

**SelectedAircraftContext** を導入し、選択航空機の callsign（および将来的に Aircraft 全体）を React 状態として共有する。

- **Phase 1**: callsign のみ Context 化（最小変更で #6 解消）
- **Phase 2（将来）**: altitude / speed / heading の入力値も Context 経由にし、DOM 直接書き込みを廃止

---

## 影響範囲

### 書き込み元

| ファイル | 現状 | 変更後 |
|----------|------|--------|
| `radarCanvas.tsx` | `changeDisplayAircraftInfo` で `fontElement.textContent = aircraft.callsign` | `setSelectedCallsign(aircraft.callsign)` を呼ぶ。既存の altitude/speed/heading の DOM 更新は Phase 1 では維持 |

### 読み取り元（callsign のみ）

| ファイル | 現状 | 変更後 |
|----------|------|--------|
| `flightPlanControl.tsx` | 500ms ポーリングで `getElementById` | `useSelectedAircraft().callsign` |
| `flightPlanDisplay.tsx` | 500ms ポーリングで `getElementById` | `useSelectedAircraft().callsign` |
| `selectFixMode.tsx` | ボタンクリック時に `getElementById` | `useSelectedAircraft().callsign` |
| `controlAircraft.tsx` | ボタンクリック時に `getElementById` | `useSelectedAircraft().callsign` |
| `inputInfoArea.tsx` | ボタンクリック時に `getElementById` | `useSelectedAircraft().callsign` |

### その他 DOM 依存（Phase 1 では変更しない）

| ファイル | 内容 |
|----------|------|
| `radarCanvas.tsx` | `altitude`, `speed`, `heading` 入力への直接代入 |
| `controlAircraft.tsx` | `altitude`, `speed`, `heading` の読み取り |
| `inputInfoArea.tsx` | 上記同様 |
| `radarCanvas.tsx` | `selectedFixName` への DOM 書き込み（SelectFixMode と共有） |
| `utility/api/simulation.ts` | `startButton`, `pauseButton` の getElementById |

---

## 実装計画

### Phase 1: SelectedAircraftContext の導入（callsign のみ）

#### 1.1 Context 作成

**新規**: `Frontend/context/selectedAircraftContext.tsx`

```typescript
// 型定義
interface SelectedAircraftContextType {
  callsign: string | null;
  setCallsign: (callsign: string | null) => void;
}

// Provider: useState で callsign を保持
// useSelectedAircraft: フックで context を取得
```

#### 1.2 Provider の配置

**変更**: `app/operator/page.tsx`, `app/controller/page.tsx`

- `SelectFixModeProvider` の内側に `SelectedAircraftProvider` を追加
- `RadarCanvas` と制御パネルを共にラップするよう配置

#### 1.3 RadarCanvas の変更

**変更**: `components/radarCanvas.tsx`

- `useSelectedAircraft()` で `setCallsign` を取得
- `changeDisplayAircraftInfo` 内で `setCallsign(aircraft.callsign)` を呼ぶ
- `fontElement.textContent = aircraft.callsign` の行を削除（表示は Context 購読側で行う）

**注意**: `<p id="callsign">` は表示用として残すが、`RadarCanvas` からの書き込みはやめる。表示コンポーネント側で Context を購読して表示する。または、callsign 表示部分を新コンポーネント化して Context から取得させる。

#### 1.4 表示の責務分離

現状、`<p id="callsign">` は各ページの JSX に直書きされている。以下のいずれかで整理する。

**案 A**: 既存 `<p id="callsign">` を `SelectedCallsignDisplay` コンポーネントに切り出し、Context を購読して表示する。

**案 B**: `callsignDisplay`  div 全体を `SelectedCallsignDisplay` にし、`callsign` を Context から取得して表示。`id="callsign"` は不要になる。

→ **案 B** を採用。DOM に `id="callsign"` を残さず、表示はすべて Context 経由にする。

#### 1.5 各コンポーネントの変更

| コンポーネント | 変更内容 |
|----------------|----------|
| `flightPlanControl.tsx` | `useEffect` のポーリングを削除。`useSelectedAircraft().callsign` を使用 |
| `flightPlanDisplay.tsx` | 同上。ポーリング削除、Context から `callsign` 取得 |
| `selectFixMode.tsx` | `getElementById("callsign")` を `useSelectedAircraft().callsign` に置換 |
| `controlAircraft.tsx` | 同上 |
| `inputInfoArea.tsx` | 同上 |

#### 1.6 `radarCanvas.tsx` の `changeDisplayAircraftInfo`

- `fontElement.textContent = aircraft.callsign` を削除
- `setCallsign(aircraft.callsign)` を追加
- `inputAltitude`, `inputSpeed`, `inputHeading` への代入は Phase 1 では維持（他コンポーネントがまだ DOM を参照しているため）

---

### Phase 2: 管制指示入力の Context 化 ✅ 実装済み

- `instructedVector`（altitude, speed, heading）も Context で保持
- `ControlAircraft` / `InputAircraftInfo` を制御コンポーネント化（value/onChange で Context と双方向バインディング）
- `changeDisplayAircraftInfo` の DOM 直接操作を廃止
- `selectedFixName` の DOM 依存を SelectFixModeContext で整理
- `radarCanvas` の confirmButton イベントリスナーを削除（InputAircraftInfo が API を直接呼び出すように変更）

---

## ファイル変更一覧（Phase 1）

| # | ファイル | 種別 |
|---|----------|------|
| 1 | `Frontend/context/selectedAircraftContext.tsx` | 新規 |
| 2 | `Frontend/app/operator/page.tsx` | Provider 追加、callsign 表示をコンポーネント化 |
| 3 | `Frontend/app/controller/page.tsx` | 同上 |
| 4 | `Frontend/components/radarCanvas.tsx` | setCallsign 呼び出し、fontElement 書き込み削除 |
| 5 | `Frontend/components/flightPlanControl.tsx` | ポーリング削除、Context 使用 |
| 6 | `Frontend/components/flightPlanDisplay.tsx` | ポーリング削除、Context 使用 |
| 7 | `Frontend/components/selectFixMode.tsx` | getElementById 削除、Context 使用 |
| 8 | `Frontend/components/controlAircraft.tsx` | getElementById 削除、Context 使用 |
| 9 | `Frontend/components/inputInfoArea.tsx` | getElementById 削除、Context 使用 |
| 10 | `Frontend/components/selectedCallsignDisplay.tsx` | 新規（callsign 表示専用、Context 購読） |

---

## 検証

1. **Operator 画面**: レーダーで航空機クリック → callsign 表示・ControlAircraft・SelectFixMode が正しく動作
2. **Controller 画面**: 同様に FlightPlanDisplay・FlightPlanControl・InputAircraftInfo が正しく動作
3. **ポーリング削除**: 開発者ツールで `getElementById` の呼び出しが無くなることを確認
4. **既存テスト**: `npm run build` が通ること

---

## リスク・注意点

- `controlAircraft.tsx` と `radarCanvas.tsx` の `confirmButton` イベントリスナーは、`selectedAircraftRef` を参照している。callsign のみ Context 化した場合、altitude/speed/heading は引き続き DOM から取得するため、この部分の挙動は変更しない。
- Operator と Controller でレイアウトが異なるため、`SelectedCallsignDisplay` の見た目は各ページのスタイルに合わせて調整する。

---

## 関連ドキュメント

- [DEFERRED_ITEMS](../DEFERRED_ITEMS/spec.md) — 項目 #6
- [flight-plan](../flight-plan/spec.md) — フライトプラン機能の経緯
