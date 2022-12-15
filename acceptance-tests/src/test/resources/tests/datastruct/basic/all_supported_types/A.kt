package `tests`.`datastruct`.`basic`.`all_supported_types`

data class A(
    val boolean: Boolean,
    val byte: Byte,
    val short: Short,
    val int: Int,
    val long: Long,
    val float: Float,
    val double: Double,
    val string: String,
    val booleanArray: BooleanArray,
    val byteArray: ByteArray,
    val shortArray: ShortArray,
    val intArray: IntArray,
    val longArray: LongArray,
    val floatArray: FloatArray,
    val doubleArray: DoubleArray,
    val stringArray: Array<String>,
    val booleanList: List<Boolean>,
    val byteList: List<Byte>,
    val shortList: List<Short>,
    val intList: List<Int>,
    val longList: List<Long>,
    val floatList: List<Float>,
    val doubleList: List<Double>,
    val stringList: List<String>,
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (this::class != other?.let { it::class }) return false

        other as A

        if (boolean != other.boolean) return false
        if (byte != other.byte) return false
        if (short != other.short) return false
        if (int != other.int) return false
        if (long != other.long) return false
        if (float != other.float) return false
        if (double != other.double) return false
        if (string != other.string) return false
        if (!booleanArray.contentEquals(other.booleanArray)) return false
        if (!byteArray.contentEquals(other.byteArray)) return false
        if (!shortArray.contentEquals(other.shortArray)) return false
        if (!intArray.contentEquals(other.intArray)) return false
        if (!longArray.contentEquals(other.longArray)) return false
        if (!floatArray.contentEquals(other.floatArray)) return false
        if (!doubleArray.contentEquals(other.doubleArray)) return false
        if (!stringArray.contentEquals(other.stringArray)) return false
        if (booleanList != other.booleanList) return false
        if (byteList != other.byteList) return false
        if (shortList != other.shortList) return false
        if (intList != other.intList) return false
        if (longList != other.longList) return false
        if (floatList != other.floatList) return false
        if (doubleList != other.doubleList) return false
        if (stringList != other.stringList) return false

        return true
    }

    override fun hashCode(): Int {
        var result = boolean.hashCode()
        result = 31 * result + byte
        result = 31 * result + short
        result = 31 * result + int
        result = 31 * result + long.hashCode()
        result = 31 * result + float.hashCode()
        result = 31 * result + double.hashCode()
        result = 31 * result + string.hashCode()
        result = 31 * result + booleanArray.contentHashCode()
        result = 31 * result + byteArray.contentHashCode()
        result = 31 * result + shortArray.contentHashCode()
        result = 31 * result + intArray.contentHashCode()
        result = 31 * result + longArray.contentHashCode()
        result = 31 * result + floatArray.contentHashCode()
        result = 31 * result + doubleArray.contentHashCode()
        result = 31 * result + stringArray.contentHashCode()
        result = 31 * result + booleanList.hashCode()
        result = 31 * result + byteList.hashCode()
        result = 31 * result + shortList.hashCode()
        result = 31 * result + intList.hashCode()
        result = 31 * result + longList.hashCode()
        result = 31 * result + floatList.hashCode()
        result = 31 * result + doubleList.hashCode()
        result = 31 * result + stringList.hashCode()
        return result
    }
}
