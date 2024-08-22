<!-- バックエンドのREADME -->
以下は、航空機の情報を受け取り、その後の状態を計算し、管制指示を反映するシステムのためのファイル構成の提案です。各ファイルに何が記載されるべきかを含めた構成ツリーと、それぞれのファイルの内容の説明をMarkdown形式で出力します。

### Backendのファイル構成

```
.
├── AtcSimulatorApplication.java
├── application
│   ├── AircraftRadarService.java
│   └── aircraft
│       └── AircraftRadarServiceImpl.java
├── config
│   ├── AtcSimulatorApplicationConfig.java
│   └── WebConfig.java
├── domain
│   └── model
│       ├── aggregate
│       │   └── airspace
│       │       ├── AirSpace.java
│       │       ├── AirSpaceImpl.java
│       │       ├── AirspaceManagement.java
│       │       └── AirspaceManagementImpl.java
│       ├── entity
│       │   └── aircraft
│       │       ├── Aircraft.java
│       │       ├── AircraftRepository.java
│       │       └── CommercialAircraft.java
│       ├── service
│       │   └── scenario
│       │       ├── ScenarioService.java
│       │       └── ScenarioServiceImpl.java
│       └── valueObject
│           ├── Callsign
│           │   ├── Callsign.java
│           │   ├── Company.java
│           │   └── FlightNumber.java
│           └── Position
│               └── AircraftPosition.java
├── infrastructure
│   └── persistance
│       └── inMemory
│           └── AircraftRepositoryInMemory.java
└── interfaces
    ├── api
    │   ├── CreateAircraftService.java
    │   ├── HelloService.java
    │   ├── LocationService.java
    │   └── ControlService.java
    └── dto
        └── CreateAircraftDto.java
```

### 各ファイルの内容

#### `AtcSimulatorApplication.java`


# AtcSimulatorApplication.java

This is the main application class for starting the Spring Boot application. It contains the `main` method which runs the application.

## Contents
- `@SpringBootApplication` annotation for Spring Boot configuration.
- `public static void main(String[] args)` method to launch the application.


#### `application/AircraftRadarService.java`


# AircraftRadarService.java

Interface for managing aircraft radar services.

## Contents
- Method definitions for retrieving and managing aircraft information.


#### `application/aircraft/AircraftRadarServiceImpl.java`


# AircraftRadarServiceImpl.java

Implementation of `AircraftRadarService`. Handles aircraft radar data processing.

## Contents
- Implementation of methods to manage aircraft information.
- Logic for updating and retrieving aircraft status based on radar data.


#### `config/AtcSimulatorApplicationConfig.java`


# AtcSimulatorApplicationConfig.java

Configuration class for defining beans used in the application.

## Contents
- Bean definitions for `AircraftRadarService`, `CreateAircraftService`, and other service components.


#### `config/WebConfig.java`


# WebConfig.java

Configuration class for setting up CORS (Cross-Origin Resource Sharing) in the application.

## Contents
- `addCorsMappings(CorsRegistry registry)` method to allow cross-origin requests.


#### `domain/model/aggregate/airspace/AirSpace.java`


# AirSpace.java

航空機が飛行する空域を管理するクラス
このクラスは、空域内の航空機を追跡し、位置情報や他の動的データを管理するために使用されます

## Contents
- aircrafts マップ:
  - このマップは、空域内に存在する航空機を追跡するために使用されます。キーは航空機のコールサイン (String) で、値は Aircraft オブジェクトです。
- addAircraft(Aircraft aircraft) メソッド:
  - 空域に航空機を追加するメソッドです。航空機のコールサインをキーとして aircrafts マップに追加します。
- removeAircraft(String callsign) メソッド:
  - 空域から指定されたコールサインの航空機を削除するメソッドです。
- getAircraft(String callsign) メソッド:
  - 指定されたコールサインの航空機を返すメソッドです。
- getAllAircrafts() メソッド:
  - 空域内に存在するすべての航空機を返すメソッドです。返されるのは Aircraft オブジェクトの Collection です。
- updateAircraftPosition(String callsign, Position newPosition) メソッド:
  - 指定されたコールサインの航空機の位置を更新するメソッドです。新しい位置 (newPosition) を Aircraft オブジェクトにセットします。
- getAircraftCount() メソッド:
  - 現在空域内に存在する航空機の数を返すメソッドです。


#### `domain/model/aggregate/airspace/AirSpaceImpl.java`

# AirSpaceImpl.java

`AirSpaceImpl.java` は、`AirSpace` インターフェースの実装クラスで、空域内の航空機を管理し、システム全体で使用されるビジネスロジックを提供する
`AirSpaceImpl` は `AirSpace` インターフェースを実装し、実際の空域操作を扱うためのメソッドを定義する

## Contents
- **`AirSpaceImpl` クラス**:
  - `AirSpace` インターフェースを実装しており、空域に関する操作を提供します。`AirSpaceImpl` クラスは、航空機を追加・削除し、位置情報の更新や取得などを行います。

- **`aircrafts` マップ**:
  - 空域内のすべての航空機を追跡するために使用される `Map` です。キーは航空機のコールサイン (`String`)、値は `Aircraft` オブジェクトです。

- **`addAircraft(Aircraft aircraft)` メソッド**:
  - 空域に新しい航空機を追加します。コールサインをキーとして `aircrafts` マップに追加します。

- **`removeAircraft(String callsign)` メソッド**:
  - 指定されたコールサインの航空機を空域から削除します。

- **`getAircraft(String callsign)` メソッド**:
  - 指定されたコールサインに対応する航空機を取得します。

- **`getAllAircrafts()` メソッド**:
  - 空域内のすべての航空機を取得します。返されるのは `Aircraft` オブジェクトの `Collection` です。

- **`updateAircraftPosition(String callsign, Position newPosition)` メソッド**:
  - 指定された航空機の位置を更新します。

- **`getAircraftCount()` メソッド**:
  - 現在空域内に存在する航空機の数を返します。

- **`isAircraftOverlapping(Position position)` メソッド**:
  - 指定された位置に他の航空機が存在するかをチェックします。存在する場合は `true` を返し、存在しない場合は `false` を返します。


#### `domain/model/aggregate/airspace/AirspaceManagement.java`


# AirspaceManagement.java

`AirspaceManagement.java` は、空域管理のインターフェースとして機能し、`AirSpace` 内の航空機を制御および管理するためのメソッドを提供する
これには、航空機の追加、削除、位置の更新などの操作が含まれる

## Contents
- **`AirspaceManagement` インターフェース**:
  - `AirSpace` 内の航空機を管理するためのメソッドを提供します。このインターフェースは、航空機の追加、削除、位置の更新、航空機の取得、航空機の数を取得するなど、空域の管理に必要な操作を定義します。

- **`addAircraftToAirspace(Aircraft aircraft)` メソッド**:
  - 空域に新しい航空機を追加します。これは新しい航空機が空域に侵入する際に使用されます。

- **`removeAircraftFromAirspace(String callsign)` メソッド**:
  - 指定されたコールサインに対応する航空機を空域から削除します。

- **`getAllAircraftsInAirspace()` メソッド**:
  - 空域内に存在するすべての航空機を取得します。返されるのは `Aircraft` オブジェクトのコレクションです。

- **`updateAircraftPositionInAirspace(String callsign, Position newPosition)` メソッド**:
  - 指定されたコールサインに対応する航空機の位置を更新します。

- **`getAircraftCountInAirspace()` メソッド**:
  - 現在空域内に存在する航空機の数を返します。

- **`isAircraftOverlappingInAirspace(Position position)` メソッド**:
  - 指定された位置に他の航空機が存在するかどうかを確認します。存在する場合は `true`、存在しない場合は `false` を返します。


#### `domain/model/aggregate/airspace/AirspaceManagementImpl.java`


# AirspaceManagementImpl.java

`AirspaceManagement` インターフェースを実装したクラス
このクラスでは、空域内の航空機の管理を具体的に行う

## Contents
- **`aircraftsInAirspace`**: `Map<String, Aircraft>` 型のデータ構造で、空域内の航空機をコールサインをキーとして管理しています。

- **`addAircraftToAirspace(Aircraft aircraft)` メソッド**: 新しい航空機を空域に追加します。このメソッドは、`aircraftsInAirspace` マップに航空機を追加します。

- **`removeAircraftFromAirspace(String callsign)` メソッド**: 指定されたコールサインを持つ航空機を空域から削除します。

- **`getAllAircraftsInAirspace()` メソッド**: 空域内のすべての航空機を返します。

- **`updateAircraftPositionInAirspace(String callsign, Position newPosition)` メソッド**: 指定されたコールサインを持つ航空機の位置を更新します。

- **`getAircraftCountInAirspace()` メソッド**: 空域内の航空機の数を返します。

- **`isAircraftOverlappingInAirspace(Position position)` メソッド**: 指定された位置に既に他の航空機が存在するかどうかを確認します。


#### `domain/model/entity/aircraft/Aircraft.java`


# Aircraft.java

航空機を表現するためのエンティティクラス

- **`Callsign`**: 航空機のコールサインを表すためのオブジェクト（`Callsign` クラスを使用）。
- **`AircraftPosition`**: 航空機の位置（緯度・経度）を表すオブジェクト（`AircraftPosition` クラスを使用）。
- **`altitude`**: 高度をフィートで表す整数値。
- **`groundSpeed`**: 地上速度をノットで表す整数値。
- **`verticalSpeed`**: 上昇または下降速度をフィート毎分で表す整数値。
- **`heading`**: 航空機の機首方向を0度から360度で表す整数値。
- **`type`**: 航空機のICAO機種コードを表す文字列。
- **`originIata`, `originIcao`, `destinationIata`, `destinationIcao`**: 出発地と目的地のIATAおよびICAOコードを表す文字列。
- **`eta`**: 到着予定時刻をISO 8601形式で表す文字列。

### Method

- **`updatePosition`**: 経過時間（秒）に基づいて航空機の位置を更新するメソッド。このメソッドは、機首方向と速度を基にして位置を計算し、また垂直速度を基に高度を更新します。


#### `domain/model/entity/aircraft/AircraftRepository.java`


# AircraftRepository.java

航空機 (Aircraft) エンティティを保存・取得するためのリポジトリインターフェース

## Contents
- **`save(Aircraft aircraft)`**: 航空機エンティティを保存するメソッド。新しい航空機を追加するか、既存の航空機を更新します。
- **`findByCallsign(Callsign callsign)`**: 指定されたコールサインに基づいて航空機を取得するメソッド。存在しない場合は空の `Optional<Aircraft>` を返します。
- **`findAll()`**: 保存されているすべての航空機エンティティをリストとして取得するメソッド。
- **`deleteByCallsign(Callsign callsign)`**: 指定されたコールサインに基づいて航空機を削除するメソッド。
- **`update(Aircraft aircraft)`**: 航空機の情報を更新するメソッド。このメソッドは `save()` メソッドと同様の役割を果たしますが、更新時には特定の処理を追加することができます。


#### `domain/model/entity/aircraft/CommercialAircraft.java`


# CommercialAircraft.java

Aircraft クラスを拡張した商業航空機を表すエンティティクラス

## Contents
- Specific properties and methods for commercial aircraft.
- CommercialAircraft クラスは、商業航空機を扱う際に使用され、シミュレーションや管制システム内で特定の航空機に対する操作や情報の保持を行うために使われます。


#### `domain/model/service/scenario/ScenarioService.java`


# ScenarioService.java

シナリオに基づいて空域や航空機の状態を管理するためのサービスを提供するインターフェース

## Contents
1. **`initializeScenario()`**:
   - シナリオを初期化するメソッドです。シナリオが開始される前に必要な設定や準備を行います。

2. **`generateAircraftsForScenario()`**:
   - シナリオに基づいて航空機を生成し、リストとして返すメソッドです。シナリオによって異なる航空機が生成されます。

3. **`updateScenarioAircraftPositions()`**:
   - シナリオに基づいて航空機の位置や状態を更新するメソッドです。時間の経過やシナリオの進行に応じて、航空機の動きをシミュレーションします。

4. **`getScenarioStatus()`**:
   - 現在のシナリオのステータスや進行状況を取得するメソッドです。フロントエンドや他のシステムと連携する際に使用します。

5. **`terminateScenario()`**:
   - シナリオを終了するための処理を行うメソッドです。シナリオの終了後に必要なクリーンアップやデータの保存を行います。

### 利用例

`ScenarioService` は、シナリオベースのシミュレーションを行うための基盤となります。例えば、航空機が登場するシナリオを開始するときには `initializeScenario()` を呼び出し、シナリオ進行中に `updateScenarioAircraftPositions()` を使って航空機の位置を更新します。また、シナリオの進行状況を確認したいときには `getScenarioStatus()` を使用し、シナリオが終了したら `terminateScenario()` で後処理を行います。



#### `domain/model/service/scenario/ScenarioServiceImpl.java`


# ScenarioServiceImpl.java

`ScenarioService` インターフェースを実装するクラス
シナリオの初期化、航空機の生成、位置の更新など、シナリオに基づいたシステムの動作を具体的に制御する

## Contents
1. **`initializeScenario()`**:
   - シナリオを初期化し、空域をクリアします。シナリオのステータスを「初期化済み」に設定し、初期化メッセージを表示します。

2. **`generateAircraftsForScenario()`**:
   - シナリオに基づいて新しい航空機を生成し、その航空機をリポジトリに保存します。例として、`Aircraft` オブジェクトを作成してリストに追加します。この方法で他の航空機も生成できます。

3. **`updateScenarioAircraftPositions()`**:
   - シナリオに基づいて、すべての航空機の位置を更新します。航空機リストを取得し、それぞれの航空機の位置を更新し、再度リポジトリに保存します。

4. **`getScenarioStatus()`**:
   - 現在のシナリオのステータスを返します。これにより、システムの外部からシナリオの進行状況を把握できます。

5. **`terminateScenario()`**:
   - シナリオを終了し、ステータスを「終了済み」に設定します。終了メッセージを表示します。


#### `domain/model/valueObject/Callsign/Callsign.java`


# Callsign.java

航空機のコールサイン（無線呼び出し符号）を表すクラス
コールサインに関連する情報や操作をカプセル化

## Contents
1. **コンストラクタ**:
   - `airlineCode` と `flightNumber` の2つのパラメータを受け取り、コールサインを初期化します。これらのフィールドは、コールサインを一意に識別するために使用されます。入力がnullや空文字の場合は、`IllegalArgumentException`をスローして入力の妥当性を保証します。

2. **`getFullCallsign()`**:
   - 完全なコールサイン（航空会社コード + フライトナンバー）を返します。これは、コールサインをフルフォーマットで表示するために使用されます。

3. **`getAirlineCode()`** および **`getFlightNumber()`**:
   - それぞれ航空会社コードとフライトナンバーを個別に取得するためのメソッドです。

4. **`equals()`** および **`hashCode()`**:
   - コールサインが等しいかどうかを判断するために `equals` をオーバーライドし、`hashCode` メソッドもオーバーライドして、コールサインが同じ場合に同じハッシュコードを返すようにしています。

5. **`toString()`**:
   - `Callsign` オブジェクトを文字列として表現するためのメソッドです。デバッグやログ出力時に役立ちます。


#### `domain/model/valueObject/Callsign/Company.java`


# Company.java

航空会社の情報を管理するクラス
航空会社コードや名前など、航空会社に関連するデータを格納し、必要に応じて操作する機能を提供

## Contents
1. **コンストラクタ**:
   - `airlineCode` と `airlineName` の2つのパラメータを受け取り、航空会社を初期化します。これらのフィールドは、航空会社を一意に識別するために使用されます。入力がnullや空文字の場合は、`IllegalArgumentException`をスローして入力の妥当性を保証します。

2. **`getAirlineCode()`** および **`getAirlineName()`**:
   - それぞれ航空会社コードと航空会社名を個別に取得するためのメソッドです。

3. **`equals()`** および **`hashCode()`**:
   - 航空会社が等しいかどうかを判断するために `equals` をオーバーライドし、`hashCode` メソッドもオーバーライドして、航空会社が同じ場合に同じハッシュコードを返すようにしています。

4. **`toString()`**:
   - `Company` オブジェクトを文字列として表現するためのメソッドです。デバッグやログ出力時に役立ちます。


#### `domain/model/valueObject/Callsign/FlightNumber.java`


# FlightNumber.java

航空機のフライト番号を管理するクラス

## Contents
1. **コンストラクタ**:
   - `flightNumber` パラメータを受け取り、フライト番号を初期化します。入力されたフライト番号が `null` または空文字の場合、`IllegalArgumentException` をスローします。
   - 正規表現 `\\d+` を使用して、フライト番号が数字で構成されているかどうかを検証します。数字以外の文字が含まれている場合、例外をスローします。

2. **`getFlightNumber()`**:
   - フライト番号を返すためのメソッドです。このメソッドを使用して、フライト番号を取得できます。

3. **`equals()`** および **`hashCode()`**:
   - フライト番号が等しいかどうかを判断するために `equals` メソッドをオーバーライドし、`hashCode` メソッドもオーバーライドして、一意性を保証します。

4. **`toString()`**:
   - `FlightNumber` オブジェクトを文字列として表現するためのメソッドです。このメソッドはデバッグやログの出力時に役立ちます。


#### `domain/model/valueObject/Position/AircraftPosition.java`


# AircraftPosition.java

航空機の位置情報を管理するためのクラス
緯度、経度、機首方向、速度、垂直速度、高度などの航空機の位置情報を保持し、航空機の状態を追跡するために使用

## Contents
1. **コンストラクタ**:
   - 緯度、経度、機首方向、高度、対地速度、垂直速度を受け取り、航空機の位置情報を初期化します。

2. **ゲッター**:
   - 各フィールドの値を取得するためのメソッド (`getLatitude()`, `getLongitude()`, `getHeading()`, `getAltitude()`, `getGroundSpeed()`, `getVerticalSpeed()`) を提供します。

3. **`equals()` および `hashCode()`**:
   - 同じ航空機の位置情報であるかどうかを判定するための `equals` メソッドをオーバーライドし、一意性を保証するために `hashCode` メソッドもオーバーライドしています。

4. **`toString()`**:
   - 航空機の位置情報を文字列として表現するためのメソッドです。デバッグやログ出力時に役立ちます。


#### `infrastructure/persistance/inMemory/AircraftRepositoryInMemory.java`


# AircraftRepositoryInMemory.java

航空機情報をメモリ内で管理するリポジトリクラス
航空機情報を格納・取得・更新・削除するための機能を提供
通常、リポジトリクラスはデータベースにアクセスしますが、この場合はインメモリでのデータ管理を行う

## Contents
1. **`ConcurrentMap`**:
   - `ConcurrentMap<String, Aircraft>` はスレッドセーフなマップで、航空機情報を `callsign` をキーにして格納しています。この構造は、並行アクセスが必要な状況でも安全に動作します。

2. **`findAll()`**:
   - メモリ内に格納されたすべての航空機情報をリストとして返します。このメソッドは、すべての航空機の情報を取得するために使用されます。

3. **`findById()`**:
   - 指定された `callsign` に対応する航空機情報を返します。航空機が存在する場合は `Optional` でラップされた `Aircraft` を返し、存在しない場合は `Optional.empty()` を返します。

4. **`save()`**:
   - メモリ内に航空機情報を保存します。既存の `callsign` がある場合は、そのエントリを更新します。

5. **`deleteById()`**:
   - 指定された `callsign` に対応する航空機情報を削除します。


#### `interfaces/api/CreateAircraftService.java`


# CreateAircraftService.java

API 層で使用されるコントローラクラスであり、新しい航空機を作成するためのエンドポイント

## Contents
1. **依存関係の注入**:
   - `AircraftRadarService` は、Spring の `@Autowired` アノテーションを使用して依存関係として注入されます。このサービスクラスは、航空機の作成ロジックを実行します。

2. **エンドポイントの設定**:
   - `@RestController` と `@RequestMapping` により、このクラスは REST API のコントローラとして機能します。すべてのリクエストは `/api/aircraft` のパスに関連付けられます。
   - `@PostMapping("/create")` によって、HTTP POST リクエストが `/api/aircraft/create` にマッピングされます。このエンドポイントは、新しい航空機を作成するために使用されます。

3. **`createAircraft()` メソッド**:
   - このメソッドは、`@RequestBody` アノテーションを使用して、リクエストボディから `CreateAircraftDto` を受け取ります。
   - サービス層の `createAircraft()` メソッドを呼び出して、新しい航空機を作成します。
   - 作成された航空機を含む `ResponseEntity` を返し、HTTP ステータスコードとして `HttpStatus.CREATED`（201 Created）を設定します。

### 期待する使用シナリオ

- フロントエンドから新しい航空機を作成するリクエストを送信すると、このエンドポイントが呼び出されます。
- リクエストボディには、航空機の情報が `CreateAircraftDto` として含まれます。
- サービス層が呼び出され、新しい航空機が作成されます。
- 作成された航空機の情報がクライアントに返されます。


#### `interfaces/api/HelloService.java`


# HelloService.java

Controller for handling basic API requests for testing purposes.

## Contents
- `GET /hello/` endpoint returning a simple greeting.
- `GET /hello/goodbye` endpoint returning a farewell message with an optional name parameter.


#### `interfaces/api/LocationService.java`


# LocationService.java

航空機の位置情報を取得するためのリクエストを受け付け、サービス層と連携して必要なデータを返す

## Contents
1. **依存関係の注入**:
   - `AircraftRadarService` は、航空機の位置情報を取得するために使用されるサービスクラスです。Spring の `@Autowired` アノテーションを使用して依存関係として注入されています。

2. **エンドポイントの設定**:
   - `@RestController` と `@RequestMapping("/api/location")` により、このクラスは REST API のコントローラとして機能します。すべてのリクエストは `/api/location` のパスに関連付けられます。

3. **`getAllAircraftPositions()` メソッド**:
   - `@GetMapping("/all")` により、このエンドポイントはすべての航空機の位置情報を取得するために使用されます。
   - `AircraftRadarService` のメソッドを呼び出し、全航空機の位置情報をリストで返します。

4. **`getAircraftPosition()` メソッド**:
   - `@GetMapping("/{callsign}")` により、特定の航空機の位置情報を取得するエンドポイントが定義されています。
   - `@PathVariable` アノテーションを使用して、URL パスから `callsign` を取得し、対応する航空機の位置情報を取得します。

### 期待する使用シナリオ

- **全航空機の位置情報取得**:
  - `/api/location/all` エンドポイントを使用して、管制空域内のすべての航空機の位置情報を取得します。このデータはフロントエンドで航空機の位置を表示する際に使用されます。

- **特定の航空機の位置情報取得**:
  - `/api/location/{callsign}` エンドポイントを使用して、指定されたコールサイン（`callsign`）に基づいて、特定の航空機の位置情報を取得します。

### 注意点

- **エラーハンドリング**:
  - 特定の航空機が存在しない場合や、位置情報が取得できない場合のエラーハンドリングが必要です。`AircraftRadarService` 内で適切な例外処理を行うことが推奨されます。


#### `interfaces/api/ControlService.java`


# ControlService.java

API interface for sending control commands to aircraft.

## Contents
- Method definitions for sending control instructions to aircraft.
- Processing and applying control commands.


#### `interfaces/dto/CreateAircraftDto.java`


# CreateAircraftDto.java

Data Transfer Object (DTO) for creating aircraft.

## Contents
- Fields for aircraft creation details such as callsign, type, and initial position.
- Used for transferring data between the frontend and backend.

