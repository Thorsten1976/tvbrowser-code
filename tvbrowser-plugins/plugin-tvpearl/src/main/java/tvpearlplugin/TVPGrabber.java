/*
 * TV-Pearl by Reinhard Lehrbaum
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
 */
package tvpearlplugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import util.io.IOUtilities;

public class TVPGrabber
{
  /**
   * do not parse anything which was created more than a month ago
   */
  private static final Date MIN_CREATION_DATE;
  static {
  	Calendar cal = Calendar.getInstance();
  	cal.add(Calendar.MONTH, -1);
  	MIN_CREATION_DATE = cal.getTime();
  }

	/**
   * regular expression to grab the content of a TV pearl
   */
  private static final Pattern PATTERN_CONTENT = Pattern
      .compile("<p class=\"author\">.*?<a.*?href=\"([^&|#]*).*?#p(([0-9])*?)\".*?>.*?<a href=\"./memberlist.php?[^\"]*\"[^>]*>(.*?)</a></strong> &raquo; </span>(.*?)</p>[\\r\\n\\t ]*?<div class=\"content\">([\\w\\W]*?)</div>", Pattern.DOTALL);
  
  /**
   * getter for content pattern
   * @return PATTERN_CONTENT
   */
  public static Pattern getPatternContent() {
    return PATTERN_CONTENT;
  }

  /**
   * regular expression to grab the URL of the next forum page
   */
  private static final Pattern PATTERN_NEXT_URL = Pattern
      .compile("<a.*?href=\"(.*?)\".*?>N\u00e4chste.*?</a>");

  /**
   * regular expression to grab post count
   */
  private static final Pattern PATTERN_POST_COUNT = Pattern
	  .compile("<div class=\"pagination\">\\s*?(0|[1-9][0-9]*)\\s*?Beitr\u00e4ge");
  
  /**
   * regular expression to grab page start
   */
  private static final Pattern PATTERN_PAGE_START = Pattern
	  .compile("start=(0|[1-9][0-9]*)");
  
  private static final int LASTPOSTREAD = 100;
  
  /**
   * format of the post create date
   */
  private static final SimpleDateFormat FORMAT_CREATE_DATE = new SimpleDateFormat(
      "dd MMMM yyyy, HH:mm", Locale.GERMAN);

  private boolean mRecursiveGrab = true;
  private boolean mOnlyProgramInFuture = true;
	private String lastUrl = "";
	private HTTPConverter mConverter;

	public TVPGrabber()
	{
		mConverter = new HTTPConverter();
	}

	boolean getOnlyProgramInFuture()
	{
		return mOnlyProgramInFuture;
	}

	void setOnlyProgrammInFuture(final boolean onlyProgrammInFuture)
	{
		mOnlyProgramInFuture = onlyProgrammInFuture;
	}

	String getLastUrl()
	{
		return lastUrl;
	}

	List<TVPProgram> parse(final String url)
	{
	  final List<TVPProgram> programList = new ArrayList<TVPProgram>();

		if (url.length() > 0)
		{
			String workingUrl = url.trim();
			do
			{
				if (workingUrl.length() > 0)
				{
					lastUrl = workingUrl.replaceFirst("\\&sid=.*?\\&", "\\&");
				}
				final String webContent = downloadUrl(workingUrl);

				workingUrl = extentUrl(getNextUrl(webContent), url);
				
				parseContent(webContent, programList, url);
			}
			while (workingUrl.length() > 0 && mRecursiveGrab);

			if (workingUrl.length() > 0)
			{
				lastUrl = workingUrl;
			}
		}
		return programList;
	}

	private String downloadUrl(final String webUrl)
	{
    final StringBuilder buffer = new StringBuilder(1024);
		InputStream stream = null;
		BufferedReader in = null;
		try
		{
		  final URL url = new URL(webUrl);
			stream = IOUtilities.getStream(url, false);
      in = new BufferedReader(new InputStreamReader(stream,
          "UTF-8")); // ISO-8859-1
			String str;
			while ((str = in.readLine()) != null)
			{
				buffer.append(str).append('\n');
			}
		}
		catch (MalformedURLException e)
		{}
		catch (IOException e)
		{}
		finally {
		  if (in != null) {
        try {
          in.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
      if (stream != null) {
        try {
          stream.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }

		return buffer.toString();
	}

	private String getNextUrl(final String content)
	{
		//Pattern pattern = Pattern.compile("<a href=\"([^\"]*?)\">Weiter</a></b><br />");
    final Matcher matcher = PATTERN_NEXT_URL.matcher(content);

		String resultUrl = "";
		
		if (matcher.find())
		{
			resultUrl = matcher.group(1).trim();
			
			final Matcher postcnt_matcher = PATTERN_POST_COUNT.matcher(content);
				if (postcnt_matcher.find())
				{
					int startpost = Integer.parseInt(postcnt_matcher.group(1).trim()) - LASTPOSTREAD ;
					if (startpost<0) startpost = 0;
					final Matcher pagestart_matcher = PATTERN_PAGE_START.matcher(resultUrl);
					int pagestart = 0;
					if (pagestart_matcher.find()){
						pagestart = Integer.parseInt(pagestart_matcher.group(1).trim());
						if (pagestart < startpost) {
							resultUrl = pagestart_matcher.replaceFirst("start=" + Integer.toString(startpost));
						}
					} else {
						resultUrl = resultUrl + "&start=" + Integer.toString(startpost);
					}
					
				}
		}

		return resultUrl;
	}

	private String extentUrl(final String url, final String originalUrl)
	{
		String resultUrl = url;

		if (resultUrl.startsWith("."))
		{
			resultUrl = resultUrl.substring(1);
		}

		if (resultUrl.length() > 0)
		{
			int index = resultUrl.indexOf('.');
			index = originalUrl.indexOf(resultUrl.substring(0, index));
			resultUrl = originalUrl.substring(0, index) + resultUrl;
		}
		return resultUrl.replaceAll("&amp;", "&");
	}

	private void parseContent(final String content,
      final List<TVPProgram> programList, final String originalUrl)
	{
    final Matcher matcher = PATTERN_CONTENT.matcher(content);
		while (matcher.find())
		{
		  final String author = matcher.group(4).trim();
		  final String link = matcher.group(1).trim();
		  
		  final String contentUrl = extentUrl(link+"#p"+link.substring(link.indexOf("=")+1), originalUrl);
			final Date createDate = parseDate(matcher.group(5).trim());
			String itemContent = matcher.group(6);
			itemContent = itemContent.replace("\n", "").replaceAll("<br>", "\n").replaceAll("<.*?>", "");
	        
			if (createDate != null && createDate.after(MIN_CREATION_DATE))
			{
				parseInfo(itemContent, author, contentUrl, createDate, programList);
			}
		}
	}
	
	private synchronized Date parseDate(final String input) {
    String source = input.trim();
    try {
      return FORMAT_CREATE_DATE.parse(source);
    } catch (ParseException e) {
      final String[] monthNames = FORMAT_CREATE_DATE.getDateFormatSymbols().getMonths();
      for (int month = 0; month < 12; month++) {
        source = source.replace(monthNames[month].substring(0, 3) + " ",
            monthNames[month] + " ");
      }
      try {
        return FORMAT_CREATE_DATE.parse(source);
      } catch (ParseException e1) {
        TVPearlPlugin.getLogger().warning(
            "Unknown date format (" + source + ")");
      }
    }
    return null;
  }

	private void parseInfo(final String value, final String author,
      final String contentUrl,
      final Date createDate,
      final List<TVPProgram> programList)
	{
	  final Calendar today = Calendar.getInstance();
		String programName = "";
		String channel = "";
		Calendar start = Calendar.getInstance();
		String programInfo = "";

		String newChannel = "";
		Calendar newStart = null;
		boolean foundProgram = false;

		for (String line : value.split("\n"))
		{
		  
			boolean isHeader = false;

			final String[] items = line.split("[,|·]");
			
			if (items.length == 4)
			{

				if (isWeekday(items[0]))
				{
					newChannel = items[3].trim();
					newStart = parseStart(items[1].trim(), items[2].trim(), createDate);
					if (newChannel.length() > 0 && newStart != null)
					{
						if (programName.length() > 0)
						{
							programList.add(createProgram(author, contentUrl, createDate, programName, channel, start, programInfo));
						}
						foundProgram = true;
						channel = newChannel;
						start = newStart;
						programInfo = "";
						isHeader = true;
					}
				}
			}
			if (!isHeader)
			{
				if (foundProgram)
				{
					if (line.trim().length() > 0)
					{
						programName = line.trim();
						foundProgram = false;
					}
				}
				else
				{
					programInfo += line + "\n";
				}
			}
		}
		if (programName.length() > 0)
		{
			if ((mOnlyProgramInFuture && today.compareTo(start) < 0) || !mOnlyProgramInFuture)
			{
				programList.add(createProgram(author, contentUrl, createDate, programName, channel, start, programInfo));
			}
		}
	}

	private TVPProgram createProgram(final String author,
      final String contentUrl, final Date createDate, final String programName,
      final String channel, final Calendar start, final String programInfo)
	{
		TVPProgram program = null;

		if (programName.length() > 0)
		{
		  final Calendar cal = Calendar.getInstance();
      cal.setTime(createDate);
      final String title = mConverter.convertToString(programName);
      final String info = programInfo.trim().replaceAll("\\n.*:$", "").trim();
      program = new TVPProgram(author, contentUrl, cal, title, channel, start,
          info, "");
		}
		return program;
	}

	private boolean isWeekday(String value)
	{
	  value = value.trim();
	  for (String weekDay : FORMAT_CREATE_DATE.getDateFormatSymbols()
        .getWeekdays()) {
      if (weekDay.length() > 0) {
        if (value.equalsIgnoreCase(weekDay)
          || value.equalsIgnoreCase(weekDay.substring(0, 2))) {
          return true;
        }
      }
    }
    return false;
	}

	private Calendar parseStart(final String date, final String time,
      final Date createDate)
	{
		Calendar result = null;
		final Calendar create = Calendar.getInstance();
		create.setTime(createDate);

		try
		{
		  final SimpleDateFormat dateFormat1 = new SimpleDateFormat("d. MMM",
          Locale.GERMAN);
      final Date d = dateFormat1.parse(date);
			result = Calendar.getInstance();
			result.setTime(d);
			result.set(Calendar.YEAR, create.get(Calendar.YEAR));
		}
		catch (Exception e)
		{
			result = null;
		}

		if (result == null)
		{
			try
			{
			  final SimpleDateFormat dateFormat1 = new SimpleDateFormat("d.MMM",
            Locale.GERMAN);
        final Date d = dateFormat1.parse(date);
				result = Calendar.getInstance();
				result.setTime(d);
				result.set(Calendar.YEAR, create.get(Calendar.YEAR));
			}
			catch (Exception e)
			{
				result = null;
			}
		}

		if (result == null)
		{
		  final int index = date.indexOf('.');
      final int month = getMonth(date.substring(index + 1).trim());

			if (month >= 0 && month < 12)
			{
				result = Calendar.getInstance();
				result.set(create.get(Calendar.YEAR), month, Integer.parseInt(date.substring(0, index)), 0, 0, 0);
			}
			else
			{
				result = null;
			}
		}

		if (result != null)
		{
		  final Calendar limit = Calendar.getInstance();
			limit.setTime(createDate);
			limit.set(Calendar.DAY_OF_MONTH, limit.get(Calendar.DAY_OF_MONTH) - 1);

			if (result.compareTo(limit) < 0)
			{
				result.set(Calendar.YEAR, result.get(Calendar.YEAR) + 1);
			}

			final String[] splitTime = time.split("[: (-/]");
			if (splitTime.length > 1)
			{
				int delta = 0;
				try
				{
					Integer.parseInt(splitTime[0]);
				}
				catch (Exception e)
				{
					delta++;
				}

				try
				{
					result.set(Calendar.HOUR, Integer.parseInt(splitTime[0 + delta]));
					result.set(Calendar.MINUTE, Integer.parseInt(splitTime[1 + delta]));
				}
				catch (NumberFormatException e)
				{
					result = null;
				}
			}
			else
			{
				result = null;
			}
		}

		if (result == null)
		{
			TVPearlPlugin.getLogger().warning("Problem with start datetime [" + date + "][" + time + "]");
		}
		return result;
	}

	private int getMonth(String value)
	{
    value = value.trim();
    final String[] months = FORMAT_CREATE_DATE.getDateFormatSymbols()
        .getShortMonths();
    for (int i = 0; i < months.length; i++) {
      if (value.equalsIgnoreCase(months[i])) {
        return i;
      }
    }
    return -1;
	}
}
