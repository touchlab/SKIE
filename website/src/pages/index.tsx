import React, {useEffect} from 'react';
import Layout from '@theme/Layout';
import useDocusaurusContext from '@docusaurus/useDocusaurusContext';
import Process from '@site/src/components/Process';
import HeroAbout from '@site/src/components/HeroAbout';
import Stats from '@site/src/components/Stats';
import FeaturesBlocks from '@site/src/components/FeaturesBlocks';
import FeaturesZigzag from '@site/src/components/FeaturesZigzag';
import HeroHome from '@site/src/components/HeroHome';
import Newsletter from '@site/src/components/Newsletter';
import NewsletterDoc from '@site/src/components/NewsletterDoc';
import StakeholderValue from '@site/src/components/StakeholderValue';
import Overview from '@site/src/components/Overview';
import Automatic from '@site/src/components/Automatic';
import AOS from 'aos';
// import 'aos/dist/aos.css';

export default function Home(): JSX.Element {
  const {siteConfig} = useDocusaurusContext();

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
      {/*<HeroHome />*/}
      {/*<Process/>*/}
      {/*<Stats/>*/}
      <Overview/>
      <StakeholderValue/>
      <Automatic/>
      {/*<NewsletterDoc/>*/}
      {/*<FeaturesZigzag/>*/}
      <FeaturesBlocks/>
      <Newsletter/>
      <div>&nbsp;</div>
      <div>&nbsp;</div>
    </Layout>
  );
}
