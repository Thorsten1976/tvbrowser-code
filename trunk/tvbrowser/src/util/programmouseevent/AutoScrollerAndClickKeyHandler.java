/*
 * TV-Browser
 * Copyright (C) 2003-2018 TV-Browser-Team (dev@tvbrowser.org)
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
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
 *     $Id$
 *   $Author$
 * $Revision$
 */
package util.programmouseevent;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.JComponent;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;

import devplugin.ContextMenuIf;
import devplugin.Plugin;
import devplugin.Program;

/**
 * A class to support Panning for scrolling.
 * 
 * @author René Mach
 * @since 4.0.2
 */
public class AutoScrollerAndClickKeyHandler {
  private JComponent mScrollComponent;
  private ProgramAutoScrollListener mProgramAutoScrollListener;
  
  private Thread mClickThread;
  
  private Point mDraggingPoint;
  private Point mDraggingPointOnScreen;
  
  private long mLastDragTime;
  private int mLastDragDeltaX;
  private int mLastDragDeltaY;
  
  private Point mAutoScroll;
  private Thread mAutoScrollThread;
  private ProgramMouseEventHandler mProgramMouseEventHandler;
  
  public AutoScrollerAndClickKeyHandler(final JComponent scrollComponent, final ProgramAutoScrollListener listener) {
    mScrollComponent = scrollComponent;
    mProgramAutoScrollListener = listener;
    
    mProgramMouseEventHandler = new ProgramMouseEventHandler(listener, null) {
      public void mousePressed(MouseEvent evt) {
        handleMousePressed(evt);
        
        if(listener.isClickAndContextMenuHandlingEnabled()) {
          super.mousePressed(evt);
        }
      }
      
      public void mouseReleased(MouseEvent evt) {
        handleMouseReleased(evt);
        
        if(listener.isClickAndContextMenuHandlingEnabled()) {
          super.mouseReleased(evt);
        }
      }
      
      public void mouseClicked(MouseEvent evt) {
        final Program program = listener.getProgramAt(evt.getX(), evt.getY());
       
        if (SwingUtilities.isLeftMouseButton(evt) && (evt.getClickCount() == 1) &&
            (evt.isShiftDown() && !evt.isControlDown() && !evt.isAltDown())) {
          if (program != null) {
            if(!listener.isSelectedItemAt(evt.getX(),evt.getY())) {
              listener.selectItemAt(evt.getX(),evt.getY());
            }
            else {
              listener.deSelectItem();
            }
          }
        }
        else if(listener.isClickAndContextMenuHandlingEnabled()) {
          super.mouseClicked(evt);
        }
      }
      
      public void mouseExited(MouseEvent evt) {
        listener.handleMouseExited(evt);
        handleMouseExited(evt);
      }
    };
    
    mScrollComponent.addMouseListener(mProgramMouseEventHandler);
    
    mScrollComponent.addMouseMotionListener(new MouseMotionAdapter() {
      public void mouseDragged(MouseEvent evt) {
        handleMouseDragged(evt);
      }

      public void mouseMoved(MouseEvent evt) {
        handleMouseMoved(evt);
      }
    });
  }
  
  private void handleMousePressed(MouseEvent evt) {
    mProgramAutoScrollListener.handleMousePressed(evt);
    mScrollComponent.requestFocus();

    if(mClickThread == null || !mClickThread.isAlive()) {
      mClickThread = new Thread("AutoScroller Singe Click") {
        public void run() {
          try {
            Thread.sleep(Plugin.SINGLE_CLICK_WAITING_TIME + 50);
            mScrollComponent.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
          } catch (InterruptedException e) {}
        }
      };

      if(!evt.isShiftDown() && SwingUtilities.isLeftMouseButton(evt)) {
        mClickThread.start();
      }
    }

    setDraggingPoint(evt.getPoint());
    setDraggingPointOnScreen(new Point(evt.getXOnScreen(), evt.getYOnScreen()));
  }
  
  public void setOwner(final ContextMenuIf owner) {
    mProgramMouseEventHandler.setOwner(owner);
  }
  
  private void handleMouseReleased(MouseEvent evt) {
    mProgramAutoScrollListener.handleMouseReleased(evt);
    // recognize auto scroll
    if (mDraggingPoint != null
        && mProgramAutoScrollListener.isAutoScrollingEnabled()
        && (System.currentTimeMillis() - mLastDragTime < 20)) {
      if (Math.abs(mLastDragDeltaX) >= 3 || Math.abs(mLastDragDeltaY) >= 3) {
        // stop last scroll, if it is still active
        stopAutoScroll();
        startAutoScroll(new Point(mLastDragDeltaX, mLastDragDeltaY), 2);
      }
    }

    // disable dragging
    setDraggingPoint(null);
    setDraggingPointOnScreen(null);

    if(mClickThread != null && mClickThread.isAlive()) {
      mClickThread.interrupt();
    }

    mScrollComponent.setCursor(Cursor.getDefaultCursor());

    if (SwingUtilities.isMiddleMouseButton(evt)) {
      stopAutoScroll();
    }
  }

  private void handleMouseExited(MouseEvent evt) {
    mProgramAutoScrollListener.handleMouseExited(evt);
    //does nothing here
  }
  
  private void handleMouseDragged(final MouseEvent evt) {
    mProgramAutoScrollListener.handleMouseDragged(evt);
    
    if (getDraggingPoint() != null && !evt.isShiftDown()) {
      if (SwingUtilities.isLeftMouseButton(evt)) {
        stopAutoScroll();
        calcualteLastDragDelta(evt.getX(),evt.getY());
        /*mLastDragDeltaX = mDraggingPoint.x - evt.getX();
        mLastDragDeltaY = mDraggingPoint.y - evt.getY();
        scrollBy(mLastDragDeltaX, mLastDragDeltaY);
        mLastDragTime = System.currentTimeMillis();*/
      } else if (SwingUtilities.isMiddleMouseButton(evt)
          && mDraggingPointOnScreen != null) {
        Point scroll = new Point(evt.getXOnScreen() - mDraggingPointOnScreen.x,
            evt.getYOnScreen() - mDraggingPointOnScreen.y);
        startAutoScroll(scroll, 10);
      }
    }
  }
  
  private void handleMouseMoved(MouseEvent evt) {
    mProgramAutoScrollListener.handleMouseMoved(evt);
  }
  
  public void startAutoScroll(final Point scroll, int scaling) {
    mScrollComponent.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
    
    // decide which direction to scroll
    if (Math.abs(scroll.x) > Math.abs(scroll.y)) {
      scroll.y = 0;
    } else {
      scroll.x = 0;
    }
    // scale the delta
    if (Math.abs(scroll.x) >= scaling) {
      scroll.x = scroll.x / scaling;
    }
    if (Math.abs(scroll.y) >= scaling) {
      scroll.y = scroll.y / scaling;
    }
    mAutoScroll = scroll;
    // now start, if we are not running already
    if (mAutoScrollThread == null) {
      mAutoScrollThread = new Thread("Autoscrolling") {
        @Override
        public void run() {
          while (mAutoScrollThread != null) {
            SwingUtilities.invokeLater(() -> {
              scrollBy(mAutoScroll.x, mAutoScroll.y);
            });
            try {
              sleep(30); // speed of scrolling
            } catch (InterruptedException e) {
              mAutoScrollThread = null;
            }
          }
          mAutoScrollThread = null;
        }
      };
      mAutoScrollThread.start();
    }
  }
  
  public void scrollBy(int deltaX, int deltaY) {
    if (mScrollComponent.getParent() instanceof JViewport) {
      JViewport viewport = (JViewport) mScrollComponent.getParent();
      Point oldViewPos = viewport.getViewPosition();
      Point viewPos = new Point(oldViewPos.x, oldViewPos.y);
      if (deltaX!=0){
        viewPos.x += deltaX;
        int maxX = mScrollComponent.getWidth() - viewport.getWidth();

        viewPos.x = Math.min(viewPos.x, maxX);
        viewPos.x = Math.max(viewPos.x, 0);
      }
      if (deltaY !=0){
        viewPos.y += deltaY;
        int maxY = mScrollComponent.getHeight() - viewport.getHeight();

        viewPos.y = Math.min(viewPos.y, maxY);
        viewPos.y = Math.max(viewPos.y, 0);
      }
      if (viewPos.equals(oldViewPos)) {
        stopAutoScroll();
      } else {
        viewport.setViewPosition(viewPos);
      }
    }
  }
  

  public boolean stopAutoScroll() {
    mScrollComponent.setCursor(Cursor.getDefaultCursor());
    
    if (mAutoScrollThread != null && mAutoScrollThread.isAlive()) {
      mAutoScrollThread.interrupt();
      mAutoScrollThread = null;
      return true;
    }
    return false;
  }
  
  public void setDraggingPoint(Point p) {
    mDraggingPoint = p;
  }
  
  public void setDraggingPointOnScreen(Point p) {
    mDraggingPointOnScreen = p;
  }
  
  public void calcualteLastDragDelta(int x, int y) {
    mLastDragDeltaX = mDraggingPoint.x - x;
    mLastDragDeltaY = mDraggingPoint.y - y;
    mLastDragTime = System.currentTimeMillis();
  }
  
  public Point getDraggingPoint() {
    return mDraggingPoint;
  }
  
  public Point getDraggingPointOnScreen() {
    return mDraggingPointOnScreen;
  }
  
  public int getLastDragDeltaX() {
    return mLastDragDeltaX;
  }
  
  public int getLastDragDeltaY() {
    return mLastDragDeltaY;
  }
  
  public long getLastDragTime() {
    return mLastDragTime;
  }
  
  public boolean isScrolling() {
    return (mAutoScrollThread != null && mAutoScrollThread.isAlive());
  }
  
  public void stopClickIfNecessary() {
    if(mClickThread != null && mClickThread.isAlive()) {
      mClickThread.interrupt();
    }
  }
  
  public static interface ProgramAutoScrollListener extends ProgramMouseAndContextMenuListener {
    public Program getProgramAt(int x, int y);
    public boolean isSelectedItemAt(int x, int y);
    public void selectItemAt(int x, int y);
    public void deSelectItem();
    public void handleMousePressed(MouseEvent evt);
    public void handleMouseReleased(MouseEvent evt);
    public void handleMouseExited(MouseEvent evt);
    public void handleMouseDragged(MouseEvent evt);
    public void handleMouseMoved(MouseEvent evt);
    public boolean isAutoScrollingEnabled();
    public boolean isClickAndContextMenuHandlingEnabled();
  }
}
