package diagrams.pViz.util;


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
	interacts,
	stimulates,
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
			if (a.name().toLowerCase().equals(t))	
				return a;
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
    	if (ArrowType.tbar == this )    				return  new double[]{-30,0,30,0 };
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
	public boolean connectsToEdges()	
	{ 
		return (ArrowType.mimactivation == this ) || (ArrowType.miminhibition == this ); 
	}
}



