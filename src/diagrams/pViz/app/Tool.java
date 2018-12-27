package diagrams.pViz.app;

import java.io.File;

import util.FileUtil;

public enum Tool {

	Arrow,
	Rectangle, RoundedRectangle, Circle, Oval, Arc, Polygon, Triangle, Pentagon, Hexagon, Octagon, Polyline, Line, GraphicalLine, Brace, Shape1,   // Shapes
	Golgi, Mitochondria, SR, ER, Cell, Nucleus, Organelle, GroupComponent, ComplexComponent,
	Protein, Pathway, GeneProduct, Metabolite, Rna,
	Browser, Text, Table, Image, SVGPath, Media,			// Controls
	;

	public static Tool fromString(String type)
	{
		if (type == null) return Arrow; 
		String t = type.toLowerCase();
		for (Tool tool : values())
			if (tool.name().toLowerCase().equals(t))	return tool;
		return Arrow;
	}
	
	static boolean contains(Tool[] vals, Tool s)
	{
		for (Tool t : vals)
			if (s.toString().equals(t.toString()))
				return true;
		return false;
	}
	static boolean contains(Tool[] vals, String s)
	{
		for (Tool t : vals)
			if (s.equals(t.toString()))
				return true;
		return false;
	}
	public static boolean contains(String[] vals, String s)
	{
		if ("{".equals(s)) s = Brace.name();
		for (String t : vals)
			if (s.equals(t))
				return true;
		return false;
	}
	public boolean resizableNodes()
	{
		if (this == GeneProduct) return false;
		if (this == Metabolite) return false;
		if (this == Protein) return false;
		return true;
	}
	public static String[] customShapes = { "Mitochondria", "Protein", "Pathway", "GeneProduct", "Metabolite", "Rna", "ER", "SR", "Golgi", "Brace"};
	static String[] tags = { "Mitochondria", "Protein", "Pathway", "GeneProduct", "Metabolite", "Rna", "ER", "SR", "Golgi"};
	static Tool[] connectors =  { Polyline, Line };
	static Tool[] shapes =  { Rectangle, RoundedRectangle, Circle, Oval, Polygon,Triangle,Pentagon,Hexagon, Polyline, Line, GraphicalLine, Brace, Shape1 };
	static Tool[] components =  { Protein, Pathway, GeneProduct, Metabolite, Rna };
	static Tool[] cellShapes =  { Golgi, Mitochondria, SR, ER, Cell, Nucleus, Organelle, GroupComponent };
	static Tool[] controls = { Browser, Text, Table, Image, SVGPath, Media};

	public boolean isArrow()				{	return this == Arrow;		}  
	public boolean isShape()				{	return contains(shapes, this);		}
	public boolean isCellShape()			{	return contains(cellShapes, this);		}
	public boolean isPencil()				{	return contains(connectors, this );		}
	public static boolean isShape(String s)	{	return contains(shapes, s);		}
	public boolean isControl()				{	return contains(controls, this);		}
	public static boolean isSVG(String type) {	return contains(tags, type);	}

	// HACK -- convert from GPML type to shape
	public static Tool lookup(String type) {
		if (type == null) return Circle;
		return  fromString(type);
// redundant:	
//		if ("Hexagon".equals(type)) 	return Hexagon;
//		if ("Pentagon".equals(type)) 	return Pentagon;
//		if ("Octagon".equals(type)) 	return Octagon;
//		if ("None".equals(type)) 		return Rectangle;
//		if ("Oval".equals(type)) 		return Oval;
//		
//		if ("Nucleus".equals(type)) 	return Nucleus;
//		if ("Cell".equals(type)) 		return Cell;
//		if ("Brace".equals(type)) 		return Brace;
//		if ("GraphicalLine".equals(type)) return GraphicalLine;
//		if ("GroupComponent".equals(type)) return GroupComponent;
//		if ("ComplexComponent".equals(type)) return ComplexComponent;
//		if ("Mitochondria".equals(type)) return Mitochondria;
//		if ("Protein".equals(type)) 	return Protein;
//		if ("Pathway".equals(type)) 	return Pathway;
//		if ("GeneProduct".equals(type)) return GeneProduct;
//		if ("Metabolite".equals(type)) 	return Metabolite;
//		if ("Rna".equals(type)) 		return Rna;
//		if ("ER".equals(type)) 			return ER;
//		if ("SR".equals(type)) 			return SR;
//		if ("Golgi".equals(type)) 		return Golgi;
	}
	public static Tool appropriateTool(File f) {
		if (FileUtil.isImageFile(f))	return Image;
		if (FileUtil.isSVG(f))			return SVGPath;
		if (FileUtil.isCSV(f))			return Table;
		if (FileUtil.isWebloc(f))		return Browser;
		if (FileUtil.isTextFile(f))		return Text;
		return null;
	}
}