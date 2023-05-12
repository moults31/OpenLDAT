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
package com.dosse.openldat.ui.tests.driver;

import com.dosse.openldat.device.Device;
import com.dosse.openldat.device.callbacks.LightSensorMonitorCallback;
import com.dosse.openldat.processing.buffers.CircularBuffer;
import com.dosse.openldat.processing.filters.FFTFilter;
import com.dosse.openldat.processing.filters.PeakHoldFilter;
import com.dosse.openldat.processing.filters.RunningAverageSmoothingFilter;
import com.dosse.openldat.ui.chart.Channel;
import com.dosse.openldat.ui.chart.Chart;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import javax.swing.JOptionPane;
import javax.swing.Timer;

/**
 *
 * @author dosse
 */
public class LightSensorMonitorDemo extends javax.swing.JFrame {

    private Device device;
    private Timer repaintTimer = null;
    private DataOutputStream dumpFile = null;

    /**
     * Creates new form LightSensorMonitorDemo
     */
    public LightSensorMonitorDemo(Device device, boolean noBuffer, byte sensitivity, boolean fastADC, int windowSize, boolean doFFT, boolean doSmooth, boolean doPeak, boolean dumpToFile) {
        try {
            initComponents();
            this.device = device;
            CircularBuffer light = new CircularBuffer(windowSize);
            FFTFilter fft = new FFTFilter(windowSize);
            RunningAverageSmoothingFilter smooth = new RunningAverageSmoothingFilter(windowSize, fastADC ? 0.996 : 0.99);
            double sampleRate = device.getLightSensorMonitorModeSampleRate(noBuffer, fastADC);
            PeakHoldFilter peak = new PeakHoldFilter(windowSize, (int) (sampleRate * 0.0085));
            if (dumpToFile) {
                try {
                    dumpFile = new DataOutputStream(new FileOutputStream("lightdump" + "_" + System.nanoTime() + "_" + sensitivity + "_" + ((int) sampleRate) + ".raw"));
                    dumpFile.writeDouble(sampleRate);
                } catch (Throwable t) {
                    dumpFile = null;
                }
            }
            device.lightSensorMonitorMode(noBuffer, sensitivity, fastADC, new LightSensorMonitorCallback() {
                @Override
                public void onDataBufferReceived(int[] data) {
                    light.add(data);
                    if (doFFT) {
                        fft.add(data);
                    }
                    if (doSmooth) {
                        smooth.add(data);
                    }
                    if (doPeak) {
                        peak.add(data);
                    }
                    if (dumpFile != null) {
                        try {
                            for (int i : data) {
                                dumpFile.writeInt(i);
                            }
                        } catch (Throwable t) {
                        }
                    }
                }

                @Override
                public void onDataSampleReceived(int data) {
                    light.add(data);
                    if (doFFT) {
                        fft.add(data);
                    }
                    if (doSmooth) {
                        smooth.add(data);
                    }
                    if (doPeak) {
                        peak.add(data);
                    }
                    if (dumpFile != null) {
                        try {
                            dumpFile.writeInt(data);
                        } catch (Throwable t) {
                        }
                    }
                }

                @Override
                public void onError(Exception e) {
                    super.onError(e);
                    System.exit(2);
                }
            });
            Channel chLight = new Channel(light, 0, 1023, new Color(192, 192, 192)),
                    chFFT = new Channel(fft, 0, 2000, new Color(192, 128, 32)),
                    chSmooth = new Channel(smooth, 0, 1023, new Color(0, 64, 255)),
                    chPeak = new Channel(peak, 0, 1023, new Color(255, 0, 128));
            Chart graph = new Chart();
            if (doFFT) {
                graph.addChannel(chFFT);
            }
            graph.addChannel(chLight);
            if (doSmooth) {
                graph.addChannel(chSmooth);
            }
            if (doPeak) {
                graph.addChannel(chPeak);
            }
            if (doFFT) {
                graph.setGrids(true, 0, sampleRate / 2, (sampleRate / 2) * 0.05, 0, true, 0, 1023, 64, 0);
                graph.setUnits("Hz", "");
            } else {
                graph.setGrids(true, 0, ((double) windowSize / sampleRate), 0.1, 1, true, 0, 1023, 64, 0);
                graph.setUnits("s", "");
            }
            getContentPane().add(graph);
            setVisible(true);
            repaintTimer = new Timer(10, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    graph.repaint();
                }
            });
            repaintTimer.setRepeats(true);
            repaintTimer.start();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(rootPane, ex.getMessage(), getTitle(), JOptionPane.ERROR_MESSAGE);
            formWindowClosing(null);
            dispose();
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("OpenLDAT Driver Test - Light Sensor Monitor");
        setMinimumSize(new java.awt.Dimension(320, 180));
        setPreferredSize(new java.awt.Dimension(1024, 600));
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });
        getContentPane().setLayout(new java.awt.GridLayout(1, 0));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        repaintTimer.stop();
        device.endCurrentActivity();
        if (dumpFile != null) {
            try {
                dumpFile.flush();
                dumpFile.close();
            } catch (Throwable t) {
            }
        }
    }//GEN-LAST:event_formWindowClosing

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
