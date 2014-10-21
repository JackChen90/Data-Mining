package indi.jackc.relev.fptree;

import java.util.ArrayList;
import java.util.List;

public class TreeNode implements Comparable<TreeNode> {

	private String name;// �ڵ�����
	private int count;// ����
	private TreeNode parent;// ���ڵ�
	private List<TreeNode> children;// �ӽڵ�
	private TreeNode nextHomonym;// ��һ��ͬ���ڵ�

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public TreeNode getParent() {
		return parent;
	}

	public void setParent(TreeNode parent) {
		this.parent = parent;
	}

	public List<TreeNode> getChildren() {
		return children;
	}

	public void addChild(TreeNode child) {
		if (this.getChildren() == null) {
			ArrayList<TreeNode> al = new ArrayList<TreeNode>();
			this.setChildren(al);
			this.getChildren().add(child);
		} else {
			this.getChildren().add(child);
		}
	}

	public TreeNode findChild(String name) {
		List<TreeNode> children = this.getChildren();
		if (children != null) {
			for (TreeNode child : children) {
				if (child.getName().equals(name)) {
					return child;
				}
			}
		}
		return null;
	}

	public void setChildren(List<TreeNode> children) {
		this.children = children;
	}

	public void printChildrenName() {
		List<TreeNode> children = this.getChildren();
		if (children != null) {
			for (TreeNode child : children) {
				System.out.print(child.getName() + " ");
			}
		} else {
			System.out.print("No Children !");
		}
	}

	public TreeNode getNextHomonym() {
		return nextHomonym;
	}

	public void setNextHomonym(TreeNode nextHomonym) {
		this.nextHomonym = nextHomonym;
	}

	public void countIncrement(int n) {
		this.setCount(this.getCount() + n);
	}

	@Override
	public int compareTo(TreeNode node) {
		// TODO Auto-generated method stub
		int count = node.getCount();

		// ��������
		return count - this.getCount();
	}

}