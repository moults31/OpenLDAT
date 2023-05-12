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
package com.dosse.openldat.tests.pixelresponse;

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
public abstract class PixelResponseTimeTest extends Thread implements ITest {

    private final Device d;
    private final ITestScreen ts;
    private boolean enterPressed, escPressed, stopASAP = false;
    private static final boolean unbuffered = true, fastADC = true;
    private ArrayList<int[]> steps = new ArrayList<>();
    private float th1, th2;

    public PixelResponseTimeTest(Device d, int step, float th1, float th2) {
        this.d = d;
        //default thresholds: use vesa standard
        if (th1 < 0) {
            th1 = 0.1f;
        }
        if (th2 < 0) {
            th2 = 0.9f;
        }
        this.th1 = th1;
        this.th2 = th2;
        int i = 0;
        while (true) {
            steps.add(new int[]{i, 3});
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
                    PixelResponseTimeTest.this.interrupt();
                }

                @Override
                public void onError(Exception e) {
                    if (stopASAP) {
                        return;
                    }
                    stopASAP = true;
                    PixelResponseTimeTest.this.interrupt();
                    PixelResponseTimeTest.this.onError(e);
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
                    PixelResponseTimeTest.this.interrupt();
                }

                @Override
                public void onError(Exception e) {
                    if (stopASAP) {
                        return;
                    }
                    stopASAP = true;
                    PixelResponseTimeTest.this.interrupt();
                    PixelResponseTimeTest.this.onError(e);
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
                    PixelResponseTimeTest.this.interrupt();
                    PixelResponseTimeTest.this.onError(e);
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
            boolean flickeringDetected = false;
            byte sensitivity = 3;
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
                        break;
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
                    ts.setColor(lFrom, lFrom, lFrom);
                    Utils.sleep(500);
                    if (stopASAP) {
                        throw new IgnorableException();
                    }
                    if (escPressed) {
                        throw new TestException(TestException.USER_ABORT);
                    }
                    sensitivity = (byte) (to[1] < from[1] ? to[1] : from[1]);
                    int startL = (int) shootMinMaxAvg(sensitivity, flickeringDetected)[flickeringDetected ? 1 : 2];
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
                            PixelResponseTimeTest.this.interrupt();
                            PixelResponseTimeTest.this.onError(e);
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
                    int endL = (int) shootMinMaxAvg(sensitivity, flickeringDetected)[flickeringDetected ? 1 : 2];
                    int[] samples = transition.getData();
                    /*DataOutputStream fos = new DataOutputStream(new FileOutputStream("t" + from[0] + "-" + to[0]));
                    for (int i : samples) {
                        fos.writeInt(i);
                    }
                    fos.flush();
                    fos.close();*/
                    byte state = 0;
                    int transitionStart = 0, transitionEnd = 0;
                    if (lFrom < lTo) { //from dark to light, flip it so it becomes light to dark and we can reuse the same algorithm
                        int temp;
                        for (int i = 0; i < samples.length / 2; i++) {
                            temp = samples[i];
                            samples[i] = samples[samples.length - i - 1];
                            samples[samples.length - i - 1] = temp;
                        }
                        temp = startL;
                        startL = endL;
                        endL = temp;
                    }
                    int range = startL - endL;
                    for (int i = 0; i < samples.length; i++) {
                        if (state == 0) {
                            if (samples[i] - endL <= range * th2) {
                                transitionStart = i;
                                state = 1;
                            }
                        } else if (state == 1) {
                            if (samples[i] - endL > range * th2) { //false trigger
                                transitionStart = 0;
                                transitionEnd = 0;
                                state = 0;
                            } else if (samples[i] - endL <= range * th1 && transitionEnd == 0) {
                                transitionEnd = i;
                            }
                        }
                    }
                    String key = "t" + from[0] + ">" + to[0];
                    keys.add(key);
                    if (transitionEnd < transitionStart) { //end threshold was never reached, probably because of noise
                        ret.put(key, Double.POSITIVE_INFINITY);
                    } else {
                        ret.put(key, 1000 * ((double) (transitionEnd - transitionStart)) / sampleRate);
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
