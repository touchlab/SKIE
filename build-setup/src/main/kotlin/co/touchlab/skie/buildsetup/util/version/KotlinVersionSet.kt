package co.touchlab.skie.buildsetup.util.version

import java.nio.file.Path
import kotlin.io.path.name

sealed interface KotlinVersionSet {

    val path: Path

    fun isActive(version: KotlinToolingVersion): Boolean

    fun isValid(version: kotlin.collections.List<KotlinToolingVersion>): Boolean

    data class Exact(override val path: Path, val version: KotlinToolingVersion) : KotlinVersionSet {

        override fun isActive(version: KotlinToolingVersion): Boolean =
            this.version == version

        override fun isValid(version: kotlin.collections.List<KotlinToolingVersion>): Boolean =
            this.version in version
    }

    data class Range(override val path: Path, val from: KotlinToolingVersion, val to: KotlinToolingVersion) : KotlinVersionSet {

        override fun isActive(version: KotlinToolingVersion): Boolean =
            version in from..to

        override fun isValid(version: kotlin.collections.List<KotlinToolingVersion>): Boolean =
            from in version && to in version
    }

    data class Min(override val path: Path, val from: KotlinToolingVersion) : KotlinVersionSet {

        override fun isActive(version: KotlinToolingVersion): Boolean =
            version >= from

        override fun isValid(version: kotlin.collections.List<KotlinToolingVersion>): Boolean =
            from in version
    }

    data class Max(override val path: Path, val to: KotlinToolingVersion) : KotlinVersionSet {

        override fun isActive(version: KotlinToolingVersion): Boolean =
            version <= to

        override fun isValid(version: kotlin.collections.List<KotlinToolingVersion>): Boolean =
            to in version
    }

    data class List(override val path: Path, val versions: kotlin.collections.List<KotlinToolingVersion>) : KotlinVersionSet {

        override fun isActive(version: KotlinToolingVersion): Boolean =
            version in versions

        override fun isValid(version: kotlin.collections.List<KotlinToolingVersion>): Boolean =
            versions.all { it in version }
    }

    companion object {

        // 2.0.0 - exact
        // 2.0.0..2.0.10 - range
        // 2.0.10.. - min
        // ..2.0.10 - max
        // 2.0.10,2.0.20 - list
        fun from(path: Path): KotlinVersionSet = when {
            path.name.contains("..") -> {
                val parts = path.name.split("..")

                check(parts.size == 2) { "Version file name must contain at most one range. Was: '${path.name}'" }

                val (from, to) = parts

                when {
                    from.isEmpty() -> Max(
                        path = path,
                        to = KotlinToolingVersion(to),
                    )
                    to.isEmpty() -> Min(
                        path = path,
                        from = KotlinToolingVersion(from),
                    )
                    else -> Range(
                        path = path,
                        from = KotlinToolingVersion(from),
                        to = KotlinToolingVersion(to),
                    )
                }
            }
            path.name.contains(",") -> List(
                path = path,
                versions = path.name.split(",")
                    .map { KotlinToolingVersion(it) },
            )
            else -> Exact(
                path = path,
                version = KotlinToolingVersion(path.name),
            )
        }
    }
}
