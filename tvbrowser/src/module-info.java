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
/**
 * @author René Mach
 */
module tvbrowser {
	exports devplugin;
	exports devplugin.beanshell;
	exports tvbrowser;
	exports util.ui;
	exports util.ui.beanshell;
	exports util.ui.customizableitems;
	exports util.ui.findasyoutype;
	exports util.ui.html;
	exports util.ui.login;
	exports util.ui.menu;
	exports util.ui.persona;
	exports util.ui.progress;
	exports util.ui.table;
	exports util.ui.textcomponentpopup;
	exports util.ui.view;
	exports util.browserlauncher;
	exports util.exc;
	exports util.i18n;
	exports util.io;
	exports util.io.stream;
	exports util.misc;
	exports util.paramhandler;
	exports util.program;
	exports util.programkeyevent;
	exports util.programmouseevent;
	exports util.settings;
	exports util.tvdataservice;
	exports tvdataservice;
	exports tvbrowser.core.plugin;
	opens tvbrowser.ui.mainframe;
	exports tvbrowser.ui.pluginview;
	exports tvbrowser.core;
	exports tvbrowser.core.icontheme;
	exports tvbrowser.core.tvdataservice;
	exports tvbrowser.core.filters;
	exports tvbrowser.core.filters.filtercomponents;
	exports tvbrowser.extras.reminderplugin;
	exports tvbrowser.ui.settings.channel;
	exports tvbrowser.ui.pluginview.contextmenu;
	exports tvbrowser.ui.settings.util;
	
	requires transitive java.rmi;
	requires transitive java.desktop;
	requires transitive java.logging;
	requires transitive java.management;
	requires transitive java.naming;
	requires transitive org.apache.commons.lang3;
	requires transitive org.apache.commons.codec;
	requires transitive bsh.core;
	requires transitive commons.net;
	requires transitive htmlparser;
	requires transitive jgoodies.common;
	requires transitive jgoodies.forms;
	requires jgoodies.looks;
	requires l2fprod.common.tasks;
	requires jnativehook;
	requires transitive texhyphj;
	requires opencsv;
}