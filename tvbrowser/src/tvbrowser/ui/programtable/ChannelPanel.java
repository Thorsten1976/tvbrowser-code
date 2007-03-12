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

package tvbrowser.ui.programtable;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JPanel;
import javax.swing.UIManager;

import devplugin.Channel;


/**
 * TV-Browser
 *
 * @author Martin Oberhauser
 */
public class ChannelPanel extends JPanel {

  private static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(ChannelPanel.class);
  
  private int mColumnWidth;
  private ChannelLabel[] mLabelArr;
  /** Height of Panel, if an Icon is > 15, it get adjusted to it's needs */
  private int mColumnHeight = 15;  
  
  public ChannelPanel(int columnWidth, Channel[] channelArr) {
    setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
    setOpaque(true);
    
    Color c = UIManager.getColor("List.selectionBackground");
    setBackground(new Color(c.getRed(),c.getGreen(),c.getBlue(),c.getAlpha()));
    
    setShownChannels(channelArr);
    setColumnWidth(columnWidth);
  }

  public static void fontChanged() {
    ChannelLabel.fontChanged();
  }
  
  public void setShownChannels(Channel[] channelArr) {
    removeAll();
    mLabelArr = new ChannelLabel[channelArr.length];
    
    for (int i = 0; i < mLabelArr.length; i++) {
      mLabelArr[i]=new ChannelLabel(channelArr[i]);  
      add(mLabelArr[i]);
   
      if ((mLabelArr[i] != null) && (mLabelArr[i].getIcon() != null) && 
              (mLabelArr[i].getIcon().getIconHeight() > mColumnHeight)) {
          mColumnHeight = mLabelArr[i].getIcon().getIconHeight();
      }
   }
    
    setColumnWidth(mColumnWidth);
    updateUI();
  }

  
  
  public void setColumnWidth(int columnWidth) {
    mColumnWidth = columnWidth;
    
    for (int i = 0; i < mLabelArr.length; i++) {
      mLabelArr[i].setPreferredSize(new Dimension(mColumnWidth, mColumnHeight));
    }
  }  

  protected void updateChannelLabelForChannel(Channel ch) {
    if(ch == null)
      return;
    for (int i = 0; i < mLabelArr.length; i++)
      if(mLabelArr[i].getChannel().equals(ch)) {
        mLabelArr[i].setChannel(ch);
        break;
      }
  }
}
