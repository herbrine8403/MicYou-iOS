import SwiftUI

struct ContentView: View {
    @State private var status = "Idle"
    @State private var host = "127.0.0.1"
    @State private var port = "5000"

    var body: some View {
        VStack(spacing: 16) {
            Text("MicYou iOS")
                .font(.largeTitle)
                .bold()

            TextField("Host", text: $host)
                .textFieldStyle(.roundedBorder)
                .padding(.horizontal)

            TextField("Port", text: $port)
                .textFieldStyle(.roundedBorder)
                .padding(.horizontal)

            Button("Prepare Audio") {
                status = "Audio prepared"
            }

            Button("Connect") {
                status = "Connecting to \(host):\(port)"
            }

            Button("Start Capture") {
                status = "Capture started"
            }

            Button("Disconnect") {
                status = "Disconnected"
            }

            Text(status)
                .padding(.top, 8)
        }
        .padding()
    }
}
