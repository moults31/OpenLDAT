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
import java.awt.Component;
import java.awt.Container;
import java.awt.FocusTraversalPolicy;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import javax.swing.JOptionPane;
import javax.swing.Timer;

/**
 *
 * @author dosse
 */
public class LinuxSerialHelp extends javax.swing.JFrame {

    /**
     * Creates new form LinuxSerialHelp
     */
    public LinuxSerialHelp() {
        initComponents();
        float DPI_SCALE = Utils.getDPIScaling();
        setSize((int) (getPreferredSize().width * DPI_SCALE), (int) (getPreferredSize().height * DPI_SCALE));
        setLocationRelativeTo(null);
        setVisible(true);
        setIconImage(Utils.loadAndScaleIcon("/com/dosse/openldat/ui/icon.png", (int) (128 * DPI_SCALE), (int) (128 * DPI_SCALE)).getImage());
        jLabel1.setIcon(Utils.loadAndScaleIcon("/com/dosse/openldat/ui/errordialog/error.png", (int) (64 * DPI_SCALE), (int) (64 * DPI_SCALE)));
        jLabel1.setFont(jLabel1.getFont().deriveFont(Config.LARGE_FONT_SIZE * DPI_SCALE));
        manualPane1.setPage(LinuxSerialHelp.class.getResource("/com/dosse/openldat/ui/manual/linuxserial.html"));
        jPanel1.setVisible(true);
        jPanel2.setVisible(false);
        setFocusTraversalPolicy(new FocusTraversalPolicy() {
            @Override
            public Component getComponentAfter(Container cntnr, Component cmpnt) {
                if (jPanel1.isVisible()) {
                    if (cmpnt == jButton1) {
                        return jButton2;
                    }
                    if (cmpnt == jButton2) {
                        return jButton4;
                    }
                    if (cmpnt == jButton4) {
                        return jButton1;
                    }
                }
                if (jPanel2.isVisible()) {
                    return manualPane1;
                }
                return null;
            }

            @Override
            public Component getComponentBefore(Container cntnr, Component cmpnt) {
                if (jPanel1.isVisible()) {
                    if (cmpnt == jButton1) {
                        return jButton4;
                    }
                    if (cmpnt == jButton2) {
                        return jButton1;
                    }
                    if (cmpnt == jButton4) {
                        return jButton2;
                    }
                }
                if (jPanel2.isVisible()) {
                    return manualPane1;
                }
                return null;
            }

            @Override
            public Component getFirstComponent(Container cntnr) {
                if (jPanel1.isVisible()) {
                    return jButton1;
                }
                if (jPanel2.isVisible()) {
                    return manualPane1;
                }
                return null;
            }

            @Override
            public Component getLastComponent(Container cntnr) {
                if (jPanel1.isVisible()) {
                    return jButton4;
                }
                if (jPanel2.isVisible()) {
                    return manualPane1;
                }
                return null;
            }

            @Override
            public Component getDefaultComponent(Container cntnr) {
                if (jPanel1.isVisible()) {
                    return jButton4;
                }
                if (jPanel2.isVisible()) {
                    return manualPane1;
                }
                return null;
            }
        });
        jButton1.setEnabled(false);
        jButton2.setEnabled(false);
        jButton4.setEnabled(false);
        Timer t=new Timer(3000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                jButton1.setEnabled(true);
                jButton2.setEnabled(true);
                jButton4.setEnabled(true);
            }
        });
        t.setRepeats(false);
        t.start();
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
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        manualPane1 = new com.dosse.openldat.ui.manual.ManualPane();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("OpenLDAT - Help");
        getContentPane().setLayout(new javax.swing.OverlayLayout(getContentPane()));

        jLabel1.setText("Failed to connect to the device");

        jLabel2.setText("<html>Wait, you're using Linux!<br/><br/>Many distros require you to allow yourself access to serial devices. If you're an administrator (can run sudo commands), OpenLDAT can try to do this automatically, or you can read the instructions on how to do it manually.</html>");

        jButton1.setText("Attempt automatic fix");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setText("Show instructions");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton4.setText("Close OpenLDAT");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jButton1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jButton2)))
                        .addGap(0, 202, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jButton4)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(44, 44, 44)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton2)
                    .addComponent(jButton1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 109, Short.MAX_VALUE)
                .addComponent(jButton4)
                .addContainerGap())
        );

        getContentPane().add(jPanel1);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(manualPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 529, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(manualPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 299, Short.MAX_VALUE)
        );

        getContentPane().add(jPanel2);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        jPanel1.setVisible(false);
        jPanel2.setVisible(true);
        manualPane1.requestFocus();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        System.exit(0);
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        setVisible(false);
        new Thread() {
            @Override
            public void run() {
                try {
                    String name = "/tmp/linuxserial.sh";
                    FileOutputStream fos = new FileOutputStream(new File(name));
                    InputStream script = LinuxSerialHelp.class.getResourceAsStream("/com/dosse/openldat/ui/errordialog/linuxserial.sh");
                    fos.write(script.readAllBytes());
                    script.close();
                    fos.flush();
                    fos.close();
                    Runtime.getRuntime().exec("chmod +x " + name).waitFor();
                    String[] knownTerminals = new String[]{"konsole -e ", "gnome-terminal -- ", "xfce4-terminal -e ", "lxterminal -e ", "xterm -e "};
                    Process p = null;
                    for (String term : knownTerminals) {
                        try {
                            p = Runtime.getRuntime().exec(term + name);
                            break;
                        } catch (Throwable t) {
                            p=null;
                        }
                    }
                    if (p == null) {
                        JOptionPane.showMessageDialog(null, "Failed to run script");
                        setVisible(true);
                    } else {
                        p.waitFor();
                        System.exit(0);
                    }
                } catch (Throwable ex) {
                    ex.printStackTrace();
                    setVisible(true);
                }
            }
        }.start();
    }//GEN-LAST:event_jButton1ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton4;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private com.dosse.openldat.ui.manual.ManualPane manualPane1;
    // End of variables declaration//GEN-END:variables
}
