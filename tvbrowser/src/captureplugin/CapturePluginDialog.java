/*
 * CapturePlugin by Andreas Hessel (Vidrec@gmx.de), Bodo Tasche
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * CVS information:
 *  $RCSfile$
 *   $Source$
 *     $Date$
 *   $Author$
 * $Revision$
 */

package captureplugin;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

import util.ui.Localizer;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;

/**
 * The Dialog for the Settings. Uses the PluginPanel
 */
public class CapturePluginDialog extends JDialog implements WindowClosingIf {
    /** Translator */
    private static final Localizer mLocalizer = Localizer.getLocalizerFor(CapturePluginDialog.class);
  
    /** Data */
    private CapturePluginData mData;

    /** PluginPanel for entering Data */
    private CapturePluginPanel mPanel;

    /**
     * creates a new Dialog
     */
    public CapturePluginDialog(Frame parent, CapturePluginData data) {
        super(parent);

        mData = data;
        setModal(true);
        createGui(parent);
    }

    /**
     * creates a new Dialog
     */
    public CapturePluginDialog(Dialog parent, CapturePluginData data) {
        super(parent);

        mData = data;
        setModal(true);
        createGui(parent);
    }    
    
    /**
     * Creates the GUI
     * @param parent Parent
     */
    public void createGui(Component parent) {
        UiUtilities.registerForClosing(this);
      
        this.getContentPane().setLayout(new BorderLayout());
        if (parent != null) {
            this.setLocation(parent.getLocation().x + (parent.getWidth() / 2) - 200, parent.getLocation().y + (parent.getHeight() / 2)
                    - 280);
        }
        this.setTitle(mLocalizer.msg("Title", "Capture Plugin - Settings"));

        mPanel = new CapturePluginPanel((JFrame) parent, mData);
        this.getContentPane().add(mPanel, BorderLayout.CENTER);
        this.setSize(500, 450);
        

        JButton okButton = new JButton(mLocalizer.msg("OK", "ok"));
        okButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                okButtonPressed(e);
            }
        });

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        btnPanel.add(okButton);
        
        this.getContentPane().add(btnPanel, BorderLayout.SOUTH);
    }

    /**
     * Updates the mPanel with the new Data and shows the Dialog
     * 
     * @param tab Tab to select
     */
    public void show(int tab) {
        mPanel.setSelectedTab(tab);
        super.setVisible(true);
    }

    /**
     * invoked when the user clicks the OK - Button, this will hide the Dialog.
     */
    public void okButtonPressed(ActionEvent e) {
        this.setVisible(false);
    }

    public void close() {
      this.setVisible(false);
    }

}