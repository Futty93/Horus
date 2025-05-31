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

  const CustomCheckbox = ({ id, checked, onChange, label }: {
    id: string;
    checked: boolean;
    onChange: (event: React.ChangeEvent<HTMLInputElement>) => void;
    label: string;
  }) => (
    <label htmlFor={id} className="flex items-center group cursor-pointer py-2">
      <div className="relative">
        <input
          id={id}
          type="checkbox"
          checked={checked}
          onChange={onChange}
          className="sr-only"
        />
        <div className={`w-5 h-5 rounded border-2 transition-all duration-300 ease-out
                        ${checked
                          ? 'bg-button-gradient border-radar-primary shadow-cyber'
                          : 'bg-matrix-dark border-matrix-accent hover:border-radar-secondary'
                        }
                        group-hover:scale-110 group-hover:shadow-neon`}>
          {checked && (
            <div className="absolute inset-0 flex items-center justify-center">
              <svg className="w-3 h-3 text-matrix-dark" fill="currentColor" viewBox="0 0 20 20">
                <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
              </svg>
            </div>
          )}
        </div>
      </div>
      <span className={`ml-3 text-sm font-mono transition-all duration-300
                       ${checked ? 'text-radar-primary' : 'text-gray-300'}
                       group-hover:text-neon-blue group-hover:drop-shadow-lg`}>
        {label}
      </span>
    </label>
  );

  return (
    <div id="routeInfoDisplaySetting"
         className="bg-control-gradient border border-matrix-accent rounded-cyber-lg p-4 backdrop-blur-sm mb-4">
      <h3 className="text-sm font-bold text-radar-primary font-mono tracking-wider mb-3 text-center">
        ROUTE DISPLAY SETTINGS
      </h3>

      <div className="space-y-3">
        {[
          {
            label: 'Waypoint',
            settings: [
              { key: 'waypointName' as keyof DisplaySettings, label: 'Name' },
              { key: 'waypointPoint' as keyof DisplaySettings, label: 'Point' }
            ]
          },
          {
            label: 'Radio Navigation AIDs',
            settings: [
              { key: 'radioNavigationAidsName' as keyof DisplaySettings, label: 'Name' },
              { key: 'radioNavigationAidsPoint' as keyof DisplaySettings, label: 'Point' }
            ]
          }
        ].map(({ label, settings }) => (
          <div key={label} className="border border-matrix-accent/50 rounded-cyber p-3
                                      bg-gradient-to-r from-matrix-dark/50 to-matrix-medium/50">
            <h4 className="text-radar-secondary font-semibold font-mono tracking-wide mb-2 text-xs">
              {label}
            </h4>
            <div className="ml-3 space-y-1">
              {settings.map(({ key, label: settingLabel }) => (
                <CustomCheckbox
                  key={key}
                  id={key}
                  checked={isDisplaying[key]}
                  onChange={handleCheckboxChange(key)}
                  label={settingLabel}
                />
              ))}
            </div>
          </div>
        ))}

        <div className="space-y-1">
          <CustomCheckbox
            id="atsLowerRoute"
            checked={isDisplaying.atsLowerRoute}
            onChange={handleCheckboxChange('atsLowerRoute')}
            label="ATS Lower Route"
          />
          <CustomCheckbox
            id="rnavRoute"
            checked={isDisplaying.rnavRoute}
            onChange={handleCheckboxChange('rnavRoute')}
            label="RNAV Route"
          />
        </div>
      </div>
    </div>
  );
};

export default RouteInfoDisplaySetting;
