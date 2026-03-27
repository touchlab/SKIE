package `tests`.`bugs`.`special_characters_in_deprecation_message`

@Deprecated("line comment // text")
fun foo1() {
}

@Deprecated("block comment start /* text")
fun foo2() {
}

@Deprecated("Quote \" text")
fun foo3() {
}

@Deprecated("New line \n text")
fun foo4() {
}

@Deprecated(
    """
   Multi line
   text
""",
)
fun foo5() {
}
