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
package tvbrowser.core.plugin;

import java.awt.Frame;
import java.io.File;

import javax.swing.Action;
import javax.swing.Icon;

import util.exc.ErrorHandler;
import util.exc.TvBrowserException;
import devplugin.ChannelDayProgram;
import devplugin.PluginInfo;
import devplugin.Program;
import devplugin.SettingsTab;

/**
 * An abstract implementation of a plugin proxy. Encapsulates all calls to the
 * plugin.
 * <p>
 * This means that
 * <ul>
 * <li>All RuntimeExceptions thrown by the plugin are catched</li>
 * <li>An error message will be shown, if an operation is called on an inactive
 *     plugin that is only allowed with active plugins.</li>
 * </ul>
 *
 * @author Til Schneider, www.murfman.de
 */
public abstract class AbstractPluginProxy implements PluginProxy {

  /** The localizer for this class. */
  public static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(AbstractPluginProxy.class);
  
  /**
   * Holds whether the plugin is currently activated.
   * <p>
   * The {@link PluginProxyManager} holds the real state of each plugin.
   * This variable is only for speeding up the activated test.
   * 
   * @see #assertActivatedState()
   */
  private boolean mIsActivated = false;


  /**
   * Gets whether the plugin is currently activated.
   * 
   * @return whether the plugin is currently activated.
   * @see #setActivated(boolean)
   * @see #assertActivatedState()
   */
  public final boolean isActivated() {
    return mIsActivated;
  }


  /**
   * Sets whether the plugin is currently activated.
   * <p>
   * This method may only be called by the {@link PluginProxyManager} (that's 
   * why it is package private).
   * 
   * @param activated Whether the plugin is currently activated.
   * @see #isActivated()
   * @see #assertActivatedState()
   */
  final void setActivated(boolean activated) {
    mIsActivated = activated;
  }
  
  
  /**
   * Sets the parent frame to the plugin.
   * 
   * @param parent The parent frame to set.
   */
  abstract void setParentFrame(Frame parent);
  
  
  /**
   * Loads the settings for this plugin.
   * 
   * @param userDirectory The directory where the user data is stored.
   * @throws TvBrowserException If loading failed.
   */
  final void loadSettings(File userDirectory) throws TvBrowserException {
    try {
      doLoadSettings(userDirectory);
    }
    catch (RuntimeException exc) {
      throw new TvBrowserException(AbstractPluginProxy.class,
        "error.loading.runtimeException",
        "The plugin {0} caused an error when loading the plugin settings.",
        getInfo().getName(), exc);
    }
  }


  /**
   * Really loads the settings for this plugin.
   * 
   * @param userDirectory The directory where the user data is stored.
   * @throws TvBrowserException If loading failed.
   */
  protected abstract void doLoadSettings(File userDirectory)
    throws TvBrowserException;


  /**
   * Saves the settings for this plugin.
   * 
   * @param userDirectory The directory where the user data is stored.
   * @throws TvBrowserException If saving failed.
   */
  final void saveSettings(File userDirectory) throws TvBrowserException {
    // Check whether the plugin is activated
    if (! mIsActivated) {
      throw new TvBrowserException(AbstractPluginProxy.class,
        "error.saving.notActivated",
        "The plugin {0} can't save its settings, because it is not activated.",
        getInfo().getName());
    }
    
    // Try to save the settings
    try {
      doSaveSettings(userDirectory);
    }
    catch (RuntimeException exc) {
      throw new TvBrowserException(AbstractPluginProxy.class,
        "error.saving.runtimeException",
        "The plugin {0} caused an error when saving the plugin settings.",
        getInfo().getName(), exc);
    }
  }


  /**
   * Really saves the settings for this plugin.
   * 
   * @param userDirectory The directory where the user data is stored.
   * @throws TvBrowserException If saving failed.
   */
  protected abstract void doSaveSettings(File userDirectory)
    throws TvBrowserException;
  

  /**
   * Gets the meta information about the plugin.
   * 
   * @return The meta information about the plugin.
   */
  public final PluginInfo getInfo() {
    try {
      return doGetInfo();
    }
    catch (RuntimeException exc) {
      // NOTE: In this case we can't use the handleError method because this
      //       would cause a cyclic calling.
      
      String msg = mLocalizer.msg("error.getInfo", "The plugin {0} caused an " +
          "error when getting the plugin information.", getClass().getName());
      ErrorHandler.handle(msg, exc);
      
      return new PluginInfo();
    }
  }


  /**
   * Really gets the meta information about the plugin.
   * 
   * @return The meta information about the plugin.
   */
  protected abstract PluginInfo doGetInfo();


  /**
   * Gets whether the plugin supports receiving programs from other plugins.
   * 
   * @return Whether the plugin supports receiving programs from other plugins.
   * @see #receivePrograms(Program[])
   */
  public final boolean canReceivePrograms() {
    try {
      return doCanReceivePrograms();
    }
    catch (RuntimeException exc) {
      handlePluginException(exc);
      return false;
    }
  }


  /**
   * Really gets whether the plugin supports receiving programs from other
   * plugins.
   * 
   * @return Whether the plugin supports receiving programs from other plugins.
   * @see #receivePrograms(Program[])
   */
  protected abstract boolean doCanReceivePrograms();


  /**
   * Receives a list of programs from another plugin.
   * 
   * @param programArr The programs passed from the other plugin.
   * @see #canReceivePrograms()
   */
  public final void receivePrograms(Program[] programArr) {
    try {
      assertActivatedState();
      doReceivePrograms(programArr);
    }
    catch (Exception exc) {
      handlePluginException(exc);
    }
  }


  /**
   * Really receives a list of programs from another plugin.
   * 
   * @param programArr The programs passed from the other plugin.
   * @see #canReceivePrograms()
   */
  protected abstract void doReceivePrograms(Program[] programArr);


  /**
   * Gets the SettingsTab object, which is added to the settings-window.
   * 
   * @return the SettingsTab object or <code>null</code> if the plugin does not
   *         provide this feature.
   */
  public final SettingsTab getSettingsTab() {
    try {
      assertActivatedState();
      return doGetSettingsTab();
    }
    catch (Exception exc) {
      handlePluginException(exc);
      return null;
    }
  }


  /**
   * Rally gets the SettingsTab object, which is added to the settings-window.
   * 
   * @return the SettingsTab object or <code>null</code> if the plugin does not
   *         provide this feature.
   */
  protected abstract SettingsTab doGetSettingsTab();

  
  /**
   * Gets the actions for the context menu of a program.
   * 
   * @param program The program the context menu will be shown for.
   * @return the actions this plugin provides for the given program or
   *         <code>null</code> if the plugin does not provide this feature.
   */
  public final Action[] getContextMenuActions(Program program) {
    try {
      return goGetContextMenuActions(program);
    }
    catch (RuntimeException exc) {
      handlePluginException(exc);
      return null;
    }
  }

  
  /**
   * Really gets the actions for the context menu of a program.
   * 
   * @param program The program the context menu will be shown for.
   * @return the actions this plugin provides for the given program or
   *         <code>null</code> if the plugin does not provide this feature.
   * 
   * @see #getContextMenuActions(Program)
   */
  protected abstract Action[] goGetContextMenuActions(Program program);

  
  /**
   * Gets the action to use for the main menu and the toolbar.
   *
   * @return the action to use for the menu and the toolbar or <code>null</code>
   *         if the plugin does not provide this feature.
   */
  public final Action getButtonAction() {
    try {
      return doGetButtonAction();
    }
    catch (RuntimeException exc) {
      handlePluginException(exc);
      return null;
    }
  }

  
  /**
   * Really gets the action to use for the main menu and the toolbar.
   *
   * @return the action to use for the menu and the toolbar or <code>null</code>
   *         if the plugin does not provide this feature.
   * 
   * @see #getButtonAction()
   */
  protected abstract Action doGetButtonAction();
  
  
  /**
   * Gets the icon to use for marking programs in the program table.
   * 
   * @return the icon to use for marking programs in the program table.
   */
  public final Icon getMarkIcon() {
    try {
      return doGetMarkIcon();
    }
    catch (RuntimeException exc) {
      handlePluginException(exc);
      return null;
    }
  }

  
  /**
   * Really gets the icon to use for marking programs in the program table.
   * 
   * @return the icon to use for marking programs in the program table.
   */
  protected abstract Icon doGetMarkIcon();
  
  
  /**
   * Gets the description text for the program table icons provided by this
   * Plugin.
   * <p>
   * Return <code>null</code> if your plugin does not provide this feature.
   * 
   * @return The description text for the program table icons or
   *         <code>null</code> if the plugin does not provide this feature.
   *
   * @see #getProgramTableIcons(Program)
   */
  public final String getProgramTableIconText() {
    try {
      return doGetProgramTableIconText();
    }
    catch (RuntimeException exc) {
      handlePluginException(exc);
      return null;
    }
  }

  
  /**
   * Gets the description text for the program table icons provided by this
   * Plugin.
   * <p>
   * Return <code>null</code> if your plugin does not provide this feature.
   * 
   * @return The description text for the program table icons or
   *         <code>null</code> if the plugin does not provide this feature.
   *
   * @see #getProgramTableIcons(Program)
   */
  protected abstract String doGetProgramTableIconText();


  /**
   * Gets the icons this Plugin provides for the given program. These icons will
   * be shown in the program table under the start time.
   * <p>
   * Return <code>null</code> if your plugin does not provide this feature.
   * 
   * @param program The programs to get the icons for.
   * @return The icons for the given program or <code>null</code>.
   *
   * @see #getProgramTableIconText()
   */
  public final Icon[] getProgramTableIcons(Program program) {
    try {
      assertActivatedState();
      return doGetProgramTableIcons(program);
    }
    catch (Exception exc) {
      handlePluginException(exc);
      return null;
    }
  }


  /**
   * Really gets the icons this Plugin provides for the given program. These
   * icons will be shown in the program table under the start time.
   * <p>
   * Return <code>null</code> if your plugin does not provide this feature.
   * 
   * @param program The programs to get the icons for.
   * @return The icons for the given program or <code>null</code>.
   *
   * @see #getProgramTableIconText()
   */
  protected abstract Icon[] doGetProgramTableIcons(Program program);


  /**
   * This method is automatically called, when the TV data update is finished.
   * 
   * @see #handleTvDataAdded(ChannelDayProgram)
   * @see #handleTvDataDeleted(ChannelDayProgram)
   */
  public final void handleTvDataUpdateFinished() {
    try {
      assertActivatedState();
      doHandleTvDataUpdateFinished();
    }
    catch (Exception exc) {
      handlePluginException(exc);
    }
  }

  
  /**
   * This method is automatically called, when the TV data update is finished.
   * 
   * @see #handleTvDataAdded(ChannelDayProgram)
   * @see #handleTvDataDeleted(ChannelDayProgram)
   */
  protected abstract void doHandleTvDataUpdateFinished();


  /**
   * This method is automatically called, when TV data was added.
   * (E.g. after an update).
   * <p>
   * The TV data may be modified by the plugin! So this method must be called
   * before new TV data is saved.
   * 
   * @param newProg The new ChannelDayProgram.
   * @see #handleTvDataDeleted(ChannelDayProgram)
   * @see #handleTvDataUpdateFinished()
   */
  public final void handleTvDataAdded(ChannelDayProgram newProg) {
    try {
      assertActivatedState();
      doHandleTvDataAdded(newProg);
    }
    catch (Exception exc) {
      handlePluginException(exc);
    }
  }

  
  /**
   * This method is automatically called, when TV data was added.
   * (E.g. after an update).
   * <p>
   * The TV data may be modified by the plugin! So this method must be called
   * before new TV data is saved.
   * 
   * @param newProg The new ChannelDayProgram.
   * @see #handleTvDataDeleted(ChannelDayProgram)
   * @see #handleTvDataUpdateFinished()
   */
  protected abstract void doHandleTvDataAdded(ChannelDayProgram newProg);
  
  
  /**
   * This method is automatically called, when TV data was deleted.
   * (E.g. after an update).
   * 
   * @param oldProg The old ChannelDayProgram which was deleted.
   * @see #handleTvDataAdded(ChannelDayProgram)
   * @see #handleTvDataUpdateFinished()
   */
  public final void handleTvDataDeleted(ChannelDayProgram oldProg) {
    try {
      assertActivatedState();
      doHandleTvDataDeleted(oldProg);
    }
    catch (Exception exc) {
      handlePluginException(exc);
    }
  }

  
  /**
   * This method is automatically called, when TV data was deleted.
   * (E.g. after an update).
   * 
   * @param oldProg The old ChannelDayProgram which was deleted.
   * @see #handleTvDataAdded(ChannelDayProgram)
   * @see #handleTvDataUpdateFinished()
   */
  protected abstract void doHandleTvDataDeleted(ChannelDayProgram oldProg);
  

  /**
   * Gets the name of the plugin.
   * <p>
   * This way Plugin objects may be used directly in GUI components like JLists.
   *
   * @return the name of the plugin.
   */
  final public String toString() {
    return getInfo().getName();
  }


  /**
   * Hanles a runtime exception that was caused by the plugin.
   * 
   * @param exc The exception to handle
   */
  protected void handlePluginException(Exception exc) {
    String msg = mLocalizer.msg("error.runtimeException",
      "The plugin {0} caused an error.",
      getInfo().getName());
    ErrorHandler.handle(msg, exc);
  }


  /**
   * Checks whether the plugin is activated. If it is not an error message is
   * shown.
   * 
   * @throws TvBrowserException If the plugin is not activated
   */
  protected void assertActivatedState() throws TvBrowserException {
    if (! isActivated()) {
      throw new TvBrowserException(AbstractPluginProxy.class, "error.notActive",
        "It was attempted to call an operation of the inactive plugin {0} that "
        + "may only be called on activated plugins.", getInfo().getName());
    }
  }

}
