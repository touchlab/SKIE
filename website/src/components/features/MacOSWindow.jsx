import React, {useState} from "react";

export default function MacOSWindow({ tabs }) {
    const [activeTab, setActiveTab] = useState(tabs[0]);

    return (
        <div className="sm:rounded-lg shadow-lg bg-white overflow-hidden relative">
            <div className="flex items-center gap-2 px-2 py-2 bg-gray-100">
                <div className="rounded-full bg-red-500 w-4 h-4"></div>
                <div className="rounded-full bg-yellow-400 w-4 h-4"></div>
                <div className="rounded-full bg-green-500 w-4 h-4"></div>
            </div>
            <div className={`grid grid-flow-col auto-cols-auto`}>
                {tabs.map((tab, index) => {
                    const isActive = tab === activeTab
                    const commonClasses = "font-sans py-1 px-4 text-gray-700 text-base font-medium border-0 before:border-0 before:border-solid before:border-gray-300 relative before:absolute before:inset-0 before:content-['']";
                    const positionClasses = index > 0 ? 'before:border-l' : '';
                    const activeStateClasses = isActive ? 'bg-gray-100 cursor-default' : 'hover:text-gray-900 hover:bg-gray-200 cursor-pointer before:border-t';

                    return <button
                        key={index}
                        className={`${commonClasses} ${positionClasses} ${activeStateClasses}`}
                        onClick={() => setActiveTab(tab)}>
                        <div className="flex justify-center align-middle items-center gap-2.5">
                            <div className="text-xl">{tab.icon}</div><div className={isActive ? 'block' : 'hidden sm:block'}>{tab.title}</div>
                        </div>
                    </button>
                })
                }
            </div>
            <div>
                {tabs.map((tab, index) => {
                    const isActive = tab === activeTab
                    return (
                        <div key={index} className={`${isActive ? '' : 'hidden'} ${tab.background.code} pt-2`}>
                            <div>
                                {tab.content || <img src={tab.contentImage} alt={tab.title}/>}
                            </div>
                        </div>
                    )
                })}
            </div>

            <div className={`absolute bottom-0 left-0 right-0 text-white z-5 px-2 pb-2 text-base sm:text-2xl text-center ${activeTab.background.descriptionGradient}`}>
                {activeTab.description}
            </div>
        </div>
    )
}
