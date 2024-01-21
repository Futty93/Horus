// Package msg provides messaging server to client app.
package msg

import (
	"context"
	"flag"
	"fmt"
	"google.golang.org/grpc"
	"log"
	"net"
	"sync"
	aircraftsPb "takahashi.qse.tohoku.ac.jp/atcGameProject/pkg/pb/airplane"
	statusPb "takahashi.qse.tohoku.ac.jp/atcGameProject/pkg/pb/status"
)

var (
	port = flag.Int("port", 50051, "The server port")
)

type msgServer interface {
	Start(context.Context)
}

type GameMessageServer struct {
	s        *grpc.Server
	portNum  string
	isActive bool
}

var messageServerSingletonOnce = sync.OnceValue(func() *GameMessageServer {
	return &GameMessageServer{}
})

func GetGameMessageServerInstance() *GameMessageServer {
	return messageServerSingletonOnce()
}

type server struct {
	statusPb.UnimplementedServerStatusServer
	aircraftsPb.UnimplementedAirplaneServer
}

func (g *GameMessageServer) Start() {
	flag.Parse()
	lis, err := net.Listen("tcp", fmt.Sprintf(":%d", *port))
	if err != nil {
		log.Fatalf("failed to listen: %v", err)
	}
	g.s = grpc.NewServer()
	statusPb.RegisterServerStatusServer(g.s, &server{})
	aircraftsPb.RegisterAirplaneServer(g.s, &server{})
	g.isActive = true
	g.portNum = lis.Addr().String()
	if err := g.s.Serve(lis); err != nil {
		log.Fatalf("failed to serve: %v", err)
	}
}

func (g *GameMessageServer) Stop() {
	g.s.GracefulStop()
	g.isActive = false
}

func (g *GameMessageServer) GetStatusMessage() string {
	var message string
	if g.isActive {
		message += "Server is Running."
		message += fmt.Sprintf("Server is listening at %v", g.portNum)
	} else {
		message = "Server is not running."
	}
	return message
}

func (g *GameMessageServer) IsActive() bool {
	return g.isActive
}
