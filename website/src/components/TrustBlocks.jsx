import React from 'react';
import {Geometry, Link, NetworkConnection, PillBottle} from "./FeatureIcons";

function TrustBlock(name, description, svgBody) {
    return (
        <div className="relative flex flex-col items-center" data-aos="fade-up" data-aos-anchor="[data-aos-id-blocks]">
            {svgBody()}
            <h4 className="h4 mb-2">{name}</h4>
            <p className="text-lg text-gray-700 dark:text-gray-400 text-center">{description}</p>
        </div>
    )
}

export default function TrustBlocks() {
    return (
        <section className="bg-slate-50">

            <div className="max-w-5xl mx-auto px-4 sm:px-6 py-16 sm:px-6 border-0 border-solid border-t border-slate-100">
                <div className="border-t border-gray-800">
                    {/* Section header */}
                    <div className="max-w-3xl mx-auto text-center">
                        <h2 className="h2 mb-4">__ Confidence/trust copy __</h2>
                    </div>
                </div>

                <div className="border-t border-gray-800 pt-4">

                    {/* Items */}
                    <div
                        className="max-w-sm mx-auto grid gap-8 md:grid-cols-3 lg:gap-16 items-start md:max-w-2xl lg:max-w-none"
                        data-aos-id-blocks>

                        {TrustBlock(
                            "Comprehensive Testing",
                            <>We test SKIE against a <strong>thousand</strong> public KMM libraries along with <strong>hundreds</strong> of hand-written tests.<br/><br/>(TODO: 1000 or thousand, 100s or hundreds?)</>,
                            Link
                        )}

                        {TrustBlock(
                            "No Vendor Lock-in",
                            "Flexibility and _risk aversion_ is paramount to us, we gotchu fam. TODO: We care about this too, we built it in from day one.",
                            Geometry
                        )}

                        {TrustBlock(
                            "Made by Touchlab",
                            "Touchlab has years of experience with Kotlin Multiplatform in production. We built SKIE to solve our problems, let it solve yours too.",
                            Geometry
                        )}

                    </div>
                </div>
            </div>
        </section>
    );
}
