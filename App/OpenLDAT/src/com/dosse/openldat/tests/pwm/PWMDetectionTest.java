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
package com.dosse.openldat.tests.pwm;

import com.dosse.openldat.Config;
import com.dosse.openldat.Utils;
import com.dosse.openldat.device.Device;
import com.dosse.openldat.device.callbacks.LightSensorMonitorCallback;
import com.dosse.openldat.processing.buffers.CircularBuffer;
import com.dosse.openldat.processing.filters.FFTFilter;
import com.dosse.openldat.tests.ITest;
import com.dosse.openldat.tests.IgnorableException;
import com.dosse.openldat.tests.TestException;
import com.dosse.openldat.tests.testscreen.ITestScreen;
import com.dosse.openldat.tests.testscreen.opengl.TestScreenGL;
import com.dosse.openldat.tests.testscreen.swing.TestScreenSwing;
import java.util.Arrays;
import java.util.HashMap;

/**
 *
 * @author dosse
 */
public abstract class PWMDetectionTest extends Thread implements ITest {

    private final Device d;
    private final ITestScreen ts;
    private boolean enterPressed, escPressed, stopASAP = false;

    public PWMDetectionTest(Device d) {
        this.d = d;
        if (Config.TESTSCREEN_GL) {
            ts = new TestScreenGL(TestScreenGL.VSYNC_ON) {
                @Override
                public void onEnterPressed() {
                    enterPressed = true;
                }

                @Override
                public void onCancel() {
                    escPressed = true;
                    PWMDetectionTest.this.interrupt();
                }

                @Override
                public void onError(Exception e) {
                    if (stopASAP) {
                        return;
                    }
                    stopASAP = true;
                    PWMDetectionTest.this.interrupt();
                    PWMDetectionTest.this.onError(e);
                }
            };
        } else {
            ts = new TestScreenSwing() {
                @Override
                public void onEnterPressed() {
                    enterPressed = true;
                }

                @Override
                public void onCancel() {
                    escPressed = true;
                    PWMDetectionTest.this.interrupt();
                }

                @Override
                public void onError(Exception e) {
                    if (stopASAP) {
                        return;
                    }
                    stopASAP = true;
                    PWMDetectionTest.this.interrupt();
                    PWMDetectionTest.this.onError(e);
                }
            };
        }
    }

    public static double detectPWMFrequency(int data[], double sampleRate, double minFreq, double maxFreq) {
        double[][] freqs = new double[data.length][2];
        for (int i = 0; i < data.length; i++) {
            freqs[i][0] = ((double)i/(double)data.length)*(sampleRate/2.0) ;
            freqs[i][1] = data[i];
        }
        if (freqs[0][1] == 0) {
            return 0;
        }
        double pwmFreq = 0, maxIntensity = 0;
        double f, p;
        for (double[] freq : freqs) {
            f = freq[0];
            p = freq[1];
            if (f >= minFreq && f <= maxFreq && p > 0) {
                if (p > maxIntensity) {
                    pwmFreq = f;
                    maxIntensity = p;
                }
            }
        }
        if (maxIntensity < data.length * 0.15) {
            return 0;
        } else {
            return pwmFreq;
        }
    }

    private double[] shootMinMax(byte sensitivity, boolean absolute) {
        CircularBuffer b;
        try {
            int bSize = (int) (d.getLightSensorMonitorModeSampleRate(true, false) * 0.5);
            b = new CircularBuffer(bSize);
            d.lightSensorMonitorMode(true, sensitivity, false, new LightSensorMonitorCallback() {
                @Override
                public void onDataSampleReceived(int data) {
                    b.add(data);
                }

                @Override
                public void onError(Exception e) {
                    if (stopASAP) {
                        return;
                    }
                    stopASAP = true;
                    PWMDetectionTest.this.interrupt();
                    PWMDetectionTest.this.onError(e);
                }
            });
        } catch (Throwable t) {
            return new double[]{-1, -1};
        }
        while (!b.isFilled()) {
            if (stopASAP || escPressed) {
                return new double[]{-1, -1};
            }
            Utils.sleep(10);
        }
        d.endCurrentActivity();
        int[] data = b.getData();
        Arrays.sort(data);
        return new double[]{data[absolute ? 0 : (int) (data.length * 0.05)], data[absolute ? data.length - 1 : (int) (data.length * 0.95)]};
    }

    @Override
    public void run() {
        try {
            ts.setColor(0.8f, 0.8f, 0.8f);
            ts.setTarget(0.5f, 0.5f, 0.2f, true);
            while (!(enterPressed || escPressed || stopASAP)) {
                Utils.sleep(10);
            }
            if (stopASAP) {
                throw new IgnorableException();
            }
            if (escPressed) {
                throw new TestException(TestException.USER_ABORT);
            }
            enterPressed = false;
            ts.hideTarget();
            Utils.sleep(500);
            byte sensitivity = 3;
            HashMap<String, Object> ret = new HashMap<>();
            while (true) {
                if (stopASAP) {
                    throw new IgnorableException();
                }
                if (escPressed) {
                    throw new TestException(TestException.USER_ABORT);
                }
                double[] white = shootMinMax(sensitivity, true);
                if (white[1] > 750 && sensitivity > 0) {
                    sensitivity--;
                } else {
                    ret.put("noisy", white[1] - white[0] > 8);
                    break;
                }
            }
            int bSize = (int) (d.getLightSensorMonitorModeSampleRate(true, true) * 1);
            bSize = (int) Math.pow(2, Math.ceil(Math.log(bSize) / Math.log(2)));
            FFTFilter b = new FFTFilter(bSize);
            double sampleRate = d.lightSensorMonitorMode(true, sensitivity, true, new LightSensorMonitorCallback() {
                @Override
                public void onDataBufferReceived(int[] data) {
                    b.add(data);
                }

                @Override
                public void onDataSampleReceived(int data) {
                    b.add(data);
                }

                @Override
                public void onError(Exception e) {
                    if (stopASAP) {
                        return;
                    }
                    stopASAP = true;
                    PWMDetectionTest.this.interrupt();
                    PWMDetectionTest.this.onError(e);
                }
            });
            while (!b.isFilled()) {
                if (stopASAP) {
                    throw new IgnorableException();
                }
                if (escPressed) {
                    throw new TestException(TestException.USER_ABORT);
                }
                Utils.sleep(1);
            }
            d.endCurrentActivity();
            if (stopASAP) {
                throw new IgnorableException();
            }
            if (escPressed) {
                throw new TestException(TestException.USER_ABORT);
            }
            double pwmFreq = detectPWMFrequency(b.getData(), sampleRate, 40, 2500);
            ts.close();
            ret.put("frequency", pwmFreq);
            ret.put("raw", b.getOriginalData());
            ret.put("sampleRate", sampleRate);
            onDone(ret);
        } catch (Exception ex) {
            ts.close();
            if (!(ex instanceof IgnorableException)) {
                onError(ex);
            }
        }
    }

    @Override
    public void cancel() {
        stopASAP = true;
        this.interrupt();
    }

    @Override
    public void begin() {
        start();
    }

}
