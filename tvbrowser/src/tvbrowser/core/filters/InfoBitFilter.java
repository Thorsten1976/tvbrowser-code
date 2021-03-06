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
 *     $Date: 2011-07-24 22:09:15 +0200 (So, 24 Jul 2011) $
 *   $Author: ds10 $
 * $Revision: 7060 $
 */
package tvbrowser.core.filters;

import devplugin.Program;
import devplugin.ProgramFilter;

/**
 * This Filter filters for infobits, depending on the given key name
 */
public class InfoBitFilter implements ProgramFilter {
  public static final String SUBTITLE_FILTER_KEY = "[SUBTITLE_FILTER]";
  public static final String AUDIO_DESCRIPTION_FILTER_KEY = "[AUDIO_DESCRIPTION_FILTER]";
  public static final String HD_FILTER_KEY = "[HD_FILTER]";
  public static final String NEW_FILTER_KEY = "[NEW_FILTER]";
  public static final String ORIGINAL_AUDIO_FILTER_KEY = "[ORIGINAL_AUDIO_FILTER]";
  public static final String ARTS_FILTER_KEY = "[ARTS_FILTER]";
  public static final String CHILDRENS_FILTER_KEY = "[CHILDRENS_FILTER]";
  public static final String DOCUMENTARY_FILTER_KEY = "[DOCUMENTARY_FILTER]";
  public static final String MAGAZINE_FILTER_KEY = "[MAGAZINE_FILTER]";
  public static final String MOVIE_FILTER_KEY = "[MOVIE_FILTER]";
  public static final String NEWS_FILTER_KEY = "[NEWS_FILTER]";
  public static final String OTHERS_FILTER_KEY = "[OTHERS_FILTER]";
  public static final String SERIES_FILTER_KEY = "[SERIES_FILTER]";
  public static final String SHOW_FILTER_KEY = "[SHOW_FILTER]";
  public static final String SPORTS_FILTER_KEY = "[SPORTS_FILTER]";
  public static final String UNCATEGORIZED_FILTER_KEY = "[UNCATEGORIZED_FILTER]";
  
  private int[] mInfoBits;
  private String mName;
  private String mKey;
  private String mLocalized;

  public InfoBitFilter(String name) {
    
    util.ui.Localizer catLocalizer = util.ui.Localizer.getLocalizerFor(devplugin.ProgramInfoHelper.class);
    
    if (name.equals(SUBTITLE_FILTER_KEY)) {
      mName = "Subtitled";
      mKey = name;
      mInfoBits = new int[] {
          Program.INFO_SUBTITLE_FOR_AURALLY_HANDICAPPED, Program.INFO_ORIGINAL_WITH_SUBTITLE, Program.INFO_SIGN_LANGUAGE};
    } else if (name.equals(AUDIO_DESCRIPTION_FILTER_KEY)) {
      mName = "Audiodescription";
      mKey = name;
      mInfoBits = new int[] {
          Program.INFO_AUDIO_DESCRIPTION};
    } else if (name.equals(HD_FILTER_KEY)) {
      mName = "HD";
      mKey = name;
      mInfoBits = new int[] { Program.INFO_VISION_HD };
    } else if (name.equals(NEW_FILTER_KEY)) {
      mName = "New";
      mKey = name;
      mInfoBits = new int[] { Program.INFO_NEW };
    } else if (name.equals(ORIGINAL_AUDIO_FILTER_KEY)) {
      mName = "OriginalAudio";
      mKey = name;
      mInfoBits = new int[] { Program.INFO_ORIGINAL_WITH_SUBTITLE, Program.INFO_AUDIO_TWO_CHANNEL_TONE };
    }
    else if (name.equals(ARTS_FILTER_KEY)) {
      mLocalized = catLocalizer.msg("categorie_arts", "Theater/Concert");
      mKey = name;
      mInfoBits = new int[] {Program.INFO_CATEGORIE_ARTS};
    } else if (name.equals(CHILDRENS_FILTER_KEY)) {
      mLocalized = catLocalizer.msg("categorie_childrens", "Children's Programming");
      mKey = name;
      mInfoBits = new int[] {Program.INFO_CATEGORIE_CHILDRENS};
    } else if (name.equals(DOCUMENTARY_FILTER_KEY)) {
      mLocalized = catLocalizer.msg("categorie_documentary", "Documentary/Reportage");
      mKey = name;
      mInfoBits = new int[] {Program.INFO_CATEGORIE_DOCUMENTARY};
    } else if (name.equals(MAGAZINE_FILTER_KEY)) {
      mLocalized = catLocalizer.msg("categorie_magazine_infotainment", "Magazine/Infotainment");
      mKey = name;
      mInfoBits = new int[] {Program.INFO_CATEGORIE_MAGAZINE_INFOTAINMENT};
    } else if (name.equals(MOVIE_FILTER_KEY)) {
      mLocalized = catLocalizer.msg("categorie_movie", "Movie");
      mKey = name;
      mInfoBits = new int[] {Program.INFO_CATEGORIE_MOVIE};
    } else if (name.equals(NEWS_FILTER_KEY)) {
      mLocalized = catLocalizer.msg("categorie_news", "News");
      mKey = name;
      mInfoBits = new int[] {Program.INFO_CATEGORIE_NEWS};
    } else if (name.equals(OTHERS_FILTER_KEY)) {
      mLocalized = catLocalizer.msg("categorie_others", "Other Program");
      mKey = name;
      mInfoBits = new int[] {Program.INFO_CATEGORIE_OTHERS};
    } else if (name.equals(SERIES_FILTER_KEY)) {
      mLocalized = catLocalizer.msg("categorie_series", "Series");
      mKey = name;
      mInfoBits = new int[] {Program.INFO_CATEGORIE_SERIES};
    } else if (name.equals(SHOW_FILTER_KEY)) {
      mLocalized = catLocalizer.msg("categorie_show", "Show/Entertainment");
      mKey = name;
      mInfoBits = new int[] {Program.INFO_CATEGORIE_SHOW};
    } else if (name.equals(SPORTS_FILTER_KEY)) {
      mLocalized = catLocalizer.msg("categorie_sports", "Sports");
      mKey = name;
      mInfoBits = new int[] {Program.INFO_CATEGORIE_SPORTS};
    } else if (name.equals(UNCATEGORIZED_FILTER_KEY)) {
      mName = "Uncategorized";
      mKey = name;
      mInfoBits = new int[] {
          0,
          -Program.INFO_CATEGORIE_ARTS, 
          -Program.INFO_CATEGORIE_CHILDRENS, 
          -Program.INFO_CATEGORIE_DOCUMENTARY,
          -Program.INFO_CATEGORIE_MAGAZINE_INFOTAINMENT,
          -Program.INFO_CATEGORIE_MOVIE,
          -Program.INFO_CATEGORIE_NEWS,
          -Program.INFO_CATEGORIE_OTHERS,
          -Program.INFO_CATEGORIE_SERIES,
          -Program.INFO_CATEGORIE_SHOW,
          -Program.INFO_CATEGORIE_SPORTS};
    }
    
    else { 
      throw new IllegalArgumentException("Unknown filter: "+name);      
    }
  }
  
  public InfoBitFilter(String name, String key, int infoBit) {
    this(name, key, new int[] {infoBit});
  }
  
  public InfoBitFilter(String name, String key, int[] infoBits) {
    mName = name;
    mKey = key;
    mInfoBits = infoBits;
  }
  
  /**
   * Localizer
   */
  private static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(InfoBitFilter.class);

  /**
   * Accept only programs with subtitle or sign language
   *
   * @param prog Program to check
   * @return true if prog is subtitled
   */
  public boolean accept(devplugin.Program prog) {
    int info = prog.getInfo();
    if (info < 0) {
      info = 0;
    }
    

    boolean accept = false;
    for (int bit: mInfoBits) {
      if (bit >= 0) {
        accept = accept || bitSet(info, bit);
      } else {
        if (bitSet(info, -bit)) {
          return false;           
        }
      }
    }
    return accept;

  }

  /**
   * Checks if bits are set
   *
   * @param num     check in here
   * @param pattern this pattern
   * @return Pattern set?
   */
  private boolean bitSet(int num, int pattern) {
    if (num == 0) {
      return num == pattern;
    }
    return (num & pattern) == pattern;
  }
  

  public String getName() {
    return toString();
  }
  
  public String getKey() {
    return mKey;
  }


  /**
   * Name of Filter
   */
  public String toString() {
    if (mLocalized != null) {
      return mLocalized+"*";
    }
    return mLocalizer.msg(mName, mName)+"*";
  }

  public boolean equals(Object o) {
    return o instanceof ProgramFilter && getClass().equals(o.getClass())
        && getName().equals(((ProgramFilter) o).getName());
  }
}