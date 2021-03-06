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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

import devplugin.Plugin;
import devplugin.ProgramReceiveIf;
import devplugin.ProgramReceiveTarget;
import tvbrowser.core.Settings;
import util.ui.customizableitems.SelectableItem;
import util.ui.customizableitems.SelectableItemList;

/**
 * The PluginChooserDlg class provides a Dialog for choosing plugins. The user
 * can choose from all Plugins that are able to receive Programs.
 */
public class PluginChooserDlg extends JDialog implements WindowClosingIf {

  private ProgramReceiveIf[] mResultPluginArr;
  private Hashtable<ProgramReceiveIf,ArrayList<ProgramReceiveTarget>> mReceiveTargetTable;
  private SelectableItemList<ProgramReceiveIf> mPluginItemList;
  private ProgramReceiveTarget[] mCurrentTargets;
  private boolean mOkWasPressed;

  private static final util.ui.Localizer mLocalizer
     = util.ui.Localizer.getLocalizerFor(PluginChooserDlg.class);

  /**
   *
   * @param parent The parent window.
   * @param pluginArr
   *          The initially selected ProgramReceiveIfs.
   * @param description
   *          A description text below the ProgramReceiveIf list.
   * @param caller
   *          The caller ProgramReceiveIf.
   * @since 3.0
   */
  public PluginChooserDlg(Window parent, ProgramReceiveIf[] pluginArr,
      String description, ProgramReceiveIf caller) {
    super(parent);
    setModalityType(ModalityType.DOCUMENT_MODAL);
    init(pluginArr, description, caller, null, null, parent);
  }

  /**
   *
   * @param parent The parent window
   * @param pluginArr
   *          The initially selected ProgramReceiveIfs.
   * @param description
   *          A description text below the ProgramReceiveIf list.
   * @param caller
   *          The caller ProgramReceiveIf.
   * @since 3.0
   */
  public PluginChooserDlg(Window parent, ProgramReceiveTarget[] pluginArr,
      String description, ProgramReceiveIf caller) {
    this(parent, pluginArr,description, caller, null);
 }

  /**
   *
   * @param parent The parent window
   * @param pluginArr
   *          The initially selected ProgramReceiveIfs.
   * @param description
   *          A description text below the ProgramReceiveIf list.
   * @param caller
   *          The caller ProgramReceiveIf.
   * @param disabledTargets
   *          Targets that cannot be selected/deselected
   * @since 3.0
   */
  public PluginChooserDlg(Window parent, ProgramReceiveTarget[] pluginArr,
      String description, ProgramReceiveIf caller,
      ProgramReceiveTarget[] disabledTargets) {
    super(parent);
    setModalityType(ModalityType.DOCUMENT_MODAL);

    Hashtable<ProgramReceiveIf, ArrayList<ProgramReceiveTarget>> table = createReceiveTable(pluginArr);

    init(table.keySet().toArray(new ProgramReceiveIf[table.keySet().size()]),
        description, caller, table, disabledTargets, parent);
  }

  private Hashtable<ProgramReceiveIf,ArrayList<ProgramReceiveTarget>> createReceiveTable(ProgramReceiveTarget[] targets) {
    Hashtable<ProgramReceiveIf,ArrayList<ProgramReceiveTarget>> table = new Hashtable<ProgramReceiveIf,ArrayList<ProgramReceiveTarget>>();

    if(targets != null) {
      for(ProgramReceiveTarget target : targets) {
        if(target != null && target.getReceifeIfForIdOfTarget() != null) {
          ArrayList<ProgramReceiveTarget> receiveTargetList = table.get(target.getReceifeIfForIdOfTarget());

          if(receiveTargetList != null) {
            receiveTargetList.add(target);
          }
          else {
            receiveTargetList = new ArrayList<ProgramReceiveTarget>();
            receiveTargetList.add(target);

            table.put(target.getReceifeIfForIdOfTarget(),receiveTargetList);
          }
        }
      }
    }

    return table;
  }

  private void init(ProgramReceiveIf[] pluginArr, String description, ProgramReceiveIf caller, Hashtable<ProgramReceiveIf,ArrayList<ProgramReceiveTarget>> targetTable, final ProgramReceiveTarget[] disabledReceiveTargets, Window parent) {
    mOkWasPressed = false;
    setTitle(mLocalizer.msg("title","Choose Plugins"));
    UiUtilities.registerForClosing(this);

    if (pluginArr == null) {
      mResultPluginArr = new ProgramReceiveIf[]{};
      mReceiveTargetTable = new Hashtable<ProgramReceiveIf,ArrayList<ProgramReceiveTarget>>();
    }
    else {
      mResultPluginArr = pluginArr;
      mReceiveTargetTable = targetTable;
    }

    JPanel contentPane = (JPanel)getContentPane();
    FormLayout layout = new FormLayout("fill:pref:grow", "");
    contentPane.setLayout(layout);
    contentPane.setBorder(Borders.DLU4);
    CellConstraints cc = new CellConstraints();

    ProgramReceiveIf[] tempProgramReceiveIf = Plugin.getPluginManager().getReceiveIfs(caller,null);

    ArrayList<ProgramReceiveIf> disabledList = new ArrayList<ProgramReceiveIf>(disabledReceiveTargets != null ? disabledReceiveTargets.length : 0);

    if(disabledReceiveTargets != null) {
      for(ProgramReceiveTarget target : disabledReceiveTargets) {
        disabledList.add(target.getReceifeIfForIdOfTarget());
      }
    }

    if(caller != null) {
      ArrayList<ProgramReceiveIf> list = new ArrayList<ProgramReceiveIf>();

      for(ProgramReceiveIf tempIf : tempProgramReceiveIf) {
        if(tempIf.getId().compareTo(caller.getId()) != 0) {
          list.add(tempIf);
        }
      }

      mPluginItemList = new SelectableItemList<>(mResultPluginArr,
          list.toArray(new ProgramReceiveIf[list.size()]), disabledList.toArray(new ProgramReceiveIf[disabledList.size()]));
    } else {
      mPluginItemList = new SelectableItemList<>(mResultPluginArr,
          tempProgramReceiveIf, disabledList.toArray(new ProgramReceiveIf[disabledList.size()]));
    }

    mPluginItemList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    int pos = 1;
    layout.appendRow(RowSpec.decode("fill:default:grow"));
    layout.appendRow(RowSpec.decode("3dlu"));

    if (targetTable != null) {
      JSplitPane splitPane = new JSplitPane();

      splitPane.setLeftComponent(mPluginItemList);

      final JPanel targetPanel = new JPanel();
      targetPanel.setLayout(new BorderLayout());

      // JScrollPane targetScrollPane = new JScrollPane(mTargetPanel);
      // targetScrollPane.getVerticalScrollBar().setUnitIncrement(10);

      mPluginItemList.addListSelectionListener(e -> {
        try {
          if (!e.getValueIsAdjusting()) {
            targetPanel.removeAll();
            SelectableItem<ProgramReceiveIf> pluginItem = mPluginItemList.getSelectedValue();

            final ProgramReceiveIf plugin = (ProgramReceiveIf) pluginItem
                .getItem();
            ProgramReceiveTarget[] pluginTargets = plugin.getProgramReceiveTargets();
            
            mCurrentTargets = Arrays.copyOf(pluginTargets,pluginTargets.length);
            
            if (mCurrentTargets != null) {
              Arrays.sort(mCurrentTargets);
              ArrayList<ProgramReceiveTarget> targets = mReceiveTargetTable
                  .get(plugin);
              if (targets == null || !pluginItem.isSelected()) {
                targets = new ArrayList<ProgramReceiveTarget>();
              }
              if (pluginItem.isSelected() && targets.isEmpty()) {
                targets.add(mCurrentTargets[0]);
              }
              mReceiveTargetTable.put(plugin, targets);
              final SelectableItemList<ProgramReceiveTarget> targetList = new SelectableItemList<>(
                  targets.toArray(new ProgramReceiveTarget[targets.size()]), mCurrentTargets, disabledReceiveTargets);
              targetPanel.add(targetList, BorderLayout.CENTER);
              targetList
                  .addListSelectionListener(listEvent -> {
                    if (!listEvent.getValueIsAdjusting()) {
                      SelectableItem<ProgramReceiveIf> currPluginItem = mPluginItemList.getSelectedValue();
                      ProgramReceiveIf currPlugin = (ProgramReceiveIf) currPluginItem
                          .getItem();
                      List<ProgramReceiveTarget> sel = targetList.getSelectionList();
                      ArrayList<ProgramReceiveTarget> selTargets = new ArrayList<ProgramReceiveTarget>(
                          sel.size());
                      for (ProgramReceiveTarget obj : sel) {
                        selTargets.add(obj);
                      }
                      if (currPluginItem.isSelected() != (sel.size() > 0)) {
                        currPluginItem.setSelected(sel.size() > 0);
                        mPluginItemList.updateUI();
                      }
                      mReceiveTargetTable.put(currPlugin, selTargets);
                    }
                  });
            }
            targetPanel.updateUI();

          }
        } catch (Exception e1) {
          e1.printStackTrace();
        }
      });

      splitPane.setRightComponent(targetPanel);
      contentPane.add(splitPane, cc.xy(1, pos));

    } else {
      contentPane.add(mPluginItemList, cc.xy(1, pos));
    }
    mPluginItemList.setSelectedIndex(0);

    pos += 2;

    if (description != null) {
      JLabel lb = new JLabel(description);
      layout.appendRow(RowSpec.decode("pref"));
      layout.appendRow(RowSpec.decode("3dlu"));
      contentPane.add(lb, cc.xy(1,pos));
      pos += 2;
    }

    JButton okBt = new JButton(Localizer.getLocalization(Localizer.I18N_OK));
    JButton cancelBt = new JButton(Localizer.getLocalization(Localizer.I18N_CANCEL));

    okBt.addActionListener(event -> {
      mOkWasPressed = true;
      List<ProgramReceiveIf> list = mPluginItemList.getSelectionList();
      mResultPluginArr = new ProgramReceiveIf[list.size()];
      for (int i=0;i<list.size();i++) {
        mResultPluginArr[i]=list.get(i);
      }
      close();
    });

    cancelBt.addActionListener(event -> {
      mOkWasPressed = false;
      mResultPluginArr = null;
      close();
    });

    ButtonBarBuilder builder = new ButtonBarBuilder();
    builder.addGlue();
    builder.addButton(new JButton[] {okBt, cancelBt});

    layout.appendRow(RowSpec.decode("pref"));
    contentPane.add(builder.getPanel(), cc.xy(1,pos));

    Settings.layoutWindow("pluginChooserDlg", this, new Dimension(350,300), parent);

    setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        close();
      }
    });
  }

  /**
   *
   * @return an array of the selected plugins. If the user canceled the dialog,
   *         the array from the constructor call is returned.
   */
  public ProgramReceiveIf[] getPlugins() {
    if (mResultPluginArr==null) {
      return new ProgramReceiveIf[0];
    }
    return mResultPluginArr.clone();
  }

  public ProgramReceiveTarget[] getReceiveTargets() {
    if(mOkWasPressed) {
      ArrayList<ProgramReceiveTarget> targetList = new ArrayList<ProgramReceiveTarget>();
      Enumeration<ProgramReceiveIf> keyEnum = mReceiveTargetTable.keys();
      while (keyEnum.hasMoreElements()) {
        ProgramReceiveIf plugin = keyEnum.nextElement();
        for (ProgramReceiveIf selectedPlugin : mResultPluginArr) {
          if (selectedPlugin == plugin) {
            targetList.addAll(mReceiveTargetTable.get(plugin));
          }
        }
      }

      return targetList.toArray(new ProgramReceiveTarget[targetList.size()]);
    } else {
      return null;
    }
  }

  public void close() {
    setVisible(false);
    dispose();
  }

}