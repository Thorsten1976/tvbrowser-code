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

/**
 * A value of a Windows Registry key.
 * 
 * @author René Mach
 * @since 4.1
 */
public class RegistryValue {
	static final int TYPE_REG_UNKNOWN = 0;
	public static final int TYPE_REG_SZ = 1;
	public static final int TYPE_REG_BINARY = 2;
	public static final int TYPE_REG_DWORD = 3;
	public static final int TYPE_REG_QWORD = 4;
	
	private int mType;
	private String mName;
	private String mValue;
	
	/**
	 * @param name The name of the registry key.
	 * @param type The type of the value either {@value #TYPE_REG_BINARY}, {@value #TYPE_REG_DWORD},
	 * {@value #TYPE_REG_QWORD}, {@value #TYPE_REG_SZ} or {@value #TYPE_REG_UNKNOWN}
	 * @param value The value of the key.
	 */
	public RegistryValue(final String name, final int type, final String value) {
		mName = name;
		mType = type;
		mValue = value;
	}
	
	/**
	 * @return The name of this Registry value.
	 */
	public String getName() {
		return mName;
	}
	
	/**
	 * @return The type of the value either {@value #TYPE_REG_BINARY}, {@value #TYPE_REG_DWORD},
	 * {@value #TYPE_REG_QWORD}, {@value #TYPE_REG_SZ} or {@value #TYPE_REG_UNKNOWN}
	 */
	public int getType() {
		return mType;
	}
	
	/**
	 * @return The data for this Registry value.
	 */
	public String getData() {
		return mValue;
	}
	
	/**
	 * @return <code>true</code> if the type of this Registry value is {@value #TYPE_REG_UNKNOWN},
	 * <code>false</code> otherwise.
	 */
	public boolean isUnknown() {
		return mType == TYPE_REG_UNKNOWN;
	}
	
	/**
	 * @return <code>true</code> if the type of this Registry value is {@value #TYPE_REG_SZ},
	 * <code>false</code> otherwise.
	 */
	public boolean isRegSz() {
		return mType == TYPE_REG_SZ;
	}
	
	/**
	 * @return <code>true</code> if the type of this Registry value is {@value #TYPE_REG_BINARY},
	 * <code>false</code> otherwise.
	 */
	public boolean isRegBinary() {
		return mType == TYPE_REG_BINARY;
	}
	
	/**
	 * @return <code>true</code> if the type of this Registry value is {@value #TYPE_REG_DWORD},
	 * <code>false</code> otherwise.
	 */
	public boolean isRegDword() {
		return mType == TYPE_REG_DWORD;
	}
	
	/**
	 * @return <code>true</code> if the type of this Registry value is {@value #TYPE_REG_QWORD},
	 * <code>false</code> otherwise.
	 */
	public boolean isRegQword() {
		return mType == TYPE_REG_QWORD;
	}
}
