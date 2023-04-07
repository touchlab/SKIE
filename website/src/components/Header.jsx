import React, { useState, useRef, useEffect } from 'react';
import { Link } from 'react-router-dom';
// import Dropdown from '../utils/Dropdown';

function Header() {

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
                            <li>
                                <Link to="/" className="text-gray-500 hover:text-gray-900 px-4 py-2 flex items-center transition duration-150 ease-in-out hover:no-underline">
                                    Home
                                </Link>
                            </li>
                            <li>
                                <Link to="#features" className="text-gray-500 hover:text-gray-900 px-4 py-2 flex items-center transition duration-150 ease-in-out hover:no-underline">
                                    Features
                                </Link>
                            </li>
                            <li>
                                <Link to="#why-skie" className="text-gray-500 hover:text-gray-900 px-4 py-2 flex items-center transition duration-150 ease-in-out hover:no-underline">
                                    Why
                                </Link>
                            </li>
                            <li>
                                <Link to={{ pathname: "https://www.touchlab.co" }} target="_blank" className="text-gray-500 hover:text-gray-900 px-4 py-2 flex items-center transition duration-150 ease-in-out hover:no-underline">
                                    About Us
                                </Link>
                            </li>
                            <li>
                                <Link to="/intro" className="text-gray-500 hover:text-gray-900 px-4 py-2 flex items-center transition duration-150 ease-in-out hover:no-underline">
                                    Docs
                                </Link>
                            </li>
                            {/* 1st level: hover */}
                            {/*<Dropdown title="Support">*/}
                            {/*    /!* 2nd level: hover *!/*/}
                            {/*    <li>*/}
                            {/*        <Link to="/contact" className="font-medium text-sm text-gray-400 hover:text-purple-600 flex py-2 px-4 leading-tight">Contact us</Link>*/}
                            {/*    </li>*/}
                            {/*    <li>*/}
                            {/*        <Link to="/help" className="font-medium text-sm text-gray-400 hover:text-purple-600 flex py-2 px-4 leading-tight">Help center</Link>*/}
                            {/*    </li>*/}
                            {/*    <li>*/}
                            {/*        <Link to="/404" className="font-medium text-sm text-gray-400 hover:text-purple-600 flex py-2 px-4 leading-tight">404</Link>*/}
                            {/*    </li>*/}
                            {/*</Dropdown>*/}
                        </ul>

                        {/* Desktop sign in links */}
                        <div className="flex grow justify-end flex-wrap items-center">
                            <Link to="/signup" className="btn-sm text-white bg-purple-600 hover:bg-purple-700 ml-3">Book a Demo</Link>
                        </div>

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
                                <li>
                                    <Link to="/features" className="flex text-gray-300 hover:text-gray-200 py-2">Features</Link>
                                </li>
                                <li>
                                    <Link to="/pricing" className="flex text-gray-300 hover:text-gray-200 py-2">Pricing</Link>
                                </li>
                                <li>
                                    <Link to="/blog" className="flex text-gray-300 hover:text-gray-200 py-2">Blog</Link>
                                </li>
                                <li>
                                    <Link to="/about" className="flex text-gray-300 hover:text-gray-200 py-2">About us</Link>
                                </li>
                                <li className="py-2 my-2 border-t border-b border-gray-700">
                                    <span className="flex text-gray-300 py-2">Support</span>
                                    <ul className="pl-4">
                                        <li>
                                            <Link to="/contact" className="text-sm flex font-medium text-gray-400 hover:text-gray-200 py-2">Contact us</Link>
                                        </li>
                                        <li>
                                            <Link to="/help" className="text-sm flex font-medium text-gray-400 hover:text-gray-200 py-2">Help center</Link>
                                        </li>
                                        <li>
                                            <Link to="/404" className="text-sm flex font-medium text-gray-400 hover:text-gray-200 py-2">404</Link>
                                        </li>
                                    </ul>
                                </li>
                                <li>
                                    <Link to="/signin" className="flex font-medium w-full text-purple-600 hover:text-gray-200 py-2 justify-center">Sign in</Link>
                                </li>
                                <li>
                                    <Link to="/signup" className="font-medium w-full inline-flex items-center justify-center border border-transparent px-4 py-2 my-2 rounded-sm text-white bg-purple-600 hover:bg-purple-700 transition duration-150 ease-in-out">Sign up</Link>
                                </li>
                            </ul>
                        </nav>

                    </div>

                </div>
            </div>
        </header>
    );
}

export default Header;
