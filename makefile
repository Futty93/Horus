protoAirplane:
	protoc --go_out=./backend/pkg/ --go_opt=paths=source_relative \
        --go-grpc_out=./backend/pkg/ --go-grpc_opt=paths=source_relative \
        pb/airplane/airplane.proto
protoStatus:
	protoc --go_out=./backend/pkg/ --go_opt=paths=source_relative \
           --go-grpc_out=./backend/pkg/ --go-grpc_opt=paths=source_relative \
           pb/status/status.proto