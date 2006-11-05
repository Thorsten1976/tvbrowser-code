/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin@tvbrowser.org)
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
 */
package util.settings;

import util.ui.PictureSettingsPanel;

/**
 * A class that contains setting values for the program panel.
 * 
 * @author Ren� Mach
 * @since 2.2.2
 */
public class ProgramPanelSettings {
  private int mType;
  private int mTimeRangeStart;
  private int mTimeRangeEnd;
  private int mDuration;
  private boolean mShowOnlyDateAndTitle; 
  private boolean mShowDescription;
  private String[] mPluginIds;

  /**
   * Creates an instance of this class.
   * 
   * @param type The picture showing type.
   * @param timeRangeStart The time range start time.
   * @param timeRangeEnd The time range end time.
   * @param showOnlyDateAndTitle If the program panel should only contain date and title.
   * @param showDescription If the picture description should be shown.
   */
  public ProgramPanelSettings(int type, int timeRangeStart, int timeRangeEnd, boolean showOnlyDateAndTitle, boolean showDescription, int duration) {
    this(type, timeRangeStart, timeRangeEnd,showOnlyDateAndTitle, showDescription, duration, null);
  }
  
  /**
   * Creates an instance of this class.
   * 
   * @param type The picture showing type.
   * @param timeRangeStart The time range start time.
   * @param timeRangeEnd The time range end time.
   * @param showOnlyDateAndTitle If the program panel should only contain date and title.
   * @param showDescription If the picture description should be shown.
   * @param pluginIds The ids of the plugins to show the pictures for.
   */
  public ProgramPanelSettings(int type, int timeRangeStart, int timeRangeEnd, boolean showOnlyDateAndTitle, boolean showDescription, int duration, String[] pluginIds) {
    mType = type;
    mTimeRangeStart = timeRangeStart;
    mTimeRangeEnd = timeRangeEnd;
    mShowOnlyDateAndTitle = showOnlyDateAndTitle;
    mShowDescription = showDescription;
    mPluginIds = pluginIds;
    mDuration = duration;
  }
  
  /**
   * @return If the type of the picture showing is set to show pictures in time range. 
   */
  public boolean isShowingPictureInTimeRange() {
    return mType == PictureSettingsPanel.SHOW_IN_TIME_RANGE || 
           mType == PictureSettingsPanel.SHOW_IN_TIME_RANGE + PictureSettingsPanel.SHOW_FOR_DURATION + PictureSettingsPanel.SHOW_FOR_PLUGINS ||
           mType == PictureSettingsPanel.SHOW_IN_TIME_RANGE + PictureSettingsPanel.SHOW_FOR_DURATION ||
           mType == PictureSettingsPanel.SHOW_IN_TIME_RANGE + PictureSettingsPanel.SHOW_FOR_PLUGINS;
  }

  /**
   * @return If the type of the picture showing is set to show picture always.
   */
  public boolean isShowingPictureEver() {
    return mType == PictureSettingsPanel.SHOW_EVER;
  }
  
  /**
   * @return If the type of the picture showing is set to show picture never.
   */
  public boolean isShowingPictureNever() {
    return mType == PictureSettingsPanel.SHOW_NEVER;
  }
  
  /**
   * @return If the type of the picture showing is set to show picture for plugins.
   */
  public boolean isShowingPictureForPlugins() {
    return mType == PictureSettingsPanel.SHOW_FOR_PLUGINS ||
           mType == PictureSettingsPanel.SHOW_FOR_PLUGINS + PictureSettingsPanel.SHOW_IN_TIME_RANGE + PictureSettingsPanel.SHOW_FOR_DURATION ||
           mType == PictureSettingsPanel.SHOW_FOR_PLUGINS + PictureSettingsPanel.SHOW_IN_TIME_RANGE ||
           mType == PictureSettingsPanel.SHOW_FOR_PLUGINS + PictureSettingsPanel.SHOW_FOR_DURATION;
  }
  
  /**
   * @return True if the type of the picture showing is set to show picture for duration.
   */
  public boolean isShowingPictureForDuration() {
    return mType == PictureSettingsPanel.SHOW_FOR_DURATION ||
           mType == PictureSettingsPanel.SHOW_FOR_DURATION + PictureSettingsPanel.SHOW_FOR_PLUGINS + PictureSettingsPanel.SHOW_IN_TIME_RANGE ||
           mType == PictureSettingsPanel.SHOW_FOR_DURATION + PictureSettingsPanel.SHOW_FOR_PLUGINS ||
           mType == PictureSettingsPanel.SHOW_FOR_DURATION + PictureSettingsPanel.SHOW_IN_TIME_RANGE;
  }
   
  /**
   * @return If the program panel should only containg date and title.
   */
  public boolean isShowingOnlyDateAndTitle() {
    return mShowOnlyDateAndTitle;
  }

  /**
   * @return If the picture description should be shown.
   */
  public boolean isShowingPictureDescription() {
    return mShowDescription;
  }
  
  /**
   * @return The type of the picture showing.
   */
  public int getPictureShowingType() {
    return mType;
  }
  
  /**
   * @return The time range start time.
   */
  public int getPictureTimeRangeStart() {
    return mTimeRangeStart;
  }

  /**
   * @return The time range end time.
   */
  public int getPictureTimeRangeEnd() {
    return mTimeRangeEnd;
  }
  
  /**
   * @return The duration value
   */
  public int getDuration() {
    return mDuration;
  }
  
  /**
   * @return The plugin ids to show the pictures for.
   */
  public String[] getPluginIds() {
    return mPluginIds;
  }
}
