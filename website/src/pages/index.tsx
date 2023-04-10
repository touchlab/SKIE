import React, {useEffect, useState} from 'react';
import Layout from '@theme/Layout';
import HeroAbout from '@site/src/components/HeroAbout';
import FeaturesBlocks from '@site/src/components/FeaturesBlocks';
import TrustBlocks from '@site/src/components/TrustBlocks';
import FeaturesZigzag from '@site/src/components/FeaturesZigzag';
import EarlyAccess from '@site/src/components/EarlyAccess';
import StakeholderValue from '@site/src/components/StakeholderValue';
import Overview from '@site/src/components/Overview';
import Automatic from '@site/src/components/Automatic';
import Roadmap from '@site/src/components/Roadmap';
import Header from '@site/src/components/Header';
import Footer from '@site/src/components/Footer';
import Typeform from '@site/src/components/Typeform';
import {HtmlClassNameProvider} from '@docusaurus/theme-common';
import {useColorMode} from '@docusaurus/theme-common';
import {Link} from "react-router-dom";

// Uncomment for animations
// import AOS from 'aos';

// Uncomment for animations
// import 'aos/dist/aos.css';

export default function Home(): JSX.Element {
    // Uncomment for animations
    /*useEffect(() => {
      AOS.init({
        once: true,
        disable: 'phone',
        duration: 350,
        easing: 'ease-out-sine',
      });
    });*/


    return (
        <div className="tailwind">
            <TailwindPage/>
        </div>
    );
}

function TailwindPage(): JSX.Element {

    const menuLinks = [
        ['Home', '/'],
        ['Features', '#features'],
        ['Why', '#why-skie']
    ];

    return (
        <div>
            <Header
                menuLinkList={menuLinks}
                rightContent={(scrollPosition, closeMobileMenu) => (
                    <div className="flex grow items-center">
                        <Link to="#demo" onClick={closeMobileMenu} className={`btn-sm grow md:grow-0 font-semibold text-lg text-gray-700 bg-amber-300 hover:bg-amber-200 hover:md:bg-amber-400 no-underline ${scrollPosition > 0 ? '' : 'md:text-cyan-600 hover:md:text-cyan-800 md:bg-transparent hover:md:bg-transparent'}`}>
                            Book a Demo
                        </Link>
                    </div>
                )}
            />
            <HeroAbout/>
            <FeaturesZigzag/>
            <Automatic/>
            <FeaturesBlocks/>
            <Roadmap/>
            <StakeholderValue/>
            <TrustBlocks/>
            <Footer>
                <Typeform/>
            </Footer>
        </div>
    );
}
