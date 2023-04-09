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
                            Our existing feature-set is merely scratching the surface of what’s possible. As we continuously explore new ideas and possibilities, we invite you to join us on our journey and take part in shaping our future direction.
                        </p>
                        <a className="btn text-white bg-cyan-600 hover:bg-cyan-700 sm:w-auto drop-shadow-lg no-underline"
                           href="#">Become a Customer</a>
                    </div>

                </div>
            </div>
        </section>
    );
}
