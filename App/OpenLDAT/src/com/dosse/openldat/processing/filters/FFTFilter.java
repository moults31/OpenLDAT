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
import org.jtransforms.fft.FloatFFT_1D;

/**
 *
 * @author dosse
 */
public class FFTFilter extends CircularBuffer {

    private final float[] fdata;
    private final float[] window;
    
    public FFTFilter(int size) {
        super(size);
        fdata=new float[size];
        window=new float[size];
        //precompute blackman-harris window for this size
        for(int i=0;i<size;i++){
            window[i]=(float) (0.35875-0.48829*Math.cos((2*Math.PI*i)/(double)size)+0.14128*Math.cos((4*Math.PI*i)/(double)size)-0.01168*Math.cos((6*Math.PI*i)/(double)size));
        }
    }

    @Override
    public int[] getData() {
        int[] data = super.getData();
        FloatFFT_1D fft = new FloatFFT_1D(data.length);
        for (int i = 0; i < data.length; i++) {
            fdata[i] = window[i]*data[i];
        }
        fft.realForward(fdata);
        for (int i = 0; i < data.length; i++) {
            data[i] = (int) (fdata[i]>=0?fdata[i]:-fdata[i]);
        }
        return data;
    }
    
    public int[] getOriginalData(){
        return super.getData();
    }
    
}
