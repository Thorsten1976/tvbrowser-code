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
package tvbrowser.ui.filter.dlgs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

import org.apache.commons.lang3.StringUtils;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;

import tvbrowser.core.Settings;
import tvbrowser.core.filters.FilterComponent;
import tvbrowser.core.filters.FilterComponentList;
import tvbrowser.core.filters.FilterList;
import tvbrowser.core.filters.ParserException;
import tvbrowser.core.filters.UserFilter;
import tvbrowser.core.filters.filtercomponents.AcceptNoneFilterComponent;
import tvbrowser.core.filters.filtercomponents.SingleChannelFilterComponent;
import util.ui.DragAndDropMouseListener;
import util.ui.ListDragAndDropHandler;
import util.ui.ListDropAction;
import util.ui.Localizer;
import util.ui.TVBrowserIcons;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;

public class EditFilterDlg extends JDialog implements ActionListener, DocumentListener, CaretListener, WindowClosingIf, ListDropAction<FilterItem> {

  private static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(EditFilterDlg.class);

  private static final util.ui.Localizer mFilterLocalizer = util.ui.Localizer.getLocalizerFor(UserFilter.class);

  private JButton mNewBtn, mEditBtn, mRemoveBtn, mOkBtn, mCancelBtn;

  private Window mParent;

  private JTextField mFilterNameTF, mFilterRuleTF;

  private UserFilter mFilter = null;

  private JLabel mFilterRuleErrorLb, mColLb;

  private String mFilterName = null;

  private FilterList mFilterList;
  
  private JList<FilterItem> mFilterConstruction;
  private JList<FilterItem> mFilterComponentList;
  
  private DefaultListModel<FilterItem> mFilterComponentListModel;
  private DefaultListModel<FilterItem> mFilterConstructionListModel;
  
  private boolean mFromFilterList;
  private boolean mOkWasPressed;

  public EditFilterDlg(Window parent, FilterList filterList, UserFilter filter, boolean fromFilterList) {
    super(parent, ModalityType.DOCUMENT_MODAL);
    init(parent,filterList,filter, fromFilterList);
  }
  
  private void init(Window parent,FilterList filterList, UserFilter filter, boolean fromFilterList) {
    UiUtilities.registerForClosing(this);
    
    mFromFilterList = fromFilterList;
    mOkWasPressed = false;
    
    if (filter == null) {
      setTitle(mLocalizer.msg("titleNew", "Create filter"));
    } else {
      setTitle(mLocalizer.msg("titleEdit", "Edit filter {0}", filter.toString()));
      mFilterName = filter.toString();
    }
    
    mFilterList = filterList;
    mParent = parent;
    mFilter = filter;
    
    mFilterNameTF = new JTextField(new PlainDocument() {
      public void insertString(int offset, String str, AttributeSet a) throws BadLocationException {
        str = str.replaceAll("[\\p{Punct}&&[^_]]", "_");
        super.insertString(offset, str, a);
      }
    }, "", 30);
    mFilterNameTF.getDocument().addDocumentListener(this);
    mFilterNameTF.setEditable(fromFilterList);
    
    mFilterRuleTF = new JTextField();
    mFilterRuleTF.getDocument().addDocumentListener(this);
    mFilterRuleTF.addCaretListener(this);  
    
    FormLayout layout = new FormLayout("5dlu,fill:min:grow,5dlu,default,5dlu","default,5dlu,default,10dlu,default,5dlu,default,default,5dlu,default,5dlu,fill:min:grow,5dlu,default,5dlu,default,5dlu,default");
    PanelBuilder filterCreation = new PanelBuilder(layout);
    filterCreation.border(Borders.DIALOG);
    
    filterCreation.addSeparator(mLocalizer.msg("filterName", "Filter name:"), CC.xyw(1,1,5));
    filterCreation.add(mFilterNameTF, CC.xyw(2,3,3));
    filterCreation.addSeparator(mLocalizer.msg("ruleString", "Filter rule:"), CC.xyw(1,5,5));
    filterCreation.add(mFilterRuleTF, CC.xy(2,7));
    mColLb = filterCreation.addLabel("0", CC.xy(4,7));
    mFilterRuleErrorLb = filterCreation.addLabel(mLocalizer.msg("ruleExample",
    "example: component1 or (component2 and not component3)"), CC.xy(2,8));
    
    FormLayout filterCompLayout = new FormLayout("default:grow,5dlu,default","default,5dlu,default,5dlu,default,fill:min:grow");
    PanelBuilder filterComponents = new PanelBuilder(filterCompLayout);

    mNewBtn = new JButton(TVBrowserIcons.newIcon(TVBrowserIcons.SIZE_SMALL));
    mNewBtn.setToolTipText(mLocalizer.msg("newButton", "Create new filter component..."));
    mEditBtn = new JButton(TVBrowserIcons.edit(TVBrowserIcons.SIZE_SMALL));
    mEditBtn.setToolTipText(mLocalizer.msg("editButton", "Edit selected filter component..."));
    mRemoveBtn = new JButton(TVBrowserIcons.delete(TVBrowserIcons.SIZE_SMALL));
    mRemoveBtn.setToolTipText(mLocalizer.msg("removeButton", "Delete selected filter component"));

    mNewBtn.addActionListener(this);
    mEditBtn.addActionListener(this);
    mRemoveBtn.addActionListener(this);

    ButtonBarBuilder bottomBar = Utilities.createFilterButtonBar();

    mOkBtn = new JButton(Localizer.getLocalization(Localizer.I18N_OK));
    mOkBtn.addActionListener(this);
    getRootPane().setDefaultButton(mOkBtn);

    mCancelBtn = new JButton(Localizer.getLocalization(Localizer.I18N_CANCEL));
    mCancelBtn.addActionListener(this);

    bottomBar.addButton(new JButton[] {mOkBtn, mCancelBtn});

    FilterComponent[] fc = FilterComponentList.getInstance().getAvailableFilterComponents();

    Arrays.sort(fc, new FilterComponent.NameComparator());

    mFilterComponentListModel = new DefaultListModel<>();
    
    mFilterComponentList = new JList<>(mFilterComponentListModel);
    mFilterComponentList.setCellRenderer(new DefaultListCellRenderer() {      
      public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        JLabel label = (JLabel) super.getListCellRendererComponent(list,value,index,isSelected,cellHasFocus);
        
        if(value instanceof FilterItem) {
          FilterItem item = (FilterItem)value;
          
          if(item.isAndItem() || item.isNotItem() || item.isOrItem() || item.isOpenBracketItem() || item.isCloseBracketItem()) {
            label.setFont(label.getFont().deriveFont(Font.BOLD));
          }
          else if(item.getComponent().getDescription().length() > 0) {
            label.setText(label.getText() + " [" + item.getComponent().getDescription() + "]");
          }
          
          label.setText(FilterComponentList.getLabelForComponent(item.getComponent(), label.getText()));
        }
        
        return label;
      }
    });
    mFilterComponentList.addListSelectionListener(e -> {
      updateBtns();
    });
    mFilterComponentList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    
    mFilterComponentListModel.addElement(new FilterItem(FilterItem.AND_KEY,0));
    mFilterComponentListModel.addElement(new FilterItem(FilterItem.OR_KEY,0));
    mFilterComponentListModel.addElement(new FilterItem(FilterItem.NOT_KEY,0));
    mFilterComponentListModel.addElement(new FilterItem(FilterItem.OPEN_BRACKET_KEY,0));
    mFilterComponentListModel.addElement(new FilterItem(FilterItem.CLOSE_BRACKET_KEY,0));
    
    mFilterConstructionListModel = new DefaultListModel<>();
    mFilterConstruction = new JList<>(mFilterConstructionListModel);
    mFilterConstruction.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    mFilterConstruction.setCellRenderer(new DefaultListCellRenderer() {
      public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        JLabel label = (JLabel) super.getListCellRendererComponent(list,value,index,isSelected,cellHasFocus);
        try {
        if(value instanceof FilterItem) {
          FilterItem item = (FilterItem)value;
          
          FormLayout layout = new FormLayout("default:grow","default");
          
          for(int i = 0; i < item.getLevel(); i++) {
            layout.insertColumn(1,ColumnSpec.decode("9dlu"));
          }
          
          JPanel panel = new JPanel(layout);
          panel.setOpaque(isSelected);
          
          if(isSelected) {
            panel.setBackground(list.getSelectionBackground());
            label.setForeground(list.getSelectionForeground());
          }
          else {
            panel.setBackground(list.getBackground());
            label.setForeground(list.getForeground());
          }
          
          label.setOpaque(false);
          
          if(item.isAndItem() || item.isOrItem() || item.isNotItem()) {
            label.setFont(label.getFont().deriveFont(Font.BOLD));
            layout.insertColumn(1,ColumnSpec.decode("3dlu"));
            panel.add(label,CC.xy(Math.max(1,item.getLevel()+2),1));
          }
          else if(item.isOpenBracketItem() || item.isCloseBracketItem()) {
            label.setFont(label.getFont().deriveFont(Font.BOLD));
            panel.add(label,CC.xy(Math.max(1, item.getLevel()+1),1));
          }
          else {
            panel.add(label,CC.xy(Math.max(1,item.getLevel()+1),1));

            label.setText(FilterComponentList.getLabelForComponent(item.getComponent(), label.getText()));
          }

          if(index > 0) {
            FilterItem test = (FilterItem)list.getModel().getElementAt(index-1);
            FilterItem test2 = null;
            
            if(index < list.getModel().getSize() - 2) {
              test2 = (FilterItem)list.getModel().getElementAt(index+1);
            }
                        
            if(test.isCloseBracketItem() && !item.isAndItem() && !item.isOrItem() && !item.isCloseBracketItem()) {
              label.setForeground(Color.red);
            }
            else if(item.isNotItem() && ((!test.isAndItem() && !test.isOrItem()) || test.isNotItem()) && (test2 == null || test2.getComponent() == null)) {
              label.setForeground(Color.red);
            }
            else if((item.isAndItem() || item.isOrItem()) && (test.isAndItem() || test.isOrItem() || test.isOpenBracketItem())) {
              label.setForeground(Color.red);
            }
            else if(item.getComponent() != null && test2 != null && test2.isNotItem()) {
              label.setForeground(Color.red);
            }
            else if(test2 != null && test2.isOpenBracketItem() && !item.isAndItem() && !item.isOrItem() && !item.isNotItem() && !item.isOpenBracketItem()) {
              label.setForeground(Color.red);
            }            
            else if((index == list.getModel().getSize()-1) && (item.isAndItem() || item.isNotItem() || item.isOrItem() || item.isOpenBracketItem())) {
              label.setForeground(Color.red);
            }
            else if(item.getComponent() != null && test.getComponent() != null) {
              label.setForeground(Color.red);
            }
          }

          return panel;
        }
        }catch(Throwable t) {t.printStackTrace();}
        
        return label;
      }
    });
    
    if (mFilter != null) {
      mFilterName = filter.getName();
      mFilterNameTF.setText(mFilter.toString());
      mFilterRuleTF.setText(mFilter.getRule());
      fillFilterConstruction();
    }
    
    //Register DnD on the List.
    ListDragAndDropHandler dnDHandler = new ListDragAndDropHandler(mFilterComponentList, mFilterConstruction, this);
    new DragAndDropMouseListener<FilterItem>(mFilterComponentList,mFilterConstruction,this,dnDHandler,false);
    new DragAndDropMouseListener<FilterItem>(mFilterConstruction,mFilterComponentList,this,dnDHandler);
    
    mFilterComponentList.addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        mFilterComponentList.requestFocus();
      }
      public void mouseClicked(MouseEvent e) {
        
        if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) {
          int index = mFilterComponentList.locationToIndex(e.getPoint());
          mFilterComponentList.setSelectedIndex(index);
          editSelectedFilterComponent();
        }
      }
    });
    
    for (FilterComponent element : fc) {
      mFilterComponentListModel.addElement(new FilterItem(element,0));
    }
        
    filterComponents.add(mNewBtn, CC.xy(3,1));
    filterComponents.add(mEditBtn, CC.xy(3,3));
    filterComponents.add(mRemoveBtn, CC.xy(3,5));
    
    filterComponents.add(new JScrollPane(mFilterComponentList), CC.xywh(1,1,1,6));
    
    PanelBuilder listPanel = new PanelBuilder(new FormLayout("5dlu,min:grow,5dlu,10dlu,5dlu,min:grow,5dlu","default,5dlu,fill:min:grow"));
    listPanel.addSeparator(mLocalizer.msg("componentsTitle","Available filter components:"), CC.xyw(5,1,3));
    listPanel.add(filterComponents.getPanel(), CC.xy(6,3));
    listPanel.addSeparator(mLocalizer.msg("filterConstruction", "Filter construction"), CC.xyw(1,1,3));
    listPanel.add(new JScrollPane(mFilterConstruction), CC.xy(2,3));
    
    filterCreation.add(listPanel.getPanel(), CC.xyw(1,12,4));
    filterCreation.add(UiUtilities.createHelpTextArea(mLocalizer.msg("help","To create or edit a filter you can enter the rules in the text field or drag and drop the rules to the left side.")), CC.xyw(2,14,4));
    filterCreation.add(new JSeparator(JSeparator.HORIZONTAL), CC.xyw(1,16,4));
    filterCreation.add(bottomBar.getPanel(), CC.xyw(1,18,4));
    
    updateBtns();

    setMinimumSize(new Dimension(600,560));
    
    setLayout(new BorderLayout());
    
    add(filterCreation.getPanel(), BorderLayout.CENTER);
    
    Settings.layoutWindow("editFilterDlg",this,getMinimumSize(),mParent);
    setVisible(true);
  }
  

  private void updateBtns() {
    if (mFilterComponentList == null) {
      return;
    }
    if(mFilterComponentList.getSelectedIndex() > 0) {
      FilterItem item = (FilterItem)mFilterComponentList.getSelectedValue();
      
      mRemoveBtn.setEnabled(!item.isAndItem() && !item.isOrItem() && !item.isNotItem() && !item.isOpenBracketItem() && !item.isCloseBracketItem() && !(item.getComponent() instanceof SingleChannelFilterComponent));
      
      mEditBtn.setEnabled(mRemoveBtn.isEnabled() && !(item.getComponent() instanceof AcceptNoneFilterComponent));
    }
    else {
      mEditBtn.setEnabled(false);
      mRemoveBtn.setEnabled(false);
    }

    boolean validRule = true;
    
    try {
      UserFilter.testTokenTree(mFilterRuleTF.getText(),false);
      mFilterRuleErrorLb.setForeground(UIManager.getColor("Label.foreground"));
      mFilterRuleErrorLb.setText(mLocalizer.msg("ruleExample",
      "example: component1 or (component2 and not component3)"));
    } catch (ParserException e) {
      mFilterRuleErrorLb.setForeground(Color.red);
      mFilterRuleErrorLb.setText(e.getMessage());
      validRule = false;
    }

    if(mFilterRuleTF.hasFocus()) {
      fillFilterConstruction();
    }
    
    mOkBtn.setEnabled(StringUtils.isNotBlank(mFilterNameTF.getText()) && mFilterComponentList.getModel().getSize() > 0 && validRule);
  }

  public void actionPerformed(ActionEvent e) {

    Object o = e.getSource();
    if (o == mNewBtn) {
      EditFilterComponentDlg dlg = new EditFilterComponentDlg(this);
            
      FilterComponent rule = dlg.getFilterComponent();
      if (rule != null) {
        mFilterComponentListModel.addElement(new FilterItem(rule,0));
        
        tvbrowser.core.filters.FilterComponentList.getInstance().add(rule);
        String text = mFilterRuleTF.getText();
        if (text.length() > 0) {
          text += " " + mFilterLocalizer.msg("or", "or") + " ";
        }
        text += rule.getName();
        mFilterRuleTF.setText(text);
        fillFilterConstruction();
      }
    } else if (o == mEditBtn) {
      editSelectedFilterComponent();

    } else if (o == mFilterComponentList) {
      updateBtns();
    } else if (o == mRemoveBtn) {
      boolean allowRemove = true;
      UserFilter[] userFilterArr = mFilterList.getUserFilterArr();
      FilterComponent fc = ((FilterItem)mFilterComponentListModel.getElementAt(mFilterComponentList.getSelectedIndex())).getComponent();

      // Create the Filter based on the new Rule and check if the FC exists
      // there
      UserFilter testFilter = new UserFilter("test");

      try {
        testFilter.setRule(mFilterRuleTF.getText());

        if (testFilter.containsRuleComponent(fc.getName())) {
          allowRemove = false;
          JOptionPane.showMessageDialog(this, mLocalizer.msg("usedByAnotherFilter",
              "This filter component is used by filter '{0}'\nRemove the filter first.", mFilterNameTF.getText()));
        }
      } catch (Exception ex) {
        // Filter creation failed, assume the old one is correct
        if ((mFilter != null) && (mFilter.containsRuleComponent(fc.getName()))) {
          allowRemove = false;
          JOptionPane.showMessageDialog(this, mLocalizer.msg("usedByAnotherFilter",
              "This filter component is used by filter '{0}'\nRemove the filter first.", mFilterNameTF.getText()));
        }
      }

      for (int i = 0; i < userFilterArr.length && allowRemove; i++) {
        if ((userFilterArr[i] != mFilter) && userFilterArr[i].containsRuleComponent(fc.getName())) {
          allowRemove = false;
          JOptionPane.showMessageDialog(this, mLocalizer.msg("usedByAnotherFilter",
              "This filter component is used by filter '{0}'\nRemove the filter first.", userFilterArr[i].toString()));
        }
      }
      if (allowRemove) {
        FilterComponentList.getInstance().remove(fc.getName());
        mFilterComponentListModel.removeElementAt(mFilterComponentList.getSelectedIndex());
        updateBtns();
      }

    } else if (o == mOkBtn) {
      mOkWasPressed = true;
      
      if(mFromFilterList) {
        String filterName = mFilterNameTF.getText();
        if (!filterName.equalsIgnoreCase(mFilterName) && mFilterList.containsFilter(filterName)) {
          JOptionPane
              .showMessageDialog(this, mLocalizer.msg("alreadyExists", "Filter '{0}' already exists.", filterName));
          mOkWasPressed = false;
        } else {
          if (mFilter == null) {
            mFilter = new UserFilter(mFilterNameTF.getText());
          } else {
            mFilter.setName(mFilterNameTF.getText());
          }
  
          try {
            mFilter.setRule(mFilterRuleTF.getText());
            FilterComponentList.getInstance().store();
            setVisible(false);
          } catch (ParserException exc) {
            mOkWasPressed = false;
            JOptionPane.showMessageDialog(this, mLocalizer.msg("invalidRule", "Invalid rule: ") + exc.getMessage());
          }
        }
      }
      else {
        try {
          mFilter.setRule(mFilterRuleTF.getText());
          FilterComponentList.getInstance().store();
          setVisible(false);
        } catch (ParserException e1) {
          mOkWasPressed = false;
          JOptionPane.showMessageDialog(this, mLocalizer.msg("invalidRule", "Invalid rule: ") + e1.getMessage());
        }
      }
    } else if (o == mCancelBtn) {
      FilterComponentList.getInstance().store();
      setVisible(false);
    }

  }
  
  public boolean getOkWasPressed() {
    return mOkWasPressed;
  }

  public UserFilter getUserFilter() {
    return mFilter;
  }

  public void changedUpdate(DocumentEvent e) {
    updateBtns();
  }

  public void insertUpdate(DocumentEvent e) {
    updateBtns();
  }

  public void removeUpdate(DocumentEvent e) {
    updateBtns();
  }

  public void caretUpdate(javax.swing.event.CaretEvent e) {
    mColLb.setText("pos: " + mFilterRuleTF.getCaretPosition());
  }

  public void close() {
    setVisible(false);
  }
  /*
  private class FilterItem {    
    private String mRuleType;
    private FilterComponent mComponent;
    private int mLevel;
    
    private FilterItem(String ruleType, int level, FilterComponent comp) {
      mRuleType = ruleType;
      mLevel = level;
      mComponent = comp;
    }
    
    public FilterItem(String ruleType, int level) {
      mRuleType = ruleType;
      mComponent = null;
      mLevel = level;
    }
    public FilterItem(FilterComponent comp, int level) {
      mRuleType = null;
      mComponent = comp;
      mLevel = level;
    }
    
    public String toString() {
      if(mRuleType != null) {
        if(getLocale().getLanguage().equals("de")) {
          if(mRuleType.equals(AND_KEY)) {
            return "UND";
          }
          else if(mRuleType.equals(OR_KEY)) {
            return "ODER";
          }
          else if(mRuleType.equals(NOT_KEY)) {
            return "NICHT";
          } 
        }
        else {
          if(mRuleType.equals(AND_KEY)) {
            return "AND";
          }
          else if(mRuleType.equals(OR_KEY)) {
            return "OR";
          }
          else if(mRuleType.equals(NOT_KEY)) {
            return "NOT";
          } 
        }
        
        if(mRuleType.equals(OPEN_BRACKET_KEY)) {
          return "(";
        }
        else if(mRuleType.equals(CLOSE_BRACKET_KEY)) {
          return ")";
        }
      }
      return mComponent.getName();
    }
    
    public void setLevel(int level) {
      mLevel = level;
    }
    
    public int getLevel() {
      return mLevel;
    }
    
    public FilterComponent getComponent() {
      return mComponent;
    }
    
    public FilterItem clone(int level) {
      return new FilterItem(mRuleType,level,mComponent);
    }
    
    public boolean isOpenBracketItem() {
      return mRuleType != null && mRuleType.equals(OPEN_BRACKET_KEY);
    }
    
    public boolean isCloseBracketItem() {
      return mRuleType != null && mRuleType.equals(CLOSE_BRACKET_KEY);
    }
    
    public boolean isAndItem() {
      return mRuleType != null && mRuleType.equals(AND_KEY);
    }

    public boolean isOrItem() {
      return mRuleType != null && mRuleType.equals(OR_KEY);
    }
    
    public boolean isNotItem() {
      return mRuleType != null && mRuleType.equals(NOT_KEY);
    }
  }*/
  
  public boolean checkValueForRuleType(String value, String ruleType) {
    if(ruleType != null) {
      if(ruleType.equals(FilterItem.AND_KEY) && (value.toLowerCase().equals("and") || value.toLowerCase().equals("und"))) {
        return true;
      }
      else if(ruleType.equals(FilterItem.OR_KEY) && (value.toLowerCase().equals("or") || value.toLowerCase().equals("oder"))) {
        return true;
      }
      else if(ruleType.equals(FilterItem.NOT_KEY) && (value.toLowerCase().equals("not") || value.toLowerCase().equals("nicht"))) {
        return true;
      }
      else if(ruleType.equals(FilterItem.OPEN_BRACKET_KEY) && value.toLowerCase().equals("(")) {
        return true;
      }
      else if(ruleType.equals(FilterItem.CLOSE_BRACKET_KEY) && value.toLowerCase().equals(")")) {
        return true;
      }
    }
    
    return false;
  }

  @Override
  public void drop(JList<FilterItem> source, JList<FilterItem> target, int row, boolean move) {try {
    mFilterNameTF.requestFocus();
    
    if(target.equals(mFilterConstruction)) {
      if(source.equals(mFilterComponentList)) {
        FilterItem value = (FilterItem)source.getSelectedValue();
        ((DefaultListModel<FilterItem>)target.getModel()).add(row,value.clone(0));
        
        int level = 0;
        
        for(int i = 0; i < target.getModel().getSize(); i++) {
          FilterItem item = (FilterItem)target.getModel().getElementAt(i);
          
          if(item.toString().equals("(")) {
            item.setLevel(level++);
          }
          else if(item.toString().equals(")")) {
            level--;
            level = Math.max(level,0);
            item.setLevel(level);
          }
          else {
            item.setLevel(level);
          }

        }        
      }
      else {
        UiUtilities.moveSelectedItems(target,row,false);
        
        int level = 0;
        
        for(int i = 0; i < target.getModel().getSize(); i++) {
          FilterItem item = (FilterItem)target.getModel().getElementAt(i);
          
          if(item.toString().equals("(")) {
            item.setLevel(level++);
          }
          else if(item.toString().equals(")")) {
            level--;
            level = Math.max(level,0);
            item.setLevel(level);
          }
          else {
            item.setLevel(level);
          }

        }
      }
    }
    else if(source.equals(mFilterConstruction)){
      ((DefaultListModel<FilterItem>)source.getModel()).remove(source.getSelectedIndex());
      
      
      int level = 0;
      
      for(int i = 0; i < source.getModel().getSize(); i++) {
        FilterItem item = (FilterItem)source.getModel().getElementAt(i);
        
        if(item.toString().equals("(")) {
          item.setLevel(level++);
        }
        else if(item.toString().equals(")")) {
          level--;
          level = Math.max(level,0);
          item.setLevel(level);
        }
        else {
          item.setLevel(level);
        }
      }
    }
        
    StringBuilder build = new StringBuilder();
    
    for(int i = 0; i < mFilterConstruction.getModel().getSize(); i++) {
      build.append(mFilterConstruction.getModel().getElementAt(i).toString()).append(" ");
    }
    
    if(build.length() > 0) {
      build.delete(build.length()-1,build.length());
    }
    
    mFilterRuleTF.setText(build.toString());
    
    if(source != null) {
      source.repaint();
    }
    if(target != null) {
      target.repaint();
    }
  }catch(Throwable t) {t.printStackTrace();}
  }
  
  private void fillFilterConstruction() {
    mFilterConstructionListModel.clear();
    
    ArrayList<String> values = new ArrayList<String>();
    
    String rule = mFilterRuleTF.getText();
    
    for(int i = rule.length()-1; i >= 0; i--) {
      
      if(rule.charAt(i) == ' ') {
        
        values.add(0,rule.substring(i).trim());
        rule = rule.substring(0,i).trim();
        i = rule.length();
      }
      else if(rule.charAt(i) == '(' || rule.charAt(i) == ')') {
        String test = rule.substring(i+1).trim();
        
        if(test.length() > 0) {
          values.add(0,test);
        }
        values.add(0,rule.substring(i,i+1));
        rule = rule.substring(0,i).trim();
        i = rule.length();
      }
    }
    
    if(!rule.isEmpty()) {
      values.add(0,rule.trim());
    }
    
    int level = 0;
    
    for(String value : values) {
      if(checkValueForRuleType(value,FilterItem.AND_KEY)) {
        mFilterConstructionListModel.addElement(new FilterItem(FilterItem.AND_KEY,level));
        
      }
      else if(checkValueForRuleType(value,FilterItem.OR_KEY)) {
        mFilterConstructionListModel.addElement(new FilterItem(FilterItem.OR_KEY,level));
        
      }
      else if(checkValueForRuleType(value,FilterItem.NOT_KEY)) {
        mFilterConstructionListModel.addElement(new FilterItem(FilterItem.NOT_KEY,level));
        
      }
      else if(checkValueForRuleType(value,FilterItem.OPEN_BRACKET_KEY)) {
        mFilterConstructionListModel.addElement(new FilterItem(FilterItem.OPEN_BRACKET_KEY,level));
        level++;
        
      }
      else if(checkValueForRuleType(value,FilterItem.CLOSE_BRACKET_KEY)) {
        level--;
        mFilterConstructionListModel.addElement(new FilterItem(FilterItem.CLOSE_BRACKET_KEY,level));
        
      }
      else {
        for (FilterComponent element : FilterComponentList.getInstance().getAvailableFilterComponents()) {
          
          if(element.getName().equals(value)) {
            mFilterConstructionListModel.addElement(new FilterItem(element,level));
          }
        }
      }
      level = Math.max(level,0);
    }
  }

  private void editSelectedFilterComponent() {
    int inx = mFilterComponentList.getSelectedIndex();

    if(inx == -1) {
      return;
    }

    FilterComponent rule = ((FilterItem)mFilterComponentListModel.getElementAt(inx)).getComponent();
    final String oldName = rule.getName();
    FilterComponentList.getInstance().remove(oldName);
    mFilterComponentListModel.removeElementAt(inx);
    EditFilterComponentDlg dlg = null;
    
    if((mParent instanceof JFrame)) {
      dlg = new EditFilterComponentDlg((JFrame)mParent,rule);
    }
    else {
      dlg = new EditFilterComponentDlg((JDialog)mParent,rule);
    }
    
    FilterComponent newRule = dlg.getFilterComponent();
    if (newRule == null) {
      newRule = rule;
    }
    
    FilterComponentList.getInstance().add(newRule);
    FilterTreeModel.getInstance().updateFilterComponent(oldName, newRule);
    
    if(!oldName.equalsIgnoreCase(newRule.getName())) {
      FilterComponentList.getInstance().remove(oldName);
      
      final String[] parts = mFilterRuleTF.getText().split("\\s+");
      final StringBuilder newRuleText = new StringBuilder();
      
      for(int i = 0; i < parts.length; i++) {
        if(parts[i].equals(oldName)) {
          parts[i] = newRule.getName();
        }
        
        if(newRuleText.length() > 0) {
          newRuleText.append(" ");
        }
        
        newRuleText.append(parts[i]);
      }
      
      mFilterRuleTF.setText(newRuleText.toString());
    }
    
    mFilterComponentListModel.addElement(new FilterItem(newRule,0));
    mFilterConstruction.repaint();
    
    updateBtns();
  }
}