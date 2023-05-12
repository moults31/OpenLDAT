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

import com.dosse.openldat.Config;
import com.dosse.openldat.Utils;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import javax.swing.JPanel;

/**
 *
 * @author dosse
 */
public class Chart extends JPanel {

    private ArrayList<Channel> channels = new ArrayList<>();
    private ArrayList<ConstantEnvelope> envelopes = new ArrayList<>();

    public Color BACKGROUND = new Color(24, 24, 24), GRID = new Color(96, 96, 96), SCALE = new Color(255, 255, 255);

    private boolean xScale = false, yScale = false;
    private double xScaleFrom = 0, xScaleTo = 1000, xScaleStep = 200, yScaleFrom = 0, yScaleTo = 1023, yScaleStep = 100, scaleFontSize;
    private int xScaleDigits = 0, yScaleDigits = 0;
    private String xUnit = "", yUnit = "";
    private Font scaleFont;

    public Chart() {
        this(Utils.getDPIScaling());
    }
    
    public Chart(float dpiScale){
        super();
        scaleFontSize = Config.FONT_SIZE * dpiScale;
        scaleFont = new Font(Font.MONOSPACED, Font.PLAIN, (int) scaleFontSize);
    }

    public void addChannel(Channel c) {
        synchronized (channels) {
            channels.add(c);
        }
    }

    public void removeChannel(Channel c) {
        synchronized (channels) {
            channels.remove(c);
        }
    }
    
    public void removeAllChannels(){
        synchronized(channels){
            channels.clear();
        }
    }

    public void addEnvelope(ConstantEnvelope e) {
        synchronized (envelopes) {
            envelopes.add(e);
        }
    }

    public void removeEnvelope(ConstantEnvelope e) {
        synchronized (envelopes) {
            envelopes.remove(e);
        }
    }
    
    public void removeAllEnvelopes(){
        synchronized(envelopes){
            envelopes.clear();
        }
    }

    public void setGrids(boolean xScale, double xScaleFrom, double xScaleTo, double xScaleStep, int xScaleDigits, boolean yScale, double yScaleFrom, double yScaleTo, double yScaleStep, int yScaleDigits) {
        if (xScale && (xScaleTo <= xScaleFrom || xScaleStep <= 0)) {
            xScale = false;
        }
        if (yScale && (yScaleTo <= yScaleFrom || yScaleStep <= 0)) {
            yScale = false;
        }
        this.xScale = xScale;
        this.xScaleFrom = xScaleFrom;
        this.xScaleTo = xScaleTo;
        this.xScaleStep = xScaleStep;
        this.yScale = yScale;
        this.yScaleFrom = yScaleFrom;
        this.yScaleTo = yScaleTo;
        this.yScaleStep = yScaleStep;
        this.xScaleDigits = xScaleDigits < 0 ? 0 : xScaleDigits;
        this.yScaleDigits = yScaleDigits < 0 ? 0 : yScaleDigits;
    }

    public void removeGrids() {
        xScale = false;
        yScale = false;
    }

    public void setUnits(String x, String y) {
        xUnit = x;
        yUnit = y;
    }

    public void removeUnits() {
        xUnit = null;
        yUnit = null;
    }

    @Override
    public void paint(Graphics g) {
        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, Config.CHART_ANTIALIASING ? RenderingHints.VALUE_ANTIALIAS_ON : RenderingHints.VALUE_ANTIALIAS_OFF);
        g.setColor(BACKGROUND);
        g.fillRect(0, 0, getWidth(), getHeight());
        int chartAreaWidth = getWidth(), chartAreaHeight = getHeight(), chartAreaStartX = 0, chartAreaStartY = 0;
        FontMetrics scaleFontMetrics = g.getFontMetrics(scaleFont);
        if (xScale) {
            chartAreaHeight -= scaleFontMetrics.getHeight() * 1.1;
        }
        if (yScale) {
            g.setColor(SCALE);
            g.setFont(scaleFont);
            int maxW = 0;
            for (double y = yScaleFrom; y <= yScaleTo; y += yScaleStep) {
                String label = String.format("%." + yScaleDigits + "f" + yUnit, y);
                int labelWidth = scaleFontMetrics.stringWidth(label), labelHeight = scaleFontMetrics.getHeight();
                if (labelWidth > maxW) {
                    maxW = labelWidth;
                }
                int labelY;
                if (y == yScaleFrom) {
                    labelY = chartAreaHeight - 1;
                } else if (y == yScaleTo) {
                    labelY = labelHeight;
                } else {
                    labelY = (int) ((double) chartAreaHeight - ((y - yScaleFrom) / (yScaleTo - yScaleFrom)) * (double) chartAreaHeight + (double) labelHeight / 2.0);
                }
                g.drawString(label, (int) (scaleFontMetrics.stringWidth("0") * 0.2), labelY);
            }
            chartAreaStartX += maxW + scaleFontMetrics.stringWidth("0") * 0.8;
            chartAreaWidth -= chartAreaStartX;
            g.setColor(GRID);
            for (double y = yScaleFrom; y <= yScaleTo; y += yScaleStep) {
                int lY = (int) ((double) chartAreaHeight - ((y - yScaleFrom) / (yScaleTo - yScaleFrom)) * (double) chartAreaHeight);
                g.drawLine(chartAreaStartX, lY, getWidth(), lY);
            }
            g.setColor(SCALE);
            g.drawLine(chartAreaStartX - 1, 0, chartAreaStartX - 1, chartAreaHeight);
        }
        if (xScale) {
            g.setColor(SCALE);
            g.setFont(scaleFont);
            for (double x = xScaleFrom; x <= xScaleTo; x += xScaleStep) {
                String label = String.format("%." + xScaleDigits + "f" + xUnit, x);
                int labelWidth = scaleFontMetrics.stringWidth(label), labelHeight = scaleFontMetrics.getHeight();
                int labelY = (int) (getHeight() - labelHeight * 0.1);
                int labelX;
                if (x == xScaleFrom && !yScale) {
                    labelX = chartAreaStartX;
                } else if (x == xScaleTo) {
                    labelX = getWidth() - labelWidth;
                } else {
                    labelX = (int) ((((x - xScaleFrom) / (xScaleTo - xScaleFrom))) * chartAreaWidth - (double) labelWidth / 2.0 + (getWidth() - chartAreaWidth));
                }
                g.drawString(label, labelX, labelY);
            }
            g.setColor(GRID);
            for (double x = xScaleFrom; x <= xScaleTo; x += xScaleStep) {
                int lX = (int) ((((x - xScaleFrom) / (xScaleTo - xScaleFrom))) * chartAreaWidth + (getWidth() - chartAreaWidth));
                g.drawLine(lX, 0, lX, chartAreaHeight);
            }
            g.setColor(SCALE);
            g.drawLine(chartAreaStartX - 1, chartAreaHeight, getWidth(), chartAreaHeight);
        }
        synchronized (channels) {
            for (Channel c : channels) {
                g.setColor(c.color);
                double pxPerSample = (double) chartAreaWidth / (double) c.data.getSize();
                int[] data = c.data.getData();
                if (c.max - c.min != 1) {
                    double pxPerUnit = (double) chartAreaHeight / (double) (c.max - c.min);
                    int[] xcrd = new int[data.length];
                    for (int i = 0; i < data.length; i++) {
                        xcrd[i] = (int) (i * pxPerSample + chartAreaStartX);
                        data[i] = (int) (chartAreaHeight - ((data[i] - c.min) * pxPerUnit) + chartAreaStartY - 1);
                    }
                    if (data.length == 1) {
                        g.drawLine(chartAreaStartX, data[0], chartAreaStartX + chartAreaWidth, data[0]);
                    } else {
                        g.drawPolyline(xcrd, data, data.length);
                    }
                } else {
                    for (int i = 0; i < data.length; i++) {
                        if (data[i] == c.max) {
                            g.drawLine((int) (i * pxPerSample + chartAreaStartX), 0, (int) (i * pxPerSample + chartAreaStartX - 1), (int) chartAreaHeight);
                        }
                    }
                }
            }
        }
        synchronized (envelopes) {
            for (ConstantEnvelope e : envelopes) {
                g.setColor(e.color);
                int[] xcrd = new int[e.xData.length], ycrd = new int[e.yData.length];
                double xMin = e.xData[0], xMax = e.xData[e.xData.length - 1];
                for (int i = 0; i < xcrd.length; i++) {
                    xcrd[i] = (int) (chartAreaWidth * ((e.xData[i] - xMin) / (xMax - xMin)) + chartAreaStartX);
                    ycrd[i] = (int) (chartAreaHeight - chartAreaHeight * ((e.yData[i] - e.min) / (e.max - e.min)) + chartAreaStartY);
                }
                if (xcrd.length == 1) {
                    g.drawLine(chartAreaStartX, ycrd[0], chartAreaStartX + chartAreaWidth, ycrd[0]);
                } else {
                    g.drawPolyline(xcrd, ycrd, ycrd.length);
                }
            }
        }
    }

}
