/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin@tvbrowser.org)
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
 *     $Date$
 *   $Author$
 * $Revision$
 */

package util.ui;

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import util.ui.customizableitems.SelectableItemList;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

import devplugin.Plugin;
import devplugin.PluginAccess;

/**
 * The PluginChooserDlg class provides a Dialog for choosing plugins. The user
 * can choose from all Plugins that are able to receive Programs.
 */
public class PluginChooserDlg extends JDialog implements WindowClosingIf {

  private static final long serialVersionUID = 1L;
  private PluginAccess[] mResultPluginArr;
  private PluginAccess[] mPluginArr;
  private SelectableItemList mPluginItemList;
  
  private static final util.ui.Localizer mLocalizer
     = util.ui.Localizer.getLocalizerFor(PluginChooserDlg.class);

  /**
   *
   * @param parent
   * @param pluginArr The initially selected Plugins.
   * @param description A description text below the Plugin list.
   */
  public PluginChooserDlg(Dialog parent, PluginAccess[] pluginArr, String description) {
    super(parent,true);
    init(pluginArr, description);
  }

  /**
   *
   * @param parent
   * @param pluginArr The initially selected Plugins.
   * @param description A description text below the Plugin list.
   */
  public PluginChooserDlg(Frame parent, PluginAccess[] pluginArr, String description) {
    super(parent,true);
    init(pluginArr, description);
  }
  
  private void init(PluginAccess[] channelArr, String description) {
    setTitle(mLocalizer.msg("title","Choose Plugins"));
    UiUtilities.registerForClosing(this);
    
    if (channelArr == null) {
      mPluginArr = new PluginAccess[]{};
      mResultPluginArr = new PluginAccess[]{};
    }
    else {
      mPluginArr = channelArr;
      mResultPluginArr = channelArr;
    }

    JPanel contentPane = (JPanel)getContentPane();
    FormLayout layout = new FormLayout("fill:pref:grow", "");
    contentPane.setLayout(layout);
    contentPane.setBorder(Borders.DLU4_BORDER);
    CellConstraints cc = new CellConstraints();
    
    PluginAccess[] pluginAccess = Plugin.getPluginManager().getActivatedPlugins();
    
    ArrayList list = new ArrayList();
    
    for(int i = 0; i < pluginAccess.length; i++)
      if(pluginAccess[i].canReceivePrograms())
        list.add(pluginAccess[i]);


    mPluginItemList = new SelectableItemList(mResultPluginArr, list.toArray());

    int pos = 1;
    layout.appendRow(new RowSpec("fill:pref:grow"));
    layout.appendRow(new RowSpec("3dlu"));
    contentPane.add(mPluginItemList, cc.xy(1,pos));   

    pos += 2;
    
    if (description != null) {
      JLabel lb = new JLabel(description);
      layout.appendRow(new RowSpec("pref"));
      layout.appendRow(new RowSpec("3dlu"));
      contentPane.add(lb, cc.xy(1,pos));
      pos += 2;
    }

    JButton okBt = new JButton(mLocalizer.msg("ok","OK"));
    JButton cancelBt = new JButton(mLocalizer.msg("cancel","Cancel"));

    okBt.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent event) {
        Object[] o = mPluginItemList.getSelection();
        mResultPluginArr = new PluginAccess[o.length];
        for (int i=0;i<o.length;i++) {
          mResultPluginArr[i]=(PluginAccess)o[i];
        }
        setVisible(false);
      }
      });

    cancelBt.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent event) {
        mResultPluginArr = null;
        setVisible(false);
      }
    });

    ButtonBarBuilder builder = new ButtonBarBuilder();
    builder.addGlue();
    builder.addGriddedButtons(new JButton[] {okBt, cancelBt});
    
    layout.appendRow(new RowSpec("pref"));
    contentPane.add(builder.getPanel(), cc.xy(1,pos));
    
    pack();
  }

  /**
   *
   * @return an array of the selected plugins. If the user cancelled the dialog,
   * the array from the constructor call is returned.
   */
  public PluginAccess[] getPlugins() {

    if (mResultPluginArr==null) {
      return mPluginArr;
    }
    return mResultPluginArr;
  }

  public void close() {
    setVisible(false);
  }

}