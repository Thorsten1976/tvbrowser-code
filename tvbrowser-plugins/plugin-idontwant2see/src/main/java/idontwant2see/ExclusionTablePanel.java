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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.Sizes;

import tvbrowser.core.icontheme.IconLoader;
import tvbrowser.ui.settings.util.ColorLabel;
import util.ui.Localizer;
import util.ui.UiUtilities;

/**
 * The exclusion table panel for settings of this plugin.
 * 
 * @author René Mach
 */
public class ExclusionTablePanel extends JPanel {
  private JTable mTable;
  private IDontWant2SeeSettingsTableModel mTableModel;
  private static final Localizer mLocalizer = IDontWant2See.mLocalizer;
  
  protected ExclusionTablePanel(final IDontWant2SeeSettings settings) {
    mTableModel = new IDontWant2SeeSettingsTableModel(settings.getSearchList(),settings.getLastEnteredExclusionString());
    
    final IDontWant2SeeSettingsTableRenderer renderer = new IDontWant2SeeSettingsTableRenderer(settings.getLastUsedDate(), settings);        
    mTable = new JTable(mTableModel);
    mTableModel.setTable(mTable);
    mTable.setRowHeight(25);
    mTable.setPreferredScrollableViewportSize(new Dimension(200,150));
    mTable.getColumnModel().getColumn(0).setCellRenderer(renderer);
    mTable.getColumnModel().getColumn(1).setCellRenderer(renderer);
    mTable.getColumnModel().getColumn(1).setMaxWidth(Locale.getDefault().getLanguage().equals("de") ? Sizes.dialogUnitXAsPixel(80,mTable) : Sizes.dialogUnitXAsPixel(55,mTable));
    mTable.getColumnModel().getColumn(1).setMinWidth(mTable.getColumnModel().getColumn(1).getMaxWidth());
    mTable.getTableHeader().setReorderingAllowed(false);
    mTable.getTableHeader().setResizingAllowed(false);
    
    final JScrollPane scrollPane = new JScrollPane(mTable);
    
    mTable.addMouseListener(new MouseAdapter() {
      public void mouseClicked(final MouseEvent e) {
        final int column = mTable.columnAtPoint(e.getPoint());
        
        if(column == 1) {
          final int row = mTable.rowAtPoint(e.getPoint());
          
          mTable.getModel().setValueAt(!((Boolean)mTable.getValueAt(row,column)),row,1);
          mTable.repaint();
        }
      }
    });
    
    mTable.addKeyListener(new KeyAdapter() {
      public void keyPressed(final KeyEvent e) {
        if(e.getKeyCode() == KeyEvent.VK_DELETE) {
          deleteSelectedRows();
          e.consume();
        }
        else if(mTable.getSelectedColumn() == 1 && 
            (e.getKeyCode() == KeyEvent.VK_F2 || e.getKeyCode() == KeyEvent.VK_SPACE)) {
          mTable.getModel().setValueAt(!((Boolean)mTable.getValueAt(mTable.getSelectedRow(),1)),
              mTable.getSelectedRow(),1);
          mTable.repaint();
        }
      }
    });
    
    addAncestorListener(new AncestorListener() {
      public void ancestorAdded(final AncestorEvent event) {
        for(int row = 0; row < mTableModel.getRowCount(); row++) {
          if(mTableModel.isLastChangedRow(row)) {
            final Rectangle rect = mTable.getCellRect(row, 0, true);
            rect.setBounds(0,scrollPane.getVisibleRect().height + rect.y - rect.height,0,0);
            
            mTable.scrollRectToVisible(rect);
            break;
          }
        }
      }

      public void ancestorMoved(final AncestorEvent event) {
      }

      public void ancestorRemoved(final AncestorEvent event) {
      }
    });
    
    final JButton add = new JButton(mLocalizer.msg("settings.add",
        "Add entry"),
        IDontWant2See.getInstance().createImageIcon("actions","document-new",16));
    add.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        mTableModel.addRow();
        mTable.scrollRectToVisible(mTable.getCellRect(mTableModel.getRowCount()-1,0,true));
      }
    });
    
    final JButton removeDuplicateEntries = new JButton(mLocalizer.msg("settings.duplicates", "Remove duplicates"));
    removeDuplicateEntries.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        removeDuplicateRows();
      }
    });
    
    final JButton delete = new JButton(mLocalizer.msg("settings.delete",
        "Delete selected entries"),IDontWant2See.getInstance().createImageIcon("actions","edit-delete",16));
    delete.setEnabled(false);
    delete.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        deleteSelectedRows();
      }
    });
    
    mTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(final ListSelectionEvent e) {
        if(!e.getValueIsAdjusting()) {
          delete.setEnabled(e.getFirstIndex() >= 0);
        }
      }
    });
    
    final JButton clearFilter = new JButton(IconLoader.getInstance().getIconFromTheme("actions", "process-stop", 16));
    final PanelBuilder pb1 = new PanelBuilder(
        new FormLayout("default,3dlu,default,10dlu,default,3dlu,default:grow,1dlu,default", "default"));
    
    final JComboBox<String> sort = new JComboBox<String>();
    
    sort.addItem(mLocalizer.msg("sort.alphabetically","Alphabetically"));
    sort.addItem(mLocalizer.msg("sort.unused","Last used"));
    
    final JTextField filter = new JTextField();
    filter.addCaretListener(new CaretListener() {
      private String mPreviousText = "";
      
      @Override
      public void caretUpdate(CaretEvent e) {
        if(!mPreviousText.equals(filter.getText())) {
          int row = mTableModel.filter(filter.getText().trim());
          
          if(!filter.getText().trim().isEmpty()) {
            mTable.scrollRectToVisible(mTable.getCellRect(0,0,true));
          }
          else if(sort.getSelectedIndex() == 0 && row >= 0) {
            mTable.scrollRectToVisible(mTable.getCellRect(row,0,true));
          }
          
          clearFilter.setEnabled(!filter.getText().trim().isEmpty());
          mPreviousText = filter.getText();
        }
      }
    });
    
    clearFilter.setEnabled(false);
    clearFilter.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    clearFilter.setPressedIcon(IconLoader.getInstance().getIconFromTheme(
        "actions", "close-pressed", 16));
    clearFilter.setToolTipText(mLocalizer.msg("filter.clear","Clear filter"));
    clearFilter.setContentAreaFilled(false);
    clearFilter.setFocusable(false);
    clearFilter.setOpaque(false);
    clearFilter.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        filter.setText("");
      }
    });
    
    
    sort.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        if(e.getStateChange() == ItemEvent.SELECTED) {
          int row = mTableModel.sort(sort.getSelectedIndex() == 1 ? IDontWant2SeeSettingsTableModel.TYPE_SORT_OUTDATED : IDontWant2SeeSettingsTableModel.TYPE_SORT_ALPHABETICALLY);
          
          if(sort.getSelectedIndex() == 1) {
            row = 0;
          }
          
          mTable.scrollRectToVisible(mTable.getCellRect(row > 0 ? row : 0,0,true));
        }
      }
    });
    
    pb1.addLabel(mLocalizer.msg("sort","Sorting"), CC.xy(1, 1));
    pb1.add(sort, CC.xy(3, 1));
    pb1.addLabel(mLocalizer.msg("filter","Filter:"), CC.xy(5, 1));
    pb1.add(filter, CC.xy(7, 1));
    pb1.add(clearFilter, CC.xy(9, 1));
    
    final FormLayout layout = new FormLayout("default,0dlu:grow,default,0dlu:grow,default",
        "default,3dlu,fill:default:grow,1dlu,default,4dlu,default,5dlu,pref");
    final PanelBuilder pb = new PanelBuilder(layout, this);
    
    int y = 1;
    
    pb.add(pb1.getPanel(), CC.xyw(1, y, 5));
    
    y += 2;
    
    pb.add(scrollPane, CC.xyw(1,y++,5));
    
    final PanelBuilder pb2 = new PanelBuilder(
        new FormLayout("default,3dlu:grow,default,3dlu:grow,default,3dlu:grow,default",
            "default"));
    
    final ColorLabel blueLabel = new ColorLabel(
        IDontWant2SeeSettingsTableRenderer.LAST_CHANGED_COLOR);
    blueLabel.setText(mLocalizer.msg("changed","Last change"));
    pb2.add(blueLabel, CC.xy(1,1));
    
    final ColorLabelComboBox yellowLabel = new ColorLabelComboBox(IDontWant2SeeSettingsTableRenderer.LAST_USAGE_FIRST_COLOR, settings.getOutdated(IDontWant2SeeSettings.OUTDATED_DAY_COUNT_DEFAULT_FIRST));
    yellowLabel.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        if(e.getStateChange() == ItemEvent.SELECTED) {
          settings.setOutdated(IDontWant2SeeSettings.OUTDATED_DAY_COUNT_DEFAULT_FIRST, (Integer)e.getItem());
          mTableModel.fireTableDataChanged();
        }
      }
    });
    pb2.add(yellowLabel, CC.xy(3,1));
    
    final ColorLabelComboBox orangeLabel = new ColorLabelComboBox(IDontWant2SeeSettingsTableRenderer.LAST_USAGE_SECOND_COLOR,settings.getOutdated(IDontWant2SeeSettings.OUTDATED_DAY_COUNT_DEFAULT_SECOND));
    orangeLabel.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        if(e.getStateChange() == ItemEvent.SELECTED) {
          settings.setOutdated(IDontWant2SeeSettings.OUTDATED_DAY_COUNT_DEFAULT_SECOND, (Integer)e.getItem());
          mTableModel.fireTableDataChanged();
        }
      }
    });
    pb2.add(orangeLabel, CC.xy(5,1));
    
    final ColorLabel redLabel = new ColorLabel(
        IDontWant2SeeSettingsTableRenderer.NOT_VALID_COLOR);
    redLabel.setText(mLocalizer.msg("invalid","Invalid"));
    pb2.add(redLabel, CC.xy(7,1));
    
    pb.add(pb2.getPanel(), CC.xyw(1,++y,5));
    
    y++;
    pb.add(add, CC.xy(1,++y));
    pb.add(removeDuplicateEntries, CC.xy(3, y));
    pb.add(delete, CC.xy(5,y++));
    pb.add(UiUtilities.createHelpTextArea(mLocalizer.msg("settings.help",
    "To edit a value double click a cell. You can use wildcard * to search for any text.")), CC.xyw(1,++y,5));
  }
  
  private void removeDuplicateRows() {
    ArrayList<Integer> deleteEntries = new ArrayList<Integer>();
    
    for(int i = mTableModel.getRowCount()-1; i >= 0; i--) {
      String testRowText = (String)mTableModel.getValueAt(i, 0);
      boolean testRowCase = (Boolean)mTableModel.getValueAt(i, 1);

      Pattern testPattern = IDontWant2SeeListEntry.createSearchPattern(testRowText, testRowCase);
      
      for(int j = i-1; j >= 0; j--) {
        try {
          String actualRowText = (String)mTableModel.getValueAt(j, 0);
          boolean actualRowCase = (Boolean)mTableModel.getValueAt(j, 1);
          
          if(testRowCase == actualRowCase && testRowText.equals(actualRowText)) {
            if(!deleteEntries.contains(j)) {
              deleteEntries.add(j);
            }
          }
          else {
            if(testPattern.matcher(actualRowText).matches()) {
              if(!deleteEntries.contains(j)) {
                deleteEntries.add(j);
              }
            }
            else {
              if(IDontWant2SeeListEntry.createSearchPattern(actualRowText, actualRowCase).matcher(testRowText).matches()) {
                if(!deleteEntries.contains(i)) {
                  deleteEntries.add(i);
                }
              }
            }
          }
        }catch(Throwable t) {t.printStackTrace();}
      }
    }
    
    Integer[] values = deleteEntries.toArray(new Integer[deleteEntries.size()]);
    Arrays.sort(values);
    
    for(int i = values.length - 1; i >= 0; i--) {
      mTableModel.deleteRow(values[i]);
    }
    
    if(!deleteEntries.isEmpty()) {
      JOptionPane.showMessageDialog(this, mLocalizer.msg("settings.infoMsg.1","{0} duplicate entries were removed.",deleteEntries.size()));
    }
    else {
      JOptionPane.showMessageDialog(this, mLocalizer.msg("settings.infoMsg.2","No duplicate entries were found."));
    }
  }
  
  private void deleteSelectedRows() {
    final int selectedIndex = mTable.getSelectedRow();
    final int[] selection = mTable.getSelectedRows();
    
    for(int i = selection.length-1; i >= 0; i--) {
      mTableModel.deleteRow(selection[i]);
    }
    
    if ((selectedIndex > 0) && (selectedIndex<mTable.getRowCount())) {
      mTable.setRowSelectionInterval(selectedIndex,selectedIndex);
    }
    else if(mTable.getRowCount() > 0) {
      if(mTable.getRowCount() - selectedIndex > 0) {
        mTable.setRowSelectionInterval(0,0);
      }
      else {
        mTable.setRowSelectionInterval(mTable.getRowCount()-1,mTable.getRowCount()-1);
      }
    }
    
    if(mTable.getSelectedRow() >= 0) {
      mTable.scrollRectToVisible(mTable.getCellRect(mTable.getSelectedRow(), 0, true));
    }
  }
  
  protected void saveSettings(IDontWant2SeeSettings settings) {try {
    if(mTable.isEditing()) {
      mTable.getCellEditor().stopCellEditing();
    }
    
    settings.setSearchList(mTableModel.getChangedList());
    
    if(mTableModel.getLastChangedValue() != null) {
      settings.setLastEnteredExclusionString(mTableModel.getLastChangedValue());
    }
    
    IDontWant2See.getInstance().updateFilter(true);}catch(Throwable t) {t.printStackTrace();}
  }
  
  private static final class ColorLabelComboBox extends JPanel {
    private static final int GAP = 5;
    private ColorLabel mColorLabel;
    private JComboBox<Integer> mSelection;
    
    public ColorLabelComboBox(final Color color, final Integer selection) {
      mColorLabel = new ColorLabel(color);
      mSelection = new JComboBox<Integer>();
      
      mSelection.addItem(7);
      mSelection.addItem(14);
      mSelection.addItem(30);
      mSelection.addItem(90);
      mSelection.addItem(180);
      mSelection.addItem(365);
      mSelection.setSelectedItem(selection);
      
      setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
      
      final String pre = mLocalizer.msg("unusedSince.pre", "Not used for");
      final String post = mLocalizer.msg("unusedSince.post", "days");
      
      add(mColorLabel);
      
      if(!pre.trim().isEmpty()) {
        add(Box.createRigidArea(new Dimension(GAP, 0)));
        add(new JLabel(pre));
      }
      
      add(Box.createRigidArea(new Dimension(GAP, 0)));
      add(mSelection);
      
      if(!post.trim().isEmpty()) {
        add(Box.createRigidArea(new Dimension(GAP, 0)));
        add(new JLabel(post));
      }
    }
    
    public void addItemListener(final ItemListener listener) {
      mSelection.addItemListener(listener);
    }
  }
}

