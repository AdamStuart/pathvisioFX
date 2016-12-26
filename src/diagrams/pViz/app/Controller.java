package diagrams.pViz.app;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

import javax.imageio.ImageIO;

import animation.BorderPaneAnimator;
import database.forms.EntrezQuery;
import diagrams.pViz.app.Action.ActionType;
import diagrams.pViz.dialogs.LegendDialog;
import diagrams.pViz.gpml.Anchor;
import diagrams.pViz.gpml.GPML;
import diagrams.pViz.model.Edge;
import diagrams.pViz.model.MNode;
import diagrams.pViz.model.Model;
import diagrams.pViz.tables.DraggableTableRow;
import diagrams.pViz.tables.GeneListController;
import diagrams.pViz.tables.GeneListTable;
import diagrams.pViz.tables.LegendRecord;
import diagrams.pViz.tables.MultiGeneListController;
import diagrams.pViz.tables.PathwayController;
import diagrams.pViz.view.Inspector;
import diagrams.pViz.view.Pasteboard;
import diagrams.pViz.view.VNode;
import gui.Backgrounds;
import gui.Borders;
import icon.FontAwesomeIcons;
import icon.GlyphIcon;
import icon.GlyphIcons;
import icon.GlyphsDude;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventTarget;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.Window;
import model.AttributeMap;
import model.bio.BiopaxRef;
import model.bio.Gene;
import model.bio.MIM;
import model.bio.PathwayRecord;
import model.bio.Species;
import util.FileUtil;
import util.StringUtil;


public class Controller implements Initializable, IController 
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
	@FXML public void clear()			{ 	undoStack.push(ActionType.Delete);	getSelectionManager().deleteAll(); 	}
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
	public  void buildStage(String title, VBox contents)		
	{
		Stage browserStage = new Stage();
		try 
		{
			browserStage.setTitle(title);
		    browserStage.setScene(new Scene(new ScrollPane(contents), 500, 800));
		    browserStage.show();
		}
		catch (Exception e) { e.printStackTrace();}
	}
	
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

	public Species getSpecies()	{ return model.getSpecies();	}
	
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
	public Selection getSelectionManager() 		{ 	return pasteboard.getSelectionMgr();  }
	public ObservableList<VNode> getSelection() { 	return getSelectionManager().getAll();  }
	private Stage stage;
	private Inspector inspector;
	public Inspector getInspector() { return inspector;	}
	// **-------------------------------------------------------------------------------
	@Override public void initialize(URL location, ResourceBundle resources)
	{
		undoStack = new UndoStack(this, null);
		assert(drawPane != null);
		model = new Model(this);
		pasteboard = new Pasteboard(this);
		drawPane.getChildren().add(pasteboard);
		
		doc = new Document(this);
		setupInspector();
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
		
		rightSideBarButton.fire();
		bottomSideBarButton.fire();
		
		pasteboard.makeGrid(toggleGridButton, scrollPane);


		boolean startWithShapes = false;
		if (startWithShapes) test1();
	}
	private void setupInspector() {
		URL res = getClass().getResource("../view/Inspector.fxml");
	    FXMLLoader inspectorLoader = new FXMLLoader(res);
	   try
	   {
		   inspectorLoader.load();
		   inspector = inspectorLoader.getController();
		   inspector.setParentController(this);
		   container.setBottom(inspector);
	   }
	   catch (Exception e) 
	   { System.err.println("Inspector failed to load");}

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
	   String text = EntrezQuery.getPubMedAbstract(pmid);
	   if (StringUtil.hasText(text))
	   {  
		   Alert a = new Alert(AlertType.INFORMATION, text);
		   a.setHeaderText("PubMed Abstract");
		   a.getDialogPane().setMinWidth(600);
		   a.setResizable(true);
		   a.showAndWait();
	   }
	}
	
	
	public void getInfo(DataFormat mimeType, String idx, String id) {
		try 
		{
			int index = StringUtil.toInteger(idx);
			if (mimeType == GeneListController.GENE_MIME_TYPE)
			 {
			   String rowName = "";
			   if (index >=0 && model.getNReferences() > index)
				   	rowName = model.getGenes().get(index).getId();
//			   showGeneInfo(rowName);
			   return;
		  }
			if (mimeType == BIOPAX_MIME_TYPE)
			 {
			   String rowName = "";
			   if (index >=0 && model.getNReferences() > index)
				   	rowName = model.getReference(index).getId();
			   showPMIDInfo(rowName);
			   return;
		  }
		  if (mimeType == PathwayController.PATHWAY_MIME_TYPE)
		   {
				System.out.println(" fetching pathway: " + id);					//TODO
				viewPathway(id);
			}
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
	}	//-----------------------------------------------------------------------
	@FXML private void addLegend()
	{
		LegendRecord.makeLegend("Legend", "", true, true, false, model, this, true);
	}
	@FXML private void addLegendDlog()
	{
		LegendDialog dlog = new LegendDialog();
		Optional<String> value = dlog.showAndWait();
		if (value.isPresent())
			LegendRecord.makeLegend(dlog.resultProperty(), model, this);
	}
	@FXML private void test1()	{		Test.test1(this);	}
	 @FXML private void test2()	{		Test.test2(this);	}
	 @FXML private void test3()	{		Test.test3(this);	}

// **-------------------------------------------------------------------------------
	// Tool palette
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
//		@FXML private ToggleButton shape2;
		

	// **-------------------------------------------------------------------------------
	private void setGraphic(ToggleButton b, Tool t, GlyphIcons i)
	{
		b.setGraphic(GlyphsDude.createIcon(i, GlyphIcon.DEFAULT_ICON_SIZE));
		b.setId(t.name());
	}
	
	static public void setGraphic(Button b, GlyphIcons i)
	{
		b.setGraphic(GlyphsDude.createIcon(i, GlyphIcon.DEFAULT_ICON_SIZE));
		b.setText("");
	}
	// **-------------------------------------------------------------------------------
	
	private ToggleGroup paletteGroup;
	public ToggleGroup getToolGroup()			{ 	return paletteGroup;	}

	private void setupPalette()	
	{
		paletteGroup = new ToggleGroup();
		paletteGroup.getToggles().addAll(arrow, rectangle, circle, polygon, polyline, line);
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
	Tool curTool = Tool.Arrow;
	public Tool getTool() {		return curTool;	}
	
	public void setTool(Tool inTool)
	{
		curTool = inTool;
		ToggleGroup group = getToolGroup();
//		group.selectToggle(value);
		for (Toggle t : group.getToggles())		// TODO this should reduce to a single line.
		{
			if (t instanceof ToggleButton)
			{
				ToggleButton b = (ToggleButton) t;
				if (b.getId().equals(curTool.name()))
				{
					group.selectToggle(t);
					break;
				}
			}
		}
	}	// **-------------------------------------------------------------------------------
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
			
	public void updateTables() {
		
		refTable.getItems().addAll(model.getReferences());
		geneListTable.populateTable(model, model.getGenes());
	}

	//-----------------------------------------------------------------------------
	static final String RESOURCE = "../gpml/GeneList.fxml";
    static final String STYLE = "genelistStyles.css";
	public void doNewGeneList() 
	{
		try
		{
			Stage stage = new Stage();
			URL res = getClass().getResource(RESOURCE);
		    FXMLLoader geneListLoader = new FXMLLoader(res);
	        Scene scene = new Scene(geneListLoader.load());
		    GeneListController glc = (GeneListController) geneListLoader.getController();   
		    glc.setParentController(this);
//		    glc.loadTables(model);
		    glc.loadTables(geneListTable.getItems());
//			scene.getStylesheets().add(GeneListController.class.getResource(STYLE).toExternalForm());
	        stage.setTitle("Gene List Window");
	        stage.setX(20);
			stage.setWidth(800);
			stage.setHeight(650);
			stage.setScene(scene);
			stage.show();
		}
		catch (Exception e) { e.printStackTrace();	}
	}
	
	static final String MG_RESOURCE = "../tables/MultiGeneList.fxml";
    static final String MG_STYLE = "../tables/genelistStyles.css";
	public void doNewMultiGeneList() 
	{
		try
		{
			Stage stage = new Stage();
			URL res = getClass().getResource(MG_RESOURCE);
		    FXMLLoader geneListLoader = new FXMLLoader(res);
	        Scene scene = new Scene(geneListLoader.load());
		    MultiGeneListController glc = (MultiGeneListController) geneListLoader.getController();   
		    glc.setParentController(this);
			scene.getStylesheets().add(getClass().getResource(MG_STYLE).toExternalForm());
	        stage.setTitle("Multi Gene List Window");
	        stage.setX(20);
			stage.setWidth(800);
			stage.setHeight(650);
			stage.setScene(scene);
			stage.show();
		}
		catch (Exception e) { e.printStackTrace();	}
	}
	
		
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
	void addGenes(List<Gene> inList)
	{
		model.addGenes(inList);
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
			if (prop != null && model.findGene(""+prop) == null)
				model.addGene(new Gene(""+prop));
		}
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
			VNode anchorStack = anchor.getStack();
			Shape shap = anchorStack.getFigure();
			if (shap instanceof Circle)
				System.out.println(String.format("(%.2f, %.2f)", ((Circle)shap).getCenterX(), ((Circle)shap).getCenterY()));
			pasteboard.add(anchorStack);
		}
		e.connect();
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
					Shape shape = node.getStack().getFigure();
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
			Shape shape = node.getStack().getFigure();
			if (shape != null)
			{	
				shape.setFill(Color.WHITE);
				node.getStack().getTextField().setStyle("-fx-text-fill: black");
			}
		}
	}
	
	public void setGeneList(GeneListRecord rec) 	
	{ 		
		if (rec == null) return;			// TODO clear gene lists?
		geneListTable.getItems().addAll(rec.getGeneList());			// ADD UNIQUE
		model.setGeneList(rec, rec.getGeneList());
		model.fillIdlist();
	}
	public void viewPathway(PathwayRecord rec)		
	{ 
		String url = rec.getUrl();
//		Thread thread = new Thread() {
		String result = StringUtil.callURL(url, false);   // TODO threaded
		viewPathway(result);
	}
	public void viewPathwayAsImage(PathwayRecord rec)		
	{ 
		String url = rec.getPngUrl();
//		Thread thread = new Thread() {
		String result = StringUtil.callURL(url, false);   // TODO threaded
		String png64  =StringUtil.readTag( result, "ns1:data");
		Decoder decoder = Base64.getDecoder();
		byte[] imageBytes  = decoder.decode(png64);
		InputStream stream = new ByteArrayInputStream(imageBytes);
		WritableImage wimg;
		BufferedImage bufferedimage = null;
		try {
			 bufferedimage = ImageIO.read(stream);
			 wimg = new WritableImage(bufferedimage.getWidth(),  bufferedimage.getHeight());
			wimg = SwingFXUtils.toFXImage(bufferedimage, wimg);
			ImageView view = new ImageView(wimg);
			view.prefWidth(300);
			view.minWidth(300);
			view.prefHeight(300);
			view.minHeight(300);
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setGraphic(view);
			alert.showAndWait();		 
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private void viewPathway(String result)		
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
	public void getPathwayAt(Integer o) {
		
		
	}
}
