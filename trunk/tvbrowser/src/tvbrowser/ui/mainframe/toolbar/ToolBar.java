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

package tvbrowser.ui.mainframe.toolbar;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.dnd.InvalidDnDOperationException;
import java.awt.event.*;
import java.io.IOException;
import java.util.HashMap;

import javax.swing.*;

import tvbrowser.ui.PictureButton;
import tvbrowser.ui.ToggleButton;
import tvbrowser.core.Settings;
import devplugin.Plugin;

public class ToolBar extends JToolBar {




  public static final String ACTION_VALUE = "ActionValue";
  public static final String ACTION_TYPE_KEY = "ActionType";
  public static final String ACTION_ID_KEY = "ActionId";
  public static final String ACTION_IS_SELECTED = "ActionIsSelected";

  public static final int BUTTON_ACTION = 0;
  public static final int TOOGLE_BUTTON_ACTION = 1;
  public static final int SEPARATOR = 2;

  public static final int STYLE_TEXT = 1, STYLE_ICON = 2;
  private static final int ICON_BIG = 1, ICON_SMALL = 2;

  private static Insets NULL_INSETS = new Insets(0, 0, 0, 0);
  private static Font TEXT_FONT = new Font("Dialog", Font.PLAIN, 10);



  private ToolBarModel mModel;
  private ContextMenu mContextMenu;
  private int mStyle;
  private int mIconSize;
  private String mLocation;

  public ToolBar(ToolBarModel model) {
    super();
    mModel = model;
    loadSettings();
    mContextMenu = new ContextMenu(this);
    setFloatable(false);
    update();
    addMouseListener(new MouseAdapter(){
      public void mouseClicked(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e)) {
          mContextMenu.show(e.getX(), e.getY());
        }
      }
    });
  }


  public void update() {
    super.removeAll();
    Action[] actions = mModel.getActions();
    for (int i=0; i<actions.length; i++) {
      Action action = actions[i];
      Integer typeInteger = (Integer)action.getValue(ACTION_TYPE_KEY);
      int type = -1;
      if (typeInteger != null) {
        type = typeInteger.intValue();
      }

      if (type == TOOGLE_BUTTON_ACTION) {
        addToggleButton(action);
      }
      else if (type == SEPARATOR) {
        addSeparator();
      }
      else {
        addButton(action);
      }

    }


    updateUI();
  }



  private void addToggleButton(Action action) {
    final JToggleButton button = new JToggleButton(action);
    action.putValue(ACTION_VALUE, button);
    addButtonProperties(button, action);
    Boolean isSelected = (Boolean)action.getValue(ACTION_IS_SELECTED);
    if (isSelected!=null) {
      button.setSelected(isSelected.booleanValue());
    }
    button.setBorderPainted(isSelected!=null && isSelected.booleanValue());
    button.addMouseListener(new MouseAdapter () {
      public void mouseEntered(MouseEvent e) {
        if (!button.isSelected()) {
          button.setBorderPainted(true);
        }
      }
      public void mouseExited(MouseEvent e) {
        if (!button.isSelected()) {
          button.setBorderPainted(false);
        }
      }
    });

    add(button);
  }

  private void addButton(final Action action) {
    final JButton button = new JButton();
    addButtonProperties(button, action);
    button.setBorderPainted(false);

    button.addMouseListener(new MouseAdapter () {
      public void mouseEntered(MouseEvent e) {
        button.setBorderPainted(true);
      }
      public void mouseExited(MouseEvent e) {
        button.setBorderPainted(false);
      }
    });

    add(button);
  }

  private void addButtonProperties(final AbstractButton button, final Action action) {
    String tooltip = (String)action.getValue(Action.SHORT_DESCRIPTION);
    Icon icon = getIcon(action);
    String title = getTitle(action);

    button.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        action.actionPerformed(e);
      }
    });



    button.setText(title);
    button.setIcon(icon);
    button.setVerticalTextPosition(SwingConstants.BOTTOM);
    button.setHorizontalTextPosition(SwingConstants.CENTER);
    button.setFont(TEXT_FONT);
    button.setMargin(NULL_INSETS);
    button.setFocusPainted(false);
    button.setToolTipText(tooltip);
  }

  private String getTitle(Action action) {
    if ((mStyle & STYLE_TEXT) == STYLE_TEXT) {
      return (String)action.getValue(Action.NAME);
    }
    return null;
  }

  private Icon getIcon(Action action) {
    if ((mStyle & STYLE_ICON) == STYLE_ICON) {
      Icon icon;
      if (mIconSize == ICON_BIG) {
        icon = (Icon)action.getValue(Plugin.BIG_ICON);
      }
      else {
        icon = (Icon)action.getValue(Action.SMALL_ICON);
      }
      return icon;
    }
    return null;
  }

  public void setStyle(int style) {
    mStyle = style;
  }

  public int getStyle() {
    return mStyle;
  }


  private void loadSettings() {

    String styleStr = Settings.propToolbarButtonStyle.getString();
    if ("text".equals(styleStr)) {
      mStyle = STYLE_TEXT;
    }
    else if ("icon".equals(styleStr)) {
      mStyle = STYLE_ICON;
    }
    else {
      mStyle = STYLE_ICON|STYLE_TEXT;
    }


    setUseBigIcons(Settings.propToolbarUseBigIcons.getBoolean());

    String locationStr = Settings.propToolbarLocation.getString();
    mLocation=null;
    if ("west".equals(locationStr)) {
      mLocation = BorderLayout.WEST;
    }else {
      mLocation = BorderLayout.NORTH;
    }

    if (mLocation == BorderLayout.EAST || mLocation == BorderLayout.WEST) {
      setOrientation(JToolBar.VERTICAL);
    }else {
      setOrientation(JToolBar.HORIZONTAL);
    }

  }

  public void storeSettings() {

    if (mStyle == STYLE_TEXT) {
      Settings.propToolbarButtonStyle.setString("text");
    }
    else if (mStyle == STYLE_ICON) {
      Settings.propToolbarButtonStyle.setString("icon");
    }
    else {
      Settings.propToolbarButtonStyle.setString("text&icon");
    }

    Settings.propToolbarUseBigIcons.setBoolean(mIconSize == ICON_BIG);

    if (mLocation == null) {
      Settings.propToolbarLocation.setString("hidden");
    }
    else if (mLocation == BorderLayout.WEST) {
      Settings.propToolbarLocation.setString("west");
    }
    else {
      Settings.propToolbarLocation.setString("north");
    }



  }

  public void setToolbarLocation(String location) {
    mLocation = location;
  }

  public String getToolbarLocation() {
    return mLocation;
  }

  public void setUseBigIcons(boolean arg) {
    if (arg) {
      mIconSize = ICON_BIG;
    }
    else {
      mIconSize = ICON_SMALL;
    }
  }

  public boolean useBigIcons() {
    return mIconSize == ICON_BIG;
  }



}

