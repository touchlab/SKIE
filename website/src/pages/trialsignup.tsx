import React from 'react';

import TrialSignup from '@site/src/components/TrialSignup';
import HeroTrial from '@site/src/components/HeroTrial';
import Header from '@site/src/components/Header';
import Footer from '@site/src/components/Footer';

export default function TrialsignupPage(): JSX.Element {
    return (
        <div className="tailwind">
            <div>
                <Header rightContent={(scrollPosition, closeMobileMenu) => {
                    const linkClasses = closeMobileMenu ? 'flex text-gray-300 hover:text-gray-200 py-2 hover:no-underline mobile' : 'text-gray-500 hover:text-gray-900 px-4 py-2 flex items-center transition duration-150 ease-in-out hover:no-underline'
                    return (
                        <a href='https://touchlab.co' onClick={closeMobileMenu} target='_blank' className={linkClasses}>
                            About Us
                        </a>
                    )
                }}/>
                <HeroTrial/>
                <TrialSignup/>
                <Footer/>
            </div>
        </div>
    );
}
