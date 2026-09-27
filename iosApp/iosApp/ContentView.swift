import SwiftUI

struct ContentView: View {
    var body: some View {
        ZStack {
            Color(red: 7/255, green: 11/255, blue: 18/255)
                .ignoresSafeArea()
            VStack(spacing: 16) {
                Image(systemName: "indianrupeesign.circle.fill")
                    .resizable()
                    .frame(width: 72, height: 72)
                    .foregroundColor(Color(red: 16/255, green: 185/255, blue: 129/255))
                Text("RupeeMath")
                    .font(.system(size: 28, weight: .bold))
                    .foregroundColor(.white)
                Text("Smart Digital Ledger & EMI Manager")
                    .font(.system(size: 14))
                    .foregroundColor(.gray)
            }
        }
    }
}
