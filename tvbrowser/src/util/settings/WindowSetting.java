/*
* TV-Browser
* Copyright (C) 04-2003 Martin Oberhauser (darras@users.sourceforge.net)
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
package util.settings;

import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import util.ui.UiUtilities;

/**
 * A class with the position and size settings for a window.
 *
 * @author René Mach
 * @since 2.7
 */
public final class WindowSetting {
  private Window mWindowCache;

  private int mXPos;
  private int mYPos;
  private int mWidth;
  private int mHeight;

  private Dimension mMinSize;

  private int mExtendedState;
  
  private boolean mIgnoreAndMinSizeLocation;

  /**
   * Creates an instance of this class with the values read from the stream.
   *
   * @param in The stream to read the settings from.
   * @throws IOException Thrown if something went wrong.
   */
  public WindowSetting(ObjectInputStream in) throws IOException {
    final int version = in.readInt(); // read version;

    mXPos = in.readInt();
    mYPos = in.readInt();
    mWidth = in.readInt();
    mHeight = in.readInt();
    
    if(version > 1) {
      mExtendedState = in.readInt();
    }
    
    if(version > 2) {
      mIgnoreAndMinSizeLocation = in.readBoolean();
    }
  }

  /**
   * Creates an instance of this class with the default setting -1 for all values.
   * @param size The default size of the window.
   */
  public WindowSetting(Dimension size) {
    mXPos = -1;
    mYPos = -1;

    if(size == null) {
      mWidth = -1;
      mHeight = -1;
    }
    else {
      mWidth = size.width;
      mHeight = size.height;
    }
    
    mExtendedState = JFrame.NORMAL;
    mIgnoreAndMinSizeLocation = false;
  }

  /**
   * Saves the values in the given stream.
   *
   * @param out The stream to save the values in.
   * @throws IOException Thrown if an IO operation went wrong.
   */
  public void saveSettings(ObjectOutputStream out) throws IOException {
    out.writeInt(3); // write version

    out.writeInt(mXPos);
    out.writeInt(mYPos);
    out.writeInt(mWidth);
    out.writeInt(mHeight);
    
    out.writeInt(mExtendedState);
    out.writeBoolean(mIgnoreAndMinSizeLocation);
  }
  
  /**
   * Sets the values to the given window.
   *
   * @param window The window to set the values for.
   */
  public void layout(final Window window) {
    layout(window, null);
  }
  
  /**
   * Sets the values to the given window.
   *
   * @param window The window to set the values for.
   * @param parent The parent window of the window to layout (if not <code>null</code> the window is placed relative to it.)
   * @since 3.3
   */
  public void layout(final Window window, final Window parent) {
    final Rectangle d = window.getGraphicsConfiguration().getBounds();
    
    int width = mWidth;
    int height = mHeight;

    if(width < 20 || width > (d.x + d.width)) {
      window.pack();
      width = window.getWidth();
    }

    if(height < 20 || height > (d.y + d.height)) {
      window.pack();
      height = window.getHeight();
    }

    // never make the dialog smaller than minimum size
    window.pack();
    mMinSize = window.getMinimumSize();    
    mMinSize = new Dimension(Math.min(mMinSize.width, d.width),Math.min(mMinSize.height, d.height));
    
    width = Math.max(width, mMinSize.width);
    height = Math.max(height, mMinSize.height);

    // assure that dialog is not larger than screen
    width = Math.min(width, d.width);
    height = Math.min(height, d.height);

    window.setSize(width, height);
    
    boolean locationSet = false;

    if(mXPos < 0 || mYPos < 0 || mXPos > (d.x + d.width) || mYPos > (d.y + d.height)) {
      UiUtilities.centerAndShow(window, false);
    }
    else {
      window.setLocation(mXPos, mYPos);
      locationSet = true;
    }
    
    if(mExtendedState == JFrame.MAXIMIZED_BOTH) {
      if(window instanceof JFrame) {
        ((JFrame) window).setExtendedState(mExtendedState);
      }
    }
    
    if(mWindowCache == null || !window.equals(mWindowCache)) {
      window.addComponentListener(new ComponentAdapter() {
    	private Thread mSavePosWait;
        private AtomicBoolean mWaitSavePos = new AtomicBoolean(false);
          
        private Thread mSaveSizeWait;
        private AtomicBoolean mWaitSaveSize = new AtomicBoolean(false);
        
        private int getExtendedState(ComponentEvent e) {
          mExtendedState = JFrame.NORMAL;
        
          if(window instanceof JFrame) {
            mExtendedState = ((JFrame) window).getExtendedState();
          }
          else if(window instanceof JDialog && e.getComponent().getSize().equals(GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().getSize())) {
            mExtendedState = JFrame.MAXIMIZED_BOTH;
          }
        
          return mExtendedState;
        }
        
        private void savePos(ComponentEvent e) {
            mWaitSavePos.set(true);
            
            if(mSavePosWait == null || !mSavePosWait.isAlive()) {
              mSavePosWait = new Thread("SAVE WINDOW POSITION WAITING THREAD") {
                @Override
                public void run() {
                  while(mWaitSavePos.getAndSet(false)) {
                    try {
                      sleep(100);
                    } catch (InterruptedException e) {
                      // ignore
                    }
                  }
                  
                  if((getExtendedState(e) & JFrame.MAXIMIZED_BOTH) != JFrame.MAXIMIZED_BOTH) {
                    mXPos = e.getComponent().getX();
                    mYPos = e.getComponent().getY();
                  }
                }
              };
              mSavePosWait.start();
            }                  
        }
        
        private void saveSize(ComponentEvent e) {
            mWaitSaveSize.set(true);
            
            if(mSaveSizeWait == null || !mSaveSizeWait.isAlive()) {
              mSaveSizeWait = new Thread("SAVE WINDOW SIZE WAITING THREAD") {
                @Override
                public void run() {
                  while(mWaitSaveSize.getAndSet(false)) {
                    try {
                      sleep(100);
                    } catch (InterruptedException e) {
                      // ignore
                    }
                  }
                                          
                  if((getExtendedState(e) & JFrame.MAXIMIZED_BOTH) != JFrame.MAXIMIZED_BOTH) {
                    mWidth = e.getComponent().getWidth();
                    mHeight = e.getComponent().getHeight();
                  }
                }
              };
              mSaveSizeWait.start();
            }
        }

        @Override
        public void componentHidden(ComponentEvent e) {
          savePos(e);
        }
        
        @Override
        public void componentMoved(ComponentEvent e) {
          savePos(e);
        }
        
        public void componentResized(ComponentEvent e) {
            if (mMinSize != null && !mIgnoreAndMinSizeLocation) {
              int winWidth = window.getWidth();
              int winHeight = window.getHeight();
              boolean resize = false;
              if (winWidth < mMinSize.getWidth()) {
                winWidth = mMinSize.width;
                resize = true;
              }
              if (winHeight < mMinSize.getHeight()) {
                winHeight = mMinSize.height;
                resize = true;
              }
              if (resize) {
                window.setSize(winWidth, winHeight);
              }
            }
            saveSize(e);
          }
        
	    @Override
	    public void componentShown(ComponentEvent e) {
          SwingUtilities.invokeLater(new Thread() {
            public void run() {
              try {
                sleep(100);
              } catch (InterruptedException e1) {}
              
              if(parent == null && !mIgnoreAndMinSizeLocation) {
                Point p = e.getComponent().getLocation();
                
                if(mXPos < 0 || mYPos < 0 || mXPos > (d.x + d.width) || mYPos > (d.y + d.height)) {
                  UiUtilities.centerAndShow(window, false);
                }
                else if((p.x != mXPos || mYPos != p.y) && System.getenv("DESKTOP_SESSION") != null &&  System.getenv("DESKTOP_SESSION").toLowerCase().equals("ubuntu")) {
                  window.setLocation(mXPos, mYPos);
                }
              }
              else {
            	Point p = e.getComponent().getLocation();
            	
            	if((mXPos >= 0 || mYPos >= 0) && (p.x != mXPos || mYPos != p.y) && System.getenv("DESKTOP_SESSION") != null &&  System.getenv("DESKTOP_SESSION").toLowerCase().equals("ubuntu")) {
                  window.setLocation(mXPos, mYPos);
                }
              }
            }
          });
        }
      });
    }
    
    if(parent != null && !mIgnoreAndMinSizeLocation && !locationSet) {
      window.setLocationRelativeTo(parent);
    }
    
    mWindowCache = window;
  }


  public String toString() {
    return new StringBuilder("x:").append(mXPos).append(" y:").append(mYPos)
        .append(" ").append(mWidth).append("x").append(mHeight).toString();
  }
  
  public void setIgnoreAndMinSizeLocation(boolean ignoreAndMinSizeLocation) {
    mIgnoreAndMinSizeLocation = ignoreAndMinSizeLocation;
  }
}

