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
 *  $RCSfile$
 *   $Source$
 *     $Date$
 *   $Author$
 * $Revision$
 */

package devplugin;

import java.io.*;

import tvdataservice.TvDataService;

public class Channel {

  private TvDataService mDataService;
  private String mName;
  private int mId;



  public Channel(TvDataService dataService, String name, int id) {
    mDataService = dataService;
    mName = name;
    mId = id;
  }

  
  
  public static Channel readData(ObjectInputStream in, boolean allowNull)
    throws IOException, ClassNotFoundException
  {
    int version = in.readInt();
    
    String dataServiceClassName = (String) in.readObject();
    int channelId = in.readInt();
    
    Channel channel = getChannel(dataServiceClassName, channelId);
    if ((channel == null) && (! allowNull)) {
      throw new IOException("Channel with id " + channelId + " of data service "
        + dataServiceClassName + " not found!");
    }
    
    return channel;
  }
  


  /**
   * Serialized this object.
   */
  public void writeData(ObjectOutputStream out) throws IOException {
    out.writeInt(1); // version

    out.writeObject(mDataService.getClass().getName());
    out.writeInt(mId);
  }

  
  
  public static Channel getChannel(String dataServiceClassName, int channelId) {
    if (dataServiceClassName == null) {
      // Fast return
      return null;
    }
    
    Channel[] channelArr = Plugin.getPluginManager().getSubscribedChannels();
    for (int i = 0; i < channelArr.length; i++) {
      if (dataServiceClassName.equals(channelArr[i].getDataService().getClass().getName())
        && (channelArr[i].getId() == channelId))
      {
        return channelArr[i];
      }      
    }
    
    return null;
  }
  


  public TvDataService getDataService() {
    return mDataService;
  }



  public String toString() {
    return mName + " (" + mDataService.getName() + ")";
  }



  public String getName() {
    return mName;
  }



  public int getId() {
    return mId;
  }



  public boolean equals(Object obj) {
    if (obj instanceof Channel) {
      Channel cmp = (Channel) obj;
      return (mDataService == cmp.mDataService) && (mId == cmp.mId);
    }

    return false;
  }

}
