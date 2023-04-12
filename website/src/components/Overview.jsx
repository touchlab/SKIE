import React from 'react';

export default function Overview() {

    return (
        <section>
            <div className="max-w-6xl mx-auto px-4 sm:px-6">
                <div className="pt-10 pb-10 md:pt-16 md:pb-16 border-t border-gray-800">

                    {/* Section header */}
                    <div className="max-w-3xl mx-auto text-center">
                        <h1 className="h2 mb-4">A Swift-friendly API Generator for Kotlin Multiplatform</h1>
                        <p className="text-xl text-gray-700 dark:text-gray-400">Kotlin Multiplatform generates libraries for iOS, but the
                            interface is Objective-C, stripping your code of
                            modern language features. SKIE restores the expressiveness of modern languages and provides a better iOS
                            developer experience.</p>
                    </div>

                </div>
            </div>
        </section>
    );
}
