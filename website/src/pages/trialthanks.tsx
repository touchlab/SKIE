import React from 'react';

import HeroThanks from '@site/src/components/HeroThanks';
import Header from '@site/src/components/Header';
import FooterBlank from '@site/src/components/FooterBlank';

export default function TrialsignupPage(): JSX.Element {
    return (
        <div className="tailwind">
            <div>
                <Header/>
                <HeroThanks/>
                <FooterBlank/>
            </div>
        </div>
    );
}
