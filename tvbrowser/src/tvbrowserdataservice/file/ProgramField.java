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
package tvbrowserdataservice.file;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import java.util.logging.Level;

import util.io.IOUtilities;

import devplugin.*;

/**
 * 
 * 
 * @author Til Schneider, www.murfman.de
 */
public class ProgramField implements Cloneable {
  
  private static java.util.logging.Logger mLog
    = java.util.logging.Logger.getLogger(ProgramField.class.getName());
  
  private static final String TEXT_CHARSET = "UTF-8";

  private int mTypeId;

  private ProgramFieldType mType;
  
  private byte[] mData;
  
  private int mDataFormat;
  
  
  
  public ProgramField() {
    mDataFormat = ProgramFieldType.UNKOWN_FORMAT;
    mType = null;
  }
  /**
   * @deprecated use the factory method
   */
  public ProgramField(ProgramFieldType type, String text) {
    setType(type);
    setTextData(text);
  }
  /**
    * @deprecated use the factory method
    */
  public ProgramField(ProgramFieldType type, byte[] data) {
    setType(type);
    setBinaryData(data);
  }
  
  /**
    * @deprecated use the factory method
    */
  public ProgramField(ProgramFieldType type, int value) {
    setType(type);
    
    if (type.getFormat() == ProgramFieldType.TIME_FORMAT) {
      setTimeData(value);
    } else {
      setIntData(value);
    }
  }

  public static ProgramField create(ProgramFieldType type, String text) {
    if ((text == null) || (text.length() == 0)) {
      return null;
    }
    
    ProgramField p=new ProgramField();
    p.setType(type);
    p.setTextData(text);
    return p;
  }
  
  public static ProgramField create(ProgramFieldType type, byte[] data) {
    if ((data == null) || (data.length == 0)) {
      return null;
    }
    
    ProgramField p=new ProgramField();
    p.setType(type);
    p.setBinaryData(data);
    return p;
  }
    
  public static ProgramField create(ProgramFieldType type, int value) {
    ProgramField p=new ProgramField();
    p.setType(type);
    if (type.getFormat() == ProgramFieldType.TIME_FORMAT) {
      p.setTimeData(value);
    } else {
      p.setIntData(value);
    }
    return p;
  }
  
  

  public Object clone() {
    try {
      return super.clone();
    }
    catch (CloneNotSupportedException exc) {
      // This will never happen, since this class implements Cloneable
      return null;
    }
  }



 
  public int getTypeId() {
    return mTypeId;
  }



 
  public ProgramFieldType getType() {
    if (mType == null) {
      mType = ProgramFieldType.getTypeForId(mTypeId);
    }
    
    return mType;
  }



  /**
   * @param type
   */
  public void setType(ProgramFieldType type) {
    mType = type;
    mTypeId = type.getTypeId();
  }
  
  
  
  /**
   * Is used for a field in an update file that should be deleted.
   */
  public void removeData() {
    mDataFormat = ProgramFieldType.UNKOWN_FORMAT;
    mData = null;
  }



  
  public byte[] getBinaryData() {
    return mData;
  }



  /**
   * @param data
   */
  public void setBinaryData(byte[] data) {
    mDataFormat = ProgramFieldType.BINARY_FORMAT;
    
    mData = data;
  }
  
  
  
  public String getTextData() {
    if (mData==null) {
    	return null;
    }
    
    try {
      return new String(mData, TEXT_CHARSET);
    }
    catch (UnsupportedEncodingException exc) {
      // This will never happen, because UTF-8 is always supported
      mLog.log(Level.SEVERE, "Charset " + TEXT_CHARSET + " is not supported", exc);
      
      return null;
    }
  }



  public void setTextData(String text) {
    mDataFormat = ProgramFieldType.TEXT_FORMAT;

    try {
      mData = text.getBytes(TEXT_CHARSET);
    }
    catch (UnsupportedEncodingException exc) {
      // This will never happen, because UTF-8 is always supported
      mLog.log(Level.SEVERE, "Charset " + TEXT_CHARSET + " is not supported", exc);
    }
  }
  
  
  
  public int getIntData() {
    return dataToInt(mData);
  }
  
  
  
  public void setIntData(int value) {
    mDataFormat = ProgramFieldType.INT_FORMAT;
    
    mData = intToData(value);
  }



  public int getTimeData() {
    return dataToInt(mData);
  }
  
  
  
  public void setTimeData(int minutesAfter1970) {
    mDataFormat = ProgramFieldType.TIME_FORMAT;
    
    mData = intToData(minutesAfter1970);
  }

  
  /**
   * Gets a String representation of the data value. 
   * 
   * @return the data value as String.
   */
  public String getDataAsString() {
    if (mDataFormat == ProgramFieldType.TEXT_FORMAT) {
      return "'" + getTextData() + "'";
    }
    else if (mDataFormat == ProgramFieldType.INT_FORMAT) {
      return Integer.toString(getIntData());
    }
    else if (mDataFormat == ProgramFieldType.TIME_FORMAT) {
      int time = getTimeData();
      int hours = time / 60;
      int minutes = time % 60;
      return hours + ":" + ((minutes < 10) ? "0" : "") + minutes;
    }
    else if (mDataFormat == ProgramFieldType.BINARY_FORMAT) {
      return "(binary)";
    }
    else {
      return "(unknown)";
    }
  }


  private static int dataToInt(byte[] data) {
    if (data == null) {
      return 0;
    }
    
    return (((int) data[0] & 0xFF) << (3 * 8))
         | (((int) data[1] & 0xFF) << (2 * 8))
         | (((int) data[2] & 0xFF) << (1 * 8))
         | (((int) data[3] & 0xFF) << (0 * 8));
  }



  private static byte[] intToData(int value) {
    byte[] data = new byte[4];
    
    data[0] = (byte) (value >> (3 * 8));
    data[1] = (byte) (value >> (2 * 8));
    data[2] = (byte) (value >> (1 * 8));
    data[3] = (byte) (value >> (0 * 8));
    
    return data;
  }



  private void checkFormat() throws FileFormatException {
    // Check whether the field data has the right format
    if (! getType().isRightFormat(mDataFormat)) {
      throw new FileFormatException("The field '" + getType().getName()
        + "' must have the " + ProgramFieldType.getFormatName(getType().getFormat())
        + " but it has the " + ProgramFieldType.getFormatName(mDataFormat));
    }
  }



  public void readFromStream(InputStream stream)
    throws IOException, FileFormatException
  {
    mTypeId = stream.read();
    mType = null;

    mDataFormat = ProgramFieldType.UNKOWN_FORMAT;
    
    int dataLength = ((stream.read() & 0xFF) << 16)
                   | ((stream.read() & 0xFF) << 8)
                   |  (stream.read() & 0xFF);
                   
    if (dataLength == 0) {
      mData = null;
    } else {
      mData = IOUtilities.readBinaryData(stream, dataLength);
    }
  }



  public void writeToStream(OutputStream stream)
    throws IOException, FileFormatException
  {
    // Check whether the field has the right format
    checkFormat();

    stream.write(mTypeId);
    
    if (mData == null) {
      stream.write(0); // Length highest byte
      stream.write(0); // Length middle byte
      stream.write(0); // Length lowest byte
    } else {
      // Write the data length
      stream.write((byte) (mData.length >> 16));
      stream.write((byte) (mData.length >> 8));
      stream.write((byte) (mData.length));
      
      // Write the data
      stream.write(mData);
    }
  }



  public static boolean arraysAreEqual(byte[] array1, byte[] array2) {
    if ((array1 == null) || (array2 == null)) {
      return array1 == array2; // true when both are null
    }
    
    if (array1.length != array2.length) {
      return false;
    }
    
    for (int i = 0; i < array1.length; i++) {
      if (array1[i] != array2[i]) {
        return false;
      }
    }
    
    // Everything was equal
    return true;
  }



  public boolean equals(Object obj) {
    if (obj instanceof ProgramField) {
      ProgramField field = (ProgramField) obj;
      
      if (getTypeId() != field.getTypeId()) {
        return false;
      }
      
      return arraysAreEqual(mData, field.mData);
    } else {
      return false;
    }
  }

}
