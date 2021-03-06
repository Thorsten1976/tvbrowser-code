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
package util.settings;

import devplugin.Date;

/**
 * 
 * 
 * @author Til Schneider, www.murfman.de
 */
public class DateProperty extends Property {

  private Date mDefaultValue;
  private Date mCachedValue;
  
  
  
  public DateProperty(PropertyManager manager, String key,
    Date defaultValue)
  {
    super(manager, key);

    mDefaultValue = defaultValue;
    mCachedValue = null;
  }


  public Date getDefault() {
    return mDefaultValue;
  }


  public Date getDate() {
    if (mCachedValue == null) {
      String asString = getProperty();

      if (asString != null) {
        String[] splits = asString.split("-");
        if (splits.length == 3) {
          try {
            mCachedValue = new Date(Integer.parseInt(splits[0]),
                                    Integer.parseInt(splits[1]),
                                    Integer.parseInt(splits[2]));
          }
          catch(NumberFormatException exc) {
            // We use the default value
          }
        }
      }

      if (mCachedValue == null) {
        mCachedValue = mDefaultValue;
      }
    }

    return mCachedValue;
  }
  
  
  public void setDate(Date value) {
    if (value == null) {
      throw new IllegalArgumentException("You can't set a null value");
    }
    
    if (value.equals(mDefaultValue)) {
      setProperty(null);
    } else {
      String asString = value.getYear() + "-" + value.getMonth() + "-"
        + value.getDayOfMonth();
      setProperty(asString);
    }
    
    mCachedValue = value;
  }
  
  
  protected void clearCache() {
    mCachedValue = null;
  }

}
