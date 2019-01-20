package util.io.windows.registry;

import java.io.File;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import util.browserlauncher.Launch;
import util.io.ExecutionHandler;

public class RegistryKey {
	public static final String HKEY_LOCAL_MACHINE = "HKLM";
	public static final String HKEY_CURRENT_USER = "HKCU";
	
	private String mKey;
	private String mPath;
	
	private static final File mRegTool = new File(System.getenv("windir")+File.separator+"SysWOW64"+File.separator+"reg.exe");
	
	private Pattern mPatternQuery = Pattern.compile("\\s{2,}(.*?)\\s+(REG_.*?)\\s+(.*?)$",Pattern.DOTALL);
	
	public static boolean isUsable() {
		return mRegTool.isFile();
	}
	
	public RegistryKey(final String key, final String path) throws RuntimeException {
		if(!isUsable() || Launch.getOs() != Launch.OS_WINDOWS) {
			throw new RuntimeException("Reg tool '" + mRegTool.getAbsolutePath() + "' not available. No access to Windows Registry");
		}
		
		mKey = key;
		mPath = path;
	}
	
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
