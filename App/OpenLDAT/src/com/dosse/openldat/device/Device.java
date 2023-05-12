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

import com.dosse.openldat.Utils;
import com.dosse.openldat.device.callbacks.LightSensorButtonCallback;
import com.dosse.openldat.device.callbacks.LightSensorMonitorCallback;
import com.dosse.openldat.device.errors.MissingSensorException;
import com.dosse.openldat.device.errors.DeviceError;
import com.fazecast.jSerialComm.SerialPort;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author dosse
 */
public class Device {
    
    public static final int DRIVER_VERSION = 1;
    
    private SerialPort com;
    private InputStream in;
    private boolean hasLightSensor, isPrototype, oscilloscopeDebug;
    private int largeBufferSize = -1, smallBufferSize = -1;
    private String firmwareVersion, serialNumber;
    private int model = -1, minver = -1;
    
    private Thread workerThread = null;
    private boolean stopWorkerThreadASAP = false;
    
    private static final byte COMMAND_ID = 0x44,
            COMMAND_IDLE = 0x49,
            COMMAND_LIGHTSENSOR = 0x4C;
    
    private static final byte LIGHTSENSOR_FEATURE_AUTOFIRE = 0b00000001,
            LIGHTSENSOR_FEATURE_NOBUFFER = 0b00000010,
            LIGHTSENSOR_FEATURE_HIGHSENS1 = 0b00000100,
            LIGHTSENSOR_FEATURE_MONITOR = 0b00001000,
            LIGHTSENSOR_FEATURE_NOCLICK = 0b00010000,
            LIGHTSENSOR_FEATURE_FASTADC = 0b00100000,
            LIGHTSENSOR_FEATURE_HIGHSENS2 = 0b01000000;
    
    private static final byte NO_FLAGS = 0x00;
    
    public Device(SerialPort com) throws DeviceError {
        this.com = com;
        String name = com.getPortDescription().toLowerCase().trim();
        if (name.startsWith("openldat ")) {
            if (name.equals("openldat model 1") || name.equals("openldat prototype")) {
                //supported, model 1
                model = 1;
                if (!com.openPort(100, 2, 131072)) {
                    throw new DeviceError(DeviceError.FAILED_TO_CONNECT);
                }
                com.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 10, 10);
                in = com.getInputStream();
                sendCommand(COMMAND_IDLE, NO_FLAGS);
                waitForInactivity();
                sendCommand(COMMAND_ID, NO_FLAGS);
                int lines = 0;
                while (true) {
                    String s = readString();
                    if (s.isEmpty()) {
                        break;
                    } else {
                        String[] parts = s.split(":");
                        if (parts.length == 1) {
                            continue;
                        }
                        String property = parts[0].trim(), value = parts[1].trim();
                        switch (property) {
                            case "FW": {
                                firmwareVersion = value;
                                lines++;
                                break;
                            }
                            case "LightSensor": {
                                hasLightSensor = value.equals("1");
                                lines++;
                                break;
                            }
                            case "LBuffer": {
                                largeBufferSize = Integer.parseInt(value);
                                lines++;
                                break;
                            }
                            case "SBuffer": {
                                smallBufferSize = Integer.parseInt(value);
                                lines++;
                                break;
                            }
                            case "OscilloscopeDebug": {
                                oscilloscopeDebug = value.equals("1");
                                lines++;
                                break;
                            }
                            case "SerialDebug": {
                                if (value.equals("1")) {
                                    throw new DeviceError(DeviceError.FIRMWARE_BUILT_WITH_SERIALPLOT);
                                }
                                break;
                            }
                            case "Prototype": {
                                isPrototype = value.equals("1");
                                lines++;
                                break;
                            }
                            case "MinAppVer": {
                                minver = Integer.parseInt(value);
                                lines++;
                                break;
                            }
                            case "SerialNo": {
                                serialNumber = value;
                                lines++;
                                break;
                            }
                            default: {
                                System.err.println("WARNING: unknown device property \"" + property + "\" with value \"" + parts[1].trim() + "\" ignored");
                                break;
                            }
                        }
                    }
                }
                if (lines == 0) {
                    throw new DeviceError(DeviceError.DEVICE_ID_FAILED);
                }
                if (minver < DRIVER_VERSION) {
                    throw new DeviceError(DeviceError.FIRMWARE_NEEDS_NEWER_DRIVER);
                }
                if (firmwareVersion == null) {
                    throw new DeviceError(DeviceError.FIRMWARE_UNKNOWN);
                }
                if (hasLightSensor && (largeBufferSize == -1 || smallBufferSize == -1)) {
                    throw new DeviceError(DeviceError.FIRMWARE_LIGHTSENSOR_MISSING_BUFSIZES);
                }
                if (serialNumber == null) {
                    serialNumber = "DIY";
                }
            } else {
                throw new DeviceError(DeviceError.UNSUPPORTED_MODEL);
            }
        } else {
            throw new DeviceError(DeviceError.NOT_OPENLDAT_DEVICE);
        }
    }
    
    private void sendCommand(byte cmd, byte flags) {
        com.writeBytes(new byte[]{cmd, flags}, 2);
    }
    
    private String readString() {
        StringBuilder b = new StringBuilder();
        while (true) {
            try {
                int c = in.read();
                if (c == 0x0A) {
                    break;
                } else {
                    b.append((char) c);
                }
            } catch (IOException ex) {
                break;
            }
        }
        return b.toString().trim();
    }
    
    private int readUInt16() throws IOException {
        return (in.read() & 0xFF) | ((in.read() & 0xFF) << 8);
    }
    
    private int readUInt8() throws IOException {
        return in.read() & 0xFF;
    }
    
    private void readUInt16Array(int[] buffer) throws IOException {
        for (int i = 0; i < buffer.length; i++) {
            buffer[i] = (in.read() & 0xFF) | ((in.read() & 0xFF) << 8);
        }
    }
    
    private void readUInt8Array(int[] buffer) throws IOException {
        for (int i = 0; i < buffer.length; i++) {
            buffer[i] = in.read() & 0xFF;
        }
    }
    
    private float readFloat32() throws IOException {
        int temp = (in.read() & 0xFF) | ((in.read() & 0xFF) << 8) | ((in.read() & 0xFF) << 16) | ((in.read() & 0xFF) << 24);
        return Float.intBitsToFloat(temp);
    }
    
    public void endCurrentActivity() {
        sendCommand(COMMAND_IDLE, NO_FLAGS);
        if (workerThread != null) {
            if (workerThread.isAlive()) {
                stopWorkerThreadASAP = true;
                while (stopWorkerThreadASAP) {
                    Utils.sleep(1);
                }
            }
            workerThread = null;
        }
        waitForInactivity();
    }
    
    private void waitForInactivity() {
        try {
            long lastRead = System.currentTimeMillis();
            while (System.currentTimeMillis() - lastRead < 100) {
                if (in.available() != 0) {
                    in.read();
                    lastRead = System.currentTimeMillis();
                }
            }
        } catch (IOException ex) {
        }
    }
    
    public void close() {
        if (com.isOpen()) {
            endCurrentActivity();
            com.closePort();
        }
    }
    
    public boolean isOpen() {
        return com.isOpen();
    }
    
    public boolean hasLightSensor() {
        return hasLightSensor;
    }
    
    public boolean isPrototype() {
        return isPrototype;
    }
    
    public boolean hasOscilloscopeDebug() {
        return oscilloscopeDebug;
    }
    
    public String getFirmwareVersion() {
        return firmwareVersion;
    }
    
    public String getModel() {
        return com.getPortDescription();
    }
    
    public int getModelCode() {
        return model;
    }
    
    public String getPortName() {
        return com.getSystemPortName();
    }
    
    public int getMinDriverVersion() {
        return minver;
    }
    
    public String getSerialNumber() {
        return serialNumber;
    }
    
    public boolean isBusy() {
        return workerThread != null && workerThread.isAlive();
    }
    
    public double getLightSensorMonitorModeSampleRate(boolean noBuffer, boolean fastADC) throws MissingSensorException {
        if (!hasLightSensor) {
            throw new MissingSensorException(MissingSensorException.LIGHT_SENSOR);
        }
        if (noBuffer) {
            if (fastADC) {
                return 21000.0;
            } else {
                return 7798.0;
            }
        } else {
            if (fastADC) {
                return 29574.4;
            } else {
                return 8780.8;
            }
        }
    }
    
    public double lightSensorMonitorMode(boolean noBuffer, byte sensitivity, boolean fastADC, LightSensorMonitorCallback callback) throws MissingSensorException, IOException {
        if (!com.isOpen()) {
            throw new IOException("Device closed");
        }
        endCurrentActivity();
        if (!hasLightSensor) {
            throw new MissingSensorException(MissingSensorException.LIGHT_SENSOR);
        }
        sendCommand(COMMAND_LIGHTSENSOR, (byte) (LIGHTSENSOR_FEATURE_MONITOR | (noBuffer ? LIGHTSENSOR_FEATURE_NOBUFFER : 0) | ((sensitivity & 0b01) != 0 ? LIGHTSENSOR_FEATURE_HIGHSENS1 : 0) | ((sensitivity & 0b10) != 0 ? LIGHTSENSOR_FEATURE_HIGHSENS2 : 0) | (fastADC ? LIGHTSENSOR_FEATURE_FASTADC : 0)));
        workerThread = new Thread() {
            @Override
            public void run() {
                setPriority(Thread.MAX_PRIORITY);
                if (noBuffer) {
                    while (true) {
                        if (stopWorkerThreadASAP) {
                            stopWorkerThreadASAP = false;
                            return;
                        }
                        if (!com.isOpen()) {
                            callback.onError(new IOException("Device closed"));
                            return;
                        }
                        if (com.bytesAvailable() >= 2) {
                            try {
                                callback.onDataSampleReceived(readUInt16());
                            } catch (IOException ex) {
                                callback.onError(ex);
                                return;
                            }
                        } else {
                            Utils.sleep(1);
                        }
                    }
                } else {
                    int[] lightBuffer = new int[largeBufferSize];
                    while (true) {
                        if (stopWorkerThreadASAP) {
                            stopWorkerThreadASAP = false;
                            return;
                        }
                        if (!com.isOpen()) {
                            callback.onError(new IOException("Device closed"));
                            return;
                        }
                        if (com.bytesAvailable() >= largeBufferSize * 2) {
                            try {
                                readUInt16Array(lightBuffer);
                                callback.onDataBufferReceived(lightBuffer);
                            } catch (IOException ex) {
                                callback.onError(ex);
                                return;
                            }
                        } else {
                            Utils.sleep(1);
                        }
                    }
                }
            }
        };
        workerThread.start();
        return getLightSensorMonitorModeSampleRate(noBuffer, fastADC);
    }
    
    public double getLightSensorButtonModeSampleRate(boolean noBuffer, boolean fastADC) throws MissingSensorException {
        if (!hasLightSensor) {
            throw new MissingSensorException(MissingSensorException.LIGHT_SENSOR);
        }
        if (noBuffer) {
            if (fastADC) {
                return 20710.0;
            } else {
                return 7796.0;
            }
        } else {
            if (fastADC) {
                return 28896.0;
            } else {
                return 8738.1;
            }
        }
    }
    
    public double lightSensorButtonMode(boolean noBuffer, byte sensitivity, boolean fastADC, boolean noClick, boolean autoFire, LightSensorButtonCallback callback) throws MissingSensorException, IOException {
        if (!com.isOpen()) {
            throw new IOException("Device closed");
        }
        endCurrentActivity();
        if (!hasLightSensor) {
            throw new MissingSensorException(MissingSensorException.LIGHT_SENSOR);
        }
        sendCommand(COMMAND_LIGHTSENSOR, (byte) ((noBuffer ? LIGHTSENSOR_FEATURE_NOBUFFER : 0) | ((sensitivity & 0b01) != 0 ? LIGHTSENSOR_FEATURE_HIGHSENS1 : 0) | ((sensitivity & 0b10) != 0 ? LIGHTSENSOR_FEATURE_HIGHSENS2 : 0) | (fastADC ? LIGHTSENSOR_FEATURE_FASTADC : 0) | (noClick ? LIGHTSENSOR_FEATURE_NOCLICK : 0) | (autoFire ? LIGHTSENSOR_FEATURE_AUTOFIRE : 0)));
        workerThread = new Thread() {
            @Override
            public void run() {
                setPriority(Thread.MAX_PRIORITY);
                if (noBuffer) {
                    while (true) {
                        if (stopWorkerThreadASAP) {
                            stopWorkerThreadASAP = false;
                            return;
                        }
                        if (!com.isOpen()) {
                            callback.onError(new IOException("Device closed"));
                            return;
                        }
                        if (com.bytesAvailable() >= 3) {
                            try {
                                callback.onDataSampleReceived(readUInt16(), readUInt8());
                            } catch (IOException ex) {
                                callback.onError(ex);
                                return;
                            }
                        } else {
                            Utils.sleep(1);
                        }
                    }
                } else {
                    int[] lightBuffer = new int[smallBufferSize], clickBuffer = new int[smallBufferSize];
                    while (true) {
                        if (stopWorkerThreadASAP) {
                            stopWorkerThreadASAP = false;
                            return;
                        }
                        if (!com.isOpen()) {
                            callback.onError(new IOException("Device closed"));
                            return;
                        }
                        if (com.bytesAvailable() >= smallBufferSize * 3) {
                            try {
                                readUInt16Array(lightBuffer);
                                readUInt8Array(clickBuffer);
                                callback.onDataBufferReceived(lightBuffer, clickBuffer);
                            } catch (IOException ex) {
                                callback.onError(ex);
                                return;
                            }
                        } else {
                            Utils.sleep(1);
                        }
                    }
                }
            }
        };
        workerThread.start();
        return getLightSensorButtonModeSampleRate(noBuffer, fastADC);
    }
    
}
