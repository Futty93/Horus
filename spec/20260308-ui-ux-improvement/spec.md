# UI/UX 改善 - トップページとコントロールパネル

## メタデータ

- **Status**: Done
- **Date**: 2026-03-08

## 概要

初回訪問ユーザーが「何をすべきか」「各画面で何ができるか」を直感的に理解できるよう、トップページをオンボーディングハブとして再設計し、コントロールパネルを情報階層と視覚的グルーピングで改善する。設計指針は [frontend-design SKILL](https://github.com/anthropics/claude-code/blob/main/plugins/frontend-design/skills/frontend-design/SKILL.md) に準拠する。

## 背景・課題

### 現状

- **トップページ**: 「Welcome to the Home Page」＋ Operator / Controller への2リンクのみ。初見ユーザーは役割の違いや推奨フローが不明
- **コントロールパネル**: Controller と Operator の右側パネルは `bg-cyber-gradient` や `bg-zinc-900` を使用しているが、Controller 側はデザインが不統一（InputAircraftInfo が green-400 ベースで ControlAircraft と異なる）。セクションのグルーピング・見出し・余白の階層が弱い

### 課題（Problem Statement）

- 初めて開いたユーザーが「まず Controller で航空機を登録し、Operator で管制する」というワークフローを把握できない
- Operator / Controller の役割説明が画面上に存在しない
- コントロールパネルが機能的には揃っているが、視覚的なまとまりが弱く、設定・制御・入力エリアの区別がつきにくい

### なぜ今か（Motivation）

フロントエンドの核となる画面の第一印象を改善し、研究・教育利用におけるオンボーディング体験を向上させる。既存のサイバーテーマを活かしつつ、frontend-design の原則で差別化されたデザインに昇華する。

---

## デザイン方針（frontend-design 準拠）

### Design Thinking（2026 改訂: ATC 洗練テーマへ移行）

| 項目 | 決定 |
|------|------|
| **Purpose** | 航空管制レーダーとしての訓練シミュレータ。Controller / Operator の役割説明と導線提供 |
| **Tone** | **Industrial / Utilitarian** — 本物の ATC コンソールに近い洗練された見た目。cyber テーマは廃止 |
| **Constraints** | Next.js + React、Tailwind CSS、アクセシビリティ対応 |
| **Differentiation** | ネオンやグローを廃止し、控えめなアクセントカラー（`atc-accent`）と高い可読性で管制業務に適した UI |

### Typography（フォント）

- Geist Sans / Geist Mono を維持
- 見出しは `font-mono` + `tracking-wider` で管制画面らしい機械的・情報的な印象
- Inter / Roboto / Arial は使用しない

### Color & Theme（ATC パレット）

- **背景**: `atc-bg` (#0d1117), `atc-surface` (#161b22), `atc-surface-elevated` (#21262d)
- **ボーダー**: `atc-border` (#30363d)
- **テキスト**: `atc-text` (#c9d1d9), `atc-text-muted` (#8b949e)
- **アクセント**: `atc-accent` (#238636), `atc-accent-hover` (#2ea043) — 控えめな緑
- **警告・危険**: `atc-warning` (#9e6a03), `atc-danger` (#cf2222)
- cyber / neon / グラデーションは廃止

### Motion

- トップページ: staggered fade-in のみ。`animation-delay` で 0ms, 100ms, 200ms
- コントロールパネル: glow / scan アニメーションは廃止。ホバーは `transition-colors` のみ

### Spatial Composition

- トップページ: 中央に縦レイアウト。タイトル → 説明 → 2カード
- コントロールパネル: `rounded-lg` + `border-atc-border` のカード型。`space-y-4` で余白を確保

### Backgrounds & Visual Details

- ソリッドカラー中心。グラデーションやノイズテクスチャは使用しない
- スクロールバーは控えめな `atc-border` トーン

### 避けるべき点（NEVER）

- Inter, Roboto, Arial
- 白背景に紫グラデーション
- ネオングロー、スキャンライン、過剰なアニメーション

---

## 方針

### 決定方針（Decision）

1. **トップページ再構築**
   - ヒーロー: 「HORUS」ロゴ + 1文のシステム説明
   - 2つのナビゲーションカード: Operator / Controller。各カードに「何ができるか」を2〜3行で記載
   - 推奨フロー: 「はじめに Controller で航空機を登録し、Operator で管制指示を実行します」を文言で補足

2. **コントロールパネル統一**
   - Controller の InputAircraftInfo を ControlAircraft 相当の cyber デザインに統一
   - 両画面のパネルを共通の「カード型セクション」コンポーネントで包み、見出し・余白・ボーダーを統一
   - 設定エリア・制御エリア・入力エリアの3層でグルーピング

### 検討した他案（Alternatives Considered）

- **案 A**: トップをダッシュボード化し、レーダー縮小版を埋め込む。採用しなかった理由: 初回負荷・複雑さが増すため、シンプルなハブを優先
- **案 B**: コントロールパネルをタブやアコーディオンで折りたたむ。採用しなかった理由: 管制画面では主要操作を常時表示したいため、全表示＋スクロールで対応

### トレードオフ（Trade-offs）

- **メリット**: 初見ユーザーの理解促進、デザイン一貫性の向上、frontend-design 原則に沿った差別化
- **デメリット / 受容する制約**: 既存コンポーネントのスタイル変更により、テストのスクリーンショットや E2E のセレクタに影響が出る可能性

---

## 完了条件（Success Criteria）

- [x] トップページに「HORUS」＋システム説明＋Operator/Controller の役割説明が表示される
- [x] 初回訪問ユーザーが「まず Controller、次に Operator」という推奨フローを画面上で把握できる
- [x] Controller の InputAircraftInfo が Operator の ControlAircraft と同等の cyber テーマで統一されている
- [x] コントロールパネル内のセクション（設定・制御・入力）が視覚的に区別できる

---

## 影響範囲

- **app/page.tsx**: トップページを全面書き換え
- **app/controller/page.tsx**: コントロールパネル構造の微調整（必要に応じて）
- **app/operator/page.tsx**: 現状維持 or 細部調整
- **components/inputInfoArea.tsx**: スタイルを ControlAircraft 相当に統一
- **Frontend/README.md**: トップページ・コントロールパネルの説明を更新

---

## 実装計画

### Phase 1: トップページ

1. `app/page.tsx` を新レイアウトで実装
   - ヒーロー（ロゴ + 説明）
   - Operator / Controller カード（リンク + 役割説明）
   - 推奨フローの補足文
2. staggered アニメーション（`animation-delay`）を適用
3. レスポンシブ対応（モバイルで縦並び維持）

### Phase 2: コントロールパネル

1. `InputAircraftInfo` のスタイルを `control-gradient`, `radar-primary`, `button-gradient` 等で統一
2. Controller / Operator のパネル内セクションに共通のカード型スタイルを適用（必要なら `ControlPanelSection` のようなラッパーを検討）
3. 見出しのフォント・余白を統一

### Phase 3: ドキュメント更新

1. `Frontend/README.md` の「主要コンポーネント」「トップページ」説明を更新

---

## 検証

- [x] テストが通る
- [x] ビルドが通る
- [ ] 手動動作確認（トップ → Controller → Operator の遷移、制御パネル操作）

---

## 未解決事項（Unresolved Questions）

- トップページに「バックエンドが起動しているか」のヘルスチェック表示を入れるか
- 多言語（日本語 / 英語）対応の要否

---

## Out of Scope（本 spec 対象外）

- **指示メモのレーダーラベル表示**: Controller が記録した指示メモを、本物のレーダー同様に航空機ラベル（データブロック）の隣や上に表形式で表示する機能。現在は InstructionMemo コンポーネント内でのみ表示。将来的な拡張として検討。

---

## 関連ドキュメント

- [frontend-design SKILL](https://github.com/anthropics/claude-code/blob/main/plugins/frontend-design/skills/frontend-design/SKILL.md)
- [Frontend/README.md](../../Frontend/README.md)
