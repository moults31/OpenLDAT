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
package com.dosse.openldat.ui.tests.microstuttering;

import com.dosse.openldat.Config;
import com.dosse.openldat.Utils;
import com.dosse.openldat.device.Device;
import com.dosse.openldat.tests.TestException;
import com.dosse.openldat.tests.microstuttering.StutteringDetectionTest;
import com.dosse.openldat.ui.chart.ConstantEnvelope;
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
public abstract class StutteringDetectionTestUI extends javax.swing.JFrame {

    private String asText;

    /**
     * Creates new form StutteringDetectionTestUI
     */
    public StutteringDetectionTestUI(int stutters, Double[] times, double threshold, boolean flickeringDetected) {
        initComponents();
        jLabel2.setText("" + stutters);
        if (stutters != 0) {
            jLabel2.setForeground(new Color(255, 96, 16));
        }
        asText = "FlickeringDetected\t" + flickeringDetected + "\r\nStutteringThreshold\t" + threshold + "\r\n\r\nRun\tTime\tStutter\r\n";
        DefaultTableModel dtm = (DefaultTableModel) jTable1.getModel();
        for (int i = 0; i < times.length; i++) {
            dtm.addRow(new Object[]{(Integer) (i + 1), times[i]});
            asText += i + "\t" + times[i] + "\t" + (times[i] >= threshold ? "1" : "0") + "\r\n";
        }
        double[] xvals = new double[times.length], yvals = new double[times.length];
        double max = 0;
        for (int i = 0; i < times.length; i++) {
            xvals[i] = (double) i / (double) times.length;
            yvals[i] = times[i];
            if (times[i] > max) {
                max = times[i];
            }
        }
        max = (max > threshold ? max : threshold) * 1.3;
        chart1.addEnvelope(new ConstantEnvelope(new double[]{0}, new double[]{threshold}, 0, max, new Color(255, 96, 16)));
        chart1.addEnvelope(new ConstantEnvelope(xvals, yvals, 0, max, new Color(128, 255, 96)));
        chart1.setGrids(false, 0, 0, 0, 0, true, 0, max, max * 0.2, 0);
        chart1.setUnits(null, " ms");
        String notes = "<html>";
        if (flickeringDetected) {
            notes += "Warning: PWM or noise was detected during the test, results may be inaccurate<br/>";
        }
        if (!Config.TESTSCREEN_GL) {
            notes += "Warning: the test was performed without OpenGL, results may be inaccurate<br/>";
        }
        notes += "</html>";
        jLabel3.setText(notes);
        jLabel3.setForeground(new Color(255, 128, 16));
        float DPI_SCALE = Utils.getDPIScaling();
        setFocusTraversalPolicy(new FocusTraversalPolicy() {
            @Override
            public Component getComponentAfter(Container cntnr, Component cmpnt) {
                if (cmpnt == jScrollPane1) {
                    return jButton2;
                }
                if (cmpnt == jButton2) {
                    return jButton1;
                }
                if (cmpnt == jButton1) {
                    return jScrollPane1;
                }
                return null;
            }

            @Override
            public Component getComponentBefore(Container cntnr, Component cmpnt) {
                if (cmpnt == jScrollPane1) {
                    return jButton1;
                }
                if (cmpnt == jButton2) {
                    return jScrollPane1;
                }
                if (cmpnt == jButton1) {
                    return jButton2;
                }
                return null;
            }

            @Override
            public Component getFirstComponent(Container cntnr) {
                return jScrollPane1;
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
        setSize((int) (getPreferredSize().width * DPI_SCALE), (int) (getPreferredSize().height * DPI_SCALE));
        setLocationRelativeTo(null);
        setIconImage(Utils.loadAndScaleIcon("/com/dosse/openldat/ui/icon.png", (int) (128 * DPI_SCALE), (int) (128 * DPI_SCALE)).getImage());
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
        jPanel2 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jButton2 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Microstuttering Detection Test - Results");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });
        getContentPane().setLayout(new java.awt.GridLayout(2, 0));

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Times"));
        jPanel1.setLayout(new java.awt.GridLayout(1, 0));

        javax.swing.GroupLayout chart1Layout = new javax.swing.GroupLayout(chart1);
        chart1.setLayout(chart1Layout);
        chart1Layout.setHorizontalGroup(
            chart1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 664, Short.MAX_VALUE)
        );
        chart1Layout.setVerticalGroup(
            chart1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 198, Short.MAX_VALUE)
        );

        jPanel1.add(chart1);

        getContentPane().add(jPanel1);

        jButton1.setText("Close");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jPanel3.setLayout(new java.awt.GridLayout(1, 2));

        jLabel1.setText("Stutters:");

        jLabel2.setText("...");

        jLabel3.setText("...");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel2)))
                .addContainerGap(257, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2))
                .addGap(18, 18, 18)
                .addComponent(jLabel3)
                .addContainerGap(113, Short.MAX_VALUE))
        );

        jPanel3.add(jPanel4);

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

        jPanel3.add(jScrollPane1);

        jButton2.setText("Save to file");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, 650, Short.MAX_VALUE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jButton2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButton1)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, 159, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(jButton2))
                .addContainerGap())
        );

        getContentPane().add(jPanel2);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        dispose();
        onClose();
    }//GEN-LAST:event_formWindowClosing

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        dispose();
        onClose();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
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
        c.setSelectedFile(new File("Microstuttering - " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HHmmss")) + ".txt"));
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
            w.write(asText);
            w.flush();
            w.close();
        } catch (IOException ex) {
            new ErrorDialog(new ApplicationError("File not saved", ex.getMessage(), null)) {
                @Override
                public void onClose() {
                }
            };
        }
    }//GEN-LAST:event_jButton2ActionPerformed

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
        if (!Config.TESTSCREEN_GL) {
            ErrorDialog e = new ErrorDialog(new ApplicationError("OpenGL required", "This test requires accurate timing that cannot be achieved with the Swing backend", null)) {
                @Override
                public void onClose() {
                    doneCallback.run();
                }
            };
            return;
        }
        StutteringDetectionTest test = new StutteringDetectionTest(d, 7000) {
            @Override
            public void onDone(Map results) {
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        StutteringDetectionTestUI ui = new StutteringDetectionTestUI((int) results.get("stutters"), (Double[]) results.get("frameTimes"), (double) results.get("stutteringThreshold"), (boolean) results.get("flickeringDetected")) {
                            @Override
                            public void onClose() {
                                doneCallback.run();
                            }
                        };
                        Utils.focusWindow(ui);
                        ui.setVisible(true);
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        new ErrorDialog(e) {
                            @Override
                            public void onClose() {
                                doneCallback.run();
                            }
                        };
                    }
                });
            }
        };
        test.begin();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private com.dosse.openldat.ui.chart.Chart chart1;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    // End of variables declaration//GEN-END:variables
}
