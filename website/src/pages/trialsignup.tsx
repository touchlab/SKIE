import React from 'react';

import TrialSignup from '@site/src/components/TrialSignup';
import HeroTrial from '@site/src/components/HeroTrial';
import Header from '@site/src/components/Header';
import Footer from '@site/src/components/Footer';

export default function TrialsignupPage(): JSX.Element {
    return (
        <div className="tailwind">
            <div>
                <Header/>
                <HeroTrial/>
                <TrialSignup/>
                <Footer/>
            </div>
        </div>
    );
}
