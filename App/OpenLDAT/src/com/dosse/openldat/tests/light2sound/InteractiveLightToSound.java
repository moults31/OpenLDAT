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
package com.dosse.openldat.tests.light2sound;

import com.dosse.openldat.device.Device;
import com.dosse.openldat.device.callbacks.LightSensorMonitorCallback;
import com.dosse.openldat.device.errors.MissingSensorException;
import com.dosse.openldat.processing.buffers.CircularBuffer;
import com.dosse.openldat.processing.buffers.IBuffer;
import com.dosse.openldat.processing.filters.FFTFilter;
import com.dosse.openldat.tests.ITest;
import java.io.IOException;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;

/**
 *
 * @author dosse
 */
public abstract class InteractiveLightToSound implements ITest {

    private Device d;
    private byte sensitivity = 2;
    private SourceDataLine speaker;
    private double sampleRate;

    private final boolean fastADC = true, unbuffered = false;

    private IBuffer chartBuffer;
    private FFTFilter fft;

    private LightSensorMonitorCallback callback;

    private int volMul = 32;

    public InteractiveLightToSound(Device d) throws MissingSensorException, IOException {
        this.d = d;
        sampleRate = d.getLightSensorMonitorModeSampleRate(unbuffered, fastADC);
        try {
            chartBuffer = new CircularBuffer((int) (sampleRate * 0.5));
            int fftSize = (int) (sampleRate * 0.5);
            fftSize = (int) Math.pow(2, Math.ceil(Math.log(fftSize) / Math.log(2)));
            fft = new FFTFilter(fftSize);
            AudioFormat af = new AudioFormat((float) sampleRate, 16, 1, true, false); //16 bit, big endian, signed
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, af);
            speaker = (SourceDataLine) AudioSystem.getLine(info);
            speaker.open(af, 8192);
            speaker.start();
        } catch (Throwable t) {
            throw new IOException("Failed to open sound device");
        }
        callback = new LightSensorMonitorCallback() {
            @Override
            public void onDataBufferReceived(int[] data) {
                chartBuffer.add(data);
                fft.add(data);
                byte[] buf = new byte[data.length * 2];
                for (int i = 0; i < data.length; i++) {
                    data[i] *= volMul;
                    buf[2 * i + 1] = (byte) ((data[i] >> 8) & 0xFF);
                    buf[2 * i] = (byte) (data[i] & 0xFF);
                }
                speaker.write(buf, 0, buf.length);
            }

            private byte[] singleSampleBuf = new byte[2];

            @Override
            public void onDataSampleReceived(int data) {
                chartBuffer.add(data);
                fft.add(data);
                data *= volMul;
                singleSampleBuf[1] = (byte) ((data >> 8) & 0xFF);
                singleSampleBuf[0] = (byte) (data & 0xFF);
                speaker.write(singleSampleBuf, 0, 2);
            }

            @Override
            public void onError(Exception e) {
                try {
                    speaker.stop();
                    speaker.close();
                } catch (Throwable t) {
                }
                InteractiveLightToSound.this.onError(e);
            }
        };
    }

    public void setSensitivity(byte sensitivity) {
        if (sensitivity == this.sensitivity) {
            return;
        }
        this.sensitivity = sensitivity > 3 ? 3 : sensitivity < 0 ? 0 : sensitivity;
        begin();
    }

    public byte getSensitivity() {
        return sensitivity;
    }

    public void setVolume(int volume) {
        volMul = volume;
    }

    public int getVolume() {
        return volMul;
    }

    public IBuffer getChartBuffer() {
        return chartBuffer;
    }

    public double getStrongestFrequency(double from, double to) {
        int[] bins = fft.getData();
        double ret = -1;
        int max = 0;
        for (int i = 0; i < bins.length; i++) {
            double freq = ((double) i / (double) bins.length) * (sampleRate / 2.0);
            if (freq >= from && freq <= to && bins[i] > max) {
                max = bins[i];
                ret = freq;
            }
        }
        if (max < bins.length * 0.15) {
            return -1;
        } else {
            return ret;
        }
    }

    public double getSampleRate() {
        return sampleRate;
    }

    @Override
    public void begin() {
        try {
            speaker.stop();
            d.lightSensorMonitorMode(unbuffered, sensitivity, fastADC, callback);
            speaker.start();
        } catch (Exception ex) {
            try {
                speaker.stop();
                speaker.close();
            } catch (Throwable t) {
            }
            onError(ex);
        }
    }

    @Override
    public void cancel() {
        d.endCurrentActivity();
        speaker.stop();
        speaker.close();
        onDone(null);
    }

}
