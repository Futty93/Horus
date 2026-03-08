# フライトプラン関連・保留事項（PR レビュー）

将来対応を検討する項目。方針のみ記載。

| # | 項目 | 方針 |
|---|------|------|
| 3 | FlightPlanController IATA ハードコード | DTO に departureIata / arrivalIata / eta を追加し、呼び出し元から渡す形に変更 |
| 4 | FlightPlanController ダウンキャスト多用 | Aircraft にフライトプラン関連メソッドを追加するか、FlightPlanCapableAircraft インターフェースを導入して型安全化 |
| 6 | フロント DOM ポーリング (getElementById callsign) | React Context または props で callsign を共有し、DOM 依存を排除 |
| 8 | スレッドセーフティ (Controller からの setDirectTo 等) | Controller → Service 経由の呼び出しを nextStep と同様に Repository の lock 内で実行するよう設計を見直す |
