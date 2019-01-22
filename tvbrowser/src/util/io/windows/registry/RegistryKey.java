/*
 * TV-Browser
 * Copyright (C) 2019 TV-Browser team (dev@tvbrowser.org)
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
 * SVN information:
 *     $Date$
 *   $Author$
 * $Revision$
 */
package util.io.windows.registry;

import java.io.File;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import util.browserlauncher.Launch;
import util.io.ExecutionHandler;

/**
 * Class to access windows registry.
 * 
 * @author René Mach
 * @since 4.1
 */
public class RegistryKey {
	public static final String HKEY_LOCAL_MACHINE = "HKLM";
	public static final String HKEY_CURRENT_USER = "HKCU";
	
	private String mKey;
	private String mPath;
	
	private static final File mRegTool = new File(System.getenv("windir")+File.separator+"SysWOW64"+File.separator+"reg.exe");
	
	private Pattern mPatternQuery = Pattern.compile("\\s{2,}(.*?)\\s+(REG_.*?)\\s+(.*?)$",Pattern.DOTALL);
	
	/**
	 * @return <code>true</code> if the registry is accessible, <code>false</code> otherwise.
	 */
	public static boolean isUsable() {
		return mRegTool.isFile();
	}
	
	/**
	 * Creates a registry key to access.
	 * 
	 * @param hkey The H key to access either {@value #HKEY_CURRENT_USER} or {@value #HKEY_LOCAL_MACHINE}
	 * @param path The path to access.
	 * @throws RuntimeException Thrown if the registry is not accessible or if operating system is not Windows.
	 */
	public RegistryKey(final String hkey, final String path) throws RuntimeException {
		if(!isUsable() || Launch.getOs() != Launch.OS_WINDOWS) {
			throw new RuntimeException("Reg tool '" + mRegTool.getAbsolutePath() + "' not available. No access to Windows Registry");
		}
		
		mKey = hkey;
		mPath = path;
	}
	
	
	/**
	 * Get the value of the given key.
	 * 
	 * @param key The key to get the value for.
	 * @return The result of the registry query.
	 */
	public RegistryValue getValue(final String key) {
		final ArrayList<String> cmdList = new ArrayList<>();
		cmdList.add(mRegTool.getAbsolutePath());
		cmdList.add("query");
		cmdList.add(mKey + "\\" + mPath);
		cmdList.add("/v");
		cmdList.add(key);
		
		final ExecutionHandler handler = new ExecutionHandler(cmdList.toArray(new String[0]));
		
		RegistryValue result = new RegistryValue(key, RegistryValue.TYPE_REG_UNKNOWN, "");
		
		try {
			handler.execute(true);
			handler.getProcess().waitFor();
			
			final Matcher m = mPatternQuery.matcher(handler.getOutput());
			int pos = 0;
			
			while(m.find(pos)) {
				if(key.equals(m.group(1))) {
					if("REG_DWORD".equals(m.group(2))) {
						result = new RegistryValue(key, RegistryValue.TYPE_REG_DWORD, String.valueOf(Long.parseLong(m.group(3).trim().replace("0x", ""), 16)));
					}
					else if("REG_SZ".equals(m.group(2))) {
						result = new RegistryValue(key, RegistryValue.TYPE_REG_SZ, m.group(3).trim());
					}
					else if("REG_QWORD".equals(m.group(2))) {
						result = new RegistryValue(key, RegistryValue.TYPE_REG_QWORD, String.valueOf(Long.parseLong(m.group(3).trim().replace("0x", ""))));
					}
					else if("REG_BINARY".equals(m.group(2))) {
						result = new RegistryValue(key, RegistryValue.TYPE_REG_BINARY, m.group(3).trim());
					}
					
					break;
				}
				
				pos = m.end();
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return result;
	}
}
