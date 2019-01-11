package tvbrowser.extras.favoritesplugin.dlgs;

import java.util.Comparator;

import javax.swing.tree.TreeNode;

public class FavoriteNodeCountComparator implements Comparator<TreeNode> {

  private static FavoriteNodeCountComparator instance;

  public static FavoriteNodeCountComparator getInstance() {
    if (instance == null) {
      instance = new FavoriteNodeCountComparator();
    }
    return instance;
  }

  public int compare(TreeNode node1a, TreeNode node2a) {
	int result = 0;
    
	if(node1a instanceof FavoriteNode && node2a instanceof FavoriteNode) {
		FavoriteNode node1 = (FavoriteNode)node1a;
		FavoriteNode node2 = (FavoriteNode)node2a;
		
	    result = node2.getAllPrograms(false).length-node1.getAllPrograms(false).length;
	    
	    if (result == 0 && !node1.isDirectoryNode() && !node2.isDirectoryNode()) {
	      result = node1.getFavorite().getName().compareTo(node2.getFavorite().getName());
	    }
	}
    
    return result;
  }

  private FavoriteNodeCountComparator() {
    super();
  }

}
