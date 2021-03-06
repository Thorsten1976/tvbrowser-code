/*
 * TV-Browser
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

package tvbrowser.ui.update;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.layout.Sizes;

import devplugin.InfoIf;
import devplugin.Version;
import tvbrowser.TVBrowser;
import tvbrowser.core.Settings;
import tvbrowser.core.icontheme.IconLoader;
import tvbrowser.extras.common.InternalPluginProxyIf;
import tvbrowser.ui.mainframe.MainFrame;
import tvbrowser.ui.mainframe.SoftwareUpdater;
import util.browserlauncher.Launch;
import util.exc.TvBrowserException;
import util.ui.EnhancedPanelBuilder;
import util.ui.LinkButton;
import util.ui.Localizer;
import util.ui.TVBrowserIcons;
import util.ui.TextAreaIcon;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;
import util.ui.customizableitems.ItemFilter;
import util.ui.customizableitems.SelectableItem;
import util.ui.customizableitems.SelectableItemList;
import util.ui.customizableitems.SelectableItemRendererCenterComponentIf;
import util.ui.html.HTMLTextHelper;

/**
 * A dialog class that shows the plugin updates/new plugins.
 *
 */
public class SoftwareUpdateDlg extends JDialog implements ActionListener, ListSelectionListener, WindowClosingIf {

  /** The localizer for this class. */
  public static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(SoftwareUpdateDlg.class);

  private JButton mCloseBtn, mDownloadBtn;

  private String mDownloadUrl;

  private JCheckBox mAutoUpdates;

  private SelectableItemList<SoftwareUpdateItem> mSoftwareUpdateItemList;

  private int mLastIndex;

  private JButton mHelpBtn;
  
  private boolean mIsVersionChange;
  
  private Version mOldTvbVersion;
  
  private int mDialogType;

  /**
   * Creates an instance of this class.
   * <p>
   * @param parent The parent dialog.
   * @param downloadUrl The url to download the data from.
   * @param dialogType The type of the dialog.
   * @param itemArr The array with the available update items.
   * @param isVersionChange If this dialog is shown for a TV-Browser version change.
   * @param oldTvbVersion Version that is updated from. 
   */
  public SoftwareUpdateDlg(Window parent, String downloadUrl,
      int dialogType, SoftwareUpdateItem[] itemArr, boolean isVersionChange, Version oldTvbVersion) {
    super(parent,ModalityType.DOCUMENT_MODAL);
    mDialogType = dialogType;
    mIsVersionChange = isVersionChange;
    mOldTvbVersion = oldTvbVersion;
    createGui(downloadUrl, dialogType, itemArr, parent);
    
    if(dialogType == SoftwareUpdater.DRAG_AND_DROP_TYPE || dialogType == SoftwareUpdater.ONLY_UPDATE_TYPE) {
      mSoftwareUpdateItemList.selectAll();
      mDownloadBtn.setEnabled(!mSoftwareUpdateItemList.getSelectionList().isEmpty());
    }
  }
  
  /**
   * Creates an instance of this class.
   * <p>
   * @param parent The parent dialog.
   * @param downloadUrl The url to download the data from.
   * @param dialogType The type of the dialog.
   * @param itemArr The array with the available update items.
   */
  public SoftwareUpdateDlg(Window parent, String downloadUrl,
      int dialogType, SoftwareUpdateItem[] itemArr) {
    this(parent,downloadUrl,dialogType,itemArr,false,null);
  }

  /**
   * Creates an instance of this class for drag-n-drop instead of normal plugin downloads.
   * <p>
   * @param parent The parent dialog.
   * @param dialogType The type of the dialog.
   * @param itemArr The array with the available update items.
   */
  public SoftwareUpdateDlg(Window parent,
      int dialogType, SoftwareUpdateItem[] itemArr) {
    this(parent, null, dialogType, itemArr);
    mDownloadBtn.setEnabled(itemArr.length > 0);
  }
  
  /**
   * Creates an instance of this class for drag-n-drop instead of normal plugin downloads.
   * <p>
   * @param parent The parent dialog.
   * @param dialogType The type of the dialog.
   * @param itemArr The array with the available update items.
   * @param isVersionChange If this dialog is shown for a TV-Browser version change.
   * @param oldTvbVersion Version that is updated from. 
   */
  public SoftwareUpdateDlg(Window parent,
      int dialogType, SoftwareUpdateItem[] itemArr, boolean isVersionChange, Version oldTvbVersion) {
    this(parent, null, dialogType, itemArr, isVersionChange, oldTvbVersion);
    mDownloadBtn.setEnabled(mSoftwareUpdateItemList.getItemCount() > 0);
  }

  private void createGui(String downloadUrl, int dialogType, SoftwareUpdateItem[] itemArr, Window parent) {
    Arrays.sort(itemArr, new Comparator<SoftwareUpdateItem>() {
      @Override
      public int compare(SoftwareUpdateItem item1, SoftwareUpdateItem item2) {
        return item1.getName().compareToIgnoreCase(item2.getName());
      }
    });
    
    mDownloadUrl = downloadUrl;
    setTitle(mLocalizer.msg("title", "Download plugins"));

    if(mIsVersionChange) {
      setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
    }
    
    JPanel contentPane = (JPanel) getContentPane();
    contentPane.setLayout(new BorderLayout(0, 10));
    contentPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 11, 11));
    mCloseBtn = new JButton(Localizer.getLocalization(Localizer.I18N_CLOSE));
    mCloseBtn.addActionListener(this);
    mCloseBtn.setEnabled(!mIsVersionChange);

    mDownloadBtn = new JButton(mLocalizer.msg("download", "Download selected items"));
    mDownloadBtn.addActionListener(this);

    mHelpBtn = new JButton(mLocalizer.msg("openWebsite","Open website"), TVBrowserIcons.webBrowser(TVBrowserIcons.SIZE_SMALL));
    mHelpBtn.addActionListener(this);
    mHelpBtn.setEnabled(false);

    ButtonBarBuilder builder = new ButtonBarBuilder();

    if(dialogType == SoftwareUpdater.ONLY_UPDATE_TYPE && !mIsVersionChange) {
      mAutoUpdates = new JCheckBox(mLocalizer.msg("autoUpdates","Find plugin updates automatically"), Settings.propAutoUpdatePlugins.getBoolean());
      mAutoUpdates.addItemListener(e -> {
        Settings.propAutoUpdatePlugins.setBoolean(e.getStateChange() == ItemEvent.SELECTED);
      });

      builder.addFixed(mAutoUpdates);
      builder.addRelatedGap();
    }
    builder.addFixed(mHelpBtn);

    builder.addGlue();
    builder.addFixed(mDownloadBtn);
    
    builder.addRelatedGap();
    builder.addFixed(mCloseBtn);
    

    final CellConstraints cc = new CellConstraints();

    FormLayout layout = new FormLayout("default,5dlu,0dlu:grow","default");

    JPanel northPn = new JPanel(layout);
    
    JLabel info = new JLabel(mLocalizer.msg("header","Here you can download new plugins and updates for it."));
    
    if(dialogType == SoftwareUpdater.ONLY_UPDATE_TYPE) {
      info.setText(mLocalizer.msg("updateHeader","Updates for installed plugins/new matching data plugins were found."));
    }
    else if(dialogType == SoftwareUpdater.ONLY_DATA_SERVICE_TYPE) {
      info.setText(mLocalizer.msg("dataServiceHeader","TV-Browser is based on Plugins. You will need at least one of the listed data Plugins."));
      info.setFont(info.getFont().deriveFont(Font.BOLD).deriveFont((float)14));
    }
    
    northPn.add(info, cc.xyw(1,1,3));

    JPanel southPn = new JPanel(new BorderLayout());

    southPn.add(builder.getPanel(), BorderLayout.SOUTH);
    
    ArrayList<SoftwareUpdateItem> selectedItems = new ArrayList<SoftwareUpdateItem>();
    ArrayList<SoftwareUpdateItem> notSelectableItems = new ArrayList<SoftwareUpdateItem>();
    
    ArrayList<String> selectedDataServices = new ArrayList<String>(0);
    
    if(dialogType == SoftwareUpdater.ONLY_DATA_SERVICE_TYPE || mIsVersionChange) {
      String country = Locale.getDefault().getCountry();
      
      if(country.equals(Locale.GERMANY.getCountry()) || country.equals("ES") || country.equals("IT") || country.equals("FR") || country.equals("DK") || country.equals("CH") || country.equals("AT") || Locale.getDefault().getLanguage().toLowerCase().startsWith("de")) {
        selectedDataServices.add("TvBrowserDataService");
        
        if(country.equals(Locale.GERMANY.getCountry()) || country.equals("CH") || country.equals("AT") || Locale.getDefault().getLanguage().toLowerCase().startsWith("de")) {
          selectedDataServices.add("EPGdonateData");
          selectedDataServices.add("EPGpaidData");
        }
      }
      else if(country.equals(Locale.CANADA.getCountry()) || country.equals(Locale.US.getCountry())) {
        selectedDataServices.add("SchedulesDirectDataService");
        selectedDataServices.add("TvBrowserDataService");
      }
      else if(country.equals(Locale.UK.getCountry())) {
        selectedDataServices.add("BBCDataService");
        selectedDataServices.add("TvBrowserDataService");
      }
      else if(country.equals("NO")) {
        selectedDataServices.add("TvBrowserDataService");
        selectedDataServices.add("SweDBTvDataService");
      }
      else if(country.equals("SE") || country.equals("AU") || country.equals("HR")) {
        selectedDataServices.add("SweDBTvDataService");
      }
      else {
        selectedDataServices.add("TvBrowserDataService");
        selectedDataServices.add("SchedulesDirectDataService");
        selectedDataServices.add("BBCDataService");
        selectedDataServices.add("RadioTimesDataService");
        selectedDataServices.add("SweDBTvDataService");
      }
    }
    
    ArrayList<SoftwareUpdateItem> mItemList = new ArrayList<SoftwareUpdateItem>();
    
    for (SoftwareUpdateItem item : itemArr) {
      if(item.isPreSelected() || (selectedDataServices.contains(item.getClassName()) && (mIsVersionChange || dialogType == SoftwareUpdater.ONLY_DATA_SERVICE_TYPE))) {
        if(mIsVersionChange) {
          mItemList.add(item);
        }
        
        selectedItems.add(item);
        
    	if(mIsVersionChange && (item.getEssentialTvbVersion() != null && mOldTvbVersion != null && mOldTvbVersion.compareTo(item.getEssentialTvbVersion()) < 0)) {
    	  notSelectableItems.add(item);
    	}
      }
    }
    
    mDownloadBtn.setEnabled(!selectedItems.isEmpty());
    
    mSoftwareUpdateItemList = new SelectableItemList<>(selectedItems.toArray(new SoftwareUpdateItem[selectedItems.size()]),mIsVersionChange ? mItemList.toArray(new SoftwareUpdateItem[mItemList.size()]) : itemArr,notSelectableItems.toArray(new SoftwareUpdateItem[notSelectableItems.size()]));
    mSoftwareUpdateItemList.addListSelectionListener(this);
    mSoftwareUpdateItemList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    mSoftwareUpdateItemList.setListUI(new MyListUI());
    mSoftwareUpdateItemList.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

    mSoftwareUpdateItemList.addCenterRendererComponent(PluginsSoftwareUpdateItem.class,new SelectableItemRendererCenterComponentIf<SoftwareUpdateItem>() {
      private final ImageIcon NEW_VERSION_ICON = IconLoader.getInstance().getIconFromTheme("status", "software-update-available", 16);

      public JPanel createCenterPanel( JList<? extends SelectableItem<SoftwareUpdateItem>> list, SoftwareUpdateItem value, int index, boolean isSelected, boolean isEnabled, JScrollPane parentScrollPane, int leftColumnWidth) {
        FormLayout lay = new FormLayout("5dlu,default,5dlu,default:grow","2dlu,default,2dlu,fill:pref:grow,2dlu");
        EnhancedPanelBuilder pb = new EnhancedPanelBuilder(lay);
        pb.getPanel().setOpaque(false);

        SoftwareUpdateItem item = (SoftwareUpdateItem)value;

        JLabel label = pb.addLabel(HTMLTextHelper.convertHtmlToText(item.getName()) + " " + item.getVersion(), cc.xy(2,2));
        label.setFont(label.getFont().deriveFont(Font.BOLD, label.getFont().getSize2D()+2));

        int width = parentScrollPane.getSize().width - parentScrollPane.getVerticalScrollBar().getWidth() - leftColumnWidth - Sizes.dialogUnitXAsPixel(5,pb.getPanel()) * 4 - parentScrollPane.getInsets().left - parentScrollPane.getInsets().right;

        if (width <= 0) {
          width = Settings.propColumnWidth.getInt();
        }

        TextAreaIcon icon = new TextAreaIcon(HTMLTextHelper.convertHtmlToText(item.getDescription()), UIManager.getFont("Label.font"), width, 2);

        JLabel iconLabel = new JLabel("") {
          @Override
          protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D)g;
            RenderingHints rh = new RenderingHints(
                     RenderingHints.KEY_TEXT_ANTIALIASING,
                     RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setRenderingHints(rh);
            
            super.paintComponent(g);
          }
        };
        iconLabel.setIcon(icon);

        pb.add(iconLabel, cc.xyw(2,4,3));

        JLabel label3 = new JLabel();

        if (item.isAlreadyInstalled()) {
	        Version installedVersion = item.getInstalledVersion();
	        if ((installedVersion != null) && (installedVersion.compareTo(item.getVersion()) < 0)) {
	          label.setIcon(NEW_VERSION_ICON);

	          label3.setText("(" + mLocalizer.msg("installed","Installed version: ") + installedVersion.toString()+")");
	          label3.setFont(label3.getFont().deriveFont(label3.getFont().getSize2D()+2));

	          pb.add(label3, cc.xy(4,2));
	        }
        }

        if (isSelected && isEnabled) {
          label.setForeground(list.getSelectionForeground());

          String author = item.getProperty("author");
          String website = item.getWebsite();

          FormLayout authorAndWebsiteLayout = new FormLayout("default,5dlu,default","default");
          JPanel authorAndWebsite = new JPanel(authorAndWebsiteLayout);
          authorAndWebsite.setOpaque(false);

          if (author != null) {
            lay.appendRow(RowSpec.decode("2dlu"));
            lay.appendRow(RowSpec.decode("default"));
            lay.appendRow(RowSpec.decode("2dlu"));

            pb.add(authorAndWebsite, cc.xyw(2,7,3));

            JLabel authorLabel = new JLabel(mLocalizer.msg("author", "Author"));
            authorLabel.setFont(authorLabel.getFont().deriveFont(Font.BOLD));
            authorLabel.setForeground(list.getSelectionForeground());
            authorLabel.setHorizontalAlignment(SwingConstants.RIGHT);

            JLabel authorName = new JLabel(HTMLTextHelper.convertHtmlToText(author));
            authorName.setForeground(list.getSelectionForeground());

            authorAndWebsite.add(authorLabel, cc.xy(1,1));
            authorAndWebsite.add(authorName, cc.xy(3,1));
          }

          if (website != null) {
            if(author == null) {
              lay.appendRow(RowSpec.decode("2dlu"));
              lay.appendRow(RowSpec.decode("default"));
              lay.appendRow(RowSpec.decode("2dlu"));

              pb.add(authorAndWebsite, cc.xyw(2,7,3));
            }
            else {
              authorAndWebsiteLayout.appendRow(RowSpec.decode("1dlu"));
              authorAndWebsiteLayout.appendRow(RowSpec.decode("default"));
            }

            JLabel webLabel = new JLabel(mLocalizer.msg("website", "Website"));
            webLabel.setFont(webLabel.getFont().deriveFont(Font.BOLD));
            webLabel.setForeground(list.getSelectionForeground());
            webLabel.setHorizontalAlignment(SwingConstants.RIGHT);

            LinkButton webLink = new LinkButton(HTMLTextHelper.convertHtmlToText(website));
            webLink.setForeground(list.getSelectionForeground());

            authorAndWebsite.add(webLabel, cc.xy(1,author == null ? 1 : 3));
            authorAndWebsite.add(webLink, cc.xy(3,author == null ? 1 : 3));
          }

          icon.setMaximumLineCount(-1);
          iconLabel.setForeground(list.getSelectionForeground());

          label3.setForeground(list.getSelectionForeground());
        } else {
          if(!item.isStable()) {
            label.setForeground(new Color(200, 0, 0));
          }
          else {
            label.setForeground(list.getForeground());
          }

          icon.setMaximumLineCount(1);
          iconLabel.setForeground(list.getSelectionForeground());
          iconLabel.setForeground(list.getForeground());
          label3.setForeground(Color.gray);
        }

        return pb.getPanel();
      }

      public void calculateSize(JList<? extends SelectableItem<SoftwareUpdateItem>> list, int index, JPanel contPane) {
        if(list.getUI() instanceof MyListUI) {
          ((MyListUI)list.getUI()).setCellHeight(index, contPane.getPreferredSize().height);
        }
      }
    });

    mSoftwareUpdateItemList.addMouseListener(new MouseAdapter() {
      @Override
      public void mousePressed(MouseEvent e) {
        if(SwingUtilities.isRightMouseButton(e) && e.isPopupTrigger()) {
          showPopupMenu(e);
        }
      }

      @Override
      public void mouseReleased(MouseEvent e) {
        if(SwingUtilities.isRightMouseButton(e) && e.isPopupTrigger()) {
          showPopupMenu(e);
        }
      }
    });

    if(dialogType != SoftwareUpdater.ONLY_UPDATE_TYPE && dialogType != SoftwareUpdater.ONLY_DATA_SERVICE_TYPE) {
      layout.appendRow(RowSpec.decode("5dlu"));
      layout.appendRow(RowSpec.decode("default"));

      JLabel filterLabel = new JLabel(mLocalizer.msg("filterLabel","Show only Plugins with the following category:"));

      northPn.add(filterLabel, cc.xy(1,3));

      ArrayList<FilterItem> filterList = new ArrayList<FilterItem>(0);

      for(SoftwareUpdateItem item : itemArr) {
        int index = 0;

        for(int i = 0; i < filterList.size(); i++) {
          int compareValue = filterList.get(i).compareTo(item.getCategory());

          if(compareValue == 0) {
            index = -1;
            break;
          }
          else if(compareValue < 0) {
            index = i+1;
          }
        }

        if(index != -1) {
          filterList.add(index,new FilterItem(item.getCategory()));
        }
      }

      filterList.add(0, new FilterItem("all"));

      JComboBox<FilterItem> filterBox = new JComboBox<>(filterList.toArray(new FilterItem[filterList.size()]));

      mSoftwareUpdateItemList.setFilterComboBox(filterBox);

      northPn.add(filterBox, cc.xy(3,3));
    }

    contentPane.add(northPn, BorderLayout.NORTH);
    contentPane.add(mSoftwareUpdateItemList, BorderLayout.CENTER);
    contentPane.add(southPn, BorderLayout.SOUTH);

    Settings.layoutWindow("softwareUpdateDlg", this, new Dimension(700,600), parent);

    setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        close();
      }
    });
    
    addComponentListener(new ComponentAdapter() {
      @Override
      public void componentResized(ComponentEvent e) {
        mSoftwareUpdateItemList.setVerticalScrollBarBlockIncrement(mSoftwareUpdateItemList.getSize().height-5);
      }
    });

    UiUtilities.registerForClosing(this);
  }
  
  /**
   * Gets if this update dialog contains selectable items.
   * <p>
   * @return <code>true</code> if this dialog doesn't contains selectable items.
   * @since 3.4.3
   */
  public boolean isEmpty() {
    return mSoftwareUpdateItemList.isEmpty();
  }
  
  public void actionPerformed(ActionEvent event) {
    if (event.getSource() == mCloseBtn) {
      close();
    } else if (event.getSource() == mDownloadBtn) {
      mDownloadBtn.setEnabled(false);
      Cursor cursor = getCursor();
      this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
      int successfullyDownloadedItems = 0;
      try {
        for (SoftwareUpdateItem item : mSoftwareUpdateItemList.getSelectionList()) {
          try {
            item.download(mDownloadUrl);
            successfullyDownloadedItems++;
          } catch (TvBrowserException e) {
            util.exc.ErrorHandler.handle(e);
          }
        }
      } finally {
        mDownloadBtn.setEnabled(true);
        setCursor(cursor);
      }
      if (successfullyDownloadedItems > 0 && !mIsVersionChange && mDialogType != SoftwareUpdater.ONLY_DATA_SERVICE_TYPE) {
    	if (TVBrowser.restartEnabled()) {
    		String[] options = {"",""};
    		options[0] = mLocalizer.msg("restartnow", "restart");
    		options[1] = mLocalizer.msg("restartlater", "later");
    		if (JOptionPane.showOptionDialog(UiUtilities.getLastModalChildOf(MainFrame.getInstance()),  mLocalizer.msg("restartplugin", "plugins has been installed.\nrestart TV-Browser?"),
    				mLocalizer.msg("restartdialog","restart"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null,options ,options[0])==0){
    	        TVBrowser.addRestart();
    	        MainFrame.getInstance().quit();
    		}
    	} else {
    		JOptionPane.showMessageDialog(UiUtilities.getLastModalChildOf(MainFrame.getInstance()), mLocalizer.msg("restartprogram", "please restart tvbrowser before..."));
    	}
        setVisible(false);
      }
      else if(mIsVersionChange || mDialogType == SoftwareUpdater.ONLY_DATA_SERVICE_TYPE) {
        setVisible(false);
      }
    }
    else if (event.getSource() == mHelpBtn) {
      final SoftwareUpdateItem item = mSoftwareUpdateItemList.getSelectedValue().getItem();
      if (item != null) {
        Launch.openURL(item.getWebsite());
      }
    }
  }

  @SuppressWarnings("unchecked")
  public void valueChanged(ListSelectionEvent event) {
    mDownloadBtn.setEnabled(!mSoftwareUpdateItemList.getSelectionList().isEmpty());
    
    if(mIsVersionChange) {
      mCloseBtn.setEnabled(mSoftwareUpdateItemList.getSelectionList().isEmpty());
    }

    if(event.getSource() instanceof JList) {
      if(!event.getValueIsAdjusting()) {
        @SuppressWarnings("rawtypes")
        JList list = ((JList)event.getSource());
        
        if(mLastIndex != -1 && list.getSelectedIndex() != mLastIndex && list.getModel().getSize()-1 >= mLastIndex) {
          ((MyListUI)list.getUI()).setCellHeight(mLastIndex,list.getCellRenderer().getListCellRendererComponent(list, list.getModel().getElementAt(mLastIndex),
              mLastIndex, false, false).getPreferredSize().height);
        }

        mLastIndex = list.getSelectedIndex();
        if (mLastIndex < 0) {
          mHelpBtn.setEnabled(false);
        }
        else {
          SoftwareUpdateItem item = ((SelectableItem<SoftwareUpdateItem>)mSoftwareUpdateItemList.getSelectedValue()).getItem();
          String website = item.getWebsite();
          mHelpBtn.setEnabled(website != null && website.length() > 0);
        }
      }
    }

    mSoftwareUpdateItemList.calculateSize();
  }

  public void close() {
    if(!mIsVersionChange || mSoftwareUpdateItemList.getSelectionList().isEmpty()) {
      setVisible(false);
    }
  }

  private static class MyListUI extends javax.swing.plaf.basic.BasicListUI {
    protected synchronized void setCellHeight(int row, int height) {
      cellHeights[row] = height;
    }

    @Override
    public Dimension getPreferredSize(JComponent c) {
      int width = super.getPreferredSize(c).width;
      int height = 0;

      Insets i = c.getInsets();

      height += i.top + i.bottom;

      for(int localCellHeight : cellHeights) {
        height += localCellHeight;
      }

      return new Dimension(width,height);
    }
  }

  private void showPopupMenu(MouseEvent e) {
    if(e.getSource() instanceof JList) {
      @SuppressWarnings("rawtypes")
      JList list = (JList)e.getSource();

      Object listItem = list.getModel().getElementAt(list.locationToIndex(e.getPoint()));

      if(listItem instanceof SelectableItem) {
        @SuppressWarnings("rawtypes")
        final Object item = ((SelectableItem)listItem).getItem();

        if(item instanceof SoftwareUpdateItem) {
          if(((SoftwareUpdateItem)item).getWebsite() != null) {
            JPopupMenu menu = new JPopupMenu();

            JMenuItem menuItem = new JMenuItem(mLocalizer.msg("openWebsite","Open website"), TVBrowserIcons.webBrowser(TVBrowserIcons.SIZE_SMALL));
            menuItem.addActionListener(evt -> {
              Launch.openURL(((SoftwareUpdateItem)item).getWebsite());
            });

            menu.add(menuItem);

            menu.show(e.getComponent(), e.getX(), e.getY());
          }
        }
      }
    }
  }

  public static class FilterItem implements ItemFilter {
    private String mType;

    public FilterItem(String type) {
      mType = type;
    }

    @Override
    public String toString() {
      return mLocalizer.msg(mType,mType);
    }

    @Override
    public boolean equals(Object o) {
      if(o != null) {
        if(o instanceof FilterItem) {
          return mType.equals(((FilterItem)o).mType);
        }
        else if(o instanceof String) {
          return mType.equals(o);
        }
      }

      return false;
    }

    /**
     * Compares the names of this filter item and
     * the given Object if it is a filter item or
     * if the given Object is a String with it's
     * internalisation.
     * <p>
     * @param o The Object to compare with
     * @return &lt; 0 if the name of this item is alphabetical smaller as the given String
     * 0 if they are equal and &gt; 0 if this name is greater.
     */
    public int compareTo(Object o) {
      if(o != null) {
        if(o instanceof FilterItem) {
          return toString().compareToIgnoreCase(((FilterItem)o).toString());
        }
        else if(o instanceof String) {
          return toString().compareToIgnoreCase(mLocalizer.msg((String)o,(String)o));
        }
      }

      return 0;
    }

    public boolean accept(Object o) {
      if(o instanceof SoftwareUpdateItem) {
        if(mType.equals("all")) {
          return true;
        }

        return equals(((SoftwareUpdateItem)o).getCategory());
      }
      if(o instanceof InternalPluginProxyIf) {
        if(mType.equals("all")) {
          return true;
        }

        return equals(((InternalPluginProxyIf)o).getPluginCategory());        
      }
      if(o instanceof InfoIf) {
        if(mType.equals("all")) {
          return true;
        }

        return equals(((InfoIf)o).getPluginCategory());        
      }

      return false;
    }
  }
}
