import React, {useEffect} from 'react';
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
import {HtmlClassNameProvider} from '@docusaurus/theme-common';
import {useColorMode} from '@docusaurus/theme-common';

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

    // const {colorMode} = useColorMode();

    return (
        <div>
            <Header/>
            <HeroAbout/>
            <FeaturesZigzag/>
            <Automatic/>
            <FeaturesBlocks/>
            <Roadmap/>
            <StakeholderValue/>
            <TrustBlocks/>
            <Footer/>
        </div>
    );
}
