package `tests`.`bugs`.`transitively_exposed_renamed_method`

class A : Appendable {

    override fun append(value: Char): Appendable {
        return this
    }

    override fun append(value: CharSequence?): Appendable {
        return this
    }

    override fun append(value: CharSequence?, startIndex: Int, endIndex: Int): Appendable {
        return this
    }
}
