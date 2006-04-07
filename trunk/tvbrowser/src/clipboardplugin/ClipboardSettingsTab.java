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
package clipboardplugin;

import java.util.Properties;

import javax.swing.Icon;
import javax.swing.JPanel;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import util.paramhandler.ParamInputField;
import util.ui.Localizer;
import devplugin.SettingsTab;

/**
 * This is the Settings-Tab for the ClipboardPlugin
 * 
 * @author bodum
 */
public class ClipboardSettingsTab implements SettingsTab {
  /** Translator */
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(ClipboardSettingsTab.class);

  /** Settings to use */
  private Properties mSettings;

  /** Text-Area for the Parameters */
  private ParamInputField mParamText;

  /** Plugin */
  private ClipboardPlugin mPlugin;
  
  /**
   * Creates the SettingsTab
   * 
   * @param setttings Settings to use
   */
  public ClipboardSettingsTab(ClipboardPlugin plugin, Properties setttings) {
    mSettings = setttings;
    mPlugin = plugin;
  }

  /**
   * Creates the SettingsPanel
   * 
   * @return Settings-Panel
   */
  public JPanel createSettingsPanel() {
    PanelBuilder pb = new PanelBuilder(new FormLayout("5dlu,fill:default:grow,5dlu","5dlu,pref,fill:default:grow,5dlu"));
    CellConstraints cc = new CellConstraints();
    
    pb.addLabel(mLocalizer.msg("createText","Text to create for each Program") + ":", cc.xy(2,2));
    
    mParamText = new ParamInputField(mSettings.getProperty("ParamToUse", ClipboardPlugin.DEFAULT_PARAM));
    pb.add(mParamText, cc.xy(2,3));

    return pb.getPanel();
  }

  /**
   * Save the Settings
   */
  public void saveSettings() {
    mSettings.setProperty("ParamToUse", mParamText.getText());
  }

  /**
   * Get the Tab-Icon
   * 
   * @return Icon
   */
  public Icon getIcon() {
    return mPlugin.createImageIcon("actions", "edit-paste", 16);
  }

  /**
   * Get the Title for this Tab
   * 
   * @return Tab-Title
   */
  public String getTitle() {
    return mLocalizer.msg("title", "Clipboard Settings");
  }
}