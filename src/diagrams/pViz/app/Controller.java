package diagrams.pViz.app;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import animation.BorderPaneAnimator;
import diagrams.pViz.app.Controller.DrawActionCell;
import diagrams.pViz.dialogs.LegendDialog;
import diagrams.pViz.gpml.Anchor;
import diagrams.pViz.gpml.GPML;
import diagrams.pViz.model.Edge;
import diagrams.pViz.model.MNode;
import diagrams.pViz.model.Model;
import diagrams.pViz.tables.GeneListController;
import diagrams.pViz.tables.GeneListTable;
import diagrams.pViz.tables.LegendRecord;
import diagrams.pViz.tables.PathwayController;
import diagrams.pViz.view.Inspector;
import diagrams.pViz.view.Layer;
import diagrams.pViz.view.LayerController;
import diagrams.pViz.view.LayerRecord;
import diagrams.pViz.view.Pasteboard;
import diagrams.pViz.view.VNode;
import gui.Action;
import gui.Action.ActionType;
import gui.Borders;
import gui.DropUtil;
import gui.UndoStack;
import icon.FontAwesomeIcons;
import icon.GlyphIcon;
import icon.GlyphIcons;
import icon.GlyphsDude;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.event.EventTarget;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableView;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
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
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Pair;
import model.AttributeMap;
import model.IController;
import model.bio.BiopaxRecord;
import model.bio.Gene;
import model.bio.GeneListRecord;
import model.bio.Species;
import table.referenceList.ReferenceController;
import util.FileUtil;
import util.StringUtil;


public class Controller implements Initializable, IController 
{

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
	private PathwayController pathwayController = null;
	@FXML private BorderPane container;			// root of fxml	
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
	@FXML private BorderPane 	loadContainer;

	@Override public void resetTableColumns() {	}
	@Override public void reorderColumns(int a, int b) {	}
	
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
	@FXML private void selectProtein()	{ 	pasteboard.selectByType("Protein");	}
	@FXML private void selectGene()		{ pasteboard.selectByType("Gene");		}
	@FXML private void selectMetabolite(){ 	pasteboard.selectByType("Metabolite");		}
	@FXML private void selectRna()		{ 	pasteboard.selectByType("Rna");		}
	@FXML private void selectLabels()	{ 	pasteboard.selectByType("Label");		}
	@FXML private void selectShapes()	{ 	pasteboard.selectByType("Shape");		}
	@FXML private void selectEdges()	{ 	pasteboard.selectByType("Edge");		}
	@FXML private void moveToLayer()	{ 	System.err.println("moveToLayer called, not its subitem");		}
	@FXML private void addKeyFrame()	{ 	inspector.addKeyFrame();	}

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
	static String jupy = "https://nbviewer.jupyter.org/github/CamDavidsonPilon/Probabilistic-Programming-and-Bayesian-Methods-for-Hackers/blob/master/Chapter1_Introduction/Ch1_Introduction_PyMC3.ipynb";
	@FXML private void showJupyter()	
	{ 	
		try 
		{
			Stage browser = new Stage();
		    WebView webview = new WebView();
		    browser.setScene(new Scene(webview, 800, 1200));
			final WebEngine webEngine = webview.getEngine();
			webEngine.getLoadWorker().stateProperty().addListener((x,y,newState) -> {
			     if (newState == Worker.State.SUCCEEDED) 
			         browser.setTitle(webEngine.getLocation());   }  );
			 webEngine.load(jupy);
			 browser.show();
		}
		catch(Exception e) { e.printStackTrace();	}
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
	
	@FXML private void resetEdgeTable()	{		model.resetEdgeTable();	}
	@FXML private void dumpEdgeTable()	{		model.dumpEdgeTable();	}
	@FXML private void dumpViewHierarchy()	{	model.dumpViewHierarchy();	}
	@FXML private void dumpNodeTable()	{		model.dumpNodeTable();	}
	// **-------------------------------------------------------------------------------
	@FXML public  void group()			{ 	undoStack.push(ActionType.Group);	getSelectionManager().doGroup();  }
	@FXML public  void ungroup()		{ 	undoStack.push(ActionType.Ungroup);	getSelectionManager().ungroup(); }
	@FXML public  void toFront()		{	undoStack.push(ActionType.Reorder);	getSelectionManager().toFront(); 	}
	@FXML public  void toBack()			{	undoStack.push(ActionType.Reorder);	getSelectionManager().toBack();  pasteboard.restoreBackgroundOrder();  	}
	@FXML public  void toLayer()		
	{	
//		String layername = "Content";
//		undoStack.push(ActionType.Reorder);	  
//		getSelectionManager().setLayer(layername);
//		pasteboard.restoreBackgroundOrder();  	
	}
	public List<LayerRecord> getLayers()	{	return layerController.getLayers();	}
	public void moveSelectionToLayer(String layername)
	{
		undoStack.push(ActionType.Reorder);	  
		getSelectionManager().setLayer(layername);
		pasteboard.restoreBackgroundOrder();  	
	}
	@FXML private void hideAnchors()	{ 	model.setAnchorVisibility(false);	}
	@FXML private void showAnchors()	{ 	model.setAnchorVisibility(true);	}

	// **-------------------------------------------------------------------------------
	@FXML public  void browsePathways()		
	{
			App.browsePathways(this);
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
				add(0, e, getActiveLayerName());
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
	@FXML private void showInspector() {
		bottomSideBarButton.fire();
	}
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
			DropUtil.makeFileDropPane(geneListTable, e -> { geneListTable.doDrag(e); });
			east.getChildren().add(geneListTable);
		}
		setupPalette();
		setupListviews();
		new BorderPaneAnimator(container, leftSideBarButton, Side.LEFT, false, 90);
		new BorderPaneAnimator(container, rightSideBarButton, Side.RIGHT, false, 300);
		new BorderPaneAnimator(container, bottomSideBarButton, Side.BOTTOM, false, 150);
		
		rightSideBarButton.fire();
		bottomSideBarButton.fire();
		
		pasteboard.makeGrid(toggleGridButton, scrollPane);
		
		Pair<FXMLLoader, Stage> loader = App.getInstance().makeLoader();
		
		Pair<FXMLLoader, Stage> pair = App.buildStage("Layers", "../view/Layers.fxml", 250, 300);
		layerController = (LayerController) pair.getKey().getController();
		layerController.setParentController(this);
		Stage stage = pair.getValue();
		layerController.setStage(stage);
		stage.hide();
		stage.setOnCloseRequest(ev -> { stage.hide(); } );
		stage.setAlwaysOnTop(true);

		loader.getValue().toFront();
		boolean startWithShapes = false;
		if (startWithShapes) test1();
		
        Thread postponed =  new Thread(() -> Platform.runLater(() -> {  loader.getValue().toFront();	} )  );
        postponed.start();
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

	public void doNewGeneList()	{		App.doNewGeneList(null);	}
	
	
	public void getInfo(DataFormat mimeType, String idx, String colname, MouseEvent ev) {
		try 
		{
			int index = StringUtil.toInteger(idx);
			if (mimeType == GeneListController.GENE_MIME_TYPE)
			 {
			   String rowName = "";
			   if (index >=0 && model.getNGenes() > index)
				   	rowName = model.getGenes().get(index).getId();
//			   showGeneInfo(rowName);
			   return;
		  }
		  if (mimeType == PathwayController.PATHWAY_MIME_TYPE)
		   {
			   if (pathwayController != null)
			   {
				   pathwayController.viewPathwayByIndex(idx);
				   return;
			   }
			}
		  
		  System.out.println("getInfo wasn't matched: " + mimeType.toString());					//TODO
		  //
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
	//-----------------------------------------------------------------------
	@FXML private void doNewMultiGeneList()  { 	App.doNewMultiGeneList(); 	}
	
	//-----------------------------------------------------------------------
	@FXML private void addLegend()
	{
		LegendRecord.makeLegend("Legend", "", true, true, false, model, this, true);
	}
	LayerController layerController;
	@FXML private void showLayers()
	{
		if (layerController.getStage().isShowing())
			layerController.getStage().hide();
		else layerController.getStage().show();
	}
	public LayerRecord getLayerRecord(String name)
	{
		return layerController.getLayer(name);
	}
	@FXML private void addLayer()
	{
		if (layerController != null)
			layerController.addLayer();
	}
	@FXML private void removeLayer()
	{
		if (layerController != null)
			layerController.removeLayer();
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
	
		IController.setGraphic(leftSideBarButton, FontAwesomeIcons.ARROW_CIRCLE_O_RIGHT);
		IController.setGraphic(rightSideBarButton, FontAwesomeIcons.ARROW_CIRCLE_O_LEFT);
		IController.setGraphic(bottomSideBarButton, FontAwesomeIcons.ARROW_CIRCLE_DOWN);
//		setGraphic(toggleRulerButton, FontAwesomeIcons.BARS);
		IController.setGraphic(toggleGridButton, FontAwesomeIcons.TH);
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
//		pasteboard.getChildren().addAll(pasteboard.getGrid().getNodes());
		model.setState(s);
	}
	//-----------------------------------------------------------------------------
	public void addXMLDoc(org.w3c.dom.Document doc)
	{
		new GPML(model, getActiveLayerName()).read(doc);
		updateTables();
//		Window window = refTable.getScene().getWindow();
//        if (window instanceof Stage) 
//        	((Stage) window).setTitle(model.getTitle());	
        Thread postponed =  new Thread(() -> Platform.runLater(() -> {  getModel().resetEdgeTable();	} )  );
        postponed.start();  
        hideAnchors();

//		addModelToPasteboard();

	}
			
	public Layer getActiveLayer() 		{		return getLayerRecord(getActiveLayerName()).getLayer();	}
	public String getActiveLayerName() 	{		return pasteboard.activeLayerName();	}
	public void updateTables() {
//		refTable.getItems().addAll(model.getReferences());
		geneListTable.populateTable(model, model.getGenes());
	}

	//-----------------------------------------------------------------------------
	
//		
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
//	void addGenes(List<Gene> inList) 	{ 	model.addGenes(inList); }
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
	}
	public void add(int idx, Edge e, String layername)							
	{		
		if (e == null) return;
		e.getAttributes().put("Layer", layername);
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
//		e.connect();
	}
//	public void add(Edge e)		{	pasteboard.add(0,e);	}
	public void add(VNode n)							
	{		
		pasteboard.add(n);	
		MNode modelNode = n.modelNode();
		if (n.isAnchor()) return;
		if (n.isLabel()) return;
		Object prop  = modelNode.getAttributeMap().get("TextLabel");
		if (prop != null && model.findGene(""+prop) == null)
		{
			model.addGene(new Gene(model.getGeneList(), ""+prop));
			model.addResource(modelNode);
		}
	}
	public void add(int index, VNode n)		 {	 pasteboard.add(index, n); 	}
	public void addAll(List<VNode> n)	{		pasteboard.addAllVNodes(n);	}
	public void addAll(VNode... n)		{		pasteboard.addAllVNodes(n);	}
	
	public void remove(Node n)						
	{		
		pasteboard.getChildren().remove(n);	
	}
	public void remove(VNode n)						
	{		
		getDrawModel().removeNode(n);
		pasteboard.getChildren().remove(n);	
	}
	// **-------------------------------------------------------------------------------
	public String getState()					{ 	return model.saveState();  }
	public void reportStatus(String string)	 {}	 //		status1.setText(string);	
	
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

	public void timeseries(TableView content)	{	}
	public void hiliteByReference(String ref) {
//		for (BiopaxRecord biopax : model.getReferences())
//			if (ref.equals(biopax.getRdfid()))
//				refTable.getSelectionModel().select(biopax);
	}
	public void openByReference(String ref) {
		for (BiopaxRecord biopax :  model.getReferences())
			if (ref.equals(biopax.getRdfid()))
			{
				String pmid = biopax.getId();
				if (StringUtil.hasText(pmid))
					ReferenceController.showPMIDInfo(pmid);
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
	public void setActiveLayer(String s) {		pasteboard.setActiveLayer(s);	}
	public void addGeneList(File f, double dropX, double dropY) {
		GeneListRecord rec = new GeneListRecord(f);
		geneListTable.getItems().addAll(rec.getGeneList());			// ADD UNIQUE
		
		double COLWID = 90;
		double ROWHGHT = 40;
		int NCOLS = (int) Math.ceil(Math.sqrt(rec.getRowCount()));
		
		for (int i=0; i< rec.getGeneList().size(); i++)
		{
			Gene g = rec.getGeneList().get(i);
			AttributeMap attrs = new AttributeMap(g);
			attrs.put("CenterX", "" + (dropX + (i % NCOLS) * COLWID));
			attrs.put("CenterY", "" + (dropY + (i / NCOLS) * ROWHGHT));
			attrs.put("Width", "80");
			attrs.put("Height", "30");
			attrs.put("TextLabel", g.getName());
			MNode node = new MNode(attrs, getModel());
			add(node.getStack());			
		}

	}
	// **-------------------------------------------------------------------------------
	boolean isGeneList(File f)
	{
		List<String> strs = FileUtil.readFileIntoStringList(f.getAbsolutePath());
		int sz = strs.size();
		if (sz < 10) return false;
		String DELIM = "\t";
		String firstRow = strs.get(0);
		int nCols = firstRow.split(DELIM).length;
		for (int i = 1; i< sz; i++)
		{
			String row = strs.get(i);
			if (row.split(DELIM).length != nCols)
				return false;
		}
		return true;
	}

}
