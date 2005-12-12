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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import tvbrowser.core.ChannelList;
import tvbrowser.core.Settings;
import tvbrowser.ui.programtable.DefaultProgramTableModel;
import util.ui.ChannelListCellRenderer;
import util.ui.DragAndDropMouseListener;
import util.ui.ListDragAndDropHandler;
import util.ui.ListDropAction;
import util.ui.UiUtilities;
import devplugin.Channel;

/**
 * @author bodum
 */
public class ChannelChooserPanel extends JPanel implements ListDropAction, ActionListener {

  private DefaultListModel mChannelChooserModel;
  private JList mList;
  private MainFrame mParent;

  /**
   * @param frame
   */
  public ChannelChooserPanel(MainFrame frame) {
    mParent = frame;

    mChannelChooserModel = new DefaultListModel();

    mList = new JList(mChannelChooserModel);
    updateChannelChooser();
    setLayout(new BorderLayout());
    add(new JScrollPane(mList));

    ListDragAndDropHandler dnDHandler = new ListDragAndDropHandler(mList,
        mList, this);
    new DragAndDropMouseListener(mList, mList, this, dnDHandler);    
    
    mList.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        Channel selectedChannel = (Channel) mList.getSelectedValue();
        if (selectedChannel != null) {
          mParent.showChannel(selectedChannel);
        }
      }
      
      public void mousePressed(MouseEvent e) {
        showPopupMenu(e);
      }
      
      public void mouseReleased(MouseEvent e) {
        showPopupMenu(e);      }
    });
  }

  private void showPopupMenu(MouseEvent e) {
    if(e.isPopupTrigger()) {
      JPopupMenu menu = new JPopupMenu();
      JMenuItem item = new JMenuItem(MainFrame.mLocalizer.msg("context.channels","Cutomize channel list"));
      item.addActionListener(this);
      menu.add(item);
      
      menu.show(mList,e.getX(),e.getY());
    }
  }
  
  public void updateChannelChooser() {
    mList.setCellRenderer(new ChannelListCellRenderer(
        Settings.propShowChannelIconsInChannellist.getBoolean()));
    mChannelChooserModel.removeAllElements();
    Channel[] channelList = tvbrowser.core.ChannelList.getSubscribedChannels();
    for (int i = 0; i < channelList.length; i++) {
      mChannelChooserModel.addElement(channelList[i]);
    }
  }

  public void drop(JList source, JList target, int rows, boolean move) {
    UiUtilities.moveSelectedItems(target, rows, true);

    Object[] list = ((DefaultListModel) mList.getModel()).toArray();

    // Convert the list into a Channel[] and fill channels
    Channel[] channelArr = new Channel[list.length];
    for (int i = 0; i < list.length; i++) {
      channelArr[i] = (Channel) list[i];
    }

    ChannelList.setSubscribeChannels(channelArr);
    Settings.propSubscribedChannels.setChannelArray(channelArr);
    
    ChannelList.create();
    DefaultProgramTableModel model = MainFrame.getInstance().getProgramTableModel();
    model.setChannels(ChannelList.getSubscribedChannels());
    MainFrame.getInstance().updateChannellist();
  }

  public void actionPerformed(ActionEvent e) {
    MainFrame.getInstance().showSettingsDialog();   
  }
}
