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

import com.dosse.openldat.Config;
import com.dosse.openldat.Utils;
import com.dosse.openldat.device.Device;
import com.dosse.openldat.tests.TestException;
import com.dosse.openldat.tests.inputlag.InputLagTest;
import com.dosse.openldat.ui.errordialog.ErrorDialog;
import java.awt.Component;
import java.awt.Container;
import java.awt.EventQueue;
import java.awt.FocusTraversalPolicy;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Timer;

/**
 *
 * @author dosse
 */
public abstract class InputLagTestStarter extends javax.swing.JFrame {

    private final Device d;

    private Timer notesUpdater;

    /**
     * Creates new form PixelResponseTestStarter
     */
    public InputLagTestStarter(Device d) {
        this.d = d;
        initComponents();
        if (!Config.TESTSCREEN_GL) {
            jComboBox1.setEnabled(false);
            jComboBox1.setModel(new DefaultComboBoxModel<>(new String[]{"Unsupported", "System", "Unsupported", "Unsupported"}));
            jComboBox1.setSelectedIndex(1);
            jComboBox2.setEnabled(false);
            jComboBox2.setSelectedIndex(0);
            jComboBox3.setEnabled(false);
            jComboBox3.setSelectedIndex(0);
        }
        setFocusTraversalPolicy(new FocusTraversalPolicy() {
            @Override
            public Component getComponentAfter(Container cntnr, Component cmpnt) {
                if (Config.TESTSCREEN_GL) {
                    if (cmpnt == jComboBox1) {
                        return jComboBox2;
                    }
                    if (cmpnt == jComboBox2) {
                        return jComboBox3;
                    }
                    if (cmpnt == jComboBox3) {
                        return jComboBox4;
                    }
                    if (cmpnt == jComboBox4) {
                        return jButton1;
                    }
                    if (cmpnt == jButton1) {
                        return jComboBox1;
                    }
                } else {
                    return jButton1;
                }
                return null;
            }

            @Override
            public Component getComponentBefore(Container cntnr, Component cmpnt) {
                if (Config.TESTSCREEN_GL) {
                    if (cmpnt == jComboBox1) {
                        return jButton1;
                    }
                    if (cmpnt == jComboBox2) {
                        return jComboBox1;
                    }
                    if (cmpnt == jComboBox3) {
                        return jComboBox2;
                    }
                    if (cmpnt == jComboBox4) {
                        return jComboBox3;
                    }
                    if (cmpnt == jButton1) {
                        return jComboBox4;
                    }
                } else {
                    return jButton1;
                }
                return null;
            }

            @Override
            public Component getFirstComponent(Container cntnr) {
                if (Config.TESTSCREEN_GL) {
                    return jComboBox1;
                } else {
                    return jButton1;
                }
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
        setMinimumSize(getSize());
        setIconImage(Utils.loadAndScaleIcon("/com/dosse/openldat/ui/icon.png", (int) (128 * DPI_SCALE), (int) (128 * DPI_SCALE)).getImage());
        notesUpdater = new Timer(50, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                String notes = "<html>";
                if (!Config.TESTSCREEN_GL) {
                    notes += "Settings are only available with the OpenGL backend<br/>";
                }
                long fakeCPULoadMs = 0, fakeGPULoadMs = 0;
                if (jComboBox2.getSelectedIndex() > 0) {
                    fakeCPULoadMs = Long.parseLong(((String) jComboBox2.getSelectedItem()).split(" ")[0].trim());
                }
                if (jComboBox3.getSelectedIndex() > 0) {
                    fakeGPULoadMs = Long.parseLong(((String) jComboBox3.getSelectedItem()).split(" ")[0].trim());
                }
                long totLoad = fakeCPULoadMs + fakeGPULoadMs;
                if (totLoad > 0) {
                    notes += "Max FPS with this load: ~" + (1000 / totLoad) + "<br/>";
                }
                notes += "</html>";
                jLabel4.setText(notes);
            }
        });
        notesUpdater.setRepeats(true);
        notesUpdater.start();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jButton1 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jComboBox1 = new javax.swing.JComboBox<>();
        jLabel2 = new javax.swing.JLabel();
        jComboBox2 = new javax.swing.JComboBox<>();
        jComboBox3 = new javax.swing.JComboBox<>();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jComboBox4 = new javax.swing.JComboBox<>();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Total System Latecy Test - Configuration");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        jButton1.setText("Run test");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jLabel1.setText("VSync mode");

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Off", "On", "On (Alternative implementation)" }));

        jLabel2.setText("Fake additional CPU load");

        jComboBox2.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "No", "1 ms/frame", "2 ms/frame", "3 ms/frame (Typical)", "5 ms/frame", "7 ms/frame", "10 ms/frame", "15 ms/frame", "20 ms/frame" }));

        jComboBox3.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "No", "1 ms/frame", "2 ms/frame", "3 ms/frame", "5 ms/frame", "7 ms/frame", "10 ms/frame (Typical)", "15 ms/frame", "20 ms/frame", "25 ms/frame", "30 ms/frame", "40 ms/frame", "50 ms/frame", "60 ms/frame" }));

        jLabel3.setText("Fake additional GPU load");

        jLabel4.setText("Notes...");

        jLabel5.setText("Test duration");

        jComboBox4.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "20 seconds", "1 minute", "2 minutes", "5 minutes" }));
        jComboBox4.setSelectedIndex(1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(0, 310, Short.MAX_VALUE)
                        .addComponent(jButton1))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jComboBox3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel5)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jComboBox4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jComboBox3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(jComboBox4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 20, Short.MAX_VALUE)
                .addComponent(jButton1)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        setVisible(false);
        notesUpdater.stop();
        int vsyncMode = jComboBox1.getSelectedIndex();
        long fakeCPULoadMs = 0, fakeGPULoadMs = 0, duration = 20000;
        if (jComboBox2.getSelectedIndex() > 0) {
            fakeCPULoadMs = Long.parseLong(((String) jComboBox2.getSelectedItem()).split(" ")[0].trim());
        }
        if (jComboBox3.getSelectedIndex() > 0) {
            fakeGPULoadMs = Long.parseLong(((String) jComboBox3.getSelectedItem()).split(" ")[0].trim());
        }
        switch (jComboBox4.getSelectedIndex()) {
            case 0:
                duration = 20000;
                break;
            case 1:
                duration = 60000;
                break;
            case 2:
                duration = 120000;
                break;
            case 3:
                duration = 300000;
                break;
        }
        String vSyncModeString = (String) jComboBox1.getSelectedItem(), fakeCPULoadString = (String) jComboBox2.getSelectedItem(), fakeGPULoadString = (String) jComboBox3.getSelectedItem();
        InputLagTest test = new InputLagTest(d, duration, vsyncMode, fakeCPULoadMs, fakeGPULoadMs) {
            @Override
            public void onDone(Map results) {
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        InputLagTestResults ui = new InputLagTestResults((double) results.get("percentileL"), (double) results.get("percentileH"), (double) results.get("percentile50"), (Double[]) results.get("times"), (Double[]) results.get("distribution"), vSyncModeString, fakeCPULoadString, fakeGPULoadString) {
                            @Override
                            public void onClose() {
                                InputLagTestStarter.this.onClose();
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
                                InputLagTestStarter.this.onClose();
                            }
                        };
                    }
                });
            }
        };
        dispose();
        test.begin();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        notesUpdater.stop();
        dispose();
        onClose();
    }//GEN-LAST:event_formWindowClosing

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
        InputLagTestStarter ui = new InputLagTestStarter(d) {
            @Override
            public void onClose() {
                doneCallback.run();
            }
        };
        Utils.focusWindow(ui);
        ui.setVisible(true);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JComboBox<String> jComboBox1;
    private javax.swing.JComboBox<String> jComboBox2;
    private javax.swing.JComboBox<String> jComboBox3;
    private javax.swing.JComboBox<String> jComboBox4;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    // End of variables declaration//GEN-END:variables
}
