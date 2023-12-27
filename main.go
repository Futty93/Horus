package main

import (
    "fmt"
    "net/http"
)

func main() {
    // ハンドラ関数を作成
    handler := func(w http.ResponseWriter, r *http.Request) {
        fmt.Fprintf(w, "Hello, this is your Go server!")
    }

    // ハンドラを指定してサーバーを起動
    http.HandleFunc("/", handler)
    fmt.Println("Server is listening on port 8080...")
    err := http.ListenAndServe(":8080", nil)
    if err != nil {
        fmt.Println("Server couldn't start:", err)
    }
}
