package indi.jackc.relev.fptree;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FPTree {

	private final static String ITEM_SPLIT = ",";// 项之间的分隔符
	private final static String CON = " <-- ";// 强关联项之间的分隔符
	private int minSupport;

	public int getMinSupport() {
		return minSupport;
	}

	public void setMinSupport(int minSupport) {
		this.minSupport = minSupport;
	}

	// 从文件中读取transRecords
	// String... 的作用：0或多个参数
	private List<List<String>> readTransRecords(String... filenames) {
		// TODO Auto-generated method stub
		// 记录所有的records
		List<List<String>> transaction = null;

		if (filenames.length > 0) {
			transaction = new LinkedList<List<String>>();

			// 遍历filenames，获取所有records
			for (String filename : filenames) {
				try {
					FileReader fr = new FileReader(filename);
					BufferedReader br = new BufferedReader(fr);
					try {
						String line;

						/**
						 * 错误原因：先进行了初始化，在下面的while循环中record不断增加，且重复
						 */
						// List<String> record=new ArrayList<String>();

						List<String> record;

						while ((line = br.readLine()) != null) {
							record = new ArrayList<String>();
							String strs[] = line.split(ITEM_SPLIT);

							// Collections提供操作和返回collection的方法
							Collections.addAll(record, strs);

							transaction.add(record);
						}
					} finally {
						br.close();
					}
				} catch (IOException e) {
					System.out.println("Read transaction records failed !"
							+ e.getMessage());

					// 非正常退出程序，参数为非0
					System.exit(1);
				}
			}
		}
		return transaction;
	}

	// 构建项头表,同时也是频繁1项集
	private ArrayList<TreeNode> buildHeaderTable(List<List<String>> transRecords) {
		Map<String, TreeNode> headMap = new HashMap<String, TreeNode>();

		// 计算transRecords中各项的支持度
		for (List<String> record : transRecords) {
			for (String item : record) {
				if (headMap.keySet().contains(item)) {
					headMap.get(item).countIncrement(1);
				} else {
					TreeNode node = new TreeNode();
					node.setCount(1);
					node.setName(item);
					headMap.put(item, node);
				}
			}
		}

		// 将支持度大于阀值的项加入headList
		ArrayList<TreeNode> headList = new ArrayList<TreeNode>();
		Set<String> keySet = headMap.keySet();
		TreeNode node = null;
		for (String key : keySet) {
			if (headMap.get(key).getCount() >= this.getMinSupport()) {
				node = headMap.get(key);
				headList.add(node);
			}
		}
		Collections.sort(headList);
		return headList;
	}

	private LinkedList<String> sortByHeader(List<String> transRecord,
			ArrayList<TreeNode> headerTable) {
		LinkedList<String> list = new LinkedList<String>();

		// 由于headerTable中已进行降序排序，直接顺序遍历，将存在的项加入list，即可实现降序排列
		for (TreeNode node : headerTable) {
			if (transRecord.contains(node.getName())) {
				list.add(node.getName());
			}
		}
		return list;
	}

	// 生成FP-Growth树
	private TreeNode buildFPTree(List<List<String>> transRecords,
			ArrayList<TreeNode> headerTable) {
		TreeNode root = new TreeNode();
		for (List<String> transRecord : transRecords) {
			// 按照频繁1项集中的顺序将事务排序
			LinkedList<String> record = sortByHeader(transRecord, headerTable);

			TreeNode tempRoot = null;
			TreeNode subTreeNode = root;

			// 有子节点时，遍历record，相同，计数加1；出现不相同，则要添加新子节点
			if (root.getChildren() != null) {
				while (record.size() > 0
						&& (tempRoot = subTreeNode.findChild(record.peek())) != null) {
					tempRoot.countIncrement(1);
					subTreeNode = tempRoot;
					record.poll();
				}
			}
			// 无子节点，直接新加子节点；有子节点且出现不同时，添加新子节点
			addNodes(subTreeNode, record, headerTable);
		}
		return root;
	}

	// 添加新子节点
	private void addNodes(TreeNode ancestorNode, LinkedList<String> record,
			ArrayList<TreeNode> headerTable) {
		if (record.size() > 0) {
			// 将record添加为ancestorNode的子节点
			TreeNode child = new TreeNode();
			String name = record.poll();
			child.setName(name);
			child.setParent(ancestorNode);
			child.setCount(1);
			ancestorNode.addChild(child);
			ancestorNode = child;

			// 在headerTable中添加同名节点
			for (TreeNode node : headerTable) {
				if (node.getName().equals(name)) {
					while (node.getNextHomonym() != null) {
						node = node.getNextHomonym();
					}
					node.setNextHomonym(child);
					break;
				}
			}

			// 递归执行
			addNodes(ancestorNode, record, headerTable);
		}
	}

	// FP-Growth算法
	private void fpGrowth(List<List<String>> transRecords,
			List<String> postPattern) {
		if (transRecords != null) {
			// 构建项头表,同时也是频繁1项集
			ArrayList<TreeNode> headerTable = buildHeaderTable(transRecords);

			// 构建FP-Tree
			TreeNode treeRoot = buildFPTree(transRecords, headerTable);

			// 若FP-Tree为空，直接返回
			if (treeRoot.getChildren() == null
					|| treeRoot.getChildren().size() == 0) {
				return;
			}

			// 输出项头表的每一项和postPattern
			if (postPattern != null) {
				for (TreeNode node : headerTable) {
					System.out.print(node.getCount() + "\t" + node.getName()
							+ CON);
					for (String item : postPattern) {
						if (item == postPattern.get(postPattern.size() - 1)) {
							System.out.print(item);
						} else {
							System.out.print(item + ITEM_SPLIT);
						}
					}
					System.out.println();
				}
			}

			// 递归，查找每个表头元素的条件模式基
			for (TreeNode header : headerTable) {

				// 后缀模式增加一项
				List<String> newPostPattern = new LinkedList<String>();
				newPostPattern.add(header.getName());
				if (postPattern != null) {
					newPostPattern.addAll(postPattern);
				}

				// 查询header的条件模式基，将条件模式基中各元素加入newTransRecords，生成频繁模式
				List<List<String>> newTransRecords = new LinkedList<List<String>>();
				TreeNode backNode = header.getNextHomonym();
				while (backNode != null) {
					List<String> preNodes = new ArrayList<String>();
					int count = backNode.getCount();
					TreeNode parent = backNode.getParent();
					while (parent.getName() != null) {
						preNodes.add(parent.getName());
						parent = parent.getParent();
					}
					while (count-- > 0) {
						newTransRecords.add(preNodes);
					}
					backNode = backNode.getNextHomonym();
				}

				// 递归迭代
				fpGrowth(newTransRecords, newPostPattern);
			}
		}
	}

	public static void main(String[] args) {
		FPTree fpTree = new FPTree();

		// 设定最小支持度为3
		fpTree.setMinSupport(3);

		List<List<String>> transRecords = fpTree
				.readTransRecords("src/indi/jackc/relev/fptree/market.txt");
		fpTree.fpGrowth(transRecords, null);
	}

}