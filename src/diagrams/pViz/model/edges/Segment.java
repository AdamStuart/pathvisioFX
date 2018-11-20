package diagrams.pViz.model.edges;

import java.util.List;

import javafx.geometry.Point2D;

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
	public String toString() { return "(" + (int) getStart().getX() + ", " +  (int) getStart().getY() +  " -> " + (int) getEnd().getX() + ", " +  (int) getStart().getY() + ")"; }
	
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
// R=RIGHT, L=LEFT, T=TOP, B=BOTTOM
// N=NORTH, E=EAST, S=SOUTH, W=WEST
	/* The number of connector for each side and relative position
		RN	RE	RS	RW
BLN		1	2	1	0
TLN		1	2	3	2

BLE		3	1	0	1
TLE		0	1	2	1

BLS		3	2	1	2
TLS		1	2	1	0

BLW		2	3	2	1
TLW		2	3	2	1
	There should be some logic behind this, but hey, it's Friday...
	(so we just hard code the array)
	
BUG:  	There should be some cases where 4 is returned !!  
	 */
	private int[][][] waypointNumbers;

	private int getNrWaypoints(int x, int y, int z) {
		waypointNumbers = new int[][][] {
			{	{ 1, 1 },	{ 2, 2 },	{ 1, 3 },	{ 0, 2 }	},
			{	{ 2, 0 }, 	{ 1, 1 }, 	{ 0, 2 }, 	{ 1, 1 },	},
			{	{ 3, 1 },	{ 2, 2 },	{ 1, 1 },	{ 2, 0 },	},
			{ 	{ 2, 2 },	{ 3, 3 },	{ 2, 2 },	{ 1, 1 },	}
		};
		return waypointNumbers[x][y][z];
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

	protected int getNrSegments(Point2D start, Point2D end, int startSide, int endSide) {

		boolean leftToRight = getDirectionX(start, end) > 0;

		Point2D left = leftToRight ? start : end;
		Point2D right = leftToRight ? end : start;
		boolean leftBottom = getDirectionY(left, right) < 0;

		int z = leftBottom ? 0 : 1;
		int x = leftToRight ? startSide : endSide;
		int y = leftToRight ? endSide : startSide;
		return getNrWaypoints(x, y, z) + 2;
	}

    protected Point2D fromLineCoordinate(double l, List<Segment> segments) 
    {
		double totalLength = getTotalLength(segments);
		double pixelsRemaining = totalLength * l;
		if (pixelsRemaining < 0) pixelsRemaining = 0;
		if (pixelsRemaining > totalLength) pixelsRemaining = totalLength;

		// count off each segment from pixelsRemaining, until there aren't enough pixels left
		Segment segment = null;
		double slength = 0.0;
		for(Segment s : segments) 
		{
			slength = s.length();
			segment = s;
			if (pixelsRemaining < slength) 		break; // not enough pixels left, we found our segment.
			pixelsRemaining -= slength;
		}

		//Find the location on the segment
		Point2D s = segment.getStart();
		Point2D e = segment.getEnd();

		// protection against division by 0
		if (slength == 0)
			return new Point2D(s.getX(), s.getY());
			// start from s, in the direction of e, for pixelRemaining pixels.
		double deltax = e.getX() - s.getX();
		double deltay = e.getY() - s.getY();

		return new Point2D(s.getX() + deltax / slength * pixelsRemaining,
				s.getY() + deltay / slength * pixelsRemaining );
		}


	/** @returns sum of the lengths of the segments */
	static public double getTotalLength (List<Segment> segments) 
	{
		double totLength = 0.0;
		for (Segment seg : segments)
			totLength += seg.length();
		return totLength;
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
