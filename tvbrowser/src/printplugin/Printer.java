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

import java.awt.Graphics;
import java.awt.print.*;
import java.util.ArrayList;


public class Printer implements Printable {
  
  private Page[] mPages;
  
  public Printer(PageModel[] pageModelArr, PageRenderer pageRenderer) {
  
    System.out.println("Printer.java: Creating pages for "+pageModelArr.length+" virtual pages");
  
    ArrayList pages = new ArrayList();
    for (int i=0;i<pageModelArr.length;i++) {
      Page[] p = pageRenderer.createPages(pageModelArr[i]);
      for (int j=0;j<p.length;j++) {
        pages.add(p[j]);
      }
    }
    mPages = new Page[pages.size()];
    pages.toArray(mPages);   
  }

  public int getNumberOfPages() {
    return mPages.length;
  }

	public int print(Graphics g, PageFormat pageFormat, int pageIndex) throws PrinterException {
    
    if (pageIndex>=mPages.length) {
      return NO_SUCH_PAGE;
    }
    mPages[pageIndex].printPage(g);
    
		return PAGE_EXISTS;
	}
  
  
}