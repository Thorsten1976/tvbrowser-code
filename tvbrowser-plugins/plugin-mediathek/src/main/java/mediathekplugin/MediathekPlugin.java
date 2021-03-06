/*
 * Copyright Michael Keppler
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package mediathekplugin;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;

import util.io.IOUtilities;
import util.ui.html.HTMLTextHelper;
import devplugin.ActionMenu;
import devplugin.Channel;
import devplugin.ContextMenuSeparatorAction;
import devplugin.Date;
import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.PluginTreeNode;
import devplugin.Program;
import devplugin.SettingsTab;
import devplugin.ThemeIcon;
import devplugin.Version;

/**
 * @author Bananeweizen
 *
 */
public class MediathekPlugin extends Plugin {

  private static final boolean IS_STABLE = true;

  private static final Version PLUGIN_VERSION = new Version(3, 3, 0, IS_STABLE);

  /** The localizer used by this class. */
  private static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(MediathekPlugin.class);

  private Icon contextIcon, pluginIconSmall, mIconWeb;

  private MediathekSettings mSettings;

  private PluginTreeNode rootNode = new PluginTreeNode(this, false);

  private static MediathekPlugin instance = null;

  /** The logger for this class */
  private static final Logger logger = Logger.getLogger(MediathekPlugin.class.getName());

  public static Version getVersion() {
    return PLUGIN_VERSION;
  }

  private ArrayList<WebMediathek> mMediatheks;

  private Database mDatabase;
  
  private Timer mMediathekUpdaterTimer = new Timer();
  
  private Thread mMarkThread;

  @Override
  public PluginInfo getInfo() {
    final String name = mLocalizer.msg("name", "Mediathek");
    final String description = mLocalizer.msg("description", "Shows video information for several mediatheks.");
    return new PluginInfo(MediathekPlugin.class, name, description, "Michael Keppler, Peter Heinzig", "GPL 3");
  }

  public MediathekPlugin() {
    rememberInstance(this);
    pluginIconSmall = createImageIcon("actions", "web-search", 16);
    createImageIcon("actions", "web-search", 22);
    contextIcon = createImageIcon("actions", "web-search", 16);
    mIconWeb = createImageIcon("apps", "internet-web-browser", 16);
    rootNode.setGroupingByDateEnabled(false);
  }

  private static void rememberInstance(final MediathekPlugin plugin) {
    instance = plugin;
  }

  @Override
  public ActionMenu getContextMenuActions(final Program program) {
    // pseudo action for example program
    if (program.equals(getPluginManager().getExampleProgram())) {
      return new ActionMenu(new AbstractAction(mLocalizer.msg("name", "Mediathek"), getContextMenuIcon()) {
        private static final long serialVersionUID = 3759118498893676458L;

        public void actionPerformed(final ActionEvent e) {
          // empty
        }
      });
    }
    if (mDatabase != null) {
      ArrayList<MediathekProgramItem> programs = mDatabase.getMediathekPrograms(program);
      Collections.sort(programs);
      return getContextMenu(programs);
    }
    return null;
  }
  
  public ThemeIcon getMarkIconFromTheme() {
    return new ThemeIcon("actions", "web-search", 16);
  }

  private ActionMenu getContextMenu(ArrayList<MediathekProgramItem> programs) {
    if (programs.isEmpty()) {
      return null;
    }
    
    final int entryLimit = 50;
    
    int days = 0;
    int maxPerMonth = 0;
    
    int perMonthCurr = 0;
    
    Date curr = null;
    Date currM = null;
    
    final TreeMap<Date, TreeMap<Date, ArrayList<Action>>> actionmap = new TreeMap<Date,TreeMap<Date,ArrayList<Action>>>();
    TreeMap<Date,ArrayList<Action>> monthactions = new TreeMap<Date,ArrayList<Action>>();
    ArrayList<Action> dayactions = new ArrayList<Action>();
    for (final MediathekProgramItem episode : programs) {
      Date date = episode.getDate();
      Date dateM = getFirstOfMonth(date);
      if ((curr!=null) && (!curr.equals(date))){
        monthactions.put(curr, dayactions);
        if ((currM!=null) && (!currM.equals(dateM))){
          actionmap.put(currM, monthactions);
          monthactions = new TreeMap<Date,ArrayList<Action>>();
          
          maxPerMonth = Math.max(maxPerMonth, perMonthCurr);
          perMonthCurr = 0;
        } else {
          perMonthCurr += dayactions.size();
        }
        dayactions = new ArrayList<Action>();        
        days++;
      }
      curr = date;
      currM = dateM;
      
      dayactions.add(new AbstractAction(episode.getInfoDate(), episode.getIcon()) {
        private static final long serialVersionUID = 3896711485832090278L;

        public void actionPerformed(final ActionEvent e) {
          episode.show();
        }
      });
    }
    monthactions.put(curr, dayactions);
    actionmap.put(currM, monthactions);
    
    boolean useMonths = days>entryLimit;
    boolean useDays = (useMonths?(maxPerMonth):(programs.size()))>entryLimit;
    
    if (useMonths){
      final ArrayList<ActionMenu> actionMonths = new ArrayList<ActionMenu>();
      if (useDays){
        for(final Entry<Date,TreeMap<Date,ArrayList<Action>>> eMonth: actionmap.entrySet()){
          ArrayList<ActionMenu> actionListSubMenu = new ArrayList<ActionMenu>();
          for(final Entry<Date,ArrayList<Action>> eDate: eMonth.getValue().entrySet()){
            actionListSubMenu.add(new ActionMenu(getDay(eDate.getKey()),eDate.getValue().toArray(new Action[eDate.getValue().size()])));
          }
          actionMonths.add(new ActionMenu(getMonthOnly(eMonth.getKey()),actionListSubMenu.toArray(new ActionMenu[actionListSubMenu.size()])));
        }
      } else {
        for(final Entry<Date,TreeMap<Date,ArrayList<Action>>> eMonth: actionmap.entrySet()){
          ArrayList<Action> actionListSub = new ArrayList<Action>();
          for(final Entry<Date,ArrayList<Action>> eDate: eMonth.getValue().entrySet()){
            actionListSub.addAll(eDate.getValue());
          }
          actionMonths.add(new ActionMenu(getMonthOnly(eMonth.getKey()),actionListSub.toArray(new Action[actionListSub.size()])));        
        }
      } 
      return new ActionMenu(mLocalizer.msg("context", "Episodes in the Mediathek {0}", programs.size()), contextIcon, actionMonths.toArray(new ActionMenu[actionMonths.size()]));
    } else {
      if (useDays){
        final ArrayList<ActionMenu> actionDays = new ArrayList<ActionMenu>();
        for(final Entry<Date,TreeMap<Date,ArrayList<Action>>> eMonth: actionmap.entrySet()){
          for(final Entry<Date,ArrayList<Action>> eDate: eMonth.getValue().entrySet()){
            actionDays.add(new ActionMenu(getDay(eDate.getKey()),eDate.getValue().toArray(new Action[eDate.getValue().size()])));
          }
          
        }
        return new ActionMenu(mLocalizer.msg("context", "Episodes in the Mediathek {0}", programs.size()), contextIcon, actionDays.toArray(new ActionMenu[actionDays.size()]));
      } else {
        final ArrayList<Action> actions = new ArrayList<Action>();
        for(final Entry<Date,TreeMap<Date,ArrayList<Action>>> eMonth: actionmap.entrySet()){
          for(final Entry<Date,ArrayList<Action>> eDate: eMonth.getValue().entrySet()){
            actions.addAll(eDate.getValue());
          }
        }
        return new ActionMenu(mLocalizer.msg("context", "Episodes in the Mediathek {0}", programs.size()), contextIcon, actions.toArray(new Action[actions.size()]));        
      }    
    }
    
    
    
   
  }
  
  private Date getFirstOfMonth(Date date){
    Calendar cal = date.getCalendar();
    cal.set(Calendar.DAY_OF_MONTH, 1);
    return new Date(cal);
  }
  
  private String getDay(Date date) {
    return date.getFormattedString("EE, dd.MMM yy");
  }
  
  private String getMonthOnly(Date date) {
    return date.getFormattedString("MMMMM yy");
  }

  private void setMediathekUpdateInterval(int minutes){
    mMediathekUpdaterTimer.cancel();
    if (minutes>0){
      mMediathekUpdaterTimer = new Timer();
      mMediathekUpdaterTimer.schedule(new TimerTask(){    
        @Override
        public void run(){
          startMediathekUpdate();
        }
      }, minutes*60000, minutes*60000); // 60s -> 60000ms
    }
  }

  public void startMediathekUpdate(){
    String path =  mSettings.getMediathekProgramPath();
    File mediathekProgram = new File(path);
    if (!mediathekProgram.exists()) {
      logger.warning("missing mediathek program at " + path);
      return;
    }
    try {
      ProcessBuilder pb = new ProcessBuilder("java", "-Xmx512m", "-Xms64M", "-Djava.awt.headless=true", "-jar", mSettings.getMediathekProgramPath(), "-auto");
      Process p = pb.start();
      BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
      String s = "";
      try {
        while((s = in.readLine()) != null){
            logger.info(s);
        }
        logger.info("Finished MediathekView Update");
      } catch (IOException e) {
        logger.warning("MediathekView Update error");
        e.printStackTrace();
      }
    } catch (IOException e) {
      e.printStackTrace();
    } 
  }
  
  protected Icon getContextMenuIcon() {
    return contextIcon;
  }

  @Override
  public ActionMenu getButtonAction() {
    final ArrayList<Object> subscribedList = new ArrayList<Object>(50);
    final ArrayList<Action> remainingList = new ArrayList<Action>(50);

    for (WebMediathek mediathek : getMediatheks()) {
      boolean subscribed = false;
      for (Channel channel : getPluginManager().getSubscribedChannels()) {
        if (mediathek.acceptsChannel(channel)) {
          subscribedList.add(setActionDescription(mediathek.getAction(true)));
          subscribed = true;
          break;
        }
      }
      if (!subscribed) {
        remainingList.add(setActionDescription(mediathek.getAction(true)));
      }
    }
    if (!remainingList.isEmpty()) {
      subscribedList.add(ContextMenuSeparatorAction.getInstance());
      subscribedList.add(new ActionMenu(mLocalizer.msg("notSubscribed", "Not subscribed channels"),remainingList.toArray()));
    }
    return new ActionMenu("Mediathek", pluginIconSmall, subscribedList.toArray());
  }

  private Action setActionDescription(AbstractAction action) {
    action.putValue(Action.SHORT_DESCRIPTION, action.getValue(Action.NAME));
    action.putValue(Action.LONG_DESCRIPTION, action.getValue(Action.NAME));
    return action;
  }

  private ArrayList<WebMediathek> getMediatheks() {
    if (mMediatheks == null) {
      mMediatheks = new ArrayList<WebMediathek>();
      try {
        final InputStream stream = getClass().getResourceAsStream("urls.txt");
        if (stream != null) {
          BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
          String separator = "";
          while (separator != null) {
            String title = reader.readLine();
            String regex = reader.readLine();
            String url = reader.readLine();
            separator = reader.readLine();
            mMediatheks.add(new WebMediathek(title, regex, url));
          }
          reader.close();
        }
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    return mMediatheks;
  }

  public static MediathekPlugin getInstance() {
    return instance;
  }

  @Override
  public SettingsTab getSettingsTab() {
    return new MediathekSettingsTab(mSettings);
  }

  @Override
  public void handleTvBrowserStartFinished() {
    mDatabase = Database.getInstance();
    setMediathekUpdateInterval(mSettings.getMediathekUpdateInterval());
  }

  @Override
  public void loadSettings(final Properties properties) {
    mSettings = new MediathekSettings(properties);
  }

  @Override
  public Properties storeSettings() {
    return mSettings.storeSettings();
  }
  
  @Override
  public void handleTvDataUpdateFinished() {
    updatePluginTree();
  }

  void settingsChanged() {
    if (mDatabase!=null) {
      mDatabase.settingsChanged();
      setMediathekUpdateInterval(mSettings.getMediathekUpdateInterval());
    }
  }

  public String convertHTML(final String html) {
    String result = HTMLTextHelper.convertHtmlToText(html);
    result = IOUtilities.replace(result, "&amp;", "&");
    return result;
  }

  @Override
  public boolean canUseProgramTree() {
    return true;
  }

  @Override
  public PluginTreeNode getRootNode() {
    return rootNode;
  }

  /**
   * Updates the plugin tree.
   */
  void updatePluginTree() {
    if(mMarkThread == null || !mMarkThread.isAlive()) {
      mMarkThread = new Thread("Mark mediathek programs") {
        public void run() {
          logger.info("Started Mediathek Tree-Update");
          final PluginTreeNode node = getRootNode();
          node.removeAllActions();
          node.removeAllChildren();
          node.getMutableTreeNode().setShowLeafCountEnabled(false);
          node.setGroupingByDateEnabled(false);
          node.setGroupingByWeekEnabled(false);
          
          ArrayList<Channel> channels = new ArrayList<Channel>();
          
          for (WebMediathek mediathek : getMediatheks()) {
            for (Channel channel : getPluginManager().getSubscribedChannels()) {
              if (mediathek.acceptsChannel(channel)) {
                channels.add(channel);
              }
            }
          }
                    
          boolean found = false;
          int dateOffset = -1;
          Date today = Date.getCurrentDate();
          
          do {
            found = false;

            Date currentDate = today.addDays(dateOffset);
            
            PluginTreeNode addNode = null;
            
            for(Channel ch : channels) {
              Iterator<Program> channelDayProgram = getPluginManager().getChannelDayProgram(currentDate, ch);
              
              PluginTreeNode channelNode = null;
              
              if(channelDayProgram != null && channelDayProgram.hasNext()) {
                found = true;
                
                while(channelDayProgram.hasNext()) {
                  Program p = channelDayProgram.next();
                  
                  if(!mDatabase.getMediathekPrograms(p).isEmpty()) {
                    if(addNode == null) {
                      addNode = node.addNode(currentDate.getShortDayLongMonthString());
                      addNode.setGroupingByDateEnabled(false);
                      addNode.setGroupingByWeekEnabled(false);
                    }
                    if(channelNode == null) {
                      channelNode = addNode.addNode(ch.getName());
                      channelNode.setGroupingByDateEnabled(false);
                      channelNode.setGroupingByWeekEnabled(false);
                    }
                    
                    channelNode.addProgram(p);
                    p.mark(MediathekPlugin.this);
                  }
                }
              }
            }
            
            dateOffset++;
          }while(found);
          
          node.update();

          logger.info("Finished Mediathek Tree-Update");
        }

      };
      mMarkThread.start();
      
    }
    else {
      if(mMarkThread.isAlive()) {
        try {
          mMarkThread.join();
          updatePluginTree();
        } catch (InterruptedException e) {
          // ignore
        }
      }
    }

  }

  public MediathekSettings getSettings() {
    return mSettings;
  }

  public Logger getLogger() {
    return logger;
  }

  public Icon getPluginIcon() {
    return pluginIconSmall;
  }

  public Icon getWebIcon() {
    return mIconWeb;
  }

  protected Frame getFrame() {
    return this.getParentFrame();
  }

  @Override
  public ActionMenu getContextMenuActions(final Channel channel) {
    ArrayList<Action> actions = new ArrayList<Action>();
    for (WebMediathek mediathek : getMediatheks()) {
      if (mediathek.acceptsChannel(channel)) {
        actions.add(mediathek.getAction(false));
      }
    }
    if (actions.isEmpty()) {
      return null;
    }
    if (actions.size() == 1) {
      return new ActionMenu(actions.get(0));
    }
    return new ActionMenu("Mediathek", pluginIconSmall, actions.toArray());
  }

  public Icon getImageIcon(String fileName) {
    return createImageIcon(fileName);
  }
}