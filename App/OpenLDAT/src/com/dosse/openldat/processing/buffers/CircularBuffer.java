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
package com.dosse.openldat.processing.buffers;

import java.util.Arrays;

/**
 *
 * @author dosse
 */
public class CircularBuffer implements IBuffer {

    private final int[] buffer;
    private int pos = 0;
    private long added = 0;

    public CircularBuffer(int size) {
        buffer = new int[size];
    }

    @Override
    public void add(int val) {
        synchronized (this) {
            buffer[pos] = val;
            pos = (pos + 1) % buffer.length;
            added++;
        }
    }

    @Override
    public void add(int[] data) {
        synchronized (this) {
            if (data.length < buffer.length) {
                if (pos + data.length < buffer.length) {
                    System.arraycopy(data, 0, buffer, pos, data.length);
                    pos = (pos + data.length) % buffer.length;
                } else {
                    System.arraycopy(data, 0, buffer, pos, buffer.length - pos);
                    System.arraycopy(data, buffer.length - pos, buffer, 0, data.length - buffer.length + pos);
                    pos = (data.length - buffer.length + pos) % buffer.length;
                }
            } else {
                System.arraycopy(data, data.length - buffer.length, buffer, 0, buffer.length);
                pos = 0;
            }
            added += data.length;
        }
    }

    @Override
    public int[] getData() {
        synchronized(this){
            if (pos != 0) {
                int[] ret = new int[buffer.length];
                System.arraycopy(buffer, pos, ret, 0, buffer.length - pos);
                System.arraycopy(buffer, 0, ret, buffer.length - pos, pos);
                return ret;
            } else {
                return Arrays.copyOf(buffer, buffer.length);
            }
        }
    }

    @Override
    public int[] getDataUnsafe() {
        //this operation is not supported on circular buffers so we use the regular copy instead
        return getData();
    }
    
    public int[] getInternalBuffer(){
        return buffer;
    }

    @Override
    public int getSize() {
        return buffer.length;
    }

    @Override
    public boolean isFilled() {
        return added >= buffer.length;
    }

}
