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

package printplugin;

import java.awt.*;

import javax.swing.Icon;

public class ColumnHeader implements Icon {

  private static Font mFont = new Font("Dialog",Font.BOLD,24);
  private int mWidth, mHeight;
  private String mTitle;
  private double mZoom;
  
  public ColumnHeader(String title, int width, int height, double zoom) {
    mTitle = title;
    mWidth = width;
    mHeight = height;
    mZoom = zoom;
  }

	public int getIconHeight() {
		return mHeight;
	}

	
	public int getIconWidth() {
		return mHeight;
	}

	
	public void paintIcon(Component comp, Graphics g, int x, int y) {
    g.translate(x,y);
   // Graphics2D g = (Graphics2D)graphics;
  //  g.scale(mZoom, mZoom);
		FontMetrics metrics = g.getFontMetrics(mFont);
    int width=metrics.stringWidth(mTitle);
    g.setFont(mFont);
    g.drawRect(0,0,mWidth,mHeight);
    g.drawString(mTitle,(mWidth-width)/2,mFont.getSize());
  //  g.scale(1/mZoom, 1/mZoom);
  g.translate(-x,-y);
	}
  
  
  
}