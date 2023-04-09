import React from 'react';

import HeroThanks from '@site/src/components/HeroThanks';
import Header from '@site/src/components/Header';
import Footer from '@site/src/components/Footer';

export default function TrialsignupPage(): JSX.Element {
    return (
        <div className="tailwind">
            <div>
                <Header/>
                <HeroThanks/>
                <Footer/>
            </div>
        </div>
    );
}
