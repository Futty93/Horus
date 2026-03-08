# Horus 開発方針

## 実装計画（spec）の作成

新機能・リファクタリング・設計変更を実施する際は、**必ず spec を作成**してください。

### spec の形式

- **パス**: `spec/YYYYMMDD-xxxxxx/spec.md`
- **YYYYMMDD**: spec 作成日または着手予定日（例: 20260308）
- **xxxxxx**: 機能名・変更内容を表すスラッグ（例: frontend-bff, flight-plan）

### spec に含める内容

- **メタデータ**: Status, Author, Date（RFC/ADR 形式）
- **概要・背景・課題**: 問題の定義、なぜ今か
- **方針**: 決定方針、検討した他案、トレードオフ（MADR 風）
- **完了条件**: 測定可能な Success Criteria（Google Design Doc 風）
- **影響範囲・実装計画**: Phase ごとのステップ
- **検証・未解決事項**: テスト方針、残課題

### spec 記述のベストプラクティス

| 原則 | 内容 |
|------|------|
| **問題ファースト** | 「なぜ」を先に、解決策は後で。Motivation を明確に |
| **他案の検討** | 採用した案以外に何を検討したか、なぜ却下したかを記載 |
| **トレードオフの明示** | メリット・デメリットを明文化し、受容する制約を記録 |
| **測定可能な条件** | 「速い」「使いやすい」ではなく、具体的な完了条件を書く |
| **実装中立** | 可能な範囲で What/Why を中心に、How は設計に委ねる |

### spec の新規作成

テンプレートから spec をコピーして作成するには、以下を実行してください。

```bash
npm run spec:new <スラッグ>
```

例:

```bash
npm run spec:new feature-auth
# → spec/20260308-feature-auth/spec.md が作成される（日付は実行日）
```

---

## 関連ドキュメント

- [project_wide.mdc](../.cursor/rules/project_wide.mdc) — プロジェクト共通ルール
- [Backend README](../Backend/README.md)
- [Frontend README](../Frontend/README.md)
