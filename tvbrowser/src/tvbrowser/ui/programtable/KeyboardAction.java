package tvbrowser.ui.programtable;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.SwingUtilities;

/**
 * A class for keyboard selection of programs in the ProgramTable.
 * 
 * @author René Mach
 * 
 */
public class KeyboardAction extends AbstractAction {

  public static final int KEY_UP = 0;
  public static final int KEY_DOWN = 1;
  public static final int KEY_RIGHT = 2;
  public static final int KEY_LEFT = 3;
  public static final int KEY_CONTEXTMENU = 4;
  public static final int KEY_DESELECT = 5;

  private ProgramTableScrollPane mScrollPane;

  private int mType;

  /**
   * 
   * @param pane The ProgramTableScrollPane to start the action on.
   * @param type The Type of Action ( KEY_UP, KEY_DOWN, ...)
   */
  public KeyboardAction(ProgramTableScrollPane pane, int type) {
    mScrollPane = pane;
    mType = type;
  }

  public void actionPerformed(ActionEvent e) {
    SwingUtilities.invokeLater(() -> {
      if (mType == KEY_UP) {
        mScrollPane.up();
      }
      if (mType == KEY_DOWN) {
        mScrollPane.down();
      }
      if (mType == KEY_LEFT) {
        mScrollPane.left();
      }
      if (mType == KEY_RIGHT) {
        mScrollPane.right();
      }
      if (mType == KEY_CONTEXTMENU) {
        mScrollPane.togglePopupMenu();
      }
      if (mType == KEY_DESELECT) {
        mScrollPane.deSelectItem(false);
      }
    });
  }
}