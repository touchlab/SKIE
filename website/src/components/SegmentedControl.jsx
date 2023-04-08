import React, { useState } from 'react';

const SegmentedControl = ({ tabs }) => {
    const [activeTab, setActiveTab] = useState(0);

    return (
        <div className="segmented-control">
            {tabs.map((tab, index) => (
                <button
                    key={index}
                    className={`segmented-control__button ${index === activeTab ? 'active' : ''}`}
                    onClick={() => setActiveTab(index)}
                    aria-label={tab.title}
                >
                    <span className="segmented-control__button-icon">{tab.icon}</span>
                    <span className="segmented-control__button-title">{tab.title}</span>
                </button>
            ))}
            <style jsx>{`
        .segmented-control {
          display: flex;
          justify-content: center;
          align-items: center;
        }

        .segmented-control__button {
          border: none;
          background: none;
          color: #666;
          padding: 10px;
          font-size: 16px;
          cursor: pointer;
          display: flex;
          flex-direction: row;
          justify-content: center;
          align-items: center;
          position: relative;
        }

        .segmented-control__button-icon {
          margin-right: 8px;
        }

        .segmented-control__button-title {
          position: relative;
          top: -2px;
        }

        .segmented-control__button.active {
          color: #fff;
          background-color: #007aff;
        }

        .segmented-control__button.active .segmented-control__button-title {
          display: block;
        }
      `}</style>
        </div>
    );
};

export default SegmentedControl;
