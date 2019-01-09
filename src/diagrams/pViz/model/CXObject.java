package diagrams.pViz.model;

import java.util.List;


import diagrams.pViz.model.edges.Edge;
import diagrams.pViz.model.nodes.DataNode;
import util.StringUtil;
class CartesianLayout
{
	int node;	
	String x;
	String y;
	String z;
	int view;

	public String toString()
	{
		return CXObject.iline("node", node)+ CXObject.iline("view", view) + CXObject.line("x", x) + CXObject.line("y", y) ;
	}
	
}

class NodeAttribute
{
	int propOf;	
	String name;
	String val;
	String type;
	String subnet;

	public String toString()
	{
		return CXObject.line("n", name) + CXObject.line("po", propOf) + CXObject.line("v", val) + CXObject.line("d", type);
	}
}

class EdgeAttribute
{
	String propOf;	
	String name;
	String val;
	String type;
	String subnet;

	public String toString()
	{
		return CXObject.line("n", name) + CXObject.line("po", propOf) + CXObject.line("v", val) + CXObject.line("d", type) + CXObject.line("s", subnet);
	}
}

public class CXObject {
	
	private List<NodeAttribute> nodeAttributes;
	private List<EdgeAttribute> edgeAttributes;
	private List<DataNode> nodes;
	private List<Edge> edges;
	private List<CartesianLayout> cartesianLayout;
	
	public void addNode(DataNode node)
	{
		
	}	

	class Node
	{
		int id;	
		String name;
		String rep;
		Node(int i, String n, String r)
		{
			id = i;
			name = n;
			rep = r;
		}
	
		public String toString()
		{
			return line("@id", id) + line("n", name) + line("r", rep);
		}
	}
	

	class Edge
	{
		int id;	
		int src;
		int targ;
		String type;
		public String toString()
		{
			return line("@id", id) + line("s", src) + line("t", targ) + line("i", type);
		}
	}
	
	
	class NetworkAttribute
	{
		String name;
		String val;
		String type;
		String subnet;
	
		public String toString()
		{
			return line("n", name) + line("v", val) + line("d", type) + line("s", subnet);
		}
		
	}

	
	public static String line(String attr, int val)
	{
		return q(attr) + ": " + val + ",\n";
	}

	public static String iline(String attr, int val)
	{
		return q(attr) + ": " + val + ",\n";
	}

	public static String line(String attr, String val)
	{
		if (StringUtil.isEmpty(val)) return "";
		return q(attr) + ": " + q(val) + ",\n";
	}

	public static String line(String attr, double val)
	{
		return q(attr) + ": " + val + ",\n";
	}
	
	public static String q(String a)		{			return '"' + a + '"';		}
	public static String b(String a)		{			return '{' + a + '}';		}
}
