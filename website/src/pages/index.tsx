import React from 'react';
import Layout from '@theme/Layout';
import HeroAbout from '@site/src/components/HeroAbout';
import FeaturesBlocks from '@site/src/components/FeaturesBlocks';
import FeaturesZigzag from '@site/src/components/FeaturesZigzag';
import EarlyAccess from '@site/src/components/EarlyAccess';
import StakeholderValue from '@site/src/components/StakeholderValue';
import Overview from '@site/src/components/Overview';
import Automatic from '@site/src/components/Automatic';
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
        <Layout>
            <HeroAbout/>
            <Overview/>
            <StakeholderValue/>
            <Automatic/>
            <FeaturesZigzag/>
            <FeaturesBlocks/>
            <EarlyAccess/>
            <div>&nbsp;</div>
            <div>&nbsp;</div>
        </Layout>
    );
}
