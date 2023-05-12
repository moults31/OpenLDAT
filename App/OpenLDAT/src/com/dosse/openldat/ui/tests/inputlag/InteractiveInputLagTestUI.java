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
package com.dosse.openldat.ui.tests.inputlag;

import com.dosse.openldat.device.Device;
import com.dosse.openldat.processing.buffers.CircularBuffer;
import com.dosse.openldat.processing.buffers.ArrayBuffer;
import com.dosse.openldat.tests.inputlag.InteractiveInputLagTest;
import com.dosse.openldat.ui.chart.Channel;
import com.dosse.openldat.Utils;
import com.dosse.openldat.tests.TestException;
import com.dosse.openldat.ui.errordialog.ApplicationError;
import com.dosse.openldat.ui.errordialog.ErrorDialog;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.EventQueue;
import java.awt.FocusTraversalPolicy;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author dosse
 */
public abstract class InteractiveInputLagTestUI extends javax.swing.JFrame {
    
    private Device d;
    private InteractiveInputLagTest test;
    private int n = 0;
    private int[] threshold = new int[1];
    private double tot = 0;

    /**
     * Creates new form InteractiveInputLagTestUI
     */
    public InteractiveInputLagTestUI(Device d) {
        this.d = d;
        initComponents();
        CircularBuffer lightWindow = new CircularBuffer(65536), clickWindow = new CircularBuffer(65536);
        chart1.addChannel(new Channel(lightWindow, 0, 800, new Color(255, 255, 255)));
        chart1.addChannel(new Channel(clickWindow, 0, 1, new Color(96, 128, 255)));
        chart1.addChannel(new Channel(new ArrayBuffer(threshold), 0, 800, new Color(96, 255, 128)));
        chart1.setGrids(false, 0, 0, 0, 0, true, 0, 800, 128, 0);
        thresholdSlider1.setBackground(new Color(19, 50, 26));
        thresholdSlider1.setForeground(new Color(96, 255, 128));
        try {
            test = new InteractiveInputLagTest(d, lightWindow, clickWindow) {
                @Override
                public void onNewDataPoint(double delay) {
                    DefaultTableModel dtm = (DefaultTableModel) jTable1.getModel();
                    n++;
                    dtm.addRow(new Object[]{(Integer) n, (Double) delay});
                    tot += delay;
                }
                
                @Override
                public void onDone(Map results) {
                    
                }
                
                @Override
                public void onError(Exception e) {
                    stopUIUpdater = true;
                    dispose();
                    new ErrorDialog(e) {
                        @Override
                        public void onClose() {
                            InteractiveInputLagTestUI.this.onClose();
                        }
                    };
                }
            };
            test.begin();
        } catch (Exception ex) {
            dispose();
            new ErrorDialog(ex) {
                @Override
                public void onClose() {
                    InteractiveInputLagTestUI.this.onClose();
                }
            };
            return;
        }
        thresholdSlider1.setValue(test.getThreshold());
        jComboBox1.setSelectedIndex(test.getSensitivity());
        jComboBox2.setSelectedIndex(test.getAutoFire()?1:0);
        uiUpdater();
        setFocusTraversalPolicy(new FocusTraversalPolicy() {
            @Override
            public Component getComponentAfter(Container cntnr, Component cmpnt) {
                if (cmpnt == thresholdSlider1) {
                    return jComboBox1;
                }
                if (cmpnt == jComboBox1) {
                    return jComboBox2;
                }
                if (cmpnt == jComboBox2) {
                    return jScrollPane1;
                }
                if (cmpnt == jScrollPane1) {
                    return jButton3;
                }
                if (cmpnt == jButton3) {
                    return jButton2;
                }
                if (cmpnt == jButton2) {
                    return jButton1;
                }
                if (cmpnt == jButton1) {
                    return thresholdSlider1;
                }
                return null;
            }
            
            @Override
            public Component getComponentBefore(Container cntnr, Component cmpnt) {
                if (cmpnt == thresholdSlider1) {
                    return jButton1;
                }
                if (cmpnt == jComboBox1) {
                    return thresholdSlider1;
                }
                if (cmpnt == jComboBox2) {
                    return jComboBox1;
                }
                if (cmpnt == jScrollPane1) {
                    return jComboBox2;
                }
                if (cmpnt == jButton3) {
                    return jScrollPane1;
                }
                if (cmpnt == jButton2) {
                    return jButton3;
                }
                if (cmpnt == jButton1) {
                    return jButton2;
                }
                return null;
            }
            
            @Override
            public Component getFirstComponent(Container cntnr) {
                return thresholdSlider1;
            }
            
            @Override
            public Component getLastComponent(Container cntnr) {
                return jButton1;
            }
            
            @Override
            public Component getDefaultComponent(Container cntnr) {
                return jButton1;
            }
        });
        jButton1.requestFocus();
        float DPI_SCALE = Utils.getDPIScaling();
        setSize((int) (getPreferredSize().width * DPI_SCALE), (int) (getPreferredSize().height * DPI_SCALE));
        setLocationRelativeTo(null);
        setIconImage(Utils.loadAndScaleIcon("/com/dosse/openldat/ui/icon.png", (int) (128 * DPI_SCALE), (int) (128 * DPI_SCALE)).getImage());
    }
    
    private boolean stopUIUpdater = false;
    private long lastUIUpdateT = 0;
    
    private void uiUpdater() {
        if (stopUIUpdater) {
            return;
        }
        long t = System.nanoTime();
        if (t - lastUIUpdateT > 10000000) {
            chart1.repaint();
            switch (test.getState()) {
                case 0:
                    jLabel2.setText("Waiting for click");
                    break;
                case 1:
                    jLabel2.setText("Clicked, waiting for light");
                    break;
                case 2:
                    jLabel2.setText("Triggered, waiting for dark");
                    break;
            }
            jLabel4.setText(n == 0 ? "0.00" : String.format("%.2f", (tot / n)));
            lastUIUpdateT = t;
        } else {
            Utils.sleep(1);
        }
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                uiUpdater();
            }
        });
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        chart1 = new com.dosse.openldat.ui.chart.Chart();
        thresholdSlider1 = new com.dosse.openldat.ui.tests.inputlag.ThresholdSlider();
        jPanel2 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jButton2 = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jButton3 = new javax.swing.JButton();
        jPanel5 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jComboBox1 = new javax.swing.JComboBox<>();
        jLabel7 = new javax.swing.JLabel();
        jComboBox2 = new javax.swing.JComboBox<>();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Total System Latency - Interactive Test");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });
        getContentPane().setLayout(new java.awt.GridLayout(2, 0));

        javax.swing.GroupLayout chart1Layout = new javax.swing.GroupLayout(chart1);
        chart1.setLayout(chart1Layout);
        chart1Layout.setHorizontalGroup(
            chart1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 723, Short.MAX_VALUE)
        );
        chart1Layout.setVerticalGroup(
            chart1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        thresholdSlider1.setMaximum(800);
        thresholdSlider1.setValue(250);
        thresholdSlider1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                thresholdSlider1StateChanged(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(chart1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(0, 0, 0)
                .addComponent(thresholdSlider1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(thresholdSlider1, javax.swing.GroupLayout.DEFAULT_SIZE, 278, Short.MAX_VALUE)
            .addComponent(chart1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        getContentPane().add(jPanel1);

        jButton1.setText("Close");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jPanel3.setLayout(new java.awt.GridLayout(1, 3));

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Run", "Time (ms)"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.Double.class
            };
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(jTable1);

        jButton2.setText("Clear");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jLabel3.setText("Average input lag:");

        jLabel5.setText("ms");

        jLabel4.setText("...");

        jButton3.setText("Save to file");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 357, Short.MAX_VALUE)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel5)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addComponent(jButton3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButton2))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jLabel4)
                    .addComponent(jLabel5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 174, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton2)
                    .addComponent(jButton3)))
        );

        jPanel3.add(jPanel4);

        jLabel1.setText("State:");

        jLabel2.setText("Starting...");

        jLabel6.setText("Sensitivity");

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Low", "Medium", "High", "Maximum" }));
        jComboBox1.setSelectedIndex(2);
        jComboBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox1ActionPerformed(evt);
            }
        });

        jLabel7.setText("Click source");

        jComboBox2.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "External button", "Autofire (to this PC)" }));
        jComboBox2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel2))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel6)
                            .addComponent(jLabel7))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(125, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(152, Short.MAX_VALUE))
        );

        jPanel3.add(jPanel5);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jButton1)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton1)
                .addContainerGap())
        );

        getContentPane().add(jPanel2);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        test.cancel();
        stopUIUpdater = true;
        dispose();
        onClose();
    }//GEN-LAST:event_formWindowClosing

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        test.cancel();
        stopUIUpdater = true;
        dispose();
        onClose();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox1ActionPerformed
        test.setSensitivity((byte) jComboBox1.getSelectedIndex());
    }//GEN-LAST:event_jComboBox1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        tot = 0;
        n = 0;
        DefaultTableModel dtm = (DefaultTableModel) jTable1.getModel();
        while (dtm.getRowCount() > 0)
            dtm.removeRow(0);
    }//GEN-LAST:event_jButton2ActionPerformed

    private void thresholdSlider1StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_thresholdSlider1StateChanged
        test.setThreshold(thresholdSlider1.getValue());
        threshold[0] = thresholdSlider1.getValue();
    }//GEN-LAST:event_thresholdSlider1StateChanged

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        JFileChooser c = new JFileChooser();
        c.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isDirectory() || file.getName().toLowerCase().endsWith(".txt");
            }
            
            @Override
            public String getDescription() {
                return "Text files (*.txt)";
            }
        });
        c.setSelectedFile(new File("Interactive Input Lag - " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HHmmss")) + ".txt"));
        if (c.showSaveDialog(rootPane) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File f = c.getSelectedFile();
        if (f == null) {
            return;
        }
        if (!f.getName().toLowerCase().endsWith(".txt")) {
            f = new File(f.getAbsolutePath() + ".txt");
        }
        try {
            BufferedWriter w = new BufferedWriter(new FileWriter(f));
            w.write("Run\tTime\r\n");
            for (int i = 0; i < jTable1.getRowCount(); i++) {
                w.write(i + "\t" + jTable1.getValueAt(i, 1) + "\r\n");
            }
            w.flush();
            w.close();
        } catch (IOException ex) {
            new ErrorDialog(new ApplicationError("File not saved", ex.getMessage(), null)) {
                @Override
                public void onClose() {
                }
            };
        }
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jComboBox2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox2ActionPerformed
        test.setAutoFire(jComboBox2.getSelectedIndex() == 1);
    }//GEN-LAST:event_jComboBox2ActionPerformed
    
    public abstract void onClose();
    
    public static void run(Device d, Runnable doneCallback) {
        if (d == null || !d.isOpen() || !d.hasLightSensor()) {
            new ErrorDialog(new TestException(TestException.INCOMPATIBLE_DEVICE)) {
                @Override
                public void onClose() {
                    doneCallback.run();
                }
            };
            return;
        }
        InteractiveInputLagTestUI ui = new InteractiveInputLagTestUI(d) {
            @Override
            public void onClose() {
                doneCallback.run();
            }
        };
        Utils.focusWindow(ui);
        ui.setVisible(true);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private com.dosse.openldat.ui.chart.Chart chart1;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JComboBox<String> jComboBox1;
    private javax.swing.JComboBox<String> jComboBox2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private com.dosse.openldat.ui.tests.inputlag.ThresholdSlider thresholdSlider1;
    // End of variables declaration//GEN-END:variables

}
