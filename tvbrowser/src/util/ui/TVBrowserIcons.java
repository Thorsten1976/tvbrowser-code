package util.ui;

import javax.swing.ImageIcon;

import tvbrowser.core.icontheme.IconLoader;

/**
 * common icons in TV-Browser
 *
 * @author Bananeweizen
 *
 */
public class TVBrowserIcons {
  /**
   * size of large icons
   */
  public static final int SIZE_LARGE = 22;
  /**
   * size of small icons
   */
  public static final int SIZE_SMALL = 16;
  
  private static final ImageIcon DEFAULT_CHANNEL_LOGO = new ImageIcon("./imgs/tvbrowser16.png"); 

  /**
   * icon for channels without logo
   * @return default channel logo
   */
  public static final ImageIcon defaultChannelLogo() {
    return DEFAULT_CHANNEL_LOGO;
  }
  
  /**
   * icon for plugins
   * @param size The size of the icon
   * @return icon for plugins
   */
  public static final ImageIcon plugin(int size) {
    return icon("actions", "view-plugins", size);
  }

  /**
   * icon for web browsing related actions
   * @param size The size of the icon
   * @return icon for web browsing related actions
   */
  public static final ImageIcon webBrowser(int size) {
    return icon("apps", "internet-web-browser", size);
  }

  private static ImageIcon icon(String category, String name, int size) {
    return IconLoader.getInstance().getIconFromTheme(category, name, size);
  }

  /**
   * icon for preferences
   * @param size The size of the icon
   * @return icon for preferences
   */
  public static ImageIcon preferences(int size) {
    return icon("categories", "preferences-system", size);
  }

  /**
   * refresh icon
   * @param size The size of the icon
   * @return refresh icon
   */
  public static ImageIcon refresh(int size) {
    return icon("actions", "view-refresh", size);
  }

  /**
   * refresh icon
   * @param size The size of the icon
   * @return refresh icon
   */
  public static ImageIcon reset(int size) {
    return icon("actions", "edit-undo", size);
  }

  /**
   * go up icon
   * @param size The size of the icon
   * @return go up icon
   */
  public static ImageIcon up(int size) {
    return icon("actions", "go-up", size);
  }

  /**
   * go down icon
   * @param size The size of the icon
   * @return go down icon
   */
  public static ImageIcon down(int size) {
    return icon("actions", "go-down", size);
  }

  /**
   * go to top icon
   * @param size The size of the icon
   * @return go to top icon
   */
  public static ImageIcon top(int size) {
    return icon("actions", "go-top", size);
  }

  /**
   * go to bottom icon
   * @param size The size of the icon
   * @return go to bottom icon
   */
  public static ImageIcon bottom(int size) {
    return icon("actions", "go-bottom", size);
  }

  /**
   * delete icon
   * @param size The size of the icon
   * @return delete icon
   */
  public static ImageIcon delete(int size) {
    return icon("actions", "edit-delete", size);
  }

  /**
   * edit icon
   * @param size The size of the icon
   * @return edit icon
   */
  public static ImageIcon edit(int size) {
    return icon("actions","document-edit", size);
  }

  /**
   * new icon
   * @param size The size of the icon
   * @return new icon
   */
  public static ImageIcon newIcon(int size) {
    return icon("actions","document-new", size);
  }

  /**
   * filter icon
   * @param size The size of the icon
   * @return filter icon
   */
  public static ImageIcon filter(int size) {
    return icon("actions", "view-filter", size);
  }

  /**
   * left icon
   * @param size The size of the icon
   * @return left icon
   */
  public static ImageIcon left(int size) {
    return icon("action", "go-previous", size);
  }

  /**
   * right icon
   * @param size The size of the icon
   * @return right icon
   */
  public static ImageIcon right(int size) {
    return icon("action", "go-next", size);
  }

  /**
   * search icon
   * @param size The size of the icon
   * @return search icon
   */
  public static ImageIcon search(int size) {
    return icon("actions", "system-search", size);
  }

  /**
   * copy icon
   * @param size The size of the icon
   * @return copy icon
   */
  public static ImageIcon copy(int size) {
    return icon("actions", "edit-copy", size);
  }

  /**
   * zoom in icon
   * @param size The size of the icon
   * @return zoom in icon
   */
  public static ImageIcon zoomIn(int size) {
    return icon("action", "zoom-in", size);
  }

  /**
   * zoom out icon
   * @param size The size of the icon
   * @return zoom out icon
   */
  public static ImageIcon zoomOut(int size) {
    return icon("action", "zoom-out", size);
  }

  /**
   * next week icon
   * @param size The size of the icon
   * @return next week icon
   */
  public static ImageIcon nextWeek(int size) {
    return icon("actions", "go-to-next-week", size);
  }

  /**
   * previous week icon
   * @param size The size of the icon
   * @return previous week icon
   */
  public static ImageIcon previousWeek(int size) {
    return icon("actions", "go-to-previous-week", size);
  }

  /**
   * quit icon
   * @param size The size of the icon
   * @return quit icon
   */
  public static ImageIcon quit(int size) {
    return icon("actions", "system-log-out", size);
  }

  /**
   * restart icon
   * @param size The size of the icon
   * @return restart icon
   */
  public static ImageIcon restart(int size) {
    return icon("actions", "system-restart", size);
  }

  /**
   * full screen icon
   * @param size The size of the icon
   * @return full screen icon
   */
  public static ImageIcon fullScreen(int size) {
    return icon("actions", "view-fullscreen", size);
  }

  /**
   * warning icon
   * @param size The size of the icon
   * @return warning icon
   */
  public static ImageIcon warning(int size) {
    return icon("status", "dialog-warning", size);
  }

  /**
   * update data icon
   * @param size The size of the icon
   * @return update data icon
   */
  public static ImageIcon update(final int size) {
    return icon("apps", "system-software-update", size);
  }

  /**
   * show plugin tree icon
   * @param size The size of the icon
   * @return show plugin tree icon
   */
  public static ImageIcon viewTree(final int size) {
    return icon("actions", "view-tree", size);
  }

  /**
   * scroll to now icon
   * @param size The size of the icon
   * @return scroll to now icon
   */
  public static ImageIcon scrollToNow(final int size) {
    return icon("actions", "scroll-to-now", size);
  }

  /**
   * go to date icon
   * @param size The size of the icon
   * @return go to date icon
   */
  public static ImageIcon goToDate(final int size) {
    return icon("actions", "go-to-date-list", size);
  }

  /**
   * scroll to channel icon
   * @param size The size of the icon
   * @return scroll to channel icon
   */
  public static ImageIcon scrollToChannel(final int size) {
    return icon("actions", "scroll-to-channel-list", size);
  }

  /**
   * scroll to time icon
   * @param size The size of the icon
   * @return scroll to time icon
   */
  public static ImageIcon scrollToTime(int size) {
    return icon("actions", "scroll-to-time-list", size);
  }
  
  /**
   * scroll to show menu
   * @param size The size of the icon
   * @return show menu icon
   */
  public static ImageIcon showMenu(int size) {
    return icon("actions", "show-menu", size);
  }
}
