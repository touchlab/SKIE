package co.touchlab.skie.plugin.generator.internal.util

import io.outfoxx.swiftpoet.ARRAY
import io.outfoxx.swiftpoet.BOOL
import io.outfoxx.swiftpoet.DOUBLE
import io.outfoxx.swiftpoet.DeclaredTypeName
import io.outfoxx.swiftpoet.FLOAT32
import io.outfoxx.swiftpoet.FLOAT64
import io.outfoxx.swiftpoet.INT
import io.outfoxx.swiftpoet.INT16
import io.outfoxx.swiftpoet.INT32
import io.outfoxx.swiftpoet.INT64
import io.outfoxx.swiftpoet.INT8
import io.outfoxx.swiftpoet.STRING
import io.outfoxx.swiftpoet.TypeName
import io.outfoxx.swiftpoet.UIN16
import io.outfoxx.swiftpoet.UINT32
import io.outfoxx.swiftpoet.UINT64
import io.outfoxx.swiftpoet.UINT8
import io.outfoxx.swiftpoet.parameterizedBy

object SwiftType {

    val bool = BOOL
    val int8 = INT8
    val uint8 = UINT8
    val int16 = INT16
    val uint16 = UIN16
    val int32 = INT32
    val uint32 = UINT32
    val int64 = INT64
    val uint64 = UINT64
    val int = INT
    val uint = DeclaredTypeName.typeName("Swift.UInt")
    val float32 = FLOAT32
    val float64 = FLOAT64
    val double = DOUBLE
    val character = DeclaredTypeName.typeName("Swift.Character")
    val unicodeScalar = DeclaredTypeName.typeName("Swift.UnicodeScalar")

    val string = STRING
    val nsString = DeclaredTypeName.typeName("Foundation.NSString")

    val nsMutableArray = DeclaredTypeName.typeName("Foundation.NSMutableArray")

    val array = ARRAY
    fun arrayOf(elementType: TypeName) = array.parameterizedBy(elementType)
}
