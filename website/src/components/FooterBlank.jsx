import React from 'react';
import { Link } from 'react-router-dom';
import {Widget} from "@typeform/embed-react";

function Footer() {
    return (
        <footer id="demo">
            <div className="pb-12 md:pb-16 bg-slate-800">
                <div className="max-w-5xl mx-auto px-4 sm:px-6">
                    {/* Bottom area */}
                    <div className="md:flex md:items-center md:justify-between">
                        {/* Copyrights note */}
                        <div className="text-gray-400 text-sm mr-4">&copy; 2011-2023 All Rights Reserved. Touchlab&reg; is a registered trademark of Touchlab Inc.</div>

                    </div>

                </div>
            </div>
        </footer>
    );
}

export default Footer;
