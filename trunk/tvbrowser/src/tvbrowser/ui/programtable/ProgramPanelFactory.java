/*
* TV-Browser
* Copyright (C) 04-2003 Martin Oberhauser (martin_oat@yahoo.de)
*
* This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU General Public License
* as published by the Free Software Foundation; either version 2
* of the License, or (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */


 /**
  * TV-Browser
  * @author Martin Oberhauser
  */

package tvbrowser.ui.programtable;

import java.util.ArrayList;

/**
 * For a better performance, a program panel factory is used (reuse unused objects).
 */

public class ProgramPanelFactory {

  private static ArrayList panelList=new ArrayList(100);
  {
    for (int i=0;i<100;i++) {
      panelList.add(new ProgramPanel());
    }
  }

  private static int curPos=0;


  /**
   * Instead of the new-operator this method is used.
   */
  static ProgramPanel createProgramPanel(devplugin.Program prog) {
    ProgramPanel result=null;

    if (curPos>=panelList.size()) {
      for (int i=0;i<10;i++) {
        panelList.add(new ProgramPanel());
      }
    }

    result=(ProgramPanel)panelList.get(curPos);
    result.init(prog);
    curPos++;
    return result;

  }

  /**
   * Kind of ProgramPanel-destructor. All ProgramPanel objects are "removed".
   */

  static void reset() {
    curPos=0;
  }

}