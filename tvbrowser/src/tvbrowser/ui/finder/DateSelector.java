package tvbrowser.ui.finder;

import javax.swing.JComponent;

import tvbrowser.core.DateListener;
import devplugin.Date;

public interface DateSelector {
  void setDateListener(DateListener dateChangedListener);
  JComponent getComponent();

  void updateContent();

  void markDate(Date d, boolean informPluginPanels);

  void markPreviousDate();

  void markNextDate();

  void markNextWeek();

  void markPreviousWeek();

  void markDate(Date d, Runnable callback, boolean informPluginPanels);

  void updateItems();

  Date getSelectedDate();
}
