package diagrams.pViz.gpml;

import javafx.geometry.Point2D;
import util.StringUtil;

public class GPMLPoint {

	private double x = 0;
	private double y = 0;
	private double relX = 0;
	private double relY = 0;
	private ArrowType head = ArrowType.none;
	private String graphRef;
	
	public double getX()				{ return x;	}
	public void setX(double d)			{ x = d;	}

	public double getY()				{ return y;	}
	public void setY(double s)			{ y = s;	}

	public Point2D getPoint()			{ return new Point2D(x,y);	}

	public String getGraphRef()			{ return graphRef;	}
	public void setGraphRef(String s)	{ graphRef = s;	}

	public ArrowType getArrowType()		{ return head;	}
	public void setArrowType(ArrowType s)	{ head = s;	}

	public double getRelX()				{ return relX;	}
	public void setRelX(double s)		{ relX = s;	}
	public double getRelY()				{ return relY;	}
	public void setRelY(double s)		{ relY = s;	}
	//-----------------------------------------------------------------------
	
	public GPMLPoint(Point2D pt) {
		x = pt.getX();
		y = pt.getY();
	}
	
	public GPMLPoint(double X, double Y) {
		x = X;
		y = Y;
	}

	public GPMLPoint(double X, double Y, String ref, ArrowType h) {
		x = X;
		y = Y;
		head = h;
		graphRef = ref;
	}
	
	public GPMLPoint(org.w3c.dom.Node node) {
		for (int i=0; i<node.getAttributes().getLength(); i++)
		{
			org.w3c.dom.Node child = node.getAttributes().item(i);
			String name = child.getNodeName();
			String val = child.getNodeValue();
			if ("X".equals(name))  x = StringUtil.toDouble(val);
			else if ("Y".equals(name))  y = StringUtil.toDouble(val);
			else if ("RelX".equals(name))  relX = StringUtil.toDouble(val);
			else if ("RelY".equals(name))  relY = StringUtil.toDouble(val);
			else if ("GraphRef".equals(name))  graphRef = val;
			else if ("ArrowHead".equals(name))  head = ArrowType.lookup(val);
		}
//		System.out.println("(" + x + ", " + y + ")"); 
	}
	

	public String toString()
	{
		String firstPart = String.format("<Point X=\"%.2f\" Y=\"%.2f\" ", x, y);
		String secondPart = String.format("GraphRef=\"%s\" RelX=\"%.2f\" RelY=\"%.2f\" ArrowHead=\"%s\" />\n", getGraphRef(), getRelX(), getRelY(), getArrowType());
		return firstPart + secondPart;
	}

	//-----------------------------------------------------------------------
	public enum ArrowType
	{
		none,
		mimactivation,
		mimbinding, 
		miminhibition,
		mimcatalysis,
		mimconversion,
		mimtranscriptiontranslation,
		mimtranslation,
		circle,
		arrow,
		tbar,
		x,
		big,
		square,
		small;

		public static ArrowType fromString(String type)
		{
			if (type == null) return none;
			String t = type.toLowerCase();
			for (ArrowType a : values())
				if (a.name().toLowerCase().equals(t))	return a;
			return none;
		}
		public static ArrowType lookup(String nodeValue) {

			String noDash = nodeValue.replace("-", "");
			ArrowType a = fromString(noDash);
			if (a != ArrowType.none)  return a;
			// keep mapping here
			return none;
		}
		
		public  double[] getArrowShape() {

			if (ArrowType.none == this) 					return  new double[]{ };
	    	if (ArrowType.miminhibition == this )    		return  new double[]{-10,0,10,0 };
	    	if (ArrowType.mimactivation == this )    		return  new double[]{0,0,5,12,-5,12};
	    	if (ArrowType.mimbinding == this )    			return  new double[]{0,0,5,12,0, 6, -5,12};
	    	if (ArrowType.square == this )    				return  new double[]{-4,-4, -4, 4, 4,4, 4,-4 };
	    	if (ArrowType.mimconversion == this )    		return  new double[]{0,0,4,7, -4,7};
	    	if (ArrowType.mimtranscriptiontranslation == this )  return  new double[]{ 0,0,6,18,-6,18 };
	    	return  new double[]{0,0,5,12,-5,12};

		}
		public static double[] getArrowShape(String s) {

			return fromString(s).getArrowShape();
		}
		public static boolean isShape(String shape) {
			if ( "Circle".equals(shape) ) return true;
			return lookup(shape) == mimcatalysis;
		}
	}

}
