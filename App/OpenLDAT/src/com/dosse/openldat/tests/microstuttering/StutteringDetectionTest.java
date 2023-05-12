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
package com.dosse.openldat.tests.microstuttering;

import com.dosse.openldat.Config;
import com.dosse.openldat.Utils;
import com.dosse.openldat.device.Device;
import com.dosse.openldat.device.callbacks.LightSensorMonitorCallback;
import com.dosse.openldat.processing.buffers.CircularBuffer;
import com.dosse.openldat.processing.buffers.IBuffer;
import com.dosse.openldat.processing.filters.RunningAverageSmoothingFilter;
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
public abstract class StutteringDetectionTest extends Thread implements ITest {

    private final Device d;
    private final ITestScreen ts;
    private boolean enterPressed, escPressed, stopASAP = false;
    private int durationMs;
    private static final boolean unbuffered = true, fastADC = false;

    public StutteringDetectionTest(Device d, int durationMs) {
        this.d = d;
        this.durationMs = durationMs;
        if (Config.TESTSCREEN_GL) {
            ts = new TestScreenGL(TestScreenGL.VSYNC_ON) {
                @Override
                public void onEnterPressed() {
                    enterPressed = true;
                }

                @Override
                public void onCancel() {
                    escPressed = true;
                    StutteringDetectionTest.this.interrupt();
                }

                @Override
                public void onError(Exception e) {
                    if (stopASAP) {
                        return;
                    }
                    stopASAP = true;
                    StutteringDetectionTest.this.interrupt();
                    StutteringDetectionTest.this.onError(e);
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
                    StutteringDetectionTest.this.interrupt();
                }

                @Override
                public void onError(Exception e) {
                    if (stopASAP) {
                        return;
                    }
                    stopASAP = true;
                    StutteringDetectionTest.this.interrupt();
                    StutteringDetectionTest.this.onError(e);
                }
            };
        }
    }

    private double[] shootMinMax(byte sensitivity, boolean absolute) {
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
                    StutteringDetectionTest.this.interrupt();
                    StutteringDetectionTest.this.onError(e);
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
        if (stopASAP || escPressed) {
            return new double[]{-1, -1};
        }
        int[] data = b.getData();
        Arrays.sort(data);
        return new double[]{data[absolute ? 0 : (int) (data.length * 0.05)], data[absolute ? data.length - 1 : (int) (data.length * 0.95)]};
    }

    @Override
    public void run() {
        try {
            ts.setColor(1, 1, 1);
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
            byte sensitivity = 3;
            boolean flickeringDetected = false;
            ts.setColor(1, 1, 1);
            ts.hideTarget();
            Utils.sleep(500);
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
                    if (white[1] - white[0] > 16) {
                        flickeringDetected = true;
                    }
                    break;
                }
            }
            if (stopASAP) {
                throw new IgnorableException();
            }
            if (escPressed) {
                throw new TestException(TestException.USER_ABORT);
            }
            HashMap<String, Object> ret = new HashMap<>();
            ret.put("flickeringDetected", flickeringDetected);
            IBuffer f;
            int bSize = (int) ((d.getLightSensorMonitorModeSampleRate(unbuffered, fastADC) * (float) durationMs / 1000.0));
            if (flickeringDetected) {
                f = new RunningAverageSmoothingFilter(bSize, fastADC ? 0.996 : 0.99);
            } else {
                f = new CircularBuffer(bSize);
            }
            ts.setFlicker(true);
            double sampleRate = d.lightSensorMonitorMode(unbuffered, sensitivity, fastADC, new LightSensorMonitorCallback() {
                @Override
                public void onDataBufferReceived(int[] data) {
                    f.add(data);
                }

                @Override
                public void onDataSampleReceived(int data) {
                    f.add(data);
                }

                @Override
                public void onError(Exception e) {
                    if (stopASAP) {
                        return;
                    }
                    stopASAP = true;
                    StutteringDetectionTest.this.interrupt();
                    StutteringDetectionTest.this.onError(e);
                }
            });
            while (!f.isFilled()) {
                if (stopASAP) {
                    throw new IgnorableException();
                }
                if (escPressed) {
                    throw new TestException(TestException.USER_ABORT);
                }
                Utils.sleep(1);
            }
            Utils.sleep(300); //short sleep to make sure we don't get a shot of the brightness from before the test started
            d.endCurrentActivity();
            ts.setFlicker(false);
            ts.setColor(0, 0, 0);
            if (stopASAP) {
                throw new IgnorableException();
            }
            if (escPressed) {
                throw new TestException(TestException.USER_ABORT);
            }
            int[] samples = f.getData();
            double max = 0, min = 1023, whiteThreshold, blackThreshold, range;
            for (int i : samples) {
                if (i > max) {
                    max = i;
                }
                if (i < min) {
                    min = i;
                }
            }
            range = max - min;
            if (range < 32) {
                throw new TestException(TestException.INSUFFICIENT_CONTRAST);
            }
            for (int i = 0; i < samples.length; i++) {
                samples[i] = (int) (1023 * ((double) (samples[i] - min) / range));
            }
            /*DataOutputStream fos = new DataOutputStream(new FileOutputStream("microstuttering.dat"));
            for (int i : samples) {
                fos.writeInt(i<<21);
            }
            fos.flush();
            fos.close();*/
            int[] sortedSamples=Arrays.copyOf(samples, samples.length);
            Arrays.sort(sortedSamples);
            whiteThreshold = sortedSamples[(int)(sortedSamples.length*0.4)]; //above this it's considered white
            blackThreshold = sortedSamples[(int)(sortedSamples.length*0.6)]; //below this it's considered black
            ArrayList<Integer> transitions = new ArrayList<>();
            byte state = 0;
            for (int i = 1; i < samples.length; i++) {
                if (state == 0) { //0=black, waiting for white
                    if (samples[i - 1] < whiteThreshold && samples[i] >= whiteThreshold) {
                        transitions.add(i);
                        state = 1;
                    }
                } else if (state == 1) { //1=white, waiting for black
                    if (samples[i - 1] > blackThreshold && samples[i] <= blackThreshold) {
                        state = 0;
                    }
                }
            }
            ArrayList<Double> frameTimes = new ArrayList<>();
            for (int i = 1; i < transitions.size(); i++) {
                frameTimes.add(1000.0 * (double) (transitions.get(i) - transitions.get(i - 1)) / sampleRate);
            }
            if (frameTimes.isEmpty()) {
                throw new TestException(TestException.ANALYSIS_FAILED);
            }
            ret.put("frameTimes", frameTimes.toArray(new Double[0]));
            double percentile1, percentile50, percentile99;
            Collections.sort(frameTimes);
            percentile1 = frameTimes.get((int) (frameTimes.size() * 0.01));
            percentile50 = frameTimes.get((int) (frameTimes.size() * 0.5));
            percentile99 = frameTimes.get((int) (frameTimes.size() * 0.99));
            ret.put("percentile1", percentile1);
            ret.put("percentile50", percentile50);
            ret.put("percentile99", percentile99);
            int stutters = 0;
            double stutteringThreshold = (1000.0 / (double) ts.getRefreshRate()) * 2 * 1.1;
            for (double i : frameTimes) {
                if (i > stutteringThreshold) {
                    stutters++;
                }
            }
            ret.put("stutteringThreshold", stutteringThreshold);
            ret.put("stutters", stutters);
            ts.close();
            onDone(ret);
        } catch (Exception ex) {
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
