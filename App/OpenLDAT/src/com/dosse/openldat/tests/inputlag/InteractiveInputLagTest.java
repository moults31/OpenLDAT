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
package com.dosse.openldat.tests.inputlag;

import com.dosse.openldat.device.Device;
import com.dosse.openldat.device.callbacks.LightSensorButtonCallback;
import com.dosse.openldat.processing.buffers.IBuffer;
import com.dosse.openldat.tests.ITest;

/**
 *
 * @author dosse
 */
public abstract class InteractiveInputLagTest implements ITest {

    private Device d;
    private int threshold = 100;
    private byte sensitivity = 2, state = 0; //0=waiting for click, 1=waiting for light, 2=waiting for dark
    private boolean autoFire = false;
    private static final boolean unbuffered = false, fastADC = true;
    private IBuffer lWindow, cWindow;
    private final double sampleRate;

    private final LightSensorButtonCallback callback = new LightSensorButtonCallback() {
        private long sampleN = 0;
        private double clickT = 0;

        @Override
        public void onDataBufferReceived(int[] light, int[] click) {
            for (int i = 0; i < light.length; i++) {
                double t = (double) sampleN / sampleRate;
                switch (state) {
                    case 0:
                        if (click[i] == 1) {
                            clickT = t;
                            if (light[i] < threshold) {
                                state = 1;
                            }
                        }
                        break;
                    case 1:
                        if (click[i] == 1) {
                            clickT = t;
                        }
                        if (light[i] >= threshold) {
                            onNewDataPoint(1000.0 * (t - clickT));
                            state = 2;
                        } else {
                            if (t - clickT > 0.9) {
                                state = 0;
                            }
                        }
                        break;
                    case 2:
                        if (light[i] < threshold && (t - clickT) > 0.2 /*"debounce" 200ms*/) {
                            state = 0;
                        }
                        break;
                    default:
                        break;
                }
                sampleN++;
            }
            if (cWindow != null) {
                cWindow.add(click);
            }
            if (lWindow != null) {
                lWindow.add(light);
            }
        }

        @Override
        public void onDataSampleReceived(int light, int click) {
            double t = (double) sampleN / sampleRate;
            switch (state) {
                case 0:
                    if (click == 1) {
                        clickT = t;
                        if (light < threshold) {
                            state = 1;
                        }
                    }
                    break;
                case 1:
                    if (click == 1) {
                        clickT = t;
                    }
                    if (light >= threshold) {
                        onNewDataPoint(1000.0 * (t - clickT));
                        state = 2;
                    } else {
                        if (t - clickT > 0.9) {
                            state = 0;
                        }
                    }
                    break;
                case 2:
                    if (light < threshold && (t - clickT) > 0.2 /*"debounce" 200ms*/) {
                        state = 0;
                    }
                    break;
                default:
                    break;
            }
            sampleN++;
            if (cWindow != null) {
                cWindow.add(click);
            }
            if (lWindow != null) {
                lWindow.add(light);
            }
        }

        @Override
        public void onError(Exception e) {
            InteractiveInputLagTest.this.onError(e);
        }

        public void reset() {
            sampleN = 0;
            clickT = 0;
            state = 0;
        }
    };

    public InteractiveInputLagTest(Device d, IBuffer lightWindow, IBuffer clickWindow) throws Exception {
        this.d = d;
        lWindow = lightWindow;
        cWindow = clickWindow;
        sampleRate = d.getLightSensorButtonModeSampleRate(unbuffered, fastADC);
    }

    @Override
    public void begin() {
        try {
            d.lightSensorButtonMode(unbuffered, sensitivity, fastADC, !autoFire, autoFire, callback);
        } catch (Exception ex) {
            onError(ex);
        }
    }

    @Override
    public void cancel() {
        d.endCurrentActivity();
        onDone(null);
    }

    public abstract void onNewDataPoint(double delay);

    public int getThreshold() {
        return threshold;
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold > 1023 ? 1023 : threshold < 0 ? 0 : threshold;
    }

    public byte getSensitivity() {
        return sensitivity;
    }

    public void setSensitivity(byte sensitivity) {
        if (sensitivity == this.sensitivity) {
            return;
        }
        this.sensitivity = sensitivity > 3 ? 3 : sensitivity < 0 ? 0 : sensitivity;
        begin();
    }

    public void setAutoFire(boolean autofire) {
        if (autofire == this.autoFire) {
            return;
        }
        this.autoFire = autofire;
        begin();
    }

    public boolean getAutoFire() {
        return autoFire;
    }

    public byte getState() {
        return state;
    }

}
