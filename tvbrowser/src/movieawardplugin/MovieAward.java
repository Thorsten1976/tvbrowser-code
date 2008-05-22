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
 *     $Date: 2007-10-02 10:19:08 +0200 (Di, 02 Okt 2007) $
 *   $Author: Bananeweizen $
 * $Revision: 3966 $
 */
package movieawardplugin;

import au.com.bytecode.opencsv.CSVReader;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.io.IOException;
import java.util.logging.Level;

public class MovieAward {
  private static java.util.logging.Logger mLog = java.util.logging.Logger.getLogger(MovieAward.class.getName());

  private String mNameGerman;
  private String mNameEnglish;

  public MovieAward(String file, String nameEnglish, String nameGerman) {
    mNameEnglish = nameEnglish;
    mNameGerman = nameGerman;

    loadFromFile(file);
  }

  private void loadFromFile(String file) {
    InputStream stream = getClass().getResourceAsStream("data/" +  file);

    if (stream != null) {
      try {
        CSVReader reader = new CSVReader(new InputStreamReader(stream, "ISO-8859-15"), ';');

        String[] tokens;
        while ((tokens = reader.readNext()) != null) {
          System.out.println(tokens[0]);
        }

      } catch (UnsupportedEncodingException e) {
        mLog.log(Level.SEVERE, "Could not load data from " + file, e);
      } catch (IOException e) {
        mLog.log(Level.SEVERE, "Could not load data from " + file, e);
      }

    } else {
      mLog.warning("Could not load data from " + file);
    }
  }
}
