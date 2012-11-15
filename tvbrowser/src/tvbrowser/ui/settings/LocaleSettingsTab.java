/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (darras@users.sourcceforge.net)
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

package tvbrowser.ui.settings;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;
import java.util.TimeZone;

import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JRootPane;
import javax.swing.JTextArea;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import tvbrowser.core.PluginLoader;
import tvbrowser.core.Settings;
import tvbrowser.core.icontheme.IconLoader;
import tvbrowser.ui.mainframe.MainFrame;
import util.io.IOUtilities;
import util.ui.CustomComboBoxRenderer;
import util.ui.Localizer;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;
import util.ui.customizableitems.SelectableItemList;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.DefaultComponentFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Locale Settings
 */
public class LocaleSettingsTab implements devplugin.SettingsTab {
  /** The localizer for this class. */
  private static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(LocaleSettingsTab.class);
  private static final Logger mLog =  Logger.getLogger(LocaleSettingsTab.class.getName());
  
  private JPanel mSettingsPn;

  private JComboBox mLanguageCB, mTimezoneCB;

  private JLabel mTimezoneLB;

  private JRadioButton mTwelveHourFormat;

  private JRadioButton mTwentyfourHourFormat;

  private JTextArea mInfoArea;

  private static boolean mSomethingChanged = false;

  private static int mStartLanguageIndex;
  private static int mStartTimeZoneIndex;
  private static boolean mTwelveHourFormatIsSelected;

  /**
   * Creates a new instance of ProxySettingsTab.
   */
  public LocaleSettingsTab() {
  }

  /**
   * Creates the settings panel for this tab.
   */
  public JPanel createSettingsPanel() {
    mSettingsPn = new JPanel(new FormLayout("5dlu, pref, 3dlu, default, 5dlu, default, fill:3dlu:grow, 3dlu",
        "pref, 5dlu, pref, 10dlu, pref, 5dlu, pref, 10dlu, pref, 5dlu, pref, 2dlu, pref, fill:3dlu:grow, pref"));
    mSettingsPn.setBorder(Borders.DIALOG_BORDER);

    CellConstraints cc = new CellConstraints();

    mSettingsPn.add(DefaultComponentFactory.getInstance().createSeparator(mLocalizer.msg("titleLanguage", "Locale")), cc.xyw(1,1,7));

    mSettingsPn.add(new JLabel(mLocalizer.msg("language", "Language:")), cc.xy(2,3));
    Locale[] allLocales = mLocalizer.getAllAvailableLocales();
    ArrayList<Locale> localesList = new ArrayList<Locale>(Arrays.asList(allLocales));
    mSettingsPn.add(mLanguageCB = new JComboBox(allLocales), cc.xy(4,3));

    mLanguageCB.setRenderer(new CustomComboBoxRenderer(mLanguageCB.getRenderer()) {
      @Override
      public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        return getBackendRenderer().getListCellRendererComponent(list, ((Locale)value).getDisplayName(), index, isSelected, cellHasFocus);
      }
    });
    
    JButton downloadLanguages = new JButton(mLocalizer.msg("downloadLanguages", "Install additional languages"));
    downloadLanguages.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        downloadAdditionalLanguages();
      }
    });
    
    mSettingsPn.add(downloadLanguages, cc.xy(6,3));

    String language = Settings.propLanguage.getString();
    String country = Settings.propCountry.getString();
    String variant = Settings.propVariant.getString();

    Locale loc = new Locale(language, country, variant);
    if (localesList.contains(loc)) {
      mLanguageCB.setSelectedItem(loc);
    }
    else {
      loc = new Locale(language, country);
      if (localesList.contains(loc)) {
        mLanguageCB.setSelectedItem(loc);
      }
      else {
        loc = new Locale(language);
        if (localesList.contains(loc)) {
          mLanguageCB.setSelectedItem(loc);
        }
      }
    }

    // time zone data may not be accessible, therefore use try-catch everywhere
    String[] zoneIds = new String[0];
    try {
      zoneIds = TimeZone.getAvailableIDs();
      
      Arrays.sort(zoneIds);
    } catch (Exception e) {
        zoneIds = new String[24];
        zoneIds[12] = "GMT+0";
      for(int i=0; i < 12; i++) {
        zoneIds[i] = "GMT-"+Math.abs(i-12);
      }
        
      for(int i=1; i < 12; i++) {
        zoneIds[i+12] = "GMT+"+i;
      }
      
      mLog.log(Level.INFO, "TimeZone IDs not available, use default values", e);
    }
    
    mTimezoneCB = new JComboBox(zoneIds);
    String zone = Settings.propTimezone.getString();
    if (zone == null) {
      try {
        zone = TimeZone.getDefault().getID();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    for (int i = 0; i < zoneIds.length; i++) {
      if (zoneIds[i].equals(zone)) {
        mTimezoneCB.setSelectedIndex(i);
        break;
      }
    }

    mTimezoneLB = new JLabel(mLocalizer.msg("timezone", "Timezone:"));

    mSettingsPn.add(DefaultComponentFactory.getInstance().createSeparator(mLocalizer.msg("titleTimezone", "Locale")), cc.xyw(1,5,7));

    mSettingsPn.add(mTimezoneLB, cc.xy(2,7));
    mSettingsPn.add(mTimezoneCB, cc.xyw(4,7,3));

    mSettingsPn.add(DefaultComponentFactory.getInstance().createSeparator(mLocalizer.msg("titleTimeFormat", "Time format")), cc.xyw(1,9,7));

    mSettingsPn.add(new JLabel(mLocalizer.msg("timeFormat", "Time format:")), cc.xy(2,11));

    mTwentyfourHourFormat = new JRadioButton(mLocalizer.msg("twentyFour", "24 hour format"));
    mTwelveHourFormat = new JRadioButton(mLocalizer.msg("twelve", "12 hour format"));
    ButtonGroup group = new ButtonGroup();
    group.add(mTwentyfourHourFormat);
    group.add(mTwelveHourFormat);

    mSettingsPn.add(mTwentyfourHourFormat, cc.xy(4, 11));
    mSettingsPn.add(mTwelveHourFormat, cc.xy(4, 13));

    if (Settings.propTwelveHourFormat.getBoolean()) {
      mTwelveHourFormat.setSelected(true);
    } else {
      mTwentyfourHourFormat.setSelected(true);
    }

    mInfoArea = UiUtilities.createHelpTextArea(mLocalizer.msg("restartNote", "Please Restart"));
    mInfoArea.setForeground(Color.RED);
    mInfoArea.setVisible(mSomethingChanged);

    if(!mSomethingChanged) {
      mStartLanguageIndex = mLanguageCB.getSelectedIndex();
      mStartTimeZoneIndex = mTimezoneCB.getSelectedIndex();
      mTwelveHourFormatIsSelected = mTwelveHourFormat.isSelected();
    }

    ItemListener itemListener= new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        mInfoArea.setVisible(mLanguageCB.getSelectedIndex() != mStartLanguageIndex ||
            mTimezoneCB.getSelectedIndex() != mStartTimeZoneIndex ||
            (mTwelveHourFormatIsSelected && !mTwelveHourFormat.isSelected() ||
                !mTwelveHourFormatIsSelected && !mTwentyfourHourFormat.isSelected()));
      }
    };

    mLanguageCB.addItemListener(itemListener);
    mTimezoneCB.addItemListener(itemListener);
    mTwelveHourFormat.addItemListener(itemListener);
    mTwentyfourHourFormat.addItemListener(itemListener);

    mSettingsPn.add(mInfoArea, cc.xyw(1, 15, 7));

    return mSettingsPn;
  }


  /**
   * Called by the host-application, if the user wants to save the settings.
   */
  public void saveSettings() {
    Locale loc = (Locale) mLanguageCB.getSelectedItem();

    Settings.propLanguage.setString(loc.getLanguage());
    Settings.propCountry.setString(loc.getCountry());
    Settings.propVariant.setString(loc.getVariant());

    try {
      Settings.propTimezone.setString((String) mTimezoneCB.getSelectedItem());
    } catch (Exception e) {
      e.printStackTrace();
    }

    Settings.propTwelveHourFormat.setBoolean(mTwelveHourFormat.isSelected());

    mSomethingChanged = mInfoArea.isVisible();

    // remove all plugin proxies as their cached plugin description needs to adapt to the new locale
    if (mSomethingChanged) {
      PluginLoader.getInstance().deleteAllPluginProxies();
    }
  }

  /**
   * Returns the name of the tab-sheet.
   */
  public Icon getIcon() {
    return IconLoader.getInstance().getIconFromTheme("apps", "preferences-desktop-locale", 16);
  }

  /**
   * Returns the title of the tab-sheet.
   */
  public String getTitle() {
    return mLocalizer.msg("locale", "Locale");
  }

  private void downloadAdditionalLanguages() {
    int option = JOptionPane.showConfirmDialog(mSettingsPn, mLocalizer.msg("downloadInfo", "TV-Browser will try to find additional languages, therefor an internet connection is needed.\nDo you wish to proceed?"));
    
    if(option == JOptionPane.YES_OPTION) {
     ArrayList<LocaleLink> availableLocales = new ArrayList<LocaleLink>();
      
      try {
        String siteText = new String(IOUtilities.loadFileFromHttpServer(new URL("http://www.tvbrowser.org/downloads/lang/index.php")));
        
        Pattern p = Pattern.compile("<li><a href=\"([^\"]*)\">([^<]*)</a></li>");
        
        Matcher matcher = p.matcher(siteText);
        
        int pos = 0;
        
        while(matcher.find(pos)) {
          String link = matcher.group(1);
          String lang = matcher.group(2);
          
          Locale loc = Localizer.getLocaleForString(lang);
          
          boolean add = true;
          
          for(int i = 0; i < mLanguageCB.getItemCount(); i++) {
            if(loc.equals(mLanguageCB.getItemAt(i))) {
              add = false;
              break;
            }
          }
          
          if(add) {
            availableLocales.add(new LocaleLink(loc, link));
          }
          
          pos = matcher.end();
        }
      } catch (Exception e) {e.printStackTrace();}
      
      if(availableLocales.isEmpty()) {
        JOptionPane.showMessageDialog(mSettingsPn, mLocalizer.msg("noAdditionalLang", "No additional languages available."));
      }
      else {
        showLanguageDownloadDialog(availableLocales);
      }
    }
  }
  
  private void showLanguageDownloadDialog(ArrayList<LocaleLink> availableLocales) {
    Collections.sort(availableLocales, new Comparator<LocaleLink>() {
      @Override
      public int compare(LocaleLink o1, LocaleLink o2) {
        return o1.toString().compareTo(o2.toString());
      }
    });
    
    FormLayout layout = new FormLayout("default:grow,default,5dlu,default","default,3dlu,fill:default:grow,5dlu,default");
    
    PanelBuilder pb = new PanelBuilder(layout);
    pb.setDefaultDialogBorder();
    
    final JDialog dialog = new JDialog(UiUtilities.getLastModalChildOf(MainFrame.getInstance()));
    dialog.setTitle(mLocalizer.msg("downloadLanguages", "Install additional languages"));
    dialog.setContentPane(pb.getPanel());
    
    CellConstraints cc = new CellConstraints();
    
    final SelectableItemList list = new SelectableItemList(new LocaleLink[0], availableLocales.toArray(new LocaleLink[availableLocales.size()]));
    
    pb.addLabel(mLocalizer.msg("additionalLanguagesFound", "The following additional languages were found:"), cc.xyw(1,1,4));
    pb.add(list, cc.xyw(1,3,4));
    
    final JButton download = new JButton(mLocalizer.msg("downloadSelectedLanguages", "Download selected languages"));
    download.setEnabled(false);
    download.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        for(Object o : list.getSelection()) {
          LocaleLink localeLink = (LocaleLink)o;
          
          if(localeLink.download()) {
            mLanguageCB.addItem(localeLink.getLocale());
          }
        }
        dialog.dispose();
      }
    });
    
    JButton close = new JButton(Localizer.getLocalization(Localizer.I18N_CLOSE));
    close.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        dialog.dispose();
      }
    });
    
    pb.add(download, cc.xy(2,5));
    pb.add(close, cc.xy(4,5));
    
    WindowClosingIf windowClosing = new WindowClosingIf() {
      @Override
      public JRootPane getRootPane() {
        return dialog.getRootPane();
      }
      
      @Override
      public void close() {
        dialog.dispose();
      }
    };
    
    list.addListSelectionListener(new ListSelectionListener() {
      @Override
      public void valueChanged(ListSelectionEvent e) {
        if(!e.getValueIsAdjusting()) {
          download.setEnabled(list.getSelection().length > 0);
        }
      }
    });
    
    UiUtilities.registerForClosing(windowClosing);
    
    Settings.layoutWindow("languageDownloadDialog", dialog, new Dimension(400,300));
    dialog.setVisible(true);
  }
  
  private class LocaleLink {
    private Locale mLocale;
    private String mLink;
    
    public LocaleLink(Locale locale, String link) {
      mLocale = locale;
      mLink = link;
    }
    
    public String toString() {
      return mLocale.getDisplayName();
    }
    
    public boolean download() {
      File dir = new File(Settings.getUserSettingsDirName() + "/languages/");
      
      if(!dir.isDirectory()) {
        dir.mkdirs();
      }
      
      try {
        IOUtilities.saveStream(IOUtilities.getStream(new URL(mLink)), new File(dir,mLink.substring(mLink.lastIndexOf("/")+1)));
      } catch (Exception e) {
        return false;
      }
      
      return true;
    }
    
    public Locale getLocale() {
      return mLocale;
    }
  }
}