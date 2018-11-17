package diagrams.pViz.view;

import java.util.ArrayList;
import java.util.List;

import diagrams.pViz.app.Controller;
import diagrams.pViz.app.Selection;
import diagrams.pViz.app.Tool;
import diagrams.pViz.gpml.GPML;
import diagrams.pViz.model.DataNode;
import diagrams.pViz.model.DataNodeGroup;
import diagrams.pViz.model.DataNodeState;
import diagrams.pViz.tables.ReferenceController;
import diagrams.pViz.util.ResizableBox;
import gui.Action.ActionType;
import gui.Backgrounds;
import javafx.beans.property.BooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.InnerShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.util.Pair;
import model.AttributeMap;
import model.bio.BiopaxRecord;
import model.stat.RelPosition;
// a VNode can be a shape or a control or a container
import util.StringUtil;

public class VNode extends ResizableBox implements Comparable<VNode> {		//StackPane

	private DataNode dataNode;
	private Shape figure;
	private Label text;
	private Label graphIdLabel;
	private Label zOrderLabel;
	private Label lockLabel;
	private String title;
	private boolean movable;
	private boolean resizable;
	private boolean editable;
	private boolean connectable;
//	private TooltipBehavior myTTB = new TooltipBehavior(
//	        new Duration(1000), new Duration(5000), new Duration(200), false);
	
	private Pasteboard pasteboard = null;
	private Selection selection = null;
	private ControlFactory controlFactory;
	public VNode clone()
	{
		return new VNode(this);
	}
	public VNode(VNode orig)		 {		 this(orig.modelNode(), orig.pasteboard);	}
	public void setScale(double x)
	{
		setScaleX(x);
		setScaleY(x);
	}
	public VNode(DataNode modelNode, Pasteboard p)
	{
		super(p);
		assert(modelNode != null && p != null);
		dataNode = modelNode;
		dataNode.setStack(this);
		pasteboard = p;
		selection = pasteboard.getSelectionMgr();
		controlFactory = new ControlFactory(this);
//		AttributeMap attributes = modelNode;
		if (modelNode instanceof DataNodeGroup)
		{
			System.out.println("GROUP");
		}
		createFigure(modelNode.getPosition());
		String id = modelNode.get("GraphId");
		setId(id);
		title = modelNode.get("TextLabel");
		if (title == null) title = id;
		addText(title);
		String biopaxRef = modelNode.get("BiopaxRef");
		if (biopaxRef != null)
			tagCorner(Color.LIGHTSEAGREEN, Pos.TOP_LEFT, biopaxRef);

//		String type = attributes.get("ShapeType");
//		System.out.println(title);
		addPorts();
        readGeometry(modelNode, this);
		addGraphIdDisplay();
		addZOrderDisplay();
		setZOrder();
		String shapeType = dataNode.get("ShapeType");
        if (shapeType == null) shapeType = "";
        String type = dataNode.get("Type");
        if (type == null) type = "";

        readGeometry(modelNode, this);
        movable = modelNode.getBool("Movable", true);
        resizable = modelNode.getBool("Resizable", false);
        setResize(resizable);
        editable = modelNode.getBool("Editable", true);
        boolean connectable = isConnectable();
        if ("Label".equals(type) || "Shape".equals(type))
        	connectable = false;
        setConnectable(connectable);
        modelNode.putBool("Connectable", connectable);

        Tooltip tooltip = new Tooltip();
        tooltip.setOnShowing(v -> { tooltip.setText(modelNode.getSortedAttributes());});
        Tooltip.install(this,  tooltip);

        if ("Label".equals(modelNode.get("Type"))) 	connectable = false;
        if (shapeType.equals("Mitochondria)")) 	connectable = false;
        if (shapeType.equals("Oval)"))        	connectable = false;
        if (shapeType.equals("Cell)"))        	connectable = false;
        modelNode.putBool("Connectable", connectable);
  		layoutBoundsProperty().addListener(e -> { extractPosition(); } ); 
 		pasteboard.add(this);
	}

	// **-------------------------------------------------------------------------------
	public void addGraphIdDisplay()
	{
		String id = modelNode().get("GraphId");
		graphIdLabel = new Label(id);
		addAnnotation(graphIdLabel, getController().graphIdsVisibleProperty(),Pos.TOP_LEFT, -getWidth()/4, -getHeight()/4-10);
	}	
	public void setZOrder()
	{
		zOrderLabel.setText(dataNode.get("ZOrder"));
	}
	public void addZOrderDisplay()
	{
		zOrderLabel = new Label("Z");
		addAnnotation(zOrderLabel, getController().zOrderVisibleProperty(),Pos.TOP_RIGHT, getWidth()/4, -getHeight()/4-10);
	}	
	public void addLockDisplay()
	{
		lockLabel = new Label("Z");
		addAnnotation(lockLabel, getController().lockVisibleProperty(),Pos.BOTTOM_RIGHT, 0, -10);
	}	
	
	public void addAnnotation(Label label, BooleanProperty visibility, Pos align, double offsetX, double offsetY)
	{
		label.setPrefWidth(100);
		label.setTextFill(Color.BROWN);
		label.setPrefHeight(20);
		label.setMouseTransparent(true);
		label.setBackground(Backgrounds.transparent());   
		label.setFont(new Font(10));
		label.setAlignment(align);
		label.setTranslateX(offsetX);
		label.setTranslateY(offsetY);
		label.visibleProperty().bind(visibility);
		getChildren().add(label);
	}
	
	// **-------------------------------------------------------------------------------
	public void setText(String s)		{ 	text.setText(s);	}
	public String getText()				
	{ 	
		String s = text.getText();	
		if (StringUtil.isEmpty(s))
		{
//			if (isAnchor()) return "Anchor (" + getGraphId() + ")";
			return getId();
		}
		return s;
	}
	// **-------------------------------------------------------------------------------
	public boolean isSelected()
	{
		ObservableMap<Object, Object> properties = getProperties(); 
		BooleanProperty selectedProperty = (BooleanProperty) properties.get("selected"); 
		return  (selectedProperty != null && selectedProperty.getValue());
	}
	
	public String getGraphId()			{ 	return dataNode.getGraphId();	}
	private Controller getController()  { 	return pasteboard.getController();   	}
	public Label getTextField()			{ 	return text;	}
	public Shape getFigure()			{	return figure;	}
	public void setFigure(Shape s)		{	figure = s;	}
	public DataNode modelNode()			{	return dataNode;	}
	public String gensym(String s)		{	return dataNode.getModel().gensym(s);	}
	public AttributeMap getAttributes() {	return dataNode;	}
	// **-------------------------------------------------------------------------------
	public boolean canResize()	{	return resizable;	}
	public void handleResize(MouseEvent event)
	{
		if (resizable)
			super.handleResize(event.getSceneX(), event.getSceneY());
	}

	public void setResize(boolean enable)
	{
		setResizeEnabledNorth(enable);
		setResizeEnabledEast(enable);
		setResizeEnabledWest(enable);
		setResizeEnabledSouth(enable);
		resizable = enable;	
	}
	static InnerShadow effect = new InnerShadow();
	
	   protected void processMousePosition(final MouseEvent event) {

	       super.processMousePosition(event);
	        if (event.getEventType().equals(MouseEvent.MOUSE_ENTERED))
	        {
	        	boolean live = true;  //isSelected() || pasteboard.getDragLine() != null;
	        	setEffect(live ? effect : null);
	        	showPorts(isConnectable());
	        }
	        if (event.getEventType().equals(MouseEvent.MOUSE_MOVED))
	        {
	        	boolean live = true;  //= isSelected() || pasteboard.getDragLine() != null;
	        	setEffect(live ? effect : null);
	        	showPorts(live);
	        }
	        if (event.getEventType().equals(MouseEvent.MOUSE_EXITED))
	        {
	        	setEffect(isSelected() ? effect : null);
	        	showPorts(false);
	        }
	    }
	// **-------------------------------------------------------------------------------
	public boolean isMovable()	{	return movable;	}
	public void setMovable(boolean b)	{ movable = b;	}
	public boolean isEditable()	{	return editable;}
	public void setEditable(boolean b)	{ editable = b;	}
	public boolean isConnectable()	{	return connectable;}
	public void setConnectable(boolean b)	{ connectable = b;	}
	// **-------------------------------------------------------------------------------
	public void applyLocks(boolean mov, boolean resiz, boolean edit, boolean connect) {
		setMovable(mov);
		setResize(resiz);
		setEditable(edit);
		setConnectable(connect);
	}
	// **-------------------------------------------------------------------------------
	public Point2D boundsCenter()	{
		Bounds b = getBoundsInParent();
		double x = (b.getMinX() + b.getMaxX()) / 2;
		double y = (b.getMinY() + b.getMaxY()) / 2;
		return new Point2D(x, y);		
	}
	// **-------------------------------------------------------------------------------
	public void tagCorner(Color color, Pos position, String ref)	
	{
		double cornerX = 0;
		double dX = 1;
		double dY = 1;
		double cornerY = 0;
		double size = 8;
		if (position == Pos.BOTTOM_LEFT || position == Pos.BOTTOM_RIGHT) 	cornerY = getHeight();
		if (position == Pos.TOP_RIGHT  || position == Pos.BOTTOM_RIGHT)  	cornerX = getHeight();
		if (position == Pos.BOTTOM_LEFT || position == Pos.BOTTOM_RIGHT) 	dY = -1;
		if (position == Pos.TOP_RIGHT || position == Pos.BOTTOM_RIGHT) 		dX = -1;
		
		PathElement[] elements = new PathElement[4];
		elements[0] = new MoveTo(cornerX, cornerY);
		elements[1] = new LineTo(cornerX, cornerY + dY *  size);
		elements[2] = new LineTo(cornerX + dX *  size, cornerY);
		elements[3] = new ClosePath();
		Path tag = new Path(elements);
		tag.setStrokeWidth(0.5);
		tag.setFill(color);
		getChildren().add(tag);
		StackPane.setAlignment(tag, position);
		
        Tooltip tooltip = new Tooltip();
        tooltip.setOnShowing(v -> { tooltip.setText("BioPax Reference: " + ref);});
        Tooltip.install(this,  tooltip);
	
	}
	// **-------------------------------------------------------------------------------
	public String getShapeType(){ return modelNode().getShapeType(); 	}
	public boolean isLabel()	{ return "Label".equals(getShapeType());	}
	// **-------------------------------------------------------------------------------
	public boolean isGroup()	{ return false;	}			// TODO
	public List<VNode> ungroup()	
	{ 
		return FXCollections.emptyObservableList();	
	}			// TODO
	
//	private double PIN(double v, double min, double max) { return (v < min)  ? min : ((v > max) ?  max : v);	}

	@Override
	protected void handleMouseDragged(final MouseEvent event) {
		
		if (!isMovable()) return;
		
		boolean isaGroup = modelNode() instanceof DataNodeGroup;
		boolean isInGroup = (getParent() instanceof Group);
			
//		if (isInGroup && !isaGroup)
//		{
//			Group g = (Group) getParent();
//			return;
//		}
		super.handleMouseDragged(event);
        if (isSelected())
        {	
           	double dx = 0;
        	double dy = 0;
        	if (localLastMouseX > 0 && localLastMouseY > 0)
        	{
        		dx = localLastMouseX - event.getSceneX();
        		dy = localLastMouseY - event.getSceneY();
        	}
        	
        	selection.translate(dx,dy, this);
        	selection.extract();
        	localLastMouseX = event.getSceneX();
        	localLastMouseY = event.getSceneY();
        }
  
	}
	double localLastMouseX = -1; 
	double localLastMouseY = -1; 
 	// **-------------------------------------------------------------------------------
	private void createFigure(Point2D center) 
	{	
		String type = dataNode.getShapeType();
		if ("None".equals(type)) return;
		if (type == null) type = "Rectangle";			// TODO
		Tool tool = Tool.lookup(type);
		if (tool == null) return ;
		if (tool.isControl())
		{
			controlFactory.addNewNode(tool, dataNode);
			return;
		}
		else figure = ShapeFactory.makeNewShape(type, center, dataNode, this);
	}
 	// **-------------------------------------------------------------------------------

	private double getCenterX() {		return modelNode().getDouble("CenterX");	}
	private double getCenterY() {		return modelNode().getDouble("CenterY");	}

	public Point2D center() {
    	Bounds bounds = getBoundsInParent();
    	double x = (bounds.getMinX() + bounds.getWidth()  / 2);
    	double y = (bounds.getMinY() + bounds.getHeight() / 2);
    	return new Point2D(x,y);
	}

	public void setCenter(Point2D pt)	{		setCenter(pt.getX(), pt.getY());	}
	
	public void setCenter(double x, double y)
	{
		getAttributes().putDouble("CenterX", x);
		getAttributes().putDouble("CenterY", y);
		installPosition();
	}

	public Point2D getPortPosition(Pos pos) {
		Point2D center = center();
		double x = getAdjustmentX(pos, getWidth());
		double y = getAdjustmentY(pos, getHeight());
		return new Point2D(center.getX()+x, center.getY()+y);
	}


	public Point2D getRelativePosition(double relX, double relY) {
		Point2D center = center();
		double dx = relX * getWidth() / 2;
		double dy = relY * getHeight() / 2;
		return new Point2D(center.getX()+dx, center.getY()+dy);
	}

	public Point2D getRelativePosition(RelPosition rel) {
		return getRelativePosition(rel.x(), rel.y());
	}

	private double getAdjustmentX(Pos srcPosition, double nodeWidth) {
	
		String s = srcPosition.name();
		if (s.contains("LEFT")) 	return -nodeWidth / 2;
		if (s.contains("RIGHT")) 	return nodeWidth / 2;
		return 0;
}
	private double getAdjustmentY(Pos srcPosition, double nodeHeight) {
		
		String s = srcPosition.name();
		if (s.contains("TOP")) 		return -nodeHeight / 2;
		if (s.contains("BOTTOM")) 	return nodeHeight / 2;
		return 0;
}//		x += getAdjustmentX(srcPosition, nodeWidth);
//	y += getAdjustmentY(srcPosition, nodeHeight);

	
	public String getLayerName() 		{	return getAttributes().get("Layer");	}
	public void setLayerName(String s) 	{	getAttributes().put("Layer", s);	}
	public boolean isLayerLocked() 		{	LayerRecord rec = getLayer(); return rec == null || rec.getLock();	}

 	// **-------------------------------------------------------------------------------
	private void installPosition()
	{
		double x = getAttributes().getDouble("CenterX");
		double y = getAttributes().getDouble("CenterY");
		double w = getAttributes().getDouble("Width",20);
		double h = getAttributes().getDouble("Height",20);
		fill(x,y,w,h, getText(), getId());
	}
	
	public void extractPosition()
	{
		Bounds b = getBoundsInParent();
		double minX = b.getMinX();
		double minY = b.getMinY();
		double oldMinX = getAttributes().getDouble("X");
		double oldMinY = getAttributes().getDouble("Y");
		double offsetX = minX - oldMinX;
		double offsetY = minY - oldMinY;
		getAttributes().putDouble("X", minX);
		getAttributes().putDouble("Y", minY);
		getAttributes().putDouble("CenterX", (minX + b.getMaxX()) / 2);
		getAttributes().putDouble("CenterY", (minY + b.getMaxY()) / 2);
		getAttributes().putDouble("Width",b.getWidth());
		getAttributes().putDouble("Height",b.getHeight());
		getController().redrawEdgesToMe(this);
	}
	
	public void setWidth(double w)	{	super.setWidth(w);		resizeFigureToNode();	}
	public void setHeight(double h)	{	super.setHeight(h);		resizeFigureToNode();	}
	
 	// **-------------------------------------------------------------------------------
	private void resizeFigureToNode() {
		if (figure != null)
		{
			if (figure instanceof Rectangle)
			{
				Rectangle r = (Rectangle) figure;
				r.setWidth(getWidth());	
				r.setHeight(getHeight());
			}
			if (figure instanceof Circle)
			{
				Circle c = (Circle) figure;
				c.setCenterX(getCenterX()); 
				c.setCenterY(getCenterY());
				c.setRadius(Math.min(getHeight(),getWidth())/2);
			}
			if (figure instanceof Ellipse)
			{
				Ellipse e = (Ellipse) figure;
				e.setCenterX(getCenterX()); 
				e.setCenterY(getCenterY());
				e.setRadiusX(getWidth()/2);
				e.setRadiusY(getHeight()/2);
			}
			if (figure instanceof Path)
			{
				Path p = (Path) figure;
				double scale = Math.min(getWidth() / 400, getHeight() / 300);
				p.setScaleX(5 * scale);
				p.setScaleY(5 * scale);
			}
		}
	}
	// **-------------------------------------------------------------------------------
	public String toString()
	{
		Shape shape = getFigure();
		String shapeName = shape == null ? "n/a" : shape.getClass().getSimpleName();
		double x = getLayoutX();
		double y = getLayoutY();
		return String.format("(%d) %s %s %s [@%3.1f, %3.1f; %3.1f x %3.1f ] @%3.1f, %3.1f", getChildren().size(), shapeName, getId(), getText(), getCenterX(), getCenterY(), getWidth(), getHeight(), x, y);
	}
	// **-------------------------------------------------------------------------------
	public void addContent(Node content)
	{
		getChildren().add(content);
		setAlignment(content, Pos.BOTTOM_CENTER);
	    setMargin(content, new Insets(25,0,0,0));
	}

	// **-------------------------------------------------------------------------------
	public void addText(String textLabel)
	{
//		boolean showAlignmentSpots = false;
//		if (showAlignmentSpots )		// DEBUG-------------------
//		{
//			for (int i=0; i< 9 ; i++)
//			{
//				Pos pos = Pos.values()[i];
//				String posName = pos.name();
//				final Label label = new Label(posName);
//				label.setFont(new Font(12));
//				label.setMouseTransparent(true);
//				setAlignment(label, pos);
//				label.setTextAlignment(TextAlignment.CENTER);
//				getChildren().add(label);
//			}
//		}								// End DEBUG-------------------
		Pos pos = Pos.CENTER;
		text = new Label(textLabel);
		text.setMinWidth(180);
		text.setMinHeight(32);
		text.setPrefWidth(220);
		text.setPrefHeight(40);
		text.setMouseTransparent(true);
		text.setBackground(Backgrounds.transparent());   
		text.setFont(new Font(18));
		setAlignment(text, pos);
		text.setAlignment(pos);
		getChildren().add(text);
//		setBorder(Borders.greenBorder);
	}

public void addBadge(String letter)
{
	StackPane badge = new StackPane();
	final Circle circle = new Circle(6);
	badge.setTranslateX(getWidth() / 2.);
	badge.setTranslateY(getHeight() / -2.);
	circle.setFill(Color.WHITE);
	circle.setStroke(Color.BLACK);
	Label label = new Label(letter);
	label.setTextFill(Color.VIOLET);
	badge.getChildren().add(circle);
	badge.getChildren().add(label);
	getChildren().add(badge);
	
}
  	List<Shape> ports = new ArrayList<Shape>();

  	public void addPorts()
	{
		for (int i=0; i< 9 ; i++)
		{
			if (i == 4) continue;			//skip center
			Pos pos = Pos.values()[i];
			Shape port = null;
			if (i % 2 == 0) 
				port = new Rectangle(5,5);
			else  port = new Circle(3);
			port.setFill(portFillColor(EState.STANDBY));
			port.setStroke(Color.MEDIUMAQUAMARINE);
			addPortHandlers(port);
			setAlignment(port, pos);
			getChildren().add(port);
			ports.add(port);
			port.setVisible(false);
			port.setId(""+(i+1));
		}

//		for (int i=0; i< 2 ; i++)
//		{
//			if (i == 4) continue;			//skip center
////			if (i % 2 == 0) continue;
//			Pos pos = (i==0) ? Pos.CENTER_LEFT : Pos.CENTER_RIGHT;
//			
//			final Circle port = new Circle(4.8);
//			port.setFill(portFillColor(EState.STANDBY));
//			port.setStroke(Color.MEDIUMAQUAMARINE);
//			addPortHandlers(port);
//			setAlignment(port, pos);
//			getChildren().add(port);
//			ports.add(port);
//			port.setVisible(false);
//			port.setId(""+i);
//		}

	}
	
	public void showPorts(boolean vis)
	{
		if (getWidth() < 40 || getHeight() < 18)
			vis = false;
		for (Shape p : ports)
			p.setVisible(vis);
	}
	
	enum EState { OFF, ACTIVE, STANDBY, FILLED, MISMATCH }; 
	
	public Color portFillColor(EState state)
	{
		if (state == EState.OFF)		return  Color.WHITE;
		if (state == EState.STANDBY)	return Color.YELLOW;
		if (state == EState.FILLED)		return Color.BLACK;
		if (state == EState.MISMATCH)	return  Color.RED;
		return Color.GREEN;
	}
	
	private void addPortHandlers(Shape port)
	{		
		port.addEventHandler(MouseEvent.MOUSE_MOVED, e ->  { 	if (pasteboard.getDragLine() != null) return; 	port.setFill(portFillColor( EState.ACTIVE)); });
		port.addEventHandler(MouseEvent.MOUSE_EXITED, e -> {	port.setFill(portFillColor( EState.OFF)); } );
		port.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {  	port.setFill(portFillColor( EState.STANDBY));  	 finishDragLine(port, this); });
		port.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> { 	pasteboard.startPortDrag(e, this, port);} );
	}

	private void finishDragLine(Node port, VNode target)
	{
		String id = port.getId();
		RelPosition relPos = RelPosition.idToRelPosition(id);
		finishDragLine(target, relPos);
	}
	private void finishDragLine( VNode target, RelPosition relPos)
	{
		pasteboard.connectTo(target, relPos);
	}
	// **-------------------------------------------------------------------------------
	  /**
     * Handles mouse events.
     *
     * @param event {@link MouseEvent}
     */
//	protected void handleMouseEntered(MouseEvent event) 
//	{
//		showPorts(pasteboard.getDragLine() != null && isConnectable());
//		System.out.println("enter");
//	}
//
//	protected void handleMouseExited(MouseEvent event) 
//	{
//		System.out.println("exit");
//		showPorts(false);
//	}
//
	protected void handleMouseReleased(final MouseEvent event) {
		if (pasteboard.getDragLine() != null && isConnectable()) {
			VNode starter = pasteboard.getDragSource();
			if (starter != this)
			{	
				getController().getUndoStack().push(ActionType.AddEdge);
				getController().addInteraction(starter, this);
				pasteboard.removeDragLine();
		
			}
			event.consume();
			return;
		}

		getController().getUndoStack().push(ActionType.Move);
		pasteboard.getSelectionMgr().extract();
		super.handleMouseReleased(event);
    }

	double prevMouseX, prevMouseY;
	protected void handleMousePressed(final MouseEvent event) {


		if (event.getClickCount() > 1)
		   {
			   getInfo();
			   event.consume();
			   return;
		   }
		   if (event.isPopupTrigger())
		   {
			   doContextMenu(event);
			   event.consume();
			   return;
		   }
		   if (pasteboard.getDragLine() != null)
		   {
			   double halfWidth = getWidth() / 2.0;
			   double halfHeight = getHeight() / 2.0;
			   double x = event.getSceneX();
			   double y = event.getSceneY();
			   double centerX = getLayoutX() + halfWidth ;
			   double centerY = getLayoutY() + halfHeight;
			   Point2D local = pasteboard.sceneToLocal(new Point2D(x,y));
			   
			   
				double relX = (local.getX() - centerX)  / halfWidth;
				double relY = (local.getY() - centerY)  / halfHeight;
				RelPosition pos =  new RelPosition(relX,relY);
			   finishDragLine(this, pos);
				event.consume();
				return;
		   }

		   Tool curTool = pasteboard.getTool();
		   if (curTool != null && !curTool.isArrow()) return;
		   
		   if (event.isAltDown())
			   selection.duplicateSelection();
		   
		   prevMouseX = event.getSceneX();
		   prevMouseY = event.getSceneY();
			boolean wasSelected = selection.isSelected(this);
			if (event.isControlDown() || event.isShiftDown())			//TODO -- configurable?
				selection.select(this, !wasSelected);
			else if (!wasSelected)
				selection.selectX(this);
			event.consume();
	}
	// **------------------------------------------------------------------------------
	private void getInfo() {		
		String name = getText();
		String biopaxRef = getAttributes().get("BiopaxRef");
		if (biopaxRef != null)
		{
			BiopaxRecord rec = modelNode().getModel().getReference(biopaxRef);
			if (rec != null)
			{
				ReferenceController.showPMIDInfo(rec.getId());
				return;
			}
		}
//		Gene gene =  modelNode().getModel().findGene(name);
//		if (gene != null)
//		{	
//			gene.getInfo();
//			return;
//		}
		String s = modelNode().getInfoStr();
		   if (StringUtil.hasText(s))
		   {  
			   Alert a = new Alert(AlertType.INFORMATION, s);
			   a.setHeaderText("Other Component Information");
			   a.getDialogPane().setMinWidth(600);
			   a.setResizable(true);
			   a.showAndWait();
		   }		
		System.out.println(s);		 	// get as HTML and Open in Browser
	}
	// **------------------------------------------------------------------------------
	public void rememberPositionEtc() 		{		rememberPositionEtc(0);		}
	public void rememberPositionEtc(int offset) 			
	{		
		AttributeMap attributes = getAttributes();
		Bounds bounds = getBoundsInParent();
		attributes.putDouble("X",  bounds.getMinX() + offset);	
		attributes.putDouble("Y",  bounds.getMinY() + offset);	
		attributes.putDouble("Width",  bounds.getWidth());	
		attributes.putDouble("Height",   bounds.getHeight());
		attributes.putDouble("ZOrder",   pasteboard.getChildren().indexOf(this));
		Shape shape = getShape();
		if (shape == null) shape = getFigure();
		if (shape != null)
		{
			attributes.putColor("Fill",   (Color) shape.getFill());	
			attributes.putColor("Stroke",   (Color) shape.getStroke());	
			attributes.putDouble("LineThickness",   shape.getStrokeWidth());	
		}
	}
	public void readGeometry(AttributeMap attrMap, Node content)
	{
		String id = attrMap.get("GraphId");
		double x = attrMap.getDouble("X");
		double y = attrMap.getDouble("Y");
		double centerx = attrMap.getDouble("CenterX");
		double centery = attrMap.getDouble("CenterY");
		double w = attrMap.getDouble("Width", 100) ;
		String type = attrMap.get("ShapeType");
		double h = attrMap.getDouble("Height", 150);
		if ("GeneProduct".equals(type)) h = 50;
		
		String title = attrMap.get("TextLabel");
		if (Double.isNaN(x)) {	x = centerx - w / 2;	attrMap.putDouble("X", x);  }
		if (Double.isNaN(y)) { y = centery - h / 2;		attrMap.putDouble("Y", y);  }
		fill( x,  y,  w,  h,  title,  id);
	}

	public void fill(double x, double y, double w, double h, String title, String id)
	{
		setId(id);
		if (Double.isNaN(w)) w = 20;
		if (Double.isNaN(h)) h = 20;
	
		setLayoutX(x);
		setLayoutY(y);
		setWidth(w);	prefWidth(w);	setMinWidth(w);  
		setHeight(h);	prefHeight(h);	setMinHeight(h); 
		System.out.println(String.format("[@%3.1f, %3.1f; %3.1f x %3.1f ]", x, y, w, h));
		AttributeMap attr = modelNode();
		StringBuilder bldr = new StringBuilder();

//		double fontsize = attr.getDouble("FontSize");
		FontWeight wt =  FontWeight.NORMAL;
		FontPosture posture =  FontPosture.REGULAR;
		double size = 12;
		
		String fontweight = attr.get("FontWeight");		// Bold or nothing
		if ("Bold".equals(fontweight))		
			wt = FontWeight.BOLD;
			//bldr.append("-fx-font-weight: bolder; ");
		String style = attr.get("FontStyle");			// Italic or nothing
		if ("Italic".equals(style))			
			posture =  FontPosture.ITALIC;		//bldr.append("-fx-font-style: italic; ");
		String align = attr.get("Valign");
		if ("Middle".equals(align))
			bldr.append("-fx-row-valignment:center;");
		String fontsize = attr.get("FontSize");
			if (fontsize != null) 
				size = StringUtil.toDouble(fontsize);

		
		
		String colorTag = dataNode.get("Color");
		if (colorTag != null)
			bldr.append("-fx-text-fill:#" + colorTag + "; ");
		bldr.append(makeStyleString("FontSize"));
		String str = bldr.toString();
		if (getTextField() != null) getTextField().setStyle(str);
//		System.out.println(str);
		setStyle(str);
		if (text != null) text.setFont(Font.font("times", wt, posture, size)); 
		bldr = new StringBuilder();		// start over with Shape
		colorTag = dataNode.get("Color");
		if (colorTag != null)
			bldr.append("-fx-border-color:#" + colorTag + "; ");
		bldr.append(makeStyleString("FillColor"));
//		bldr.append(makeStyleString("Opacity"));
		bldr.append(makeStyleString("LineThickness"));
		str = bldr.toString();
		if (getFigure()!= null)
		{	
			getFigure().setStyle(str);	    
		
		}
	}
	
	private String makeStyleString(String gpmlTag) {
		String val = dataNode.get(gpmlTag);
		if (val == null) return "";
		String fxml = GPML.asFxml(gpmlTag);
		if (fxml == null) return "";
		if ("FillColor".equals(gpmlTag) || "Color".equals(gpmlTag))
			val = "#" + val;
		return fxml + ":" + val + "; ";
	}
	
	//--------------------------------------------------------
	public String asGPML()
	{
		ObservableMap<Object, Object> pro = getProperties();
		Object o = pro.get("TextLabel");
		String textLabel = o == null ? "" : o.toString();
		o = pro.get("Type");
		String type = o == null ? "" : o.toString();
		String header = "<DataNode TextLabel=\"%s\" GraphId=\"%s\" Type=\"%s\" >\n";
		StringBuilder buffer = new StringBuilder(String.format(header, textLabel, getGraphId(), type));

		String[] tokens = toString().split(" ");
		String shape = tokens.length > 1 ? tokens[1] : "Error";
		double w = getLayoutBounds().getWidth();
		double h = getLayoutBounds().getHeight();
		double cx = getLayoutX() + w / 2;
		double cy = getLayoutY() + h / 2;
		if (getShape() instanceof Rectangle)
		{
			Rectangle sh = (Rectangle) getShape();
			cx = sh.getX() + w / 2;
			cy = sh.getY() + h / 2;
			if (sh.getArcWidth() > 0)
				shape = "RoundedRectangle";
		}
		String graphics1 = String.format("  <Graphics CenterX=\"%.2f\" CenterY=\"%.2f\" Width=\"%.2f\" Height=\"%.2f\" ZOrder=\"32768\" ", cx, cy, w, h);
		String graphics2 = String.format("FontWeight=\"%s\" FontSize=\"%d\" Valign=\"%s\" ShapeType=\"%s\"", "Bold", 12, "Middle", shape);
		buffer.append(graphics1).append(graphics2).append(" />\n") ;
		buffer.append("  <Xref Database=\"\" ").append("ID=\"\"").append("/>\n") ;
		buffer.append("</DataNode>\n");
		return buffer.toString();
	}
	// **-------------------------------------------------------------------------------
	protected void doContextMenu(MouseEvent event) {
		ContextMenu menu = new ContextMenu();
		menu.getItems().addAll(getMenuItems(event));
		menu.show(pasteboard, event.getScreenX(), event.getScreenY());		
	}

	public List<MenuItem> getMenuItems(MouseEvent event) {
		ObservableList<MenuItem> list = FXCollections.observableArrayList();
		//System.out.println("ContextMenu");
		int nKids = getChildren().size();
		Controller controller = getController();
		if (nKids > 0)
		{
			Node content = getChildren().get(0);
			if (content instanceof TableView)
			{
				MenuItem scatter = makeItem("Make Scatter Chart", e -> {});
				MenuItem timeseries = makeItem("Make Time Series", e -> {});
				SeparatorMenuItem sep = new SeparatorMenuItem();
				list.addAll(new MenuItem[] {scatter, timeseries, sep});
			}
		}
		MenuItem toFront = makeItem("Bring To Front", e -> {   	controller.toFront();   });
		MenuItem toBack = 	makeItem("Send To Back", e -> {   	controller.toBack();   });
		Menu toLayer = 	makeLayerMenu();
		MenuItem dup = 		makeItem("Duplicate", a -> 	{		controller.duplicateSelection();	});
		MenuItem del = 		makeItem("Delete", a -> 	{		controller.deleteSelection();	});
		list.addAll(toFront, toBack, toLayer, dup, del);   

//		Selection selection = pasteboard.getSelectionMgr();
		if (selection.isGroupable())	list.add(makeItem("Group", e ->  	{	controller.group();    }));   
		if (isUngroupable())			list.add(makeItem("Ungroup", e -> 	{ 	controller.ungroup();  }));   
		return list;
	}
	
	private boolean isUngroupable() {		return false;	}

	private MenuItem makeItem(String name, EventHandler<ActionEvent> foo) {
		MenuItem item = new MenuItem(name);
		item.setOnAction(foo);
		return item;
	}

	private Menu makeLayerMenu() {
		Menu menu = new Menu("Move To Layer");
		for (LayerRecord layer : getController().getLayers())
		{
			MenuItem item = new MenuItem(layer.getName());
			menu.getItems().add(item);
			item.setOnAction(e -> 	
			{ 	
				getController().moveSelectionToLayer(layer.getName());  
			});
		}
		return menu;
	}
	
	// **-------------------------------------------------------------------------------
	public void setBounds(BoundingBox bounds) {
		setLayoutX(bounds.getMinX());
		setLayoutY(bounds.getMinY());
		setWidth(bounds.getWidth());
		setHeight(bounds.getHeight());
	}
	// **-------------------------------------------------------------------------------
	public LayerRecord getLayer()
	{
		String layername = getAttributes().get("Layer");
		if (layername == null) return null;
		return getController().getLayerRecord(layername);
	}
	
	// **-------------------------------------------------------------------------------
	public void setLayer(String layername) {
		LayerRecord myLayer = getLayer();
		if (myLayer != null)
			myLayer.getLayer().remove(this);
		getAttributes().put("Layer", layername);
		LayerRecord active = getController().getLayerRecord(layername);
		active.getLayer().add(this);
		setVisible(active.getVis());
		setMouseTransparent(active.getLock());
	}

	// **-------------------------------------------------------------------------------
	@Override
	public int compareTo(VNode other ) {
		double a = getAttributes().getDouble("ZOrder",0);
		double b = ((VNode)other).getAttributes().getDouble("ZOrder",0);
		return a == b ? 0 : (a > b ? 1 : -1);
	}
	public void addState(DataNodeState statenode) {
		String label = statenode.get("TextLabel");		// getName includes "State:"
		addBadge(label);
	}
}
