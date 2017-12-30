package primarydatamanager.primarydataservice.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

import devplugin.Program;

/**
 * This class helps guessing the category (movie, series, show) for a program
 * using the genre
 *
 * @since 2.6
 */
public class InfoCategoryParser {

  private ArrayList<String> mUnknown = new ArrayList<String>();
  private ArrayList<Integer> mCategoryInfos = new ArrayList<Integer>();
  private ArrayList<ArrayList<String>> mGenres = new ArrayList<ArrayList<String>>();

  /**
   * Constructor
   */
  public InfoCategoryParser() {
    loadProperties(Program.INFO_CATEGORIE_MOVIE, "movies");
    loadProperties(Program.INFO_CATEGORIE_SERIES, "series");
    loadProperties(Program.INFO_CATEGORIE_NEWS, "news");
    loadProperties(Program.INFO_CATEGORIE_SHOW, "show");
    loadProperties(Program.INFO_CATEGORIE_MAGAZINE_INFOTAINMENT, "magazine");
    loadProperties(Program.INFO_CATEGORIE_DOCUMENTARY, "documentary");
    loadProperties(Program.INFO_CATEGORIE_ARTS, "arts");
    loadProperties(Program.INFO_CATEGORIE_SPORTS, "sports");
    loadProperties(Program.INFO_CATEGORIE_CHILDRENS, "childrens");
    loadProperties(Program.INFO_CATEGORIE_OTHERS, "others");
  }

  private void loadProperties(int infoCategory, String fileNamePart) {
    Properties properties = new Properties();
    
    File test = new File("category_" + fileNamePart + ".properties");
    
    System.out.println(test.getAbsolutePath() + ": " + test.isFile());
    
    InputStream stream = null;
    
    try {
      if(test.isFile()) {
        stream = new FileInputStream(test);
      }
      else {
        stream = InfoCategoryParser.class.getResourceAsStream("category_" + fileNamePart + ".properties");
      }
    } catch (IOException e1) {
      stream = InfoCategoryParser.class.getResourceAsStream("category_" + fileNamePart + ".properties");
      e1.printStackTrace();
    }
    
    ArrayList<String> genres = new ArrayList<String>();
    if (stream != null) {
      try {
        properties.load(stream);
        for (final Enumeration<?> genre = properties.propertyNames(); genre
            .hasMoreElements();) {
          String key = (String) genre.nextElement();
          if (key != null) {
            key = key.trim().toLowerCase();
            if (key.length() > 0) {
              genres.add(key);
            }
          }
        }
        mGenres.add(genres);
        mCategoryInfos.add(infoCategory);
      } catch (IOException e) {
        e.printStackTrace();
      } finally {
        if(stream != null) {
          try {
            stream.close();
          }catch(IOException ioe) {}
        }
      }
    }
  }

  public int getCategory(String genreText) {
    genreText = genreText.trim().replaceAll("(-| )", "").toLowerCase();

    if (StringUtils.isEmpty(genreText)) {
      return 0;
    }

    String[] genreParts = genreText.split(",");
    for (String genrePart : genreParts) {
      String genre = genrePart.trim();
      if (genre.length() > 0) {
        for (int i = 0; i < mGenres.size(); i++) {
          if (mGenres.get(i).contains(genre)) {
            return mCategoryInfos.get(i);
          }
        }

        if (!mUnknown.contains(genreText)) {
          mUnknown.add(genreText);
          System.out.println("Unknown Category : " + genreText);
        }
      }
    }

    return 0;
  }
}
