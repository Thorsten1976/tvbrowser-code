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

package util.ui;

import java.awt.Window;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

import devplugin.Channel;
import devplugin.Plugin;
import util.ui.customizableitems.SelectableItemList;

/**
 * The ChannelChooserDlg class provides a Dialog for choosing channels. The user
 * can choose from all subscribed channels.
 */
public class ChannelChooserDlg extends JDialog implements WindowClosingIf {

  private Channel[] mResultChannelArr;
  private Channel[] mChannelArr;
  private OrderChooser<Channel> mChannelOrderChooser;
  private SelectableItemList<Channel> mChannelItemList;
  
  /**
   * If this Dialog should contain an OrderChooser use this type.
   */
  public static final int ORDER_CHOOSER = 0;
  
  /**
   * If this Dialog should contain a SelectableItemList use this type.
   */
  public static final int SELECTABLE_ITEM_LIST = 1;

  private static final util.ui.Localizer mLocalizer
     = util.ui.Localizer.getLocalizerFor(ChannelChooserDlg.class);

  /**
   * 
   * @param parent The parent window.
   * @param channelArr
   *          The initially selected channels
   * @param description
   *          A description text below the channel list.
   * @since 3.0
   */
  public ChannelChooserDlg(Window parent, Channel[] channelArr,
      String description) {
    super(parent);
    setModalityType(ModalityType.DOCUMENT_MODAL);
    init(channelArr, description, ORDER_CHOOSER);
  }

  /**
   * 
   * @param parent The parent window.
   * @param channelArr
   *          The initially selected channels
   * @param description
   *          A description text below the channel list.
   * @param type
   *          The type of this ChannelChooser
   * @since 3.0
   */
  public ChannelChooserDlg(Window parent, Channel[] channelArr,
      String description, int type) {
    super(parent);
    setModalityType(ModalityType.DOCUMENT_MODAL);
    init(channelArr, description, type);
  }

  private void init(Channel[] channelArr, String description, int type) {
    setTitle(mLocalizer.msg("chooseChannels","choose channels"));
    UiUtilities.registerForClosing(this);
    
    if (channelArr == null) {
      mChannelArr = new Channel[]{};
      mResultChannelArr = new Channel[]{};
    }
    else {
      mChannelArr = channelArr;
      mResultChannelArr = channelArr;
    }

    JPanel contentPane = (JPanel)getContentPane();
    FormLayout layout = new FormLayout("fill:pref:grow", "");
    contentPane.setLayout(layout);
    contentPane.setBorder(Borders.DLU4);
    CellConstraints cc = new CellConstraints();
    
    if(type == ORDER_CHOOSER) {
      mChannelOrderChooser = new OrderChooser<>(mResultChannelArr, Plugin.getPluginManager().getSubscribedChannels());
      mChannelItemList = null;
    }
    else {
      mChannelItemList = new SelectableItemList<>(mResultChannelArr, Plugin.getPluginManager().getSubscribedChannels());
      mChannelOrderChooser = null;
    }

    int pos = 1;
    layout.appendRow(RowSpec.decode("fill:default:grow"));
    layout.appendRow(RowSpec.decode("3dlu"));
    
    if(mChannelOrderChooser != null) {
      contentPane.add(mChannelOrderChooser, cc.xy(1,pos));
    } else {
      contentPane.add(mChannelItemList, cc.xy(1,pos));
    }
      
    pos += 2;
    
    if (description != null) {
      JLabel lb = new JLabel(description);
      layout.appendRow(RowSpec.decode("pref"));
      layout.appendRow(RowSpec.decode("3dlu"));
      contentPane.add(lb, cc.xy(1,pos));
      pos += 2;
    }

    JButton okBt = new JButton(Localizer.getLocalization(Localizer.I18N_OK));
    JButton cancelBt = new JButton(Localizer.getLocalization(Localizer.I18N_CANCEL));

    okBt.addActionListener(event -> {
      List<Channel> listChannel = mChannelOrderChooser != null ? mChannelOrderChooser.getOrderList() : mChannelItemList.getSelectionList();
      mResultChannelArr = new Channel[listChannel.size()];
      for (int i = 0; i < listChannel.size(); i++) {
        mResultChannelArr[i] = listChannel.get(i);
      }
      setVisible(false);
    });

    cancelBt.addActionListener(event -> {
      mResultChannelArr = null;
      setVisible(false);
    });

    ButtonBarBuilder builder = new ButtonBarBuilder();
    builder.addGlue();
    builder.addButton(new JButton[] {okBt, cancelBt});
    
    layout.appendRow(RowSpec.decode("pref"));
    contentPane.add(builder.getPanel(), cc.xy(1,pos));
    
    pack();
  }

  /**
   *
   * @return an array of the selected channels. If the user cancelled the dialog,
   * the array from the constructor call is returned.
   */
  public Channel[] getChannels() {

    if (mResultChannelArr==null) {
      return mChannelArr;
    }
    return mResultChannelArr;
  }

  public void close() {
    setVisible(false);
  }

}