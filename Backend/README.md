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

Interface representing an aircraft entity.

## Contents
- Method definitions for aircraft properties and operations.


#### `domain/model/entity/aircraft/AircraftRepository.java`


# AircraftRepository.java

Interface for repository operations related to aircraft.

## Contents
- Method definitions for accessing and managing aircraft data.


#### `domain/model/entity/aircraft/CommercialAircraft.java`


# CommercialAircraft.java

Implementation of the `Aircraft` interface, representing a commercial aircraft.

## Contents
- Specific properties and methods for commercial aircraft.
- CommercialAircraft クラスは、商業航空機を扱う際に使用され、シミュレーションや管制システム内で特定の航空機に対する操作や情報の保持を行うために使われます。


#### `domain/model/service/scenario/ScenarioService.java`


# ScenarioService.java

Interface for handling scenarios involving aircraft and airspace.

## Contents
- Method definitions for managing and processing flight scenarios.


#### `domain/model/service/scenario/ScenarioServiceImpl.java`


# ScenarioServiceImpl.java

Implementation of `ScenarioService`.

## Contents
- Logic for processing flight scenarios and updating aircraft information.


#### `domain/model/valueObject/Callsign/Callsign.java`


# Callsign.java

Value object representing the aircraft's callsign.

## Contents
- Properties and methods for handling aircraft callsigns.


#### `domain/model/valueObject/Callsign/Company.java`


# Company.java

Value object representing an airline company.

## Contents
- Properties and methods for airline company information.


#### `domain/model/valueObject/Callsign/FlightNumber.java`


# FlightNumber.java

Value object representing a flight number.

## Contents
- Properties and methods for managing flight numbers.


#### `domain/model/valueObject/Position/AircraftPosition.java`


# AircraftPosition.java

Value object representing the position of an aircraft.

## Contents
- Properties and methods for handling aircraft positions, including latitude, longitude, altitude, etc.


#### `infrastructure/persistance/inMemory/AircraftRepositoryInMemory.java`


# AircraftRepositoryInMemory.java

In-memory implementation of `AircraftRepository`.

## Contents
- Methods for storing and retrieving aircraft data in-memory.
- Used for testing or development purposes.


#### `interfaces/api/CreateAircraftService.java`


# CreateAircraftService.java

API interface for creating aircraft.

## Contents
- Method definitions for handling aircraft creation requests.


#### `interfaces/api/HelloService.java`


# HelloService.java

Controller for handling basic API requests for testing purposes.

## Contents
- `GET /hello/` endpoint returning a simple greeting.
- `GET /hello/goodbye` endpoint returning a farewell message with an optional name parameter.


#### `interfaces/api/LocationService.java`


# LocationService.java

API interface for handling aircraft location requests.

## Contents
- Method definitions for retrieving the locations of aircraft within the airspace.


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

