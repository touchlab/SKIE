import React from 'react';

export default function Automatic() {

    return (
        <section className="max-w-5xl mx-auto px-4 pt-16 pb-16 sm:px-6 border-0 border-solid border-t border-slate-100">
            <div>

                {/* Section header */}
                <div className="max-w-3xl mx-auto text-center pb-8 md:pb-12">
                    <h2 className="h2 mb-4" data-aos="fade-up">Zero-Effort Integration</h2>
                    <p className="text-xl text-gray-700" data-aos="fade-up" data-aos-delay="200">With SKIE, there's no need for additional development effort or training. This powerful tool automatically analyzes your code and modifies the API surface to streamline your workflow. Getting started is as easy as following three simple steps.</p>
                </div>

                {/* Items */}
                <div className="max-w-sm mx-auto grid gap-8 md:grid-cols-3 lg:gap-16 items-start md:max-w-none">

                    {/* 1st item */}
                    <div className="relative flex flex-col items-center" data-aos="fade-up">
                        <div aria-hidden="true" className="absolute h-1 border-0 border-t border-dashed border-gray-300 hidden md:block" style={{ width: 'calc(100% - 32px)', left: 'calc(50% + 48px)', top: '32px' }} data-aos="fade-in" data-aos-delay="200"></div>
                        <svg className="w-16 h-16 mb-4" viewBox="0 0 64 64" xmlns="http://www.w3.org/2000/svg">
                            <rect className="fill-current text-purple-600" width="64" height="64" rx="32" />
                            <path className="stroke-current text-purple-300" strokeWidth="2" strokeLinecap="square" d="M21 23h22v18H21z" fill="none" fillRule="evenodd" />
                            <path className="stroke-current text-purple-100" d="M26 28h12M26 32h12M26 36h5" strokeWidth="2" strokeLinecap="square" />
                        </svg>
                        <h4 className="h4 mb-2"><span className="text-gray-400">1</span>. License</h4>
                        <p className="text-lg text-gray-700 text-center">Apply Touchlab's licensing plugin in your Gradle Settings file and give it your access key.</p>
                    </div>

                    {/* 2nd item */}
                    <div className="relative flex flex-col items-center" data-aos="fade-up" data-aos-delay="200">
                        <div aria-hidden="true" className="absolute h-1 border-0 border-t border-dashed border-gray-300 hidden md:block" style={{ width: 'calc(100% - 32px)', left: 'calc(50% + 48px)', top: '32px' }} data-aos="fade-in" data-aos-delay="400"></div>
                        <svg className="w-16 h-16 mb-4" viewBox="0 0 64 64" xmlns="http://www.w3.org/2000/svg">
                            <rect className="fill-current text-purple-600" width="64" height="64" rx="32" />
                            <g fill="none" fillRule="evenodd">
                                <path className="stroke-current text-purple-300" d="M40 22a2 2 0 012 2v16a2 2 0 01-2 2H24a2 2 0 01-2-2V24a2 2 0 012-2" strokeWidth="2" strokeLinecap="square" />
                                <path className="stroke-current text-purple-100" strokeWidth="2" strokeLinecap="square" d="M36 32l-4-3-4 3V22h8z" />
                            </g>
                        </svg>
                        <h4 className="h4 mb-2"><span className="text-gray-400">2</span>. Plugin</h4>
                        <p className="text-lg text-gray-700 text-center">Add SKIE Gradle plugin to your shared module, that produces a framework for iOS.</p>
                    </div>

                    {/* 3rd item */}
                    <div className="relative flex flex-col items-center" data-aos="fade-up" data-aos-delay="400">
                        <svg className="w-16 h-16 mb-4" viewBox="0 0 64 64" xmlns="http://www.w3.org/2000/svg">
                            <rect className="fill-current text-purple-600" width="64" height="64" rx="32" />
                            <path className="stroke-current text-purple-300" strokeWidth="2" strokeLinecap="square" d="M21 35l4 4 12-15" fill="none" fillRule="evenodd" />
                            <path className="stroke-current text-purple-100" d="M42 29h-3M42 34h-7M42 39H31" strokeWidth="2" strokeLinecap="square" />
                        </svg>
                        <h4 className="h4 mb-2"><span className="text-gray-400">3</span>. Profit</h4>
                        <p className="text-lg text-gray-700 text-center">You and your team are ready to enjoy Swift-friendly and compile-time safe APIs.</p>
                    </div>

                </div>

            </div>
        </section>
    );
}
