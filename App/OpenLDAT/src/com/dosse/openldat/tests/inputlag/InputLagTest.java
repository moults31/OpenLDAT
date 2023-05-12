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

import com.dosse.openldat.Config;
import com.dosse.openldat.Utils;
import com.dosse.openldat.device.Device;
import com.dosse.openldat.device.callbacks.LightSensorButtonCallback;
import com.dosse.openldat.device.callbacks.LightSensorMonitorCallback;
import com.dosse.openldat.processing.buffers.CircularBuffer;
import com.dosse.openldat.tests.ITest;
import com.dosse.openldat.tests.IgnorableException;
import com.dosse.openldat.tests.TestException;
import com.dosse.openldat.tests.testscreen.ITestScreen;
import com.dosse.openldat.tests.testscreen.opengl.TestScreenGL;
import com.dosse.openldat.tests.testscreen.swing.TestScreenSwing;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

/**
 *
 * @author dosse
 */
public abstract class InputLagTest extends Thread implements ITest {

    private final Device d;
    private final ITestScreen ts;
    private boolean enterPressed, escPressed, stopASAP = false;
    private long durationMs;
    private static final boolean unbuffered = false, fastADC = true;

    public InputLagTest(Device d, long durationMs, int vsyncMode, long fakeCPULoadMs, long fakeGPULoadMs) {
        this.d = d;
        this.durationMs = durationMs;
        if (Config.TESTSCREEN_GL) {
            ts = new TestScreenGL(vsyncMode) {
                @Override
                public void onEnterPressed() {
                    enterPressed = true;
                }

                @Override
                public void onCancel() {
                    escPressed = true;
                    InputLagTest.this.interrupt();
                }

                @Override
                public void onError(Exception e) {
                    if (stopASAP) {
                        return;
                    }
                    stopASAP = true;
                    InputLagTest.this.interrupt();
                    InputLagTest.this.onError(e);
                }
            };
            ts.setFakeLoad(fakeCPULoadMs, fakeGPULoadMs);
        } else {
            ts = new TestScreenSwing() {
                @Override
                public void onEnterPressed() {
                    enterPressed = true;
                }

                @Override
                public void onCancel() {
                    escPressed = true;
                    InputLagTest.this.interrupt();
                }

                @Override
                public void onError(Exception e) {
                    if (stopASAP) {
                        return;
                    }
                    stopASAP = true;
                    InputLagTest.this.interrupt();
                    InputLagTest.this.onError(e);
                }
            };
        }
    }

    private double shootMax(byte sensitivity) {
        CircularBuffer b;
        try {
            int bSize = (int) (d.getLightSensorMonitorModeSampleRate(true, false) * 0.2);
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
                    InputLagTest.this.interrupt();
                    InputLagTest.this.onError(e);
                }
            });
        } catch (Throwable t) {
            return -1;
        }
        while (!b.isFilled()) {
            if (stopASAP || escPressed) {
                return -1;
            }
            Utils.sleep(10);
        }
        d.endCurrentActivity();
        if (stopASAP || escPressed) {
            return -1;
        }
        int[] data = b.getData();
        int max = data[0];
        for (int i = 1; i < data.length; i++) {
            if (data[i] > max) {
                max = data[i];
            }
        }
        return max;
    }

    @Override
    public void run() {
        try {
            ts.setColor(0, 0, 0);
            ts.setTarget(0.5f, 0.5f, 0.2f, false);
            while (!(enterPressed || escPressed || stopASAP)) {
                Utils.sleep(10);
            }
            if (stopASAP) {
                ts.close();
                return;
            }
            if (escPressed) {
                ts.close();
                onError(new TestException(TestException.USER_ABORT));
                return;
            }
            enterPressed = false;
            byte sensitivity = 3;
            double black;
            ts.setColor(0, 0, 0);
            ts.hideTarget();
            Utils.sleep(500);
            while (true) {
                if (stopASAP) {
                    throw new IgnorableException();
                }
                if (escPressed) {
                    throw new TestException(TestException.USER_ABORT);
                }
                black = shootMax(sensitivity);
                if (black > 100 && sensitivity > 0) {
                    sensitivity--;
                } else {
                    break;
                }
            }
            ts.setFlashOnClick(true);
            HashMap<String, Object> ret = new HashMap<>();
            double sampleRate = d.getLightSensorButtonModeSampleRate(unbuffered, fastADC);
            CircularBuffer light = new CircularBuffer((int) (sampleRate * (float) durationMs / 1000.0)),
                    click = new CircularBuffer((int) (sampleRate * (float) durationMs / 1000.0));
            d.lightSensorButtonMode(unbuffered, sensitivity, fastADC, false, true, new LightSensorButtonCallback() {
                @Override
                public void onDataBufferReceived(int[] l, int[] c) {
                    light.add(l);
                    click.add(c);
                }

                @Override
                public void onDataSampleReceived(int l, int c) {
                    light.add(l);
                    click.add(c);
                }

                @Override
                public void onError(Exception e) {
                    if (stopASAP) {
                        return;
                    }
                    stopASAP = true;
                    InputLagTest.this.interrupt();
                    InputLagTest.this.onError(e);
                }
            });
            while (!light.isFilled()) {
                if (stopASAP) {
                    throw new IgnorableException();
                }
                if (escPressed) {
                    throw new TestException(TestException.USER_ABORT);
                }
                Utils.sleep(1);
            }
            d.endCurrentActivity();
            ts.setFlashOnClick(false);
            if (stopASAP) {
                throw new IgnorableException();
            }
            if (escPressed) {
                throw new TestException(TestException.USER_ABORT);
            }
            int[] samples = light.getData(), clickSamples = click.getData();
            double max = 0, whiteThreshold, blackThreshold, range;
            for (int i : samples) {
                if (i > max) {
                    max = i;
                }
            }
            range = max - black;
            if (range < 32) {
                throw new TestException(TestException.INSUFFICIENT_CONTRAST);
            }
            whiteThreshold = black + range * 0.3f; //above this it's considered white
            blackThreshold = black + range * 0.7f; //below this it's considered black
            ArrayList<Integer> transitions = new ArrayList<>(), clicks = new ArrayList<>();
            byte state = -1;
            for (int i = 0; i < samples.length; i++) {
                switch (state) {
                    case -1:
                        //-1=waiting for first click
                        if (clickSamples[i] == 1 && samples[i] <= blackThreshold) {
                            clicks.add(i);
                            state = 0;
                        }
                        break;
                    case 0:
                        //0=black, waiting for white
                        if (clickSamples[i] == 1) {
                            clicks.add(i);
                        }
                        if (samples[i - 1] < whiteThreshold && samples[i] >= whiteThreshold) {
                            transitions.add(i);
                            state = 1;
                        }
                        break;
                    case 1:
                        //1=white, waiting for black
                        if (clickSamples[i] == 1) {
                            clicks.add(i);
                        }
                        if (samples[i - 1] > blackThreshold && samples[i] <= blackThreshold) {
                            state = 0;
                        }
                        break;
                }
            }
            ArrayList<Double> latencyTimes = new ArrayList<>();
            int j = 0;
            for (int i = 0; i < clicks.size(); i++) {
                while (j < transitions.size()) {
                    if (transitions.get(j) > clicks.get(i)) {
                        for (int k = i; k < clicks.size(); k++) {
                            if (clicks.get(k) < transitions.get(j)) {
                                i = k; //some flash was skipped, k is a better candidate click for this transition
                            }
                        }
                        double delay = 1000.0 * (double) (transitions.get(j) - clicks.get(i)) / sampleRate;
                        latencyTimes.add(delay);
                        j++;
                        break;
                    } else {
                        j++;
                    }
                }
            }
            if (latencyTimes.isEmpty()) {
                throw new TestException(TestException.ANALYSIS_FAILED);
            }
            ret.put("times", latencyTimes.toArray(new Double[0]));
            double percentileL, percentile50, percentileH;
            Collections.sort(latencyTimes);
            percentileL = latencyTimes.get((int) (latencyTimes.size() * 0.33));
            percentile50 = latencyTimes.get((int) (latencyTimes.size() * 0.5));
            percentileH = latencyTimes.get((int) (latencyTimes.size() * 0.66));
            ret.put("percentileL", percentileL);
            ret.put("percentile50", percentile50);
            ret.put("percentileH", percentileH);
            ret.put("distribution", latencyTimes.toArray(new Double[0]));
            ts.close();
            onDone(ret);
        } catch (Exception ex) {
            try {
                d.endCurrentActivity();
            } catch (Throwable t) {
            }
            ts.close();
            if (!(ex instanceof IgnorableException)) {
                onError(ex);
            }
        }
    }

    @Override
    public void begin() {
        start();
    }

    @Override
    public void cancel() {
        stopASAP = true;
        this.interrupt();
    }

}
