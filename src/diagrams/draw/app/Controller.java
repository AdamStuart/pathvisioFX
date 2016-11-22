package diagrams.draw.app;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import animation.BorderPaneAnimator;
import database.forms.EntrezQuery;
import diagrams.draw.app.Action.ActionType;
import diagrams.draw.gpml.BiopaxRef;
import diagrams.draw.gpml.GPML;
import diagrams.draw.gpml.Gene;
import diagrams.draw.model.Edge;
import diagrams.draw.model.EdgeFactory;
import diagrams.draw.model.MNode;
import diagrams.draw.model.Model;
import diagrams.draw.model.NodeFactory;
import diagrams.draw.view.Pasteboard;
import diagrams.draw.view.ShapeFactory;
import diagrams.draw.view.VNode;
import gui.Borders;
import icon.FontAwesomeIcons;
import icon.GlyphIcon;
import icon.GlyphIcons;
import icon.GlyphsDude;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Slider;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Callback;
import model.AttributeMap;
import model.AttributeValue;
import model.RandomAttributeValueData;
import util.FileUtil;
import util.NodeCenter;
import util.StringUtil;


public class Controller implements Initializable
{
    private static final DataFormat SERIALIZED_MIME_TYPE = new DataFormat("application/x-java-serialized-object");

	//@formatter:off
	private Model model;
	public Model getDrawModel()   		{ 		return model;  }
	public Model getModel() 			{		return model;	}	
	private Pasteboard pasteboard;
	public Pasteboard getPasteboard()   { 		return pasteboard;  }
	public NodeFactory getNodeFactory()	{		return pasteboard.getNodeFactory();	}
	public EdgeFactory getEdgeFactory()	{		return pasteboard.getEdgeFactory();	}
	private UndoStack undoStack;
	public UndoStack getUndoStack()		{		return undoStack;	}
	private Document doc;

	int verbose = 0;
	
	@FXML private Pane drawPane;
	@FXML private ScrollPane scrollPane;
	@FXML private ListView<Action> undoview = null;
	@FXML private TableView<Gene> geneListTable = null;
	@FXML private TableColumn<Gene, String> nameCol;
	@FXML private TableColumn<Gene, String> ensemblCol;
	
	@FXML private TableView<AttributeValue> attributeTable;
	@FXML private TableColumn<AttributeValue, String> attributeCol;
	@FXML private TableColumn<AttributeValue, String> valueCol;
//	@FXML private AnchorPane zoomAnchor;
	@FXML private BorderPane container;			// root of fxml
//	@FXML private BorderPane drawContainer;			// rulers and content
//	public ListView<Gene> getResourceListView()		{		return resourceListView;	}
	
	
	@FXML private TableView<BiopaxRef> refTable;
	@FXML private TableColumn<BiopaxRef, String> refCol;
	@FXML private TableColumn<BiopaxRef, String> idCol;
	@FXML private TableColumn<BiopaxRef, String> dbCol;
	@FXML private TableColumn<BiopaxRef, String> authorCol;
	@FXML private TableColumn<BiopaxRef, String> titleCol;
	@FXML private TableColumn<BiopaxRef, String> sourceCol;
	@FXML private TableColumn<BiopaxRef, String> yearCol;
	@FXML private Button tableOptions;

	@FXML private Button gene;

	@FXML private Button bottomSideBarButton;
	@FXML private Button leftSideBarButton;
	@FXML private Button rightSideBarButton;
//	@FXML private Button toggleRulerButton;
	@FXML private Button toggleGridButton;
	
	@FXML private void setArrow()		{ pasteboard.setTool(Tool.Arrow);	}
	@FXML private void setRectangle()	{ pasteboard.setTool(Tool.Rectangle);}	// TODO capture double click for stickiness
	@FXML private void setOval()		{ pasteboard.setTool(Tool.Circle);		}
	@FXML private void setPolygon()		{ pasteboard.setTool(Tool.Polygon);	}
	@FXML private void setPolyline()	{ pasteboard.setTool(Tool.Polyline);	}
	@FXML private void setLine()		{ pasteboard.setTool(Tool.Line);	}
	@FXML private void setShape1()		{ pasteboard.setTool(Tool.Shape1);	}
	
	@FXML private void setBrace()		{ pasteboard.setTool(Tool.Brace);	}

	@FXML private ToggleButton arrow;
	@FXML private ToggleButton rectangle;
	@FXML private ToggleButton circle;
	@FXML private ToggleButton polygon;
	@FXML private ToggleButton polyline;
	@FXML private ToggleButton line;
	@FXML private ToggleButton shape1;
//	@FXML private ToggleButton shape2;
	
	@FXML private ColorPicker fillColor;
	@FXML private ColorPicker lineColor;
	@FXML private Slider weight;
//	@FXML private Slider opacity;
	@FXML private Slider rotation;
//	@FXML private Label status1;
//	@FXML private Label status2;
//	@FXML private Label status3;
	@FXML private Label strokeLabel;
	@FXML private Label fillLabel;
	
//	@FXML private Slider translateX;			// debug fns for zoom view
//	@FXML private Slider translateY;			// debug fns for zoom view
//	@FXML private Slider scale;
	//-------------------------------------------------------------
	@FXML private MenuItem undo;
	@FXML private MenuItem redo;
	@FXML private MenuItem clearundo;
	
	@FXML private MenuItem clearColors;
	@FXML private MenuItem tofront;
	@FXML private MenuItem toback;
	@FXML private MenuItem group;
	@FXML private MenuItem ungroup;
	@FXML private MenuItem delete;
	@FXML private VBox inspector;
	@FXML private HBox bottomPadding;

	//------------------------------------------
	static Image dragImage;
	@FXML private void dragControl(MouseEvent e)
	{
		EventTarget targ  = e.getTarget();
		Button b = null;
		if (targ instanceof Text)
		{
			Text text = (Text) targ;
			Node parent = text.getParent();
			if (parent instanceof Button)
				b = (Button) parent;
		}
		else if (targ instanceof Button)
			b = (Button) targ;
			
		if (b != null)
		{
			System.out.println("drag " + b.getText());
	        Dragboard db = b.startDragAndDrop(TransferMode.COPY);
	        db.setDragView(b.snapshot(null, null), e.getX(), e.getY());
	        ClipboardContent cc = new ClipboardContent();
	        cc.putString(b.getText());
	        db.setContent(cc);
		}
	}

	
	@FXML private void undo()			{ 	undoStack.undo();	}
	@FXML private void redo()			{ 	undoStack.redo();		}
	
	// **-------------------------------------------------------------------------------
	@FXML private void doNew()			{ 	App.getInstance().doNew(null);			}
	@FXML private void open()			{ 	doc.open();	}			//doc.open();
	@FXML private void save()			{ 	doc.save();	}
	@FXML private void saveas()			{	doc.saveas();		}
	@FXML private void close()			
	{ 
		doc.close();	
		if (pasteboard != null)
		{
			Window w = pasteboard.getScene().getWindow();
			if (w instanceof Stage)
				((Stage) w).close();
		}
	}
	@FXML private void print()			{ 	doc.print();			}
	@FXML private void quit()			{ 	Platform.exit();			}
	@FXML private void clickDrawPane()			{ 	System.out.println("clicked");			}
	// **-------------------------------------------------------------------------------
	@FXML private  void cut()			{ 	undoStack.push(ActionType.Cut);		getSelectionManager().cut();	}
	@FXML private  void copy()			{ 	getSelectionManager().copy();		}	// not undoable
	@FXML private  void paste()			{ 	undoStack.push(ActionType.Paste);	doPaste();		}
	@FXML private  void clearUndo()		{	undoStack.clear(); 	}
	@FXML private void selectAll()		{ 	undoStack.push(ActionType.Select); getSelectionManager().selectAll(); 		}
	@FXML public void deleteSelection(){ 	undoStack.push(ActionType.Delete);	getSelectionManager().deleteSelection(); 	}
	@FXML public void duplicateSelection(){ undoStack.push(ActionType.Duplicate);	getSelectionManager().cloneSelection(); 	}
	@FXML public void clear()			{ undoStack.push(ActionType.Delete);	getSelectionManager().deleteAll(); 	}
	// **-------------------------------------------------------------------------------
	@FXML public  void group()			{ 	undoStack.push(ActionType.Group);	getSelectionManager().doGroup();  }
	@FXML public  void ungroup()		{ 	undoStack.push(ActionType.Ungroup);	getSelectionManager().ungroup(); }
	@FXML public  void toFront()		{	undoStack.push(ActionType.Reorder);	getSelectionManager().toFront(); 	}
	@FXML public  void toBack()			{	undoStack.push(ActionType.Reorder);	getSelectionManager().toBack();  pasteboard.getGrid().toBack();  	}

	//@formatter:on
	// **-------------------------------------------------------------------------------
	@FXML private MenuItem connect;			// TODO bind enableProperty to selection size > 2
	@FXML private void addEdges()		
	{ 	
		if (getSelection().size() >= 2)		
		{
			List<Edge> edges = getDrawModel().connectSelectedNodes();
			for (Edge e: edges)
				add(0, e);
		}
	}
	
	static String CSS_Gray2 = "-fx-border-width: 2; -fx-border-color: blue;";
	static String CSS_cellBackground(boolean undone) 	{		return "-fx-background-color: " + (undone ? "GREY; " : "BEIGE; ");	}
	static String ctrlStr = "fx:id=\"%s\" was not injected: check your FXML file '%s'.";
	static String missing(String s)	{		return String.format(ctrlStr, s, "AttributeValueFXML.fxml");	}
	
	// **-------------------------------------------------------------------------------
	private ToggleGroup paletteGroup;
	public ToggleGroup getToolGroup()			{ 	return paletteGroup;	}
	public Selection getSelectionManager() 		{ 	return pasteboard.getSelectionMgr();  }
	public ObservableList<VNode> getSelection() { 	return getSelectionManager().getAll();  }
	Stage stage;

	// **-------------------------------------------------------------------------------
	@Override public void initialize(URL location, ResourceBundle resources)
	{
//		final double SCALE_DELTA = 1.1;
		undoStack = new UndoStack(this, null);
		assert(drawPane != null);
		model = new Model(this);
		pasteboard = new Pasteboard(this);
		drawPane.getChildren().add(pasteboard);
		
		assert attributeCol != null : missing("attributeCol");
		assert valueCol != null : missing("valueCol");
		assert attributeTable != null : missing("attributeTable");
		doc = new Document(this);
		paletteGroup = new ToggleGroup();
		paletteGroup.getToggles().addAll(arrow, rectangle, circle, polygon, polyline, line);
		bindInspector();
		setupBiopaxTable();
		
		String cssURL = this.getClass().getResource("styles.css").toExternalForm();
		pasteboard.getStylesheets().add(cssURL);
		stage = App.getInstance().getStage();
//		drawContainer.setBorder(Borders.etchedBorder);
		scrollPane.setOnScroll(ev -> {
			ev.consume();
	        if (ev.getDeltaY() == 0)   return;	
//	        double scaleFactor = (ev.getDeltaY() > 0) ? SCALE_DELTA : 1 / SCALE_DELTA;
//	        if (scale.valueProperty().get() == 1)
//	        {
//	        	scale.valueProperty().set(0.9);
//	        }
//	        scale.valueProperty().set(scale.valueProperty().get() * scaleFactor);
//	        drawContainer.setScaleX(drawContainer.getScaleX() * scaleFactor);
//	        drawContainer.setScaleY(drawContainer.getScaleY() * scaleFactor);
		});
//		bottomDash.setBorder(Borders.dashedBorder );
		if (bottomPadding!= null)
			bottomPadding.setPadding(new Insets(3,3,4,4));

		if (inspector != null)
			inspector.setBorder(Borders.lineBorder);
		setupPalette();
		setupListviews();
		new BorderPaneAnimator(container, leftSideBarButton, Side.LEFT, false, 90);
		new BorderPaneAnimator(container, rightSideBarButton, Side.RIGHT, false, 300);
//		new BorderPaneAnimator(container, bottomSideBarButton, Side.BOTTOM, false, 300);
//		new BorderPaneRulers(drawContainer, toggleRulerButton);
		pasteboard.makeGrid(toggleGridButton, scrollPane);

		boolean startWithShapes = false;
		if (startWithShapes) test1();
	}
	static TableRow<BiopaxRef> thisRow = null;
	private void setupBiopaxTable() {
		
		TableColumn[] allCols = { refCol, idCol, dbCol, authorCol, titleCol, sourceCol, yearCol };

		refCol.setCellValueFactory(new PropertyValueFactory<BiopaxRef, String>("xrefid"));
		idCol.setCellValueFactory(new PropertyValueFactory<BiopaxRef, String>("id"));
		dbCol.setCellValueFactory(new PropertyValueFactory<BiopaxRef, String>("db"));
		authorCol.setCellValueFactory(new PropertyValueFactory<BiopaxRef, String>("authors"));
		titleCol.setCellValueFactory(new PropertyValueFactory<BiopaxRef, String>("title"));
		sourceCol.setCellValueFactory(new PropertyValueFactory<BiopaxRef, String>("source"));
		yearCol.setCellValueFactory(new PropertyValueFactory<BiopaxRef, String>("year"));

		refTable.setRowFactory(tv -> {
	        TableRow<BiopaxRef> row = new TableRow<>();

	        row.setOnDragDetected(event -> {
	            if (! row.isEmpty()) {
	                Integer index = row.getIndex();
	                Dragboard db = row.startDragAndDrop(TransferMode.MOVE);
	                db.setDragView(row.snapshot(null, null));
	                ClipboardContent cc = new ClipboardContent();
	                cc.put(SERIALIZED_MIME_TYPE, index);
	                db.setContent(cc);
	                event.consume();
	                thisRow = row;
	            }
	        });

	        row.setOnDragEntered(event -> {
	            Dragboard db = event.getDragboard();
	            if (db.hasContent(SERIALIZED_MIME_TYPE)) {
	                if (row.getIndex() != ((Integer)db.getContent(SERIALIZED_MIME_TYPE)).intValue()) {
	                    event.acceptTransferModes(TransferMode.MOVE);
	                    event.consume();
	                    thisRow = row;
//	                  if (thisRow != null) 
//	                 	   thisRow.setOpacity(0.3);
	                }
	            }
	        });

	        row.setOnDragExited(event -> {
	            if (event.getGestureSource() != thisRow &&
	                    event.getDragboard().hasString()) {
//	               if (thisRow != null) 
//	            	   thisRow.setOpacity(1);
	               thisRow = null;
	            }
	        });

	        row.setOnDragOver(event -> {
	            Dragboard db = event.getDragboard();
	            if (db.hasContent(SERIALIZED_MIME_TYPE)) {
	                if (row.getIndex() != ((Integer)db.getContent(SERIALIZED_MIME_TYPE)).intValue()) {
	                    event.acceptTransferModes(TransferMode.MOVE);
	                    event.consume();
	                }
	            }
	        });

	        row.setOnMouseClicked(event -> {
	        	if (event.getClickCount() == 2)
	            {
	                int idx = row.getIndex();
	        		getInfo(idx);
	              event.consume();
	            }
	        });

	        row.setOnDragDropped(event -> {
	            Dragboard db = event.getDragboard();
	            if (db.hasContent(SERIALIZED_MIME_TYPE)) {
	                int draggedIndex = (Integer) db.getContent(SERIALIZED_MIME_TYPE);
	                BiopaxRef draggedNode = refTable.getItems().remove(draggedIndex);

	                int  dropIndex = (row.isEmpty()) ? refTable.getItems().size() : row.getIndex();
	                refTable.getItems().add(dropIndex, draggedNode);

	                event.setDropCompleted(true);
	                refTable.getSelectionModel().select(dropIndex);
	                event.consume();
//	                if (thisRow != null) 
//	             	   thisRow.setOpacity(1);
	                thisRow = null;
	            }
	        });

	        return row ;
	    });

		
	}
	private void getInfo(String pmid)
	{
	   String text = EntrezQuery.getPubMedAbstract(pmid);			// TODO move to libFX
	   if (StringUtil.hasText(text))
	   {  
		   Alert a = new Alert(AlertType.INFORMATION, text);
		   a.setHeaderText("PubMed Abstract");
		   a.getDialogPane().setMinWidth(600);
		   a.setResizable(true);
		   a.showAndWait();
	   }
	}
	
	
	private void getInfo(int idx) {
		try 
		{
		   String rowName = "" + idx;
		   if (idx >=0 && references.size() > idx)
			   	rowName = references.get(idx).getId();
		   getInfo(rowName);
		   return;
//		   
//			   
//			stage = new Stage();
//		   stage.setTitle("Information: " + rowName );
//			FXMLLoader fxmlLoader = new FXMLLoader();
//			String fullname = "webview.fxml";
//		    URL url = getClass().getResource(fullname);		// this gets the fxml file from the same directory as this class
//		    if (url == null)
//		    {
//		    	System.err.println("Bad path to the FXML file");
//		    	return;
//		    }
//		    fxmlLoader.setLocation(url);
//		    VBox appPane =  fxmlLoader.load();
//		    stage.setScene(new Scene(appPane, 600, 400));
//		    stage.show();
		}
		catch (Exception e) { e.printStackTrace();}

		
	}

	// ------------------------------------------------------
	final ContextMenu tableColumnOptions = new ContextMenu();
	String[] colnames = new String[] { "Id", "Reference", "Database", "Authors", "Title", "Source", "Year" };

	@FXML private void doTableOptionsPopup()
	{
		ObservableList<MenuItem> items = tableColumnOptions.getItems();
		if (items.size() == 0)
		{
			for (String c : colnames)
				items.add(new CheckMenuItem(c));
		}
			
		tableColumnOptions.setX(tableOptions.getLayoutX() + stage.getX() - 60); 
		tableColumnOptions.setY(tableOptions.getLayoutY() + stage.getY()+ 40);
		tableColumnOptions.show(stage);
	}
	//-----------------------------------------------------------------------
	@FXML private void doAbout()
	{
//		AboutDialog dlog = new AboutDialog();
//		dlog.showAndWait();
	}
//	Map<Object, Object> dependents = new HashMap<Object, Object>();
//	public Map<Object, Object> getDependents() {		return dependents;	}
	//-----------------------------------------------------------------------
//	private final ChangeListener<Object> changeListener = 
//		    (obs, oldValue, newValue) ->  System.out.println("The binding is now invalid.");
	
	 @FXML private void test1()
	{
		double[] vals = { 50, 50, 50, 100, 150, 100};
//		Polygon p = new Polygon(vals);
//		add(p);
		
		if (-1 > vals.length) return;
		
		undoStack.push(ActionType.Test);	
		ShapeFactory f = getNodeFactory().getShapeFactory();
		AttributeMap attrMap = new AttributeMap();
		attrMap.putFillStroke(Color.PINK, Color.INDIGO);
		attrMap.putCircle(new Circle(120, 230, 40));
		attrMap.put("TextLabel", "root");
		MNode n1 = new MNode(attrMap, getModel(), getPasteboard());
		add(n1.getStack());
	
		attrMap.putFillStroke(Color.CORNSILK, Color.BLUE);
		attrMap.putCircle(new Circle(220, 130, 60));
		attrMap.setTool(Tool.Circle.toString()); 
		MNode circ = new MNode(attrMap, getModel(), pasteboard);		//, "Eli"
		add(circ.getStack());
	
		attrMap.putFillStroke(Color.LIGHTSKYBLUE, Color.DARKOLIVEGREEN);
		attrMap.putCircle(new Circle(220, 330, 60));
		MNode n3 = new MNode(attrMap, getModel(), pasteboard);	//, "Fristcut"
		add(n3.getStack());
		
		Edge line1 = model.addEdge(circ, n3);
		Edge line2 = model.addEdge(circ, n1);
		
		add(0, line1);
//		Arrow a = new Arrow(line1, 0.0f);
//		add(1, a);
//		
		add(0, line2);
		
		Rectangle r1 = new Rectangle(290, 230, 60, 60);
		attrMap.setTool(Tool.Rectangle.toString());
		attrMap.putRect(r1);
		attrMap.putFillStroke(Color.CORNSILK, Color.DARKOLIVEGREEN);
		MNode n4 = new MNode(attrMap, getModel(), pasteboard);
		add(n4.getStack());
			
		Edge line3 = model.addEdge(n4, circ);
//		line3.getPolyline().setStrokeWidth(2);
//		add(0, line3);
	
		Edge line4 = model.addEdge(n1, n3);
//		line4.getPolyline().setStrokeWidth(4);
//		add(0, line4);
	}

// **-------------------------------------------------------------------------------
	@FXML private void test2()
	{
		undoStack.push(ActionType.Test);	
		double WIDTH = 20;
		double HEIGHT = 20;
		double RADIUS = 10;
		double spacer = 5 * RADIUS;
		ShapeFactory f = getNodeFactory().getShapeFactory();
		AttributeMap attrMap = new AttributeMap();
		attrMap.putFillStroke(Color.PINK, Color.INDIGO, 1.0);
		for (int i=0; i<WIDTH; i++)
			for (int j=0; j<HEIGHT; j++)
			{
				Circle c1 = new Circle(i * spacer, j * spacer, RADIUS);
				attrMap.putCircle(c1);
				attrMap.put("ShapeType","Circle");
				attrMap.put("GraphId", i + ", " + j);
				add(new MNode(attrMap, getModel(), pasteboard).getStack());
			}
	}
	
	@FXML private void test3()
	{
//		addAll(new GPML(this).makeTestItems());
	}
	// **-------------------------------------------------------------------------------
	private void setGraphic(ToggleButton b, Tool t, GlyphIcons i)
	{
		b.setGraphic(GlyphsDude.createIcon(i, GlyphIcon.DEFAULT_ICON_SIZE));
		b.setId(t.name());
	}
	
	private void setGraphic(Button b, GlyphIcons i)
	{
		b.setGraphic(GlyphsDude.createIcon(i, GlyphIcon.DEFAULT_ICON_SIZE));
		b.setText("");
	}
	
	private void setupPalette()
	{
		setGraphic(arrow, Tool.Arrow, FontAwesomeIcons.LOCATION_ARROW);
		setGraphic(rectangle, Tool.Rectangle, FontAwesomeIcons.SQUARE);
		setGraphic(circle, Tool.Circle, FontAwesomeIcons.CIRCLE);
		setGraphic(polygon, Tool.Polygon, FontAwesomeIcons.STAR);
		setGraphic(polyline, Tool.Polyline, FontAwesomeIcons.PENCIL);
		setGraphic(line, Tool.Line, FontAwesomeIcons.LONG_ARROW_RIGHT);
		setGraphic(shape1, Tool.Shape1, FontAwesomeIcons.HEART);
//		setGraphic(shape2, Tool.Brace, FontAwesomeIcons.BARCODE);

		setGraphic(leftSideBarButton, FontAwesomeIcons.ARROW_CIRCLE_O_RIGHT);
		setGraphic(rightSideBarButton, FontAwesomeIcons.ARROW_CIRCLE_O_LEFT);
//		setGraphic(bottomSideBarButton, FontAwesomeIcons.ARROW_CIRCLE_DOWN);
//		setGraphic(toggleRulerButton, FontAwesomeIcons.BARS);
		setGraphic(toggleGridButton, FontAwesomeIcons.TH);
	}
	// **-------------------------------------------------------------------------------
	public void setState(String s)
	{
		pasteboard.getChildren().clear();
		pasteboard.getChildren().add(pasteboard.getGrid());
//		addState(s);
	}
	//-----------------------------------------------------------------------------
	public void addState(org.w3c.dom.Document doc)
	{
		new GPML(this).read(doc);

	}
	public void updateTable() {
		
		refTable.setItems(references);
	}

	public void addComment(String key, String val) {
		comments.add(key + ": " + val);
		
	}
	public void clearComments() {		comments.clear();			}
	public String getComments() {
		StringBuilder b = new StringBuilder();
		for (String c : comments)
			b.append(c).append("\n");
		return b.toString();
	}
	
	List<String> comments = new ArrayList<String>();
	ObservableList<Gene> genes = FXCollections.observableArrayList();
	public void addGene(Gene g) 		{		genes.add(g);	}
	public void clearGenes() 			{		genes.clear();	}

	ObservableList<BiopaxRef> references = FXCollections.observableArrayList();
	public void addRef(BiopaxRef ref) 	{	if (!refExists(ref))			references.add(ref);		}
	private boolean refExists(BiopaxRef ref) {
		String id = ref.getId();
		if (id == null) return false;
		for (BiopaxRef r : references)
			if (id.equals(r.getId())) return true;
		return false;
	}
	public void clearRefs() 			{		references.clear();	}
	//-----------------------------------------------------------------------------
//	public void addState(String s)			// used in undo restore
//	{
//		Scanner scan = new Scanner(s);
//		while (scan.hasNextLine())
//		{
//			String line = scan.nextLine();
//			Node node = getNodeFactory().parseNode(line, true);
//			if (node != null)
//			{
//				if (verbose > 0) System.out.print(", adding " + node.getId());
//				add(node);
//			}
//			if (verbose > 0) System.out.println();
//		} 
//		scan.close();
//		refreshZoomPane();
//	}
	//-----------------------------------------------------------------------------
	public void doPaste()
	{
		// TODO Auto-generated method stub
		
	}
	//-----------------------------------------------------------------------------
	private void setupListviews()
	{
		if (undoview != null)
		{
			undoview.setCellFactory(list -> {  return new DrawActionCell();  });	
			undoview.setStyle(CSS_Gray2);
		}
		
		//setupGeneList() 
		if (geneListTable != null)
		{
			geneListTable.setItems(genes);
//			geneListTable.setCellFactory(list ->  { return new GeneCell();   });
			geneListTable.setStyle(CSS_Gray2);
			nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
			ensemblCol.setCellValueFactory(new PropertyValueFactory<>("ensembl"));
		}
		
		attributeTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE); //  multiple selection
		attributeTable.setItems(RandomAttributeValueData.getRandomAttributeValueData()); // set Dummy Data for the TableView
		attributeTable.getSelectionModel().setCellSelectionEnabled(false);

		setRowFactory();				// set the Row Factory of the table
		
		attributeTable.setStyle(CSS_Gray2);
		attributeCol.setCellValueFactory(new PropertyValueFactory<>("attribute"));
		valueCol.setCellValueFactory(new PropertyValueFactory<>("value"));
		
		
	}
	//---------------------------------------------------------------------------------
//	  static class GeneCell extends ListCell<Gene> {
//		    @Override
//		    public void updateItem(Gene item, boolean empty) {
//		      super.updateItem(item, empty);
//		      if (item != null) 
//		      {
////		    	  String style = CSS_cellBackground(item.isUndone());
////		    	  setStyle(style);
//		    	  setText(item.toString());
//		      }
//		      else     {   setStyle("");  	  setText("");   }		// the cell is reused, so clear it
//		    }
//		  }
//	  
	  
	  static class DrawActionCell extends ListCell<Action> {
		    @Override
		    public void updateItem(Action item, boolean empty) {
		      super.updateItem(item, empty);
		      if (item != null) 
		      {
		    	  String style = CSS_cellBackground(item.isUndone());
		    	  setStyle(style);
		    	  setText(item.toString());
		      }
		      else   {  	  setStyle("");  	  setText("");    }
		    }
		  }
	  
	  static class StyleSheetCell extends ListCell<String> {
		    @Override
		    public void updateItem(String item, boolean empty) {
		      super.updateItem(item, empty);
		      if (item != null) 
		      {
//		    	  String style = CSS_cellBackground(item.isUndone());
//		    	  setStyle(style);
		    	  setText(item.toString());
		      }
		      else    {     setStyle("");   	  setText("");      }
		    }
		  }
	// **-------------------------------------------------------------------------------
	void bindInspector()
	{
		strokeLabel.setGraphic(GlyphsDude.createIcon(FontAwesomeIcons.PENCIL, GlyphIcon.DEFAULT_ICON_SIZE));
		fillLabel.setGraphic(GlyphsDude.createIcon(FontAwesomeIcons.CAR, GlyphIcon.DEFAULT_ICON_SIZE));
		fillLabel.setText("");		fillLabel.setMinWidth(30);		fillLabel.setAlignment(Pos.BASELINE_CENTER);
		strokeLabel.setText(""); 	strokeLabel.setMinWidth(30);	strokeLabel.setAlignment(Pos.BASELINE_CENTER);
		
		fillColor.setOnAction(evt -> apply(true, fillColor));
		lineColor.addEventHandler(ActionEvent.ACTION, evt -> apply(true, lineColor));

//		opacity.valueProperty().addListener((ov, old, val) ->   {  	apply(false, opacity);        });	
		weight.valueProperty().addListener((ov, old, val) ->    {   apply(false, weight);   });	
		rotation.valueProperty().addListener((ov, old, val) ->  {   apply(false, rotation);  });	
		
		// sliders don't record undoable events (because they make so many) so snapshot the state on mousePressed
		EventHandler<Event> evH = event -> {	undoStack.push(ActionType.Property);  };
//		opacity.setOnMousePressed(evH); 
		weight.setOnMousePressed(evH);
		rotation.setOnMousePressed(evH);
	}
		
	// **-------------------------------------------------------------------------------
	private String getStyleSettings(Control src)
	{
		Color fill = fillColor.getValue();
		Color stroke = lineColor.getValue();
		if (fill == null) fill = Color.GRAY;
		if (stroke == null) stroke = Color.DARKGRAY;
		String fillHex = "-fx-fill: #" + fill.toString().substring(2, 8) + ";\n";
		String strokeHex = "-fx-stroke: #" + stroke.toString().substring(2, 8) + ";\n";
		String wtStr = String.format("-fx-stroke-width: %.1f;\n", weight.getValue());
		String opacStr = ""; // String.format("-fx-opacity: %.2f;\n", opacity.getValue());
		String rotStr = String.format("-fx-rotate: %.1f;\n", rotation.getValue());

//		if (src == fillColor)		return fillHex;
//		if (src == lineColor)		return strokeHex;
//		if (src == weight)			return wtStr;
//		if (src == opacity)			return opacStr;
//		if (src == rotation)		return rotStr;

		StringBuilder buff = new StringBuilder();
		buff.append(fillHex).append(strokeHex).append(wtStr).append(opacStr).append(rotStr);
		return buff.toString();
	}
//	
//	public static String getStyle(Shape n)
//	{
//		DecimalFormat fmt = new DecimalFormat("0.00");
//		StringBuilder buff = new StringBuilder();
//		Paint fill = n.getFill();
//		String fillStr = fill.toString();
//		Paint stroke = n.getStroke();
//		String strokeStr = stroke.toString();
//		double wt = n.getStrokeWidth();
//		String wtStr = fmt.format(wt);
//		double opac = n.getOpacity();
//		String opacStr = fmt.format(opac);
//		buff.append("-fx-fill: #").append(fillStr).append(";\n");
//		buff.append("-fx-stroke: #").append(strokeStr).append(";\n");
//		buff.append("-fx-stroke-width: ").append(wtStr).append(";\n");
//		buff.append("-fx-opacity: ").append(opacStr).append(";\n");
//		return buff.toString();
//	}
	
	// **-------------------------------------------------------------------------------
	public void setUpInspector()
	{
		if(getSelectionManager().count() == 1)
		{
			VNode firstNode = getSelection().get(0);
			if (firstNode.getShape() != null)
			{
				Shape n = firstNode.getShape();
				Paint fill = n.getFill();
				Paint stroke = n.getStroke();
				double wt = n.getStrokeWidth();
				double opac = n.getOpacity();
				double rot = n.getRotate();
				
				fillColor.setValue((Color) fill);
				lineColor.setValue((Color) stroke);
				weight.setValue(wt);
//				opacity.setValue(100 * opac);
				rotation.setValue(rot);
			}
			if (firstNode instanceof StackPane)
			{
				StackPane n = (StackPane) getSelection().get(0);
				String style = n.getStyle();
				AttributeMap attr = new AttributeMap(style, true);
				Color fill = attr.getColor("-fx-background-color");
				Color stroke = attr.getColor("-fx-border-color");
				double opac = attr.getDouble("-fx-opacity", 1);
				double wt = attr.getDouble("-fx-border-width", 12);
				double rot = attr.getDouble("-fx-rotate", 0);
//				stroke = attr.getColor("-fx-border-color");
//				n.getBackground().getFills().get(0).getFill();
//				""
//				Paint stroke = n.getChildren().get(0).getBetBorder().getStrokes().get(0).get();
//				double wt = n.getStrokeWidth();
//				double opac = n.getOpacity();
//				
				fillColor.setValue(fill);
				lineColor.setValue(stroke);
				weight.setValue(wt);
				rotation.setValue(rot);
//				opacity.setValue(100 * opac);
			}
			
		}
	}
	// **-------------------------------------------------------------------------------
	private void apply(boolean undoable, Control src)							
	{ 	 			getSelectionManager().applyStyle(getStyleSettings(src));	
		if (undoable) 
			undoStack.push(ActionType.Property); 
	}
	// **-------------------------------------------------------------------------------
	public void selectAll(ObservableList<Node> n)	{		getSelectionManager().selectAll(n);	}

//	public void setStatus(String s)					{ 		status1.setText(s);	}
	// **-------------------------------------------------------------------------------
	public void add(Node n)							
	{		
		if (n == null) return;
		pasteboard.getChildren().add(n);	
		if ("Marquee".equals(n.getId())) 	return;
//		model.addResource(n.getId(), n.getModel());
		Object prop  = n.getProperties().get("TextLabel");
		if (prop != null && findGene(""+prop) == null)
			addGene(new Gene(""+prop));
	}
	private Gene findGene(String string) {
		if (StringUtil.isEmpty(string)) return null;
		for (Gene g : genes)
			if (string.equals(g.getName()))
				return g;
		return null;
	}
	public void add(int idx, Edge e)							
	{		
		if (e == null) return;
		pasteboard.getChildren().add(idx, e.getEdgeLine());	
//		drawPane.getChildren().add(idx, e.getLine() );	
		model.addEdge(e);
	}
	public void add(int idx, VNode n)							
	{		
		pasteboard.getChildren().add(idx, n);	
		if ("Marquee".equals(n.getId())) 	return;
		model.addResource(n.getId(), n.getModel());
	}
	public void addAll(ObservableList<Node> n)	{		pasteboard.getChildren().addAll(n);	}
	public void addAll(Node... n)				{		pasteboard.getChildren().addAll(n);	}

	public void remove(Node n)						
	{		
		getDrawModel().removeNode(n);
//		Object dependent = dependents.get(n);
//		if (dependent != null)
//		{
//			pasteboard.getChildren().remove(dependent);	
//			dependents.remove(n);
//		}
		pasteboard.getChildren().remove(n);	
	}

	// **-------------------------------------------------------------------------------
	public String getState()			{ 	return model.traverseSceneGraph(pasteboard).toString();  }

	// **-------------------------------------------------------------------------------
	public void reportStatus(String string)	
	{		
//		status1.setText(string);	
	}
//	public void setStatus2(String status)	{	status2.setText(status);	}
//	public void setStatus3(String status)	{	status3.setText(status);	}
	// **-------------------------------------------------------------------------------
	public void addStylesheet(File f)
	{
		Scene scene = pasteboard.getScene();
		ObservableList<String> sheets = scene.getStylesheets();
		sheets.add(f.getName());
//		attributeTable.setItems(sheets);
	}
	// **-------------------------------------------------------------------------------
	/**
	 * Set Row Factory for the TableView
	 */
	public void setRowFactory()
	{
		attributeTable.setRowFactory(new Callback<TableView<AttributeValue>, TableRow<AttributeValue>>()
		{
			@Override public TableRow<AttributeValue> call(TableView<AttributeValue> p)
			{
				final TableRow<AttributeValue> row = new TableRow<AttributeValue>();
				row.setOnDragEntered(e ->	{		});

				row.setOnDragDetected(e ->
				{
					Dragboard db = row.getTableView().startDragAndDrop(TransferMode.COPY);
					ClipboardContent content = new ClipboardContent();
					AttributeValue av = row.getItem();
					String s = av.makeString();			/// handle multiple selection by asking table to generate the string
					content.put(DataFormat.PLAIN_TEXT, s);
					db.setContent(content);
//					setSelection(row);
					e.consume();
				});
				return row;
			}
		});
	}
	// **-------------------------------------------------------------------------------
	// TODO add charts
	
	public void scatter(TableView content)	{
	}

	public void timeseries(TableView content)	{
	}
	public void hiliteByReference(String ref) {
		for (BiopaxRef biopax : references)
			if (ref.equals(biopax.getXrefid()))
				refTable.getSelectionModel().select(biopax);
	}
	public void openByReference(String ref) {
		for (BiopaxRef biopax : references)
			if (ref.equals(biopax.getXrefid()))
			{
				String pmid = biopax.getId();
				if (StringUtil.hasText(pmid))
					getInfo(pmid);
			}
	}
	public void assignDataFile(File f) {
			System.out.println("assignDataFile");		
			List<String> lines = FileUtil.readFileIntoStringList(f.getAbsolutePath());
			for (String s : lines)
			{
				String[] parts = s.split("\t");
				if (parts.length == 2)
				{
					MNode node = model.getResourceByKey(parts[0]);
					if (node != null)
						node.getAttributeMap().put("value", parts[1]);
				}
			}
			setColorByValue();
	}
	
	
	private void setColorByValue() {
//		clearColors();
		for (MNode node : model.getResourceMap().values())
		{
			Object val = node.getAttributeMap().get("value");
			if (val != null)
			{
				double d = StringUtil.toDouble("" + val);
				if (!Double.isNaN(d) && 0 <= d && 1 >= d)
				{
					Color gray = new Color(d,d,d, 1);
					if (node.getStack().getShape() != null)
						(node.getStack().getShape()).setFill(gray);			// TODO set the attribute
				}
			}
		}
	}	
@FXML	private void clearColors(ActionEvent ev) {
			for (MNode node : model.getResourceMap().values())
			{
				node.getAttributeMap().remove("value");
				Shape shape = node.getStack().getShape();
				if (shape != null)
					shape.setFill(Color.WHITE);
			}
	}
public void finishRead() {
	
	StringBuilder builder = new StringBuilder();
	for (Gene g : genes)
		builder.append(g.getName()).append('\n');
		String genelist = builder.toString();
		System.out.println(genelist);		

}
	

}
