/*
 * TV-Browvent.ser
 * Copyright (C) 04-2003 Martin Oberhauser (darras@users.sourceforge.net)
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
package tvbrowser.ui.tray;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

import devplugin.ActionMenu;
import devplugin.Channel;
import devplugin.ChannelFilter;
import devplugin.Date;
import devplugin.Marker;
import devplugin.Program;
import devplugin.ProgramFilter;
import devplugin.SettingsItem;
import tvbrowser.TVBrowser;
import tvbrowser.core.Settings;
import tvbrowser.core.TvDataBase;
import tvbrowser.core.filters.FilterManagerImpl;
import tvbrowser.core.plugin.ButtonActionIf;
import tvbrowser.core.plugin.PluginProxy;
import tvbrowser.core.plugin.PluginProxyManager;
import tvbrowser.extras.common.InternalPluginProxyIf;
import tvbrowser.extras.common.InternalPluginProxyList;
import tvbrowser.ui.DontShowAgainOptionBox;
import tvbrowser.ui.mainframe.MainFrame;
import tvdataservice.MarkedProgramsMap;
import util.io.IOUtilities;
import util.misc.OperatingSystem;
import util.program.ProgramUtilities;
import util.ui.ScrollableMenu;
import util.ui.TVBrowserIcons;
import util.ui.UiUtilities;
import util.ui.menu.MenuUtil;

/**
 * This Class creates a SystemTray
 */
public class SystemTray {
  /** Using SystemTray ? */
  private boolean mUseSystemTray;

  /** Logger */
  private static final Logger mLog = Logger.getLogger(SystemTray.class.getName());

  /** The localizer for this class. */
  private static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(SystemTray.class);

  /** State of the Window (max/normal) */
  private static int mState;
  private boolean mMenuCreated;
  private boolean mTime24 = !Settings.propTwelveHourFormat.getBoolean();

  private Java6Tray mSystemTray;

  private JMenuItem mOpenCloseMenuItem, mRestartMenuItem, mQuitMenuItem, mConfigure;

  private JPopupMenu mTrayMenu;
  private Thread mClickTimer;

  private JMenu mPluginsMenu;
  private static JDialog mTrayParent;

  /**
   * Creates the SystemTray
   *
   */
  public SystemTray() {
  }

  /**
   * Initializes the System
   *
   * @return true if successful
   */
  public boolean initSystemTray() {

    mUseSystemTray = false;

    mSystemTray = Java6Tray.create();

    if (mSystemTray != null) {
      mUseSystemTray = mSystemTray.init(MainFrame.getInstance(), TVBrowser.MAINWINDOW_TITLE);
      mLog.info("using default system tray");
    } else {
      mUseSystemTray = false;
      Settings.propTrayIsEnabled.setBoolean(false);
    }

    if (mUseSystemTray) {
      mTrayParent = new JDialog();
      mTrayParent.setTitle("Tray-Menu-Program-Popup");

      mTrayParent.setSize(0, 0);
      mTrayParent.setUndecorated(true);
      mTrayParent.setAlwaysOnTop(true);
      mTrayParent.setVisible(false);
    }

    return mUseSystemTray;
  }
  
  private NativeKeyListener mGlobalKeyToggleListener = null;
  
  public synchronized void registerGlobalKeyToggle() {
    if(OperatingSystem.is64Bit() && Settings.propTrayGlobalKeyToggle.getBoolean() && mGlobalKeyToggleListener == null) {
      try {
        if(!GlobalScreen.isNativeHookRegistered()) {
          // Get the logger for "org.jnativehook" and set the level to off.
          Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
          logger.setLevel(Level.OFF);
  
          // Don't forget to disable the parent handlers.
          logger.setUseParentHandlers(false);
        
          GlobalScreen.registerNativeHook();
        }
        mGlobalKeyToggleListener = new NativeKeyListener() {
          @Override
          public void nativeKeyTyped(NativeKeyEvent event) {}
           
          @Override
          public void nativeKeyReleased(NativeKeyEvent event) {}
           
          @Override
          public void nativeKeyPressed(NativeKeyEvent event) {
            if(event.getKeyCode() == NativeKeyEvent.VC_A && (event.getModifiers() & NativeKeyEvent.SHIFT_MASK) != 0
                && (event.getModifiers() & NativeKeyEvent.CTRL_MASK) != 0) {
              if((event.getModifiers() & NativeKeyEvent.ALT_MASK) != 0) {
                SwingUtilities.invokeLater(() -> {
                  MainFrame.getInstance().showFromTray(mState);
                  
                  if (Settings.propNowOnRestore.getBoolean()) {
                    MainFrame.getInstance().scrollToNow();
                  }
                });
              }
              else {
                toggleShowHide();
              }
            }
          }
        };
        GlobalScreen.addNativeKeyListener(mGlobalKeyToggleListener);
        TVBrowser.createLockGlobalToggle();
      } catch (NativeHookException e) {
        mGlobalKeyToggleListener = null;
      }
    }
  }
  
  public synchronized void unregisterGlobalKeyToggle() {
    if(mGlobalKeyToggleListener != null) {
      TVBrowser.deleteLockGlobalToggle();
      GlobalScreen.removeNativeKeyListener(mGlobalKeyToggleListener);
      mGlobalKeyToggleListener = null;
    }
  }

  /**
   * Creates the Menus
   *
   */
  public void createMenus() {
    if (!mUseSystemTray) {
      return;
    }
    if (!mMenuCreated) {
      mLog.info("platform independent mode is OFF");

      mOpenCloseMenuItem = new JMenuItem(mLocalizer.msg("menu.open", "Open"));
      Font f = mOpenCloseMenuItem.getFont();

      mOpenCloseMenuItem.setFont(f.deriveFont(Font.BOLD));
      mRestartMenuItem = new JMenuItem(mLocalizer.msg("menu.restart", "Restart"), TVBrowserIcons.restart(TVBrowserIcons.SIZE_SMALL));
      mQuitMenuItem = new JMenuItem(mLocalizer.msg("menu.quit", "Quit"), TVBrowserIcons.quit(TVBrowserIcons.SIZE_SMALL));
      mConfigure = new JMenuItem(mLocalizer.msg("menu.configure", "Configure"), TVBrowserIcons
          .preferences(TVBrowserIcons.SIZE_SMALL));

      mConfigure.addActionListener(e -> {
        MainFrame.getInstance().showSettingsDialog(SettingsItem.TRAY);
      });

      mOpenCloseMenuItem.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent e) {
          toggleShowHide();
        }
      });
      
      mRestartMenuItem.addActionListener(e -> {
        TVBrowser.addRestart();
        MainFrame.getInstance().quit();
      });

      mQuitMenuItem.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent e) {
          MainFrame.getInstance().quit();
        }
      });

      mSystemTray.addLeftClickAction(e -> {
        if (mClickTimer == null || !mClickTimer.isAlive()) {
          toggleShowHide();
        }
      });

      MainFrame.getInstance().addComponentListener(new ComponentListener() {

        public void componentResized(ComponentEvent e) {
          int state = MainFrame.getInstance().getExtendedState();

          if ((state & Frame.MAXIMIZED_BOTH) == Frame.MAXIMIZED_BOTH) {
            mState = Frame.MAXIMIZED_BOTH;
          } else if ((state & Frame.ICONIFIED) != Frame.ICONIFIED) {
            mState = Frame.NORMAL;
          }
        }

        public void componentHidden(ComponentEvent e) {
        }

        public void componentMoved(ComponentEvent e) {
        }

        public void componentShown(ComponentEvent e) {
        }
      });

      MainFrame.getInstance().addWindowListener(new java.awt.event.WindowAdapter() {

        public void windowOpened(WindowEvent e) {
          toggleOpenCloseMenuItem(false);
        }

        public void windowClosing(java.awt.event.WindowEvent evt) {
          if (Settings.propOnlyMinimizeWhenWindowClosing.getBoolean()) {
            toggleShowHide();
          } else {
            if(DontShowAgainOptionBox.showOptionDialog("minimizeToTrayClickQuestion", UiUtilities.getLastModalChildOf(MainFrame.getInstance()), mLocalizer.msg("minimizeToTray","Should TV-Browser be closed in future on click instead of beeing mimimized to tray?"),mLocalizer.msg("minimizeToTrayTitle","Close TV-Browser?"), JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION, null, null, null) == JOptionPane.NO_OPTION) {
              Settings.propOnlyMinimizeWhenWindowClosing.setBoolean(true);
              toggleShowHide();
            }
            else {
              MainFrame.getInstance().quit();
            }
          }
        }

        public void windowDeiconified(WindowEvent e) {
          toggleOpenCloseMenuItem(false);
        }

        public void windowIconified(java.awt.event.WindowEvent evt) {
          if (Settings.propTrayMinimizeTo.getBoolean()) {
            MainFrame.getInstance().setVisible(false);
          }
          toggleOpenCloseMenuItem(true);
        }
      });

      toggleOpenCloseMenuItem(false);

      mTrayMenu = new JPopupMenu();

      mSystemTray.addRightClickAction(e -> {
        buildMenu();
      });
      mSystemTray.setTrayPopUp(mTrayMenu);

      mSystemTray.setVisible(Settings.propTrayIsEnabled.getBoolean());

      if (!Settings.propTrayUseSpecialChannels.getBoolean()
          && Settings.propTraySpecialChannels.getChannelArray().length == 0) {
        Channel[] channelArr = Settings.propSubscribedChannels.getChannelArray();
        Channel[] tempArr = new Channel[channelArr.length > 10 ? 10 : channelArr.length];
        System.arraycopy(channelArr, 0, tempArr, 0, tempArr.length);

        Settings.propTraySpecialChannels.setChannelArray(tempArr);
      }
      mMenuCreated = true;
    } else {
      mSystemTray.setVisible(Settings.propTrayIsEnabled.getBoolean());
    }
  }

  /**
   * Sets the visibility of the tray.
   *
   * @param value
   *          True if visible.
   */
  public void setVisible(boolean value) {
    mSystemTray.setVisible(value);
  }

  /**
   * Shows a balloon tip on the TV-Browser tray icon.
   * <p>
   *
   * @param caption
   *          The caption of the displayed message.
   * @param message
   *          The message to display in the balloon tip.
   * @param messageType
   *          The type of the displayed ballon tip.
   * @return If the balloon tip could be shown.
   */
  public boolean showBalloonTip(String caption, String message, java.awt.TrayIcon.MessageType messageType) {
    return mSystemTray.showBalloonTip(caption, message, messageType);
  }

  private void buildMenu() {
    mTrayMenu.removeAll();
    mTrayMenu.add(mOpenCloseMenuItem);
    mTrayMenu.addSeparator();

    mPluginsMenu = createPluginsMenu();

    mTrayMenu.add(mPluginsMenu);
    mTrayMenu.addSeparator();

    mTrayMenu.addPopupMenuListener(new PopupMenuListener() {
      public void popupMenuCanceled(PopupMenuEvent e) {
      }

      public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
        //TODO Maybe it could be useful to suppress the info dialog of Plugins until a user interaction 
       //FavoritesPlugin.getInstance().showInfoDialog();
      }

      public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
        mPluginsMenu.setEnabled(!UiUtilities.containsModalDialogChild(MainFrame.getInstance()));
      }
    });

    if (Settings.propTrayOnTimeProgramsEnabled.getBoolean() || Settings.propTrayNowProgramsEnabled.getBoolean()
        || Settings.propTraySoonProgramsEnabled.getBoolean() || Settings.propTrayImportantProgramsEnabled.getBoolean()) {
      searchForToAddingPrograms();
    }

    if (Settings.propTrayOnTimeProgramsEnabled.getBoolean()) {
      if (!Settings.propTrayNowProgramsInSubMenu.getBoolean() && Settings.propTrayNowProgramsEnabled.getBoolean()
          && Settings.propTraySoonProgramsEnabled.getBoolean()) {
        mTrayMenu.addSeparator();
      }
      addTimeInfoMenu();
    }

    if (Settings.propTrayNowProgramsEnabled.getBoolean() || Settings.propTraySoonProgramsEnabled.getBoolean()
        || Settings.propTrayOnTimeProgramsEnabled.getBoolean()) {
      mTrayMenu.addSeparator();
    }
    mTrayMenu.add(mConfigure);
    mTrayMenu.addSeparator();
    mTrayMenu.add(mRestartMenuItem);
    mTrayMenu.addSeparator();
    mTrayMenu.add(mQuitMenuItem);
  }

  /**
   * Searches the programs to show in the Tray.
   */
  private void searchForToAddingPrograms() {
    // show the now/soon running programs
    try {
      Channel[] channels = Settings.propSubscribedChannels.getChannelArray();

      JComponent subMenu;

      // Put the programs in a sub menu?
      if (Settings.propTrayNowProgramsInSubMenu.getBoolean() && Settings.propTrayNowProgramsEnabled.getBoolean()) {
        subMenu = new ScrollableMenu(mLocalizer.msg("menu.programsNow", "Now running programs"));
      } else {
        subMenu = mTrayMenu;
      }

      ArrayList<ProgramMenuItem> programs = new ArrayList<ProgramMenuItem>();
      ArrayList<ProgramMenuItem> additional = new ArrayList<ProgramMenuItem>();
      ArrayList<ProgramMenuItem> nextPrograms = new ArrayList<ProgramMenuItem>();
      ArrayList<ProgramMenuItem> nextAdditionalPrograms = new ArrayList<ProgramMenuItem>();

      /*
       * Fill the ArrayList to support storing the programs on the correct
       * position in the list.
       */
      for (int i = 0; i < Settings.propTraySpecialChannels.getChannelArray().length; i++) {
        programs.add(i, null);
        nextPrograms.add(i, null);
      }
      
      /*
       * Search through _ALL_ channels.
       */
      Date currentDate = Date.getCurrentDate();
      for (Channel channel : channels) {
        ArrayList<Program> prog = ProgramUtilities.getJointProgramListForYesterdayTodayTomorrow(currentDate, channel, true);
        
        boolean nextAdded = false;
        boolean nowAdded = false;
        
        for(int i = 0; i < prog.size(); i++) {
          Program test = prog.get(i);
          
          if(!nowAdded && test.isOnAir()) {
            Program now = test;
            
            int start = test.getStartTime();
            int end = test.getStartTime() + test.getLength();
            
            int j = i+1;
            
            while(j < prog.size() && prog.get(j).isOnAir()) {
              Program nowTest = prog.get(j++);
              
              int testStart = nowTest.getStartTime();
              
              if(now.getDate().addDays(1).compareTo(nowTest.getDate()) == 0) {
                testStart += 24 * 60;
              }
              
              if(testStart >= start && (testStart + nowTest.getLength()) < end) {
                now = nowTest;
                start = now.getStartTime();
                end = now.getStartTime() + now.getLength();
              }
            }
            
            addToNowRunning(now, programs, additional);
            
            nowAdded = true;
          }
          
          if(!nextAdded && (test.getStartTime() > IOUtilities.getMinutesAfterMidnight() || test.getDate().compareTo(currentDate) >= 0)) {
            int j = i+1;
            Program next = test;
            
            while(!addToNext(next, nextPrograms, nextAdditionalPrograms) && j < prog.size()) {
              next = prog.get(j++);
            }
            
            nextAdded = true;
          }
          
          if((nowAdded || test.getStartTime() > IOUtilities.getMinutesAfterMidnight()) && nextAdded) {
            break;
          }
        }
      }

      // Show important programs?
      if (Settings.propTrayImportantProgramsEnabled.getBoolean()) {
        if (Settings.propTrayImportantProgramsInSubMenu.getBoolean()) {
          mTrayMenu.add(addToImportantMenu(new ScrollableMenu(mLocalizer.msg("menu.programsImportant",
              "Important programs"))));
        } else {
          addToImportantMenu(mTrayMenu);
        }
      }

      /*
       * if there are running programs and they should be displayed add them to
       * the menu.
       */

      if (Settings.propTrayImportantProgramsEnabled.getBoolean()) {
        mTrayMenu.addSeparator();
      }

      boolean now = false;

      // filter duplicates from additional programs
      for (int i = additional.size()-1; i >= 0; i--) {
        ProgramMenuItem addItem = additional.get(i);
        
        boolean equal = false;
        for (ProgramMenuItem item : programs) {
          if (item != null && item.getProgram().equals(addItem.getProgram())) {
            equal = true;
            break;
          }
        }
        if (equal) {
          additional.remove(addItem);
        }
      }

      if (Settings.propTrayNowProgramsEnabled.getBoolean() && (programs.size() > 0 || additional.size() > 0)) {
        addMenuItems(subMenu, programs);
        
        for(ProgramMenuItem item : additional) {
          subMenu.add(item);
        }

        now = true;

        if (subMenu instanceof JMenu) {
          addNoProgramsItem((JMenu) subMenu);
        }
      }

      if (Settings.propTrayNowProgramsInSubMenu.getBoolean() && Settings.propTrayNowProgramsEnabled.getBoolean()) {
        mTrayMenu.add(subMenu);
      }

      if (Settings.propTraySoonProgramsEnabled.getBoolean()
          && (!nextPrograms.isEmpty() || !nextAdditionalPrograms.isEmpty())) {

        final JMenu next = new ScrollableMenu(now ? mLocalizer.msg("menu.programsSoon", "Soon runs") : mLocalizer.msg(
            "menu.programsSoonAlone", "Soon runs"));

        addMenuItems(next, nextPrograms);
        
        for(ProgramMenuItem item : nextAdditionalPrograms) {
          next.add(item);
        }
        
        addNoProgramsItem(next);

        mTrayMenu.add(next);
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static final class FilterAndMarkedAcceptFilter implements ProgramFilter {
    private ProgramFilter mFilter;
    
    public FilterAndMarkedAcceptFilter(ProgramFilter filter) {
      mFilter = filter;
    }
    
    @Override
    public boolean accept(Program program) {
      Marker[] test = program.getMarkerArr();
      
      return (test != null && test.length > 0) || mFilter.accept(program);
    }

    @Override
    public String getName() {
      return "TRAY ONLY MARKED OR FILTER";
    }
  }
  
  /**
   * Adds the important programs to the menu.
   *
   * @param menu
   *          The menu to on
   * @param normalPrograms
   * @return The filled menu menu.
   */
  private JComponent addToImportantMenu(JComponent menu) {
    ProgramFilter filter = MainFrame.getInstance().getProgramFilter();
    
    if(Settings.propTrayFilterNot.getBoolean()) {
      filter = FilterManagerImpl.getInstance().getAllFilter();
    }
    else if(Settings.propTrayFilterNotMarked.getBoolean()) {
      filter = new FilterAndMarkedAcceptFilter(filter);
    }
    
    Program[] p = MarkedProgramsMap.getInstance().getTimeSortedProgramsForTray(filter, Settings.propTrayImportantProgramsPriority.getInt(),
        Settings.propTrayImportantProgramsSize.getInt(), !Settings.propTrayNowProgramsEnabled.getBoolean());

    boolean added = false;

    if (p.length > 0) {
      for (int i = 0; i < p.length; i++) {
        menu.add(new ProgramMenuItem(p[i], ProgramMenuItem.IMPORTANT_TYPE, -1, i));
        added = true;
      }
    }

    if (p.length == 0 || !added) {
      JMenuItem item = new JMenuItem(mLocalizer.msg("menu.noImportantPrograms", "No important programs found."));

      item.setEnabled(false);
      item.setForeground(Color.red);
      menu.add(item);
    }

    return menu;
  }

  /**
   * Add the time info menu.
   */
  private void addTimeInfoMenu() {
    JComponent time;

    if (Settings.propTrayOnTimeProgramsInSubMenu.getBoolean()) {
      time = new JMenu(mLocalizer.msg("menu.programsAtTime", "Programs at time"));
      mTrayMenu.add(time);
    } else {
      time = mTrayMenu;
    }

    int[] tempTimes = Settings.propTimeButtons.getIntArray();

    ArrayList<Integer> today = new ArrayList<Integer>();
    ArrayList<Integer> tomorrow = new ArrayList<Integer>();

    for (int tempTime : tempTimes) {
      if (tempTime < IOUtilities.getMinutesAfterMidnight()) {
        tomorrow.add(tempTime);
      } else {
        today.add(tempTime);
      }
    }

    int[] times;

    if (tomorrow.isEmpty() || today.isEmpty()) {
      times = tempTimes;
    } else {
      times = new int[tempTimes.length + 1];

      int j = 0;

      for (int i = 0; i < today.size(); i++) {
        times[j] = today.get(i).intValue();
        j++;
      }

      times[j] = -1;
      j++;

      for (int i = 0; i < tomorrow.size(); i++) {
        times[j] = tomorrow.get(i).intValue();
        j++;
      }
    }

    for (int value : times) {
      if (value == -1) {
        if (time instanceof JMenu) {
          ((JMenu) time).addSeparator();
        } else {
          ((JPopupMenu) time).addSeparator();
        }
      } else {
        final int fvalue = value;

        final JMenu menu = new ScrollableMenu(IOUtilities.timeToString(value) + " "
            + (mTime24 ? mLocalizer.msg("menu.time", "") : ""));

        if (value < IOUtilities.getMinutesAfterMidnight()) {
          menu.setText(menu.getText() + " " + mLocalizer.msg("menu.tomorrow", ""));
        }

        menu.addMenuListener(new MenuListener() {
          public void menuSelected(MenuEvent e) {
            createTimeProgramMenu(menu, fvalue);
          }

          public void menuCanceled(MenuEvent e) {
          }

          public void menuDeselected(MenuEvent e) {
          }
        });
        time.add(menu);
      }
    }
  }

  /**
   * Creates the entries of a time menu.
   *
   * @param menu
   *          The menu to put the programs on
   * @param time
   *          The time on which the programs are allowed to run.
   */
  private void createTimeProgramMenu(JMenu menu, int time) {
    // the menu is empty, so search for the programs at the time
    if (menu.getMenuComponentCount() < 1) {
      ProgramFilter filter = MainFrame.getInstance().getProgramFilter();
      
      if(Settings.propTrayFilterNot.getBoolean()) {
        filter = FilterManagerImpl.getInstance().getAllFilter();
      }
      else if(Settings.propTrayFilterNotMarked.getBoolean()) {
        filter = new FilterAndMarkedAcceptFilter(filter);
      }
      
      Channel[] c = Settings.propSubscribedChannels.getChannelArray();

      ArrayList<ProgramMenuItem> programs = new ArrayList<ProgramMenuItem>();
      ArrayList<ProgramMenuItem> programsNext = new ArrayList<ProgramMenuItem>();
      ArrayList<ProgramMenuItem> additional = new ArrayList<ProgramMenuItem>();
      ArrayList<ProgramMenuItem> additionalNext = new ArrayList<ProgramMenuItem>();

      for (int i = 0; i < Settings.propTraySpecialChannels.getChannelArray().length; i++) {
        programs.add(i, null);
      }
      for (int i = 0; i < Settings.propTraySpecialChannels.getChannelArray().length; i++) {
        programsNext.add(i, null);
      }
      
      Date currentDate = Date.getCurrentDate();
      for (Channel ch : c) {
        Iterator<Program> it = null;
        int day = 0;

        try {
          it = ProgramUtilities.getJointProgramIteratorFor(
              currentDate.addDays((time < IOUtilities.getMinutesAfterMidnight() ? ++day : day)), ch);
        } catch (Exception ee) {
        }

        int count = 0;

        while (it != null && it.hasNext()) {
          Program p = it.next();

          int start = p.getStartTime();
          int end = p.getStartTime() + p.getLength();

          if (start <= time && time < end && filter.accept(p)) {
            if (isOnChannelList(ch)) {
              programs.set(getIndexOfChannel(ch), new ProgramMenuItem(p, ProgramMenuItem.ON_TIME_TYPE, time, -1));
              
              if(it.hasNext()) {
                programsNext.set(getIndexOfChannel(ch), new ProgramMenuItem(it.next(), ProgramMenuItem.AFTER_TYPE, time, -1));
              }
              else {
                try {
                  programsNext.set(getIndexOfChannel(ch), new ProgramMenuItem(TvDataBase.getInstance().getDayProgram(currentDate.addDays(day+1),ch).getProgramAt(0), ProgramMenuItem.AFTER_TYPE, time, -1));
                }catch(Exception ee) {}
              }
            }
            else if (p.getMarkerArr().length > 0
                && p.getMarkPriority() >= Settings.propTrayImportantProgramsPriority.getInt()) {
              additional.add(new ProgramMenuItem(p, ProgramMenuItem.ON_TIME_TYPE, time, -1));
              
              if(it.hasNext()) {
                Program test = it.next();
                
                if(test.getMarkerArr().length > 0 && test.getMarkPriority() >= Settings.propTrayImportantProgramsPriority.getInt()) {
                  additionalNext.add(new ProgramMenuItem(test, ProgramMenuItem.AFTER_TYPE, time, -1));
                }
              }
              else {
                try {
                  Program test = ProgramUtilities.getJointProgramIteratorFor(currentDate.addDays(day+1),ch).next();
                  
                  if(test.getMarkerArr().length > 0 && test.getMarkPriority() >= Settings.propTrayImportantProgramsPriority.getInt()) {
                    additionalNext.add(new ProgramMenuItem(test, ProgramMenuItem.AFTER_TYPE, time, -1));
                  }
                }catch(Exception ee) {}
              }
            }
          } else if (start > time && day == 1 && count == 0) {

            int temptime = time + 24 * 60;
            try {
              Iterator<Program> dayProg = ProgramUtilities.getJointProgramIteratorFor(currentDate, ch);
              
              while(dayProg.hasNext()) {
                p = dayProg.next();
              }

              start = p.getStartTime();
              end = p.getStartTime() + p.getLength();

              if (start <= temptime && temptime < end && filter.accept(p)) {
                if (isOnChannelList(ch)) {
                  programs.set(getIndexOfChannel(ch), new ProgramMenuItem(p, ProgramMenuItem.ON_TIME_TYPE, time, -1));
                  
                  if(it.hasNext()) {
                    programsNext.set(getIndexOfChannel(ch), new ProgramMenuItem(it.next(), ProgramMenuItem.AFTER_TYPE, time, -1));
                  }
                  else {
                    try {
                      programsNext.set(getIndexOfChannel(ch), new ProgramMenuItem(TvDataBase.getInstance().getDayProgram(currentDate.addDays(day+1),ch).getProgramAt(0), ProgramMenuItem.AFTER_TYPE, time, -1));
                    }catch(Exception ee) {}
                  }
                }
                else if (p.getMarkerArr().length > 0
                    && p.getMarkPriority() >= Settings.propTrayImportantProgramsPriority.getInt()) {
                  additional.add(new ProgramMenuItem(p, ProgramMenuItem.ON_TIME_TYPE, time, -1));
                  
                  if(it.hasNext()) {
                    Program test = it.next();
                    
                    if(test.getMarkerArr().length > 0 && test.getMarkPriority() >= Settings.propTrayImportantProgramsPriority.getInt()) {
                      additionalNext.add(new ProgramMenuItem(test, ProgramMenuItem.AFTER_TYPE, time, -1));
                    }
                  }
                  else {
                    try {
                      Program test = ProgramUtilities.getJointProgramIteratorFor(currentDate.addDays(day+1),ch).next();
                      
                      if(test.getMarkerArr().length > 0 && test.getMarkPriority() >= Settings.propTrayImportantProgramsPriority.getInt()) {
                        additionalNext.add(new ProgramMenuItem(test, ProgramMenuItem.AFTER_TYPE, time, -1));
                      }
                    }catch(Exception ee) {}
                  }
                }
              }
            } catch (Exception ee) {
            }
          } else if (start > time) {
            break;
          }

          count++;
        }
      }

      addMenuItems(menu, programs);
      
      for(ProgramMenuItem item : additional) {
        menu.add(item);
      }
      
      final JMenu next = new ScrollableMenu(mLocalizer.msg("menu.programsAfter", "After that"));
      menu.add(next);
      
      addMenuItems(next, programsNext);
      
      for(ProgramMenuItem item : additionalNext) {
        next.add(item);
      }
      
      addNoProgramsItem(menu);
    }
  }

  /**
   * add a limited number of items to the current tray menu (or sub menu)
   *
   * @param menu
   * @param items
   */
  private void addMenuItems(final JMenu menu, final ArrayList<ProgramMenuItem> items) {
    int maxCount = getMaxItemCount();
    for (ProgramMenuItem pItem : items) {
      if (menu.getItemCount() >= maxCount) {
        break;
      }
      if (!acceptedChannel(pItem)) {
        continue;
      }
      if (!containsProgram(menu, pItem) && menu != null && pItem != null) {
        pItem.setBackground(menu.getItemCount());
        menu.add(pItem);
      }
    }
  }

  private boolean containsProgram(final JMenu menu, ProgramMenuItem pItem) {
    for (int i = 0; i < menu.getMenuComponentCount(); i++) {
      Component comp = menu.getMenuComponent(i);
      if (comp instanceof ProgramMenuItem) {
        ProgramMenuItem oldItem = (ProgramMenuItem) comp;
        if (oldItem != null && pItem != null && oldItem.getProgram().equals(pItem.getProgram())) {
          return true;
        }
      }
    }
    return false;
  }

  private void addMenuItems(final JComponent subMenu, final ArrayList<ProgramMenuItem> programs) {
    int maxCount = getMaxItemCount();
    int itemCount = 0;
    for (ProgramMenuItem item : programs) {
      if (item != null && acceptedChannel(item)) {
        subMenu.add(item);
        itemCount++;
        if (itemCount >= maxCount) {
          break;
        }
      }
    }
  }

  private boolean acceptedChannel(final ProgramMenuItem item) {
    boolean result = false;
    
    if(item != null) {
      ChannelFilter channelFilter = MainFrame.getInstance().getChannelFilter();
      
      if (channelFilter == null) {
        result = true;
      }
      else {
        result = channelFilter.accept(item.getProgram());
      }
    }
    
    return result;
  }

  private int getMaxItemCount() {
    if (Settings.propTrayUseSpecialChannels.getBoolean()) {
      return Settings.propTraySpecialChannels.getChannelArray().length;
    }
    return 30;
  }

  private void addNoProgramsItem(JMenu menu) {
    if (menu.getItemCount() == 0) {
      JMenuItem item = new JMenuItem(mLocalizer.msg("menu.noPrograms", "No programs found."));
      item.setEnabled(false);
      menu.add(item);
    }
  }

  /**
   * Checks and adds programs to a next list.
   *
   * @param program
   *          The program to check and add.
   */
  private boolean addToNext(Program program, ArrayList<ProgramMenuItem> nextPrograms,
      ArrayList<ProgramMenuItem> nextAdditionalPrograms) {
    if (!program.isExpired() && !program.isOnAir() && (Settings.propTrayFilterNot.getBoolean() ||
        (Settings.propTrayFilterNotMarked.getBoolean() && program.getMarkerArr().length > 0) ||
        MainFrame.getInstance().getProgramFilter().accept(program))) {
      addToListInternal(program, nextPrograms, nextAdditionalPrograms, ProgramMenuItem.SOON_TYPE);
      return true;
    }
    
    return false;
  }

  private void addToListInternal(Program program, ArrayList<ProgramMenuItem> listStandard,
      ArrayList<ProgramMenuItem> listAdditional, int menuItemType) {
    // put the program on the standard list for selected channels
    // or on the additional list if there is a marking
    if (isOnChannelList(program.getChannel())) {
      listStandard.set(getIndexOfChannel(program.getChannel()),new ProgramMenuItem(program, menuItemType, -1, -1));
    }
    else if (program.getMarkerArr().length > 0
        && program.getMarkPriority() >= Settings.propTrayImportantProgramsPriority.getInt()) {
      listAdditional.add(new ProgramMenuItem(program, menuItemType, -1, -1));
    }
  }

  /**
   * Checks and adds programs to a now running list.
   *
   * @param program
   *          The program to check and add to a list.
   * @param listStandard
   *          The list with the programs on a selected channel.
   * @param listAdditional
   *          The list with the programs that are not on a selected channel, but
   *          are important.
   */
  private void addToNowRunning(Program program, ArrayList<ProgramMenuItem> listStandard,
      ArrayList<ProgramMenuItem> listAdditional) {
    if (program.isOnAir() && (Settings.propTrayFilterNot.getBoolean() ||
        (Settings.propTrayFilterNotMarked.getBoolean() && program.getMarkerArr().length > 0) ||
        MainFrame.getInstance().getProgramFilter().accept(program))) {
      addToListInternal(program, listStandard, listAdditional, ProgramMenuItem.NOW_TYPE);
    }
  }

  /**
   * Toggle the Text in the Open/Close-Menu
   *
   * @param open
   *          True, if "Open" should be displayed
   */
  private void toggleOpenCloseMenuItem(boolean open) {
    if (open) {
      mOpenCloseMenuItem.setText(mLocalizer.msg("menu.open", "Open"));
    } else {
      mOpenCloseMenuItem.setText(mLocalizer.msg("menu.close", "Close"));
    }
  }

  /**
   * Toggle Hide/Show of the MainFrame
   */
  private void toggleShowHide() {
    mClickTimer = new Thread("Click timer thread") {
      public void run() {
        try {
          sleep(200);
        } catch (InterruptedException e) {
        }
      }
    };
    mClickTimer.start();

    if (!MainFrame.getInstance().isVisible()
        || ((MainFrame.getInstance().getExtendedState() & Frame.ICONIFIED) == Frame.ICONIFIED)) {
      SwingUtilities.invokeLater(() -> {
        MainFrame.getInstance().showFromTray(mState);
        //toggleReminderState(true);

        if (Settings.propNowOnRestore.getBoolean()) {
          MainFrame.getInstance().scrollToNow();
        }
      });
      toggleOpenCloseMenuItem(false);
    } else {
      if (OperatingSystem.isWindows() || !Settings.propTrayMinimizeTo.getBoolean()) {
        MainFrame.getInstance().setExtendedState(Frame.ICONIFIED);
      }

      if (Settings.propTrayMinimizeTo.getBoolean()) {
        MainFrame.getInstance().setVisible(false);
      }

      toggleOpenCloseMenuItem(true);
    }
  }

  /**
   * Creates the Plugin-Menus
   *
   * @return Plugin-Menu
   */
  private static JMenu createPluginsMenu() {
    JMenu pluginsMenu = new JMenu(mLocalizer.msg("menu.plugins", "Plugins"));

    PluginProxy[] plugins = PluginProxyManager.getInstance().getActivatedPlugins();
    updatePluginsMenu(pluginsMenu, plugins);

    return pluginsMenu;
  }

  /**
   * @deprecated TODO: check, if we can remove this method
   * @param pluginsMenu
   * @param plugins
   */
  @Deprecated
  private static void updatePluginsMenu(JMenu pluginsMenu, PluginProxy[] plugins) {
    pluginsMenu.removeAll();

    Arrays.sort(plugins, new PluginProxy.Comparator());

    InternalPluginProxyIf[] internalPlugins = InternalPluginProxyList.getInstance().getAvailableProxys();

    for (InternalPluginProxyIf internalPlugin : internalPlugins) {
      if (internalPlugin instanceof ButtonActionIf) {
        ActionMenu action = ((ButtonActionIf) internalPlugin).getButtonAction();

        if (action != null) {
          pluginsMenu.add(MenuUtil.createMenuItem(action, false));
        }
      }
    }

    ArrayList<ActionMenu> buttonActions = new ArrayList<ActionMenu>();
    for (PluginProxy plugin : plugins) {
      ActionMenu action = plugin.getButtonAction();
      if (action != null) {
    	buttonActions.add(action); 
      }
    }
    
    if (!buttonActions.isEmpty()) {
      pluginsMenu.addSeparator();
      for (ActionMenu action : buttonActions) {
      	pluginsMenu.add(MenuUtil.createMenuItem(action, false));
	  }
    }
  }
  
  /**
   * @param ch
   *          The channel to get the index from.
   * @return The index of the channel in the tray channel list.
   */
  private int getIndexOfChannel(Channel ch) {
    Channel[] channels = Settings.propTraySpecialChannels.getChannelArray();

    for (int i = 0; i < channels.length; i++)
      if (ch.equals(channels[i]))
        return i;

    return -1;
  }

  /**
   * @param ch
   *          The channel to check.
   * @return True if the channel is on the tray channel list.
   */
  private boolean isOnChannelList(Channel ch) {
    Channel[] channels = Settings.propTraySpecialChannels.getChannelArray();

    for (Channel channel : channels) {
      if (ch.equals(channel)) {
        return true;
      }
    }

    return false;
  }
  
  /**
   * Is the Tray activated and used?
   *
   * @return is Tray used?
   */
  public boolean isTrayUsed() {
    return mUseSystemTray;
  }

  protected static JDialog getProgamPopupParent() {
    return mTrayParent;
  }
}