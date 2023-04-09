import React from "react";

export default function ImageTitleBlock({image, title, children}) {
    return (
        <div className="relative flex flex-col items-center" data-aos="fade-up" data-aos-anchor="[data-aos-id-blocks]">
            {image}
            <h4 className="h4 mb-2">{title}</h4>
            <p className="text-lg text-gray-700 dark:text-gray-400 text-center">{children}</p>
        </div>
    )
}
