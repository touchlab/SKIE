import React, { useState, useRef, useEffect } from 'react';
import { Link } from 'react-router-dom';
// import Dropdown from '../utils/Dropdown';

function Header({menuLinkList, children}) {

    const [mobileNavOpen, setMobileNavOpen] = useState(false);

    const trigger = useRef(null);
    const mobileNav = useRef(null);

    // close the mobile menu on click outside
    useEffect(() => {
        const clickHandler = ({ target }) => {
            if (!mobileNav.current || !trigger.current) return;
            if (!mobileNavOpen || mobileNav.current.contains(target) || trigger.current.contains(target)) return;
            setMobileNavOpen(false);
        };
        document.addEventListener('click', clickHandler);
        return () => document.removeEventListener('click', clickHandler);
    });

    // close the mobile menu if the esc key is pressed
    useEffect(() => {
        const keyHandler = ({ keyCode }) => {
            if (!mobileNavOpen || keyCode !== 27) return;
            setMobileNavOpen(false);
        };
        document.addEventListener('keydown', keyHandler);
        return () => document.removeEventListener('keydown', keyHandler);
    });

    const [scrollPosition, setScrollPosition] = useState(0);

    useEffect(() => {
        const updatePosition = () => {
            setScrollPosition(window.pageYOffset)
        }

        window.addEventListener('scroll', updatePosition)

        updatePosition()

        return () => window.removeEventListener('scroll', updatePosition)
    }, []);

    const menuLinks = menuLinkList ? menuLinkList : []

    return (
        <header className={`sticky top-0 w-full z-30 bg-slate-100 ${scrollPosition > 0 ? 'shadow' : 'shadow-none'}`}>
            <div className="max-w-5xl mx-auto px-4 lg:px-0">
                <div className="flex items-center justify-between h-20">

                    {/* Site branding */}
                    <div className="shrink-0 mr-4">
                        {/* Logo */}
                        <Link to="/" className="block h-8" aria-label="Touchlab">
                            <img className="h-8 fill-current" src="img/Touchlab_Gradient.png"/>
                        </Link>
                    </div>

                    {/* Desktop navigation */}
                    <nav className="hidden md:flex md:grow">

                        {/* Desktop menu links */}
                        <ul className="flex grow justify-center flex-wrap items-center list-none m-0">
                            {menuLinks.map(([title, url]) => (
                                <li key={url}>
                                    <Link to={url} className="text-gray-500 hover:text-gray-900 px-4 py-2 flex items-center transition duration-150 ease-in-out hover:no-underline">
                                        {title}
                                    </Link>
                                </li>
                            ))}
                        </ul>

                        {children}

                    </nav>

                    {/* Mobile menu */}
                    <div className="md:hidden">

                        {/* Hamburger button */}
                        <button ref={trigger} className={`hamburger ${mobileNavOpen && 'active'}`} aria-controls="mobile-nav" aria-expanded={mobileNavOpen} onClick={() => setMobileNavOpen(!mobileNavOpen)}>
                            <span className="sr-only">Menu</span>
                            <svg className="w-6 h-6 fill-current text-gray-300 hover:text-gray-200 transition duration-150 ease-in-out" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                                <rect y="4" width="24" height="2" rx="1" />
                                <rect y="11" width="24" height="2" rx="1" />
                                <rect y="18" width="24" height="2" rx="1" />
                            </svg>
                        </button>

                        {/*Mobile navigation */}
                        <nav id="mobile-nav" ref={mobileNav} className="absolute top-full z-20 left-0 w-full px-4 sm:px-6 overflow-hidden transition-all duration-300 ease-in-out" style={mobileNavOpen ? { maxHeight: mobileNav.current.scrollHeight, opacity: 1 } : { maxHeight: 0, opacity: .8 } }>
                            <ul className="bg-gray-800 px-4 py-2">
                                {menuLinks.map(([title, url]) => (
                                    <li key={url}>
                                        <Link to={url} onClick={() => setMobileNavOpen(!mobileNavOpen)} className="flex text-gray-300 hover:text-gray-200 py-2">{title}</Link>
                                    </li>
                                ))}

                                <li> {children}</li>
                            </ul>

                        </nav>

                    </div>

                </div>
            </div>
        </header>
    );
}

export default Header;
