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
package tvbrowser.core.filters.filtercomponents;

import tvbrowser.core.filters.FilterComponent;

public abstract class AbstractFilterComponent implements FilterComponent {

  private String mName, mDescription;

  public AbstractFilterComponent(String name, String description) {
    setName(name);
    setDescription(description);
  }

  public String getName() {
    return mName;
  }

  public String getTypeDescription() {
    return null;
  }
  
  public String getDescription() {
    return mDescription;
  }

  public void setName(String name) {
    mName = name;
  }

  public void setDescription(String desc) {
    mDescription = desc;
  }

}
