/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin_oat@yahoo.de)
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

package reminderplugin;

import java.util.Properties;
import javax.swing.*;
import java.awt.*;
import java.io.*;

import devplugin.*;

import util.ui.*;

/**
 * TV-Browser
 *
 * @author Martin Oberhauser
 */
public class ReminderSettingsTab extends devplugin.SettingsTab {

  private static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(ReminderSettingsTab.class);

  private Properties settings;
  
  private JCheckBox reminderwindowCheckBox;
  private FileCheckBox soundFileCheckBox;
  private FileCheckBox execFileCheckBox;
  private JSpinner mAutoCloseReminderTimeSp;

  
  
  public ReminderSettingsTab(Properties settings) {
    super();
    
    String msg;
    JPanel p1;

    setLayout(new BorderLayout());
    this.settings=settings;

    JPanel reminderPanel=new JPanel();

    msg = mLocalizer.msg("remindBy", "Remind me by");
    reminderPanel.setBorder(BorderFactory.createTitledBorder(msg));

    reminderPanel.setLayout(new BoxLayout(reminderPanel,BoxLayout.Y_AXIS));
    JPanel panel1=new JPanel(new BorderLayout());
    msg = mLocalizer.msg("reminderWindow", "Reminder window");
    reminderwindowCheckBox = new JCheckBox(msg);
    panel1.add(reminderwindowCheckBox,BorderLayout.WEST);
    reminderPanel.add(panel1);

    String soundFName=settings.getProperty("soundfile","/");
    String execFName=settings.getProperty("execfile","/");

    File soundFile=new File(soundFName);
    File execFile=new File(execFName);

    msg = mLocalizer.msg("playlingSound", "Play sound");
    soundFileCheckBox = new FileCheckBox(msg, soundFile, 200);
    msg = mLocalizer.msg("executeProgram", "Execute program");
    execFileCheckBox = new FileCheckBox(msg, execFile, 200);

    JFileChooser soundChooser=new JFileChooser("sound/");
    JFileChooser execChooser=new JFileChooser("/");

    String[] extArr = { ".wav", ".aif", ".rmf", ".au", ".mid" };
    msg = mLocalizer.msg("soundFileFilter", "Sound file ({0})",
      "*.wav, *.aif, *.rmf, *.au, *.mid");
    soundChooser.setFileFilter(new ExtensionFileFilter(extArr, msg));

    reminderwindowCheckBox.setSelected(settings.getProperty("usemsgbox","true").equals("true"));
    soundFileCheckBox.setSelected(settings.getProperty("usesound","false").equals("true"));
    execFileCheckBox.setSelected(settings.getProperty("useexec","false").equals("true"));

    soundFileCheckBox.setFileChooser(soundChooser);
    execFileCheckBox.setFileChooser(execChooser);

    reminderPanel.add(soundFileCheckBox);
    reminderPanel.add(execFileCheckBox);

    // Auto close time of the reminder frame
    p1 = new JPanel(new FlowLayout(FlowLayout.LEADING));
    reminderPanel.add(p1);
    
    p1.add(new JLabel(mLocalizer.msg("autoCloseReminderTime",
      "Automatically close reminder after (in seconds, 0 = off)")));
    int autoCloseReminderTime = 0;
    try {
      String asString = settings.getProperty("autoCloseReminderTime", "0");
      autoCloseReminderTime = Integer.parseInt(asString);
    } catch (Exception exc) {}
    mAutoCloseReminderTimeSp = new JSpinner();
    mAutoCloseReminderTimeSp.setBorder(null);
    mAutoCloseReminderTimeSp.setValue(new Integer(10000));
    mAutoCloseReminderTimeSp.setPreferredSize(mAutoCloseReminderTimeSp.getPreferredSize());
    mAutoCloseReminderTimeSp.setValue(new Integer(autoCloseReminderTime));
    p1.add(mAutoCloseReminderTimeSp);
    
    add(reminderPanel,BorderLayout.NORTH);
    updateUI();
  }



  public String getName() {
    return mLocalizer.msg("tabName", "Reminder");
  }



  public void ok() {
    settings.setProperty("soundfile",soundFileCheckBox.getTextField().getText());
    settings.setProperty("execfile",execFileCheckBox.getTextField().getText());

    settings.setProperty("usemsgbox",new Boolean(reminderwindowCheckBox.isSelected()).toString());
    settings.setProperty("usesound",new Boolean(soundFileCheckBox.isSelected()).toString());
    settings.setProperty("useexec",new Boolean(execFileCheckBox.isSelected()).toString());
    
    settings.setProperty("autoCloseReminderTime", mAutoCloseReminderTimeSp.getValue().toString());
  }
  
}