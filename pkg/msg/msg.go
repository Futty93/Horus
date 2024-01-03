// Package msg provides messaging server to client app.
package msg

import (
	"flag"
	"fmt"
	"google.golang.org/grpc"
	"log"
	"net"
	aircraftsPb "takahashi.qse.tohoku.ac.jp/atcGameProject/pb/airplane"
	statusPb "takahashi.qse.tohoku.ac.jp/atcGameProject/pb/status"
)

// Messenger is server.
type Messenger struct {
}

var (
	port = flag.Int("port", 50051, "The server port")
)

// server is used to implement status.GreeterServer.
type server struct {
	statusPb.UnimplementedServerStatusServer
	aircraftsPb.UnimplementedAirplaneServer
}

func Start() {
	flag.Parse()
	lis, err := net.Listen("tcp", fmt.Sprintf(":%d", *port))
	if err != nil {
		log.Fatalf("failed to listen: %v", err)
	}
	s := grpc.NewServer()
	statusPb.RegisterServerStatusServer(s, &server{})
	aircraftsPb.RegisterAirplaneServer(s, &server{})
	log.Printf("server listening at %v", lis.Addr())
	if err := s.Serve(lis); err != nil {
		log.Fatalf("failed to serve: %v", err)
	}
}

//
