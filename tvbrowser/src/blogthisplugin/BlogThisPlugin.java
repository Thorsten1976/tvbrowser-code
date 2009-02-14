/*
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
package blogthisplugin;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Properties;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import util.browserlauncher.Launch;
import util.paramhandler.ParamParser;
import util.program.AbstractPluginProgramFormating;
import util.program.LocalPluginProgramFormating;
import util.ui.Localizer;
import devplugin.ActionMenu;
import devplugin.ContextMenuAction;
import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.Program;
import devplugin.SettingsTab;
import devplugin.ThemeIcon;
import devplugin.Version;

/**
 * The Main-Class for the Blog-Plugin
 * 
 * @author bodum
 */
public class BlogThisPlugin extends Plugin {
  private static final Version mVersion = new Version(2,60);
  
    /** Translator */
    private static final Localizer mLocalizer = Localizer
            .getLocalizerFor(BlogThisPlugin.class);

    /** Default URLs */
    public static final String URL_WORDPRESS = "http://yoursite.com/wordpress/wp-admin/bookmarklet.php";

    public static final String URL_B2EVOLUTION = "http://yourblog.com/admin.php";

    /** Service Names */
    public static final String BLOGGER = "BLOGGER";

    public static final String WORDPRESS = "WORDPRESS";

    public static final String B2EVOLUTION = "B2EVOLUTION";
    
    private static LocalPluginProgramFormating DEFAULT_CONFIG = new LocalPluginProgramFormating("blogDefault", mLocalizer.msg("defaultName","BlogThisPlugin - Default"),"{title} ({channel_name})","{channel_name} - {title}\n{leadingZero(start_day,\"2\")}.{leadingZero(start_month,\"2\")}.{start_year} {leadingZero(start_hour,\"2\")}:{leadingZero(start_minute,\"2\")}-{leadingZero(end_hour,\"2\")}:{leadingZero(end_minute,\"2\")}\n\n{splitAt(short_info,\"78\")}\n\n","UTF-8");

    private AbstractPluginProgramFormating[] mConfigs = null;
    private LocalPluginProgramFormating[] mLocalFormatings = null;
    
    /** Settings */
    private Properties mSettings;

    private PluginInfo mPluginInfo;
    
    public BlogThisPlugin() {
      createDefaultConfig();
      createDefaultAvailable();
    }
    
    private void createDefaultConfig() {
      mConfigs = new AbstractPluginProgramFormating[1];
      mConfigs[0] = DEFAULT_CONFIG;
    }
    
    private void createDefaultAvailable() {
      mLocalFormatings = new LocalPluginProgramFormating[1];
      mLocalFormatings[0] = DEFAULT_CONFIG;        
    }
    
    public static Version getVersion() {
      return mVersion;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see devplugin.Plugin#getInfo()
     */
    public PluginInfo getInfo() {
      if(mPluginInfo == null) {
        String name = mLocalizer.msg("pluginName", "BlogThis");
        String desc = mLocalizer.msg("description", "Creates a new Blog-Entry");
        String author = "Bodo Tasche";
        
        mPluginInfo = new PluginInfo(BlogThisPlugin.class, name, desc, author);
      }
      
      return mPluginInfo;
    }

    /*
     * (non-Javadoc)
     * 
     * @see devplugin.Plugin#getContextMenuActions(devplugin.Program)
     */
    public ActionMenu getContextMenuActions(final Program program) {
      ImageIcon img = createImageIcon("apps","internet-web-browser", 16);

      if(mConfigs.length > 1) {
        ContextMenuAction blog = new ContextMenuAction(mLocalizer.msg("contextMenuText",
        "Create a new Blog-Entry") + "...");
        
        ArrayList<AbstractAction>list = new ArrayList<AbstractAction>();

        for (final AbstractPluginProgramFormating config : mConfigs) {
          if (config != null && config.isValid())
            list.add(new AbstractAction(config.getName()) {
              public void actionPerformed(ActionEvent e) {
                blogThis(program, config);
              }
            });
        }
      
        blog.putValue(Action.SMALL_ICON, img);

        return new ActionMenu(blog, list.toArray(new AbstractAction[list.size()]));
      }
      else {
        AbstractAction blog = new AbstractAction(mLocalizer.msg("contextMenuText",
        "Create a new Blog-Entry")) {
          public void actionPerformed(ActionEvent evt) {            
            blogThis(program,mConfigs.length != 1 ? DEFAULT_CONFIG : mConfigs[0]);
          }
        };
        
        blog.putValue(Action.SMALL_ICON, img);
        
        return new ActionMenu(blog);
      }
    }

    /**
     * Creates a new Blog-Entry
     * 
     * @param program Program to use for the Entry
     */
    private void blogThis(Program program, AbstractPluginProgramFormating formating) {

        if (mSettings.getProperty("BlogService") == null) {
            
            int ret = JOptionPane.showConfirmDialog(getParentFrame(),
                mLocalizer.msg("configure", "This Plugin must be configured before first use. Do you want to do this now?"), 
                mLocalizer.msg("notConfigured", "Not configured yet"),
                JOptionPane.YES_NO_OPTION);
            
            if (ret == JOptionPane.YES_OPTION) {
              getPluginManager().showSettings(this);
            }
            
            return;
        }
    
        ParamParser parser = new ParamParser();

        String title = parser.analyse(formating.getTitleValue(), program);
        String content = parser.analyse(formating.getContentValue(), program);
        String url = program.getChannel().getWebpage();
               
        try {
            Launch.openURL(urlFactory(title, content, url));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates the URL that should open in the Web-Browser 
     * @param title Title to show
     * @param content Content to show
     * @param url URL of the Channel
     * @return URL for the Web-Browser
     *
     * @throws UnsupportedEncodingException Problems with the selected Encoding
     */
    private String urlFactory(String title, String content, String url) throws UnsupportedEncodingException{
      if (mSettings.getProperty("BlogService", "").equals(BLOGGER)) {
        StringBuffer toUrl = new StringBuffer("http://www.blogger.com/blog_this.pyra?");
        
        toUrl.append("n=").append(URLEncoder.encode(title, "UTF-8"));
        toUrl.append("&t=").append(URLEncoder.encode(content.trim(), "UTF-8"));
        toUrl.append("&u=").append(URLEncoder.encode(url, "UTF-8"));
        toUrl.append("&sourceid=").append(URLEncoder.encode("TV-Browser", "UTF-8"));
        
        return toUrl.toString();
      } else if (mSettings.getProperty("BlogService", "").equals(WORDPRESS)) {
        StringBuffer toUrl = new StringBuffer(mSettings.getProperty("BlogUrl", URL_WORDPRESS));
        
        toUrl.append("?popuptitle=").append(URLEncoder.encode(title, "UTF-8"));
        toUrl.append("&text=").append(URLEncoder.encode(content, "UTF-8"));
        toUrl.append("&popupurl=").append(URLEncoder.encode(url, "UTF-8"));
        toUrl.append("&sourceid=").append(URLEncoder.encode("TV-Browser", "UTF-8"));
        
        return toUrl.toString();
      } else if (mSettings.getProperty("BlogService", "").equals(B2EVOLUTION)) {        
        StringBuffer toUrl = new StringBuffer(mSettings.getProperty("BlogUrl", URL_B2EVOLUTION));
        toUrl.append("?ctrl=items&action=new&mode=bookmarklet");

        toUrl.append("&post_title=").append(URLEncoder.encode(title, "ISO-8859-1"));
        toUrl.append("&content=").append(URLEncoder.encode(content, "ISO-8859-1"));
        toUrl.append("&post_url=").append(URLEncoder.encode(url, "ISO-8859-1"));
        toUrl.append("&sourceid=").append(URLEncoder.encode("TV-Browser", "ISO-8859-1"));
        
        return toUrl.toString();
      }
      
      return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see devplugin.Plugin#getSettingsTab()
     */
    public SettingsTab getSettingsTab() {
        return new BlogSettingsTab(this, mSettings);
    }

    /*
     * (non-Javadoc)
     * 
     * @see devplugin.Plugin#loadSettings(java.util.Properties)
     */
    public void loadSettings(Properties settings) {
        mSettings = settings;
        
        if(settings != null && settings.containsKey("Content")) {
          mConfigs = new AbstractPluginProgramFormating[1];
          mConfigs[0] = new LocalPluginProgramFormating(mLocalizer.msg("defaultName","BlogThisPlugin - Default"),settings.getProperty("Title",DEFAULT_CONFIG.getTitleValue()),settings.getProperty("Content",DEFAULT_CONFIG.getContentValue()),"UTF-8");
          mLocalFormatings = new LocalPluginProgramFormating[1];
          mLocalFormatings[0] = (LocalPluginProgramFormating)mConfigs[0];
          DEFAULT_CONFIG = mLocalFormatings[0];
          
          settings.remove("Title");
          settings.remove("Content");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see devplugin.Plugin#storeSettings()
     */
    public Properties storeSettings() {
        return mSettings;
    }


    /*
     * (non-Javadoc)
     * @see devplugin.Plugin#getMarkIconFromTheme()
     */
    @Override
    public ThemeIcon getMarkIconFromTheme() {
      return new ThemeIcon("apps", "internet-web-browser", 16);
    }
    
    public void writeData(ObjectOutputStream out) throws IOException {
      out.writeInt(1); // write version
      
      if(mConfigs != null) {
        ArrayList<AbstractPluginProgramFormating> list = new ArrayList<AbstractPluginProgramFormating>();
        
        for(AbstractPluginProgramFormating config : mConfigs)
          if(config != null)
            list.add(config);
        
        out.writeInt(list.size());
        
        for(AbstractPluginProgramFormating config : list)
          config.writeData(out);
      }
      else
        out.writeInt(0);
      
      if(mLocalFormatings != null) {
        ArrayList<AbstractPluginProgramFormating> list = new ArrayList<AbstractPluginProgramFormating>();
        
        for(AbstractPluginProgramFormating config : mLocalFormatings)
          if(config != null)
            list.add(config);
        
        out.writeInt(list.size());
        
        for(AbstractPluginProgramFormating config : list)
          config.writeData(out);      
      }
      
    }
    
    public void readData(ObjectInputStream in) throws IOException, ClassNotFoundException {
      try {
        in.readInt();
      
        int n = in.readInt();
      
        ArrayList<AbstractPluginProgramFormating> list = new ArrayList<AbstractPluginProgramFormating>();
          
        for(int i = 0; i < n; i++) {
          AbstractPluginProgramFormating value = AbstractPluginProgramFormating.readData(in);
        
          if(value != null) { 
            if(value.equals(DEFAULT_CONFIG))
              DEFAULT_CONFIG = (LocalPluginProgramFormating)value;
          
            list.add(value);
          }
        }
      
        mConfigs = list.toArray(new AbstractPluginProgramFormating[list.size()]);
      
        mLocalFormatings = new LocalPluginProgramFormating[in.readInt()];
      
        for(int i = 0; i < mLocalFormatings.length; i++) {
          LocalPluginProgramFormating value = (LocalPluginProgramFormating)LocalPluginProgramFormating.readData(in);
          LocalPluginProgramFormating loadedInstance = getInstanceOfFormatingFromSelected(value);
        
          mLocalFormatings[i] = loadedInstance == null ? value : loadedInstance;
        }
      }catch(Exception e) {
        // Empty
      }
    }
    
    private LocalPluginProgramFormating getInstanceOfFormatingFromSelected(LocalPluginProgramFormating value) {
      for(AbstractPluginProgramFormating config : mConfigs)
        if(config.equals(value))
          return (LocalPluginProgramFormating)config;
      
      return null;
    }
    
    protected static LocalPluginProgramFormating getDefaultFormating() {    
      return new LocalPluginProgramFormating(mLocalizer.msg("defaultName","BlogThisPlugin - Default"),"{title} ({channel_name})","<blockquote><strong>{title}</strong>\n\n<em>{leadingZero(start_day,\"2\")}.{leadingZero(start_month,\"2\")}.{start_year} {leadingZero(start_hour,\"2\")}:{leadingZero(start_minute,\"2\")}-{leadingZero(end_hour,\"2\")}:{leadingZero(end_minute,\"2\")} {channel_name}</em>\n\n{short_info}</blockquote>\n","UTF-8");
    }

    protected LocalPluginProgramFormating[] getAvailableLocalPluginProgramFormatings() {
      return mLocalFormatings;
    }
    
    protected void setAvailableLocalPluginProgramFormatings(LocalPluginProgramFormating[] value) {
      if(value == null || value.length < 1)
        createDefaultAvailable();
      else
        mLocalFormatings = value;
    }

    protected AbstractPluginProgramFormating[] getSelectedPluginProgramFormatings() {
      return mConfigs;
    }
    
    protected void setSelectedPluginProgramFormatings(AbstractPluginProgramFormating[] value) {
      if(value == null || value.length < 1)
        createDefaultConfig();
      else
        mConfigs = value;
    }
}