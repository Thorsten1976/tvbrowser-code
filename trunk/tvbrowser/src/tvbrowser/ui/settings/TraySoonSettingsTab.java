package tvbrowser.ui.settings;

import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.SettingsItem;
import devplugin.SettingsTab;
import tvbrowser.core.Settings;
import util.ui.Localizer;
import util.ui.UiUtilities;

/**
 * The settings tab for the SOON_TYPE of the ProgramMenuItem.
 * 
 * @author René Mach
 *
 */
public class TraySoonSettingsTab implements SettingsTab {

  private static final Localizer mLocalizer = TrayBaseSettingsTab.mLocalizer;
  private JCheckBox mIsEnabled, mShowTime, mShowToolTip;
  private JLabel mIconSeparator,mSeparator1, mSeparator2;
  private static boolean mTrayIsEnabled = Settings.propTrayIsEnabled.getBoolean();
  
  private JEditorPane mHelpLabel;
  private JRadioButton mShowIconAndName, mShowName, mShowIcon;
  
  private JCheckBox mShowSortNumber;
  
  private static TraySoonSettingsTab mInstance;
  
  public JPanel createSettingsPanel() {
    mInstance = this;
    
    PanelBuilder builder = new PanelBuilder(new FormLayout("5dlu,pref:grow,5dlu",
        "pref,5dlu,pref,10dlu,pref,5dlu,pref,pref,pref,5dlu,pref," +
        "10dlu,pref,5dlu,pref,pref,fill:default:grow,pref"));
    builder.border(Borders.DIALOG);
    
    mIsEnabled = new JCheckBox(mLocalizer.msg("soonEnabled","Show Soon running programs"),Settings.propTraySoonProgramsEnabled.getBoolean());
    
    mShowIconAndName = new JRadioButton(mLocalizer.msg("showIconName","Show channel icon and channel name"),Settings.propTraySoonProgramsContainsName.getBoolean() && Settings.propTraySoonProgramsContainsIcon.getBoolean());
    mShowName = new JRadioButton(mLocalizer.msg("showName","Show channel name"),Settings.propTraySoonProgramsContainsName.getBoolean() && !Settings.propTraySoonProgramsContainsIcon.getBoolean());
    mShowIcon = new JRadioButton(mLocalizer.msg("showIcon","Show channel icon"),!Settings.propTraySoonProgramsContainsName.getBoolean() && Settings.propTraySoonProgramsContainsIcon.getBoolean());
    
    ButtonGroup bg = new ButtonGroup();
    bg.add(mShowIconAndName);
    bg.add(mShowIcon);
    bg.add(mShowName);
    
    mShowSortNumber = new JCheckBox(mLocalizer.msg("showChannelNumber", "Show sort number"),Settings.propTraySoonProgramsShowingSortNumber.getBoolean());
    
    mShowTime = new JCheckBox(mLocalizer.msg("showTime","Show start time"),Settings.propTraySoonProgramsContainsTime.getBoolean());
    mShowToolTip = new JCheckBox(mLocalizer.msg("showToolTip","Show additional information of the program in a tool tip"),Settings.propTraySoonProgramsContainsToolTip.getBoolean());
    mShowToolTip.setToolTipText(mLocalizer.msg("toolTipTip","Tool tips are small helper to something, like this one."));
    
    mHelpLabel = UiUtilities.createHtmlHelpTextArea(mLocalizer.msg("help","The Tray is deactivated. To activate these settings activate the option <b>Tray activated</b> in the <a href=\"#link\">Tray Base settings</a>."), new HyperlinkListener() {
      public void hyperlinkUpdate(HyperlinkEvent e) {
        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
          SettingsDialog.getInstance().showSettingsTab(SettingsItem.TRAY);
        }
      }
    });
        
    JPanel c = (JPanel) builder.addSeparator(mLocalizer.msg("soon","Soon running programs"), CC.xyw(1,1,3));
    builder.add(mIsEnabled, CC.xy(2,3));
    
    JPanel c1 = (JPanel) builder.addSeparator(mLocalizer.msg("iconNameSeparator","Channel icons/channel name"), CC.xyw(1,5,3));
    
    builder.add(mShowIconAndName, CC.xy(2,7));
    builder.add(mShowIcon, CC.xy(2,8));
    builder.add(mShowName, CC.xy(2,9));
    
    builder.add(mShowSortNumber, CC.xy(2,11));
    
    JPanel c2 = (JPanel) builder.addSeparator(mLocalizer.msg("settings","Settings"), CC.xyw(1,13,3));
    
    builder.add(mShowTime, CC.xy(2,15));
    builder.add(mShowToolTip, CC.xy(2,16));
    builder.add(mHelpLabel, CC.xyw(1,18,3));
    
    mSeparator1 = (JLabel)c.getComponent(0);
    mIconSeparator = (JLabel)c1.getComponent(0);
    mSeparator2 = (JLabel)c2.getComponent(0);
    
    setEnabled(true);
    
    mIsEnabled.addActionListener(e -> {
      setEnabled(false);
    });
    
    return builder.getPanel();
  }

  private void setEnabled(boolean trayStateChange) {
    mHelpLabel.setVisible(!mTrayIsEnabled);
    
    if(trayStateChange) {
      mSeparator1.setEnabled(mTrayIsEnabled);
      mIsEnabled.setEnabled(mTrayIsEnabled);
    }
    
    TrayProgramsChannelsSettingsTab.setSoonIsEnabled(mIsEnabled.isSelected());
    mIconSeparator.setEnabled(mIsEnabled.isSelected() && mTrayIsEnabled);
    mSeparator2.setEnabled(mIsEnabled.isSelected() && mTrayIsEnabled);
    mShowName.setEnabled(mIsEnabled.isSelected() && mTrayIsEnabled);
    mShowIcon.setEnabled(mIsEnabled.isSelected() && mTrayIsEnabled);
    mShowIconAndName.setEnabled(mIsEnabled.isSelected() && mTrayIsEnabled);
    mShowSortNumber.setEnabled(mIsEnabled.isSelected() && mTrayIsEnabled);
    
    mShowTime.setEnabled(mIsEnabled.isSelected() && mTrayIsEnabled);
    mShowToolTip.setEnabled(mIsEnabled.isSelected() && mTrayIsEnabled);
  }
  
  public void saveSettings() {
    if(mIsEnabled != null) {
      Settings.propTraySoonProgramsEnabled.setBoolean(mIsEnabled.isSelected());
    }
    if(mShowIconAndName != null && mShowName != null && mShowIcon != null) {
      Settings.propTraySoonProgramsContainsName.setBoolean(mShowIconAndName.isSelected() || mShowName.isSelected());
      Settings.propTraySoonProgramsContainsIcon.setBoolean(mShowIconAndName.isSelected() || mShowIcon.isSelected());
    }
    if(mShowTime != null) {
      Settings.propTraySoonProgramsContainsTime.setBoolean(mShowTime.isSelected());
    }
    if(mShowToolTip != null) {
      Settings.propTraySoonProgramsContainsToolTip.setBoolean(mShowToolTip.isSelected());
    }
    if(mShowSortNumber != null) {
      Settings.propTraySoonProgramsShowingSortNumber.setBoolean(mShowSortNumber.isSelected());
    }
  }

  public Icon getIcon() {
    return null;
  }

  public String getTitle() {
    return mLocalizer.msg("soon","Soon running programs");
  }
  
  protected static void setTrayIsEnabled(boolean value) {
    mTrayIsEnabled = value;
    if(mInstance != null) {
      mInstance.setEnabled(true);
    }
  }
}
