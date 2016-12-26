package diagrams.pViz.view;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import diagrams.grapheditor.utils.ResizableBox;
import diagrams.pViz.app.Controller;
import diagrams.pViz.app.Selection;
import diagrams.pViz.app.Tool;
import diagrams.pViz.gpml.Anchor;
import diagrams.pViz.gpml.GPML;
import diagrams.pViz.model.EdgeLine;
import diagrams.pViz.model.MNode;
import gui.Backgrounds;
import javafx.beans.binding.Bindings;
import javafx.beans.property.StringProperty;
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
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.scene.web.WebView;
import model.AttributeMap;
import model.bio.Gene;
import model.dao.CSVTableData;
import util.FileUtil;
import util.MacUtil;
// a VNode can be a shape or a control or a container
import util.StringUtil;
// 
/*
 * VNode: the view node 
 * Created by the constructor of "model" the corresponding MNode
 * It inherits from ResizableBox > DragbleBox > StackPane > Pane > Region etc.
 * 
 */

public class VNode extends ResizableBox {		//StackPane

	private MNode model;
	private Shape figure;
	private TextField text;
	private String title;
	
	private Pasteboard pasteboard = null;
	private Selection selection = null;
	
	public VNode(MNode modelNode, Pasteboard p)
	{
		super();
		assert(modelNode != null && p != null);
		model = modelNode;
		pasteboard = p;
		selection = pasteboard.getSelectionMgr();
		AttributeMap attributes = modelNode.getAttributeMap();
		
		createFigure(attributes.getPosition());
		setId(modelNode.getId());
		title = attributes.get("TextLabel");
		if (title == null) title = "";
		addText(title);
		System.out.println(title);
		addPorts();
//		setBorder(Borders.redBorder);
//        addEventHandler(MouseEvent.DRAG_DETECTED, this::handleMouseDetected);
        readGeometry(attributes, this);
      }
	
	public void setText(String s)		{ 	text.setText(s);	}
	public String getText()				
	{ 	
		String s = text.getText();	
		if (StringUtil.isEmpty(s))
		{
			if (isAnchor()) return "Anchor (" + getModel().getId() + ")";
			return getId();
		}
		return s;
	}
	public TextField getTextField()		{ 	return text;	}
	public Shape getFigure()			{	return figure;	}
	public void setFigure(Shape s)		{	figure = s;	}
	public MNode getModel()				{	return model;	}
	private String gensym(String s)		{	return model.getModel().gensym(s);	}
	public AttributeMap getAttributes() {	return model.getAttributeMap();	}
//	public void setAttributes(AttributeMap attrs){	model.getAttributeMap().addAll(attrs);	}

	public Point2D boundsCenter()	{
		Bounds b = getBoundsInParent();
		double x = (b.getMinX() + b.getMaxX()) / 2;
		double y = (b.getMinY() + b.getMaxY()) / 2;
		return new Point2D(x, y);		
	}
	public boolean isAnchor()	{ return getModel() instanceof Anchor;	}
	public boolean isLabel()	{ return "Label".equals(getModel().getAttributeMap().get("ShapeType"));	}
	public boolean isShape()	{ return "RoundedRectangle".equals(getModel().getAttributeMap().get("ShapeType"));	}// TODO
	public void disableResize()	{ setResizeBorderTolerance(0);	}
	public boolean isGroup()	{ return false;	}			// TODO
	public List<VNode> ungroup()	{ return FXCollections.emptyObservableList();	}			// TODO
	private double PIN(double v, double min, double max) { return (v < min)  ? min : ((v > max) ?  max : v);	}

	protected void handleMouseDragged(final MouseEvent event) {
        if (isAnchor()) 
 	   {
            Anchor anchor = (Anchor) getModel();
            EdgeLine edgeLine = anchor.getEdge().getEdgeLine();
            double pX = 100; // TODO  HACK -- assumes palette is open
            double pY = 20; // pasteboard.getParent().getLayoutY();
            double evX = event.getSceneX() - pX;
            double evY = event.getSceneY() - pY;
//            evX = getCenterX();
//            evY = getCenterY();
            double pos = edgeLine.getClosestPosition(evX, evY);
            pos = PIN(pos, 0.1, 0.8);
            anchor.setPosition(pos);
            Point2D pt = edgeLine.getPointAlongLine(pos);
            setCenter(pt);
            event.consume();
            return;
//        	   System.out.println("Anchor handleMouseDragged: X at " + (int) event.getX() );
	   }
        else super.handleMouseDragged(event);
    }
 	// **-------------------------------------------------------------------------------
	private void createFigure(Point2D center) 
	{	
		String type = model.getShapeType();
		if ("None".equals(type)) return;
		if (type == null) type = "Rectangle";			// TODO
		Tool tool = Tool.lookup(type);
		if (tool == null) return ;
		if (tool.isControl())
		{
			addNewNode(tool, model);
			return;
		}
		else figure = ShapeFactory.makeNewShape(type, model, this);
//		setLayoutX(getAttributes().getDouble("X"));
//		setLayoutY(getAttributes().getDouble("Y"));
		Insets insets = new Insets(2,2,2,2);  //getInsets();
        double hInsets = insets.getLeft() + insets.getRight();
        double vInsets = insets.getTop() + insets.getBottom();
        double w = getAttributes().getDouble("Width", 15) + hInsets;
        double h = getAttributes().getDouble("Height", 15 + vInsets);
        if (figure instanceof Circle)
        {
        	Circle c = (Circle) figure;
        	c.setRadius(Math.min(w, h)/ 2); 
        	c.setCenterX(center.getX());
        	c.setCenterY(center.getY());
        	if (isAnchor())
        	{
        		setStyle("-fx-border-color: transparent; -fx-border-width: 0; -fx-background-color: transparent; -fx-opacity: 1.0;");	    
        		setResizeBorderTolerance(-2);
        	}
        }
        else if (figure instanceof Rectangle)
        {
	        Rectangle r = (Rectangle) figure;
	        r.setWidth(w);
	        r.setHeight(h);
	        r.setLayoutX(center.getX() - w /2 ); 
	        r.setLayoutY(center.getY() - h /2 ); 
        }
        else if (figure instanceof Path)
        {
//			Path p = (Path) shape;
//			p.scaleXProperty().bind(widthProperty());

        }
	}
 	// **-------------------------------------------------------------------------------

	private double getCenterX() {
		return getModel().getAttributeMap().getDouble("CenterX");
	}

	private double getCenterY() {
		return getModel().getAttributeMap().getDouble("CenterY");
	}

	public Point2D center() {
    	Bounds bounds = getBoundsInParent();
    	double x = (bounds.getMinX() + bounds.getWidth()  / 2);
    	double y = (bounds.getMinY() + bounds.getHeight() / 2);
    	return new Point2D(x,y);
	}

	public void setCenter(Point2D pt)
	{
		setCenter(pt.getX(), pt.getY());
	}
	
	public void setCenter(double x, double y)
	{
		getAttributes().putDouble("CenterX", x);
		getAttributes().putDouble("CenterY", y);
		install();
	}
	
 	// **-------------------------------------------------------------------------------
	private void install()
	{
		double x = getAttributes().getDouble("CenterX");
		double y = getAttributes().getDouble("CenterY");
		double w = getAttributes().getDouble("Width");
		double h = getAttributes().getDouble("Height");
		fill(x,y,w,h, getTitle(), getId());
	}
	
	public String getTitle() { return "getTitle"; }
	public void setWidth(double w)	{		super.setWidth(w);		resizeFigureToNode();	}
	public void setHeight(double h)	{		super.setHeight(h);		resizeFigureToNode();	}
	
 	// **-------------------------------------------------------------------------------
	private void resizeFigureToNode() {
		if (figure != null)
		{
			if (figure instanceof Rectangle)
			{
				Rectangle r = (Rectangle) figure;
				r.setWidth(getWidth());	// - ShapeFactory.MARGIN2
				r.setHeight(getHeight());	// - ShapeFactory.MARGIN2
			}
			if (figure instanceof Circle)
			{
				Circle c = (Circle) figure;
				c.setCenterX(getCenterX());  //getModel().getAttributeMap().getDouble("CenterX"));
				c.setCenterY(getCenterY());
				if (isAnchor())
				{
					c.setRadius(2);
					setText("");
				}
				else c.setRadius(Math.min(getHeight(),getWidth())/2);
			}
			if (figure instanceof Path)
			{
				Path p = (Path) figure;
				double scale = Math.min(getWidth() / 400, getHeight() / 300);
				p.setScaleX(scale);
				p.setScaleY(scale);
			}
		}
	}
	// **-------------------------------------------------------------------------------
	private void addNewNode(Tool type, MNode model)
	{
		switch (type)
		{
			case Browser:	makeBrowser();		break;
			case Text:		makeTextArea();		break;
			case Table:		makeTableView();	break;
			case Image:		makeImageView();	break;	
			case SVGPath:	makeSVGPath();		break;
			default:							break;
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

	public void addText(String textLabel)
	{
		boolean showAlignmentSpots = false;
		if (showAlignmentSpots )		// DEBUG
		{
			for (int i=0; i< 9 ; i++)
			{
				Pos pos = Pos.values()[i];
				String posName = pos.name();
				final Label label = new Label(posName);
				label.setFont(new Font(12));
				label.setMouseTransparent(true);
				setAlignment(label, pos);
				label.setTextAlignment(TextAlignment.CENTER);
				getChildren().add(label);
			}
		}								//   End DEBUG
		Pos pos = Pos.CENTER;
		text = new TextField(textLabel);
		text.setEditable(false);
//		text.setVisible(true);
		text.setMinWidth(100);
		text.setMinHeight(32);
		text.setPrefWidth(100);
		text.setPrefHeight(32);
//		text.setOnMousePressed(e -> {  mousePressedOnText(e);});
//		text.setOnMouseDragged(e -> {  mouseDraggedOnText(e);});
//		text.setOnKeyPressed(e -> 	{  keyPressedOnText(e);});
		text.setMouseTransparent(true);
		text.setBackground(Backgrounds.transparent());
		text.setStyle("-fx-font-style: italic;");
   String style =  "-fx-font: 100px Tahoma; "
    		+ "-fx-fill: linear-gradient(from 0% 0% to 100% 200%, repeat, aqua 0%, red 50%);"
		   + " -fx-stroke: black; -fx-stroke-width: 1;";
	setStyle(style);
   
		text.setFont(new Font(25));
		setAlignment(text, pos);
		text.setAlignment(pos);
		getChildren().add(text);
//		setBorder(Borders.greenBorder);
	}

//	private void mousePressedOnText(MouseEvent e) {
////		System.out.println("mousePressedOnText");
//		if (e.getClickCount() > 1) 
//		{ 
//			text.setMouseTransparent(false);
//			text.setEditable(true);
//			text.requestFocus();
//			text.selectAll();
//		}	
//		else handleMousePressed(e);
//	}
//	private void mouseDraggedOnText(MouseEvent e) {
////		System.out.println("mousePressedOnText");
//		if (!text.isEditable())
//			handleMouseDragged(e);
//	}
//
//
//	private void keyPressedOnText(KeyEvent e) {
//		KeyCode key = e.getCode();
//		if (key == KeyCode.ENTER || key == KeyCode.CANCEL|| key == KeyCode.TAB)
//		{
//			getAttributes().put("TextLabel", text.getText());
//			text.setEditable(false);
//		}
//	}

  	List<Shape> ports = new ArrayList<Shape>();
	public void addPorts()
	{
		for (int i=0; i< 9 ; i++)
		{
			if (i == 4) continue;			//skip center
			Pos pos = Pos.values()[i];
			final Circle port = new Circle(6);
			port.setFill(Color.ANTIQUEWHITE);
			port.setStroke(Color.MEDIUMAQUAMARINE);
			addPortHandlers(port);
			setAlignment(port, pos);
			getChildren().add(port);
			ports.add(port);
			port.setVisible(false);
		}
	}
	public void showPorts(boolean vis)
	{
		if (getWidth() < 70 || getHeight() < 40)
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
		port.addEventHandler(MouseEvent.MOUSE_MOVED, e -> {	port.setFill(portFillColor( EState.ACTIVE)); });
		port.addEventHandler(MouseEvent.MOUSE_EXITED, e -> {	port.setFill(portFillColor( EState.OFF)); } );
		port.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {  	port.setFill(portFillColor( EState.STANDBY));  	} );
		port.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> 
		{  
			System.out.println("Port");
			if (pasteboard.getTool() == Tool.Line)
			{
				port.setFill(Color.AQUAMARINE); 
				pasteboard.startDragLine(e.getSceneX(), e.getSceneY());
				e.consume();
			}
			e.consume();
		} );
	
	}

	// **-------------------------------------------------------------------------------
	  /**
     * Handles mouse events.
     *
     * @param event {@link MouseEvent}
     */

	double prevMouseX, prevMouseY;
	protected void handleMousePressed(final MouseEvent event) {
//		   super.handleMousePressed(event);
//			System.out.println(String.format("lastMouse: %.2f,  %.2f", lastMouseX, lastMouseY));
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
		Gene gene =  getModel().getModel().findGene(name);
		if (gene != null)
		{	
			gene.getInfo();
			return;
		}
		String s = getModel().getInfoStr();
		   if (StringUtil.hasText(s))
		   {  
			   Alert a = new Alert(AlertType.INFORMATION, s);
			   a.setHeaderText("Other Component Information");
			   a.getDialogPane().setMinWidth(600);
			   a.setResizable(true);
			   a.showAndWait();
		   }		
		System.out.println(s);		
		// get as HTML and Open in Browser
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
		if (shape != null)
		{
			attributes.putColor("Fill",   (Color) shape.getFill());	
			attributes.putColor("Stroke",   (Color) shape.getStroke());	
			attributes.putDouble("LineThickness",   shape.getStrokeWidth());	
		}
	}
	public void readGeometry(AttributeMap attrMap, Node content)
	{
		String id = attrMap.getId();
		double x = attrMap.getDouble("X");
		double y = attrMap.getDouble("Y");
		double centerx = attrMap.getDouble("CenterX");
		double centery = attrMap.getDouble("CenterY");
		double w = attrMap.getDouble("Width", 200) ;
		double h = attrMap.getDouble("Height", 150);
		String title = attrMap.get("TextLabel");
		if (Double.isNaN(x)) {	x = centerx - w / 2;	attrMap.putDouble("X", x);  }
		if (Double.isNaN(y)) { y = centery - h / 2;		attrMap.putDouble("Y", y);  }
		fill( x,  y,  w,  h,  title,  id);
//		if (content != null)
//			getChildren().add(content);
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
		AttributeMap attr = getModel().getAttributeMap();
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

		
		
		String colorTag = getAttributeMap().get("Color");
		if (colorTag != null)
			bldr.append("-fx-text-fill:#" + colorTag + "; ");
		bldr.append(makeStyleString("FontSize"));
		String str = bldr.toString();
		if (getTextField() != null) getTextField().setStyle(str);
		System.out.println(str);
		setStyle(str);
		if (text != null) text.setFont(Font.font("times", wt, posture, size)); 
		bldr = new StringBuilder();		// start over with Shape
		colorTag = getAttributeMap().get("Color");
		if (colorTag != null)
			bldr.append("-fx-border-color:#" + colorTag + "; ");
		bldr.append(makeStyleString("FillColor"));
		bldr.append(makeStyleString("LineThickness"));
		str = bldr.toString();
		System.out.println(str);
//		setStyle(str);	    
		if (getFigure()!= null)
			getFigure().setStyle(str);	    
//		addTitleBar(title);
	}
	
	private String makeStyleString(String gpmlTag) {
		String val = getAttributeMap().get(gpmlTag);
		if (val == null) return "";
		String fxml = GPML.asFxml(gpmlTag);
		if (fxml == null) return "";
		if ("FillColor".equals(gpmlTag) || "Color".equals(gpmlTag))
			val = "#" + val;
		return fxml + ":" + val + "; ";
	}

	private AttributeMap getAttributeMap() {
		return getModel().getAttributeMap();
	}

	public HBox addTitleBar(String title)
	{		
		HBox titleBar = new HBox();
	    titleBar.setMaxHeight(25);
	    Label idLabel = new Label(getId());
	    idLabel.setMinWidth(50);
	    StackPane.setAlignment(titleBar, Pos.TOP_CENTER);
	    Label titleLabel = new Label(title);
		titleBar.getChildren().addAll(idLabel, titleLabel);
	    titleBar.setMouseTransparent(true);
	    return titleBar;
	}
	
	// **-------------------------------------------------------------------------------
	private void makeBrowser()
	{
		AttributeMap attrMap = model.getAttributeMap();
		attrMap.put("ShapeType","Browser");
		String url = attrMap.get("url");
		if (url == null) 
		{
			String filepath = attrMap.get("file");		// f.getAbsolutePath()
			url = MacUtil.urlFromPlist(filepath);
		}
		if (url == null) return ;
		WebView webView = new WebView();
		webView.setZoom(0.4);
		webView.getEngine().load(url);
		getChildren().add(webView);
		addTitleBar(url);
	}
	
	// **-------------------------------------------------------------------------------
	private void makeImageView()
	{
		AttributeMap attrMap = model.getAttributeMap();
		attrMap.put("ShapeType","Image");
		String filepath = attrMap.get("file");		// f.getAbsolutePath()
		if (filepath == null) return;
		Image img = new Image("file:" + filepath);
		if (img.isError())
			System.out.println("makeImageView error");
		ImageView imgView = new ImageView(img);
		if (attrMap.getId() == null) 
			attrMap.put("GraphId", gensym("I"));
		
		imgView.prefWidth(200); 	imgView.prefHeight(200);
		imgView.setFitWidth(200); 	imgView.setFitHeight(200);
		attrMap.put("name", filepath);
//		Label imgView = new Label("TODO FIXME");
		
	    imgView.setMouseTransparent(true);
	    imgView.fitWidthProperty().bind(Bindings.subtract(widthProperty(), 20));
	    imgView.fitHeightProperty().bind(Bindings.subtract(heightProperty(), 40));
	    imgView.setTranslateY(-10);
		readGeometry(attrMap, imgView);
		getChildren().add(new VBox(addTitleBar(filepath), imgView));
	}
	
	// **-------------------------------------------------------------------------------
	private void makeSVGPath() {
		AttributeMap attrMap = model.getAttributeMap();
		attrMap.put("ShapeType","SVGPath");

		String path = attrMap.get("file");
		if (path != null)
		{
			String s = FileUtil.readFileIntoString(path);
			if (s != null)
			{
				SVGPath svg = new SVGPath();
				int idx1 = s.indexOf("<g>");
				int idx2 = s.indexOf("</g>") + 4;
				if (idx1 >0 && idx2 > idx1)
					s = s.substring(idx1, idx2);
				svg.setContent(s);
				readGeometry(attrMap, svg);
				getChildren().add(new VBox(addTitleBar(path), svg));
			}
		}
	}
	// **-------------------------------------------------------------------------------
	private void makeTableView()
	{
		AttributeMap attrMap = model.getAttributeMap();
		attrMap.put("ShapeType","Table");
		TableView<ObservableList<StringProperty>> table = new TableView<ObservableList<StringProperty>>();
		if (attrMap.getId() == null)
			attrMap.put("GraphId", gensym("T"));
		String filename = attrMap.get("file");
		CSVTableData data = FileUtil.openCSVfile(filename, table);		// TODO THIS CURRENTLY ASSUMES ALL INTS!!
		attrMap.put("name", filename);
		if (data == null) return;
		readGeometry(attrMap, table);
	    getChildren().addAll(new VBox(addTitleBar(filename), table));
	}
	// **-------------------------------------------------------------------------------
	public void makeTextArea()
	{
		AttributeMap attrMap = model.getAttributeMap();
		attrMap.put("ShapeType","Text");
		String text = attrMap.get("text");
		if (text == null)
		{
			String name = attrMap.get("file");
			StringBuilder buffer = new StringBuilder();
			attrMap.put("name", name);
			FileUtil.readFile(new File(name), buffer);
			text = buffer.toString();
			attrMap.put("text", text);
		}
		TextArea textArea = new TextArea(text);
	    textArea.setPrefColumnCount(60);
	    textArea.setPrefRowCount(20);
	    readGeometry(attrMap, textArea);
		getChildren().add(textArea);
	}
	//--------------------------------------------------------
	public String asGPML()
	{
//		if (node instanceof Edge) 
//			return edgeToGPML((Edge) node);
		ObservableMap<Object, Object> pro = getProperties();
		Object o = pro.get("TextLabel");
		String textLabel = o == null ? "" : o.toString();
		o = pro.get("Type");
		String type = o == null ? "" : o.toString();
		String header = "<DataNode TextLabel=\"%s\" GraphId=\"%s\" Type=\"%s\" >\n";
		StringBuilder buffer = new StringBuilder(String.format(header, textLabel, getId(), type));

		String[] tokens = toString().split(" ");
		String shape = tokens.length > 1 ? tokens[1] : "Error";
//		basic = StringUtil.chopLast(basic);		// chop off "]"
//		int idx = basic.indexOf("[");
//		if (idx <= 0) return "";
			
//		String shape = basic.substring(0, idx);
//		basic = basic.replaceAll(",", "");		// strip commas
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
		Controller controller = pasteboard.getController();
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
		MenuItem dup = 		makeItem("Duplicate", a -> 	{		controller.duplicateSelection();	});
		MenuItem del = 		makeItem("Delete", a -> 	{		controller.deleteSelection();	});
		list.addAll(toFront, toBack, dup, del);   

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

	public void setBounds(BoundingBox bounds) {
		setLayoutX(bounds.getMinX());
		setLayoutY(bounds.getMinY());
		setWidth(bounds.getWidth());
		setHeight(bounds.getHeight());
	}
}
