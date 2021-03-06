/*
 * Copyright Michael Keppler
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package mediathekplugin;

import java.io.File;
import java.util.Properties;

import util.settings.PropertyBasedSettings;

public class MediathekSettings extends PropertyBasedSettings {
  
  public static final String MEDIATHEK_PROG_FILE = "MediathekView.jar";
  public static final String MEDIATHEK_DATA_FILE = "filme.json";
  
  private static final String KEY_MEDIATHEK_PATH = "mediathekpath";
  private static final String KEY_MEDIATHEK_QUALITY = "mediathekquality";
  private static final String KEY_MEDIATHEK_PROGRAM_PATH = "mediathekprogram";
  private static final String KEY_MEDIATHEK_UPDATEINTERVAL = "mediathekupdateinterval";

  public MediathekSettings(final Properties properties) {
    super(properties);
  }
  
  public MediathekQuality getMediathekQuality() {
    String value = get(KEY_MEDIATHEK_QUALITY, "");
    
   return MediathekQuality.fromString(value);
  }

  public void setMediathekQuality(final MediathekQuality quality) {
    set(KEY_MEDIATHEK_QUALITY, quality.toSaveString());
  }

  public String getMediathekPath() {
    String value = get(KEY_MEDIATHEK_PATH, "");
    
    if(value.trim().length() > 0) {
      if(!new File(value).isFile()) {
        value = "";
      }
    }    
    return value;
  }

  public String getMediathekProgramPath() {
    String value = get(KEY_MEDIATHEK_PROGRAM_PATH, "");
    
    if(value.trim().length() > 0) {
      if(!new File(value).isFile()) {
        value = "";
      }
    }    
    return value;
  }

  public void setMediathekPath(final String path) {
    set(KEY_MEDIATHEK_PATH, path);
  }

  public void setMediathekProgramPath(final String path) {
    set(KEY_MEDIATHEK_PROGRAM_PATH, path);
  }
  
  public String guessMediathekPath(boolean save) {
    String value = getMediathekPath();
    
    if(value.trim().length() == 0) {
      File test = new File(System.getProperty("user.home"),".mediathek3");
      
      if(test.isDirectory()) {
        test = new File(test,MEDIATHEK_DATA_FILE);
        
        if(test.isFile()) {
          value = test.getAbsolutePath();
          
          if(save) {
            setMediathekPath(value);
          }
        }
      }
    }
    
    return value;
  }
  
  public void setMediathekUpdateInterval(int minutes){
    set(KEY_MEDIATHEK_UPDATEINTERVAL,minutes);
  }

  public int getMediathekUpdateInterval() {
    return get(KEY_MEDIATHEK_UPDATEINTERVAL, -30);
  }
}
