package util.io.windows.registry;

public class RegistryValue {
	static final int TYPE_REG_UNKNOWN = 0;
	public static final int TYPE_REG_SZ = 1;
	public static final int TYPE_REG_BINARY = 2;
	public static final int TYPE_REG_DWORD = 3;
	public static final int TYPE_REG_QWORD = 4;
	
	private int mType;
	private String mName;
	private String mValue;
	
	public RegistryValue(final String name, final int type, final String value) {
		mName = name;
		mType = type;
		mValue = value;
	}
	
	public String getName() {
		return mName;
	}
	
	public int getType() {
		return mType;
	}
	
	public String getData() {
		return mValue;
	}
	
	public boolean isUnknown() {
		return mType == TYPE_REG_UNKNOWN;
	}
	
	public boolean isRegSz() {
		return mType == TYPE_REG_SZ;
	}
	
	public boolean isRegBinary() {
		return mType == TYPE_REG_BINARY;
	}
	
	public boolean isRegDword() {
		return mType == TYPE_REG_DWORD;
	}
	
	public boolean isRegQword() {
		return mType == TYPE_REG_QWORD;
	}
	
}
