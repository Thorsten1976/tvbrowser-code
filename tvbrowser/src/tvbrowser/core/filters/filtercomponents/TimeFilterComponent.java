/*
 * TV-Browser Copyright (C) 04-2003 Martin Oberhauser
 * (darras@users.sourceforge.net)
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 * 
 * CVS information: $RCSfile$ $Source:
 * /cvsroot/tvbrowser/tvbrowser/src/tvbrowser/core/filters/filtercomponents/TimeFilterComponent.java,v $
 * $Date$ $Author$ $Revision$
 */

package tvbrowser.core.filters.filtercomponents;

import java.awt.BorderLayout;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Calendar;
import java.util.Date;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.factories.DefaultComponentFactory;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.Program;
import tvbrowser.core.Settings;
import util.ui.CaretPositionCorrector;

public class TimeFilterComponent extends AbstractFilterComponent {

  private static final util.ui.Localizer mLocalizer = util.ui.Localizer
      .getLocalizerFor(TimeFilterComponent.class);

  private JSpinner mFromTimeSp, mToTimeSp;

  private int mFromTime, mToTime;

  /**
   * show programs which are already running when the time span begins
   */
  private boolean mShowRunning;

  private JCheckBox mIncludeBtn;

  public TimeFilterComponent() {
    this("", "");
  }

  public TimeFilterComponent(String name, String description) {
    super(name, description);
    mFromTime = 16 * 60;
    mToTime = 23 * 60;

    mShowRunning = false;
  }

  public void read(ObjectInputStream in, int version) throws IOException {

    if (version == 1) {
      mFromTime = in.readInt() * 60;
      mToTime = (in.readInt() % 24) * 60;
    } else {
      mFromTime = in.readInt();
      mToTime = in.readInt();
      if (version == 3) {
        mShowRunning = in.readBoolean();
      }
    }
  }
  
  public String getTypeDescription() {
    return mLocalizer.msg("desc", "");
  }

  @Override
  public String toString() {
    return mLocalizer.msg("Time", "Time");
  }

  public void saveSettings() {
    mFromTime = getTimeFromDate((Date) mFromTimeSp.getValue());
    mToTime = getTimeFromDate((Date) mToTimeSp.getValue());
    mShowRunning = mIncludeBtn.isSelected();
  }

  public void write(ObjectOutputStream out) throws IOException {
    out.writeInt(mFromTime);
    out.writeInt(mToTime);
    out.writeBoolean(mShowRunning);
  }

  public JPanel getSettingsPanel() {
    JPanel content = new JPanel(new FormLayout("5dlu,default,3dlu,default:grow",
        "5dlu,default,5dlu,default,1dlu,default,5dlu,default"));

    mFromTimeSp = new JSpinner(new SpinnerDateModel());
    JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(mFromTimeSp,
        Settings.getTimePattern());
    mFromTimeSp.setEditor(dateEditor);
    mFromTimeSp.setValue(setTimeToDate(mFromTime));
    CaretPositionCorrector.createCorrector(dateEditor.getTextField(),
        new char[] { ':' }, -1);

    mToTimeSp = new JSpinner(new SpinnerDateModel());
    dateEditor = new JSpinner.DateEditor(mToTimeSp, Settings.getTimePattern());
    mToTimeSp.setEditor(dateEditor);
    mToTimeSp.setValue(setTimeToDate(mToTime));
    CaretPositionCorrector.createCorrector(dateEditor.getTextField(),
        new char[] { ':' }, -1);
    
    mIncludeBtn = new JCheckBox(mLocalizer.msg("includeRunning",
        "Include programs running at start time"));
    mIncludeBtn.setSelected(mShowRunning);
    
    content.add(DefaultComponentFactory.getInstance().createSeparator(mLocalizer.msg(
        "TimeOfDay", "Time of day")), CC.xyw(1, 2, 4));
    
    content.add(new JLabel(mLocalizer.msg("from", "from")), CC.xy(2, 4));
    content.add(mFromTimeSp, CC.xy(4, 4));
    content.add(new JLabel(mLocalizer.msg("till", "till")), CC.xy(2, 6));
    content.add(mToTimeSp, CC.xy(4, 6));
    content.add(mIncludeBtn, CC.xyw(2, 8, 3));

    JPanel centerPanel = new JPanel(new BorderLayout());
    centerPanel.add(content, BorderLayout.NORTH);
    return centerPanel;
  }

  public Date setTimeToDate(int minutes) {
    Calendar cal = Calendar.getInstance();
    cal.set(Calendar.HOUR_OF_DAY, minutes / 60);
    cal.set(Calendar.MINUTE, minutes % 60);
    return cal.getTime();
  }

  public int getTimeFromDate(Date time) {
    Calendar cal = Calendar.getInstance();
    cal.setTime(time);
    return cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE);
  }

  public boolean accept(Program program) {
    int start = program.getStartTime();
    int end = start + program.getLength();

    // From-To spans over 2 Days
    if (mToTime < mFromTime) {
      if (start >= mFromTime) {
        return true;
      }

      if (start < mToTime) {
        return true;
      }

      if (mShowRunning) {
        if (start < mFromTime && end > mFromTime) {
          return true;
        }
      }
    }

    // Normal-Mode
    return (start >= mFromTime && start < mToTime)
        || (mShowRunning && start < mFromTime && end > mFromTime);
  }

  public int getVersion() {
    return 3;
  }

}