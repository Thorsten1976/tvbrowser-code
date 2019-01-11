package tvbrowser.extras.favoritesplugin.dlgs;

import java.util.Comparator;

import javax.swing.tree.TreeNode;

public class FavoriteNodeComparator implements Comparator<TreeNode> {

  private static FavoriteNodeComparator instance;

  private FavoriteNodeComparator() {
    super();
  }

  public int compare(TreeNode node1, TreeNode node2) {
	  int result = 0;
	  
	  if(node1 instanceof FavoriteNode && node2 instanceof FavoriteNode) {
		  result = ((FavoriteNode)node1).compareTo((FavoriteNode)node2);
	  }
	  
	  
    return result;
  }
  
  public static FavoriteNodeComparator getInstance() {
    if (instance == null) {
      instance = new FavoriteNodeComparator();
    }
    return instance;
  }
}