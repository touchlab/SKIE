package co.touchlab.skie.configuration.gradle

import co.touchlab.skie.configuration.features.SkieFeature
import co.touchlab.skie.configuration.features.SkieFeatureSet

enum class AnalyticsTier {
    All {

        override fun buildFeatureSet(): SkieFeatureSet =
            SkieFeatureSet(
                SkieFeature.Analytics_Compiler,
                SkieFeature.Analytics_Gradle,
                SkieFeature.Analytics_Hardware,
                SkieFeature.Analytics_GradlePerformance,
                SkieFeature.Analytics_SkiePerformance,
                SkieFeature.Analytics_SkieConfiguration,
                SkieFeature.Analytics_Sysctl,
                SkieFeature.Analytics_OpenSource,
            )
    },

    NoTracking {

        override fun buildFeatureSet(): SkieFeatureSet =
            SkieFeatureSet(
                // WIP
            )
    },

    Anonymous {

        override fun buildFeatureSet(): SkieFeatureSet =
            SkieFeatureSet(
                // WIP
            )
    },

    None {

        override fun buildFeatureSet(): SkieFeatureSet =
            SkieFeatureSet()
    };

    abstract fun buildFeatureSet(): SkieFeatureSet
}
