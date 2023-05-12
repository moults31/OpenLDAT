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
package com.dosse.openldat.ui.chart;

import java.awt.Color;

/**
 *
 * @author dosse
 */
public class ConstantEnvelope {
    protected final double[] xData, yData;
    protected double min,max;
    protected Color color;

    public ConstantEnvelope(double[] xData, double[] yData, double min, double max, Color color) {
        this.xData=xData;
        this.yData=yData;
        this.min = min;
        this.max = max;
        this.color = color;
    }
    
}
