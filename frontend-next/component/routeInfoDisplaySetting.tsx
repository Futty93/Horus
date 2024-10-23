"use client"
import { useRouteInfoDisplaySetting, RouteInfoDisplaySettingContextType, DisplaySettings } from '@/context/routeInfoDisplaySettingContext';
import { useState, useContext } from 'react';

const RouteInfoDisplaySetting = () => {
  const { isDisplaying, setRouteInfoDisplaySetting } = useRouteInfoDisplaySetting();

  const handleCheckboxChange = (settingKey: keyof DisplaySettings) => {
    return (event: React.ChangeEvent<HTMLInputElement>) => {
      const isChecked = event.target.checked;
      setRouteInfoDisplaySetting((prevSettings) => ({
        ...prevSettings,
        isDisplaying: {
          ...prevSettings.isDisplaying,
          [settingKey]: isChecked, // isDisplayingの特定の設定を更新
        },
      }));
      console.log(`${settingKey} setting changed to:`, isChecked);
    };
  };

  return (
    <div id="routeInfoDisplaySetting" className="flex flex-col mb-5">
      {['Waypoint', 'Radio Navigation AIDs'].map((label) => (
        <div key={label} className="mb-2">
          <span className="font-bold text-green-400">{label}</span>
          <div className="flex flex-col ml-5">
            <label className="flex items-center">
              <input
                type="checkbox"
                checked={label === 'Waypoint' ? isDisplaying.waypointName : isDisplaying.radioNavigationAidsName}
                onChange={handleCheckboxChange(label === 'Waypoint' ? 'waypointName' : 'radioNavigationAidsName')}
                className="mr-2"
              />
              <span>Name</span>
            </label>
            <label className="flex items-center">
              <input
                type="checkbox"
                checked={label === 'Waypoint' ? isDisplaying.waypointPoint : isDisplaying.radioNavigationAidsPoint}
                onChange={handleCheckboxChange(label === 'Waypoint' ? 'waypointPoint' : 'radioNavigationAidsPoint')}
                className="mr-2"
              />
              <span>Point</span>
            </label>
          </div>
        </div>
      ))}
      <label className="flex items-center mb-2">
        <input
          type="checkbox"
          checked={isDisplaying.atsLowerRoute}
          onChange={handleCheckboxChange('atsLowerRoute')}
          className="mr-2"
        />
        <span>ATS Lower Route</span>
      </label>
      <label className="flex items-center mb-2">
        <input
          type="checkbox"
          checked={isDisplaying.rnavRoute}
          onChange={handleCheckboxChange('rnavRoute')}
          className="mr-2"
        />
        <span>RNAV Route</span>
      </label>
    </div>
  );
};

export default RouteInfoDisplaySetting;