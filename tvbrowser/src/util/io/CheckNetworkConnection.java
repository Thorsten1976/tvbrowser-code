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
package util.io;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Frame;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.atomic.AtomicInteger;

import javax.net.ssl.HttpsURLConnection;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import tvbrowser.core.Settings;
import tvbrowser.ui.mainframe.MainFrame;
import util.ui.UiUtilities;

/**
 * Checks the Network and creates a Waiting-Dialog if necessary
 * @since 2.2
 */
class CheckNetworkConnection {
  private static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(CheckNetworkConnection.class);
  
  private boolean mResult = false;

  private JDialog mWaitingDialog;
  
  private AtomicInteger mRunningCount = new AtomicInteger();

  private final static String[] CHECK_URLS = { 
    "https://duckduckgo.com/",
    "http://www.google.com/",
    "http://www.yahoo.com/",
    "http://www.bing.com/"
  };

  /**
   * Check the Network
   * 
   * @return true, if the connection is working
   */
  public boolean checkConnection() {
    // if checking is disabled, always assume existing connection
    if (!Settings.propInternetConnectionCheck.getBoolean()) {
      return true;
    }
    
    mResult = false;
    mRunningCount.set(0);
    
    for(String url : CHECK_URLS) {
      try {
        if(checkConnectionInternal(new URL(url))) {
          hideDialog();
          return true;
        }
      } catch (MalformedURLException e) {}
    }
    
    hideDialog();
    return mResult;
  }

  /**
   * Checks if a internet connection to a specific Server can be established
   * 
   * @param url check this Server
   * @return true, if a connection can be established
   */
  public boolean checkConnection(final URL url) {
    return checkConnection(url, Settings.propNetworkCheckTimeout.getInt()/100, true);
  }
  
  /**
   * Checks if a internet connection to a specific Server can be established
   * 
   * @param url check this Server
   * @param timeout the timeout for the check in milliseconds.
   * @param showWaitingDialog <code>true</code> if a waiting dialog should be shown, <code>false</code> otherwise.
   * @return <code>true</code>, if a connection can be established
   */
  public boolean checkConnection(final URL url, int timeout, boolean showWaitingDialog) {
    mResult = false;
    mRunningCount.set(0);
    
    boolean check = checkConnectionInternal(url, timeout, showWaitingDialog);
    
    hideDialog();
    return check;
  }

  /**
   * Checks if a internet connection to a specific Server can be established
   * 
   * @param url check this Server
   * @return true, if a connection can be established
   */
  private boolean checkConnectionInternal(final URL url) {
    return checkConnectionInternal(url, Settings.propNetworkCheckTimeout.getInt()/100, true);
  }
  
  /**
   * Checks if a internet connection to a specific Server can be established
   * 
   * @param url check this Server
   * @return true, if a connection can be established
   */
  private boolean checkConnectionInternal(final URL url, int timeout, final boolean showWaitingDialog) {
	  mRunningCount.incrementAndGet();
	
    // Start Check in second Thread
    new Thread((Runnable) () -> {
      if(!mResult && url != null) {
        try {
          URLConnection test = url.openConnection();
          
          if(test instanceof HttpsURLConnection) {
            HttpsURLConnection connection1 = (HttpsURLConnection)test;
            
            mResult = (connection1.getResponseCode() == HttpsURLConnection.HTTP_OK)
                || (connection1.getResponseCode() == HttpsURLConnection.HTTP_SEE_OTHER)
                || (connection1.getResponseCode() == HttpsURLConnection.HTTP_ACCEPTED)
                || (connection1.getResponseCode() == HttpsURLConnection.HTTP_CREATED) || mResult;
          }
          else {
            HttpURLConnection connection2 = (HttpURLConnection) test;
            mResult = (connection2.getResponseCode() == HttpURLConnection.HTTP_OK)
                || (connection2.getResponseCode() == HttpURLConnection.HTTP_SEE_OTHER)
                || (connection2.getResponseCode() == HttpURLConnection.HTTP_ACCEPTED)
                || (connection2.getResponseCode() == HttpURLConnection.HTTP_CREATED) || mResult;  
          }
        } catch (IOException e) {}
      }
      
      mRunningCount.decrementAndGet();
    }, "Check network connection").start();
    
    int num = 0;
    
    // Wait till second Thread is finished or Settings.propNetworkCheckTimeout is reached
    while (mRunningCount.get() > 0 && (num < timeout)) {
      num++;
      if (num == 7) {
        // Show the Dialog after 700 MS
        SwingUtilities.invokeLater(() -> {
          if(!mResult && showWaitingDialog) {
            showDialog();
          }
        });
      }
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {}
    }
    
    return mResult;
  }
  
  
  private void hideDialog() {
    if (mWaitingDialog != null) {
      if(MainFrame.getInstance().isVisible() && UiUtilities.getLastModalChildOf(MainFrame.getInstance()).equals(mWaitingDialog)) {
        mWaitingDialog.dispose();
      } else {
        new Thread("Hide network connection dialog") {
          public void run() {
            setPriority(Thread.MIN_PRIORITY);
            
            while(!MainFrame.getInstance().isVisible() && !UiUtilities.getLastModalChildOf(MainFrame.getInstance()).equals(mWaitingDialog)) {
              try {
                sleep(500);
              }catch(Exception e) {}
            }
            
            mWaitingDialog.dispose();
          }
        }.start();
      }
    }
  }

  private void showDialog() {
    try {
      if (MainFrame.isStarting()) {
        return;
      }
      if(!MainFrame.getInstance().isVisible() || MainFrame.getInstance().getExtendedState() == Frame.ICONIFIED) {
        return;
      }
    }catch(Exception e) {}
    
    if ((mRunningCount.get() > 0) && (mWaitingDialog == null) && !mResult) {
      mWaitingDialog = UiUtilities.createDialog(MainFrame.getInstance(), true);
      mWaitingDialog.setUndecorated(true);
      mWaitingDialog.setCursor(new Cursor(Cursor.WAIT_CURSOR));

      JPanel panel = (JPanel) mWaitingDialog.getContentPane();
      panel.setBorder(BorderFactory.createLineBorder(Color.BLACK));

      panel.setLayout(new FormLayout("3dlu, pref, 3dlu", "3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu"));
      CellConstraints cc = new CellConstraints();

      JLabel header = new JLabel(mLocalizer.msg("header", "Header"));
      header.setFont(header.getFont().deriveFont(Font.BOLD));

      panel.add(header, cc.xy(2, 2));

      panel.add(
          new JLabel(mLocalizer.msg("pleaseWait", "Checking the internet connection... This may take up to {0} seconds.", Settings.propNetworkCheckTimeout.getInt()/1000)), cc
              .xy(2, 4));

//      JProgressBar bar = new JProgressBar();
//      bar.setIndeterminate(true);
//      panel.add(bar, cc.xy(2, 6));

      mWaitingDialog.pack();
      if(mRunningCount.get() > 0 && !mResult) {
        UiUtilities.centerAndShow(mWaitingDialog);
      }
      mWaitingDialog.setVisible(!mResult && mRunningCount.get() > 0 && MainFrame.getInstance().isVisible() && MainFrame.getInstance().getExtendedState() != Frame.ICONIFIED);
    }
  }

  public static String[] getUrls() {
    return CHECK_URLS;
  }
}