//
//  BreedListView.swift
//  SkieDemo
//
//  Created by Russell Wolf on 7/26/21.
//  Copyright © 2021 Touchlab. All rights reserved.
//

import Combine
import SwiftUI
import shared

private let log = koin.loggerWithTag(tag: "ViewController")

class ObservableBreedModel: ObservableObject {
    private var viewModel: BreedCallbackViewModel?

    @Published
    var state: BreedViewState = BreedViewStateData(breeds: [])

    private var cancellables = [AnyCancellable]()

    func activate() {
        let viewModel = KotlinDependencies.shared.getBreedViewModel()

        doPublish(viewModel.breeds) { [weak self] dogsState in
            self?.state = dogsState
        }.store(in: &cancellables)

        self.viewModel = viewModel
    }

    func deactivate() {
        cancellables.forEach { $0.cancel() }
        cancellables.removeAll()

        viewModel?.clear()
        viewModel = nil
    }

    func onBreedFavorite(_ breed: Breed) {
        viewModel?.updateBreedFavorite(breed: breed)
    }

    func refresh() {
        viewModel?.refreshBreeds()
    }
}

struct BreedListScreen: View {
    @StateObject
    var observableModel = ObservableBreedModel()

    var body: some View {
        BreedListContent(
            breedState: observableModel.state,
            onBreedFavorite: { observableModel.onBreedFavorite($0) },
            refresh: { observableModel.refresh() }
        )
        .onAppear(perform: { observableModel.activate() })
        .onDisappear(perform: { observableModel.deactivate() })
    }
}

struct BreedListContent: View {
    var breedState: BreedViewState
    var onBreedFavorite: (Breed) -> Void
    var refresh: () -> Void

    var body: some View {
        ZStack {
            VStack {
                switch exhaustively(breedState) {
                case .Data(let state):
                    if state.breeds.isEmpty {
                        Text("Empty...")
                    } else {
                        List(state.breeds, id: \.id) { breed in
                            BreedRowView(breed: breed) { onBreedFavorite(breed) }
                        }
                    }
                case .Error(let error):
                    Text(error.type.message)
                        .foregroundColor(.red)

                    Image(systemName: error.type.imageName)
                        .renderingMode(.template)
                        .foregroundColor(.red)

                case .Loading(_):
                    Text("Loading...")
                }

                Button("Refresh") { refresh() }
            }
        }
    }
}

extension BreedErrorType {
    var imageName: String {
        switch exhaustively(self) {
        case .Network: return "wifi.exclamationmark"
        case .Invalid: return "xmark.bin.fill"
        case .Other(_): return "xmark.circle.fill"
        case .RefreshFailure: return "exclamationmark.arrow.triangle.2.circlepath"
        }
    }
}

struct BreedRowView: View {
    var breed: Breed
    var onTap: () -> Void

    var body: some View {
        Button(action: onTap) {
            HStack {
                Text(breed.name)
                    .padding(4.0)
                Spacer()
                Image(systemName: breed.favorite ? "heart.fill" : "heart")
                    .padding(4.0)
            }
        }
    }
}

struct BreedListScreen_Previews: PreviewProvider {
    static var previews: some View {
        let state = BreedViewStateData(breeds: [
            Breed(id: 0, name: "appenzeller", favorite: false),
            Breed(id: 1, name: "australian", favorite: true)
        ])

        BreedListContent(
            breedState: state,
            onBreedFavorite: { _ in },
            refresh: {}
        )
    }
}
