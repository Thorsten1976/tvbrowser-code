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
package tvbrowser.core.filters.filtercomponents;

import java.awt.BorderLayout;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

import tvbrowser.core.TvDataSearcher;
import tvbrowser.core.filters.FilterComponent;
import util.exc.ErrorHandler;
import util.exc.TvBrowserException;
import util.ui.SearchForm;
import util.ui.SearchFormSettings;
import util.ui.UiUtilities;
import devplugin.Program;
import devplugin.ProgramFieldType;

public class KeywordFilterComponent implements FilterComponent {

  private static final util.ui.Localizer mLocalizer =
    util.ui.Localizer.getLocalizerFor(KeywordFilterComponent.class);

  private SearchForm mSearchForm;
  
  private Pattern mPattern;
  private ProgramFieldType[] mSearchFieldArr;

  private String mDescription, mName;

  private SearchFormSettings mSearchFormSettings;


  public KeywordFilterComponent(String name, String desc) {
    setSearchFormSettings(new SearchFormSettings(""));
    mName = name;
    mDescription = desc;
  }

  public KeywordFilterComponent() {
    this("", "");
  }

  public void read(ObjectInputStream in, int version)
    throws IOException, ClassNotFoundException
  {
    setSearchFormSettings(new SearchFormSettings(in));
  }

  public void write(ObjectOutputStream out) throws IOException {
    mSearchFormSettings.writeData(out);
  }


  private void setSearchFormSettings(SearchFormSettings settings) {
    mSearchFormSettings = settings;
    
    String regex = mSearchFormSettings.getSearchTextAsRegex();
    boolean caseSensitive = mSearchFormSettings.getCaseSensitive();
    try {
      mPattern = TvDataSearcher.getInstance().createSearchPattern(regex, caseSensitive);
    } catch (TvBrowserException exc) {
      ErrorHandler.handle(exc);
    }
      
    mSearchFieldArr = mSearchFormSettings.getFieldTypes();
  }


  public void ok() {
    mSearchFormSettings = mSearchForm.getSearchFormSettings();
  }

  public boolean accept(Program program) {
    return TvDataSearcher.getInstance().matches(mPattern, program,
      mSearchFieldArr);
  }

  public JPanel getPanel() {
    String msg;
    
    JPanel content = new JPanel();
    content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

    JPanel pn = new JPanel(new BorderLayout());
    pn.setBorder(BorderFactory.createEmptyBorder(3, 0, 5, 0));
    msg = mLocalizer.msg("description", "Accept all programs containing the following keyword:");
    pn.add(UiUtilities.createHelpTextArea(msg));
    content.add(pn);
    
    mSearchForm = new SearchForm(false, false);
    mSearchForm.setSearchFormSettings(mSearchFormSettings);
    content.add(mSearchForm);

    return content;
  }

  public String toString() {
    return mLocalizer.msg("keyword", "keyword");
  }

  public int getVersion() {
    return 1;
  }

  public String getName() {
    return mName;
  }

  public String getDescription() {
    return mDescription;
  }

  public void setName(String name) {
    mName = name;
  }

  public void setDescription(String desc) {
    mDescription = desc;
  }

}
