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
package timelistplugin;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import util.settings.ProgramPanelSettings;
import util.ui.ProgramPanel;
import devplugin.Program;

public class TimeListCellRenderer extends DefaultListCellRenderer {

  private static final Color SECOND_ROW_COLOR = new Color(230, 230, 230, 150);

  private static final Color SECOND_ROW_COLOR_EXPIRED = new Color(230, 230, 230, 55);

  private ProgramPanel mProgramPanel;

  private JPanel mMainPanel;

  protected TimeListCellRenderer() {
    super();
    final ProgramPanelSettings settings = new ProgramPanelSettings(
        ProgramPanelSettings.SHOW_PICTURES_NEVER, 0, 0, !TimeListPlugin
            .getInstance().isShowDescriptions(), false, 1000,
        ProgramPanelSettings.X_AXIS);
    mProgramPanel = new ProgramPanel(settings);

    mMainPanel = new JPanel(new BorderLayout());
    mMainPanel.setOpaque(true);

    mMainPanel.add(mProgramPanel, BorderLayout.CENTER);
  }

  @Override
  public Component getListCellRendererComponent(final JList list,
      final Object value, final int index, final boolean isSelected,
      final boolean cellHasFocus) {
    final JLabel label = (JLabel) super.getListCellRendererComponent(list,
        value,
        index, isSelected, cellHasFocus);

    if (value instanceof Program) {
      final Program program = (Program) value;

      mProgramPanel.setProgram(program);
      mProgramPanel.setTextColor(label.getForeground());

      program.addChangeListener(new ChangeListener() {
        public void stateChanged(final ChangeEvent e) {
          if (list != null) {
            list.repaint();
          }
        }
      });
      mMainPanel.setBackground(label.getBackground());

      if (isSelected) {
        mMainPanel.setForeground(label.getForeground());
      }

      mMainPanel.setEnabled(label.isEnabled());
      mMainPanel.setBorder(label.getBorder());

      if (((index & 1) == 1) && (!isSelected)
          && program.getMarkPriority() < Program.MIN_MARK_PRIORITY) {
        mMainPanel.setBackground(program.isExpired() ? SECOND_ROW_COLOR_EXPIRED
            : SECOND_ROW_COLOR );
      }

      return mMainPanel;
    }

    return label;
  }

}
