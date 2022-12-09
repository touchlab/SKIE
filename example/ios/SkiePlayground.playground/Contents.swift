import shared

func goIn(direction: Direction) {
    switch direction {
    case .north: print("Go north")
    case .south: print("Go south")
    case .east: print("Go east")
    case .west: print("Go west")
    }
}

func configureView(for state: ViewState) {
    switch onEnum(of: state) {
    case .Success(let success):
        configureView(forData: success.data)
    case .Error(let error):
        configureView(forErrorMessage: error.message)
    case .Loading:
        configureViewForLoading()
    }
}

func configureView(forData data: [Any]) { }
func configureView(forErrorMessage message: String) { }
func configureViewForLoading() { }
