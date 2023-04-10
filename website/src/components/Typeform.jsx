import React from 'react';
import {Widget, PopupButton} from "@typeform/embed-react";
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

export function TypeformPopupButton({children}) {
    return (
        <PopupButton
            id="uOxsjmLK"
            className="btn text-teal-600 bg-teal-100 hover:bg-white shadow text-xl font-sans font-medium cursor-pointer"
        >
            {children}
        </PopupButton>
    )
}
