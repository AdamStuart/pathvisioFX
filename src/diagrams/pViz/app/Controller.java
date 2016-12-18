package diagrams.pViz.app;

import java.io.File;
import java.net.URL;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.List;
import java.util.ResourceBundle;

import animation.BorderPaneAnimator;
import database.forms.EntrezQuery;
import diagrams.pViz.app.Action.ActionType;
import diagrams.pViz.gpml.Anchor;
import diagrams.pViz.gpml.GPML;
import diagrams.pViz.model.Edge;
import diagrams.pViz.model.MNode;
import diagrams.pViz.model.Model;
import diagrams.pViz.tables.DraggableTableRow;
import diagrams.pViz.tables.GeneListTable;
import diagrams.pViz.tables.PathwayController;
import diagrams.pViz.view.Pasteboard;
import diagrams.pViz.view.VNode;
import gui.Borders;
import icon.FontAwesomeIcons;
import icon.GlyphIcon;
import icon.GlyphIcons;
import icon.GlyphsDude;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
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
import javafx.scene.control.Slider;
import javafx.scene.control.SplitPane;
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
import model.AttributeMap;
import model.bio.BiopaxRef;
import model.bio.Gene;
import model.bio.GeneList;
import model.bio.PathwayRecord;
import model.bio.Species;
import util.FileUtil;
import util.StringUtil;


public class Controller implements Initializable
{
    private static final DataFormat BIOPAX_MIME_TYPE = new DataFormat("application/x-java-biopax");

	//@formatter:off
	private Model model;
	public Model getDrawModel()   		{ 		return model;  }
	public Model getModel() 			{		return model;	}	
	private Pasteboard pasteboard;
	public Pasteboard getPasteboard()   { 		return pasteboard;  }
	private UndoStack undoStack;
	public UndoStack getUndoStack()		{		return undoStack;	}
	private Document doc;

	int verbose = 0;
	
	@FXML private VBox east;
	@FXML private VBox south;
	@FXML private Pane drawPane;
	@FXML private ScrollPane scrollPane;
	@FXML private ListView<Action> undoview = null;
	private GeneListTable geneListTable = null;
	@FXML private BorderPane container;			// root of fxml	

	@FXML private TableView<BiopaxRef> refTable;
	@FXML private TableColumn<BiopaxRef, String> refCol;
	@FXML private TableColumn<BiopaxRef, String> idCol;
	@FXML private TableColumn<BiopaxRef, String> dbCol;
	@FXML private TableColumn<BiopaxRef, String> authorCol;
	@FXML private TableColumn<BiopaxRef, String> titleCol;
	@FXML private TableColumn<BiopaxRef, String> sourceCol;
	@FXML private TableColumn<BiopaxRef, String> yearCol;
	@FXML private Button tableOptions;

	@FXML private Button gene;		// something to drag in the Genelist window

	@FXML private Button bottomSideBarButton;
	@FXML private Button leftSideBarButton;
	@FXML private Button rightSideBarButton;
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
	@FXML private Slider scale;
	@FXML private Slider rotation;
//	@FXML private Label status1;
//	@FXML private Label status2;
//	@FXML private Label status3;
	@FXML private Label strokeLabel;
	@FXML private Label fillLabel;
	//-------------------------------------------------------------
	@FXML private MenuItem undo;
	@FXML private MenuItem redo;
	@FXML private MenuItem clearundo;
	
	@FXML private MenuItem clearColors;
	@FXML private MenuItem browsePathways;
	@FXML private MenuItem tofront;
	@FXML private MenuItem toback;
	@FXML private MenuItem group;
	@FXML private MenuItem ungroup;
	@FXML private MenuItem delete;
	@FXML private VBox west;
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
	@FXML private void doNew()			{ 	App.getInstance().doNew();			}
	@FXML private void open()			{ 	doc.open();	}			//doc.open();
	public void open(File f)			{ 	doc.open(f);	}			//doc.open();
	public void open(String s)			{ 	doc.open(s);	}			//doc.open();
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
//	@FXML private void clickDrawPane()	{ 	System.out.println("clicked");			}
	// **-------------------------------------------------------------------------------
	@FXML private  void cut()			{ 	undoStack.push(ActionType.Cut);		getSelectionManager().cut();	}
	@FXML private  void copy()			{ 	getSelectionManager().copy();		}	// not undoable
	@FXML private  void paste()			{ 	undoStack.push(ActionType.Paste);	doPaste();		}
	@FXML private  void clearUndo()		{	undoStack.clear(); 	}
	@FXML private void selectAll()		{ 	undoStack.push(ActionType.Select); getSelectionManager().selectAll(); 		}
	@FXML public void deleteSelection(){ 	undoStack.push(ActionType.Delete);	getSelectionManager().deleteSelection(); 	}
	@FXML public void duplicateSelection(){ undoStack.push(ActionType.Duplicate);	getSelectionManager().cloneSelection(7); 	}
	@FXML public void clear()			{ undoStack.push(ActionType.Delete);	getSelectionManager().deleteAll(); 	}
	// **-------------------------------------------------------------------------------
	@FXML public  void group()			{ 	undoStack.push(ActionType.Group);	getSelectionManager().doGroup();  }
	@FXML public  void ungroup()		{ 	undoStack.push(ActionType.Ungroup);	getSelectionManager().ungroup(); }
	@FXML public  void toFront()		{	undoStack.push(ActionType.Reorder);	getSelectionManager().toFront(); 	}
	@FXML public  void toBack()			{	undoStack.push(ActionType.Reorder);	getSelectionManager().toBack();  pasteboard.getGrid().toBack();  	}

	@FXML private void hideAnchors()	{ 	model.setAnchorVisibility(false);	}
	@FXML private void showAnchors()	{ 	model.setAnchorVisibility(true);	}

	// **-------------------------------------------------------------------------------
	@FXML public  void browsePathways()		
	{
		Stage browserStage = new Stage();
		try 
		{
			browserStage.setTitle("Pathway Browser");
			FXMLLoader fxmlLoader = new FXMLLoader();
			String fullname = "../tables/PathwayList.fxml";
		    URL url = getClass().getResource(fullname);		// this gets the fxml file from the same directory as this class
		    if (url == null)
		    {
		    	System.err.println("Bad path to the FXML file: " + fullname);
		    	return;
		    }
		    fxmlLoader.setLocation(url);
		    Pane appPane =  fxmlLoader.load();
		    PathwayController c = (PathwayController) fxmlLoader.getController();
		    c.setController(this);
		    browserStage.setScene(new Scene(appPane, 800, 800));
		    browserStage.show();
		    c.doSearch();
		}
		catch (Exception e) { e.printStackTrace();}
	}	
	// **-------------------------------------------------------------------------------
	public  FXMLLoader buildStage(String title, String fxml)		
	{
		Stage browserStage = new Stage();
		try 
		{
			browserStage.setTitle(title);
			FXMLLoader fxmlLoader = new FXMLLoader();
		    URL url = getClass().getResource(fxml);		// this gets the fxml file from the same directory as this class
		    if (url == null)
		    {
		    	System.err.println("Bad path to the FXML file: " + fxml);
		    	return null;
		    }
		    fxmlLoader.setLocation(url);
		    SplitPane appPane =  fxmlLoader.load();
		    browserStage.setScene(new Scene(appPane, 1200, 800));
		    browserStage.show();
		    return fxmlLoader;
		}
		catch (Exception e) { e.printStackTrace();}
		return null;
	}

	Species getSpecies()	{ return model.getSpecies();	}
	
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
		undoStack = new UndoStack(this, null);
		assert(drawPane != null);
		model = new Model(this);
		pasteboard = new Pasteboard(this);
		drawPane.getChildren().add(pasteboard);
		
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
		});
//		bottomDash.setBorder(Borders.dashedBorder );
		if (bottomPadding!= null)
			bottomPadding.setPadding(new Insets(3,3,4,4));

		if (west != null)
			west.setBorder(Borders.lineBorder);
		if (east != null)
		{
			geneListTable = new GeneListTable(this);
			east.getChildren().add(geneListTable);
		}
		setupPalette();
		setupListviews();
		new BorderPaneAnimator(container, leftSideBarButton, Side.LEFT, false, 90);
		new BorderPaneAnimator(container, rightSideBarButton, Side.RIGHT, false, 300);
		new BorderPaneAnimator(container, bottomSideBarButton, Side.BOTTOM, false, 100);
		pasteboard.makeGrid(toggleGridButton, scrollPane);


		boolean startWithShapes = false;
		if (startWithShapes) test1();
	}
	static TableRow<BiopaxRef> thisRow = null;
	private void setupBiopaxTable() {
		
//		TableColumn[] allCols = { refCol, idCol, dbCol, authorCol, titleCol, sourceCol, yearCol };

		refCol.setCellValueFactory(new PropertyValueFactory<BiopaxRef, String>("xrefid"));
		idCol.setCellValueFactory(new PropertyValueFactory<BiopaxRef, String>("id"));
		dbCol.setCellValueFactory(new PropertyValueFactory<BiopaxRef, String>("db"));
		authorCol.setCellValueFactory(new PropertyValueFactory<BiopaxRef, String>("authors"));
		titleCol.setCellValueFactory(new PropertyValueFactory<BiopaxRef, String>("title"));
		sourceCol.setCellValueFactory(new PropertyValueFactory<BiopaxRef, String>("source"));
		yearCol.setCellValueFactory(new PropertyValueFactory<BiopaxRef, String>("year"));

		refTable.setRowFactory((a) -> {  return new DraggableTableRow<BiopaxRef>(refTable, BIOPAX_MIME_TYPE, this);   });
		
	}
	private void showPMIDInfo(String pmid)
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
	
	
	public void getInfo(DataFormat mimeType, int idx) {
		try 
		{
			if (mimeType == BIOPAX_MIME_TYPE)
			 {
			   String rowName = "" + idx;
			   if (idx >=0 && model.getNReferences() > idx)
				   	rowName = model.getReference(idx).getId();
			   showPMIDInfo(rowName);
			   return;
		  }
		  if (mimeType == PathwayController.PATHWAY_MIME_TYPE)
		   {
				PathwayRecord rec = pathwayTable.getItems().get(idx);
				String url = rec.getUrl();
				System.out.println("getInfo: " + rec.getId() + " Fetching: " + url);					//TODO
//				String result = StringUtil.callURL(url, false);
//				browseGenes(result);
				
			}

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

	 @FXML private void test1()
	{
		undoStack.push(ActionType.Test);	
		AttributeMap attrMap = new AttributeMap();
		attrMap.putFillStroke(Color.PINK, Color.INDIGO);
		attrMap.putCircle(new Circle(120, 230, 40));
		attrMap.put("TextLabel", "root");
		MNode n1 = new MNode(attrMap, model);
		add(n1.getStack());
	
		attrMap.putFillStroke(Color.CORNSILK, Color.BLUE);
		attrMap.putCircle(new Circle(220, 130, 60));
		attrMap.setTool(Tool.Circle.toString()); 
		MNode circ = new MNode(attrMap, model);		//, "Eli"
		add(circ.getStack());
	
		attrMap.putFillStroke(Color.LIGHTSKYBLUE, Color.DARKOLIVEGREEN);
		attrMap.putCircle(new Circle(220, 330, 60));
		MNode n3 = new MNode(attrMap, model);	//, "Fristcut"
		add(n3.getStack());
		
		Edge line1 = model.addEdge(circ, n3);		add(0, line1);
		Edge line2 = model.addEdge(circ, n1);		add(0, line2);
		
		Rectangle r1 = new Rectangle(290, 230, 60, 60);
		attrMap.setTool(Tool.Rectangle.toString());
		attrMap.putRect(r1);
		attrMap.putFillStroke(Color.CORNSILK, Color.DARKOLIVEGREEN);
		MNode n4 = new MNode(attrMap, model);
		add(n4.getStack());
	}

// **-------------------------------------------------------------------------------
	@FXML private void test2()
	{
		undoStack.push(ActionType.Test);	
		double WIDTH = 20;
		double HEIGHT = 20;
		double RADIUS = 10;
		double spacer = 5 * RADIUS;
//		ShapeFactory f = getNodeFactory().getShapeFactory();
		AttributeMap attrMap = new AttributeMap();
		attrMap.putFillStroke(Color.PINK, Color.INDIGO, 1.0);
		for (int i=0; i<WIDTH; i++)
			for (int j=0; j<HEIGHT; j++)
			{
				Circle c1 = new Circle(i * spacer, j * spacer, RADIUS);
				attrMap.putCircle(c1);
				attrMap.put("ShapeType","Circle");
				attrMap.put("GraphId", i + ", " + j);
				add(new MNode(attrMap, model).getStack());
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
		setGraphic(bottomSideBarButton, FontAwesomeIcons.ARROW_CIRCLE_DOWN);
//		setGraphic(toggleRulerButton, FontAwesomeIcons.BARS);
		setGraphic(toggleGridButton, FontAwesomeIcons.TH);
	}
	// **-------------------------------------------------------------------------------
	public void setState(String s)
	{
		pasteboard.clear();
		pasteboard.getChildren().add(pasteboard.getGrid());
//		addState(s);
	}
	//-----------------------------------------------------------------------------
	public void addState(org.w3c.dom.Document doc)
	{
		new GPML(model).read(doc);
		updateTables();
		Window window = refTable.getScene().getWindow();
        if (window instanceof Stage) 
        	((Stage) window).setTitle(model.getTitle());	
        new Thread(() ->
        Platform.runLater(() -> {  getModel().resetEdgeTable();	} )  ).start();  
        hideAnchors();

//		addModelToPasteboard();

	}
	private void addModelToPasteboard() {
//		for (String key : model.getResourceMap().keySet())
//		{
//			MNode node = model.getResourceMap().get(key);
//			if (node != null)
//				pasteboard.getChildren().add(node.getStack());
//		}
	}	
			
	public void updateTables() {
		
		refTable.getItems().addAll(model.getReferences());
		geneListTable.populateTable(model.getGeneList());
	}

	//-----------------------------------------------------------------------------
	static final String RESOURCE = "bridgeDBmap.fxml";
    static final String STYLE = "idmapping.css";
	public void doNewGeneList() 
	{
		try
		{
			Stage stage = new Stage();
			URL resource = BridgeDbController.class.getResource(RESOURCE);
	        FXMLLoader loader = new FXMLLoader(resource);
	        Scene scene = new Scene(loader.load());
	        BridgeDbController controller = (BridgeDbController) loader.getController();
			scene.getStylesheets().add(BridgeDbController.class.getResource(STYLE).toExternalForm());
	        stage.setTitle("Gene List Window");
	        stage.setX(20);
			stage.setWidth(800);
			stage.setHeight(650);
			controller.setParentController(this);
			controller.start();
			stage.setScene(scene);
			stage.show();
		}
		catch (Exception e) { e.printStackTrace();
			
		}
	}//	public void addState(String s)			// used in undo restore
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
	void addGenes(GeneList inList)
	{
		List<Gene> genes = geneListTable.getItems();
		for (Gene g: inList)
			if (null == GeneList.findInList(geneListTable.getItems(), g.getName()))
				genes.add(g);
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
			geneListTable.getItems().addAll(model.getGenes());
//			geneListTable.setCellFactory(list ->  { return new GeneCell();   });
			geneListTable.setStyle(CSS_Gray2);
		}
		
//		attributeTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE); //  multiple selection
//		attributeTable.setItems(RandomAttributeValueData.getRandomAttributeValueData()); // set Dummy Data for the TableView
//		attributeTable.getSelectionModel().setCellSelectionEnabled(false);

//		setRowFactory();				// set the Row Factory of attributeValue table
		
//		attributeTable.setStyle(CSS_Gray2);
//		attributeCol.setCellValueFactory(new PropertyValueFactory<>("attribute"));
//		valueCol.setCellValueFactory(new PropertyValueFactory<>("value"));
		
		if (pathwayTable != null)
			setupPathwayTable();
	}
	
	TableView<PathwayRecord> pathwayTable = null;
	private void setupPathwayTable()
	{
//		pathwayTable.setItems(genes);
//		pathwayTable.setCellFactory(list ->  { return new GeneCell();   });
//		pathwayTable.setStyle(CSS_Gray2);
//		nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
//		ensemblCol.setCellValueFactory(new PropertyValueFactory<>("ensembl"));
		
	}
	
	//---------------------------------------------------------------------------------
	  
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
		    @Override public void updateItem(String item, boolean empty) {
		      super.updateItem(item, empty);
		      if (item != null) 
		    	  setText(item.toString());
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

		scale.valueProperty().addListener((ov, old, val) ->   {  	pasteboard.setZoom(scale.getValue());        });	
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
	{ 	 			
		if (undoable) 
			undoStack.push(ActionType.Property); 
		getSelectionManager().applyStyle(getStyleSettings(src));	
	}
	// **-------------------------------------------------------------------------------
	public void selectAll(List<Node> n)	{		getSelectionManager().selectAll(n);	}

//	public void setStatus(String s)					{ 		status1.setText(s);	}
	// **-------------------------------------------------------------------------------
	public void add(Node n)							
	{		
		if (n == null) return;
		pasteboard.getChildren().add(n);	
		if ("Marquee".equals(n.getId())) 	return;
//		model.addResource(n.getId(), n.getModel());
		if (n instanceof VNode)
		{
			MNode modelNode = ((VNode) n).getModel();
			if (((VNode) n).isAnchor()) return;
			if (((VNode) n).isLabel()) return;
			Object prop  = modelNode.getAttributeMap().get("TextLabel");
			if (prop != null && findGene(""+prop) == null)
				model.addGene(new Gene(""+prop));
		}
	}
	
	private Gene findGene(String string) {
		if (StringUtil.isEmpty(string)) return null;
		for (Gene g : model.getGenes())
			if (string.equals(g.getName()))
				return g;
		return null;
	}
	@FXML private void resetEdgeTable()	{		model.resetEdgeTable();	}
	@FXML private void dumpEdgeTable()	{		model.dumpEdgeTable();	}
	@FXML private void dumpViewHierarchy()	{	model.dumpViewHierarchy();	}
	
	@FXML private void dumpNodeTable()	{		model.dumpNodeTable();	}
	public void add(int idx, Edge e)							
	{		
		if (e == null) return;
		model.addEdge(e);
		pasteboard.add(idx, e.getEdgeLine());
		for (Anchor anchor : e.getEdgeLine().getAnchors())
		{
			VNode stack = anchor.getStack();
			Shape shap = stack.getShapeLayer();
			if (shap instanceof Circle)
				System.out.println(String.format("(%.2f, %.2f)", ((Circle)shap).getCenterX(), ((Circle)shap).getCenterY()));
			pasteboard.add(stack);
		}
	}
	public void add(int idx, VNode n)							
	{		
		if (idx < 0) idx = pasteboard.getChildren().size();
		pasteboard.add(idx, n);	
		if ("Marquee".equals(n.getId())) 	return;
//		model.addResource(n.getId(), n.getModel());
	}
	public void addAll(List<VNode> n)	{		pasteboard.addAllVNodes(n);	}
	public void addAll(VNode... n)		{		pasteboard.addAllVNodes(n);	}
	
	public void remove(Node n)						
	{		
		pasteboard.getChildren().remove(n);	
	}
	public void remove(VNode n)						
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
	public String getState()			{ 	return model.toString();  }

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
	// TODO add charts
	
	public void scatter(TableView content)	{}

	public void timeseries(TableView content)	{
	}
	public void hiliteByReference(String ref) {
		for (BiopaxRef biopax : model.getReferences())
			if (ref.equals(biopax.getXrefid()))
				refTable.getSelectionModel().select(biopax);
	}
	public void openByReference(String ref) {
		for (BiopaxRef biopax :  model.getReferences())
			if (ref.equals(biopax.getXrefid()))
			{
				String pmid = biopax.getId();
				if (StringUtil.hasText(pmid))
					showPMIDInfo(pmid);
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
					Shape shape = node.getStack().getShapeLayer();
					if (shape != null)
					{
						shape.setFill(gray);			// TODO set the attribute
						if (gray.getRed() < 0.4 || gray.getBlue() < 0.4 || gray.getGreen() < 0.4)
						{
							node.getStack().getTextField().setStyle("-fx-text-fill: white");
						}
					}
				}
			}
		}
	}	
	@FXML	private void clearColors() {
			
		for (MNode node : model.getResourceMap().values())
		{
			node.getAttributeMap().remove("value");
			Shape shape = node.getStack().getShapeLayer();
			if (shape != null)
			{	
				shape.setFill(Color.WHITE);
				node.getStack().getTextField().setStyle("-fx-text-fill: black");
			}
		}
	}
	
	public void setGeneList(GeneList geneList) 	
	{ 		
		geneListTable.getItems().addAll(geneList);			// ADD UNIQUE
		BorderPane parent = (BorderPane) geneListTable.getParent().getParent();
		VBox east = (VBox) parent.getRight();
		VBox west = (VBox) parent.getLeft();
		east.prefWidth(1000);
		east.prefHeight(1000);
		east.maxWidth(2000);
		east.maxHeight(2000);
		parent.getCenter().setVisible(false);
		west.setVisible(false);
	}
	public void viewPathway(String result)		
	{ 		
//		String pathwayFxml = "../gpml/GeneList.fxml";
//		String fullname = "../gpml/GeneList.fxml";
		FXMLLoader loader = buildStage("Pathway", "Visio.fxml");
		Controller newController = loader.getController();
		try
		{
			String gpml  =StringUtil.readTag( result, "ns2:gpml");
			Decoder decoder = Base64.getDecoder();
			byte[] cleanXML  = decoder.decode(gpml);
			String str = new String(cleanXML);
			newController.open(str);
		}
		catch (Exception e) {}
	}
}
