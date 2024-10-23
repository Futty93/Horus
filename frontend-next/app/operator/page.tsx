import React from "react";
import Image from "next/image";
import RadarCanvas from "@/component/radarCanvas";
import { RouteInfoDisplaySettingProvider } from "@/context/routeInfoDisplaySettingContext";
import RouteInfoDisplaySetting from "@/component/routeInfoDisplaySetting";

export default function OperatorPage() {
  return (
    <RouteInfoDisplaySettingProvider>
      <div className="flex h-screen">
        <div className="flex-1 flex">
            <RadarCanvas />
          <div className="controlPanel bg-gray-800 text-white p-5 flex flex-col justify-between">
            <div id="callsignDisplay" className="text-center mb-5">
              <p id="callsign" className="text-2xl font-bold text-green-400">&nbsp;</p>
            </div>
            <div className="flex flex-col">
              {['Altitude', 'Speed', 'Heading'].map((label) => (
                <div className="mb-5" key={label}>
                  <div className="font-bold text-green-400">{label}</div>
                  <input
                    type="number"
                    id={label.toLowerCase()}
                    placeholder="0"
                    className="w-3/5 p-2 rounded border border-green-400 bg-gray-900 text-white focus:outline-none focus:border-green-400 focus:ring focus:ring-green-400 transition duration-300"
                  />
                </div>
              ))}
              <input type="button" value="Confirm" id="confirmButton" className="bg-green-400 text-gray-800 font-bold p-2 rounded transition duration-300 cursor-pointer hover:bg-green-500" />
            </div>
            <div id="settingArea" className="mt-auto">
              <RouteInfoDisplaySetting />
              <div className="flex flex-col">
                <div className="flex justify-between mb-5">
                  <label htmlFor="selectSector" className="font-bold text-green-400 mr-2">担当セクター:</label>
                  <select id="selectSector" className="p-2 rounded border border-green-400 bg-gray-900 text-white">
                    <option value="T09">T09 関東南A</option>
                    <option value="T10">T10 関東南B</option>
                    <option value="T14">T14 伊豆</option>
                    <option value="T25">T25 知多</option>
                    <option value="T30">T30</option>
                    <option value="T31">T31</option>
                    <option value="T32">T32</option>
                    <option value="T33">T33</option>
                    <option value="T34">T34</option>
                    <option value="T35">T35</option>
                    <option value="T36">T36</option>
                    <option value="T38">T38</option>
                    <option value="T39">T39</option>
                    <option value="T45">T45</option>
                    <option value="T46">T46</option>
                    <option value="T92">T92</option>
                    <option value="T93">T93</option>

                    <option value="F01">F01</option>
                    <option value="F04">F04</option>
                    <option value="F05">F05</option>
                    <option value="F07">F07</option>
                    <option value="F08">F08</option>
                    <option value="F09">F09</option>
                    <option value="F10">F10</option>
                    <option value="F11">F11</option>
                    <option value="F12">F12</option>
                    <option value="F13">F13</option>
                    <option value="F14">F14</option>
                    <option value="F15">F15</option>
                    <option value="F16">F16</option>
                    <option value="F17">F17</option>

                    <option value="N43">F43</option>
                    <option value="N44">F44</option>
                    <option value="N47">F47</option>
                    <option value="N48">F48</option>
                    <option value="N49">F49</option>
                    <option value="N50">F50</option>
                    <option value="N51">F51</option>
                    <option value="N52">F52</option>
                    <option value="N53">F53</option>
                    <option value="N54">F54</option>
                    <option value="N55">F55</option>

                    <option value="A01">A01</option>
                    <option value="A02">A02</option>
                    <option value="A03">A03</option>
                    <option value="A04">A04</option>
                    <option value="A05">A05</option>
                  </select>
                </div>
                <div className="flex justify-between mb-5">
                  <label htmlFor="displayRange" className="font-bold text-green-400 mr-2">表示範囲:</label>
                  <input
                    type="number"
                    id="displayRange"
                    defaultValue="200"
                    min="10"
                    max="4000"
                    className="p-2 rounded border border-green-400 bg-gray-900 text-white text-center"
                  />&nbsp;km
                </div>
                <div className="flex justify-between">
                  <input type="button" value="Start" id="startButton" className="bg-green-400 text-gray-800 font-bold p-2 rounded transition duration-300 cursor-pointer hover:bg-green-500" />
                  <input type="button" value="Pause" id="pauseButton" className="bg-yellow-500 text-gray-800 font-bold p-2 rounded transition duration-300 cursor-pointer hover:bg-yellow-600" />
                  <input type="button" value="Reset" id="resetButton" className="bg-red-500 text-gray-800 font-bold p-2 rounded transition duration-300 cursor-pointer hover:bg-red-600" />
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </RouteInfoDisplaySettingProvider>
  );
}
