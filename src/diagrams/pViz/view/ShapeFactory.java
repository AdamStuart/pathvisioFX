package diagrams.pViz.view;

import java.io.File;
import java.util.List;
import java.util.Set;

import diagrams.pViz.app.Controller;
import diagrams.pViz.app.Selection;
import diagrams.pViz.app.Tool;
import diagrams.pViz.gpml.CellShapeFactory;
import diagrams.pViz.gpml.GPMLPoint;
import diagrams.pViz.model.nodes.DataNode;
import gui.Action.ActionType;
import gui.UndoStack;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.event.EventType;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Line;
import javafx.scene.shape.Path;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import model.AttributeMap;
import util.FileUtil;
import util.LineUtil;
import util.RectangleUtil;
import util.StringUtil;

public class ShapeFactory {
	public static final double MARGIN = 8;
	public static final double MARGIN2 = 16;
	private Pasteboard pasteboard;
	boolean verbose = true;

	/*
	 * The ShapeFactory is responsible for creating new nodes that are shapes.
	 * Largely this is about defining the mouse event and drop handlers for the
	 * shapes.
	 * 
	 * makeMarquee creates the selection rectangle that is added on canvas drags.
	 */
	public ShapeFactory(Pasteboard l, UndoStack u) {
		pasteboard = l;
	}

	/*
	 * makeNewShape returns the shape but also adds it to stack's children
	 */
	static public Shape makeNewShape(String s,  VNode stack) {
		DataNode modelNode = stack.modelNode();
		Point2D center = modelNode.getPosition();
		Shape newShape= null;
		Tool tool = Tool.lookup(s);
//		AttributeMap attrMap = modelNode;
		switch (tool)
		{
			case Cell:				newShape = new Rectangle();	break;
			case Nucleus:			newShape = new Ellipse();	break;
			case Metabolite:		newShape = new Rectangle();	break;
			case Protein:			newShape = new Rectangle();	break;
			case Pathway:			newShape = new Rectangle();	break;
			case GroupComponent:	newShape = new Rectangle();	break;
			case ComplexComponent:	newShape = new Polygon();	break;

			case Rectangle:			newShape = new Rectangle();	break;
			case RoundedRectangle:	newShape = new Rectangle();	break;
			case Triangle:			newShape = new Polygon();	break;	// TODO
			case Pentagon:			newShape = new Polygon();	break;	// TODO
			case Hexagon:			newShape = new Polygon();	break;
			case Octagon:			newShape = new Polygon();	break;
			case Polygon:			newShape = new Polygon();	break;
			case Polyline:			newShape = new Polyline();	break;
			case Line:				newShape = new Line();		break;
			case GraphicalLine:		newShape = new Line();		break;
			case Shape1:			newShape = Shape1.getHeartPath();	break;	// TODO
			case Oval:				newShape = new Ellipse();	break;
			case Arc:				newShape = new Ellipse();	break;	// TODO
			case Circle:			newShape = new Ellipse();	break;
			default:	 
				if (Tool.contains(Tool.customShapes,s))
					newShape = CellShapeFactory.makeCustomShape(s); 
		}
		if (newShape == null) 					return null;

		String id = modelNode.get("GraphId");
		if (id == null)
		{
			id = modelNode.getModel().gensym("" + s.charAt(0));
			modelNode.setGraphId(id);
			modelNode.put("GraphId", id);
		}
		stack.setId(id);
		stack.getProperties().putAll(modelNode);
		setDefaultAttributes(newShape);
		setAttributes(newShape, modelNode);
	

		if ("Line".equals(s))
		{
			Arrow a = new Arrow((Line) newShape, 0.7f);
			a.setFill(Color.BEIGE);
		}
		if (tool == Tool.Mitochondria)
			stack.setScale(0.25);
		

		Insets insets = new Insets(2,2,2,2);  //getInsets();
        double hInsets = insets.getLeft() + insets.getRight();
        double vInsets = insets.getTop() + insets.getBottom();
        double w = modelNode.getDouble("Width", 15) + hInsets;
        double h = modelNode.getDouble("Height", 15 + vInsets);
        if (newShape instanceof Circle)
        {
        	Circle c = (Circle) newShape;
        	c.setRadius(Math.min(w, h)/ 2); 
        	c.setCenterX(center.getX());
        	c.setCenterY(center.getY());
        }
        else if (newShape instanceof Ellipse)
        {
        	Ellipse c = (Ellipse) newShape;
        	c.setRadiusX(w/ 2); 
        	c.setRadiusY(h/ 2); 
        	c.setCenterX(center.getX());
        	c.setCenterY(center.getY());
        	newShape.setVisible(true);
        }
        else if (newShape instanceof Rectangle)
        {
	        Rectangle r = (Rectangle) newShape;
	        r.setWidth(w);
	        r.setHeight(h);
	        r.setX(center.getX() - w /2 ); 
	        r.setY(center.getY() - h /2 ); 
        }
        else if (newShape instanceof Polygon)
        {
        	setupPolygon((Polygon) newShape, tool, center, w, h);
        }
       else if (newShape instanceof Path)
        {
//			Path p = (Path) figure;
//			p.scaleXProperty().bind(widthProperty());

        }
       else if ("GraphicalLine".equals(tool.name()))
		{
			List<GPMLPoint> pts = modelNode.getGPMLPoints();
			if (pts != null && !pts.isEmpty())
			{
				Line line = (Line) newShape;
				GPMLPoint start = pts.get(0);
				GPMLPoint end = pts.get(pts.size()-1);
				line.setStartX(start.getX());
				line.setStartY(start.getY());
				line.setEndX(end.getX());
				line.setEndY(end.getY());
				stack.setLayoutX(start.getX());
				stack.setLayoutY(start.getY());
				double wid = Math.abs(start.getX() - end.getX());
				double hght = Math.abs(start.getY() - end.getY());
				stack.setWidth(wid);
				stack.setHeight(hght);
				modelNode.putDouble("X", start.getX());
				modelNode.putDouble("Y", start.getY());
				modelNode.putDouble("Width",wid);
				modelNode.putDouble("Height", hght);
			}		
		}
		stack.getChildren().add(0, newShape);
//		for (Node n : stack.getChildren())
//			System.out.println(n.getId());
		return newShape;
	}
	// **-------------------------------------------------------------------------------
	private static void setupPolygon(Polygon p, Tool tool, Point2D center,  double w, double h) {
    	p.getPoints().clear();
     if (tool == Tool.ComplexComponent)			// this is an octagonal shape
    {
    	double barrelWidth = w / 8;
      	double barrelHeight = h / 8;
    	double x0 = center.getX() - w /2;
    	double x1 = x0 + barrelWidth;  //(w / 3);
    	double x3 = x0 + w;
    	double x2 = x3 - barrelWidth; 
      	double y0 = center.getY() -h /2;
    	double y1 = y0 + barrelHeight; // (h / 3);
    	double y3 = y0 + h;
    	double y2 = y3 - barrelHeight;
    	p.getPoints().addAll(x0,y1, x1,y0, x2,y0, x3,y1, x3,y2, x2,y3, x1,y3, x0,y2);
     }
      else if (tool == Tool.Triangle)
      {
    	double x0 = center.getX() - w /2;
    	double x2 = center.getX() + w /2;
    	double y0 = center.getY() -h /2;
    	double y1 = center.getY() + h /2;
    	p.getPoints().addAll(center.getX(),y0, x2,y1, x0,y1);
      }
      else if (tool == Tool.Pentagon)			// TODO check this   point at top
      {
    	   	double x0 = center.getX();
        	double y0 = center.getY() -h /2;

        	double theta1 = Math.toRadians(72);
        	double theta2 = Math.toRadians(144);
        	double r1 = x0 + (w/2) * Math.cos(theta1);
        	double r2 = x0 + (w/2) * Math.cos(theta2);
        	double l1 = x0 - (w/2) * Math.cos(theta1);
        	double l2 = x0 - (w/2) * Math.cos(theta2);
        	double y1 = center.getY() + (h /2) * Math.sin(theta1);
        	double y2 = center.getY() + (h /2) * Math.sin(theta2);
        	p.getPoints().addAll(x0, y0, r1, y1, r2, y2, l2, y2, l1, y1);
      }
      	
      else if (tool == Tool.Hexagon)		// TODO check this			flat across the top
      {
    	   	double x0 = center.getX();
      	   	double x1 = x0 - .3 * w;
      	   	double x2 = x0 + .3 * w;
	    	double y0 = center.getY();
	    	double left = x0 - w /2;
	    	double right = x0 + w /2;
	    	double top = y0 - h /2;
	    	double bottom = y0 + h /2;
	    	p.getPoints().addAll(left, y0, x1, top, x2, top, right, y0, x2, bottom, x1, bottom);
      }
  	}
	// **-------------------------------------------------------------------------------

	static public Rectangle makeMarquee() {
		Rectangle marquee = new Rectangle();
		marquee.setId("Marquee");
		marquee.setStroke(Color.GREEN);
		marquee.setFill(Color.TRANSPARENT);
		marquee.setStrokeWidth(1.6);
		marquee.getStrokeDashArray().addAll(3.0, 7.0, 3.0, 7.0);
		return marquee;
	}
	// **-------------------------------------------------------------------------------
	static private void setDefaultAttributes(Shape newShape) {
		newShape.setFill(Color.WHITE);
		newShape.setStroke(Color.BLACK);
		newShape.setStrokeWidth(0.5f);
	}
	public static void setAttributes(Shape shape, AttributeMap map) {
		// if (verbose>0) System.out.println(map.toString());		if (shape == null)	return;
		
		String shapeType = map.get("ShapeType");
		if ("{".equals(shapeType))
		{
			map.put("ShapeType", "Brace");
			map.put("Fill", "None");
			map.put("TextLabel", "");
		}
		Rectangle r = null;
		if (shape instanceof Rectangle)
		{
			r = ((Rectangle)shape);
			if ( "RoundedRectangle".equals(shapeType))
			{	
				r.setArcWidth(10); 			r.setArcHeight(10);
			}
			if ("GeneProduct".equals(shapeType))
			{	
				r.setFill(Color.GOLD);  	r.setStrokeWidth(1.0);
			}
			if ("Cell".equals(shapeType))
			{	
				r.setArcWidth(100);			r.setArcHeight(100);
				r.setStroke(Color.GOLD);	r.setStrokeWidth(5.0);
			}
			if ("Pathway".equals(shapeType))
			{	
				r.setStroke(Color.AQUAMARINE);		r.setStrokeWidth(5.0);
				r.setWidth(120); 			r.setHeight(45);
			}
			if ("Gene".equals(shapeType))
			{	
				r.setStrokeWidth(1.0);		r.setWidth(80); 	r.setHeight(30);
			}
			if ("Metabolite".equals(shapeType))
			{	
				r.setStroke(Color.NAVY);	r.setStrokeWidth(2.0);
				r.setWidth(120); 			r.setHeight(20);
			}
		}	
		for (String k : map.keySet()) 
		{
			String val = map.get(k);
			k = k.toLowerCase();
			if (k.equals("graphid"))   			shape.setId(val);
			if (k.equals("fontsize"))			k = "-fx-font-size";
			if (k.equals("fontweight"))			k = "-fx-font-size";
			if (k.equals("valign"))				;// TODO	
			if (k.equals("zorder"))				;// TODO	
			if (k.equals("stroke"))				k = "-fx-stroke";
			if (k.equals("strokewidth"))		k = "-fx-stroke-weight";
			if (k.equals("linethickness"))		k = "-fx-stroke-weight";
			if (k.equals("graphid"))			shape.setId(val);

			double d = StringUtil.toDouble(val); // exception safe: comes back NaN if val is not a number
			if (r != null) {
				if (k.equals("centerx"))		r.setX(d+MARGIN);	
				else if (k.equals("centery"))	r.setY(d+MARGIN);
				else if (k.equals("x"))			r.setX(d+MARGIN);
				else if (k.equals("y"))			r.setY(d+MARGIN); 
				else if (k.equals("width"))		r.setWidth(d+MARGIN2);
				else if (k.equals("height"))	r.setHeight(d+MARGIN2);
			}			
			if (shape instanceof Circle) {
				Circle circ = (Circle) shape;
				if (k.equals("centerx"))		circ.setCenterX(d);
				else if (k.equals("centery"))	circ.setCenterY(d);
				else if (k.equals("x"))			circ.setCenterX(d);
				else if (k.equals("y"))			circ.setCenterY(d);
				else if (k.equals("width"))		circ.setRadius(d/2);
				else if (k.equals("radius"))	circ.setRadius(d);
			}
			if (shape instanceof Polygon) {
				Polygon poly = (Polygon) shape;
				if (k.equals("points"))			parsePolygonPoints(poly, map.get(k));
			}
			if (shape instanceof Polyline) {
				Polyline poly = (Polyline) shape;
				if (k.equals("points"))			parsePolylinePoints(poly, map.get(k));
			}
			if (shape instanceof Line) {
				Line line = (Line) shape;
				if (k.equals("points"))			parseLinePoints(line, map.get(k));
				if (k.equals("stroke-width"))	line.setStrokeWidth(d);
			}
			try {
				Shape sh = shape;   
				if (k.equals("fill") || k.equals("-fx-fill") || k.equals("fillcolor")) 
					sh.setFill(Color.web(val));
				else if (k.equals("-fx-stroke") || k.equals("color"))	
					sh.setStroke(Color.web(val));
				else if (k.equals("-fx-stroke-weight") || k.equals("linethickness"))
					sh.setStrokeWidth(d);
			} 
			catch (Exception e) {		System.err.println("Parse errors: " + k);	}
		}
	}
	public static void setBounds(Pasteboard pasteboard, VNode activeStack, Point2D start, Point2D end ) {
		double w = Math.max(20,Math.abs(end.getX() - start.getX()));
		double h = Math.max(20,Math.abs(end.getY() - start.getY()));
		setBounds( pasteboard, activeStack, activeStack.getFigure(), start, end, w, h );
	}

	public static void setBounds(Pasteboard pasteboard, VNode activeStack, Shape activeShape, Point2D start, Point2D end, double w, double h ) {

		if (activeShape instanceof Rectangle)
		{
			Rectangle r = (Rectangle) activeShape;
//			r.setVisible(true);
//			RectangleUtil.setRect(r, left, top ,w, h);
			activeStack.setWidth(w);
			activeStack.setHeight(h);
		}
		if (activeShape instanceof Circle)
		{
			Circle c = (Circle) activeShape;
//			c.setVisible(true);
			c.setCenterX(end.getX());
			c.setCenterY(end.getY());
			double rad = Math.sqrt(w * w + h * h);
			c.setRadius(rad);
			activeStack.setWidth(2*rad);
			activeStack.setHeight(2*rad);
		}
		
		if (activeShape instanceof Polygon)
		{
			Polygon p = (Polygon) activeShape;
//			p.setVisible(true);
			int nPts = p.getPoints().size();
			if (nPts > 1)
			{
				p.getPoints().set(nPts-2, end.getX());
				p.getPoints().set(nPts-1, end.getY());
			}
		}
		
		if (activeShape instanceof Polyline)
		{
			Polyline p = (Polyline) activeShape;
//			p.setVisible(true);
			p.setFill(null);
			int nPts = p.getPoints().size();
			if (nPts > 1)
			{
				p.getPoints().set(nPts-2, end.getX());
				p.getPoints().set(nPts-1, end.getY());
			}
		}
		
		if (activeShape instanceof Line)
		{
			Line p = (Line) activeShape;
			p.setEndX(end.getX());
			p.setEndY(end.getY());
		}
	}

	static public void resizeFigureToNode(VNode stack) {
		Shape figure = stack.getFigure();
		if (figure != null)
		{
			if (figure instanceof Rectangle)
			{
				Rectangle r = (Rectangle) figure;
				r.setWidth(stack.getWidth());	
				r.setHeight(stack.getHeight());
			}
			if (figure instanceof Circle)
			{
				Circle c = (Circle) figure;
				c.setCenterX(stack.getCenterX()); 
				c.setCenterY(stack.getCenterY());
				c.setRadius(Math.min(stack.getHeight(),stack.getWidth())/2);
			}
			if (figure instanceof Ellipse)
			{
				Ellipse e = (Ellipse) figure;
				e.setCenterX(stack.getCenterX()); 
				e.setCenterY(stack.getCenterY());
				e.setRadiusX(stack.getWidth()/2);
				e.setRadiusY(stack.getHeight()/2);
			}
			if (figure instanceof Path)
			{
				Path p = (Path) figure;
				double scale = Math.min(stack.getWidth() / 400, stack.getHeight() / 300);
				p.setScaleX(5 * scale);
				p.setScaleY(5 * scale);
			}
			if (figure instanceof Polygon)
			{	double pad = 8;
				ShapeFactory.sizeFigureToBounds(stack.getPasteboard(),stack, figure, 
						stack.getLayoutX(), stack.getLayoutY(), stack.getWidth(), stack.getHeight(), pad);
			}
		}
	}

public static void sizeFigureToBounds(Pasteboard pasteboard, VNode vNode, Shape figure, double layoutX,
		double layoutY, double w, double h, double padding) {

	String type = vNode.getShapeType();

	if (figure instanceof Polygon)  // && "ComplexComponent".equals(type)
	{
		double centerX = layoutX  + w /2;
		double centerY = layoutY + h /2;
      	Polygon p = (Polygon) figure;
		
       	double barrelWidth = w / 6;
    	double barrelHeight = h / 6;
    	double x0 = centerX - w /2 - padding;
    	double x1 = x0 + barrelWidth;  //(w / 3);
    	double x3 = x0 + w + 2 * padding;
    	double x2 = x3 - barrelWidth; 
      	double y0 = centerY -h /2  - padding;
    	double y1 = y0 + barrelHeight; // (h / 3);
    	double y3 = y0 + h  + 2 * padding;
    	double y2 = y3 - barrelHeight;
    	p.getPoints().clear();
    	p.getPoints().addAll(x0,y1, x1,y0, x2,y0, x3,y1, x3,y2, x2,y3, x1,y3, x0,y2);
	}
}
// **-------------------------------------------------------------------------------

	private static void parsePolygonPoints(Polygon poly, String string) {	parsePoints(poly.getPoints(), string);	}

	private static void parsePolylinePoints(Polyline poly, String string) {	parsePoints(poly.getPoints(), string);	}

	private static void parsePoints(ObservableList<Double> points, String string) {
		String s = string.trim();
		s = s.substring(1, s.length());
		String[] doubles = s.split(",");
		for (String d : doubles)
			points.add(Double.parseDouble(d));
	}

	private static void parseLinePoints(Line line, String string) {
		String s = string.trim();
		s = s.substring(1, s.length());
		String[] doubles = s.split(",");
		assert(doubles.length == 4);
		line.setStartX(Double.parseDouble(doubles[0]));
		line.setStartY(Double.parseDouble(doubles[1]));
		line.setEndX(Double.parseDouble(doubles[2]));
		line.setEndY(Double.parseDouble(doubles[3]));
	}

	// **-------------------------------------------------------------------------------
	// MouseEvents and DragEvents
	public void makeHandlers(Shape s) {
		if (s == null)		return;
		if (s instanceof Circle)			new CircleMouseHandler((Circle) s, pasteboard);
		else if (s instanceof Rectangle)	new RectMouseHandler((Rectangle) s, pasteboard);
		else if (s instanceof Polygon)		new PolygonMouseHandler((Polygon) s, pasteboard);
		else if (s instanceof Polyline)		new PolylineMouseHandler((Polyline) s, pasteboard);
		else if (s instanceof Line)			new LineMouseHandler((Line) s, pasteboard);
		else if (s instanceof Shape)		new ShapeMouseHandler((Shape) s, pasteboard);
//		if (s instanceof Shape2)		new ShapeMouseHandler((Shape2) s, drawLayer);

		s.setOnDragEntered(e -> {	s.setEffect(null);			e.consume();		});  // Effects.sepia
		s.setOnDragExited(e -> 	{	s.setEffect(null);					e.consume();		});
		s.setOnDragOver(e -> 	{	e.acceptTransferModes(TransferMode.ANY); 	e.consume();	});
		s.setOnDragDropped(e -> { 	e.acceptTransferModes(TransferMode.ANY); 	handleDrop(s, e);	e.consume();	});
	}
	// **-------------------------------------------------------------------------------
	private void handleDrop(Shape s, DragEvent e) {
		Dragboard db = e.getDragboard();
		Set<DataFormat> formats = db.getContentTypes();
		formats.forEach(a -> System.out.println("getContentTypes " + a.toString()));
		Shape shape = (Shape) e.getTarget();
		if (db.hasString()) {
			String q = db.getString();
			if (q.contains("-fx-")) {
				AttributeMap attrMap = new AttributeMap(q);
				setAttributes(shape, attrMap);
				Selection sel = pasteboard.getController().getSelectionManager();
				if (sel.isSelected(shape))
					sel.setAttributes(attrMap);
			}
//			if (verbose) System.out.println(q);
		}

		if (db.hasFiles()) {
			List<File> files = db.getFiles();
			if (files != null) {
				// controller.getUndoStack().push(ActionType.Add, " file");
//				int offset = 0;
				for (File f : files) {
//					offset += 20;
					if (verbose) System.out.println("File: " + f.getAbsolutePath());
					if (FileUtil.isCSS(f)) {
						StringBuilder buff = new StringBuilder();
						FileUtil.readFileIntoBuffer(f, buff);
						String styl = buff.toString();
						shape.getStyleClass().add(styl);
						if (verbose) System.out.println("S: " + styl);
					}
				}
			}
		}
	}
	// **-------------------------------------------------------------------------------
	private void doDoubleClick(final MouseEvent event) {
		
		EventTarget target = event.getTarget();
		if (target instanceof Rectangle)
		{
			Rectangle rect = (Rectangle) target;
			Object obj = rect.getProperties().get("BiopaxRef");
			if (obj instanceof String)
			{
				pasteboard.getController().openByReference("" + obj);
			}
		}
	}
	// **-------------------------------------------------------------------------------
	// SHAPE MOUSE HANDLERS

	private class RectMouseHandler extends NodeMouseHandler {
		public RectMouseHandler(Rectangle r, Pasteboard d) {
			super(d);
			r.addEventHandler(MouseEvent.MOUSE_PRESSED, this);
			r.addEventHandler(MouseEvent.MOUSE_DRAGGED, this);
			r.addEventHandler(MouseEvent.MOUSE_MOVED, this);
			r.addEventHandler(MouseEvent.MOUSE_ENTERED, this);
			r.addEventHandler(MouseEvent.MOUSE_EXITED, this);
			r.addEventHandler(MouseEvent.MOUSE_RELEASED, this);
		}

		@Override
		protected void handleMousePressed(final MouseEvent event) {
			if (pasteboard.getTool() != Tool.Arrow) return;
			if (((Node) event.getTarget()).getParent() instanceof Group)
				return;
			super.handleMousePressed(event);
			if (event.getClickCount() > 1) {
				doDoubleClick(event);
				return;
			}
			startPoint = currentPoint;

			if (RectangleUtil.inCorner(event)) {
				resizing = true;
				dragging = false;
				startPoint = RectangleUtil.oppositeCorner(event);
				undoStack.push(ActionType.Resize);
				System.out.println("Pressed: " + currentPoint + " is opposite " + startPoint.toString());
			}
		}

		@Override
		protected void handleMouseDragged(final MouseEvent event) {
			if (pasteboard.getTool() != Tool.Arrow) return;
			if (((Node) event.getTarget()).getParent() instanceof Group)
				return;
			if (verbose > 3)
				System.out.println("RectMouseDraggedHandler, Target: " + event.getTarget());
			super.handleMouseDragged(event);

			if (resizing) {
//				System.out.println("startPoint: " + startPoint.toString());
//				System.out.println("CurrentPoint: " + currentPoint.toString());
				double x, y, width, height;
				x = Math.min(startPoint.getX(), currentPoint.getX());
				y = Math.min(startPoint.getY(), currentPoint.getY());
				width = Math.abs(startPoint.getX() - currentPoint.getX());
				height = Math.abs(startPoint.getY() - currentPoint.getY());

//				System.out.println("( " + x + ", " + y + ") Width = " + width + " height = " + height);

				if (event.getTarget() instanceof Rectangle) {
					Rectangle r = (Rectangle) event.getTarget();
					RectangleUtil.setRect(r, x, y, width, height);
				}
			}
		}

		@Override
		protected void handleMouseMoved(final MouseEvent event) {
			if (((Node) event.getTarget()).getParent() instanceof Group)
				return;
			super.handleMouseMoved(event);
			if (event.getTarget() instanceof Rectangle) {
				Rectangle r = (Rectangle) event.getTarget();
				r.setCursor(RectangleUtil.inCorner(currentPoint, r) ? Cursor.H_RESIZE : Cursor.HAND);
			}
		}

	}
	// **-------------------------------------------------------------------------------

	private class ShapeMouseHandler extends NodeMouseHandler {
		public ShapeMouseHandler(Shape s, Pasteboard d) {
			super(d);
			s.addEventHandler(MouseEvent.MOUSE_PRESSED, this);
			s.addEventHandler(MouseEvent.MOUSE_DRAGGED, this);
			s.addEventHandler(MouseEvent.MOUSE_MOVED, this);
			s.addEventHandler(MouseEvent.MOUSE_RELEASED, this);
		}

		@Override
		protected void handleMousePressed(final MouseEvent event) {
			if (pasteboard.getTool() != Tool.Arrow) return;
			if (((Node) event.getTarget()).getParent() instanceof Group)
				return;
			super.handleMousePressed(event);
		}

		@Override
		protected void handleMouseDragged(final MouseEvent event) {
			if (pasteboard.getTool() != Tool.Arrow) return;
//			if (((Node) event.getTarget()).getParent() instanceof Group)
//				return;
			if (verbose > 3)
				System.out.println("RectMouseDraggedHandler, Target: " + event.getTarget());
			super.handleMouseDragged(event);

//			if (resizing) {
////				System.out.println("startPoint: " + startPoint.toString());
////				System.out.println("CurrentPoint: " + currentPoint.toString());
//				double x, y, width, height;
//				x = Math.min(startPoint.getX(), currentPoint.getX());
//				y = Math.min(startPoint.getY(), currentPoint.getY());
//				width = Math.abs(startPoint.getX() - currentPoint.getX());
//				height = Math.abs(startPoint.getY() - currentPoint.getY());
//
////				System.out.println("( " + x + ", " + y + ") Width = " + width + " height = " + height);
//
//				if (event.getTarget() instanceof Rectangle) {
//					Rectangle r = (Rectangle) event.getTarget();
//					RectangleUtil.setRect(r, x, y, width, height);
//				}
//			}
		}

		@Override
		protected void handleMouseMoved(final MouseEvent event) {
			if (((Node) event.getTarget()).getParent() instanceof Group)
				return;
			super.handleMouseMoved(event);
//			if (event.getTarget() instanceof Rectangle) {
//				Rectangle r = (Rectangle) event.getTarget();
//				r.setCursor(RectangleUtil.inCorner(currentPoint, r) ? Cursor.H_RESIZE : Cursor.HAND);
//			}
		}
	}
	// **-------------------------------------------------------------------------------
	// **-------------------------------------------------------------------------------

	protected class CircleMouseHandler extends NodeMouseHandler {
		public CircleMouseHandler(Circle c, Pasteboard d) {
			super(d);
			c.addEventHandler(MouseEvent.MOUSE_PRESSED, this);
			c.addEventHandler(MouseEvent.MOUSE_DRAGGED, this);
			c.addEventHandler(MouseEvent.MOUSE_MOVED, this);
			c.addEventHandler(MouseEvent.MOUSE_RELEASED, this);
		}

		@Override
		protected void handleMousePressed(final MouseEvent event) {
			if (pasteboard.getTool() == Tool.Line) 
				{
//				create the line from the center
//				return;
				
				}
			if (pasteboard.getTool() != Tool.Arrow) return;
			if (((Node) event.getTarget()).getParent() instanceof Group)			return;
			super.handleMousePressed(event);
			Circle c = (Circle) event.getTarget();
			if (LineUtil.onEdge(currentPoint, c)) {
				resizing = true;
				dragging = false;
				undoStack.push(ActionType.Resize);
			}
		}

		@Override
		protected void handleMouseDragged(final MouseEvent event) {
			if (pasteboard.getTool() != Tool.Arrow) return;
			if (((Node) event.getTarget()).getParent() instanceof Group)		return;
			super.handleMouseDragged(event);
			if (verbose > 3)
				System.out.println("RectMouseDraggedHandler, Target: " + event.getTarget());

			if (resizing) {
				Circle c = (Circle) target;
				double dx = c.getCenterX() - currentPoint.getX();
				double dy = c.getCenterY() - currentPoint.getY();

				double newRadius = Math.sqrt(dx * dx + dy * dy);
				c.setRadius(newRadius);
			}
		}

		@Override
		protected void handleMouseMoved(final MouseEvent event) {
			super.handleMouseMoved(event);
			Circle c = (Circle) target;
			if (LineUtil.onEdge(currentPoint, c))	c.setCursor(Cursor.H_RESIZE);
			else							c.setCursor(Cursor.HAND);
		}
	}


	// **-------------------------------------------------------------------------------
	protected class PolygonMouseHandler extends NodeMouseHandler {
		public PolygonMouseHandler(Polygon p, Pasteboard d) {
			super(d);
			assignMouseHandlers( p, this);
		}

		@Override
		protected void handleMousePressed(final MouseEvent event) {
			super.handleMousePressed(event);
			if (event.getTarget() == pasteboard.getActiveStack()) {
				pasteboard.setActiveStack(null);
				event.consume();
				return;
			}
			if (verbose > 3)
				System.out.println("PolygonMousePressedHandler: " + event.getTarget());
			Polygon p = (Polygon) target;
			int idx = LineUtil.onVertex(currentPoint, p);
			// System.out.println("" + idx);
			if (idx >= 0)	activeIndex = idx;
			else			dragging = true;
		}

		int activeIndex = -1;

		@Override
		protected void handleMouseDragged(final MouseEvent event) {
			super.handleMouseDragged(event);
			if (verbose > 1)
				System.out.println("Index: " + activeIndex);
			Polygon p = (Polygon) target;
			if (activeIndex >= 0) {
				p.getPoints().set(activeIndex, currentPoint.getX());
				p.getPoints().set(activeIndex + 1, currentPoint.getY());
			}
		}

		@Override
		protected void handleMouseMoved(final MouseEvent event) {
			super.handleMouseMoved(event);
			Polygon p = (Polygon) target;
			if (LineUtil.onVertex(currentPoint, p) >= 0)	p.setCursor(Cursor.H_RESIZE);
			else	p.setCursor(Cursor.HAND);
		}
	}

	// **-------------------------------------------------------------------------------
	// **-------------------------------------------------------------------------------
	// **-------------------------------------------------------------------------------
	// **-------------------------------------------------------------------------------
	// **-------------------------------------------------------------------------------
	protected class PolylineMouseHandler extends NodeMouseHandler {
		public PolylineMouseHandler(Polyline p, Pasteboard d) {
			super(d);
			assignMouseHandlers( p, this);
		}
//		SimpleDoubleProperty mouseX, mouseY;
		

		@Override
		protected void handleMousePressed(final MouseEvent event) {
			super.handleMousePressed(event);
			Polyline p = (Polyline) target;
			int idx = LineUtil.onVertex(currentPoint, p);
			if (event.getTarget() == pasteboard.getActiveStack() && idx == 0) {
				pasteboard.setActiveStack(null);
				activeIndex = p.getPoints().size();
				p.getPoints().addAll(currentPoint.getX(), currentPoint.getY());
				event.consume();
				pasteboard.removeDragLine();
				pasteboard.resetTool();
				return;
			}
			if (verbose > 3)
				System.out.println("PolylineMousePressedHandler: " + event.getTarget());
			// System.out.println("" + idx);
			if (idx >= 0)		activeIndex = idx;
			else				dragging = true;

			p.getPoints().set(activeIndex, currentPoint.getX());
			p.getPoints().set(activeIndex+1, currentPoint.getY());
		}
		int activeIndex = -1;

		@Override
		protected void handleMouseDragged(final MouseEvent event) {
			super.handleMouseDragged(event);
			if (verbose > 1)				System.out.println("Index: " + activeIndex);
			Polyline p = (Polyline) target;
			if (activeIndex >= 0) {
				p.getPoints().set(activeIndex, currentPoint.getX());
				p.getPoints().set(activeIndex + 1, currentPoint.getY());
			}
			if (dragLine != null) 	
			{
				dragLine.setEndX(event.getX());
				dragLine.setEndY(event.getY());
			}
//			if (mouseX != null) 	mouseX.set(event.getX());
//			if (mouseY != null) 	mouseY.set(event.getY());
		}

		@Override
		protected void handleMouseMoved(final MouseEvent event) {
			super.handleMouseMoved(event);
//			drawLayer.setLastClick(event.getX(), event.getY());
			Polyline p = (Polyline) target;
			if (LineUtil.onVertex(currentPoint, p.getPoints()) >= 0)			p.setCursor(Cursor.H_RESIZE);
			else p.setCursor(Cursor.HAND);
			if (dragLine != null) 	
			{
				dragLine.setEndX(event.getX());
				dragLine.setEndY(event.getY());
			}
//			if (mouseY != null) 	
		}
		@Override
		protected void handleMouseReleased(final MouseEvent event) {
			
//			drawLayer.getPane().getChildren().remove(dragLine);
//			dragLine = null;
		}

	}
	// **-------------------------------------------------------------------------------
	protected class LineMouseHandler extends NodeMouseHandler {
		public LineMouseHandler(Line p, Pasteboard d) {
			super(d);
			assignMouseHandlers( p, this);
		}
//		SimpleDoubleProperty mouseX, mouseY;
		

		@Override
		protected void handleMousePressed(final MouseEvent event) {
			super.handleMousePressed(event);
			if (event.getTarget() == pasteboard.getActiveStack()) {
				pasteboard.setActiveStack(null);
				event.consume();
				return;
			}
			if (verbose > 3)
				System.out.println("LineMousePressedHandler: " + event.getTarget());
			Line p = (Line) target;
			activeIndex = LineUtil.onEndpoint(currentPoint, p);
			// System.out.println("" + idx);
//			if (idx >= 0)		activeIndex = idx;
//			else				dragging = true;
			
		}
		int activeIndex = -1;

		@Override
		protected void handleMouseDragged(final MouseEvent event) {
			super.handleMouseDragged(event);
			if (verbose > 3)				System.out.println("Index: " + activeIndex);
			Line p = (Line) target;
			if (activeIndex == 0) {
				p.setStartX(currentPoint.getX());
				p.setStartY(currentPoint.getY());
			}
			else if (activeIndex == 1) {
				p.setEndX(currentPoint.getX());
				p.setEndY(currentPoint.getY());
			}
			else super.handleMouseDragged(event);
		}
	}

	// -----------------------------------------------------------------------------------
	public void makeNodeMouseHandler(Node n) {
		NodeMouseHandler h = new NodeMouseHandler(pasteboard);
		assignMouseHandlers(n, h);
	}
	
	private void assignMouseHandlers(Node n, NodeMouseHandler h)
	{
		n.addEventHandler(MouseEvent.MOUSE_PRESSED, h);
		n.addEventHandler(MouseEvent.MOUSE_DRAGGED, h);
		n.addEventHandler(MouseEvent.MOUSE_MOVED, h);
		n.addEventHandler(MouseEvent.MOUSE_RELEASED, h);

	}
	// -----------------------------------------------------------------------------------
		private class NodeMouseHandler extends BasicMouseHandler {
			
		public NodeMouseHandler(Pasteboard d) {		super(d);	}

		@Override
		public void handle(MouseEvent e) {
			target = e.getTarget();
			// scenePoint = new Point2D(e.getSceneX(), e.getSceneY());
			currentPoint = new Point2D(e.getX(), e.getY());
			if (verbose > 4)
				System.out.println((int) currentPoint.getX() + ", " + (int) currentPoint.getY());
			EventType<?> type = e.getEventType();

			if (type == MouseEvent.MOUSE_MOVED)				handleMouseMoved(e);
			else if (type == MouseEvent.MOUSE_PRESSED)		handleMousePressed(e);
			else if (type == MouseEvent.MOUSE_DRAGGED)		handleMouseDragged(e);
			else if (type == MouseEvent.MOUSE_RELEASED)		handleMouseReleased(e);
			prevPoint = currentPoint;
		}

		// **-------------------------------------------------------------------------------
		protected Point2D startPoint, currentPoint, prevPoint, offset;

		protected int verbose = 3;
		protected boolean dragging = false;
		protected boolean resizing = false;
		protected EventTarget target;
		Line dragLine = new Line();
		private ContextMenu menu;

		// **-------------------------------------------------------------------------------
		protected void handleMouseMoved(MouseEvent event) {
			event.consume();
		}

		protected void handleMousePressed(MouseEvent event) {
			if (verbose >= 3)
				System.out.println("NodeMousePressedHandler, Target: " + event.getTarget());
			if (pasteboard.getTool().isArrow())
			{
				resizing = false;
				prevPoint = currentPoint;
				boolean altDown = event.isAltDown();
				// boolean leftClick = event.isPrimaryButtonDown();
				boolean rightClick = event.isSecondaryButtonDown();
				if (altDown)
					pasteboard.getController().getSelectionManager().cloneSelection(5);

				if (event.isPopupTrigger() || rightClick) {
					if (menu == null)
						menu = buildContextMenu();
					if (menu != null)
						menu.show(pasteboard, event.getScreenX(), event.getScreenY());
					return;
				}
				Selection sel = pasteboard.getSelectionMgr();
				if (target instanceof VNode)
				{
					VNode node = (VNode) target;
					boolean wasSelected = sel.isSelected(node);
					if (event.isControlDown())			sel.select(node, !wasSelected);
					else if ((event.isShiftDown()))		sel.select(node);
					else if (!wasSelected)				sel.selectX(node);
				}
				if (sel.count() > 0) {
					dragging = true;
					pasteboard.getController().getUndoStack().push(ActionType.Move);
					startPoint = currentPoint;
				}
				event.consume();
			}
	}
		//--------------------------------------------------------------------------------------------
		private ContextMenu buildContextMenu() {
			menu = new ContextMenu();
			Controller c = pasteboard.getController();
//			MenuItem cut = 		makeItem("Cut", a -> 			{	c.cutSelection();	});
//			MenuItem copy = 		makeItem("Copy", a -> 			{	c.copySelection();	});
			MenuItem dup = 		makeItem("Duplicate", a -> 		{	c.duplicateSelection();	});
			MenuItem del = 		makeItem("Delete", a -> 		{	c.deleteSelection();	});
			MenuItem toFront = 	makeItem("Bring To Front", a -> {	c.toFront();	});
			MenuItem toBack = 	makeItem("Send To Back", a -> 	{	c.toBack();	});
			MenuItem group = 	makeItem("Group", a -> 			{	c.group();	});
			MenuItem ungroup = 	makeItem("Ungroup", a -> 		{	c.ungroup();	});
			MenuItem lock = 	makeItem("Lock", a -> 			{	c.lock();	});
			MenuItem unlock = 	makeItem("Unlock", a -> 		{	c.unlock();	});
			menu.getItems().addAll(toFront, toBack, group, ungroup, dup, del, lock, unlock);   //cut, copy
			return menu;
		}
		
		private MenuItem makeItem(String name, EventHandler<ActionEvent> foo)
		{
			MenuItem item = new MenuItem(name);	
			item.setOnAction(foo);
			return item;
		}
		//--------------------------------------------------------------------------------------------

		protected void handleMouseDragged(MouseEvent event) {
		if (verbose > 3)
				System.out.println("NodeMouseDraggedHandler, Target: " + event.getTarget());
			// do nothing for a right-click
			if (event.isSecondaryButtonDown())
				return;
			if (pasteboard.getTool() == Tool.Arrow)
			{
				if (dragging) {
					double dx, dy;
					dx = prevPoint.getX() - currentPoint.getX();
					dy = prevPoint.getY() - currentPoint.getY();
	
					// System.out.println("Delta: " + dx + ", " + dy);
					pasteboard.getSelectionMgr().translate(dx, dy);
					prevPoint = currentPoint;
				}
			}
			event.consume();
		}

		protected void handleMouseReleased(MouseEvent event) {
			startPoint = null;
//			dragLine = null;
			resizing = dragging = false;
			pasteboard.requestFocus(); 
			event.consume();
//			drawLayer.getController().refreshZoomPane();
		}
	}

		public static void addPoint(Shape activeShape, double x, double y) {
			if (activeShape instanceof Polygon)
			{
				Polygon p = (Polygon) activeShape;
				p.getPoints().addAll(x, y);
			}
			if (activeShape instanceof Polyline)
			{
				Polyline p = (Polyline) activeShape;
				p.getPoints().addAll(x, y);
			}
			if (activeShape instanceof Line)
			{
				Line p = (Line) activeShape;
				p.setStartX(x);
				p.setStartY(y);
				p.setEndX(x);
				p.setEndY(y);
			}
		}
}
