/**
 * 
 */
/**
 * @author René Mach
 *
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
	exports tvbrowser.extras.reminderplugin;
	
	requires java.rmi;
	requires java.desktop;
	requires java.logging;
	requires java.management;
	requires org.apache.commons.lang3;
	requires org.apache.commons.codec;
	requires bsh.core;
	requires commons.net;
	requires htmlparser;
	requires jgoodies.common;
	requires jgoodies.forms;
	requires jgoodies.looks;
	requires l2fprod.common.tasks;
	requires jnativehook;
	requires texhyphj;
	requires opencsv;
}