import React from 'react';

function Footer({children}) {
    return (
        <footer>
            {children}
            <div className={`${children ? 'pt-4 sm:pt-0 pb-4 sm:pb-16' : 'py-12 md:py-16'} bg-slate-800`}>
                <div className="max-w-5xl mx-auto px-4 sm:px-6">
                    {/* Bottom area */}
                    <div className="md:flex md:items-center md:justify-center">
                        {/* Copyrights note */}
                        <div className="text-gray-400 text-sm text-center"><span>&copy; 2011-2023 All Rights Reserved.</span> <br className='sm:hidden'/><span>Touchlab&reg; is a registered trademark of Touchlab Inc.</span></div>

                    </div>

                </div>
            </div>
        </footer>
    );
}

export default Footer;
