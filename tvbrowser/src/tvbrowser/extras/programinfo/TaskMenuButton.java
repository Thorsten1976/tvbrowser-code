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
 *
 * CVS information:
 *  $RCSfile$
 *   $Source$
 *     $Date$
 *   $Author$
 * $Revision$
 */

package tvbrowser.extras.programinfo;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;

import util.ui.findasyoutype.TextComponentFindAction;

import com.l2fprod.common.swing.JTaskPane;
import com.l2fprod.common.swing.JTaskPaneGroup;

import devplugin.ActionMenu;
import devplugin.Program;

/**
 * A class that holds a ContextMenuAction of a Plugin in button.
 * 
 * @author Ren� Mach
 * 
 */
public class TaskMenuButton extends MouseAdapter implements ActionListener {

  private JButton mButton;
  private Action mAction;
  private ProgramInfoDialog mInfo;
  private TextComponentFindAction mFind;

  /**
   * 
   * @param root
   *          The root JTaskPane.
   * @param parent
   *          The parent JTaskPaneGroup
   * @param program
   *          The Program for the Action.
   * @param menu
   *          The ActionMenu.
   * @param info
   *          The ProgramInfoDialog.
   * @param id
   *          The id of the Plugin.
   * @param comp The Text Component find action to register the keyListener on.
   */
  public TaskMenuButton(JTaskPane root, JTaskPaneGroup parent, Program program,
      ActionMenu menu, ProgramInfoDialog info, String id, TextComponentFindAction comp) {
    mInfo = info;
    mFind = comp;

    if (!menu.hasSubItems())
      addButton(parent, menu);
    else
      addTaskPaneGroup(root, parent, program, menu, info, id);
  }
  
    // Adds the button to the TaskPaneGroup.
  private void addButton(JTaskPaneGroup parent, ActionMenu menu) {
    mAction = menu.getAction();

    mButton = new JButton("<html>" + (String) mAction.getValue(Action.NAME) + "</html>");
    mButton.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
    mButton.setHorizontalAlignment(JButton.LEFT);
    mButton.setVerticalTextPosition(JButton.TOP);
    
    mFind.installKeyListener(mButton);

    if (mAction.getValue(Action.SMALL_ICON) != null)
      mButton.setIcon((Icon) mAction.getValue(Action.SMALL_ICON));

    mButton.addActionListener(this);
    mButton.addMouseListener(this);
    mButton.setOpaque(false);
    parent.add(mButton);
  }

    /* Adds a new TaskPaneGroup to the parent TaskPaneGroup
     * for an ActionMenu with submenus.
     */ 
  private void addTaskPaneGroup(JTaskPane root, JTaskPaneGroup parent,
      Program program, ActionMenu menu, ProgramInfoDialog info, final String id) {
    ActionMenu[] subs = menu.getSubItems();

    final JTaskPaneGroup group = new JTaskPaneGroup();
    group.setTitle((String) menu.getAction().getValue(Action.NAME));
    group.setExpanded(ProgramInfo.getInstance().getExpanded(id));
    mFind.installKeyListener(group);
    
     /* Listener to get expand state changes and store the
      * state in the Properties for the Plguin.
      */
    group.addPropertyChangeListener(new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent evt) {
        ProgramInfo.getInstance().setExpanded(id, group.isExpanded());
      }
    });

    if (menu.getAction().getValue(Action.SMALL_ICON) != null)
      group.setIcon((Icon) menu.getAction().getValue(Action.SMALL_ICON));

    for (int i = 0; i < subs.length; i++)
      new TaskMenuButton(root, group, program, subs[i], info, id, mFind);

    parent.add(Box.createRigidArea(new Dimension(0, 10)));
    parent.add(group);
    parent.add(Box.createRigidArea(new Dimension(0, 5)));
    
  }

  public void mouseEntered(MouseEvent e) {
    mButton.setBorder(BorderFactory.createLineBorder(mButton.getForeground()
        .brighter()));
  }

  public void mouseExited(MouseEvent e) {
    mButton.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
  }

  public void actionPerformed(ActionEvent e) {
    mAction.actionPerformed(new ActionEvent(new JButton(),
        ActionEvent.ACTION_PERFORMED, (String) mAction
            .getValue(Action.ACTION_COMMAND_KEY)));
    if(mAction.getValue(Action.ACTION_COMMAND_KEY) == null || !mAction.getValue(Action.ACTION_COMMAND_KEY).equals("action"))
      mInfo.addPluginActions(true);
  }
}
