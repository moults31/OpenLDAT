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
package com.dosse.openldat.ui.errordialog;

import com.dosse.openldat.Config;
import com.dosse.openldat.Utils;
import com.dosse.openldat.device.errors.MissingSensorException;
import com.dosse.openldat.device.errors.DeviceError;
import com.dosse.openldat.tests.IgnorableException;
import com.dosse.openldat.tests.TestException;
import java.io.IOException;

/**
 *
 * @author dosse
 */
public abstract class ErrorDialog extends javax.swing.JFrame {

    private float DPI_SCALE;

    /**
     * Creates new form ErrorDialog
     */
    public ErrorDialog(Throwable ex) {
        initComponents();
        DPI_SCALE = Utils.getDPIScaling();
        setSize((int) (getPreferredSize().width * DPI_SCALE), (int) (getPreferredSize().height * DPI_SCALE));
        setLocationRelativeTo(null);
        setIconImage(Utils.loadAndScaleIcon("/com/dosse/openldat/ui/icon.png", (int) (128 * DPI_SCALE), (int) (128 * DPI_SCALE)).getImage());
        jLabel2.setIcon(Utils.loadAndScaleIcon("/com/dosse/openldat/ui/errordialog/error.png", (int) (64 * DPI_SCALE), (int) (64 * DPI_SCALE)));
        jLabel3.setFont(jLabel3.getFont().deriveFont(Config.LARGE_FONT_SIZE * DPI_SCALE));
        if (ex instanceof NoClassDefFoundError || ex instanceof ClassNotFoundException) {
            jLabel3.setText("Loading error");
            jLabel1.setText("<html>Some files required by this application are missing. You should reinstall it.</html>");
            ex.printStackTrace();
            Utils.focusWindow(this);
            setVisible(true);
        } else if (ex instanceof IgnorableException) {
            dispose();
            onClose();
        } else if (ex instanceof ApplicationError) {
            ApplicationError ae = (ApplicationError) ex;
            jLabel3.setText(ae.getTitle() == null ? "Error" : ae.getTitle());
            jLabel1.setText("<html>" + ae.getMessage() + "</html>");
            jLabel2.setIcon(Utils.loadAndScaleIcon(ae.getIcon() == null ? "/com/dosse/openldat/ui/errordialog/error.png" : ae.getIcon(), (int) (64 * DPI_SCALE), (int) (64 * DPI_SCALE)));
            Utils.focusWindow(this);
            setVisible(true);
        } else if (ex instanceof TestException) {
            TestException te=(TestException) ex;
            if (te.getType()==TestException.USER_ABORT) {
                dispose();
                onClose();
            } else {
                switch(te.getType()){
                    case TestException.CUSTOM_ERROR:
                        jLabel1.setText("<html>" + te.getMessage() + "</html>");
                        break;
                    case TestException.ANALYSIS_FAILED:
                        jLabel1.setText("<html>Analysis failed</html>");
                        break;
                    case TestException.INCOMPATIBLE_DEVICE:
                        jLabel1.setText("<html>This device can't run this test</html>");
                        break;
                    case TestException.INSUFFICIENT_CONTRAST:
                        jLabel1.setText("<html>Insufficient contrast</html>");
                        break;
                    case TestException.INVALID_SETTINGS:
                        jLabel1.setText("<html>Invalid test settings</html>");
                        break;
                    default:
                        jLabel1.setText("<html>Something happened</html>");
                        break;
                }
                jLabel3.setText("Test failed");
                ex.printStackTrace();
                Utils.focusWindow(this);
                setVisible(true);
            }
        } else if (ex instanceof MissingSensorException || ex instanceof DeviceError) {
            if (Utils.isLinux() && ex instanceof DeviceError && ((DeviceError) ex).getType() == DeviceError.FAILED_TO_CONNECT) {
                new LinuxSerialHelp().setVisible(true);
                dispose();
            } else {
                jLabel3.setText("Device error");
                if (ex instanceof DeviceError) {
                    DeviceError de = (DeviceError) ex;
                    switch (de.getType()) {
                        case DeviceError.CUSTOM_ERROR:
                            jLabel1.setText("<html>" + de.getMessage() + "</html>");
                            break;
                        case DeviceError.FAILED_TO_CONNECT:
                            jLabel1.setText("<html>Failed to connect to the device</html>");
                            break;
                        case DeviceError.NOT_OPENLDAT_DEVICE:
                            jLabel1.setText("<html>Not an OpenLDAT device</html>");
                            break;
                        case DeviceError.UNSUPPORTED_MODEL:
                            jLabel1.setText("<html>Unsupported OpenLDAT device</html>");
                            break;
                        case DeviceError.DEVICE_ID_FAILED:
                            jLabel1.setText("<html>Failed to identify device</html>");
                            break;
                        case DeviceError.FIRMWARE_BUILT_WITH_SERIALPLOT:
                            jLabel1.setText("<html>Device firmware was built with SERIALPLOT_DEBUG enabled and cannot be used with the app</html>");
                            break;
                        case DeviceError.FIRMWARE_LIGHTSENSOR_MISSING_BUFSIZES:
                            jLabel1.setText("<html>Device has a light sensor but did not specify buffer sizes for it</html>");
                            break;
                        case DeviceError.FIRMWARE_NEEDS_NEWER_DRIVER:
                            jLabel1.setText("<html>This device requires a newer version of this application</html>");
                            break;
                        case DeviceError.FIRMWARE_UNKNOWN:
                            jLabel1.setText("<html>Unknown firmware version</html>");
                            break;
                        default:
                            jLabel1.setText("<html>Something happened</html>");
                            break;
                    }
                } else if (ex instanceof MissingSensorException) {
                    MissingSensorException mse = (MissingSensorException) ex;
                    switch (mse.getType()) {
                        case MissingSensorException.LIGHT_SENSOR:
                            jLabel1.setText("<html>Missing light sensor</html>");
                            break;
                        case MissingSensorException.CUSTOM_ERROR:
                            jLabel1.setText("<html>" + mse.getMessage() + "</html>");
                            break;
                        default:
                            jLabel1.setText("<html>A required sensor is missing</html>");
                            break;
                    }
                }
                ex.printStackTrace();
                Utils.focusWindow(this);
                setVisible(true);
            }
        } else if (ex instanceof IOException) {
            jLabel3.setText("Device error");
            jLabel1.setText("<html>Failed to communicate with the device</html>");
            ex.printStackTrace();
            Utils.focusWindow(this);
            setVisible(true);
        } else if (ex instanceof Exception) {
            if (ex.getMessage() != null && ex.getMessage().toLowerCase().startsWith("glfw")) {
                jLabel3.setText("OpenGL error");
                jLabel1.setText("<html>You may need to update your graphics driver.<br/>Error details: " + ex.getMessage() + "</html>");
                ex.printStackTrace();
                Utils.focusWindow(this);
                setVisible(true);
            } else {
                jLabel3.setText("Internal error");
                jLabel1.setText("<html>Error details were printed to the terminal.<br/>Restarting the application will probably fix this.</html>");
                ex.printStackTrace();
                Utils.focusWindow(this);
                setVisible(true);
            }
        } else {
            jLabel3.setText("Unknown internal error");
            jLabel1.setText("<html>Error details were printed to the terminal.<br/>Restarting the application will probably fix this.</html>");
            ex.printStackTrace();
            Utils.focusWindow(this);
            setVisible(true);
        }
    }

    public void setErrorMessage(String title, String message, String icon) {
        jLabel3.setText(title);
        jLabel1.setText(message);
        jLabel2.setIcon(Utils.loadAndScaleIcon(icon == null ? "/com/dosse/openldat/ui/errordialog/error.png" : icon, (int) (64 * DPI_SCALE), (int) (64 * DPI_SCALE)));
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("OpenLDAT - Error");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        jLabel1.setText("Error message goes here");

        jButton1.setText("Close");
        jButton1.setFocusCycleRoot(true);
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jLabel2.setText(" ");

        jLabel3.setText("Error type goes here");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 443, Short.MAX_VALUE)
                            .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jButton1)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 79, Short.MAX_VALUE)
                .addComponent(jButton1)
                .addContainerGap())
        );

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

    public abstract void onClose();

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    // End of variables declaration//GEN-END:variables
}
