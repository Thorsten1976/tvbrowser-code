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

import java.awt.*;
import java.awt.image.BufferedImage;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

import devplugin.Channel;
import tvbrowser.core.Settings;

/**
 * A Label for Channels. It shows the Icon and/or the Channel-Name
 */
public class ChannelLabel extends JLabel {

  static Icon DEFAULT_ICON =  new ImageIcon("./imgs/tvbrowser16.png");
  private boolean mChannelIconsVisible;

  /**
   * Creates the ChannelLabel
   */
  public ChannelLabel() {
    this(true);
  }

  public ChannelLabel(boolean channelIconsVisible) {
    mChannelIconsVisible = channelIconsVisible;
  }

  /**
   * Creates the ChannelLabel
   *
   * @param ch Channel to display
   */
  public ChannelLabel(Channel ch) {
    setChannel(ch);
  }

  /**
   * Sets the Channel to display
   *
   * @param ch Channel to display
   */
  public void setChannel(Channel ch) {
    if (Settings.propEnableChannelIcons.getBoolean()) {
      setIcon(ch.getIcon());
    }
    if (!mChannelIconsVisible || !Settings.propEnableChannelIcons.getBoolean() || Settings.propShowChannelNames.getBoolean()) {
      setText(ch.getName());
    }
    setMinimumSize(new Dimension(42,22));
    setToolTipText(ch.getName());
  }

  /**
   * Set the minimum-Size.
   *
   * Overridden to set the Size now
   * @param dim
   */
  public void setMinimumSize(Dimension dim) {
    super.setMinimumSize(dim);
    Dimension current = getSize();
    if (current.width < dim.width) {
      current.width = dim.width;
    }
    if (current.height < dim.height) {
      current.height = dim.height;
    }
    setSize(current);
  }

  /**
   * Sets the Icon
   * This method immediatly returns without any changes if the Settings.propShowChannelIcons is false
   *
   * @param ic Icon
   */
  public void setIcon(Icon ic) {
    if (!mChannelIconsVisible) {
      return;
    }
    if (ic == null) {
      ic = getDefaultIcon();
    }
    BufferedImage img = new BufferedImage(42, 22, BufferedImage.TYPE_INT_RGB);

    Graphics2D g = img.createGraphics();

    g.setColor(Color.WHITE);
    g.fillRect(1, 1, 40, 20);

    int x = 1 + 20 - ic.getIconWidth() / 2;
    int y = 1 + 10 - ic.getIconHeight() / 2;

    ic.paintIcon(this, g, x, y);

    g.setColor(Color.BLACK);
    g.drawRect(0, 0, 42, 22);

    ImageIcon imgIcon = new ImageIcon(img);
    super.setIcon(imgIcon);
  }

  /**
   * Returns the Default-Icon
   * @return default-icon
   */
  private Icon getDefaultIcon() {
    return DEFAULT_ICON;
  }


}
