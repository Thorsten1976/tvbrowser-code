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
package tvbrowserdataservice;

import java.util.ArrayList;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

public class TvBrowserDataServiceSettings {
  private static final String KEY_GROUPNAME = "groupname";
  private static final String LEVEL_SEPARATOR = ":::";
  private static final String KEY_LEVEL = "level";
  private static final String KEY_SHOW_NEWS = "showNews";
  private static final String KEY_LAST_GROUP_NEWS_DATE_PREFIX = "lastGroupNewsDate_";
  private static final String KEY_LAST_SHOWN_GROUP_NEWS_DATE_PREFIX = "lastShownGroupNewsDate_";
  private static final String KEY_LAST_FORCED_UPDATE_DATE_PREFIX = "lastForcedUpdateDate_";

  public TvBrowserDataServiceSettings(final Properties properties) {
    if (properties != null) {
      mProperties = properties;
    } else {
      mProperties = new Properties();
    }
  }

  private Properties mProperties;

  public String[] getLevelIds() {
    String tvDataLevel = mProperties.getProperty(KEY_LEVEL,
        "base:::more00-16:::more16-00:::picture00-16:::picture16-00");
    if (tvDataLevel.indexOf("image") != -1) {
      tvDataLevel = StringUtils.replace(tvDataLevel, "image", "picture");
    }
    return tvDataLevel.split(LEVEL_SEPARATOR);
  }

  public Properties storeSettings() {
    return mProperties;
  }

  public String getGroupName() {
    return mProperties.getProperty(KEY_GROUPNAME);
  }

  public void setGroupName(final String groupName) {
    mProperties.setProperty(KEY_GROUPNAME, groupName);
  }

  public void setLevelIds(final ArrayList<String> levelIds) {
    StringBuilder builder = new StringBuilder();
    for (String levelId : levelIds) {
      if (builder.length() > 0) {
        builder.append(LEVEL_SEPARATOR);
      }
      builder.append(levelId);
    }
    mProperties.setProperty(KEY_LEVEL, builder.toString());
  }

  public void setGroupUrls(final String id, final String urls) {
    mProperties.setProperty("group_" + id, urls);
  }

  public String getGroupUrls(final String id) {
    return mProperties.getProperty("group_"+id,"");
  }

  public String getProvider(final String id) {
    return mProperties.getProperty(id + "_provider");
  }

  public void setProvider(final String id, final String providerName) {
    mProperties.setProperty(id + "_provider", providerName);
  }
  
  public boolean showNews() {
    return mProperties.getProperty(KEY_SHOW_NEWS,"true").equals("true");
  }
  
  public void setShowNews(boolean showNews) {
    mProperties.setProperty(KEY_SHOW_NEWS, String.valueOf(showNews));
  }
  
  public long getLastGroupNewsDateForGroupId(String groupId) {
    return Long.parseLong(mProperties.getProperty(KEY_LAST_GROUP_NEWS_DATE_PREFIX+groupId, "0"));
  }
  
  public void setLastGroupNewsDateForGroupId(String groupId, long value) {
    mProperties.setProperty(KEY_LAST_GROUP_NEWS_DATE_PREFIX+groupId, String.valueOf(value));
  }
  
  public long getLastShownGroupNewsDateForGroupId(String groupId) {
    return Long.parseLong(mProperties.getProperty(KEY_LAST_SHOWN_GROUP_NEWS_DATE_PREFIX+groupId,"0"));
  }
  
  public void setLastShownGroupNewsDateForGroupId(String groupId, long value) {
    mProperties.setProperty(KEY_LAST_SHOWN_GROUP_NEWS_DATE_PREFIX+groupId, String.valueOf(value));
  }
  
  public long getLastForcedUpdateDateForGroupId(String groupId) {
    return Long.parseLong(mProperties.getProperty(KEY_LAST_FORCED_UPDATE_DATE_PREFIX+groupId, "0"));
  }
  
  public void setLastForcedUpdateDateForGroupId(String groupId, long value) {
    mProperties.setProperty(KEY_LAST_FORCED_UPDATE_DATE_PREFIX+groupId, String.valueOf(value));
  }
}
