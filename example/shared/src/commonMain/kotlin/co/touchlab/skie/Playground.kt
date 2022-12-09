enum class Direction {
    NORTH,
    SOUTH,
    EAST,
    WEST
}

sealed class ViewState {
    class Success(val data: List<Any>) : ViewState()
    object Loading : ViewState()
    class Error(val message: String): ViewState()
}

data class SmithFamilyMember(
    val givenName: String,
    val familyName: String = "Smith",
)