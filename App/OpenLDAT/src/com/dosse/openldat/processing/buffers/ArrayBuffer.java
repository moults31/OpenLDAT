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
public class ArrayBuffer implements IBuffer{
    
    private final int[] data;

    public ArrayBuffer(int[] data) {
        this.data = data;
    }

    @Override
    public void add(int val) {
        throw new UnsupportedOperationException("Constant buffers cannot be altered");
    }

    @Override
    public void add(int[] data) {
        throw new UnsupportedOperationException("Constant buffers cannot be altered");
    }

    @Override
    public int[] getData() {
        return Arrays.copyOf(data, data.length);
    }
    
    @Override
    public int[] getDataUnsafe() {
        return data;
    }

    @Override
    public int getSize() {
        return data.length;
    }

    @Override
    public boolean isFilled() {
        return true;
    }
    
}
