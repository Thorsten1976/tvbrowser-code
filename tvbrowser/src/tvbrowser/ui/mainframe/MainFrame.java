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
 *
 * CVS information:
 *  $RCSfile$
 *   $Source$
 *     $Date$
 *   $Author$
 * $Revision$
 */

package tvbrowser.ui.mainframe;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;
import java.util.logging.Level;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.WindowConstants;

import com.jgoodies.forms.layout.Sizes;

import tvbrowser.TVBrowser;
import tvbrowser.core.ChannelList;
import tvbrowser.core.DateListener;
import tvbrowser.core.Settings;
import tvbrowser.core.TvDataBase;
import tvbrowser.core.TvDataUpdater;
import tvbrowser.core.filters.FilterComponent;
import tvbrowser.core.filters.FilterComponentList;
import tvbrowser.core.filters.FilterList;
import tvbrowser.core.filters.FilterManagerImpl;
import tvbrowser.core.filters.ShowAllFilter;
import tvbrowser.core.filters.filtercomponents.ChannelFilterComponent;
import tvbrowser.core.plugin.PluginProxyManager;
import tvbrowser.core.tvdataservice.TvDataServiceProxy;
import tvbrowser.core.tvdataservice.TvDataServiceProxyManager;
import tvbrowser.extras.favoritesplugin.FavoritesPlugin;
import tvbrowser.extras.reminderplugin.ReminderPlugin;
import tvbrowser.ui.aboutbox.AboutBox;
import tvbrowser.ui.filter.dlgs.SelectFilterDlg;
import tvbrowser.ui.finder.FinderPanel;
import tvbrowser.ui.licensebox.LicenseBox;
import tvbrowser.ui.mainframe.searchfield.SearchField;
import tvbrowser.ui.mainframe.searchfield.SearchFilter;
import tvbrowser.ui.mainframe.toolbar.ContextMenu;
import tvbrowser.ui.mainframe.toolbar.DefaultToolBarModel;
import tvbrowser.ui.mainframe.toolbar.MoreButton;
import tvbrowser.ui.mainframe.toolbar.ToolBar;
import tvbrowser.ui.pluginview.PluginView;
import tvbrowser.ui.programtable.DefaultProgramTableModel;
import tvbrowser.ui.programtable.FilterPanel;
import tvbrowser.ui.programtable.KeyboardAction;
import tvbrowser.ui.programtable.ProgramTable;
import tvbrowser.ui.programtable.ProgramTableScrollPane;
import tvbrowser.ui.settings.SettingsDialog;
import tvbrowser.ui.update.PluginAutoUpdater;
import tvbrowser.ui.update.SoftwareUpdateDlg;
import tvbrowser.ui.update.SoftwareUpdateItem;
import tvbrowser.ui.update.SoftwareUpdater;
import util.browserlauncher.Launch;
import util.exc.ErrorHandler;
import util.io.IOUtilities;
import util.misc.OperatingSystem;
import util.ui.Localizer;
import util.ui.UiUtilities;
import util.ui.DontShowAgainMessageBox;
import util.ui.progress.Progress;
import util.ui.progress.ProgressWindow;
import util.ui.view.Node;
import devplugin.Channel;
import devplugin.ChannelDayProgram;
import devplugin.Date;
import devplugin.Plugin;
import devplugin.Program;
import devplugin.ProgramFilter;
import devplugin.ProgressMonitor;
import devplugin.SettingsItem;

/**
 * TV-Browser
 * 
 * @author Martin Oberhauser
 */
public class MainFrame extends JFrame implements DateListener {

  private static java.util.logging.Logger mLog = java.util.logging.Logger
      .getLogger(tvbrowser.TVBrowser.class.getName());

  /** The localizer for this class. */
  public static final util.ui.Localizer mLocalizer = util.ui.Localizer
      .getLocalizerFor(MainFrame.class);

  private Node mTimebuttonsNode, mDateNode, mRootNode, mChannelNode;

  private Node mPluginsNode;

  private JDialog mConfigAssistantDialog;

  private SoftwareUpdateItem[] mSoftwareUpdateItems = null;

  private ProgramTableScrollPane mProgramTableScrollPane;

  private DefaultProgramTableModel mProgramTableModel;

  private Thread downloadingThread;

  private JPanel jcontentPane;

  private DefaultToolBarModel mToolBarModel;

  private ToolBar mToolBar;
  private JPanel mToolBarPanel;
  private SearchField mSearchField;

  private StatusBar mStatusBar;

  private FinderPanel mFinderPanel;

  private static MainFrame mSingleton;

  private ChannelChooserPanel mChannelChooser;

  private MenuBar mMenuBar;

  private Component mCenterComponent;

  private boolean mIsVisible;
  
  private static boolean mShuttingDown = false;
  private static boolean mStarting = true;
  
  private Node mMainframeNode;

  private Node mNavigationNode;

  private Node mDateChannelNode;

  private Date mCurrentDay;

  private PluginView mPluginView;
  
  private int mXPos, mYPos, mWidth, mHeight;

  /** Panel that Displays current Filter-Name */
  private FilterPanel mFilterPanel;

  private TimeChooserPanel mTimeChooserPanel;
  
  /** Store the Viewposition if a Filter is selected*/
  private Point mStoredViewPosition;
  
  private String mCurrentFilterName;
  
  private int mLastTimerMinutesAfterMidnight;
  
  private static Date[] mChannelDateArr;
  private static int[] mOnAirRowProgramsArr;

  private boolean mSettingsWillBeOpened;
  
  private long mLastAutoUpdateRun;
  
  private int mAutoDownloadTimer;
  
  private MainFrame() {
    super(TVBrowser.MAINWINDOW_TITLE);
    mIsVisible = false;
    mSettingsWillBeOpened = false;
    
    mAutoDownloadTimer = -1;
    mLastTimerMinutesAfterMidnight = -1;
    mLastAutoUpdateRun = System.currentTimeMillis();
    
    mChannelDateArr = null;
    mOnAirRowProgramsArr = null;
    mStatusBar = new StatusBar();

    if (OperatingSystem.isMacOs()) {
      /* create the menu bar for MacOS X */
      try {
        Class<?> impl = Class
            .forName("tvbrowser.ui.mainframe.macosx.MacOSXMenuBar");
        Class<? extends MainFrame> mainFrameClass = this.getClass();
        Class<?> jlabelClass = Class.forName("javax.swing.JLabel");
        Constructor<?> cons = impl.getConstructor(new Class[] { mainFrameClass,
            jlabelClass });
        mMenuBar = (MenuBar) cons.newInstance(new Object[] { this,
            mStatusBar.getLabel() });
      } catch (Exception e) {
        mLog.warning("Could not instantiate MacOSXMenuBar\n" + e.toString());
        mMenuBar = new DefaultMenuBar(this, mStatusBar.getLabel());
        mLog.info("Using default menu bar");
      }

    } else {
      mMenuBar = new DefaultMenuBar(this, mStatusBar.getLabel());
    }

    // create content
    jcontentPane = (JPanel) getContentPane();
    jcontentPane.setLayout(new BorderLayout());

    JPanel skinPanel = new JPanel();
    skinPanel.setLayout(new BorderLayout());

    JPanel centerPanel = new JPanel(new BorderLayout());
    centerPanel.setOpaque(false);
    centerPanel.setBorder(BorderFactory.createEmptyBorder());

    mFilterPanel = new FilterPanel();
    mFilterPanel.setVisible(false);

    mTimeChooserPanel = new TimeChooserPanel(this);
    
    centerPanel.add(mFilterPanel, BorderLayout.NORTH);

    Channel[] channelArr = ChannelList.getSubscribedChannels();
    int startOfDay = Settings.propProgramTableStartOfDay.getInt();
    int endOfDay = Settings.propProgramTableEndOfDay.getInt();
    mProgramTableModel = new DefaultProgramTableModel(channelArr, startOfDay,
        endOfDay);
    mProgramTableScrollPane = new ProgramTableScrollPane(mProgramTableModel);
    centerPanel.add(mProgramTableScrollPane);
    
    mFinderPanel = new FinderPanel();
    
    mFinderPanel.setDateListener(this);
    dateChanged(new devplugin.Date(), null, null);

    skinPanel.add(centerPanel, BorderLayout.CENTER);

    mChannelChooser = new ChannelChooserPanel(this);
    
    /* create structure */
    mRootNode = new Node(null);

    if(Settings.propPluginViewIsLeft.getBoolean()) {
      mPluginsNode = new Node(mRootNode);
    }
    else {
      mNavigationNode = new Node(mRootNode);
    }
    
    mMainframeNode = new Node(mRootNode);
    Node programtableNode = new Node(mMainframeNode);
    
    if(Settings.propPluginViewIsLeft.getBoolean()) {
      mNavigationNode = new Node(mMainframeNode);
    }
    else {
      mPluginsNode = new Node(mMainframeNode);
    }
    
    mTimebuttonsNode = new Node(mNavigationNode);
    mDateChannelNode = new Node(mNavigationNode);
    mDateNode = new Node(mDateChannelNode);
    mChannelNode = new Node(mDateChannelNode);

    mRootNode.setProperty(Settings.propViewRoot);
    mMainframeNode.setProperty(Settings.propViewMainframe);
    mNavigationNode.setProperty(Settings.propViewNavigation);
    mDateChannelNode.setProperty(Settings.propViewDateChannel);

    /* create views */
    programtableNode.setLeaf(skinPanel);
    this.setShowPluginOverview(Settings.propShowPluginView.getBoolean());
    this.setShowTimeButtons(Settings.propShowTimeButtons.getBoolean());
    this.setShowDatelist(Settings.propShowDatelist.getBoolean());
    this.setShowChannellist(Settings.propShowChannels.getBoolean());

    updateToolbar();

    mCenterComponent = mRootNode.getComponent();
    if (mCenterComponent != null) {
      jcontentPane.add(mCenterComponent, BorderLayout.CENTER);
    }

    if (Settings.propIsStatusbarVisible.getBoolean()) {
      jcontentPane.add(mStatusBar, BorderLayout.SOUTH);
    }

    setJMenuBar(mMenuBar);
    addContextMenuMouseListener(mMenuBar);

    // set program filter
    FilterList filterList = FilterList.getInstance();

    ProgramFilter filter = filterList
        .getFilterByName(Settings.propLastUsedFilter.getString());
    
    if (filter == null) {
      filter = FilterManagerImpl.getInstance().getDefaultFilter();
    }

    setProgramFilter(filter);

    // set channel group filter
    String channelGroupName = Settings.propLastUsedChannelGroup.getString();
    if (channelGroupName != null) {
      FilterComponent component = FilterComponentList.getInstance().getFilterComponentByName(channelGroupName);
      if (component != null && component instanceof ChannelFilterComponent) {
        setChannelGroup((ChannelFilterComponent) component);
      }
    }

    addKeyboardAction();

    Timer timer = new Timer(10000, new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        handleTimerEvent();
      }
    });
    timer.start();

    setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
  }
  
  /**
   * Switch the fullscreen mode of TV-Browser
   */
  public void switchFullscreenMode() {
    dispose();
    
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        
        if(isFullScreenMode()) {
          device.setFullScreenWindow(null);
          setUndecorated(false);
          setBounds(mXPos, mYPos, mWidth, mHeight);          
          
          if(mMenuBar != null) {
            mMenuBar.setFullscreenItemChecked(false);
            mMenuBar.setVisible(true);
          }
      
          if(mToolBarPanel != null) {
            mToolBarPanel.setVisible(Settings.propIsTooolbarVisible.getBoolean());
          }

          if(mStatusBar != null) {
            mStatusBar.setVisible(Settings.propIsStatusbarVisible.getBoolean());
          }
      
          if(mChannelChooser != null) {
            mChannelChooser.setVisible(Settings.propShowChannels.getBoolean());
          }
          
          if(mFinderPanel != null) {
            mFinderPanel.setVisible(Settings.propShowDatelist.getBoolean());
          }
        
          setVisible(true);
          
          setShowPluginOverview(Settings.propShowPluginView.getBoolean(),false);      
          setShowTimeButtons(Settings.propShowTimeButtons.getBoolean(), false);
          setShowDatelist(Settings.propShowDatelist.getBoolean(), false);
          setShowChannellist(Settings.propShowChannels.getBoolean(), false);
        }
        else {
          mXPos = getX();
          mYPos = getY();
          mWidth = getWidth();
          mHeight = getHeight();
    
          setShowPluginOverview(false, false);
          setShowTimeButtons(false, false);
          setShowDatelist(false, false);
          setShowChannellist(false, false);
          
          if(mStatusBar != null) {
            mMenuBar.setFullscreenItemChecked(true);
            mStatusBar.setVisible(false);
          }
          
          if(mChannelChooser != null) {
            mChannelChooser.setVisible(false);
          }
          
          if(mMenuBar != null) {
            mMenuBar.setVisible(false);
          }
          
          if(mToolBarPanel != null) {
            mToolBarPanel.setVisible(false);
          }
          
          if(mFinderPanel != null) {
            mFinderPanel.setVisible(false);
          }
          
          setUndecorated(true);
          final Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
          
          if(device.isFullScreenSupported()) {            
            device.setFullScreenWindow(MainFrame.getInstance());
          }
          else {
            setLocation(0,0);
            setSize(screen);
          }
          
          setVisible(true);
          
          new Thread("Fullscreen border detection") {
            public void run() {
              setPriority(Thread.MIN_PRIORITY);          
              
              while(isFullScreenMode()) {
                final Point p = MouseInfo.getPointerInfo().getLocation();
                
                if(isActive()) {
                  
                  // mouse pointer is at top
                  if(p.y <= 10) {
                    if(mToolBarPanel != null && mToolBar.getToolbarLocation().compareTo(BorderLayout.NORTH) == 0) {
                      if(!mToolBarPanel.isVisible()) {
                        mToolBarPanel.setVisible(Settings.propIsTooolbarVisible.getBoolean());
                      }
                    }
                    
                      if(p.y == 0) {
                        mMenuBar.setVisible(true);
                      }
                  }
                  else if(p.y > (mMenuBar != null && mMenuBar.isVisible() ? mMenuBar.getHeight() : 0) + (Settings.propIsTooolbarVisible.getBoolean() ? mToolBarPanel.getHeight() : 0)) {
                    if(mMenuBar.isVisible()) {
                      mMenuBar.setVisible(!isFullScreenMode());
                    }
                      
                    if(mToolBarPanel != null && mToolBarPanel.isVisible() && mToolBar.getToolbarLocation().compareTo(BorderLayout.NORTH) == 0) {
                      mToolBarPanel.setVisible(!isFullScreenMode());
                    }
                  }
                
                  // mouse pointer is at the bottom
                  if(p.y >= screen.height - 1 ) {
                    if(mStatusBar != null && !mStatusBar.isVisible()) {
                      mStatusBar.setVisible(Settings.propIsStatusbarVisible.getBoolean());
                    }
                  }
                  else if(mStatusBar != null && mStatusBar.isVisible() && p.y < screen.height - mStatusBar.getHeight()) {
                    mStatusBar.setVisible(!isFullScreenMode());
                  }
                
                  // mouse pointer is on the left side
                  if(p.x <= 5) {
                    if(p.x == 0 && mToolBarPanel != null && mToolBar.getToolbarLocation().compareTo(BorderLayout.WEST) == 0) {
                      if(!mToolBarPanel.isVisible()) {
                        mToolBarPanel.setVisible(Settings.propIsTooolbarVisible.getBoolean());
                      }
                    }
                    
                    if(Settings.propPluginViewIsLeft.getBoolean()) {
                      if(Settings.propShowPluginView.getBoolean())  {
                        SwingUtilities.invokeLater(new Runnable() {
                          public void run() {
                            setShowPluginOverview(true, false);
                          }
                        });                        
                      }
                    }
                    else {
                      checkIfToShowTimeDateChannelList();
                    }
                  }
                  else {
                    int toolBarWidth = (mToolBarPanel != null && mToolBarPanel.isVisible() && mToolBar.getToolbarLocation().compareTo(BorderLayout.WEST) == 0) ? mToolBarPanel.getWidth() : 0;
                  
                    if(p.x > toolBarWidth && toolBarWidth != 0) {
                      mToolBarPanel.setVisible(!isFullScreenMode());
                    }
                    
                    if(Settings.propPluginViewIsLeft.getBoolean()) {
                      if(Settings.propShowPluginView.getBoolean() && mPluginView != null && mPluginView.isVisible() && p.x > mPluginView.getWidth() + toolBarWidth + 25) {
                        SwingUtilities.invokeLater(new Runnable() {
                          public void run() {
                            setShowPluginOverview(!isFullScreenMode(), false);
                          }
                        });
                      }
                    }
                    else if(Settings.propShowChannels.getBoolean() ||
                        Settings.propShowDatelist.getBoolean() ||
                        Settings.propShowTimeButtons.getBoolean()) {
                      SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                          if(mChannelChooser != null && mChannelChooser.isVisible() && p.x > mChannelChooser.getWidth()) {
                            setShowChannellist(!isFullScreenMode(), false);
                          }
                          
                          if(mFinderPanel != null && mFinderPanel.isVisible() && p.x > mFinderPanel.getWidth()) {
                            setShowDatelist(!isFullScreenMode(), false);
                          }
                          
                          if(mTimeChooserPanel != null && mTimeChooserPanel.isVisible() && p.x > mTimeChooserPanel.getWidth()) {
                            setShowTimeButtons(!isFullScreenMode(), false);
                          }
                        }
                      });
                    }
                  }
                  
                  // mouse pointer is on the right side
                  if(p.x >= screen.width - 1) {
                    if(!Settings.propPluginViewIsLeft.getBoolean()) {
                      if(Settings.propShowPluginView.getBoolean())  {
                        SwingUtilities.invokeLater(new Runnable() {
                          public void run() {
                            setShowPluginOverview(true, false);
                          }
                        });                        
                      }
                    }
                    else {
                      checkIfToShowTimeDateChannelList();
                    }
                  }
                  else {
                    if(!Settings.propPluginViewIsLeft.getBoolean()) {
                      if(Settings.propShowPluginView.getBoolean() && mPluginView != null && mPluginView.isVisible() && p.x < screen.width - mPluginView.getWidth()) {
                        SwingUtilities.invokeLater(new Runnable() {
                          public void run() {
                            setShowPluginOverview(!isFullScreenMode(), false);
                          }
                        });
                      }
                    }
                    else if(Settings.propShowChannels.getBoolean() ||
                        Settings.propShowDatelist.getBoolean() ||
                        Settings.propShowTimeButtons.getBoolean()) {
                      SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                          if(mChannelChooser != null && mChannelChooser.isVisible() && p.x < screen.width - mChannelChooser.getWidth()) {
                            setShowChannellist(!isFullScreenMode(), false);
                          }
                          
                          if(mFinderPanel != null && mFinderPanel.isVisible() && p.x < screen.width - mFinderPanel.getWidth()) {
                            setShowDatelist(!isFullScreenMode(), false);
                          }
                          
                          if(mTimeChooserPanel != null && mTimeChooserPanel.isVisible() && p.x < screen.width - mTimeChooserPanel.getWidth()) {
                            setShowTimeButtons(!isFullScreenMode(), false);
                          }
                        }
                      });
                    }
                  }
                }
                try {
                  Thread.sleep(200);
                }catch(Exception e) {}
              }
            }
          }.start();
        }      
      }
    });
  }
  
  private void checkIfToShowTimeDateChannelList() {
    if(Settings.propShowTimeButtons.getBoolean() ||
        Settings.propShowDatelist.getBoolean() ||
        Settings.propShowChannels.getBoolean()) {
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          if(Settings.propShowTimeButtons.getBoolean() && !mTimeChooserPanel.isVisible()) {
            setShowTimeButtons(true, false);
          }
         
          if(Settings.propShowDatelist.getBoolean() && !mFinderPanel.isVisible()) {
            setShowDatelist(true, false);
          }
         
          if(Settings.propShowChannels.getBoolean() && !mChannelChooser.isVisible()) {
            setShowChannellist(true, false);
          }
        }
      });
    }
  }
  
  /**
   * Adds the keyboard actions for going to the program table with the keyboard.
   * 
   */
  public void addKeyboardAction() {
    JRootPane rootPane = this.getRootPane();

    mProgramTableScrollPane.deSelectItem();
    
    KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0, false);
    rootPane.registerKeyboardAction(new ActionListener() {
    	public void actionPerformed(ActionEvent e) {
        switchFullscreenMode();        
    	}
    }, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
    
    stroke = KeyStroke.getKeyStroke(KeyEvent.VK_UP,
        InputEvent.CTRL_MASK);
    rootPane.registerKeyboardAction(new KeyboardAction(mProgramTableScrollPane,
        KeyboardAction.KEY_UP), stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
    
    stroke = KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.CTRL_MASK);
    rootPane.registerKeyboardAction(new KeyboardAction(mProgramTableScrollPane,
        KeyboardAction.KEY_RIGHT), stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);

    stroke = KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.CTRL_MASK);
    rootPane.registerKeyboardAction(new KeyboardAction(mProgramTableScrollPane,
        KeyboardAction.KEY_DOWN), stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);

    stroke = KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.CTRL_MASK);
    rootPane.registerKeyboardAction(new KeyboardAction(mProgramTableScrollPane,
        KeyboardAction.KEY_LEFT), stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);

    stroke = KeyStroke.getKeyStroke(KeyEvent.VK_CONTEXT_MENU, 0, true);
    rootPane.registerKeyboardAction(new KeyboardAction(mProgramTableScrollPane,
        KeyboardAction.KEY_CONTEXTMENU), stroke,
        JComponent.WHEN_IN_FOCUSED_WINDOW);

    stroke = KeyStroke.getKeyStroke(KeyEvent.VK_R, 0, true);
    rootPane.registerKeyboardAction(new KeyboardAction(mProgramTableScrollPane,
        KeyboardAction.KEY_CONTEXTMENU), stroke,
        JComponent.WHEN_IN_FOCUSED_WINDOW);

    stroke = KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_MASK);
    rootPane
        .registerKeyboardAction(new KeyboardAction(mProgramTableScrollPane,
            KeyboardAction.KEY_DESELECT), stroke,
            JComponent.WHEN_IN_FOCUSED_WINDOW);

    stroke = KeyStroke.getKeyStroke(KeyEvent.VK_L, 0, true);
    rootPane.registerKeyboardAction(new KeyboardAction(mProgramTableScrollPane,
        KeyboardAction.KEY_SINGLECLICK), stroke,
        JComponent.WHEN_IN_FOCUSED_WINDOW);
    
    stroke = KeyStroke.getKeyStroke(KeyEvent.VK_D, 0, true);
    rootPane.registerKeyboardAction(new KeyboardAction(mProgramTableScrollPane,
        KeyboardAction.KEY_DOUBLECLICK), stroke,
        JComponent.WHEN_IN_FOCUSED_WINDOW);

    stroke = KeyStroke.getKeyStroke(KeyEvent.VK_M, 0, true);
    rootPane.registerKeyboardAction(new KeyboardAction(mProgramTableScrollPane,
        KeyboardAction.KEY_MIDDLECLICK), stroke,
        JComponent.WHEN_IN_FOCUSED_WINDOW);

    stroke = KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_MASK);
    rootPane.registerKeyboardAction(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        goToNextDay();
      }

    }, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);

    stroke = KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_MASK);
    rootPane.registerKeyboardAction(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        goToPreviousDay();
      }

    }, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);

    // return from fullscreen using ESCAPE
    stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,0);
    rootPane.registerKeyboardAction(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        if (isFullScreenMode()) {
          switchFullscreenMode();
        }
      }

    }, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);

    stroke = KeyStroke.getKeyStroke(KeyEvent.VK_BEGIN, 0);
    rootPane.registerKeyboardAction(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        goToLeftSide();
      }

    }, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);

    stroke = KeyStroke.getKeyStroke(KeyEvent.VK_END, 0);
    rootPane.registerKeyboardAction(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        goToRightSide();
      }

    }, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);

    // map font size keys also to numeric pad
    stroke = KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT, InputEvent.CTRL_MASK);
    rootPane.registerKeyboardAction(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        changeFontSize(-1);
      }

    }, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);

    stroke = KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD0, InputEvent.CTRL_MASK);
    rootPane.registerKeyboardAction(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        changeFontSize(0);
      }

    }, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);

    stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ADD, InputEvent.CTRL_MASK);
    rootPane.registerKeyboardAction(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        changeFontSize(1);
      }

    }, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
    
    stroke = KeyStroke.getKeyStroke(KeyEvent.VK_A, 0, true);
    rootPane.registerKeyboardAction(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        //Schnellnavigation starten
      }
    }, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);


    this.setRootPane(rootPane);
  }

  protected void goToRightSide() {
    Channel[] channels = MainFrame.getInstance().getProgramTableModel().getShownChannels();
    if (channels != null && channels.length > 0) {
      mProgramTableScrollPane.scrollToChannel(channels[channels.length-1]);
    }
  }

  protected void goToLeftSide() {
    Channel[] channels = MainFrame.getInstance().getProgramTableModel().getShownChannels();
    if (channels != null && channels.length > 0) {
      mProgramTableScrollPane.scrollToChannel(channels[0]);
    }
  }

  public JLabel getStatusBarLabel() {
    return mStatusBar.getLabel();
  }

  public void updateToolbar() {
    JPanel contentPane = (JPanel) getContentPane();

    if (mToolBarPanel != null) {
      contentPane.remove(mToolBarPanel);
    }

    mToolBarModel = DefaultToolBarModel.getInstance();
    mToolBar = new ToolBar(mToolBarModel);
    mToolBar.setOpaque(false);
    
    String location = mToolBar.getToolbarLocation();

    if (Settings.propIsTooolbarVisible.getBoolean()) {
      if (mToolBarPanel == null) {
        mToolBarPanel = new JPanel(new BorderLayout()) {
          public void updateUI() {
            super.updateUI();
            setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, getBackground().darker()));
          }
        };
        addContextMenuMouseListener(mToolBarPanel);
        mSearchField = new SearchField();
      } else {
        mToolBarPanel.removeAll();
      }
      
      if (location.compareTo(BorderLayout.NORTH) == 0) {
        mToolBarPanel.add(MoreButton.wrapToolBar(mToolBar,this), BorderLayout.CENTER);
        if(Settings.propIsSearchFieldVisible.getBoolean()) {
          mToolBarPanel.add(mSearchField, BorderLayout.EAST);
        }
      } else {
        mToolBarPanel.add(MoreButton.wrapToolBar(mToolBar,this), BorderLayout.WEST);
        if(Settings.propIsSearchFieldVisible.getBoolean()) {
          mToolBarPanel.add(mSearchField, BorderLayout.SOUTH);
        }
      }

      contentPane.add(mToolBarPanel, location);
    }
    
    contentPane.invalidate();
    contentPane.updateUI();
  }

  private void addContextMenuMouseListener(final JComponent c) {
    c.addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        if (e.isPopupTrigger()) {
          ContextMenu menu = new ContextMenu(c);
          menu.show(e.getX(), e.getY());
        }
      }

      public void mouseReleased(MouseEvent e) {
        if (e.isPopupTrigger()) {
          ContextMenu menu = new ContextMenu(c);
          menu.show(e.getX(), e.getY());
        }
      }
    });
  }

  public ProgramTableScrollPane getProgramTableScrollPane() {
    return mProgramTableScrollPane;
  }

  public ToolBar getToolbar() {
    return mToolBar;
  }

  public JPanel getToolBarPanel() {
    return mToolBarPanel;
  }

  public DefaultProgramTableModel getProgramTableModel() {
    return mProgramTableModel;
  }

  public static MainFrame getInstance() {
    if (mSingleton == null) {
      mSingleton = new MainFrame();
    }
    return mSingleton;
  }

  public void updateTimeButtons() {
    mToolBar.updateTimeButtons();
    mTimeChooserPanel.updateButtons();
    mMenuBar.updateTimeItems();
  }
  
  public boolean isShowAllFilterActivated() {
    return (mProgramTableModel == null) || (mProgramTableModel.getProgramFilter() instanceof ShowAllFilter);
  }
  
  /**
   * check if the default filter is active
   * @return true, if the default filter is active
   * @since 2.6
   */
  public boolean isDefaultFilterActivated() {
    if (mProgramTableModel == null) {
      return true;
    }
    ProgramFilter filter = mProgramTableModel.getProgramFilter(); 
    return (Settings.propDefaultFilter.getString().equals(filter.getClass().getName() + "###" + filter.getName())); 
  }

  public void setProgramFilter(ProgramFilter filter) {
    boolean isDefaultFilter = filter.equals(FilterManagerImpl.getInstance().getDefaultFilter());
    
    if (!isDefaultFilter) { // Store Position
      mStoredViewPosition = mProgramTableScrollPane.getViewport().getViewPosition();
    }
    
    if (mProgramTableModel.getProgramFilter() instanceof SearchFilter && !(filter instanceof SearchFilter)) {
      mSearchField.deactivateSearch();
    }
    
    mProgramTableScrollPane.deSelectItem();
    mProgramTableModel.setProgramFilter(filter);
    mMenuBar.updateFiltersMenu();
    
    mToolBarModel.setFilterButtonSelected(!isDefaultFilter);

    updateFilterPanel();

    mToolBar.update();
    addKeyboardAction();
    
    if(mPluginView != null) {
      mPluginView.repaint();
    }

    if ((mStoredViewPosition != null) && (isDefaultFilter)) {
      // Recreate last Position
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          if (mStoredViewPosition != null) {
            mProgramTableScrollPane.getViewport().setViewPosition(mStoredViewPosition);
          }
        }
      });
    }
    else { // on switching filters go to now, but only if we are at current date
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          if (getCurrentSelectedDate().equals(Date.getCurrentDate())) {
            scrollToNow();
          }
        }});
    }
    
    mCurrentFilterName = filter.getName();
    mProgramTableScrollPane.requestFocusInWindow();
  }
  
  /**
   * Set the active channel group
   * @param channelFilter
   * @since 2.6
   */
  public void setChannelGroup(ChannelFilterComponent channelFilter) {
    mProgramTableModel.setChannelGroup(channelFilter);
    if (channelFilter != null) {
      Settings.propLastUsedChannelGroup.setString(channelFilter.getName());
    }
    else {
      Settings.propLastUsedChannelGroup.setString(null);
    }
    mChannelChooser.setChannelGroup(channelFilter);
    mMenuBar.updateChannelGroupMenu();
  }

  public void updateFilterPanel() {
    ProgramFilter filter = mProgramTableModel.getProgramFilter();
    boolean filterVisible = !filter.equals(FilterManagerImpl.getInstance().getDefaultFilter()) && mMenuBar.isShowFilterPanelEnabled();
    if (filterVisible) {
      mFilterPanel.setCurrentFilter(filter);
    }
    mFilterPanel.setVisible(filterVisible);
    Settings.propShowFilterBar.setBoolean(mMenuBar.isShowFilterPanelEnabled());
  }

  public ProgramFilter getProgramFilter() {
    if (mProgramTableModel == null) {
      return null;
    }
    return mProgramTableModel.getProgramFilter();
  }
  
  public ChannelFilterComponent getChannelGroup() {
    if (mProgramTableModel == null) {
      return null;
    }
    return mProgramTableModel.getChannelGroup();
  }

  public void quit() {
    TVBrowser.removeTray();
    quit(true);
  }

  public void quit(boolean log) {
    if(log && this.isUndecorated()) {
      switchFullscreenMode();
    }
    if (mShuttingDown) {
      return;
    }
    mShuttingDown = true;

    if(log) {
      FavoritesPlugin.getInstance().handleTvBrowserIsShuttingDown();
    }
    
    if (log) {
      mLog.info("Finishing plugins");
    }
    PluginProxyManager.getInstance().shutdownAllPlugins(log);

    if (log) {
      mLog.info("Storing dataservice settings");
    }
    TvDataServiceProxyManager.getInstance().shutDown();
    
    FavoritesPlugin.getInstance().store();
    ReminderPlugin.getInstance().store();

    TVBrowser.shutdown(log);

    if (log) {
      mLog.info("Closing TV data base");
    }

    try {
      TvDataBase.getInstance().close();
    } catch (Exception exc) {
      if (log) {
        mLog.log(Level.WARNING, "Closing database failed", exc);
      }
    }

    if (log) {
      mLog.info("Quitting");
      System.exit(0);
    }
  }
  
  /**
   * Gets if TV-Browser is currently being shutting down.
   * @return True if TV-Browser is shutting down.
   * @since 2.5.3
   */
  public static boolean isShuttingDown() {
    return mShuttingDown;
  }
  
  /**
   * Gets whether TV-Browser is currently being started. 
   * @return True if TV-Browser is currently being started.
   * @since 2.5.3
   */
  public static boolean isStarting() {
    return mStarting;
  }
  
  /**
   * Handles done TV-Browser start.
   */
  public void handleTvBrowserStartFinished() {
    mStarting = false;
  }
  
  private void runAutoUpdate() {
    ArrayList<TvDataServiceProxy> dataServices = new ArrayList<TvDataServiceProxy>();
    ArrayList<TvDataServiceProxy> checkedServices = new ArrayList<TvDataServiceProxy>(0);
    
    Channel[] channels = Settings.propSubscribedChannels.getChannelArray();
    
    for(Channel channel : channels) {
      if(!checkedServices.contains(channel.getDataServiceProxy())) {
        checkedServices.add(channel.getDataServiceProxy());
        
        if(channel.getDataServiceProxy().supportsAutoUpdate()) {
          dataServices.add(channel.getDataServiceProxy());
        }
      }
    }
    
    checkedServices.clear();
    
    if(!dataServices.isEmpty() && licenseForTvDataServicesWasAccepted(dataServices.toArray(new TvDataServiceProxy[dataServices.size()]))) {
      runUpdateThread(14, dataServices.toArray(new TvDataServiceProxy[dataServices.size()]));
    }
  }
  
  /**
   * Resets the arrays of on air programs for relaoding all.
   */
  public static void resetOnAirArrays() {
    mChannelDateArr = null;
    mOnAirRowProgramsArr = null;
  }

  private void handleTimerEvent() {
    Date date = Date.getCurrentDate();
    
    if(mLastTimerMinutesAfterMidnight == -1) {
      resetOnAirArrays();
      mAutoDownloadTimer = (int)(Math.random() * 1430);
    }
      
    // Avoid a repaint 6 times a minute (Once a minute is enough)
    try {
      int minutesAfterMidnight = IOUtilities.getMinutesAfterMidnight();
      boolean onAirChanged = false;
      if (minutesAfterMidnight != mLastTimerMinutesAfterMidnight) {
        mLastTimerMinutesAfterMidnight = minutesAfterMidnight;
        Channel[] ch = ChannelList.getSubscribedChannels();
      
        if(ch != null) {
          /* If no date array is available we have to find
           * the on air programs */
          if(mChannelDateArr == null) {
            onAirChanged = true;
            fillOnAirArrays(ch);
          }
          else {
            /* We have a date array and can test the programs */
            for(int i = 0; i < mChannelDateArr.length; i++) {
              if(mChannelDateArr[i] != null) {
                ChannelDayProgram chProg = TvDataBase.getInstance().getDayProgram(mChannelDateArr[i],ch[i]);
            
                if(chProg != null && chProg.getProgramCount() > 0 && mOnAirRowProgramsArr[i] != -1) {
                  if (mOnAirRowProgramsArr[i] >= chProg.getProgramCount()) {
                    fillOnAirArrays(ch);
                    mLog.warning("Reset of on-air-arrays");
                  }
                  Program p = chProg.getProgramAt(mOnAirRowProgramsArr[i]);
              
                  if(p.isOnAir()) {
                    p.validateMarking();
                  } else if(p.isExpired()) {
                    onAirChanged = true;
                    p.validateMarking();
              
                    int n = mOnAirRowProgramsArr[i]+1;
              
                    if(n < chProg.getProgramCount()) {
                      mOnAirRowProgramsArr[i] = n;
                      chProg.getProgramAt(mOnAirRowProgramsArr[i]).validateMarking();
                    }
                    else {
                      /* The last day program is expired so we have to
                       * look for the on air program on the next day */
                      mChannelDateArr[i] = mChannelDateArr[i].addDays(1);
                
                      chProg = TvDataBase.getInstance().getDayProgram(mChannelDateArr[i],ch[i]);
                
                      // The next day has no data
                      if(chProg == null || chProg.getProgramCount() < 1) {
                        mOnAirRowProgramsArr[i] = -1;
                      } else {
                        mOnAirRowProgramsArr[i] = 0;
                        chProg.getProgramAt(mOnAirRowProgramsArr[i]).validateMarking();
                      }
                    }
                  }
                }
                else if(mChannelDateArr[i].compareTo(Date.getCurrentDate()) < 0) {
                  /* If the date array for the channel contains a date
                   * earlier than today we have to use today instead */
                  mChannelDateArr[i] = Date.getCurrentDate();
                  onAirChanged = true;
              
                  chProg = TvDataBase.getInstance().getDayProgram(mChannelDateArr[i],ch[i]);
              
                  if(chProg != null && chProg.getProgramCount() > 0) {
                    mOnAirRowProgramsArr[i] = 0;
                    chProg.getProgramAt(mOnAirRowProgramsArr[i]).validateMarking();
                  }
                }
              }
            }
          }
        }
      }
      // update filtered view if the "on air" condition changed for any program
      if (onAirChanged && !getProgramFilter().equals(FilterManagerImpl.getInstance().getDefaultFilter())) {
        setProgramFilter(getProgramFilter());
      }
    }catch(Exception e) {}
    
    if (mPluginView != null) {
      mPluginView.repaint();
    }
    
    if ((mLastAutoUpdateRun + Settings.propDataServiceAutoUpdateTime.getInt() * 60000) <= System.currentTimeMillis() && !TvDataUpdater.getInstance().isDownloading()) {
      runAutoUpdate();
    }
    
    if(Settings.propAutoDataDownloadEnabled.getBoolean() && (mAutoDownloadTimer < IOUtilities.getMinutesAfterMidnight() || !date.equals(mCurrentDay)) && mAutoDownloadTimer != -1 && (downloadingThread == null || !downloadingThread.isAlive())) {
      TVBrowser.handleAutomaticDownload();
      mAutoDownloadTimer = -1;
    }
    
    if (date.equals(mCurrentDay)) {
      return;
    }
    
    if(mCurrentDay != null) {
      if(mProgramTableModel.getDate().compareTo(Date.getCurrentDate().addDays(-1)) < 0) {
        scrollToNow();
      }
      
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          mLog.info("Deleting expired TV listings...");
          TvDataBase.getInstance().deleteExpiredFiles(1, true);
        }
      });
    }
    
    mLastTimerMinutesAfterMidnight = -1;
    mCurrentDay = date;
    
    if (mFinderPanel != null) {
      mFinderPanel.updateContent();
    }
    if (mPluginView != null) {
      mPluginView.update();
    }
  }

  private void fillOnAirArrays(Channel[] ch) {
    mChannelDateArr = new Date[ch.length];
    mOnAirRowProgramsArr = new int[ch.length];
    
    Arrays.fill(mOnAirRowProgramsArr, -1);
    
    Date currentDate = Date.getCurrentDate();
    for(int i = 0; i < ch.length; i++) {
      ChannelDayProgram chProg = TvDataBase.getInstance().getDayProgram(currentDate,ch[i]);
     
      if(chProg == null) {
        mChannelDateArr[i] = null;
      } else {
        int n = chProg.getProgramCount();
    
        for(int j = 0; j < n; j++) {
          Program p = chProg.getProgramAt(j);
          if(p.isOnAir() || !p.isExpired()) {
            p.validateMarking();
            mOnAirRowProgramsArr[i] = j;
            mChannelDateArr[i] = currentDate;
            break;
          }
        }
    
        if(mOnAirRowProgramsArr[i] == -1) {
          chProg = TvDataBase.getInstance().getDayProgram(currentDate.addDays(1),ch[i]);
      
          if(chProg != null && chProg.getProgramCount() > 0 && chProg.getProgramAt(0).isOnAir()) {
            chProg.getProgramAt(0).validateMarking();
            mOnAirRowProgramsArr[i] = 0;
          }
        
          mChannelDateArr[i] = currentDate.addDays(1);
        }
      }
    }
  }

  public void updatePluginsMenu() {
    mMenuBar.updatePluginsMenu();
  }

  public void scrollToProgram(final Program program) {
    scrollToProgram(program, null);
  }
  
  public void scrollToProgram(final Program program, final Runnable callback) {
    if (!getProgramFilter().accept(program)) {
      int result = JOptionPane.showOptionDialog(this, mLocalizer.msg("programFiltered", "The program {0} is not visible with the filter {1} being active.\nDo you want to deactivate the filter?", program.getTitle(), getProgramFilter().getName()),mLocalizer.msg("programNotVisible","Program not visible"),
          JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
      if (result == JOptionPane.YES_OPTION) {
        mStoredViewPosition = null;
        setProgramFilter(FilterManagerImpl.getInstance().getAllFilter());
      }
    }
    // invoke scrolling later as the upper filter deactivation may have pending operations for the UI
    // so we currently can't scroll there yet
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        mProgramTableScrollPane.scrollToChannel(program.getChannel());
        scrollTo(program.getDate(), program.getHours(), callback);
      }});
  }

  public void scrollToTime(int time) {
    mProgramTableScrollPane.deSelectItem();
    mProgramTableScrollPane.scrollToTime(time);
  }

  public void scrollToNow() {
    Calendar cal = Calendar.getInstance();
    int hour = cal.get(Calendar.HOUR_OF_DAY);
    devplugin.Date day = new devplugin.Date();
    scrollTo(day, hour);
  }
  
  private void scrollTo(Date day, int hour) {
    scrollTo(day, hour, null);
  }

  private void scrollTo(Date day, int hour, final Runnable callback) {
    mProgramTableScrollPane.deSelectItem();
    // Choose the day.
    // NOTE: If its early in the morning before the setted "day start" we should
    // stay at the last day - otherwise the user won't see the current
    // program. But until when should we stay at the old day?
    // Example: day start: 0:00, day end: 6:00
    // Directly after the day start is not a good choice, because the new
    // day program table will not contain programs that started before 0:00.
    // Directly after the day end is also not a good choice, because a
    // minute before the old day program will not contain the coming programs.
    // So I think the best choice will be the middle, in this case 3:00.
    // If the day start is later as the day end, then the table have to show the
    // old date until the day end is reached.

    int dayStart = Settings.propProgramTableStartOfDay.getInt();
    int dayEnd = Settings.propProgramTableEndOfDay.getInt();
    if ((dayStart >= dayEnd && hour < dayEnd / 60) // no overlapping -> stay on last day until day end
        || (dayStart < dayEnd && hour <= (dayEnd + dayStart) /60 /2)) { // overlapping -> stay until the middle between both times
      day = day.addDays(-1);
      hour += 24;
    }
    // Change to the shown day program to today if nessesary
    // and scroll to "now" afterwards
    final int fHour = hour;
    mFinderPanel.markDate(day, new Runnable() {
      public void run() {
        // Scroll to now
        mProgramTableScrollPane.scrollToTime(fHour * 60);
        if (callback != null) {
          callback.run();
        }
      }
    });
  }

  public void runSetupAssistant() {

    ProgressWindow progWin = new ProgressWindow(this, mLocalizer.msg(
        "loadingAssistant", ""));
    final JFrame parent = this;
    progWin.run(new Progress() {
      public void run() {
        mConfigAssistantDialog = new tvbrowser.ui.configassistant.ConfigAssistant(
            parent);
      }
    });

    util.ui.UiUtilities.centerAndShow(mConfigAssistantDialog);
    mConfigAssistantDialog.setVisible(false);
    mConfigAssistantDialog.dispose();
    mConfigAssistantDialog = null;

    boolean dataAvailable = TvDataBase.getInstance().dataAvailable(new Date());
    if (!dataAvailable) {
      askForDataUpdateNoDataAvailable();
    }

    Settings.handleChangedSettings();

  }

  // public void runTvBrowserUpdateAssistant() {
  // TvBrowserUpdateAssistant dlg = new TvBrowserUpdateAssistant(this);
  // UiUtilities.centerAndShow(dlg);
  // }

  public void storeSettings() {
    mToolBarModel.store();
    mToolBar.storeSettings();
    mRootNode.storeProperties();
    
    ProgramFilter filter = getProgramFilter();
    if (filter != null) {
      if (!(filter instanceof SearchFilter)) {
        Settings.propLastUsedFilter.setString(mCurrentFilterName);
      } else {
        Settings.propLastUsedFilter.setString(FilterManagerImpl.getInstance().getDefaultFilter().getName());
      }
    } else {
      Settings.propLastUsedFilter.setString(FilterManagerImpl.getInstance().getDefaultFilter().getName());
    }
    
    ChannelFilterComponent channelGroup = getChannelGroup();
    if (channelGroup != null) {
      Settings.propLastUsedChannelGroup.setString(channelGroup.getName());
    }
    else {
      Settings.propLastUsedChannelGroup.setString(null);
    }
  }
  
  protected void showPluginInfoDlg() {
    Window w = UiUtilities.getLastModalChildOf(this);
    
    PluginInformationDialog dlg;
    
    if(w instanceof JFrame) {
      dlg = new PluginInformationDialog((JFrame)w);
    }
    else {
      dlg = new PluginInformationDialog((JDialog)w);
    }
    
    Settings.layoutWindow("main.pluginInfoDlg",dlg, new Dimension(Sizes.dialogUnitXAsPixel(420,dlg),Sizes.dialogUnitYAsPixel(215,dlg)));
    
    dlg.setVisible(true);
  }

  private void onDownloadStart() {
    mAutoDownloadTimer = -1;
    
    if(!Settings.propPluginInfoDialogWasShown.getBoolean()) {
      Date compareDate = Settings.propFirstStartDate.getDate().addDays((int)(Math.random() * 4 + 3));
      
      if(compareDate.compareTo(Date.getCurrentDate()) <= 0) {
        showPluginInfoDlg();
        Settings.propPluginInfoDialogWasShown.setBoolean(true);
      }
    }
    
    mLastAutoUpdateRun = System.currentTimeMillis();
    mToolBar.updateUpdateButton(true);
    mMenuBar.showStopMenuItem();
    Settings.propLastDownloadDate.setDate(Date.getCurrentDate());
  }

  private void onDownloadDone() {
    TvDataUpdater.getInstance().stopDownload();
    mStatusBar.getProgressBar().setValue(0);

    mToolBar.updateUpdateButton(false);
    mMenuBar.showUpdateMenuItem();

    mFinderPanel.updateItems();
    resetOnAirArrays();
    mAutoDownloadTimer = -1;

    DontShowAgainMessageBox
        .showMessageDialog(
            "downloadDone",
            MainFrame.getInstance(),
            mLocalizer
                .msg(
                    "downloaddone.message",
                    "The download is done."),
            mLocalizer.msg("downloaddone.title", "Done"));

  }
  
  /**
   * Updates the entries of the finder panal.
   * @since 2.2.2/2.5.1
   */
  public void handleChangedTvDataDir() {
    mFinderPanel.updateItems();
    changeDate(Date.getCurrentDate(), null, new Runnable() {
      public void run() {
        scrollToNow();
        resetOnAirArrays();
      }
    });
  }

  public void showChannel(Channel ch) {
    mProgramTableScrollPane.scrollToChannel(ch);
  }

  /**
   * Updates the program table and the finder panel.
   * <p>
   * Called when new TV listings was downloaded or when TV data was imported.
   */
  private void newTvDataAvailable(boolean scroll) {
    if(scroll) {
      changeDate(mFinderPanel.getSelectedDate(), null, new Runnable() {
        public void run() {
          scrollToNow();
        }
      });
    } else {
      changeDate(mFinderPanel.getSelectedDate(), null, null);
    }
    
    mMenuBar.updateDateItems();
  }

  public void goTo(Date date) {
    mProgramTableScrollPane.deSelectItem();
    mFinderPanel.markDate(date);
  }

  public void goToNextDay() {
    mProgramTableScrollPane.deSelectItem();
    mFinderPanel.markNextDate();
  }

  public void goToPreviousDay() {
    mProgramTableScrollPane.deSelectItem();
    mFinderPanel.markPreviousDate();
  }

  /**
   * show same week day 7 days later
   * @since 2.7
   */
  public void goToNextWeek() {
    mProgramTableScrollPane.deSelectItem();
    mFinderPanel.markNextWeek();
  }
  
  /**
   * show same week day 7 days earlier
   * @since 2.7
   */
  public void goToPreviousWeek() {
    mProgramTableScrollPane.deSelectItem();
    mFinderPanel.markPreviousWeek();
  }
  
  /**
   * @since 2.7
   */
  public void goToToday() {
    goTo(devplugin.Date.getCurrentDate());
  }

  public Date getCurrentSelectedDate() {
    return mFinderPanel.getSelectedDate();
  }

  private void changeDate(final Date date, final ProgressMonitor monitor,
      final Runnable callback) {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        mProgramTableScrollPane.deSelectItem();
        mProgramTableModel.setDate(date, monitor, callback);
      }
    });
  }

  /**
   * Implementation of Interface DateListener
   */
  public void dateChanged(final devplugin.Date date,
      devplugin.ProgressMonitor monitor, Runnable callback) {
    changeDate(date, monitor, callback);
    super.setTitle(TVBrowser.MAINWINDOW_TITLE + " - "
        + date.getLongDateString());
    if (mMenuBar != null) {
        mMenuBar.dateChanged(date, monitor, callback);
    }
    if (mToolBar != null) {
    	mToolBar.dateChanged(date, monitor, callback);
    }
  }

  public void runUpdateThread(final int daysToDownload,
      final TvDataServiceProxy[] services) {
    downloadingThread = new Thread("TV data update") {
      public void run() {
        onDownloadStart();
        
        final boolean scroll = !TvDataBase.getInstance().dataAvailable(Date.getCurrentDate())
        && getProgramTableModel().getDate() != null 
        && getProgramTableModel().getDate().compareTo(Date.getCurrentDate()) == 0;
        
        JProgressBar progressBar = mStatusBar.getProgressBar();
        try {
          TvDataUpdater.getInstance().downloadTvData(daysToDownload, services,
              progressBar, mStatusBar.getLabel());
        } catch (Throwable t) {
          String msg = mLocalizer.msg("error.3", "An unexpected error occurred during update.");
          ErrorHandler.handle(msg, t);
        } finally {
          SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              onDownloadDone();
              newTvDataAvailable(scroll);
              
              if(Settings.propAutoUpdatePlugins.getBoolean() && (Settings.propLastPluginsUpdate.getDate() == null || Settings.propLastPluginsUpdate.getDate().addDays(7).compareTo(Date.getCurrentDate()) <= 0)) {
                PluginAutoUpdater.searchForPluginUpdates(mStatusBar.getLabel());
              }
            }          
          });
        }
        
      }
    };
    downloadingThread.setPriority(Thread.MIN_PRIORITY);
    downloadingThread.start();
  }

  public void updateChannellist() {
    updateChannelChooser();
    mMenuBar.updateChannelItems();
  }

  public void updateChannelChooser() {
    mChannelChooser.updateChannelChooser();
  }

  /**
   * Starts the TV listings update.
   */
  public void updateTvData() {
    if (ChannelList.getNumberOfSubscribedChannels() == 0) {
      int result = JOptionPane.showOptionDialog(this, 
          mLocalizer.msg("subscribeBeforeUpdate.msg", "You have not defined any channels.\n\nDo you want to subscribe to some channels before starting the data update?"), 
          mLocalizer.msg("subscribeBeforeUpdate.title", "No subscribed channels"),
          JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null,
          null);
      if (result == JOptionPane.YES_OPTION) {
        showSettingsDialog(SettingsItem.CHANNELS);
      }
    }
    else {
      if (TvDataUpdater.getInstance().isDownloading()) {
        TvDataUpdater.getInstance().stopDownload();
      } else {
        UpdateDlg dlg = new UpdateDlg(this, true);
        dlg.pack();
        UiUtilities.centerAndShow(dlg);
          
        int daysToDownload = dlg.getResult();
        if(daysToDownload != UpdateDlg.CANCEL && licenseForTvDataServicesWasAccepted(dlg.getSelectedTvDataServices())) {
          runUpdateThread(daysToDownload, dlg.getSelectedTvDataServices());
        }        
      }
    }
  }
  
  /**
   * Checks if all users services license were accepted. 
   * 
   * @param updateServices The service to check for license.
   * 
   * @return If all used service licenses were accepted.
   */
  public boolean licenseForTvDataServicesWasAccepted(TvDataServiceProxy[] updateServices) {    
    boolean accept = true;    
    String[] acceptedFor = Settings.propAcceptedLicenseArrForServiceIds.getStringArray();
    
    for (TvDataServiceProxy serviceProxy : updateServices) {
      boolean found = false;
      
      for (String acceptedService : acceptedFor) {
        if(serviceProxy.getId().compareTo(acceptedService) == 0) {
          found = true;
          break;
        }
      }
      
      if(!found && serviceProxy.getInfo().getLicense() != null) {
        LicenseBox box=new LicenseBox(this, serviceProxy.getInfo().getLicense(), true);
        util.ui.UiUtilities.centerAndShow(box);
        accept = accept && box.agreed();
        
        if(box.agreed()) {
          String[] oldIds = Settings.propAcceptedLicenseArrForServiceIds.getStringArray();
          String[] newIds = new String[oldIds.length + 1];
          
          System.arraycopy(acceptedFor,0,newIds,0,oldIds.length);
          newIds[newIds.length-1] = serviceProxy.getId();
          
          Settings.propAcceptedLicenseArrForServiceIds.setStringArray(newIds);
        }
      }
    }
    
    return accept;
  }

  /**
   * Shows the settings dialog.
   */
  public void showSettingsDialog() {
    showSettingsDialog(Settings.propLastUsedSettingsPath.getString());
  }

  /**
   * Show Settings Dialog for a specific TabId
   * 
   * @param visibleTabId
   *          Id of the specific Tab
   */
  public void showSettingsDialog(final String visibleTabId) {
    if(mSettingsWillBeOpened) {
      return;
    }
    
    new Thread(new Runnable() {
      public void run() {
        mSettingsWillBeOpened = true;
        
        // show busy cursor
        Window comp = UiUtilities.getLastModalChildOf(MainFrame.getInstance());
        ProgramTable programTable = MainFrame.getInstance().getProgramTableScrollPane().getProgramTable();
        Cursor oldWindowCursor = comp.getCursor();
        Cursor oldTableCursor = programTable.getCursor();
        comp.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        programTable.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        SettingsDialog dlg = new SettingsDialog(MainFrame.this, visibleTabId);
        dlg.centerAndShow();

        // restore cursors
        programTable.setCursor(oldTableCursor);
        comp.setCursor(oldWindowCursor);
        
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            Settings.handleChangedSettings();
            if (mPluginView != null) {
              mPluginView.refreshTree();
            }
          }
        });
        mSettingsWillBeOpened = false;
      }
    }, "Show settings dialog").start();
  }

  /*****************************************************************************
   * Show the Settings for a specific Plugin
   * 
   * @param plugin
   *          Plugin to show
   */
  public void showSettingsDialog(Plugin plugin) {
    showSettingsDialog(plugin.getId());
  }

  /**
   * Shows the about box
   */
  public void showAboutBox() {
    AboutBox box = new AboutBox(this);
    box.setSize(500, 580);
    UiUtilities.centerAndShow(box);
    box.dispose();
  }

  public void showUpdatePluginsDlg(boolean noQuestion) {
    int answer = JOptionPane.YES_OPTION;
  
    if(!noQuestion) {
      Object[] options = { mLocalizer.msg("checknow", "Check now"),
          Localizer.getLocalization(Localizer.I18N_CANCEL) };
      String msg = mLocalizer.msg("question.1",
          "do you want to check for new plugins");
      answer = JOptionPane.showOptionDialog(this, msg, mLocalizer.msg(
          "title.1", "update plugins"), JOptionPane.YES_NO_OPTION,
          JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
    }

    if (answer == JOptionPane.YES_OPTION) {
      updatePlugins(PluginAutoUpdater.DEFAULT_PLUGINS_DOWNLOAD_URL, false, mStatusBar.getLabel());
    }
  }
  
  /**
   * Search for updates of plugins.
   * 
   * @param baseUrl The url string to load the plugin updates from.
   * @param showOnlyUpdates If the dialog is only to show when updates of
   *                        installed plugins are found.
   * @param infoLabel The label to use to show infos.
   */
  public void updatePlugins(final String baseUrl, final boolean showOnlyUpdates, final JLabel infoLabel) {
    new Thread("Plugin Update Thread") {
      public void run() {
        try {
          infoLabel.setText(mLocalizer.msg("searchForPluginUpdates","Search for plugin updates..."));
          java.net.URL url = new java.net.URL(baseUrl + "/" + PluginAutoUpdater.PLUGIN_UPDATES_FILENAME);
          SoftwareUpdater softwareUpdater = new SoftwareUpdater(url,showOnlyUpdates);
          mSoftwareUpdateItems = softwareUpdater
              .getAvailableSoftwareUpdateItems();
          infoLabel.setText("");
        } catch (java.io.IOException e) {
          e.printStackTrace();
        }
        
        if (mSoftwareUpdateItems == null && !showOnlyUpdates) {
          JOptionPane.showMessageDialog(UiUtilities.getLastModalChildOf(MainFrame.getInstance()), mLocalizer.msg("error.1",
              "software check failed."));
        } else if (mSoftwareUpdateItems != null && mSoftwareUpdateItems.length == 0 && !showOnlyUpdates) {
          JOptionPane.showMessageDialog(UiUtilities.getLastModalChildOf(MainFrame.getInstance()), mLocalizer.msg("error.2",
              "No new items available"));
        } else if(mSoftwareUpdateItems != null && mSoftwareUpdateItems.length > 0) {
          Window w = UiUtilities.getLastModalChildOf(MainFrame.getInstance());
          SoftwareUpdateDlg dlg = null;
    
          if(w instanceof JDialog) {
            dlg = new SoftwareUpdateDlg((JDialog)w, baseUrl, showOnlyUpdates,mSoftwareUpdateItems);
          } else {
            dlg = new SoftwareUpdateDlg((JFrame)w, baseUrl, showOnlyUpdates,mSoftwareUpdateItems);
          }
    
          //dlg.setSoftwareUpdateItems(mSoftwareUpdateItems);
          dlg.setLocationRelativeTo(w);
          dlg.setVisible(true);
        }
        
        Settings.propLastPluginsUpdate.setDate(Date.getCurrentDate());
        
        infoLabel.setText("");
        mSoftwareUpdateItems = null;
      }
    }.start();
  }

  public void showFromTray(int state) {
    super.setVisible(true);
    toFront();
    setExtendedState(state);
    mIsVisible = true;
  }

  public void setVisible(boolean visible) {
    super.setVisible(visible);
    mIsVisible = visible;
  }

  public void repaint() {
    super.repaint();
    mRootNode.update();
  }
  
  public int askForDataUpdate(String reason) {
	    String msg1 = mLocalizer.msg("askforupdatedlg.1", "update now");
	    String msg2 = mLocalizer.msg("askforupdatedlg.2", "later");
	    String msg4 = mLocalizer.msg("askforupdatedlg.4",
	        "Do you want to update now?");
	    String msg5 = mLocalizer.msg("askforupdatedlg.5", "Update TV data");

	    Object[] options = { msg1, msg2 };
	    int result = JOptionPane.showOptionDialog(this, reason + "\n\n" + msg4, msg5,
	        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options,
	        options[0]);

	    if (result == JOptionPane.YES_OPTION) {
	      updateTvData();
	    }
      return result;
  }

  public int askForDataUpdateNoDataAvailable() {
	  return askForDataUpdate(mLocalizer.msg("askforupdatedlg.3",
        "No TV data for todays program available."));
  }
  
  public int askForDataUpdateChannelsAdded() {
	  return askForDataUpdate(mLocalizer.msg("askforupdatedlg.added",
      "You have added channels."));
  }

  public void showFilterDialog() {
    SelectFilterDlg dlg = new SelectFilterDlg(this);
    util.ui.UiUtilities.centerAndShow(dlg);
    mMenuBar.updateFiltersMenu();
  }
  
  public void updateFilterMenu() {
    mMenuBar.updateFiltersMenu();
  }

  public void showHelpDialog() {

    Locale locale = Locale.getDefault();
    String language = locale.getLanguage();

    java.io.File indexFile = new java.io.File("help/" + language
        + "/index.html");
    if (!indexFile.exists()) {
      indexFile = new java.io.File("help/default/index.html");
    }
    Launch.openURL(indexFile.getAbsolutePath());
  }

  /**
   * Updates the TimeChooser-Buttons
   */
  public void updateButtons() {
    mMenuBar.updateTimeItems();
    if (mTimebuttonsNode.getLeaf() != null) {
      ((TimeChooserPanel) mTimebuttonsNode.getLeaf()).updateButtons();
    }
  }

  public void setShowToolbar(boolean visible) {
    Settings.propIsTooolbarVisible.setBoolean(visible);
    mMenuBar.updateViewToolbarItem();
    updateToolbar();
  }
  
  public void setShowSearchField(boolean visible) {
    Settings.propIsSearchFieldVisible.setBoolean(visible);
    updateToolbar();
  }

  private void updateViews() {
    if (mIsVisible) {
      jcontentPane = (JPanel) getContentPane();
      jcontentPane.remove(mCenterComponent);
      mCenterComponent = mRootNode.getComponent();
      jcontentPane.add(mCenterComponent, BorderLayout.CENTER);
      jcontentPane.validate();
      jcontentPane.requestFocus();
      
      mRootNode.update();
    }
  }

  public void setShowTimeButtons(boolean visible) {
    setShowTimeButtons(visible, true);
  }
  
  public void setShowTimeButtons(boolean visible, boolean save) {
    if (visible) {
      mTimebuttonsNode.setLeaf(mTimeChooserPanel);
    } else {
      mTimebuttonsNode.setLeaf(null);
    }
    
    mTimeChooserPanel.setVisible(visible);
    
    if(save) {
      Settings.propShowTimeButtons.setBoolean(visible);
    }
    
    updateViews();
  }

  public void setShowDatelist(boolean visible) {
    setShowDatelist(visible, true);
  }
  
  public void setShowDatelist(boolean visible, boolean save) {
    if (visible) {
      mDateNode.setLeaf(new DateChooserPanel(this, mFinderPanel));
    } else {
      mDateNode.setLeaf(null);
    }
    
    mFinderPanel.setVisible(visible);
    
    if(save) {
      Settings.propShowDatelist.setBoolean(visible);
    }
    
    updateViews();
  }

  public void setShowChannellist(boolean visible) {
    setShowChannellist(visible, true);
  }
  
  public void setShowChannellist(boolean visible, boolean save) {
    if (visible) {
      mChannelNode.setLeaf(mChannelChooser);
    } else {
      mChannelNode.setLeaf(null);
    }
    
    mChannelChooser.setVisible(visible);
    
    if(save) {
      Settings.propShowChannels.setBoolean(visible);
    }
    
    updateViews();
  }

  /*
   * public void setPluginViewToolbarButtonSelected(boolean selected) {
   * mToolBar.setPluginViewToolbarButtonSelected(selected); }
   */

  public void setPluginViewButton(boolean selected) {
    if (mToolBarModel != null) {
      mToolBarModel.setPluginViewButtonSelected(selected);
      mToolBar.update();
    }
  }
  
  public void setShowPluginOverview(boolean visible) {
    setShowPluginOverview(visible, true);
  }

  /**
   * Gets if the plugin overview is shown.
   * <p>
   * @return <code>true</code> if the plugin overview is shown, <code>false</code> otherwise.
   * @since 2.2.2
   */
  public boolean isShowingPluginOverview() {
    return mPluginView != null;
  }

  public void setShowPluginOverview(boolean visible, boolean save) {
    if (visible) {
      mPluginView = new PluginView();
    } else {
      mPluginView = null;
    }
    mPluginsNode.setLeaf(mPluginView);
    mMenuBar.setPluginViewItemChecked(visible);
        
    if(save) {
      Settings.propShowPluginView.setBoolean(visible);
    }

    updateViews();
  }

 /* public void restoreViews() {
    mRootNode.setProperty(Settings.propViewRoot.getDefault());
    mMainframeNode.setProperty(Settings.propViewMainframe.getDefault());
    mNavigationNode.setProperty(Settings.propViewNavigation.getDefault());
    mDateChannelNode.setProperty(Settings.propViewDateChannel.getDefault());
    mRootNode.update();
  }*/

  /**
   * Makes the StatusBar visible
   * 
   * @param visible
   *          true if Statusbar should be visible
   */
  public void setShowStatusbar(boolean visible) {
    JPanel contentPane = (JPanel) getContentPane();

    Settings.propIsStatusbarVisible.setBoolean(visible);

    if (visible && !contentPane.isAncestorOf(mStatusBar)) {
      jcontentPane.add(mStatusBar, BorderLayout.SOUTH);
    } else if (contentPane.isAncestorOf(mStatusBar)) {
      jcontentPane.remove(mStatusBar);
    }

    contentPane.invalidate();
    contentPane.repaint();
  }

  public ProgressMonitor createProgressMonitor() {
    return mStatusBar.createProgressMonitor();
  }
  
  public void selectChannel(Channel channel) {
    mChannelChooser.selectChannel(channel);
  }

  /**
   * increase/decrease the font of the program table
   * 
   * @param offset positive values increase font, negative values decrease font, zero sets to default again
   */
  public void changeFontSize(int offset) {
    if (util.ui.ProgramPanel.updateFonts(offset)) {
      tvbrowser.ui.programtable.ChannelPanel.fontChanged();
      ProgramTableScrollPane scrollPane = getProgramTableScrollPane();
      scrollPane.forceRepaintAll();
    }
  }

  /**
   * increase/decrease the width of the program table columns
   * 
   * @param offset positive values increase column width,
   * negative values decrease column width, zero sets to default again
   */
  public void changeColumnWidth(int offset) {
    int columnWidth = util.ui.ProgramPanel.updateColumnWidth(offset);
    ProgramTableScrollPane scrollPane = getProgramTableScrollPane();
    scrollPane.setColumnWidth(columnWidth);
    scrollPane.forceRepaintAll();
  }

  public StatusBar getStatusBar() {
    return mStatusBar;
  }

  /**
   * get whether the mainframe is currently in full screen mode
   * 
   * @return in full screen mode
   * @since 2.5.3
   */
  public boolean isFullScreenMode() {
    return isUndecorated();
  }

  public void updatePluginTree() {
    if (mPluginView != null) {
      mPluginView.refreshTree();
    }
  }
}