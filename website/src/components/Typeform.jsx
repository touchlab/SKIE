import React from 'react';
import {Widget} from "@typeform/embed-react";
import SectionHeader from "./base/SectionHeader";

export default function Typeform() {
    return (
        <div
            id="demo"
            className="bg-slate-800 pt-8 md:pt-16"
        >
            <SectionHeader title="Book a Free Demo" textColor='text-white' />
            <Widget
                id="uOxsjmLK"
                hideHeaders={true}
                opacity={0}
                disableScroll={true}
                height={510}
                inlineOnMobile={true}
                iframeProps={{style: "border-radius: 0 !important"}}
            />
        </div>
    )
}
