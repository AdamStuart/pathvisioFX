package diagrams.pViz.model.edges;

import javafx.geometry.Point2D;
import javafx.scene.shape.Line;


public class Segment {
	//-----------------------------------------------------------

	public static final int AXIS_X = 0;
	public static final int AXIS_Y = 1;
		Point2D start, end;
	public Segment(Point2D a, Point2D b)
	{
		start = a;
		end = b;
	}
	public Point2D getEnd() { return end;	}
	public Point2D getStart() { return start;	}
	public Point2D getCenter()	{ return new Point2D((end.getX() + start.getX()) / 2., (end.getY() + start.getY()) / 2.); }
	double length() { return distance(end, start);	}
	public String toString() { return "(" + (int) getStart().getX() + ", " +  (int) getStart().getY() +  " -> " + (int) getEnd().getX() + ", " +  (int) getEnd().getY() + ")"; }
	public Line getLine()	{ return new Line(start.getX(), start.getY(), end.getX(), end.getY());  }
	
	static public Segment createStraightSegment(Point2D start, Point2D end, int axis) {
		double ex = end.getX();
		double ey = end.getY();
		if(axis == AXIS_X) 		ey = start.getY();
		else 					ex = start.getX();
		return new Segment(start, new Point2D(ex, ey));
	}

	static public int getOppositeAxis(int axis) {
		return axis == ConnectorShape.AXIS_X ? AXIS_Y : AXIS_X;
	}
	static public int getSide(double relX, double relY)
	{
			if (relX < 0 && Math.abs(relX) > Math.abs(relY))  		return ConnectorRestrictions.SIDE_WEST;
			if (relX > 0 && Math.abs(relX) > Math.abs(relY))  		return ConnectorRestrictions.SIDE_EAST;
			if (relY < 0)  		return ConnectorRestrictions.SIDE_NORTH;
			if (relY > 0)  		return ConnectorRestrictions.SIDE_SOUTH;
			return -1;
	}

	static public int getSegmentDirection(int side) {
		switch(side) {
		case ConnectorRestrictions.SIDE_EAST:
		case ConnectorRestrictions.SIDE_SOUTH: 		return 1;
		case ConnectorRestrictions.SIDE_NORTH:
		case ConnectorRestrictions.SIDE_WEST: 		return -1;
		}
		return 0;
	}

	static public int getSegmentAxis(int side) {
		switch(side) {
			case ConnectorRestrictions.SIDE_EAST:
			case ConnectorRestrictions.SIDE_WEST: 		return AXIS_X;
			case ConnectorRestrictions.SIDE_NORTH:
			case ConnectorRestrictions.SIDE_SOUTH: 		return AXIS_Y;
		}
		return 0;
	}

	/**
	 * Get the direction of the line on the x axis
	 * @param start The start point of the line
	 * @param end The end point of the line
	 * @return 1 if the direction is positive (from left to right),
	 * -1 if the direction is negative (from right to left)
	 */
	public static int getDirectionX(Point2D start, Point2D end) {
		return (int)Math.signum(end.getX() - start.getX());
	}

	/**
	 * Get the direction of the line on the y axis
	 * @param start The start point of the line
	 * @param end The end point of the line
	 * @return 1 if the direction is positive (from top to bottom),
	 * -1 if the direction is negative (from bottom to top)
	 */
	public int getDirectionY(Point2D start, Point2D end) {
		return (int)Math.signum(end.getY() - start.getY());
	}

//-----------------------------------------------
	static public Point2D centerPoint(Point2D start, Point2D end) 
	{
		return new Point2D( (start.getX() + end.getX() ) / 2, (start.getY() + end.getY()) / 2 );
	}
	
	static public double distance(Point2D a, Point2D b)	
	{
		double dx = a.getX() - b.getX();
		double dy = a.getY() - b.getY();
		return (Math.sqrt(dx*dx + dy*dy));
	}
}
