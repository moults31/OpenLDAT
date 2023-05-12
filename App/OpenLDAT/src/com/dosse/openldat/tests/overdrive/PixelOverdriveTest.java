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
package com.dosse.openldat.tests.overdrive;

import com.dosse.openldat.Config;
import com.dosse.openldat.Utils;
import com.dosse.openldat.device.Device;
import com.dosse.openldat.device.callbacks.LightSensorMonitorCallback;
import com.dosse.openldat.processing.buffers.CircularBuffer;
import com.dosse.openldat.processing.buffers.IBuffer;
import com.dosse.openldat.processing.filters.PeakHoldFilter;
import com.dosse.openldat.tests.ITest;
import com.dosse.openldat.tests.IgnorableException;
import com.dosse.openldat.tests.TestException;
import com.dosse.openldat.tests.testscreen.ITestScreen;
import com.dosse.openldat.tests.testscreen.opengl.TestScreenGL;
import com.dosse.openldat.tests.testscreen.swing.TestScreenSwing;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 *
 * @author dosse
 */
public abstract class PixelOverdriveTest extends Thread implements ITest {

    private final Device d;
    private final ITestScreen ts;
    private boolean enterPressed, escPressed, stopASAP = false;
    private static final boolean unbuffered = true, fastADC = true;
    private ArrayList<int[]> steps = new ArrayList<>();
    private boolean skipTo0And255 = false;
    private int method = METHOD_RELATIVE;

    public static final int METHOD_RELATIVE = 0, METHOD_ABSOLUTE = 1;

    public PixelOverdriveTest(Device d, int step, boolean skipTo0And255, int method) {
        this.d = d;
        this.skipTo0And255 = skipTo0And255;
        if (method < 0) {
            method = METHOD_RELATIVE;
        }
        this.method = method;
        int i = 0;
        while (true) {
            steps.add(new int[]{i, 3, 0, 0});
            if (i == 255) {
                break;
            }
            i += step;
            if (i > 255) {
                i = 255;
            }
        }
        if (Config.TESTSCREEN_GL) {
            ts = new TestScreenGL(TestScreenGL.VSYNC_ON) {
                @Override
                public void onEnterPressed() {
                    enterPressed = true;
                }

                @Override
                public void onCancel() {
                    escPressed = true;
                    PixelOverdriveTest.this.interrupt();
                }

                @Override
                public void onError(Exception e) {
                    if (stopASAP) {
                        return;
                    }
                    stopASAP = true;
                    PixelOverdriveTest.this.interrupt();
                    PixelOverdriveTest.this.onError(e);
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
                    PixelOverdriveTest.this.interrupt();
                }

                @Override
                public void onError(Exception e) {
                    if (stopASAP) {
                        return;
                    }
                    stopASAP = true;
                    PixelOverdriveTest.this.interrupt();
                    PixelOverdriveTest.this.onError(e);
                }
            };
        }
    }

    private int[] lastShootRawData = null;

    private double[] shootMinMaxAvg(byte sensitivity, boolean absolute) {
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
                    PixelOverdriveTest.this.interrupt();
                    PixelOverdriveTest.this.onError(e);
                }
            });
        } catch (Throwable t) {
            return new double[]{-1, -1, -1};
        }
        while (!b.isFilled()) {
            if (stopASAP || escPressed) {
                return new double[]{-1, -1, -1};
            }
            Utils.sleep(10);
        }
        if (stopASAP || escPressed) {
            return new double[]{-1, -1, -1};
        }
        int[] data = b.getData();
        lastShootRawData = Arrays.copyOf(data, data.length);
        Arrays.sort(data);
        double avg = 0;
        for (int i : data) {
            avg += i;
        }
        avg /= (double) data.length;
        return new double[]{data[absolute ? 0 : (int) (data.length * 0.05)], data[absolute ? data.length - 1 : (int) (data.length * 0.95)], avg};
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
                throw new IgnorableException();
            }
            if (escPressed) {
                throw new TestException(TestException.USER_ABORT);
            }
            enterPressed = false;
            ts.hideTarget();
            double[] gain = new double[]{1, 1.258, 2.101, 13.883};
            byte sensitivity = 3;
            boolean flickeringDetected = false;
            int[] noisiestSample = null;
            double maxNoise = 0;
            for (int[] step : steps) {
                float l = (float) step[0] / 255f;
                ts.setColor(l, l, l);
                Utils.sleep(500);
                while (true) {
                    if (stopASAP) {
                        throw new IgnorableException();
                    }
                    if (escPressed) {
                        throw new TestException(TestException.USER_ABORT);
                    }
                    double[] white = shootMinMaxAvg((byte) sensitivity, true);
                    if (white[1] > 650 && sensitivity > 0) {
                        sensitivity--;
                    } else {
                        double noise = white[1] - white[0];
                        if (noise >= 16) {
                            flickeringDetected = true;
                            if (noise >= maxNoise) {
                                noisiestSample = lastShootRawData;
                                maxNoise = noise;
                            }
                        }
                        step[1] = sensitivity;
                        step[2] = (int) white[2];
                        step[3] = (int) white[1];
                        break;
                    }
                }
            }
            if (stopASAP) {
                throw new IgnorableException();
            }
            if (escPressed) {
                throw new TestException(TestException.USER_ABORT);
            }
            if (steps.size() >= 8 && !flickeringDetected) {
                for (int s = 3; s >= 1; s--) {
                    int[] bestStep = null;
                    for (int[] step : steps) {
                        if (step[1] >= s && (bestStep == null || step[0] > bestStep[0])) {
                            bestStep = step;
                        }
                    }
                    if (bestStep != null) {
                        float l = (float) bestStep[0] / 255f;
                        if (ts.setColor(l, l, l)) {
                            Utils.sleep(500);
                        }
                        if (stopASAP) {
                            throw new IgnorableException();
                        }
                        if (escPressed) {
                            throw new TestException(TestException.USER_ABORT);
                        }
                        int[] levels = new int[4];
                        for (int i = 0; i <= s; i++) {
                            levels[i] = (int) shootMinMaxAvg((byte) i, true)[2];
                            if (stopASAP) {
                                throw new IgnorableException();
                            }
                            if (escPressed) {
                                throw new TestException(TestException.USER_ABORT);
                            }
                        }
                        for (int i = 1; i <= s; i++) {
                            gain[i] = (double) levels[i] / (double) levels[0];
                        }
                    }
                }
            }
            double sampleRate = d.getLightSensorMonitorModeSampleRate(unbuffered, fastADC);
            int peakHoldFilterWindowSize = -1;
            if (flickeringDetected) {
                peakHoldFilterWindowSize = PeakHoldFilter.findBestWindowSize(noisiestSample, (int) (sampleRate * 0.001), (int) (sampleRate * 0.02), (int) (sampleRate * 0.00011), 16);
                if (peakHoldFilterWindowSize == -1) {
                    peakHoldFilterWindowSize = (int) (sampleRate * 0.0085);
                    System.err.println("WARNING: Unable to determine best filtering parameters, using default");
                }
            }
            HashMap<String, Object> ret = new HashMap<>();
            ArrayList<String> keys = new ArrayList<>();
            ret.put("flickeringDetected", flickeringDetected);
            for (int[] from : steps) {
                for (int[] to : steps) {
                    float lFrom = ((float) from[0]) / 255f, lTo = ((float) to[0]) / 255f;
                    if (lFrom == lTo) {
                        continue;
                    }
                    if (skipTo0And255 && (lTo == 0 || lTo == 1)) {
                        continue;
                    }
                    ts.setColor(lFrom, lFrom, lFrom);
                    Utils.sleep(500);
                    if (stopASAP) {
                        throw new IgnorableException();
                    }
                    if (escPressed) {
                        throw new TestException(TestException.USER_ABORT);
                    }
                    sensitivity = (byte) to[1];
                    IBuffer transition;
                    if (flickeringDetected) {
                        transition = new PeakHoldFilter((int) (sampleRate * 0.6), peakHoldFilterWindowSize);
                    } else {
                        transition = new CircularBuffer((int) (sampleRate * 0.6));
                    }
                    d.lightSensorMonitorMode(unbuffered, sensitivity, fastADC, new LightSensorMonitorCallback() {
                        @Override
                        public void onDataBufferReceived(int[] data) {
                            if (!transition.isFilled()) {
                                transition.add(data);
                            }
                        }

                        @Override
                        public void onDataSampleReceived(int data) {
                            if (!transition.isFilled()) {
                                transition.add(data);
                            }
                        }

                        @Override
                        public void onError(Exception e) {
                            if (stopASAP) {
                                return;
                            }
                            stopASAP = true;
                            PixelOverdriveTest.this.interrupt();
                            PixelOverdriveTest.this.onError(e);
                        }

                    });
                    Utils.sleep(50); //short sleep to make sure we get a shot of the initial brightness
                    ts.setColor(lTo, lTo, lTo);
                    while (!transition.isFilled()) {
                        if (stopASAP) {
                            throw new IgnorableException();
                        }
                        if (escPressed) {
                            throw new TestException(TestException.USER_ABORT);
                        }
                        Utils.sleep(1);
                    }
                    int[] samples = transition.getData();
                    /*DataOutputStream fos = new DataOutputStream(new FileOutputStream("t" + from[0] + "-" + to[0]));
                    for (int i : samples) {
                        fos.writeInt(i);
                    }
                    fos.flush();
                    fos.close();*/
                    int endL = flickeringDetected ? to[3] : to[2];
                    double absoluteFromL, absoluteToL;
                    switch (method) {
                        case METHOD_RELATIVE:
                            absoluteFromL = (flickeringDetected ? from[3] : from[2]) / gain[from[1]];
                            absoluteToL = (flickeringDetected ? to[3] : to[2]) / gain[to[1]];
                            break;
                        case METHOD_ABSOLUTE:
                            int[] black = steps.get(0),
                             white = steps.get(steps.size() - 1);
                            if (lFrom > lTo) {
                                absoluteToL = (flickeringDetected ? black[3] : black[2]) / gain[black[1]];
                                absoluteFromL = (flickeringDetected ? white[3] : white[2]) / gain[white[1]];
                            } else {
                                absoluteFromL = (flickeringDetected ? black[3] : black[2]) / gain[black[1]];
                                absoluteToL = (flickeringDetected ? white[3] : white[2]) / gain[white[1]];
                            }
                            break;
                        default:
                            throw new TestException(TestException.INVALID_SETTINGS);
                    }
                    int transitionI = -1;
                    Arrays.sort(samples);
                    String key = "e" + from[0] + ">" + to[0];
                    keys.add(key);
                    for (int i = 1; i < samples.length; i++) {
                            if (samples[i] >= endL && samples[i - 1] < endL) {
                                transitionI = i;
                                break;
                            }
                        }
                        if (transitionI == -1) { //did not overshoot/undershoot
                            ret.put(key, 0.0);
                            continue;
                        }
                    if (lFrom > lTo) {
                        samples = Arrays.copyOfRange(samples, 0, transitionI);
                    } else {
                        samples = Arrays.copyOfRange(samples, transitionI, samples.length);
                    }
                    if (lFrom > lTo) {
                        int undershoot = samples.length == 0 ? 0 : (endL - samples[(int) (samples.length * 0.001)]);
                        double absoluteRange = absoluteFromL - absoluteToL;
                        double absoluteUndershoot = undershoot / gain[sensitivity];
                        ret.put(key, 100 * (double) absoluteUndershoot / (double) absoluteRange);
                    } else {
                        int overshoot = samples.length == 0 ? 0 : (samples[(int) (samples.length * 0.999)] - endL);
                        double absoluteRange = absoluteToL - absoluteFromL;
                        double absoluteOvershoot = overshoot / gain[sensitivity];
                        ret.put(key, 100 * (double) absoluteOvershoot / (double) absoluteRange);
                    }
                }
            }
            d.endCurrentActivity();
            if (stopASAP) {
                throw new IgnorableException();
            }
            if (escPressed) {
                throw new TestException(TestException.USER_ABORT);
            }
            ret.put("order", keys.toArray(new String[0]));
            int[] lSteps = new int[steps.size()];
            for (int i = 0; i < steps.size(); i++) {
                lSteps[i] = steps.get(i)[0];
            }
            ret.put("steps", lSteps);
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
