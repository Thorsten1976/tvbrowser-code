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
 *     $Date$
 *   $Author$
 * $Revision$
 */
package util.program;

import util.io.IOUtilities;
import devplugin.Program;
import devplugin.ProgramFieldType;

/**
 * The program field type for special compounded types.
 * 
 * @author René Mach
 */
public class CompoundedProgramFieldType {
  /** The compounded field type for the episode formating */
  public static final CompoundedProgramFieldType EPISODE_COMPOSITION =
    new CompoundedProgramFieldType(-1, new ProgramFieldType[] {ProgramFieldType.EPISODE_NUMBER_TYPE,ProgramFieldType.EPISODE_TOTAL_NUMBER_TYPE,ProgramFieldType.SEASON_NUMBER_TYPE,ProgramFieldType.EPISODE_TYPE}, ProgramFieldType.EPISODE_TYPE.getLocalizedName(), new String[] {"/"," - ",", "});
  public static final CompoundedProgramFieldType COMPOSITION_ORIGIN_YEAR =
      new CompoundedProgramFieldType(-2, null, ProgramFieldType.ORIGIN_TYPE.getLocalizedName() + "/" + ProgramFieldType.PRODUCTION_YEAR_TYPE.getLocalizedName(), new Formatter() {
        @Override
        public String getFormattedValueForProgram(ProgramFieldType[] fieldTypes, Program prog) {
          String origin = prog.getTextField(ProgramFieldType.ORIGIN_TYPE);
          int year = prog.getIntField(ProgramFieldType.PRODUCTION_YEAR_TYPE);
          int yearFirst = prog.getIntField(ProgramFieldType.FIRST_PRODUCTION_YEAR);
          int yearLast = prog.getIntField(ProgramFieldType.LAST_PRODUCTION_YEAR_TYPE);
          
          if(yearLast < year) {
            yearLast = -1;
          }
          
          if(year != -1 && ((yearLast != -1 && yearFirst == -1) || (yearLast == -1 && yearFirst < year && yearFirst != -1))) {
            yearFirst = year;
          }
          
          final StringBuilder result = new StringBuilder();
          
          int originIndex = 0;
          
          if(origin != null) {
            result.append(origin);
            originIndex = result.length();
          }
          
          if(year != -1) {
            if(result.length() >= 0) {
              result.append(" ");
            }
            
            result.append(year);
          }
          
          if(yearLast != -1 && yearLast != year) {
            if(yearFirst != -1) {
              if(result.length() >= 0) {
                if(yearFirst == year) {
                  result.append(" \u2013 ");
                }
                else {
                  result.append(" (");
                }
              }
              
              if(yearFirst == year) {
                result.append(yearLast);
              }
              else {
                result.append(yearFirst).append(" \u2013 ").append(yearLast).append(")");
              }
            }
            else {
              result.append(yearLast);
            }
          }
          else if(yearFirst != -1 && yearFirst != year) {
            if(yearFirst < year) {
              result.insert(originIndex, " \u2013 ");
              result.insert(originIndex, yearFirst);
              
              if(originIndex != 0) {
                result.insert(originIndex, " ");
              }
            }
            else if(year == -1) {
              if(originIndex > 0) {
                result.append(" ");
              }
              
              result.append(yearFirst);
            }
          }
          
          if(!result.toString().trim().isEmpty()) {
            return result.toString();
          }
          
          return null;
        }
      });
  
  private int mId;
  private ProgramFieldType[] mFieldTypes;
  private String mName;
  private String mFormatString;
  private Formatter mFormatter;
  
  private String[] mPartSeparators;
  
  /**
   * Creates an instance of this extra type.
   * 
   * @param id The id of the compounded field type. (has to be a negative value!)
   * @param fieldTypes The field type that this type contains.
   * @param name The name for this field type, or <code>null</code> if
   * the name of the first entry in the field type array should be used.
   * @param formatString The String that contains the formating for the display
   * use {n} as replacement for the values of the n-th field type.
   */
  private CompoundedProgramFieldType(int id, ProgramFieldType[] fieldTypes, String name, String formatString) {
    mId = id;
    mFieldTypes = fieldTypes;
    mName = name;
    mFormatString = formatString;
    mPartSeparators = null;
  }
  
  /**
   * Creates an instance of this extra type.
   * 
   * @param id The id of the compounded field type. (has to be a negative value!)
   * @param fieldTypes The field type that this type contains.
   * @param name The name for this field type, or <code>null</code> if
   * the name of the first entry in the field type array should be used.
   * @param partSeparators The separator string between the fields. If one field value
   * is empty the part separator isn't used between the empty field and the next one.
   */
  private CompoundedProgramFieldType(int id, ProgramFieldType[] fieldTypes, String name, String[] partSeparators) {
    mId = id;
    mFieldTypes = fieldTypes;
    mName = name;
    mFormatString = null;
    mPartSeparators = partSeparators;
  }
  

  /**
   * Creates an instance of this extra type.
   * 
   * @param id The id of the compounded field type. (has to be a negative value!)
   * @param fieldTypes The field type that this type contains.
   * @param name The name for this field type, or <code>null</code> if
   * the name of the first entry in the field type array should be used.
   * @param formatter The formatter for the program fields.
   */
  private CompoundedProgramFieldType(int id, ProgramFieldType[] fieldTypes, String name, Formatter formatter) {
    mId = id;
    mFieldTypes = fieldTypes;
    mName = name;
    mFormatString = null;
    mPartSeparators = null;
    mFormatter = formatter;
  }
  
  /**
   * Gets the formatted value for the given program for this
   * compounded field type.
   * 
   * @param prog The Program to get the formatted value for.
   * @return The formatted value.
   */
  public String getFormattedValueForProgram(Program prog) {
    if(mFormatString != null) {
      String value = mFormatString;
      boolean found = false;
      
      for(int i = 0; i < mFieldTypes.length; i++) {
        String formattedValue = "";
        
        if(mFieldTypes[i].getFormat() == ProgramFieldType.FORMAT_TEXT) {
          formattedValue = prog.getTextField(mFieldTypes[i]);
        }
        else if(mFieldTypes[i].getFormat() == ProgramFieldType.FORMAT_INT) {
          if(mFieldTypes[i].equals(ProgramFieldType.EPISODE_NUMBER_TYPE) && prog.hasFieldValue(ProgramFieldType.EPISODE_NUMBER_TYPE)) {
            formattedValue = IOUtilities.decodeSingleFieldValueToMultipleEpisodeString(prog.getIntField(mFieldTypes[i]));
          }
          else {
            formattedValue = prog.getIntFieldAsString(mFieldTypes[i]);
          }
        }
        else if(mFieldTypes[i].getFormat() == ProgramFieldType.FORMAT_TIME) {
          formattedValue = prog.getTimeFieldAsString(mFieldTypes[i]);
        }
        
        if(formattedValue == null) {
          formattedValue = "";
        }
        else {
          found = true;
        }
        
        value = value.replace("{"+i+"}",formattedValue);
      }
      
      return found ? value : null;
    }
    else if(mPartSeparators != null) {
      StringBuilder value = new StringBuilder();
      
      String prevValue = "";
      String currentValue = "";
      
      for(int i = 0; i < mFieldTypes.length; i++) {
        if(mFieldTypes[i].getFormat() == ProgramFieldType.FORMAT_TEXT) {
          currentValue = prog.getTextField(mFieldTypes[i]);
        }
        else if(mFieldTypes[i].getFormat() == ProgramFieldType.FORMAT_INT) {
          if(mFieldTypes[i].equals(ProgramFieldType.EPISODE_NUMBER_TYPE) && prog.hasFieldValue(ProgramFieldType.EPISODE_NUMBER_TYPE)) {
            currentValue = IOUtilities.decodeSingleFieldValueToMultipleEpisodeString(prog.getIntField(mFieldTypes[i]));
          }
          else {
            currentValue = prog.getIntFieldAsString(mFieldTypes[i]);
          }
        }
        else if(mFieldTypes[i].getFormat() == ProgramFieldType.FORMAT_TIME) {
          currentValue = prog.getTimeFieldAsString(mFieldTypes[i]);
        }

        
        if((prevValue != null || value.length() > 0) && currentValue != null) {
          if(i == 0) {
            value.append(currentValue);
          }
          else {
            value.append(mPartSeparators[i-1]).append(currentValue);
          }
        }
        else if(currentValue != null) {
          value.append(currentValue);
        }
        
        prevValue = currentValue;
      }
      
      if(value.toString().trim().length() > 0) {
        return value.toString();
      }
    }
    else if(mFormatter != null) {
      return mFormatter.getFormattedValueForProgram(mFieldTypes, prog);
    }
    
    return null;
  }
  
  /**
   * Gets the name of this compounded field type.
   * 
   * @return The name of this compounded field type.
   */
  public String getName() {
    if(mName != null) {
      return mName;
    }
    else {
      return mFieldTypes[0].getLocalizedName();
    }
  }
  
  public String toString() {
    return getName();
  }
  
  /**
   * Gets the id of this compounded field type.
   * 
   * @return The id of this compounded field type.
   */
  public int getId() {
    return mId;
  }
  
  /**
   * Gets the compounded program field type for the given id.
   * 
   * @param id The id to get the compounded field type for.
   * @return The compounded field type for the given id or
   * <code>null</code> if the compounded field type of the id was not found.
   */
  public static CompoundedProgramFieldType getCompoundedProgramFieldTypeForId(int id) {
    if(id == EPISODE_COMPOSITION.getId()) {
      return EPISODE_COMPOSITION;
    }
    else if(id == COMPOSITION_ORIGIN_YEAR.getId()) {
      return COMPOSITION_ORIGIN_YEAR;
    }
    
    return null;
  }
  
  private static interface Formatter {
    public String getFormattedValueForProgram(final ProgramFieldType[] fieldTypes, final Program prog);
  }
}
