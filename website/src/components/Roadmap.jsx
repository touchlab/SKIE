import React from 'react';

export default function Roadmap() {

    return (
        <section className="max-w-5xl mx-auto px-4 sm:px-6 py-16 sm:px-6 border-0 border-solid border-t border-slate-100">
            <div>
                <div>

                    {/* Section header */}
                    <div className="max-w-3xl mx-auto text-center">
                        <h1 className="h2 mb-4">There's more!</h1>
                        <p className="text-xl text-gray-700 dark:text-gray-400">
                            The current feature-set is only the beginning. But we need your feedback, become a customer now and tells us what ails you the most.
                        </p>
                        <p className="text-xl text-gray-700 dark:text-gray-400">
                            Are we missing a feature that's crucial for you? Let us know, we can chat.
                        </p>
                        <a className="btn text-white bg-cyan-600 hover:bg-cyan-700 sm:w-auto drop-shadow-lg no-underline"
                           href="#">I wish SKIE could &hellip;</a>
                    </div>

                </div>
            </div>
        </section>
    );
}
