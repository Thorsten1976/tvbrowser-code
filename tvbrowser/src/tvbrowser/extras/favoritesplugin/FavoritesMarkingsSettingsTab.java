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
 *     $Date$
 *   $Author$
 * $Revision$
 */
package tvbrowser.extras.favoritesplugin;

import javax.swing.Icon;
import javax.swing.JPanel;

import util.ui.DefaultMarkingPrioritySelectionPanel;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.SettingsTab;

/**
 * The settings tab for setting the mark priority of favorites programs.
 * 
 * @author Ren� Mach
 */
public class FavoritesMarkingsSettingsTab implements SettingsTab {
  private DefaultMarkingPrioritySelectionPanel mMarkingsPanel;
  
  public JPanel createSettingsPanel() {
    JPanel panel = new JPanel(new FormLayout("default:grow","fill:default:grow"));
    panel.add(mMarkingsPanel = DefaultMarkingPrioritySelectionPanel.createPanel(FavoritesPlugin.getInstance().getMarkPriority(),true,true), new CellConstraints().xy(1,1));
    
    return panel;
  }

  public Icon getIcon() {
    return null;
  }

  public String getTitle() {
    return DefaultMarkingPrioritySelectionPanel.getTitle();
  }

  public void saveSettings() {
    FavoritesPlugin.getInstance().setMarkPriority(mMarkingsPanel.getSelectedPriority());
  }

}
