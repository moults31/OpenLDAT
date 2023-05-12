/*
 * Copyright (C) 2021 Federico Dossena
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.dosse.openldat.device;

import com.fazecast.jSerialComm.SerialPort;
import java.util.ArrayList;

/**
 *
 * @author dosse
 */
public class DeviceFinder {

    public static SerialPort[] findDevices() {
        ArrayList<SerialPort> ret = new ArrayList<>();
        for (SerialPort p : SerialPort.getCommPorts()) {
            if (p.getPortDescription().toLowerCase().startsWith("openldat ")) {
                if (!p.getPortDescription().toLowerCase().contains("dial-in")) {
                    ret.add(p);
                }
            }
        }
        return ret.toArray(new SerialPort[0]);
    }

    public static Device getDevice() {
        for (SerialPort com : findDevices()) {
            try {
                return new Device(com);
            } catch (Throwable t) {
                System.err.println("Warning: Device on " + com.getSystemPortName() + " couldn't be used because " + t.getMessage());
                t.printStackTrace();
            }
        }
        return null;
    }

}
