import React from 'react';
import Subsection from "./base/Subsection";
import SectionHeader from "./base/SectionHeader";
import CenteringSection from "./base/CenteringSection";

export default function Roadmap() {

    return (
        <CenteringSection divider={true}>
            <SectionHeader title="There's more!" includeMargin={false}>
                Our existing feature-set is merely scratching the surface of what’s possible. As we continuously explore new ideas and possibilities, we invite you to join us on our journey and take part in shaping our future direction.
            </SectionHeader>
        </CenteringSection>
    );
}
