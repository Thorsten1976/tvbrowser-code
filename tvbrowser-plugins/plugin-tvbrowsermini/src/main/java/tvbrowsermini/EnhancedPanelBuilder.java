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
package tvbrowsermini;

import javax.swing.JComponent;
import javax.swing.JPanel;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;

/**
 * panel builder with additional methods for typical TV-Browser UI (e.g. settings tab)
 * @deprecated use util.ui after 3.0 release
 *
 *
 */
public class EnhancedPanelBuilder extends PanelBuilder {
  public EnhancedPanelBuilder(FormLayout layout, JPanel panel) {
    super(layout,panel);
  }

  public EnhancedPanelBuilder(FormLayout layout) {
    super(layout);
  }

  public EnhancedPanelBuilder(final String encodedColumnSpecs) {
    super(new FormLayout(encodedColumnSpecs,""));
  }

  public EnhancedPanelBuilder(final String encodedColumnSpecs, JPanel panel) {
    super(new FormLayout(encodedColumnSpecs,""), panel);
  }

  /**
   * create a new section in the layout, which is separated from the previous line by a PARAGRAPH_GAP
   * @param label label string
   * @return the new separator component
   */
  public JComponent addParagraph(final String label) {
    if (getRowCount() > 0) {
      appendRow(FormSpecs.PARAGRAPH_GAP_ROWSPEC);
    }
    else {
      appendRow(FormSpecs.NARROW_LINE_GAP_ROWSPEC);
    }
    appendRow(FormSpecs.PREF_ROWSPEC);
    incrementRowNumber();
    if (label != null && label.length() > 0) {
      return addSeparator(label);
    }
    return null;
  }

  /**
   * add a new layout row to the builders layout<br>
   * It is separated from the preceding row with a LINE_GAP. Use {@link #getRow()} to address this line
   * afterwards.
   *
   * @return the builder
   */
  public PanelBuilder addRow() {
    return addRow(FormSpecs.PREF_ROWSPEC.encode());
  }

  private void incrementRowNumber() {
    // there is no line number zero, therefore only add one row, if we are still in the first line
    if (getRow() == 1) {
      nextRow();
    }
    else {
      nextRow(2);
    }
  }

  /**
   * add a new growing layout row to the builders layout<br>
   * It is separated from the preceding row by LINE_GAP and will grow to take the available space.
   * @return the builder
   */
  public PanelBuilder addGrowingRow() {
    return addRow("fill:default:grow");
  }

  /**
   * add a new layout row with the given height to the builders layout<br>
   * It is separated from the preceding row with a LINE_GAP. Use {@link #getRow()} to address this line
   * afterwards.
   * @param rowHeightCode row height
   * @return the builder
   */
  public PanelBuilder addRow(final String rowHeightCode) {
    appendRow(FormSpecs.LINE_GAP_ROWSPEC);
    appendRow(rowHeightCode);
    incrementRowNumber();
    return this;
  }

  /**
   * add a new layout row with the given height to the builders layout<br>
   * Use {@link #getRowCount()} to address this line
   * @param rowHeightCode row height
   * @return the builder
   */
  public PanelBuilder addSingleRow(final String rowHeightCode) {
    appendRow(rowHeightCode);
    incrementRowNumber();
    return this;
  }
}
