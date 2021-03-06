/*
 * TV-Browser
 * Copyright (C) 2017 TV-Browser team (dev@tvbrowser.org)
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
 * SVN information:
 *     $Date: 2017-05-07 21:36:52 +0200 (So, 07 Mai 2017) $
 *   $Author: ds10 $
 * $Revision: 8672 $
 */
package util.ui;

import devplugin.Program;

/**
 * An functional interface to use for getting programs for an index.
 * 
 * @author René Mach
 * @since 3.4.5
 */
@FunctionalInterface
public interface ProgramGetter {
  /** 
   * @param index The index to get the Program for.
   * @return The program at the index or <code>null</code> if there is no program at the index */
  public Program getProgram(int index);
}
