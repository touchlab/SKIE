import React from 'react';
import {Widget} from "@typeform/embed-react";

export default function Typeform() {
    return (
        <div
            id="demo"
            className="bg-slate-800 pt-16"
        >
            <div className="max-w-3xl mx-auto text-center">
                <h2 className="h2 text-white">Book a Free Demo</h2>
            </div>
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
