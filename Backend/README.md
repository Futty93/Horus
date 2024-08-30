<!-- バックエンドのREADME -->
以下は、航空機の情報を受け取り、その後の状態を計算し、管制指示を反映するシステムのためのファイル構成の提案です。各ファイルに何が記載されるべきかを含めた構成ツリーと、それぞれのファイルの内容の説明をMarkdown形式で出力します。

### Backendのファイル構成

```
.
├── AtcSimulatorApplication.java
├── application
│   ├── AircraftRadarService.java
│   └── aircraft
│       └── AircraftRadarServiceImpl.java
├── config
│   ├── AtcSimulatorApplicationConfig.java
│   └── WebConfig.java
├── domain
│   └── model
│       ├── aggregate
│       │   └── airspace
│       │       ├── AirSpace.java
│       │       ├── AirSpaceImpl.java
│       │       ├── AirspaceManagement.java
│       │       └── AirspaceManagementImpl.java
│       ├── entity
│       │   └── aircraft
│       │       ├── Aircraft.java
│       │       ├── AircraftBase.java
│       │       ├── AircraftRepository.java
│       │       └── CommercialAircraft.java
│       ├── service
│       │   └── scenario
│       │       ├── ScenarioService.java
│       │       └── ScenarioServiceImpl.java
│       └── valueObject
│           ├── Callsign
│           │   ├── Callsign.java
│           │   ├── Company.java
│           │   └── FlightNumber.java
│           ├── Position
│           │   ├── AircraftPosition.java
│           │   ├── AircraftVector.java
│           │   └── InstructedVector.java
│           └── Type
│               └── AircraftType.java
├── infrastructure
│   └── persistance
│       └── inMemory
│           └── AircraftRepositoryInMemory.java
└── interfaces
    ├── api
    │   ├── ControlAircraftService.java
    │   ├── CreateAircraftService.java
    │   ├── HelloService.java
    │   └── LocationService.java
    └── dto
        ├── ControlAircraftDto.java
        └── CreateAircraftDto.java
```