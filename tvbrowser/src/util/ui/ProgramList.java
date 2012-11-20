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
package util.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPopupMenu;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import tvbrowser.core.plugin.PluginProxy;
import tvbrowser.core.plugin.PluginProxyManager;
import tvbrowser.core.plugin.PluginStateListener;
import util.exc.TvBrowserException;
import util.settings.PluginPictureSettings;
import util.settings.ProgramPanelSettings;
import devplugin.ContextMenuIf;
import devplugin.Date;
import devplugin.Plugin;
import devplugin.PluginManager;
import devplugin.Program;

/**
 * This Class extends a JList for showing Programs
 */
public class ProgramList extends JList implements ChangeListener,
    ListDataListener, PluginStateListener {
  private final static Localizer mLocalizer = Localizer.getLocalizerFor(ProgramList.class);
  /** Key for separator list entry */
  public final static String DATE_SEPARATOR = "DATE_SEPARATOR";

  private Vector<Program> mPrograms = new Vector<Program>();
  private boolean mSeparatorsCreated = false;

  /**
   * Creates the JList and adds the default MouseListeners (PopUpBox)
   * 
   * @param programArr
   *          Array of Programs to show
   */
  public ProgramList(Vector<Program> programArr) {
    this(programArr, new PluginPictureSettings(
        PluginPictureSettings.ALL_PLUGINS_SETTINGS_TYPE));
  }

  /**
   * Creates the JList and adds the default MouseListeners (PopUpBox)
   * 
   * @param programArr
   *          Array of Programs to show
   */
  public ProgramList(Program[] programArr) {
    this(programArr, new PluginPictureSettings(
        PluginPictureSettings.ALL_PLUGINS_SETTINGS_TYPE));
  }

  /**
   * Creates the JList and adds the default MouseListeners (PopUpBox)
   * 
   * @param programs
   *          Model with Programs to show
   */
  public ProgramList(ListModel programs) {
    this(programs, new PluginPictureSettings(
        PluginPictureSettings.ALL_PLUGINS_SETTINGS_TYPE));
  }
  
  /**
   * Creates the JList and adds the default MouseListeners (PopUpBox)
   * 
   * @param programVector
   *          Array of Programs to show
   * @param settings
   *          The settings for the program panel.
   * 
   * @since 2.2.2
   */
  public ProgramList(Vector<Program> programVector,
      ProgramPanelSettings settings) {
    super(programVector);
    initialize(settings);
  }

  private void initialize(ProgramPanelSettings settings) {
    setCellRenderer(new ProgramListCellRenderer(settings));
    setToolTipText("");
  }

  /**
   * Creates the JList and adds the default MouseListeners (PopUpBox)
   * 
   * @param programArr
   *          Array of Programs to show
   * @param settings
   *          The settings for the program panel.
   * 
   * @since 2.2.2
   */
  public ProgramList(Program[] programArr, ProgramPanelSettings settings) {
    super(programArr);
    initialize(settings);
  }

  /**
   * Creates the JList and adds the default MouseListeners (PopUpBox)
   * 
   * @param programs
   *          Model with Programs to show
   * @param settings
   *          The settings for the program panel.
   * 
   * @since 2.2.2
   */
  public ProgramList(ListModel programs, ProgramPanelSettings settings) {
    super(programs);
    programs.addListDataListener(this);
    initialize(settings);
  }

  /**
   * Creates the JList and adds the default MouseListeners (PopUpBox)
   * 
   * @param programVector
   *          Array of Programs to show
   * @param settings
   *          The plugin picture settings for the program panel.
   * 
   * @since 2.6
   */
  public ProgramList(Vector<Program> programVector,
      PluginPictureSettings settings) {
    this(programVector, new ProgramPanelSettings(settings, false));
  }

  /**
   * Creates the JList and adds the default MouseListeners (PopUpBox)
   * 
   * @param programArr
   *          Array of Programs to show
   * @param settings
   *          The plugin picture settings for the program panel.
   * 
   * @since 2.6
   */
  public ProgramList(Program[] programArr, PluginPictureSettings settings) {
    this(programArr, new ProgramPanelSettings(settings, false));
  }
  
  /**
   * Creates the JList and adds the default MouseListeners (PopUpBox)
   * 
   * @param programs
   *          Model with Programs to show
   * @param settings
   *          The plugin picture settings for the program panel.
   * 
   * @since 2.6
   */
  public ProgramList(ListModel programs, PluginPictureSettings settings) {
    this(programs, new ProgramPanelSettings(settings, false));
  }
  
  /**
   * Add a ChangeListener to all Programs for repainting
   */
  public void addNotify() {
    super.addNotify();
    removeFromPrograms();
    addToPrograms();
    PluginProxyManager.getInstance().addPluginStateListener(this);
  }

  /**
   * Remove a ChangeListener to all Programs
   */
  public void removeNotify() {
    super.removeNotify();
    removeFromPrograms();
    PluginProxyManager.getInstance().removePluginStateListener(this);
  }

  private void removeFromPrograms() {
    removeFromPrograms(0, mPrograms.size() - 1);
  }

  private void removeFromPrograms(int indexFirst, int indexLast) {
    synchronized(mPrograms) {
      while(indexFirst >= mPrograms.size()) {
        indexFirst = mPrograms.size() - 1;
      }
      while(indexLast >= mPrograms.size()) {
        indexLast = mPrograms.size() - 1;
      }
      if(indexFirst >= 0) {
        for (int i = indexLast; i >= indexFirst; i--) {
          mPrograms.remove(i).removeChangeListener(this);
        }
      }
    }
  }

  private void addToPrograms() {
    ListModel list = getModel();
    addToPrograms(0, list.getSize() - 1);
  }

  private void addToPrograms(int indexFirst, int indexLast) {
    ListModel list = getModel();
    for (int i = indexFirst; i <= indexLast; i++) {
      Object element = list.getElementAt(i);
      if (element instanceof Program) {
        Program prg = (Program) element;
        prg.addChangeListener(this);
        mPrograms.add(prg);
      }
    }
  }

  /**
   * Add a Mouse-Listener for the Popup-Box
   * 
   * The caller ContextMenuIfs menus are not shown, if you want to have all
   * available menus just use <code>null</code> for caller.
   * 
   * @param caller
   *          The ContextMenuIf that called this.
   */
  public void addMouseListeners(final ContextMenuIf caller) {
    addMouseListener(new MouseAdapter() {
      private Thread mLeftSingleClickThread;
      private Thread mMiddleSingleClickThread;
      private boolean mPerformingSingleClick = false;
      private boolean mPerformingSingleMiddleClick = false;
      
      public void mousePressed(MouseEvent e) {
        if (e.isPopupTrigger()) {
          showPopup(e, caller);
        }
      }

      public void mouseReleased(MouseEvent e) {
        if (e.isPopupTrigger()) {
          showPopup(e, caller);
        }
      }

      public void mouseClicked(final MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e) && (e.getClickCount() == 1) && (e.getModifiersEx() == 0 || e.getModifiersEx() == InputEvent.CTRL_DOWN_MASK)) {
          mLeftSingleClickThread = new Thread("Single left click") {
          	int modifiers = e.getModifiersEx();
            public void run() {
              try {
                mPerformingSingleClick = false;
                Thread.sleep(Plugin.SINGLE_CLICK_WAITING_TIME);
                mPerformingSingleClick = true;
                
                if (modifiers == 0) {
                	Plugin.getPluginManager().handleProgramSingleClick(getProgramFromEvent(e), caller);
                }
                else if (modifiers == InputEvent.CTRL_DOWN_MASK) {
                	Plugin.getPluginManager().handleProgramSingleCtrlClick(getProgramFromEvent(e), caller);
                }
                mPerformingSingleClick = false;
              } catch (InterruptedException e) {
                // ignore
              }
            }

          };
          mLeftSingleClickThread.setPriority(Thread.MIN_PRIORITY);
          mLeftSingleClickThread.start();
        }
        else if (SwingUtilities.isLeftMouseButton(e) && (e.getClickCount() == 2) && e.getModifiersEx() == 0) {
          if(!mPerformingSingleClick && mLeftSingleClickThread != null && mLeftSingleClickThread.isAlive()) {
            mLeftSingleClickThread.interrupt();
          }
          
          if(!mPerformingSingleClick) {
            Plugin.getPluginManager().handleProgramDoubleClick(getProgramFromEvent(e), caller);
          }
        }
        else if (SwingUtilities.isMiddleMouseButton(e) && (e.getClickCount() == 1)) {
          mMiddleSingleClickThread = new Thread("Single middle click") {
            public void run() {
              try {
                mPerformingSingleMiddleClick = false;
                Thread.sleep(Plugin.SINGLE_CLICK_WAITING_TIME);
                mPerformingSingleMiddleClick = true;
                
                Plugin.getPluginManager().handleProgramMiddleClick(getProgramFromEvent(e), caller);
                
                mPerformingSingleMiddleClick = false;
              } catch (InterruptedException e) {
                // ignore
              }
            }
          };
          mMiddleSingleClickThread.setPriority(Thread.MIN_PRIORITY);
          mMiddleSingleClickThread.start();
        }
        else if (SwingUtilities.isMiddleMouseButton(e) && (e.getClickCount() == 2)) {
          if(!mPerformingSingleMiddleClick && mMiddleSingleClickThread != null && mMiddleSingleClickThread.isAlive()) {
            mMiddleSingleClickThread.interrupt();
          }
          
          if(!mPerformingSingleMiddleClick) {
            Plugin.getPluginManager().handleProgramMiddleDoubleClick(getProgramFromEvent(e), caller);
          }
        }
      }
      
			private Program getProgramFromEvent(MouseEvent e) {
        final int inx = locationToIndex(e.getPoint());
        if (inx >= 0) {
          final Object element = ProgramList.this.getModel()
          .getElementAt(inx);

          if(element instanceof Program) {
          	return (Program) element;
          }
        }
        return null;
			}
    });
  }

  /**
   * Shows the Popup
   * 
   * @param e
   *          MouseEvent for X/Y Coordinates
   * @param caller
   *          The ContextMenuIf that called this
   */
  private void showPopup(MouseEvent e, ContextMenuIf caller) {
    PluginManager mng = Plugin.getPluginManager();

    int inx = locationToIndex(e.getPoint());
    setSelectedIndex(inx);

    if (getModel().getElementAt(inx) instanceof Program) {
      Program prog = (Program) getModel().getElementAt(inx);
      JPopupMenu menu = mng.createPluginContextMenu(prog, caller);
      menu.show(ProgramList.this, e.getX() - 15, e.getY() - 15);
    }
  }

  public void stateChanged(ChangeEvent e) {
    repaint();
  }

  public void contentsChanged(ListDataEvent e) {
    removeFromPrograms();
    addToPrograms();
  }

  public void intervalAdded(ListDataEvent e) {
    addToPrograms(e.getIndex0(), e.getIndex1());
  }

  public void intervalRemoved(ListDataEvent e) {
    removeFromPrograms(e.getIndex0(), e.getIndex1());
  }

  /**
   * @return The selected programs;
   * @since 2.2
   */
  public Program[] getSelectedPrograms() {
    Object[] o = getSelectedValues();

    if (o == null || o.length == 0) {
      return null;
    }

    if(mSeparatorsCreated) {
      ArrayList<Program> progs = new ArrayList<Program>(o.length);
      
      for(Object p : o) {
        if(p instanceof Program) {
          progs.add((Program)p);
        }
      }
      
      return progs.toArray(new Program[progs.size()]);
    }
    else {
      Program[] p = new Program[o.length];
      
      for (int i = 0; i < o.length; i++) {
        p[i] = (Program) o[i];
      }
  
      return p;
    }
  }
  
  public void pluginActivated(PluginProxy plugin) {
    if (plugin.getProgramTableIcons(Plugin.getPluginManager().getExampleProgram()) != null) {
      updatePrograms();
    }
  }

  public void pluginDeactivated(PluginProxy plugin) {
    updatePrograms();
  }

  private void updatePrograms() {
    repaint();
  }

  public void pluginLoaded(PluginProxy plugin) {
    // noop
  }

  public void pluginUnloaded(PluginProxy plugin) {
    // noop
  }

  @Override
  public String getToolTipText(MouseEvent event) {
    final Point point = event.getPoint();
    int index = locationToIndex(point);
    if (index >= 0) {
      Rectangle bounds = getCellBounds(index, index);
      if (bounds != null) {
        int x = point.x - bounds.x;
        int y = point.y - bounds.y;
        Component component = getCellRenderer()
            .getListCellRendererComponent(this, mPrograms.elementAt(index),
                index, false, false);
        if (component != null && component instanceof Container) {
          Container container = (Container) component;
          component = container.getComponent(1);
          if (component != null && component instanceof ProgramPanel) {
            ProgramPanel panel = (ProgramPanel) component;
            x -= panel.getX();
            y -= panel.getY();
            return panel.getToolTipText(x, y);
          }
        }
      }
    }
    // mouse is over an empty part of the list
    return null;
  }
  
  
  /**
   * Adds date separators to this list.
   * This needs to be called every time the list elements are changed.
   * <p>
   * @throws TvBrowserException Thrown if used ListModel is not {@link #javax.swing.DefaultListModel} or a child class of it.
   * @since 3.2.2
   */
  public void addDateSeparators() throws TvBrowserException {
    if(getModel() instanceof DefaultListModel) {
      mSeparatorsCreated = true;
      
      DefaultListModel newModel = new DefaultListModel();
      
      Program previous = null;
      
      for(int i = 0; i < getModel().getSize(); i++) {
        Object o = getModel().getElementAt(i);
        
        if(o instanceof Program) {
          Program prog = (Program) o;
          
          if(previous == null || prog.getDate().compareTo(previous.getDate()) > 0) {
            newModel.addElement(DATE_SEPARATOR);
          }
          
          newModel.addElement(prog);
          
          previous = prog;
        }
      }
      
      super.setModel(newModel);
    }
    else {
      throw new TvBrowserException(ProgramList.class, "unsupportedListModel", "Used ListModel not supported.");
    }
  }
  
  public void setModel(ListModel model) {
    mSeparatorsCreated = false;
    super.setModel(model);
  }
  
  /**
   * Scrolls the list to next day from
   * the current view position (if next
   * day is available)
   * <p>
   * @since 3.2.2
   */
  public void scrollToNextDayIfAvailable() {
    int index = locationToIndex(getVisibleRect().getLocation());
    
    if(index < getModel().getSize() - 1) {
      Object o = super.getModel().getElementAt(index);
      
      if(o instanceof String) {
        o = super.getModel().getElementAt(index+1);
        index++;
      }
      
      if(index < super.getModel().getSize() - 1) {
        Date current = ((Program)o).getDate();
        
        for(int i = index + 1; i < super.getModel().getSize(); i++) {
          Object test = super.getModel().getElementAt(i);
          
          if(test instanceof Program && current.compareTo(((Program)test).getDate()) < 0) {
            Point p = indexToLocation(i-(mSeparatorsCreated ? 1 : 0));
            super.scrollRectToVisible(new Rectangle(p.x,p.y,1,getVisibleRect().height));
            return;
          }
        }            
      }
    }
  }
  
  /**
   * Scrolls the list to previous day from
   * the current view position (if previous
   * day is available)
   * <p>
   * @since 3.2.2
   */
  public void scrollToPreviousDayIfAvailable() {
    int index = locationToIndex(getVisibleRect().getLocation())-1;
    
    if(index > 0) {
      Object o = super.getModel().getElementAt(index);
      
      if(o instanceof String) {
        o = super.getModel().getElementAt(index-1);
        index--;
      }
      
      if(index > 0) {
        Date current = ((Program)o).getDate();
        
        for(int i = index-1; i >= 0; i--) {
          Object test = super.getModel().getElementAt(i);
          
          if(test instanceof Program && current.compareTo(((Program)test).getDate()) > 0) {
            super.ensureIndexIsVisible(i+1);
            return;
          }
        }
      }
    }
    
    if(getModel().getSize() > 0) {
      super.ensureIndexIsVisible(0);
    }
  }
  
  /**
   * Gets the new index of a row after adding of date separators.
   * <p>
   * @param index The old index of the row.
   * @return The new index or the given index if no separators were added.
   * @since 3.2.2
   */
  public int getNewIndexForOldIndex(int index) {
    if(mSeparatorsCreated) {
      for(int i = 0; i < index; i++) {
        if(getModel().getElementAt(i) instanceof String) {
          index++;
        }
      }
    }
    
    return index;
  }
  
  /**
   * @return The tool tip text for the previous scroll action,
   */
  public static String getPreviousActionTooltip() {
    return mLocalizer.msg("prevTooltip", "Scrolls to previous day from current view position (if there is previous day in the list)");
  }
  
  /**
   * @return The tool tip text for the next scroll action,
   */
  public static String getNextActionTooltip() {
    return mLocalizer.msg("nextTooltip", "Scrolls to next day from current view position (if there is next day in the list)");
  }
}