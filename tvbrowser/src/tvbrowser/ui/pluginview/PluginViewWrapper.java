/*
 * TV-Browser
 * Copyright (C) 2012 TV-Browser team (dev@tvbrowser.org)
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
 *     $Date$
 *   $Author$
 * $Revision$
 */
package tvbrowser.ui.pluginview;

import javax.swing.JPanel;

import tvbrowser.ui.mainframe.MenuBar;
import tvbrowser.ui.mainframe.actions.TVBrowserActions;
import devplugin.PluginCenterPanel;

/**
 * A wrapper class for the plugin view,
 * used for the new center panel function since version 3.2.
 * <p>
 * @author René Mach
 * @since 3.2.1
 */
public class PluginViewWrapper extends PluginCenterPanel {
  private PluginView mPluginView = new PluginView();
  
  public PluginViewWrapper(PluginView view) {
    mPluginView = view;
    setIcon(TVBrowserActions.pluginView.getIcon());
  }
  
  @Override
  public String getName() {
    return MenuBar.mLocalizer.msg("menuitem.pluginOverview",
        "Plugin overview").replace("&", "");
  }

  @Override
  public JPanel getPanel() {
    return mPluginView;
  }
}
