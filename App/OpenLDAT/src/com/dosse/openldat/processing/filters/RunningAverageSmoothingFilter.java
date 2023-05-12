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
public class RunningAverageSmoothingFilter extends CircularBuffer {

    private final double f;
    private double currentValue = 0;
    private boolean first = true;

    public RunningAverageSmoothingFilter(int size, double smoothing) {
        super(size);
        this.f = smoothing;
    }

    @Override
    public void add(int[] data) {
        int[] processed=new int[data.length];
        synchronized (this) {
            if (first) {
                currentValue = data[0];
                first = false;
            }
            for (int i = 0; i < data.length; i++) {
                currentValue = (double) data[i] * (1 - f) + currentValue * f;
                processed[i] = (int) currentValue;
            }
            super.add(processed);
        }
    }

    @Override
    public void add(int val) {
        synchronized (this) {
            if (first) {
                currentValue = val;
                first = false;
                super.add(val);
            } else {
                currentValue = (double) val * (1 - f) + currentValue * f;
                super.add((int) currentValue);
            }
        }
    }

}
