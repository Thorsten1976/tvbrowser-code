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
 *     $Date$
 *   $Author$
 * $Revision$
 */
package util.ui;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;

import javax.swing.ButtonGroup;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import tvbrowser.ui.settings.SettingsDialog;
import util.settings.PluginPictureSettings;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.SettingsItem;

/**
 * A settings panel for the setup of the plugins picture settings.
 * 
 * @author René Mach
 * @since 2.6
 */
public class PluginsPictureSettingsPanel extends JPanel {
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(PluginsPictureSettingsPanel.class);
  
  private JRadioButton mGlobalSettings;
  private JRadioButton mPictureAndDescription;
  private JRadioButton mOnlyPictures;
  private JRadioButton mNoPictures;
  
  private ArrayList<ChangeListener> mChangeListenerList;
  
  /**
   * Creates an instance of this class.
   * 
   * @param settings The settings to read the current value from.
   * @param showDisableButtonDontShowAllButton If this is <code>true</code> the disabled picture button will be shown
   * and the global plugin settings button is not shown. If it is <code>false</code> the disabled picture button will not
   * ne shown and the global plugin settings button is shown.
   */
  public PluginsPictureSettingsPanel(PluginPictureSettings settings, boolean showDisableButtonDontShowAllButton) {
    mChangeListenerList = new ArrayList<ChangeListener>();
    
    setLayout(new FormLayout("14dlu,default:grow","default," + (!showDisableButtonDontShowAllButton ? "2dlu,default,5dlu,"  : "") + "2dlu,default,2dlu,default"));

    final ItemListener itemListener = e -> {
      if(e.getStateChange() == ItemEvent.SELECTED) {
        fireChangeEvent();
      }
    };
    
    mGlobalSettings = new JRadioButton(mLocalizer.msg("globalSettings","Use default settings for plugins"), settings.getType() == PluginPictureSettings.ALL_PLUGINS_SETTINGS_TYPE);
    mPictureAndDescription = new JRadioButton(mLocalizer.msg("pictureAndDesc","Show picture and picture description (if available)"), settings.getType() == PluginPictureSettings.PICTURE_AND_DISCRIPTION_TYPE);
    mOnlyPictures = new JRadioButton(mLocalizer.msg("onlyPictures","Show only pictures (if available)"), settings.getType() == PluginPictureSettings.ONLY_PICTURE_TYPE);
    
    mGlobalSettings.addItemListener(itemListener);
    mPictureAndDescription.addItemListener(itemListener);
    mOnlyPictures.addItemListener(itemListener);
    
    final ButtonGroup bg = new ButtonGroup();
    
    short y = 1;
    
    if(!showDisableButtonDontShowAllButton) {
      bg.add(mGlobalSettings);
      add(mGlobalSettings, CC.xyw(1,y++,2));
      
      JEditorPane helpLabel = UiUtilities.createHtmlHelpTextArea(mLocalizer.msg("help","The default plugin setting can be changed in the <a href=\"#link\">picture settings</a>."), new HyperlinkListener() {
        public void hyperlinkUpdate(HyperlinkEvent e) {
          if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            SettingsDialog.getInstance().showSettingsTab(SettingsItem.PICTURES);
          }
        }
      });
      
      add(helpLabel, CC.xy(2,++y));
      y += 3;
    }
    bg.add(mPictureAndDescription);
    bg.add(mOnlyPictures);
    
    add(mPictureAndDescription, CC.xyw(1,y,2));
    y += 2;
    add(mOnlyPictures, CC.xyw(1,y++,2));
    
    if(showDisableButtonDontShowAllButton) {
      mNoPictures = new JRadioButton(mLocalizer.msg("noPictures","Don't show pictures and description"), settings.getType() == PluginPictureSettings.NO_PICTURE_TYPE);
      mNoPictures.addItemListener(itemListener);
      bg.add(mNoPictures);
      add(mNoPictures, CC.xyw(1,++y,2));
    }
  }
  
  /**
   * Get the settings with the selected value.
   * 
   * @return The settings with the selected value.
   */
  public PluginPictureSettings getSettings() {
    int type = PluginPictureSettings.ALL_PLUGINS_SETTINGS_TYPE;
    
    if(mPictureAndDescription.isSelected()) {
      type = PluginPictureSettings.PICTURE_AND_DISCRIPTION_TYPE;
    }
    else if(mOnlyPictures.isSelected()) {
      type = PluginPictureSettings.ONLY_PICTURE_TYPE;
    }
    else if(mNoPictures != null && mNoPictures.isSelected()) {
      type = PluginPictureSettings.NO_PICTURE_TYPE;
    }

    return new PluginPictureSettings(type);
  }
  
  /**
   * Gets the title to may be used for this settings.
   * 
   * @return The title of this settings.
   */
  public static String getTitle() {
    return mLocalizer.msg("title","Picture setting of the program list");
  }
  
  /**
   * Adds a change listener, that listens for setting changes.
   * <p>
   * @param listener The listener to add.
   * @since 3.4.4
   */
  public void addChangeListener(ChangeListener listener) {
    if(!mChangeListenerList.contains(listener)) {
      mChangeListenerList.add(listener);
    }
  }
  
  /**
   * Removes a change listener from listening of setting changes.
   * <p>
   * @param listener The listener to remove.
   * @since 3.4.4
   */
  public void removeChangeListener(ChangeListener listener) {
    mChangeListenerList.remove(listener);
  }
  
  private void fireChangeEvent() {
    if(!mChangeListenerList.isEmpty()) {
      final ChangeEvent e = new ChangeEvent(this);
      
      for(ChangeListener listener : mChangeListenerList) {
        listener.stateChanged(e);
      }
    }
  }
}
