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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package tvraterplugin;

import java.awt.event.ComponentEvent;

import util.ui.UiUtilities;
import devplugin.PluginInfo;
import devplugin.Program;
import devplugin.SettingsTab;
import devplugin.Version;

/**
 * This Plugin gives the User the possibility to rate a Movie
 *
 * @author Bodo Tasche
 */
public class TVRaterPlugin extends devplugin.Plugin {
	private java.awt.Point location = null;
	private java.awt.Dimension size = null;

	private static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(TVRaterPlugin.class);

	public String getContextMenuItemText() {
		return mLocalizer.msg("contextMenuText", "View rating");
	}

	public PluginInfo getInfo() {
		String name = mLocalizer.msg("pluginName", "TV Rater");
		String desc = mLocalizer.msg("description", "Gives the User the possibility to rate a Show/Movie and get ratings from other Users");
		String author = "Bodo Tasche";
		return new PluginInfo(name, desc, author, new Version(1, 0));
	}

	public String getButtonText() {
		return null;
	}

	public void execute(Program program) {
		TVRateDialog dlg = new TVRateDialog(parent, program);
		dlg.pack();
		dlg.addComponentListener(new java.awt.event.ComponentAdapter() {

			public void componentMoved(ComponentEvent e) {
				e.getComponent().getLocation(location);
			}

			public void componentResized(ComponentEvent e) {
				e.getComponent().getSize(size);
			}

		});

		if (size != null) {
			dlg.setSize(size);
		}
		if (location != null) {
			dlg.setLocation(location);
			dlg.show();
		} else {
			UiUtilities.centerAndShow(dlg);
			size = dlg.getSize();
			location = dlg.getLocation();
		}
	}

	public SettingsTab getSettingsTab() {
	  return new TVRaterSettingsTab();
	}
	
	public String getMarkIconName() {
		return "googlesearchplugin/google.gif";
	}

	public String getButtonIconName() {
		return null;
	}

}