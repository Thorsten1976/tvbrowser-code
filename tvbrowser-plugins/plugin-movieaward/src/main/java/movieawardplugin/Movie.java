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
 *     $Date: 2007-10-02 10:19:08 +0200 (Di, 02 Okt 2007) $
 *   $Author: Bananeweizen $
 * $Revision: 3966 $
 */
package movieawardplugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import devplugin.Program;
import devplugin.ProgramFieldType;

public class Movie {
  private String mId;
  private int mYear;
  private String mDirector;
  private HashMap<String, String> mTitles = new HashMap<String, String>(4);
  private HashMap<String, ArrayList<String>> mAlternativeTitles = new HashMap<String, ArrayList<String>>(
      4);
  private String mOriginalTitle;

  public Movie(final String id) {
    mId = id;
  }
  public String getId() {
    return mId;
  }

  public void addTitle(final String lang, String title,
      final boolean original) {
    title = title.trim();
    if (original) {
      mOriginalTitle = title;
    }
    mTitles.put(MovieAwardPlugin.poolString(lang), title);
  }

  public void setProductionYear(final int year) {
    mYear = year;
  }

  public int getProductionYear() {
    return mYear;
  }

  public void setYear(final int year) {
    mYear = year;
  }

  public String getDirector() {
    return mDirector;
  }

  public void setDirector(final String director) {
    mDirector = director;
  }

  public void addAlternativeTitle(final String lang, final String title) {
    ArrayList<String> list = mAlternativeTitles.get(lang);

    if (list == null) {
      list = new ArrayList<String>();
      mAlternativeTitles.put(lang, list);
    }
    list.add(title.trim().toLowerCase());
  }

  public boolean matchesProgram(final Program program) {
    // avoid String comparison by filtering for year first
    final int year = program.getIntField(ProgramFieldType.PRODUCTION_YEAR_TYPE);
    if (year > 0 && mYear > 0) {
      if (!(year >= mYear - 2 && year <= mYear + 2)) {
        return false;
      }
    }
    // store all multiple used variables to avoid re-getting
    final String country = program.getChannel().getCountry();
    final String localizedTitle = mTitles.get(country);
    final String programTitle = program.getTitle();
    if (programTitle.equalsIgnoreCase(localizedTitle)) {
      return true;
    }
    final String originalTitle =
      program.getTextField(ProgramFieldType.ORIGINAL_TITLE_TYPE);
    String movieOriginalTitle = mOriginalTitle;
    if (movieOriginalTitle == null && mTitles.size() == 1) {
      //only 1 title given, assume it's also the original
      movieOriginalTitle = mTitles.entrySet().iterator().next().getValue();
    }

    if ((movieOriginalTitle != null
        && (programTitle.equalsIgnoreCase(movieOriginalTitle)
            || (originalTitle != null
                && originalTitle.equalsIgnoreCase(movieOriginalTitle))))) {
      return true;
    }
    // do not use toLowerCase on each program repeatedly
    final List<String> alternativeTitles = mAlternativeTitles.get(country);
    if (alternativeTitles != null) {
      for (String alternateTitle : alternativeTitles) {
        if (programTitle.equalsIgnoreCase(alternateTitle)) {
          return true;
        }
      }
    }

    //try all titles
    for (final String title : mTitles.values()) {
      if (title.equalsIgnoreCase(programTitle)) {
        return true;
      }
    }
    for (final ArrayList<String> alternatives : mAlternativeTitles.values()) {
      for (final String title : alternatives) {
        if (title.equalsIgnoreCase(programTitle)) {
          return true;
        }
      }
    }

    return false;
  }

  public final HashMap<String, String> getTitles() {
    return mTitles;
  }

  public final HashMap<String, ArrayList<String>> getAlternativeTitles() {
    return mAlternativeTitles;
  }

}
