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
package com.dosse.openldat.ui.mainmenu;

import com.dosse.openldat.Utils;
import com.dosse.openldat.device.Device;
import com.dosse.openldat.ui.errordialog.ApplicationError;
import com.dosse.openldat.ui.errordialog.ErrorDialog;
import com.dosse.openldat.ui.tests.driver.DriverTestMenu;
import com.dosse.openldat.ui.tests.inputlag.InputLagTestStarter;
import com.dosse.openldat.ui.tests.inputlag.InteractiveInputLagTestUI;
import com.dosse.openldat.ui.tests.microstuttering.StutteringDetectionTestUI;
import com.dosse.openldat.ui.tests.overdrive.PixelOverdriveTestStarter;
import com.dosse.openldat.ui.tests.pixelresponse.PixelResponseTestStarter;
import com.dosse.openldat.ui.tests.light2sound.LightToSoundUI;
import com.dosse.openldat.ui.tests.pwm.PWMDetectionTestUI;
import java.awt.Component;
import java.awt.Container;
import java.awt.FocusTraversalPolicy;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

/**
 *
 * @author dosse
 */
public class MainMenu extends javax.swing.JFrame {

    private float DPI_SCALE = 1;
    private Device d;

    private JButton currentlySelectedTestButton;

    /**
     * Creates new form MainMenu
     */
    public MainMenu(Device d) {
        this.d = d;
        initComponents();
        DPI_SCALE = Utils.getDPIScaling();
        setSize((int) (getPreferredSize().width * DPI_SCALE), (int) (getPreferredSize().height * DPI_SCALE));
        setLocationRelativeTo(null);
        JButton b = createTestButton();
        b.setText("Welcome");
        b.setIcon(Utils.loadAndScaleIcon("/com/dosse/openldat/ui/icon.png", (int) (64 * DPI_SCALE), (int) (64 * DPI_SCALE)));
        b.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                manualPane.setPage(MainMenu.class.getResource("/com/dosse/openldat/ui/manual/welcome.html"));
                while (runTest.getActionListeners().length != 0) {
                    runTest.removeActionListener(runTest.getActionListeners()[0]);
                }
                runTest.setEnabled(false);
                currentlySelectedTestButton = (JButton) ae.getSource();
            }
        });
        JButton welcomeButton = b;
        Runnable returnToMainMenu = new Runnable() {
            @Override
            public void run() {
                if (!d.isOpen()) { //we're returning after a test failed due to a device I/O error
                    System.exit(0);
                }

                Utils.focusWindow(MainMenu.this);
                if (currentlySelectedTestButton != null) {
                    currentlySelectedTestButton.requestFocus();
                } else {
                    welcomeButton.doClick();
                    welcomeButton.requestFocus();
                }
                //force a full repaint of the window, since the UI sometimes gets corrupted because of the compositor toggling
                getContentPane().repaint();
                setVisible(true);
            }
        };
        if (d.hasLightSensor()) {
            b = createTestButton();
            b.setText("<html>Input Lag<br/>Automated test</html>");
            b.setIcon(Utils.loadAndScaleIcon("/com/dosse/openldat/ui/mainmenu/icons/test_inputlag_auto.png", (int) (64 * DPI_SCALE), (int) (64 * DPI_SCALE)));
            b.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    manualPane.clearHistory();
                    manualPane.setPage(MainMenu.class.getResource("/com/dosse/openldat/ui/manual/test_inputlag_auto.html"));
                    while (runTest.getActionListeners().length != 0) {
                        runTest.removeActionListener(runTest.getActionListeners()[0]);
                    }
                    runTest.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent ae) {
                            setVisible(false);
                            InputLagTestStarter.run(d, returnToMainMenu);
                        }
                    });
                    runTest.setEnabled(true);
                    runTest.requestFocus();
                    currentlySelectedTestButton = (JButton) ae.getSource();
                }
            });
            b = createTestButton();
            b.setText("<html>Input Lag<br/>Manual test</html>");
            b.setIcon(Utils.loadAndScaleIcon("/com/dosse/openldat/ui/mainmenu/icons/test_inputlag_manual.png", (int) (64 * DPI_SCALE), (int) (64 * DPI_SCALE)));
            b.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    manualPane.clearHistory();
                    manualPane.setPage(MainMenu.class.getResource("/com/dosse/openldat/ui/manual/test_inputlag_manual.html"));
                    while (runTest.getActionListeners().length != 0) {
                        runTest.removeActionListener(runTest.getActionListeners()[0]);
                    }
                    runTest.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent ae) {
                            setVisible(false);
                            InteractiveInputLagTestUI.run(d, returnToMainMenu);
                        }
                    });
                    runTest.setEnabled(true);
                    runTest.requestFocus();
                    currentlySelectedTestButton = (JButton) ae.getSource();
                }
            });
            b = createTestButton();
            b.setText("Microstuttering detection");
            b.setIcon(Utils.loadAndScaleIcon("/com/dosse/openldat/ui/mainmenu/icons/test_microstuttering.png", (int) (64 * DPI_SCALE), (int) (64 * DPI_SCALE)));
            b.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    manualPane.clearHistory();
                    manualPane.setPage(MainMenu.class.getResource("/com/dosse/openldat/ui/manual/test_microstuttering.html"));
                    while (runTest.getActionListeners().length != 0) {
                        runTest.removeActionListener(runTest.getActionListeners()[0]);
                    }
                    runTest.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent ae) {
                            setVisible(false);
                            StutteringDetectionTestUI.run(d, returnToMainMenu);
                        }
                    });
                    runTest.setEnabled(true);
                    runTest.requestFocus();
                    currentlySelectedTestButton = (JButton) ae.getSource();
                }
            });
            b = createTestButton();
            b.setText("PWM / Strobing detection");
            b.setIcon(Utils.loadAndScaleIcon("/com/dosse/openldat/ui/mainmenu/icons/test_pwm.png", (int) (64 * DPI_SCALE), (int) (64 * DPI_SCALE)));
            b.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    manualPane.clearHistory();
                    manualPane.setPage(MainMenu.class.getResource("/com/dosse/openldat/ui/manual/test_pwm.html"));
                    while (runTest.getActionListeners().length != 0) {
                        runTest.removeActionListener(runTest.getActionListeners()[0]);
                    }
                    runTest.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent ae) {
                            setVisible(false);
                            PWMDetectionTestUI.run(d, returnToMainMenu);
                        }
                    });
                    runTest.setEnabled(true);
                    runTest.requestFocus();
                    currentlySelectedTestButton = (JButton) ae.getSource();
                }
            });
            b = createTestButton();
            b.setText("Pixel Response Time");
            b.setIcon(Utils.loadAndScaleIcon("/com/dosse/openldat/ui/mainmenu/icons/test_pixelResponse.png", (int) (64 * DPI_SCALE), (int) (64 * DPI_SCALE)));
            b.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    manualPane.clearHistory();
                    manualPane.setPage(MainMenu.class.getResource("/com/dosse/openldat/ui/manual/test_pixelResponse.html"));
                    while (runTest.getActionListeners().length != 0) {
                        runTest.removeActionListener(runTest.getActionListeners()[0]);
                    }
                    runTest.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent ae) {
                            setVisible(false);
                            PixelResponseTestStarter.run(d, returnToMainMenu);
                        }
                    });
                    runTest.setEnabled(true);
                    runTest.requestFocus();
                    currentlySelectedTestButton = (JButton) ae.getSource();
                }
            });
            b = createTestButton();
            b.setText("<html>Pixel Overdrive<br/>Overshoot/undershoot</html>");
            b.setIcon(Utils.loadAndScaleIcon("/com/dosse/openldat/ui/mainmenu/icons/test_overdrive.png", (int) (64 * DPI_SCALE), (int) (64 * DPI_SCALE)));
            b.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    manualPane.clearHistory();
                    manualPane.setPage(MainMenu.class.getResource("/com/dosse/openldat/ui/manual/test_overdrive.html"));
                    while (runTest.getActionListeners().length != 0) {
                        runTest.removeActionListener(runTest.getActionListeners()[0]);
                    }
                    runTest.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent ae) {
                            setVisible(false);
                            PixelOverdriveTestStarter.run(d, returnToMainMenu);
                        }
                    });
                    runTest.setEnabled(true);
                    runTest.requestFocus();
                    currentlySelectedTestButton = (JButton) ae.getSource();
                }
            });
            b = createTestButton();
            b.setText("Light To Sound");
            b.setIcon(Utils.loadAndScaleIcon("/com/dosse/openldat/ui/mainmenu/icons/test_light2sound.png", (int) (64 * DPI_SCALE), (int) (64 * DPI_SCALE)));
            b.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    manualPane.clearHistory();
                    manualPane.setPage(MainMenu.class.getResource("/com/dosse/openldat/ui/manual/test_light2sound.html"));
                    while (runTest.getActionListeners().length != 0) {
                        runTest.removeActionListener(runTest.getActionListeners()[0]);
                    }
                    runTest.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent ae) {
                            setVisible(false);
                            LightToSoundUI.run(d, returnToMainMenu);
                        }
                    });
                    runTest.setEnabled(true);
                    runTest.requestFocus();
                    currentlySelectedTestButton = (JButton) ae.getSource();
                }
            });
        }
        if (d.isPrototype()) {
            b = createTestButton();
            b.setText("Driver test");
            b.setIcon(Utils.loadAndScaleIcon("/com/dosse/openldat/ui/mainmenu/icons/test_driver.png", (int) (64 * DPI_SCALE), (int) (64 * DPI_SCALE)));
            b.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    manualPane.clearHistory();
                    manualPane.setPage(MainMenu.class.getResource("/com/dosse/openldat/ui/manual/test_driver.html"));
                    while (runTest.getActionListeners().length != 0) {
                        runTest.removeActionListener(runTest.getActionListeners()[0]);
                    }
                    runTest.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent ae) {
                            setVisible(false);
                            DriverTestMenu.run(d, returnToMainMenu);
                        }
                    });
                    runTest.setEnabled(true);
                    runTest.requestFocus();
                    currentlySelectedTestButton = (JButton) ae.getSource();
                }
            });
        }
        b = createTestButton();
        b.setText("Advanced settings");
        b.setIcon(Utils.loadAndScaleIcon("/com/dosse/openldat/ui/mainmenu/icons/settings.png", (int) (64 * DPI_SCALE), (int) (64 * DPI_SCALE)));
        b.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                new SettingsScreen(MainMenu.this);
            }
        });
        b = createTestButton();
        b.setText("About OpenLDAT");
        b.setIcon(Utils.loadAndScaleIcon("/com/dosse/openldat/ui/mainmenu/icons/about.png", (int) (64 * DPI_SCALE), (int) (64 * DPI_SCALE)));
        b.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                new AboutScreen(MainMenu.this, d);
            }
        });
        JPanel filler = new JPanel();
        GridBagConstraints constraints = new java.awt.GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = testList.getComponentCount();
        constraints.weighty = 1.0;
        testList.add(filler, constraints);
        MouseWheelListener[] l = jScrollPane1.getMouseWheelListeners();
        for (MouseWheelListener ll : l) {
            jScrollPane1.removeMouseWheelListener(ll);
        }
        addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent mwe) {
                jScrollPane1.getVerticalScrollBar().setValue((int) (jScrollPane1.getVerticalScrollBar().getValue() + (mwe.getPreciseWheelRotation() * mwe.getScrollAmount() * 3 * DPI_SCALE)));
                mwe.consume();
            }
        });
        runTest.setIcon(Utils.loadAndScaleIcon("/com/dosse/openldat/ui/mainmenu/icons/run.png", (int) (48 * DPI_SCALE), (int) (48 * DPI_SCALE)));
        runTest.setDisabledIcon(Utils.loadAndScaleIcon("/com/dosse/openldat/ui/mainmenu/icons/run_disabled.png", (int) (48 * DPI_SCALE), (int) (48 * DPI_SCALE)));
        runTest.setIconTextGap((int) (8 * DPI_SCALE));
        welcomeButton.doClick();
        setFocusTraversalPolicy(new FocusTraversalPolicy() {
            @Override
            public Component getComponentAfter(Container cntnr, Component cmpnt) {
                if (cmpnt.getParent() == testList) {
                    Component[] c = testList.getComponents();
                    for (int i = 0; i < c.length - 1; i++) {
                        if (c[i] == cmpnt) {
                            if (i >= c.length - 2) {
                                return welcomeButton;
                            } else {
                                return c[i + 1];
                            }
                        }
                    }
                }
                if (cmpnt.getParent() == detailsArea) {
                    if (cmpnt == runTest) {
                        return currentlySelectedTestButton;
                    }
                    if (cmpnt == manualPane) {
                        if (runTest.isEnabled()) {
                            return runTest;
                        } else {
                            return currentlySelectedTestButton;
                        }
                    }
                }
                return null;
            }

            @Override
            public Component getComponentBefore(Container cntnr, Component cmpnt) {
                if (cmpnt.getParent() == testList) {
                    Component[] c = testList.getComponents();
                    for (int i = 0; i < c.length - 1; i++) {
                        if (c[i] == cmpnt) {
                            if (i == 0) {
                                return c[c.length - 2];
                            } else {
                                return c[i - 1];
                            }
                        }
                    }
                }
                if (cmpnt.getParent() == detailsArea) {
                    if (cmpnt == runTest) {
                        return manualPane;
                    }
                    if (cmpnt == manualPane) {
                        return currentlySelectedTestButton;
                    }
                }
                return null;
            }

            @Override
            public Component getFirstComponent(Container cntnr) {
                return welcomeButton;
            }

            @Override
            public Component getLastComponent(Container cntnr) {
                if (runTest.isEnabled()) {
                    return runTest;
                } else {
                    return manualPane;
                }
            }

            @Override
            public Component getDefaultComponent(Container cntnr) {
                return welcomeButton;
            }
        });
        setIconImage(Utils.loadAndScaleIcon("/com/dosse/openldat/ui/icon.png", (int) (128 * DPI_SCALE), (int) (128 * DPI_SCALE)).getImage());
        setTitle(getTitle() + " - " + d.getModel());
        setVisible(true);
        if (!(Utils.isWindows() || Utils.isLinux() || Utils.isMac())) {
            new ErrorDialog(new ApplicationError("Unsupported platform", "OpenLDAT was not tested on this platform.<br/>The application will continue, but expect trouble.", null)) {
                @Override
                public void onClose() {
                }
            };
        }
    }

    private JButton createTestButton() {
        JButton ret = new JButton();
        ret.setText("*** MISSING TEXT ***");
        ret.setAlignmentX(0.5F);
        ret.setAlignmentY(0.0F);
        ret.setMinimumSize(new java.awt.Dimension(0, 0));
        ret.setIconTextGap((int) (8 * DPI_SCALE));
        ret.setHorizontalAlignment(SwingConstants.LEADING);
        ret.setBackground(UIManager.getColor("TestButton.background"));
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1.0;
        constraints.insets = new java.awt.Insets(0, 0, (int) (8 * DPI_SCALE), 0);
        ret.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                super.focusGained(e);
                if (ret.getLocation().y + ret.getHeight() >= jScrollPane1.getVerticalScrollBar().getValue() + jScrollPane1.getHeight()) {
                    jScrollPane1.getVerticalScrollBar().setValue(ret.getLocation().y + ret.getHeight() - jScrollPane1.getHeight());
                }
                if (ret.getLocation().y < jScrollPane1.getVerticalScrollBar().getValue()) {
                    jScrollPane1.getVerticalScrollBar().setValue(ret.getLocation().y);
                }
            }
        });
        testList.add(ret, constraints);
        return ret;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        testList = new javax.swing.JPanel();
        detailsArea = new javax.swing.JPanel();
        manualPane = new com.dosse.openldat.ui.manual.ManualPane();
        runTest = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("OpenLDAT");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });
        getContentPane().setLayout(new java.awt.GridLayout(1, 2, 4, 0));

        jScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        testList.setLayout(new java.awt.GridBagLayout());
        jScrollPane1.setViewportView(testList);

        getContentPane().add(jScrollPane1);

        runTest.setText("Run test");
        runTest.setEnabled(false);

        javax.swing.GroupLayout detailsAreaLayout = new javax.swing.GroupLayout(detailsArea);
        detailsArea.setLayout(detailsAreaLayout);
        detailsAreaLayout.setHorizontalGroup(
            detailsAreaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(manualPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(runTest, javax.swing.GroupLayout.DEFAULT_SIZE, 410, Short.MAX_VALUE)
        );
        detailsAreaLayout.setVerticalGroup(
            detailsAreaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(detailsAreaLayout.createSequentialGroup()
                .addComponent(manualPane, javax.swing.GroupLayout.DEFAULT_SIZE, 521, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(runTest))
        );

        getContentPane().add(detailsArea);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        d.close();
        System.exit(0);
    }//GEN-LAST:event_formWindowClosing

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel detailsArea;
    private javax.swing.JScrollPane jScrollPane1;
    private com.dosse.openldat.ui.manual.ManualPane manualPane;
    private javax.swing.JButton runTest;
    private javax.swing.JPanel testList;
    // End of variables declaration//GEN-END:variables
}
