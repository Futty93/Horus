"use client"
import { useRouteInfoDisplaySetting, DisplaySettings } from '@/context/routeInfoDisplaySettingContext';

const RouteInfoDisplaySetting = () => {
  const { isDisplaying, setRouteInfoDisplaySetting } = useRouteInfoDisplaySetting();

  const handleCheckboxChange = (settingKey: keyof DisplaySettings) => {
    return (event: React.ChangeEvent<HTMLInputElement>) => {
      const isChecked = event.target.checked;
      setRouteInfoDisplaySetting((prevSettings) => ({
        ...prevSettings,
        [settingKey]: isChecked, // ここで特定の設定を直接更新
      }));
    };
  };

  return (
    <div id="routeInfoDisplaySetting" className="flex flex-col mb-5 text-green-400 font-bold">
      {['Waypoint', 'Radio Navigation AIDs'].map((label) => (
        <div key={label} className="mb-2">
          <span className="">{label}</span>
          <div className="flex flex-col ml-5">
            <label htmlFor={`${label.toLowerCase()}Name`} className="flex items-center">
              <input
                id={`${label.toLowerCase()}Name`}
                type="checkbox"
                checked={label === 'Waypoint' ? isDisplaying.waypointName : isDisplaying.radioNavigationAidsName}
                onChange={handleCheckboxChange(label === 'Waypoint' ? 'waypointName' : 'radioNavigationAidsName')}
                className="mr-2"
              />
              <span>Name</span>
            </label>
            <label htmlFor={`${label.toLowerCase()}Point`} className="flex items-center">
              <input
                id={`${label.toLowerCase()}Point`}
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
      <label htmlFor="atsLowerRoute" className="flex items-center mb-2">
        <input
          id="atsLowerRoute"
          type="checkbox"
          checked={isDisplaying.atsLowerRoute}
          onChange={handleCheckboxChange('atsLowerRoute')}
          className="mr-2"
        />
        <span>ATS Lower Route</span>
      </label>
      <label htmlFor="rnavRoute" className="flex items-center mb-2">
        <input
          id="rnavRoute"
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