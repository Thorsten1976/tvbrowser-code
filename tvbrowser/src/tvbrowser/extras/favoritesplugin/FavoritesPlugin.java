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

package tvbrowser.extras.favoritesplugin;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.HyperlinkEvent;

import devplugin.ActionMenu;
import devplugin.AfterDataUpdateInfoPanel;
import devplugin.ButtonAction;
import devplugin.ChannelDayProgram;
import devplugin.Date;
import devplugin.FilterChangeListenerV2;
import devplugin.Plugin;
import devplugin.PluginCenterPanel;
import devplugin.PluginCenterPanelWrapper;
import devplugin.PluginTreeNode;
import devplugin.Program;
import devplugin.ProgramFieldType;
import devplugin.ProgramFilter;
import devplugin.ProgramReceiveIf;
import devplugin.ProgramReceiveTarget;
import devplugin.ProgressMonitor;
import devplugin.SettingsItem;
import tvbrowser.core.Settings;
import tvbrowser.core.TvDataBase;
import tvbrowser.core.TvDataBaseListener;
import tvbrowser.core.TvDataUpdateListener;
import tvbrowser.core.TvDataUpdater;
import tvbrowser.core.filters.FilterManagerImpl;
import tvbrowser.core.icontheme.IconLoader;
import tvbrowser.extras.common.ConfigurationHandler;
import tvbrowser.extras.common.DataDeserializer;
import tvbrowser.extras.common.DataSerializer;
import tvbrowser.extras.common.InternalPluginProxyIf;
import tvbrowser.extras.common.ReminderConfiguration;
import tvbrowser.extras.favoritesplugin.core.ActorsFavorite;
import tvbrowser.extras.favoritesplugin.core.AdvancedFavorite;
import tvbrowser.extras.favoritesplugin.core.Exclusion;
import tvbrowser.extras.favoritesplugin.core.Favorite;
import tvbrowser.extras.favoritesplugin.core.FavoriteFilter;
import tvbrowser.extras.favoritesplugin.core.FilterFavorite;
import tvbrowser.extras.favoritesplugin.core.PendingFilterLoader;
import tvbrowser.extras.favoritesplugin.core.TitleFavorite;
import tvbrowser.extras.favoritesplugin.core.TopicFavorite;
import tvbrowser.extras.favoritesplugin.dlgs.EditFavoriteDialog;
import tvbrowser.extras.favoritesplugin.dlgs.FavoriteNode;
import tvbrowser.extras.favoritesplugin.dlgs.FavoriteTreeModel;
import tvbrowser.extras.favoritesplugin.dlgs.ManageFavoritesDialog;
import tvbrowser.extras.favoritesplugin.dlgs.ManageFavoritesPanel;
import tvbrowser.extras.favoritesplugin.wizards.ExcludeWizardStep;
import tvbrowser.extras.favoritesplugin.wizards.TypeWizardStep;
import tvbrowser.extras.favoritesplugin.wizards.WizardHandler;
import tvbrowser.extras.reminderplugin.ReminderPlugin;
import tvbrowser.ui.mainframe.MainFrame;
import tvdataservice.MutableChannelDayProgram;
import util.exc.ErrorHandler;
import util.exc.TvBrowserException;
import util.ui.Localizer;
import util.ui.NullProgressMonitor;
import util.ui.ScrollableJPanel;
import util.ui.TVBrowserIcons;
import util.ui.UiUtilities;
import util.ui.persona.Persona;

/**
 * Plugin for managing the favorite programs.
 *
 * @author Til Schneider, www.murfman.de
 */
public class FavoritesPlugin {
  public static final String ID_ACTION_MANAGE = "manageFavorites";
  public static final String ID_ACTION_SHOW_NEW = "showNewFavorites";
  
  public static final Logger mLog = Logger.getLogger(FavoritesPlugin.class.getName());
  /**
   * Tango category of the icon to be used in this plugin
   */
  private static final String ICON_CATEGORY = "emblems";

  /**
   * Tango name of the icon to be used in this plugin
   */
  private static final String ICON_NAME = "emblem-favorite";

  /** The localizer for this class. */
  public static final util.ui.Localizer mLocalizer = util.ui.Localizer
          .getLocalizerFor(FavoritesPlugin.class);

  private static FavoritesPlugin mInstance;

  private Properties mSettings = new Properties();

  private static final String DATAFILE_PREFIX = "favoritesplugin.FavoritesPlugin";

  private ConfigurationHandler mConfigurationHandler;

  private PluginTreeNode mRootNode;

  private boolean mHasRightToUpdate = false;

  private boolean mHasToUpdate = false;

  private static final String EXPERT_MODE_KEY = "expertMode";
  private static final String TYPE_SELECTION_KEY = "showTypeSelection";
  
  /**
   * do not save the favorite tree during TV data updates because it might not be consistent
   */
  private boolean mHasRightToSave = true;

  private Hashtable<String,ReceiveTargetItem> mSendPluginsTable = new Hashtable<String,ReceiveTargetItem>();
  private ProgramReceiveTarget[] mClientPluginTargets;

  private ArrayList<PendingFilterLoader> mPendingFavorites;
  private int mMarkPriority = -2;

  private Exclusion[] mExclusions;

  private static UpdateInfoThread mUpdateInfoThread;
  private Thread mUpdateThread;
  private AfterDataUpdateInfoPanel mInfoPanel;
  private ManageFavoritesPanel panel;

  private ExecutorService mThreadPool;
  private JPanel mCenterPanel;
  
  private PluginCenterPanelWrapper mWrapper;
  
  private ManageFavoritesPanel mMangePanel;
  
  private ProgramFieldType[] mDefaultProgramFieldTypeSelection;
  
  private AncestorListener mAncestorListener;
  private AtomicReference<Program[]> mLastFoundPrograms;
  
  /**
   * Creates a new instance of FavoritesPlugin.
   */
  private FavoritesPlugin() {
    mInstance = this;
    mLastFoundPrograms = new AtomicReference<Program[]>(new Program[0]);
    mDefaultProgramFieldTypeSelection = null;
    mRootNode = new PluginTreeNode(getName());
    mWrapper = new PluginCenterPanelWrapper() {  
      FavoritesCenterPanel centerPanel = new FavoritesCenterPanel();
      @Override
      public PluginCenterPanel[] getCenterPanels() {
        return new PluginCenterPanel[] {centerPanel};
      }
      
      @Override
      public void scrolledToDate(Date date) {
        if(mMangePanel != null) {
          mMangePanel.scrollToDate(date);
        }
      }
      
      @Override
      public void scrolledToNow() {
        if(mMangePanel != null) {
          mMangePanel.scrollToNow();
        }
      }
      
      @Override
      public void scrolledToTime(int time) {
        if(mMangePanel != null) {
          mMangePanel.scrollToTime(time, timeButtonsScrollToNextTimeInTab());
        }
      }
      
      @Override
      public void filterSelected(ProgramFilter filter) {
        if(mMangePanel != null && reactOnFilterChange()) {
          mMangePanel.selectFilter(filter);
        }
      }
    };
    
    mCenterPanel = UiUtilities.createPersonaBackgroundPanel();
    mExclusions = new Exclusion[0];
    mPendingFavorites = new ArrayList<PendingFilterLoader>(0);
    mClientPluginTargets = new ProgramReceiveTarget[0];
    mConfigurationHandler = new ConfigurationHandler(DATAFILE_PREFIX);
    load();

    TvDataBase.getInstance().addTvDataListener(new TvDataBaseListener() {
      public void dayProgramTouched(final ChannelDayProgram removedDayProgram,
          final ChannelDayProgram addedDayProgram) {
        if(mThreadPool == null) {
          mThreadPool = Executors.newFixedThreadPool(Math.max(Runtime.getRuntime().availableProcessors(),3));
        }

        Runnable update = () -> {
          Favorite[] favArray = null;
          
          if(removedDayProgram != null || addedDayProgram != null) {
            favArray = FavoriteTreeModel.getInstance().getFavoriteArr();
          }
          
          if(removedDayProgram != null) {
            Iterator<Program> it1 = removedDayProgram.getPrograms();

            while (it1.hasNext()) {
              try {
                Program p1 = it1.next();

                for (Favorite fav1 : favArray) {
                  fav1.removeProgram(p1);
                }
              }catch(Throwable t) {
                ErrorHandler.handle("Error in removing program from Favorites",t);
              }
            }
          }

          if(addedDayProgram != null) {
            Iterator<Program> it2 = addedDayProgram.getPrograms();
            while (it2.hasNext()) {
              final Program p2 = it2.next();

              for (Favorite fav2 : favArray) {
                try {
                  fav2.tryToMatch(p2);
                } catch (Throwable t) {
                  ErrorHandler.handle("Error in searching programs for Favorites",t);
                }
              }
            }
          }
        };

        mThreadPool.execute(update);
      }

      public void dayProgramAdded(ChannelDayProgram prog) {}
      public void dayProgramDeleted(ChannelDayProgram prog) {}
      public void dayProgramAdded(MutableChannelDayProgram prog) {}
    });

    TvDataUpdater.getInstance().addTvDataUpdateListener(new TvDataUpdateListener() {
      public void tvDataUpdateStarted(devplugin.Date until) {
        mHasRightToSave = false;
        mSendPluginsTable.clear();
        
        if(mInfoPanel == null) {
          for (Favorite favorite : FavoriteTreeModel.getInstance().getFavoriteArr()) {
            favorite.clearNewPrograms();
            favorite.clearRemovedPrograms();
          }
        }
      }

      public void tvDataUpdateFinished() {
        // only update the favorites if new data was downloaded
        if (TvDataUpdater.getInstance().tvDataWasChanged()) {
          if(!mSendPluginsTable.isEmpty()) {
            sendToPlugins();
          }
          //handleTvDataUpdateFinished();
        }
      }
    });
  }

  /**
   * Waits for finishing the update threads.
   * @since 2.7.2
   */
  public void waitForFinishingUpdateThreads() {
    if (mThreadPool != null) {
      mLog.info("Favorites: Wait for update threads to finish");
      mThreadPool.shutdown();

      try {
        boolean success = mThreadPool.awaitTermination(Math.max(
            FavoriteTreeModel.getInstance().getFavoriteArr().length, 10),
            TimeUnit.SECONDS);

        if (success) {
          mLog.info("Favorites: Update threads were finished");
        } else {
          mLog
              .severe("Favorites: Timeout on waiting for update threads to finish was reached");
        }
      } catch (InterruptedException e) {
        mLog.log(Level.INFO,"Waiting for favorite update finishing was interrupted",e);
      }

      mThreadPool = null;
    }
  }
  
  protected void showNewFavorites() {
    final ArrayList<Favorite> infoFavoriteList = new ArrayList<Favorite>(0);

    final Favorite[] favoriteArr = FavoriteTreeModel.getInstance().getFavoriteArr();

    for (Favorite favorite : favoriteArr) {
      if (favorite.getNewPrograms().length > 0) {
        infoFavoriteList.add(favorite);
      }
    }
    
    showManageFavoritesDialog(true, infoFavoriteList.toArray(new Favorite[infoFavoriteList.size()]), null);
  }

  protected void handleTvDataUpdateFinished() {
    mHasToUpdate = true;
    mUpdateInfoThread = null;
    
    if(mHasRightToUpdate) {
      mUpdateThread = new Thread("Favorites: handle update finished") {
        public void run() {try{
          mHasToUpdate = false;

          ManageFavoritesDialog dlg = ManageFavoritesDialog.getInstance();

          if(dlg != null) {
            dlg.favoriteSelectionChanged();
          }

          //FavoriteTreeModel.getInstance().reload();

          mHasRightToSave = true;
          updateRootNode(true);

          ArrayList<Favorite> infoFavoriteList = new ArrayList<Favorite>(0);

          Favorite[] favoriteArr = FavoriteTreeModel.getInstance().getFavoriteArr();

          for (Favorite favorite : favoriteArr) {
            favorite.clearRemovedPrograms();

            if (favorite.isRemindAfterDownload() && favorite.getNewPrograms().length > 0) {
              infoFavoriteList.add(favorite);
            }
          }

          if(!infoFavoriteList.isEmpty()) {
            Favorite[] infoFavoriteArr = infoFavoriteList.toArray(new Favorite[infoFavoriteList.size()]);
            
            if(mUpdateInfoThread == null || !mUpdateInfoThread.isAlive()) {
              mUpdateInfoThread = new UpdateInfoThread();
              mUpdateInfoThread.setPriority(Thread.MIN_PRIORITY);
              mUpdateInfoThread.addFavorites(infoFavoriteArr);
              mUpdateInfoThread.start();
            }
            else {
              mUpdateInfoThread.addFavorites(infoFavoriteArr);
            }
          }

          Favorite[] favorites = FavoriteTreeModel.getInstance().getFavoriteArr();

          for(Favorite fav : favorites) {
            fav.revalidatePrograms();
          }
          
          if(mMangePanel != null) {
            mMangePanel.handleFavoriteEvent();
          }
          
          loadLastFoundPrograms();
        }catch(Throwable t) {t.printStackTrace();}
        }
      };
      mUpdateThread.start();

      try {
        mUpdateThread.join();
      } catch (InterruptedException e) { /* ignore */ }
    }
  }

  public static synchronized FavoritesPlugin getInstance() {
    if (mInstance == null) {
      new FavoritesPlugin();
    }
    return mInstance;
  }

  public void handleTvBrowserStartFinished() {
    updateRootNode(false);
    if(!mPendingFavorites.isEmpty()) {
      for(PendingFilterLoader fav : mPendingFavorites) {
        fav.loadPendingFilter();
      }

      mPendingFavorites.clear();
      mPendingFavorites = null;
    }
    
    final Favorite[] favs = FavoriteTreeModel.getInstance().getFavoriteArr();
    
    for(Favorite fav : favs) {
      fav.initializeFilterExclusions();
    }

    mHasRightToUpdate = true;

    if(mHasToUpdate) {
      handleTvDataUpdateFinished();
    }
    
    FilterManagerImpl.getInstance().registerFilterChangeListener(new FilterChangeListenerV2() {
      @Override
      public void filterTouched(ProgramFilter filter) {
        Favorite[] favorites = FavoriteTreeModel.getInstance().getFavoriteArr();
        boolean reload = false;
        
        for(Favorite fav : favorites) {
          if(fav instanceof FilterFavorite) {
            reload = ((FilterFavorite)fav).updateFilter(filter) || reload;
          }
          else if(fav instanceof AdvancedFavorite) {
            reload = ((AdvancedFavorite)fav).updateFilter(filter) || reload;
          }
          else {
            reload = fav.updateFilterExclusion(filter,true) || reload;
          }
        }
        
        if(reload) {
          if(mMangePanel != null) {
            mMangePanel.reload(true);
          }
          
          if(ManageFavoritesDialog.getInstance() != null) {
            ManageFavoritesDialog.getInstance().reload(true);
          }
          
          store();
        }
      }
      
      @Override
      public void filterRemoved(ProgramFilter filter) {
        Favorite[] favorites = FavoriteTreeModel.getInstance().getFavoriteArr();
        boolean reload = false;
        
        for(Favorite fav : favorites) {
          if(fav instanceof FilterFavorite) {
            reload = ((FilterFavorite)fav).deleteFilter(filter) || reload;
          }
          else if(fav instanceof AdvancedFavorite) {
            reload = ((AdvancedFavorite)fav).deleteFilter(filter) || reload;
          }
          else {
            reload = fav.deleteFilterExclusion(filter,true) || reload;
          }
        }
        
        if(reload) {
          if(mMangePanel != null) {
            mMangePanel.reload(true);
          }
          if(ManageFavoritesDialog.getInstance() != null) {
            ManageFavoritesDialog.getInstance().reload(true);
          }
          
          store();
        }
      }
      
      @Override
      public void filterDefaultChanged(ProgramFilter filter) {}
      
      @Override
      public void filterAdded(ProgramFilter filter) {}
    });
    
    addPanel();
  }
  
  private void addPanel() {
    SwingUtilities.invokeLater(() -> {
      if(mSettings.getProperty("provideTab", "true").equals("true")) {
        if(mMangePanel == null) {
          int splitPanePosition = getIntegerSetting(mSettings, "splitpanePosition",200);
          
          mMangePanel = new ManageFavoritesPanel(null, splitPanePosition, false, null, true);
          
          
       /*   mMangePanel.addAncestorListener(new AncestorListener() {
            private boolean mCheck = false;
            @Override
            public void ancestorRemoved(AncestorEvent event) {}
            
            @Override
            public void ancestorMoved(AncestorEvent event) {}
            
            @Override
            public void ancestorAdded(AncestorEvent event) {
              if(mMangePanel != null) {
                mMangePanel.scrollToFirstNotExpiredIndex(mCheck);
                
                mCheck = true;
              }
            }
          });*/
          
          
          mAncestorListener = new AncestorListener() {
            private boolean mCheck = false;
            @Override
            public void ancestorRemoved(AncestorEvent event) {
              Persona.getInstance().removePersonaListener(mMangePanel);
              mMangePanel.removePersonaListener();
              mCenterPanel.remove(mMangePanel);
            }
            
            @Override
            public void ancestorMoved(AncestorEvent event) {}
            
            @Override
            public void ancestorAdded(AncestorEvent event) {
              Persona.getInstance().registerPersonaListener(mMangePanel);
              mMangePanel.registerPersonaListener();
              mCenterPanel.add(mMangePanel, BorderLayout.CENTER);
              mCenterPanel.repaint();
              mMangePanel.updatePersona();
              
              SwingUtilities.invokeLater(() -> {
                mMangePanel.scrollToFirstNotExpiredIndex(mCheck);
                mCheck = true;
              });
                          
            }
          };
          mCenterPanel.addAncestorListener(mAncestorListener);
          
          if(mCenterPanel.isVisible()) {
            mAncestorListener.ancestorAdded(null);
          }
        }
      }
      else {
        if(mMangePanel != null) {
          Persona.getInstance().removePersonaListener(mMangePanel);
          mMangePanel.removePersonaListener();
        }
        
        mCenterPanel.removeAncestorListener(mAncestorListener);
        
        mMangePanel = null;
      }
    });
  }

  private void load() {
    try {
      Properties prop = mConfigurationHandler.loadSettings();
      loadSettings(prop);
    }catch(IOException e) {
      ErrorHandler.handle(mLocalizer.msg("couldNotLoadFavoritesSettings","Could not load settings for favorites"), e);
    }

    try {
      mConfigurationHandler.loadData(new DataDeserializer(){
        public void read(ObjectInputStream in) throws IOException, ClassNotFoundException {
          readData(in);
          store();
        }
      });
    }catch(IOException e) {
      ErrorHandler.handle(mLocalizer.msg("couldNotLoadFavorites","Could not load favorites"), e);
    }
  }

  public synchronized void store() {
    try {
      mConfigurationHandler.storeData(new DataSerializer(){
        public void write(ObjectOutputStream out) throws IOException {
          writeData(out);
        }
      });
    } catch (IOException e) {
      ErrorHandler.handle(mLocalizer.msg("couldNotStoreFavorites","Could not store favorites"), e);
    }

    try {
      if(mMangePanel != null) {
        mSettings.put("lastSelectedProgramFilter", mMangePanel.getSelectedProgramFilterName());
      }
      
      mConfigurationHandler.storeSettings(mSettings);
    } catch (IOException e) {
      ErrorHandler.handle(mLocalizer.msg("couldNotStoreFavoritesSettings","Could not store settings for favorites"), e);
    }
  }

  public static ImageIcon getIconFromTheme(String category, String Icon, int size) {
    return IconLoader.getInstance().getIconFromTheme(category, Icon, size);
  }

  private void readData(ObjectInputStream in) throws IOException,
          ClassNotFoundException {
    int version = in.readInt();

    Favorite[] newFavoriteArr;

    if(version < 6) {
      int size = in.readInt();

      newFavoriteArr = new Favorite[size];
      for (int i = 0; i < size; i++) {
        if (version <= 2) {
          newFavoriteArr[i] = AdvancedFavorite.loadOldFavorite(in);
        }
        else {
          String typeID = (String)in.readObject();
          if (TopicFavorite.TYPE_ID.equals(typeID)) {
            newFavoriteArr[i] = new TopicFavorite(in);
          }
          else if (TitleFavorite.TYPE_ID.equals(typeID)) {
            newFavoriteArr[i] = new TitleFavorite(in);
          }
          else if (ActorsFavorite.TYPE_ID.equals(typeID)) {
            newFavoriteArr[i] = new ActorsFavorite(in);
          }
          else if (AdvancedFavorite.TYPE_ID.equals(typeID)) {
            newFavoriteArr[i] = new AdvancedFavorite(in);
          }

        }
      }
      FavoriteTreeModel.initInstance(newFavoriteArr);
    }
    else {
      FavoriteTreeModel.initInstance(in,version);
      newFavoriteArr = FavoriteTreeModel.getInstance().getFavoriteArr();
    }

    for (Favorite newFavorite : newFavoriteArr) {
      Program[] programArr = newFavorite.getWhiteListPrograms();
      for (Program program : programArr) {
        program.mark(FavoritesPluginProxy.getInstance());
      }
    }

    boolean reminderFound = false;

    // Get the client plugins
    if(version <= 4) {
      int size = in.readInt();
      ArrayList<ProgramReceiveTarget> list = new ArrayList<ProgramReceiveTarget>(0);
      for (int i = 0; i < size; i++) {
        String id = null;

        if (version == 1) {
          // In older versions of TV-Browser, not the plugin ID was saved,
          // but its class name.
          // -> We have to translate the class name into an ID.
          String className = (String) in.readObject();
          id = "java." + className;
        } else {
          id = (String) in.readObject();
        }

        if(version <= 2) {
          if(id.equals("java.reminderplugin.ReminderPlugin")) {
            reminderFound = true;
          }
        }

        if(version > 2 || (version <= 2 && !id.equals("java.reminderplugin.ReminderPlugin"))) {
          list.add(ProgramReceiveTarget.createDefaultTargetForProgramReceiveIfId(id));
        }
      }

      if(!list.isEmpty()) {
        mClientPluginTargets = list.toArray(new ProgramReceiveTarget[list.size()]);
      }
    }
    else {
      int n = in.readInt();
      mClientPluginTargets = new ProgramReceiveTarget[n];

      for (int i = 0; i < n; i++) {
        mClientPluginTargets[i] = new ProgramReceiveTarget(in);
      }
    }

    if(version <= 2 && reminderFound) {
      for (Favorite newFavorite : newFavoriteArr) {
        newFavorite.getReminderConfiguration().setReminderServices(new String[] {ReminderConfiguration.REMINDER_DEFAULT});
      }

      updateAllFavorites();
    }

    if(version >= 4 && version < 8) {
      in.readBoolean();
    }

    if(version >= 7) {
      mExclusions = new Exclusion[in.readInt()];

      for(int i = 0; i < mExclusions.length; i++) {
        mExclusions[i] = new Exclusion(in);
      }
    }
    
    
    if (version >= 9) {
      int fieldTypeCount = in.readInt();
      
      if(fieldTypeCount > 0) {
        mDefaultProgramFieldTypeSelection = new ProgramFieldType[fieldTypeCount];
        
        for(int i = 0; i < mDefaultProgramFieldTypeSelection.length; i++) {
          int typeId = in.readInt();
          mDefaultProgramFieldTypeSelection[i] = ProgramFieldType.getTypeForId(typeId);
        }
      }
      else {
        mDefaultProgramFieldTypeSelection = null;
      }
    }
    
    loadLastFoundPrograms();
  }
  
  private void loadLastFoundPrograms() {
    final Favorite[] favoriteArr = FavoriteTreeModel.getInstance().getFavoriteArr();
    final ArrayList<Program> foundPrograms = new ArrayList<>();
    
    for (Favorite favorite : favoriteArr) {
      favorite.clearRemovedPrograms();

      final Program[] newPrograms = favorite.getNewPrograms();
      
      for(Program test : newPrograms) {
        if(!foundPrograms.contains(test)) {
          foundPrograms.add(test);
        }
      }
    }
    
    mLastFoundPrograms.set(foundPrograms.toArray(new Program[foundPrograms.size()]));
  }
  
  private void updateAllFavorites() {
    mSendPluginsTable.clear();

    ProgressMonitor monitor;

    Favorite[] favoriteArr = FavoriteTreeModel.getInstance().getFavoriteArr();

    if (favoriteArr.length > 5) {    // if we have more then 5 favorites, we show a progress bar
      try {
        monitor = MainFrame.getInstance().createProgressMonitor();
      }catch(Exception e) {e.printStackTrace();
        monitor = new NullProgressMonitor();
      }
    }
    else {
      monitor = new NullProgressMonitor();
    }
    monitor.setMaximum(favoriteArr.length);
    monitor.setMessage(mLocalizer.msg("updatingFavorites","Updating favorites"));

    for (int i=0;i<favoriteArr.length; i++) {
      monitor.setValue(i);

      try {
        favoriteArr[i].refreshBlackList();
        favoriteArr[i].updatePrograms(true,true);
      } catch (TvBrowserException e) {
        ErrorHandler.handle(e);
      }
    }
    monitor.setMessage("");
    monitor.setValue(0);

    if(!mSendPluginsTable.isEmpty()) {
      sendToPlugins();
    }
  }

  private void sendToPlugins() {
    Collection<ReceiveTargetItem> targets = mSendPluginsTable.values();
    StringBuilder buffer = new StringBuilder();
    ArrayList<Favorite> errorFavorites = new ArrayList<Favorite>(0);

    for(ReceiveTargetItem target : targets) {
      if(!target.getReceiveTarget().getReceifeIfForIdOfTarget().receivePrograms(target.getPrograms(), target.getReceiveTarget())) {
        Favorite[] favs =FavoriteTreeModel.getInstance().getFavoritesContainingReceiveTarget(target.getReceiveTarget());

        for(Favorite fav : favs) {
          if(!errorFavorites.contains(fav)) {
            errorFavorites.add(fav);
          }
        }

        buffer.append(
            target.getReceiveTarget().getReceifeIfForIdOfTarget().toString())
            .append(" - ").append(target.toString()).append('\n');
      }
    }

    if(buffer.length() > 0) {
      buffer.insert(0,mLocalizer.msg("sendError","Error by sending programs to other plugins.\n\nPlease check the favorites that should send\nprograms to the following plugins:\n"));
      buffer.append(mLocalizer.msg("sendErrorFavorites","\nThe following Favorites are affected by this:\n"));

      ScrollableJPanel panel = new ScrollableJPanel();
      panel.setBorder(BorderFactory.createEmptyBorder(0,1,0,1));
      panel.setLayout(new BoxLayout(panel,BoxLayout.Y_AXIS));

      for(Favorite fav : errorFavorites) {
        final Favorite finalFav = fav;
        panel.add(UiUtilities.createHtmlHelpTextArea("<a href=\"#link\">" + fav.getName() + "</a>",e -> {
          if(e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            editFavorite(finalFav);
          }
        }));
      }

      JScrollPane pane = new JScrollPane(panel);
      pane.setPreferredSize(new Dimension(0,100));

      Object[] msg = {buffer.toString(),pane};

      JOptionPane.showMessageDialog(UiUtilities.getLastModalChildOf(MainFrame.getInstance()),msg,Localizer.getLocalization(Localizer.I18N_ERROR),JOptionPane.ERROR_MESSAGE);
    }
  }

  /**
   * Add the programs to send to other Plugins to a Hashtable.
   *
   * @param targets The ProgramReceiveTargets to send the programs for.
   * @param programs The Programs to send.
   */
  public void addProgramsForSending(ProgramReceiveTarget[] targets, Program[] programs) {
    for(ProgramReceiveTarget target : targets) {
      if(target != null && target.getReceifeIfForIdOfTarget() != null) {
        synchronized(mSendPluginsTable) {
          ReceiveTargetItem item = mSendPluginsTable.get(getKeyForReceiveTarget(target));

          if(item == null) {
            item = new ReceiveTargetItem(target);
            mSendPluginsTable.put(getKeyForReceiveTarget(target), item);
          }

          item.addPrograms(programs);
        }
      }
    }
  }

  /**
   * @return If the management dialog should show the
   * programs on the black list too.
   */
  public boolean isShowingBlackListEntries() {
    return mSettings.getProperty("showBlackEntries","false").compareTo("true") == 0;
  }

  /**
   * Set the value for showing black list entries
   * in the management dialog.
   *
   * @param value If the programs are to show.
   */
  public void setIsShowingBlackListEntries(boolean value) {
    mSettings.setProperty("showBlackEntries",String.valueOf(value));
  }

  private void writeData(ObjectOutputStream out) throws IOException {
    out.writeInt(9); // version

    FavoriteTreeModel.getInstance().storeData(out);

    out.writeInt(mClientPluginTargets.length);
    for (ProgramReceiveTarget target : mClientPluginTargets) {
      target.writeData(out);
    }

    out.writeInt(mExclusions.length);

    for (Exclusion exclusion : mExclusions) {
      exclusion.writeData(out);
    }
    
    if(mDefaultProgramFieldTypeSelection != null) {
      out.writeInt(mDefaultProgramFieldTypeSelection.length);
      for(ProgramFieldType fieldType : mDefaultProgramFieldTypeSelection) {
        out.writeInt(fieldType.getTypeId());
      }
    }
    else {
      out.writeInt(0);
    }
  }

  /**
   * Called by the host-application during start-up. Implements this method to
   * load your plugins settings from the file system.
   */
  private void loadSettings(Properties settings) {
    mSettings = settings;
    if (settings == null) {
      throw new IllegalArgumentException("settings is null");
    }
  }

  private int getIntegerSetting(Properties prop, String key, int defaultValue) {
    int res = defaultValue;
    try {
      res = Integer.parseInt(prop.getProperty(key, Integer
          .toString(defaultValue)));
    } catch (NumberFormatException e) {
      // ignore
    }
    return res;
  }

  protected ActionMenu getButtonAction() {
    ButtonAction action = new ButtonAction();
    action.setActionListener(e -> {
      getInstance().showManageFavoritesDialog();
    });

    action.setBigIcon(getIconFromTheme(ICON_CATEGORY, ICON_NAME, 22));
    action.setSmallIcon(getIconFromTheme(ICON_CATEGORY, ICON_NAME, 16));
    action.setShortDescription(mLocalizer.msg("favoritesManager",
            "Manage favorite programs"));
    action.setText(getName());
    action.putValue(Plugin.ACTION_ID_KEY, ID_ACTION_MANAGE);

    ButtonAction showNew = new ButtonAction();
    showNew.setActionListener(e -> {
      getInstance().showNewFavorites();
    });
    showNew.setBigIcon(getIconFromTheme(ICON_CATEGORY, ICON_NAME, 22));
    showNew.setSmallIcon(getIconFromTheme(ICON_CATEGORY, ICON_NAME, 16));
    showNew.setShortDescription(mLocalizer.msg("showNewDesc",
            "Show new programs found at last data update again"));
    showNew.setText(mLocalizer.msg("showNewTitle",
        "Show new programs"));
    showNew.putValue(InternalPluginProxyIf.KEYBOARD_ACCELERATOR, KeyStroke.getKeyStroke(KeyEvent.VK_E, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
    showNew.putValue(Plugin.ACTION_ID_KEY, ID_ACTION_SHOW_NEW);
    
    final ActionMenu m = new ActionMenu(getName(), getIconFromTheme(ICON_CATEGORY, ICON_NAME, 16), new Action[] {action,showNew});
    m.getAction().putValue(Plugin.BIG_ICON, getIconFromTheme(ICON_CATEGORY, ICON_NAME, 22));
    
    return m;
  }


  protected void showManageFavoritesDialog() {
    showManageFavoritesDialog(null);
  }

  protected void showManageFavoritesDialog(final Favorite initialSelection) {
    showManageFavoritesDialog(false, null, initialSelection);
  }

  protected ActionMenu getContextMenuActions(Program program) {
    return new ContextMenuProvider(FavoriteTreeModel.getInstance().getFavoriteArr()).getContextMenuActions(program);
  }
  
  public void editFavorite(Favorite favorite) {

    Window parent = UiUtilities.getLastModalChildOf(MainFrame.getInstance());
    EditFavoriteDialog dlg = new EditFavoriteDialog(parent, favorite);
    UiUtilities.centerAndShow(dlg);
    if (dlg.getOkWasPressed()) {
      updateRootNode(true);
    }
  }

  private void showManageFavoritesDialog(final boolean showNew, final Favorite[] favoriteArr, final Favorite initialSelection) {
    int splitPanePosition = getIntegerSetting(mSettings, "splitpanePosition",
            200);
    ManageFavoritesDialog dlg = new ManageFavoritesDialog(MainFrame.getInstance(), favoriteArr, splitPanePosition, showNew, initialSelection);

    Settings.layoutWindow("extras.manageFavoritesDlg",dlg,new Dimension(650,450));
    dlg.setVisible(true);

    splitPanePosition = dlg.getSplitpanePosition();
    mSettings.setProperty("splitpanePosition", Integer
        .toString(splitPanePosition));

    if (!showNew) {
      updateRootNode(true);
    }
  }

  public boolean isUsingExpertMode() {
    return mSettings.getProperty(EXPERT_MODE_KEY,"false").compareTo("true") == 0;
  }
  
  public void setIsUsingExpertMode(boolean value) {
    mSettings.setProperty(EXPERT_MODE_KEY,String.valueOf(value));
  }
  
  public boolean showTypeSelection() {
    return mSettings.getProperty(TYPE_SELECTION_KEY,"true").compareTo("true") == 0;
  }
  
  public void setShowTypeSelection(boolean value) {
    mSettings.setProperty(TYPE_SELECTION_KEY,String.valueOf(value));
  }
  
  public boolean isShowingPictures() {
    return mSettings.getProperty("showPictures","false").compareTo("true") == 0;
  }

  public void setIsShowingPictures(boolean value) {
    mSettings.setProperty("showPictures",String.valueOf(value));
  }

  public void showCreateFavoriteWizard(Program program) {
    showCreateFavoriteWizard(program, null);
  }

  public void showCreateFavoriteWizard(Program program, String path) {
    showCreateFavoriteWizardInternal(program, null, null);
  }

  public void showCreateActorFavoriteWizard(Program program, String actor) {
    showCreateFavoriteWizardInternal(program, actor, null);
  }

  public void showCreateTopicFavoriteWizard(Program program, String topic) {
    showCreateFavoriteWizardInternal(program, null, topic);
  }

  private void showCreateFavoriteWizardInternal(Program program, String actor,
      String topic) {
    Window parent = UiUtilities.getLastModalChildOf(MainFrame.getInstance());
    Favorite favorite;
    if (isUsingExpertMode()) {
      if(showTypeSelection() && JOptionPane.showConfirmDialog(UiUtilities.getLastModalChildOf(MainFrame.getInstance()), mLocalizer.msg("askType.message", "Create a filter favorite?"), mLocalizer.msg("askType.title", "Type selection"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
        favorite = new FilterFavorite();
      }
      else {
        favorite = new AdvancedFavorite(program != null ? program.getTitle() : "");
      }
      
      EditFavoriteDialog dlg = new EditFavoriteDialog(parent, favorite);
      UiUtilities.centerAndShow(dlg);
      if (!dlg.getOkWasPressed()) {
        favorite = null;
      }

    } else {
      WizardHandler handler;
      TypeWizardStep initialStep = new TypeWizardStep(program);
      if (topic != null) {
        initialStep.setTopic(topic);
      } else if (actor != null) {
        initialStep.setActor(actor);
      }
      handler = new WizardHandler(parent, initialStep);
      favorite = (tvbrowser.extras.favoritesplugin.core.Favorite)handler.show();
    }
    
    if (favorite != null) {
      try {
        favorite.updatePrograms();
        FavoriteTreeModel.getInstance().addFavorite(favorite);
        
        if(ManageFavoritesDialog.getInstance() != null) {
          ManageFavoritesDialog.getInstance().addFavorite(favorite, null);
        }
        if(mMangePanel != null) {
          mMangePanel.reload();
        }

      }catch (TvBrowserException exc) {
        ErrorHandler.handle(mLocalizer.msg("couldNotUpdateFavorites","Could not update favorites."), exc);
      }

      if (program != null && favorite.getPrograms().length == 0 && !favorite.isRemindAfterDownload()) {
        Object[] options = {mLocalizer.msg("btn.notifyMe","Notify Me"), mLocalizer.msg("btn.editFavorite","Edit Favorite"), mLocalizer.msg("btn.ignore","Ignore")};
        int option = JOptionPane.showOptionDialog(parent, mLocalizer.msg("dlg.noMatchingPrograms","Currently no program matches the newly created favorite.\n\nDo you want TV-Browser to notify you when any program matches this favorite?"),
                  mLocalizer.msg("dlg.title.information","Information"),
                  JOptionPane.YES_NO_OPTION,
                  JOptionPane.INFORMATION_MESSAGE,
                  null,
                  options,
                  options[0]);
        if (option == JOptionPane.YES_OPTION) {
         favorite.setRemindAfterDownload(true);
        }
        else if (option == JOptionPane.NO_OPTION) {
          editFavorite(favorite);
        }
      }

      else if (program != null && !favorite.contains(program)) {
        // only show a warning for non matching favorites if the program is older than today
        if (program.getDate().compareTo(new devplugin.Date()) >= 0) {
          Object[] options = {mLocalizer.msg("btn.editFavorite","Edit Favorite"), mLocalizer.msg("btn.ignore","Ignore")};
          if (JOptionPane.showOptionDialog(parent, mLocalizer.msg("dlg.programDoesntMatch","The currently selected program does not belong to the newly created favorite.\n\nDo you want to edit the favorite?"),
              mLocalizer.msg("dlg.title.warning","Warning"),
              JOptionPane.YES_NO_OPTION,
              JOptionPane.WARNING_MESSAGE,
              null,
              options,
              options[1]) == JOptionPane.YES_OPTION) {
            editFavorite(favorite);
          }
        }
      }
    }

    saveFavorites();
  }

  public synchronized void saveFavorites() {
    store();
    
    Thread thread = new Thread("Save favorites") {
      public void run() {
        SwingUtilities.invokeLater(() -> {
          if(mMangePanel != null) {
            mMangePanel.handleFavoriteEvent();
          }
        });
      }
    };
    thread.setPriority(Thread.MIN_PRIORITY);
    thread.start();
  }


  public void showExcludeProgramsDialog(Favorite fav, Program program) {
    WizardHandler handler = new WizardHandler(UiUtilities.getLastModalChildOf(MainFrame.getInstance()), new ExcludeWizardStep(fav, program));
    Object exclusion = handler.show();

    if (exclusion != null) {
      if(fav == null) {
        Exclusion[] exclusionArr = new Exclusion[mExclusions.length + 1];
        System.arraycopy(mExclusions,0,exclusionArr,0,mExclusions.length);
        exclusionArr[mExclusions.length] = (Exclusion)exclusion;

        setGlobalExclusions(exclusionArr, true);
      }else {
        if(exclusion instanceof Exclusion) {
          fav.addExclusion((Exclusion)exclusion);
        }
        else if(exclusion instanceof String && exclusion.equals("blacklist")) {
          fav.addToBlackList(program);
        }
      }
    }
  }


  public void askAndDeleteFavorite(Favorite fav) {
    if (JOptionPane.showConfirmDialog(UiUtilities.getLastModalChildOf(MainFrame.getInstance()),
              mLocalizer.msg("reallyDelete", "Really delete favorite '{0}'?",fav.getName()),
              Localizer
        .getLocalization(Localizer.I18N_DELETE),
              JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
      FavoriteTreeModel.getInstance().deleteFavorite(fav);
      
      saveFavorites();
    }
  }

  public PluginTreeNode getRootNode() {
    return mRootNode;
  }

  public void updateRootNode(boolean save) {
    FavoriteTreeModel.getInstance().resetMultiplesCounter();
    mRootNode.removeAllActions();
    mRootNode.getMutableTreeNode().setIcon(getFavoritesIcon(16));

    Action manageFavorite = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        showManageFavoritesDialog();
      }
    };
    manageFavorite.putValue(Action.SMALL_ICON, getFavoritesIcon(16));
    manageFavorite.putValue(Action.NAME, mLocalizer.ellipsisMsg("favoritesManager", "Manage Favorites"));


    Action addFavorite = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        showCreateFavoriteWizard(null);
      }
    };
    addFavorite.putValue(Action.SMALL_ICON, TVBrowserIcons.newIcon(TVBrowserIcons.SIZE_SMALL));
    addFavorite.putValue(Action.NAME, mLocalizer.ellipsisMsg("new", "Create new favorite"));

    Action openSettings = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        MainFrame.getInstance().showSettingsDialog(SettingsItem.FAVORITE);
      }
    };
    openSettings.putValue(Action.SMALL_ICON, TVBrowserIcons.preferences(TVBrowserIcons.SIZE_SMALL));
    openSettings.putValue(Action.NAME, Localizer.getEllipsisLocalization(Localizer.I18N_SETTINGS));

    mRootNode.addAction(manageFavorite);
    mRootNode.addAction(addFavorite);
    mRootNode.addAction(null);
    mRootNode.addAction(openSettings);
    mRootNode.removeAllChildren();
    mRootNode.getMutableTreeNode().setShowLeafCountEnabled(false);

    PluginTreeNode topicNode = mRootNode.addNode(Localizer.getLocalization(Localizer.I18N_PROGRAMS));
    topicNode.setGroupingByDateEnabled(false);
    PluginTreeNode dateNode = mRootNode.addNode(mLocalizer.msg("days", "Days"));
    dateNode.setGroupingByDateEnabled(true);

    ArrayList<Program> allPrograms = new ArrayList<Program>(1000);
    FavoriteTreeModel.getInstance().updatePluginTree(topicNode, allPrograms);
    HashSet<Program> allProgramsSet = new HashSet<Program>(allPrograms);
    for (Program program : allProgramsSet) {
      dateNode.addProgramWithoutCheck(program);
    }

    mRootNode.update();
    ReminderPlugin.getInstance().updateRootNode(mHasRightToSave);

    if(save && mHasRightToSave) {
      saveFavorites();
    }
    
    if(mMangePanel != null) {
      mMangePanel.handleFavoriteEvent();
    }
  }

  public static ImageIcon getFavoritesIcon(int size) {
    return getIconFromTheme(ICON_CATEGORY, ICON_NAME, size);
  }

  public ProgramReceiveTarget[] getClientPluginTargetIds() {
    ArrayList<ProgramReceiveTarget> list = new ArrayList<ProgramReceiveTarget>(0);
    for (ProgramReceiveTarget target : mClientPluginTargets) {
      ProgramReceiveIf plugin = target.getReceifeIfForIdOfTarget();
      if (plugin != null && plugin.canReceiveProgramsWithTarget()) {
        list.add(target);
      }
    }
    
    ProgramReceiveTarget[] targets = list.toArray(new ProgramReceiveTarget[list.size()]);
    
    if(!MainFrame.isStarting()) {
      mClientPluginTargets = targets;
    }
    
    return targets;
  }

  public void setClientPluginTargets(ProgramReceiveTarget[] clientPluginTargetArr) {
    mClientPluginTargets = clientPluginTargetArr;
    getClientPluginTargetIds();
  }

  public ProgramReceiveTarget[] getDefaultClientPluginsTargets() {
    return getClientPluginTargetIds();
  }

  public static String getFavoritesPluginId() {
    return DATAFILE_PREFIX;
  }

  /**
   * @return The settings of the FavoritesPlugin.
   * @since 2.2.2
   */
  public Properties getSettings() {
    return mSettings;
  }

  /**
   * Adds a AdvancedFavorite to the pending list (for loading filters after TV-Browser start was finished).
   *
   * @param fav The AdvancedFavorite to add.
   * @since 2.5.1
   */
  public void addPendingFavorite(PendingFilterLoader fav) {
    mPendingFavorites.add(fav);
  }

  protected boolean isShowingRepetitions() {
    return mSettings.getProperty("showRepetitions","true").compareTo("true") == 0;
  }

  protected void setShowRepetitions(boolean value) {
    mSettings.setProperty("showRepetitions", String.valueOf(value));
  }

  /**
   * Gets if reminder should be automatically selected for new Favorites
   *
   * @return If the reminder should be selected.
   */
  public boolean isAutoSelectingReminder() {
    return mSettings.getProperty("autoSelectReminder","true").compareTo("true") == 0;
  }

  protected void setAutoSelectingReminder(boolean value) {
    mSettings.setProperty("autoSelectReminder", String.valueOf(value));
  }

  protected int getMarkPriority() {
    if(mMarkPriority == - 2 && mSettings != null) {
      mMarkPriority = Integer.parseInt(mSettings.getProperty("markPriority",String.valueOf(Program.PRIORITY_MARK_MIN)));
      return mMarkPriority;
    } else {
      return mMarkPriority;
    }
  }

  protected void setMarkPriority(int priority) {
    mMarkPriority = priority;

    Favorite[] favoriteArr = FavoriteTreeModel.getInstance().getFavoriteArr();

    for(Favorite favorite: favoriteArr) {
      Program[] programs = favorite.getWhiteListPrograms();

      for(Program program : programs) {
        program.validateMarking();
      }
    }

    mSettings.setProperty("markPriority",String.valueOf(priority));

    saveFavorites();
  }

  public String toString() {
    return getName();
  }

  String getName() {
    return mLocalizer.msg("manageFavorites","Favorites");
  }

  private static class ReceiveTargetItem {
    private ProgramReceiveTarget mTarget;
    private ArrayList<Program> mProgramsList;

    protected ReceiveTargetItem(ProgramReceiveTarget target) {
      mTarget = target;
      mProgramsList = new ArrayList<Program>(0);
    }

    protected void addPrograms(Program[] programs) {
      for(Program p : programs) {
        if(!mProgramsList.contains(p)) {
          mProgramsList.add(p);
        }
      }
    }

    protected ProgramReceiveTarget getReceiveTarget() {
      return mTarget;
    }

    protected Program[] getPrograms() {
      return mProgramsList.toArray(new Program[mProgramsList.size()]);
    }

    public String toString() {
      return mTarget.toString();
    }
  }

  private String getKeyForReceiveTarget(ProgramReceiveTarget target) {
    if(target != null) {
      return target.getReceiveIfId() + "###" + target.getTargetId();
    }

    return null;
  }

  protected void setGlobalExclusions(Exclusion[] exclusions, final boolean onlyAdded) {
    mExclusions = exclusions;

    new Thread("globalFavoriteExclusionRefreshThread") {
      public void run() {
        setPriority(Thread.MIN_PRIORITY);
        for(Favorite fav : FavoriteTreeModel.getInstance().getFavoriteArr()) {
          try {
            if(!onlyAdded) {
              fav.updatePrograms(false);
            }
            else {
              fav.refreshPrograms(true);
            }
          } catch (TvBrowserException e) {
            // ignore
          }
        }
      }
    }.start();
  }

  /**
   * Gets the global exclusions.
   * <p>
   * @return The global exclusions.
   */
  public Exclusion[] getGlobalExclusions() {
    return mExclusions;
  }

  private class UpdateInfoThread extends Thread {
    private Favorite[] mFavorites;

    protected UpdateInfoThread() {
      super("Manage favorites");
      mFavorites = new Favorite[0];
    }

    protected void addFavorites(Favorite[] favArr) {
      ArrayList<Favorite> newFavoriteList = new ArrayList<Favorite>(mFavorites.length + favArr.length);
      Favorite[] newFavorites;

      synchronized (mFavorites) {
        for(Favorite fav : favArr) {
          boolean found = false;

          for(Favorite knownFavorite : mFavorites) {
            if(fav.equals(knownFavorite)) {
              found = true;
              break;
            }
          }

          if(!found) {
            newFavoriteList.add(fav);
          }
        }

        newFavorites = new Favorite[mFavorites.length + newFavoriteList.size()];

        System.arraycopy(mFavorites,0,newFavorites,0,mFavorites.length);
        System.arraycopy(newFavoriteList.toArray(),0,newFavorites,mFavorites.length,newFavoriteList.size());
      }

      mFavorites = newFavorites;
    }

    public void run() {
      if(newFavoritesFound()) {
        synchronized (mFavorites) {
          panel = new ManageFavoritesPanel(mFavorites, getIntegerSetting(mSettings, "splitpanePosition",200), true, null, true);
          
          mInfoPanel = new AfterDataUpdateInfoPanel() {
            @Override
            public void closed() {
              panel.close();
              mInfoPanel = null;
              panel = null;
            }
          };
          mInfoPanel.setLayout(new BorderLayout());
          
          mInfoPanel.add(panel, BorderLayout.CENTER);
        }
      }
    }
    
    
    private boolean newFavoritesFound() {
      for(int i = 0; i < mFavorites.length; i++) {
        if(mFavorites[i] != null && mFavorites[i].getNewPrograms().length > 0) {
          return true;
        }
      }
      
      return false;
    }
  }


  public void addTitleFavorites(Program[] programArr) {
    // filter duplicates in exported programs
    ArrayList<String> allTitles = new ArrayList<String>(programArr.length);
    for (Program program : programArr) {
      allTitles.add(program.getTitle());
    }
    HashSet<String> uniqueTitles = new HashSet<String>(allTitles);
    Favorite[] allFavorites = FavoriteTreeModel.getInstance().getFavoriteArr();
    for (String newTitle : uniqueTitles) {
      // avoid duplicates by checking the titles of existing favorites
      boolean found = false;
      for (Favorite oldFavorite : allFavorites) {
        if (oldFavorite.getName().equalsIgnoreCase(newTitle)) {
          found = true;
          break;
        }
      }
      if (found) {
        continue;
      }
      TitleFavorite favorite = new TitleFavorite(newTitle);
      if (favorite != null) {
        try {
          favorite.updatePrograms();
          FavoriteTreeModel.getInstance().addFavorite(favorite);

          if(ManageFavoritesDialog.getInstance() != null) {
            ManageFavoritesDialog.getInstance().addFavorite(favorite, null);
          }
        } catch (TvBrowserException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    }
    saveFavorites();
  }
  
  public void editSelectedFavorite() {
    if(ManageFavoritesDialog.getInstance() != null && ManageFavoritesDialog.getInstance().isVisible()) {
      ManageFavoritesDialog.getInstance().editSelectedFavorite();
      
      if(mMangePanel != null) {
        mMangePanel.handleFavoriteEvent();
      }
    }
    else if(mMangePanel != null) {
      mMangePanel.editSelectedFavorite();
    }
  }
  
  public void newFavorite(FavoriteNode parent) {
    if(ManageFavoritesDialog.getInstance() != null && ManageFavoritesDialog.getInstance().isVisible()) {
      ManageFavoritesDialog.getInstance().newFavorite(parent);
      
      if(mMangePanel != null) {
        mMangePanel.handleFavoriteEvent();
      }
    }
    else if(mMangePanel != null) {
      mMangePanel.newFavorite(parent);
    }
  }
  
  public void showSendDialog() {
    if(ManageFavoritesDialog.getInstance() != null && ManageFavoritesDialog.getInstance().isVisible()) {
      ManageFavoritesDialog.getInstance().showSendDialog();
      mMangePanel.handleFavoriteEvent();
    }
    else if(mMangePanel != null) {
      mMangePanel.showSendDialog();
    }
  }
  
  public void deleteSelectedFavorite() {
    if(ManageFavoritesDialog.getInstance() != null && ManageFavoritesDialog.getInstance().isVisible()) {
      ManageFavoritesDialog.getInstance().deleteSelectedFavorite();
      
      if(mMangePanel != null) {
        mMangePanel.handleFavoriteEvent();
      }
    }
    else if(mMangePanel != null) {
      mMangePanel.deleteSelectedFavorite();
    }
  }
  
  public boolean programListIsEmpty() {
    if(ManageFavoritesDialog.getInstance() != null && ManageFavoritesDialog.getInstance().isVisible()) {
      return ManageFavoritesDialog.getInstance().programListIsEmpty();
    }
    
    return mMangePanel != null ? mMangePanel.programListIsEmpty() : false;
  }
  
  public boolean isShowingNewFoundPrograms() {
    return panel != null;
  }
  
  public void newFolder(FavoriteNode parent) {
    if(ManageFavoritesDialog.getInstance() != null && ManageFavoritesDialog.getInstance().isVisible()) {
      ManageFavoritesDialog.getInstance().newFolder(parent,ManageFavoritesDialog.getInstance());
    }
    else if(mMangePanel != null) {
      mMangePanel.newFolder(parent, MainFrame.getInstance());
    }
  }
  
  public void favoriteSelectionChanged() {
    if(panel != null) {
      panel.favoriteSelectionChanged();
    }
    
    if(ManageFavoritesDialog.getInstance() != null && ManageFavoritesDialog.getInstance().isVisible()) {
      ManageFavoritesDialog.getInstance().favoriteSelectionChanged();
    }
    else if(mMangePanel != null) {
      mMangePanel.favoriteSelectionChanged();
    }    
  }
  
  public PluginCenterPanelWrapper getPluginCenterPanelWrapper() {
    return mSettings.getProperty("provideTab", "true").equals("true") ? mWrapper : null;
  }
  
  private class FavoritesCenterPanel extends PluginCenterPanel {
    @Override
    public String getName() {
      return FavoritesPlugin.getInstance().getName();
    }

    @Override
    public JPanel getPanel() {
      return mCenterPanel;
    }
  }
  
  AfterDataUpdateInfoPanel getAfterDataUpdateInfoPanel() {
    if(mUpdateThread != null && mUpdateThread.isAlive()) {
      try {
        mUpdateThread.join();
      } catch (InterruptedException e) {}
    }
    if(mUpdateInfoThread != null) {
      if(mUpdateInfoThread.isAlive()) {
        try {
          mUpdateInfoThread.join();
        } catch (InterruptedException e) {}
      }
      
      if(mInfoPanel != null) {
        return mInfoPanel;
      }
    }
    
    return null;
  }
  
  public boolean provideTab() {
    return mSettings.getProperty("provideTab", "true").equals("true");
  }
  
  public void setProvideTab(boolean value) {
    mSettings.put("provideTab", String.valueOf(value));
    addPanel();
  }
  
  public ProgramFieldType[] getDefaultProgramFieldTypeSelection() {
    return mDefaultProgramFieldTypeSelection;
  }
  
  public void setDefaultProgramFieldTypeSelection(ProgramFieldType[] defaultSelection) {
    mDefaultProgramFieldTypeSelection = defaultSelection;
  }
  
  public boolean showDateSeparators() {
    return mSettings.getProperty("showDateSeparators","true").equals("true");
  }
  
  public void setShowDateSeparators(boolean show) {
    mSettings.put("showDateSeparators", String.valueOf(show));
    
    if(mMangePanel != null) {
      mMangePanel.setShowDateSeparators(show);
    }
  }
  
  /**
   * Tries to load channel limitations again.
   */
  public void reValidateChannelLimitation() {
    ((FavoriteNode)FavoriteTreeModel.getInstance().getRoot()).reValidateChannelLimitations();
  }
  
  public FavoriteFilter getFilterForKeyValue(String keyValue) {
    try {
      long key = Long.parseLong(keyValue);
      
      Favorite[] favorites = FavoriteTreeModel.getInstance().getFavoriteArr();
      
      for(Favorite fav : favorites) {
        if(fav.hasFilterForKey(key)) {
          return new FavoriteFilter(fav);
        }
      }
      
    }catch(NumberFormatException e) {}
    
    return null;
  }
  
  public boolean timeButtonsScrollToNextTimeInTab() {
    return mSettings.getProperty("timeButtonsScrollToNextTimeInTab", "true").equals("true");
  }
  
  public void setTimeButtonsScrollToNextTimeInTab(boolean value) {
    mSettings.setProperty("timeButtonsScrollToNextTimeInTab", String.valueOf(value));
  }
  
  public boolean reactOnFilterChange() {
    return mSettings.getProperty("reactOnFilterChange", "true").equals("true");
  }
  
  public void setReactOnFilterChange(boolean value) {
    mSettings.setProperty("reactOnFilterChange", String.valueOf(value));
  }
  
  public int getFilterStartType() {
    return Integer.parseInt(mSettings.getProperty("filterStartType", "0"));
  }
  
  public void setFilterStartType(int type) {
    mSettings.setProperty("filterStartType", String.valueOf(type));
  }
  
  public ProgramFilter getLastSelectedProgramFilter() {
    ProgramFilter test = FilterManagerImpl.getInstance().getAllFilter();
    String name = mSettings.getProperty("lastSelectedProgramFilter", test.getName());
    
    ProgramFilter[] availableFilter = FilterManagerImpl.getInstance().getAvailableFilters();
    
    for(ProgramFilter filter : availableFilter) {
      if(filter != null && filter.getName().equals(name)) {
        test = filter;
        break;
      }
    }
    
    return test;
  }
  
  public boolean isNewProgram(final Program prog) {
    boolean result = false;
    final Program[] progs = mLastFoundPrograms.get();
    
    for(Program p : progs) {
      if(p.equals(prog)) {
        result = true;
        break;
      }
    }
    
    return result;
  }
}