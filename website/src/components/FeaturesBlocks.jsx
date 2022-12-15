import React from 'react';
import {Geometry, Link, NetworkConnection, PillBottle} from "./FeatureIcons";

function FeatureBlock(name, description, svgBody) {
    return (
        <div className="relative flex flex-col items-center" data-aos="fade-up" data-aos-anchor="[data-aos-id-blocks]">
            {svgBody()}
            <h4 className="h4 mb-2">{name}</h4>
            <p className="text-lg text-gray-700 dark:text-gray-400 text-center">{description}</p>
        </div>
    )
}

function FeaturesBlocks() {
    return (
        <section>

            <div className="max-w-6xl mx-auto px-4 sm:px-6">
                <div className="border-t border-gray-800">
                    {/* Section header */}
                    <div className="max-w-3xl mx-auto text-center">
                        <h2 className="h2 mb-4">Critical features for the KMM iOS developer experience.</h2>
                    </div>
                </div>

                <div className="pb-12 md:pb-20 border-t border-gray-800 pt-4">

                    {/* Items */}
                    <div
                        className="max-w-sm mx-auto grid gap-8 md:grid-cols-2 lg:grid-cols-4 lg:gap-16 items-start md:max-w-2xl lg:max-w-none"
                        data-aos-id-blocks>

                        {FeatureBlock(
                            "Transparent Enums",
                            "Kotlin enums are transparently converted to proper Swift enums. This allows for exhaustive operations on the Swift side.",
                            Geometry
                        )}

                        {FeatureBlock(
                            "Sealed Classes",
                            "Sealed Class handling allows you to exhaustively switch on sealed Kotlin hierarchies from Swift.",
                            PillBottle
                        )}

                        {FeatureBlock(
                            "Default Parameters",
                            "Overloaded methods are added to the exposed Swift interface to allow for calling methods without needing to specify every argument.",
                            NetworkConnection
                        )}

                        {FeatureBlock(
                            "Direct Linking",
                            "The generated Swift and augmented interface are compiled and linked directly to the Xcode Framework. No extra deployment config required.",
                            Link
                        )}

                    </div>
                </div>
            </div>
        </section>
    );
}

export default FeaturesBlocks;
