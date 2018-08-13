/*
 * IDontWant2See - Plugin for TV-Browser
 * Copyright (C) 2008 René Mach
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
 * SVN information:
 *     $Date$
 *   $Author$
 * $Revision$
 */
package idontwant2see;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import devplugin.Date;

/**
 * The settings table for the value settings.
 * 
 * @author René Mach
 */
public class IDontWant2SeeSettingsTableModel extends AbstractTableModel {
  public static final int TYPE_SORT_ALPHABETICALLY = 1;
  public static final int TYPE_SORT_OUTDATED = 2;
  
  private ArrayList<IDontWant2SeeSettingsTableEntry> mDataFull = new ArrayList<IDontWant2SeeSettingsTableEntry>();
  private ArrayList<IDontWant2SeeSettingsTableEntry> mData = new ArrayList<IDontWant2SeeSettingsTableEntry>();
  
  private String mLastChangedValue;
  private JTable mTable;

  private String mFilter;
  
  protected IDontWant2SeeSettingsTableModel(final ArrayList<IDontWant2SeeListEntry> entries, final String lastEnteredExclusionString) {
    mLastChangedValue = lastEnteredExclusionString;
    mFilter = "";
    
    for(IDontWant2SeeListEntry entry : entries) {
      int index = mData.size();
      
      for(int i = mData.size() - 1; i >= 0; i--) {
        if(mData.get(i).compareTo(entry) > 0) {
          index = i;
        }
        else {
          break;
        }
      }
      
      final IDontWant2SeeSettingsTableEntry tableEntry = new IDontWant2SeeSettingsTableEntry(entry);
      
      mData.add(index, tableEntry);
      mDataFull.add(index, tableEntry);
    }
  }
  
  public int getColumnCount() {
    return 2;
  }

  public int getRowCount() {
    return mData.size();
  }
  
  public boolean isCellEditable(final int row, final int column) {
    return column == 0;
  }
  
  /**
   * Adds a new row to this table.
   */
  public void addRow() {
    mData.add(new IDontWant2SeeSettingsTableEntry(new IDontWant2SeeListEntry("DUMMY-ENTRY",true)));
    mDataFull.add(mData.get(mData.size()-1));
    
    fireTableRowsInserted(mData.size()-1,mData.size()-1);
  }
  
  protected boolean rowIsValid(final int row) {
    return mData.get(row).isValid();
  }
  
  protected boolean isRowOutdated(final int row, final Date compareValue,
      final int outdatedDayCount) {
    return mData.get(row).isOutdated(compareValue,outdatedDayCount);
  }
  
  protected boolean isLastChangedRow(final int row) {
    return mData.get(row).isLastChanged(mLastChangedValue);
  }
  
  public String getColumnName(final int column) {
    if(column == 0) {
      return IDontWant2See.mLocalizer.msg("searchText","Search text");
    }
    
    return IDontWant2See.mLocalizer.msg("settings.caseSensitive","case-sensitive");
  }
  
  /**
   * Deletes the row with the given row index.
   * @param row
   */
  public void deleteRow(final int row) {
    mDataFull.remove(mData.remove(row));
    
    fireTableRowsDeleted(row,row);
  }

  public Object getValueAt(final int rowIndex, final int columnIndex) {
    final IDontWant2SeeSettingsTableEntry entry = mData.get(rowIndex);
    
    if(columnIndex == 0) {
      return entry.mNewSearchText;
    }
    else {
      return entry.mNewCaseSensitve;
    }
  }
  
  public void setValueAt(final Object aValue, final int rowIndex,
      final int columnIndex) {
    final IDontWant2SeeSettingsTableEntry entry = mData.get(rowIndex);
    
    if(columnIndex == 0) {
      mLastChangedValue = (String)aValue;
      entry.setSearchText((String)aValue);
    }
    else {
      mLastChangedValue = (String)getValueAt(rowIndex,0);
      entry.setIsCaseSensitive((Boolean)aValue);
    }
    
    fireTableDataChanged();
    
    if(mTable != null) {
      mTable.getSelectionModel().setSelectionInterval(rowIndex,rowIndex);
    }
  }
  
  protected void setTable(final JTable table) {
    mTable = table;
  }
  
  protected ArrayList<IDontWant2SeeListEntry> getChangedList() {
    final ArrayList<IDontWant2SeeListEntry> newList = new ArrayList<IDontWant2SeeListEntry>();
    
    for(IDontWant2SeeSettingsTableEntry entry : mDataFull) {
      if(entry.isValid()) {
        newList.add(entry.doChanges());
      }
    }
    
    return newList;
  }
  
  protected String getLastChangedValue() {
    return mLastChangedValue;
  }
  
  protected int filter(final String filter) {
    final boolean wasEmpty = mFilter.trim().isEmpty(); 
    
    mFilter = filter;
    int row = updateTable();
    
    if(wasEmpty) {
      row = -1;
    }
    
    return row;
  }
  
  protected int sort(final int type) {
    mData.clear();
    
    switch (type) {
      case TYPE_SORT_ALPHABETICALLY: Collections.sort(mDataFull);break;
      case TYPE_SORT_OUTDATED: Collections.sort(mDataFull,IDontWant2SeeSettingsTableEntry.COMPARATOR_OUTDATED);break;
    }
    
    return updateTable();
  }
  
  private int updateTable() {
    int result = -1;
    
    mData.clear();
    
    for(IDontWant2SeeSettingsTableEntry entry : mDataFull) {
      if(mFilter.isEmpty() || entry.mNewSearchText.toLowerCase().replace("*", "").contains(mFilter)) {
        mData.add(entry);
        
        if(result == -1 && entry.isLastChanged(mLastChangedValue)) {
          result = mData.size()-1;
        }
      }
    }
    
    fireTableDataChanged();
    
    return result;
  }
  
  /**
   * The table entry class.
   */
  protected static class IDontWant2SeeSettingsTableEntry implements Comparable<Object> {
    private static final Comparator<IDontWant2SeeSettingsTableEntry> COMPARATOR_OUTDATED = new Comparator<IDontWant2SeeSettingsTableModel.IDontWant2SeeSettingsTableEntry>() {
      @Override
      public int compare(IDontWant2SeeSettingsTableEntry o1, IDontWant2SeeSettingsTableEntry o2) {
        int result = 0;
        
        if(o1.mLastMatchedDate == null && o2.mLastMatchedDate != null) {
          result = -1;
        }
        else if(o2.mLastMatchedDate == null && o1.mLastMatchedDate != null) {
          result = 1;
        }
        else if(o1.mLastMatchedDate != null && o2.mLastMatchedDate != null) {
          result = o1.mLastMatchedDate.compareTo(o2.mLastMatchedDate);
        }
        
        if(result == 0) {
          result = o1.compareTo(o2);
        }
        
        return result;
      }
    };
    
    private IDontWant2SeeListEntry mListEntry;
    
    private boolean mWasChanged;
    
    private boolean mNewCaseSensitve;
    private String mNewSearchText;
    private Date mLastMatchedDate;
    
    protected IDontWant2SeeSettingsTableEntry(final IDontWant2SeeListEntry entry) {
      mListEntry = entry;
      mWasChanged = false;
      mNewCaseSensitve = entry.isCaseSensitive();
      mNewSearchText = entry.getSearchText();
      mLastMatchedDate = entry.getLastMatchedDate();
    }
    
    /**
     * Sets the new value for the case-sensitive flag.
     * <p>
     * @param value The new value for the case-sensitive flag.
     */
    protected void setIsCaseSensitive(final boolean value) {
      mWasChanged = !mNewSearchText.equals(mListEntry.getSearchText()) || mListEntry.isCaseSensitive() != value;
      mNewCaseSensitve = value;
      
      if(mWasChanged) {
        mLastMatchedDate = null;
      }
      else {
        mLastMatchedDate = mListEntry.getLastMatchedDate();
      }
    }
    
    /**
     * Sets the new search text.
     * <p>
     * @param text The new search text.
     */
    protected void setSearchText(final String text) {
      mWasChanged = !mListEntry.getSearchText().equals(text) || mListEntry.isCaseSensitive() != mNewCaseSensitve;
      mNewSearchText = text;
      
      if(mWasChanged) {
        mLastMatchedDate = null;
      }
      else {
        mLastMatchedDate = mListEntry.getLastMatchedDate();
      }
    }
    
    /**
     * Gets if this table entry has a valid search text
     * <p>
     * @return <code>True</code> if the search text is valid.
     */
    protected boolean isValid() {
      String test = "";
      
      if(mNewSearchText != null) {
        test = mNewSearchText.replaceAll("\\*+","\\*").trim();
      }
      
      return test.length() > 0 && !test.equals("DUMMY-ENTRY") 
        && !test.equals("*");
    }
    
    /**
     * Performs the changes of this value.
     */
    protected IDontWant2SeeListEntry doChanges() {
      if(mWasChanged) {
        mListEntry.setValues(mNewSearchText,mNewCaseSensitve);
      }
      
      return mListEntry;
    }
    
    protected boolean isOutdated(final Date compareValue,
        final int outdatedDayCount) {
      if(mLastMatchedDate != null && compareValue != null) {
        return mLastMatchedDate.addDays(outdatedDayCount).compareTo(compareValue) < 0;
      }
      
      return false;
    }
    
    protected boolean isLastChanged(final String lastChangedText) {
      return mNewSearchText.equals(lastChangedText);
    }
    
    public int compareTo(final Object o) {
      if(o instanceof String) {
        return mNewSearchText.replace("*", "").compareToIgnoreCase(((String)o).replace("*", ""));
      }
      else if(o instanceof IDontWant2SeeSettingsTableEntry) {
        return mNewSearchText.replace("*", "").compareToIgnoreCase(((IDontWant2SeeSettingsTableEntry)o).mNewSearchText.replace("*", ""));
      }
      else if(o instanceof IDontWant2SeeListEntry) {
        return mNewSearchText.replace("*", "").compareToIgnoreCase(((IDontWant2SeeListEntry)o).getSearchText().replace("*", ""));
      }
      
      return 0;
    }
  }
}
