package diagrams.draw;

import edu.stanford.nlp.util.ArrayUtils;

public enum Tool {

	Arrow,
	Rectangle, RoundedRectangle, Circle, Polygon,	Polyline, Line,	Shape1, Shape2, // Shapes
	Browser, Text, Table, Image, Media,			// Controls
	;

	public static Tool fromString(String type)
	{
		if (type == null) return Arrow;
		String t = type.toLowerCase();
		for (Tool tool : values())
			if (tool.name().toLowerCase().equals(t))	return tool;
		return Arrow;
	}
	static String[] tags = { "Mitochondria", "Protein", "Pathway", "GeneProduct", "Metabolite", "Rna"};
	static Tool[] shapes =  { Rectangle, RoundedRectangle, Circle, Polygon, Polyline, Line, Shape1, Shape2 };
	static Tool[] controls = { Browser, Text, Table, Image, Media};

	public boolean isShape()		{	return ArrayUtils.contains(shapes, this);		}
	public static boolean isShape(String s)		{	return ArrayUtils.contains(shapes, s);		}
	public boolean isControl()		{	return ArrayUtils.contains(controls, this);		}
	public static boolean isSVG(String type) {	return ArrayUtils.contains(tags, type);	}

	// HACK -- convert from GPML type to shape
	public static Tool lookup(String type) {
		if (type == null) return Circle;
		if ("Pentagon".equals(type)) 	return Rectangle;
		if ("None".equals(type)) 	return Rectangle;
		if ("Oval".equals(type)) 		return Circle;
		
		if ("Mitochondria".equals(type)) return Circle;
		if ("Protein".equals(type)) 	return Rectangle;
		if ("Pathway".equals(type)) 	return Rectangle;
		if ("GeneProduct".equals(type)) return Rectangle;
		if ("Metabolite".equals(type)) 	return Rectangle;
		if ("Rna".equals(type)) 		return Rectangle;
		return  fromString(type);
	}
}