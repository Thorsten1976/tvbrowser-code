/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin_oat@yahoo.de)
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

package util.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Insets;
import java.util.HashSet;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.Date;
import devplugin.Program;
import tvbrowser.core.Settings;
import util.settings.ProgramPanelSettings;

/**
 * A list cell renderer that renders Programs.
 * <p>
 * <i>Keep in mind:</i> This Renderer internally uses "static" data for each
 * displayed program. If program data changes the container using this renderer
 * should be repainted to display the changed data.
 *
 * @author Til Schneider, www.murfman.de
 */
public class ProgramListCellRenderer extends DefaultListCellRenderer {

  private static final class ProgramListChangeListener implements ChangeListener {
    private final JList<?> mList;

    private ProgramListChangeListener(JList<?> list) {
      mList = list;
    }

    public void stateChanged(ChangeEvent e) {
      if (mList != null) {
        Object source = e.getSource();
        if (source instanceof Program) {
          Program program = (Program) source;
          AbstractListModel<?> model = (AbstractListModel<?>) mList.getModel();
          ListDataListener[] listeners = model.getListDataListeners();
          int itemIndex = -1;
          for (int i = 0; i < model.getSize(); i++) {
            if (model.getElementAt(i) == program) {
              itemIndex = i;
              break;
            }
          }
          if (itemIndex >= 0) {
            for (ListDataListener listener : listeners) {
              listener.contentsChanged(new ListDataEvent(program, ListDataEvent.CONTENTS_CHANGED, itemIndex, itemIndex));
            }
          }
        }
      }
    }
  }

  private static Color FIRST_ROW_COLOR = new Color(220, 220, 220, 150);
  private static Color FIRST_ROW_COLOR_EXPIRED = new Color(220, 220, 220, 55);

  private static Color SECOND_ROW_COLOR = new Color(220, 220, 220, 150);
  private static Color SECOND_ROW_COLOR_EXPIRED = new Color(220, 220, 220, 55);

  private JPanel mMainPanel;
  private JLabel mHeaderLb;
  private ProgramPanel mProgramPanel;
  private HashSet<Program> mProgramSet = new HashSet<Program>();
  private JPanel mDateSeparator;
  private JLabel mDateLabel;

  /**
   * Creates a new instance of ProgramListCellRenderer
   */
  public ProgramListCellRenderer() {    
    this(new ProgramPanelSettings(ProgramPanelSettings.SHOW_PICTURES_NEVER, -1, -1, false, true, 10));
  }

  /**
   * Creates a new instance of ProgramListCellRenderer
   *
   * @param settings
   *          The settings for the program panel.
   *
   * @since 2.2.2
   */
  public ProgramListCellRenderer(ProgramPanelSettings settings) {
    SECOND_ROW_COLOR = UIManager.getColor("List.foreground");
    SECOND_ROW_COLOR = new Color(SECOND_ROW_COLOR.getRed(),SECOND_ROW_COLOR.getGreen(),SECOND_ROW_COLOR.getBlue(),30);
    
    SECOND_ROW_COLOR_EXPIRED = UIManager.getColor("List.foreground");
    SECOND_ROW_COLOR_EXPIRED = new Color(SECOND_ROW_COLOR_EXPIRED.getRed(),SECOND_ROW_COLOR_EXPIRED.getGreen(),SECOND_ROW_COLOR_EXPIRED.getBlue(),15);
    
    FIRST_ROW_COLOR = UIManager.getColor("List.background");
    FIRST_ROW_COLOR = new Color(FIRST_ROW_COLOR.getRed(),FIRST_ROW_COLOR.getGreen(),FIRST_ROW_COLOR.getBlue(),30);

    FIRST_ROW_COLOR_EXPIRED = UIManager.getColor("List.background");
    FIRST_ROW_COLOR_EXPIRED = new Color(FIRST_ROW_COLOR_EXPIRED.getRed(),FIRST_ROW_COLOR_EXPIRED.getGreen(),FIRST_ROW_COLOR_EXPIRED.getBlue(),15);

    initializeSettings(settings);
  }

  private void initializeSettings(ProgramPanelSettings settings) {
    mMainPanel = new JPanel(new BorderLayout());
    mMainPanel.setOpaque(true);

    mHeaderLb = new JLabel();
    mMainPanel.add(mHeaderLb, BorderLayout.NORTH);
    
    mProgramPanel = new ProgramPanel(settings);
    mMainPanel.add(mProgramPanel, BorderLayout.CENTER);
    
    Settings.addFontChangeListener(e -> {
      mProgramPanel.forceRepaint();
    });
    
    mDateSeparator = new JPanel(new FormLayout("0dlu:grow,default,0dlu:grow","5dlu,default,5dlu"));
    mDateSeparator.setBorder(BorderFactory.createMatteBorder(2, 0, 2, 0, UIManager.getColor("Label.foreground")));
    
    mDateLabel = new JLabel();
    mDateLabel.setFont(mDateLabel.getFont().deriveFont(mDateLabel.getFont().getSize2D() + 4).deriveFont(Font.BOLD));
    
    mDateSeparator.add(mDateLabel, new CellConstraints().xy(2, 2));
  }

  /**
   * Return a component that has been configured to display the specified value.
   * That component's <code>paint</code> method is then called to "render" the
   * cell. If it is necessary to compute the dimensions of a list because the
   * list cells do not have a fixed size, this method is called to generate a
   * component on which <code>getPreferredSize</code> can be invoked.
   *
   * @param list
   *          The JList we're painting.
   * @param value
   *          The value returned by list.getModel().getElementAt(index).
   * @param index
   *          The cells index.
   * @param isSelected
   *          True if the specified cell was selected.
   * @param cellHasFocus
   *          True if the specified cell has the focus.
   * @return A component whose paint() method will render the specified value.
   *
   * @see JList
   * @see ListSelectionModel
   * @see ListModel
   */
  public Component getListCellRendererComponent(final JList<?> list, Object value, final int index, boolean isSelected,
      boolean cellHasFocus) {
    JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

    if (value != null && value instanceof Program) {
      Program program = (Program) value;
      
      Insets borderInsets = label.getBorder().getBorderInsets(label);
      
      mProgramPanel.setWidth(list.getWidth() - borderInsets.left - borderInsets.right);
      mProgramPanel.setProgram(program);
      mProgramPanel.setPaintExpiredProgramsPale(!isSelected);
      mProgramPanel.setBackground(label.getBackground());
      mProgramPanel.setSelectedInList(isSelected);
      
      if (!mProgramSet.contains(program)) {
        mProgramSet.add(program);
        program.addChangeListener(new ProgramListChangeListener(list));
      }

      StringBuilder labelString = new StringBuilder();
      int days = program.getDate().getNumberOfDaysSince(Date.getCurrentDate());

      switch (days) {
      case -1: {
        labelString.append(Localizer.getLocalization(Localizer.I18N_YESTERDAY));
        labelString.append(", ").append(program.getDateString());
        break;
      }
      case 0: {
        labelString.append(Localizer.getLocalization(Localizer.I18N_TODAY));
        labelString.append(", ").append(program.getDateString());
        break;
      }
      case 1: {
        labelString.append(Localizer.getLocalization(Localizer.I18N_TOMORROW));
        labelString.append(", ").append(program.getDateString());
        break;
      }
      default: {
        labelString.append(program.getDate().toString());
      }
      }

      labelString.append(" - ").append(program.getChannel().getName());
      mHeaderLb.setText(labelString.toString());

      if (program.isExpired() && !isSelected) {
        mHeaderLb.setForeground(Color.gray);
      } else {
        mHeaderLb.setForeground(mProgramPanel.getForeground());
      }
      
      if (isSelected) {
        mMainPanel.setBackground(Settings.propKeyboardSelectedColor.getColor());
      //  mMainPanel.setForeground(label.getForeground());
      }
      else {
        mMainPanel.setBackground(label.getBackground());
      }

      mMainPanel.setEnabled(label.isEnabled());
      mMainPanel.setBorder(label.getBorder());

      if ((!isSelected) && program.getMarkPriority() < Program.PRIORITY_MARK_MIN) {
        if(((index & 1) == 1)) {
          mMainPanel.setBackground(program.isExpired() ? SECOND_ROW_COLOR_EXPIRED : SECOND_ROW_COLOR);
        }
        else {
          mMainPanel.setBackground(program.isExpired() ? FIRST_ROW_COLOR_EXPIRED : FIRST_ROW_COLOR);
        }
      }

      return mMainPanel;
    }
    else if(value instanceof String && list.getModel().getSize() > index +1) {
      Object nextValue = list.getModel().getElementAt(index + 1);
      
      if(nextValue instanceof Program && list.getModel().getSize() > index + 1) {
        mDateLabel.setText(((Program)nextValue).getDateString());
        
        return mDateSeparator;
      }
    }
    
    return label;
  }
}
