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
package com.dosse.openldat.ui.tests.overdrive;

import com.dosse.openldat.Utils;
import com.dosse.openldat.ui.errordialog.ApplicationError;
import com.dosse.openldat.ui.errordialog.ErrorDialog;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FocusTraversalPolicy;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.JFileChooser;
import javax.swing.JTable;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

/**
 *
 * @author dosse
 */
public abstract class PixelOverdriveTestResults extends javax.swing.JFrame {

    private JTable jTable1;
    private String asText;

    /**
     * Creates new form PixelOverdriveTestResults
     */
    public PixelOverdriveTestResults(int[] steps, double[][] percent, boolean skipTo0And255, String method, boolean flickeringDetected) {
        initComponents();
        asText = "Method\t" + method + "\r\n" + "FlickeringDetected\t" + flickeringDetected + "\r\n\r\n";
        jTable1 = new javax.swing.JTable() {
            @Override
            public Component prepareRenderer(
                    TableCellRenderer renderer, int row, int col) {
                if (col == 0) {
                    return this.getTableHeader().getDefaultRenderer()
                            .getTableCellRendererComponent(this,
                                    this.getValueAt(row, col), false, false, row, col);
                } else {
                    return super.prepareRenderer(renderer, row, col);
                }
            }
        };
        jTable1.setAutoCreateRowSorter(false);
        final JTableHeader header = jTable1.getTableHeader();
        header.setReorderingAllowed(false);
        header.setResizingAllowed(false);
        header.setDefaultRenderer(new HeaderRenderer(jTable1));
        String[] cols = new String[steps.length + 1];
        cols[0] = "";
        asText += "\t";
        for (int i = 0; i < steps.length; i++) {
            cols[i + 1] = "From " + steps[i];
            asText += steps[i] + "\t";
        }
        asText += "\r\n";
        Object[][] rows = new Object[steps.length - (skipTo0And255 ? 2 : steps.length)][steps.length + 1];
        for (int row = 0; row < rows.length; row++) {
            rows[row][0] = "To " + steps[row + (skipTo0And255 ? 1 : 0)];
            asText += steps[row + (skipTo0And255 ? 1 : 0)] + "\t";
            for (int col = 1; col < rows[0].length; col++) {
                try {
                    rows[row][col] = String.format("%.2f%%", percent[row + (skipTo0And255 ? 1 : 0)][col - 1]);
                    asText += String.format("%.2f%%", percent[row + (skipTo0And255 ? 1 : 0)][col - 1]) + "\t";
                } catch (Throwable t) {
                    rows[row][col] = "Error";
                    asText += "Error\t";
                }
            }
            asText += "\r\n";
        }
        jTable1.setModel(new DefaultTableModel(rows, cols) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });
        jTable1.setDefaultRenderer(Object.class, new ColoredCellRenderer());
        float DPI_SCALE = Utils.getDPIScaling();
        jTable1.setSelectionMode(0);
        jTable1.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        TableColumnModel tcm = jTable1.getColumnModel();
        for (int i = 0; i < tcm.getColumnCount(); i++) {
            tcm.getColumn(i).setMinWidth((int) (80 * DPI_SCALE));
        }
        jTable1.setRowHeight((int) (48 * DPI_SCALE));
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, (int) (48 * DPI_SCALE)));
        jScrollPane1.setViewportView(jTable1);
        String notes = "<html>";
        if (flickeringDetected) {
            notes += "<font color=\"#ff8010\">PWM or other noise was detected during the test, results may be inaccurate!</font><br/>";
        }
        notes += "Method: " + method;
        notes += "</html>";
        jLabel1.setText(notes);
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

    private class HeaderRenderer implements TableCellRenderer {

        TableCellRenderer renderer;

        public HeaderRenderer(JTable jTable1) {
            renderer = jTable1.getTableHeader().getDefaultRenderer();
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int col) {
            return renderer.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, col);
        }
    }

    private class ColoredCellRenderer extends DefaultTableCellRenderer {

        public ColoredCellRenderer() {
            super();
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            try {
                String percent = (String) value;
                double d = Double.parseDouble(percent.substring(0, percent.length() - 1));
                if (Double.isFinite(d)) {
                    if (d < 0) {
                        d *= -1;
                    }
                    d = (d < 0 ? 0 : d > 15 ? 15 : d) / 15.0;
                    setBackground(new Color(Color.HSBtoRGB((float) (0.3333 * (1 - d)), 0.7f, 0.5f)));
                } else {
                    setBackground(new Color(Color.HSBtoRGB(0, 1f, 0.55f)));
                }
                setText(percent);
            } catch (Throwable t) {
            }
            return this;
        }
    }

    public abstract void onClose();

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jButton1 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jButton2 = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Pixel Overdrive Test - Results");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        jButton1.setText("Close");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setText("Save to file");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jLabel1.setText("Notes...");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1)
            .addComponent(jSeparator1)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jButton2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 609, Short.MAX_VALUE)
                        .addComponent(jButton1)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 361, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(jButton2))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        dispose();
        onClose();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        dispose();
        onClose();
    }//GEN-LAST:event_formWindowClosing

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
        c.setSelectedFile(new File("Pixel Overdrive - " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HHmmss")) + ".txt"));
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

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    // End of variables declaration//GEN-END:variables
}
