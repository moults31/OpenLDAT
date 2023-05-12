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
package com.dosse.openldat.processing.filters;

import com.dosse.openldat.processing.buffers.CircularBuffer;

/**
 *
 * @author dosse
 */
public class PeakHoldFilter extends CircularBuffer {

    private CircularBuffer window;

    public PeakHoldFilter(int size, int peakWindowSize) {
        super(size);
        window = new CircularBuffer(peakWindowSize);
    }

    @Override
    public void add(int[] data) {
        int[] processed=new int[data.length];
        synchronized (this) {
            for (int i = 0; i < data.length; i++) {
                window.add(data[i]);
                int max = 0;
                for (int d : window.getInternalBuffer()) {
                    if (d > max) {
                        max = d;
                    }
                }
                processed[i] = max;
            }
            super.add(processed);
        }
    }

    @Override
    public void add(int val) {
        synchronized (this) {
            window.add(val);
            int max = 0;
            for (int i : window.getInternalBuffer()) {
                if (i > max) {
                    max = i;
                }
            }
            super.add(max);
        }
    }

    public static int findBestWindowSize(int[] noisyData, int min, int max, int stepSize, int noiseThreshold) {
        if (min < 2) {
            min = 2;
        }
        if (stepSize < 1) {
            stepSize = 1;
        }
        if (max < min) {
            return -1;
        }
        for (int w = min; w <= max; w += stepSize) {
            PeakHoldFilter f = new PeakHoldFilter(noisyData.length, w);
            f.add(noisyData);
            int[] filtered = f.getData();
            int fMin = Integer.MAX_VALUE, fMax = Integer.MIN_VALUE;
            for (int i = w; i < filtered.length; i++) {
                if (filtered[i] < fMin) {
                    fMin = filtered[i];
                }
                if (filtered[i] > fMax) {
                    fMax = filtered[i];
                }
            }
            if (fMax - fMin <= noiseThreshold) {
                return w;
            }
        }
        return -1;
    }

}
