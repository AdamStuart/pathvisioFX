package diagrams.pViz.view;

import java.net.URL;
import java.util.ResourceBundle;

import diagrams.pViz.app.Controller;
import diagrams.pViz.app.Selection;
import diagrams.pViz.app.Tool;
import diagrams.pViz.model.edges.EdgeType;
import diagrams.pViz.util.ArrowType;
import icon.FontAwesomeIcons;
import icon.GlyphIcon;
import icon.GlyphIcons;
import icon.GlyphsDude;
import javafx.event.EventTarget;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

//-------------------------------------------------------------
public class PaletteController implements Initializable {
	Pasteboard pasteboard;
	Controller controller;
	Selection selection;
	public VBox getView()	{ return palette;	}
	
	// **------------------------------------------------------------------------------
	@Override public void initialize(URL location, ResourceBundle resources)
	{
		paletteGroup = new ToggleGroup();
		paletteGroup.getToggles().addAll(arrow, rectangle, text, gene, metabolite, pathway, protein, rna, arrow1, arrow2, arrow3, arrow4, arrow5, arrow6);
		lineTypeGroup = new ToggleGroup();
		lineTypeGroup.getToggles().addAll(straight, curved, elbowed, segmented);
		straight.setSelected(true );
//		arrowGroup = new ToggleGroup();	
//		arrowGroup.getToggles().addAll();
//		arrow1.setSelected(true);
//		EventHandler<? super MouseEvent> dragHandler = new EventHandler{
//				
//		};
//		arrow1.setOnDragDetected(dragHandler );
		setGraphic(arrow, Tool.Arrow, FontAwesomeIcons.LOCATION_ARROW);
		setGraphic(rectangle, Tool.Rectangle, FontAwesomeIcons.SQUARE);
//		setGraphic(polyline, Tool.Polyline, FontAwesomeIcons.PENCIL);
	}

	public void setParentController(Controller c)	
	{
		controller = c;
		selection = controller.getSelectionManager();
		pasteboard = controller.getPasteboard();
	}
	// **-------------------------------------------------------------------------------
	private void setGraphic(ToggleButton b, Tool t, GlyphIcons i)
	{
		b.setGraphic(GlyphsDude.createIcon(i, GlyphIcon.DEFAULT_ICON_SIZE));
		b.setId(t.name());
	}
	
	// **-------------------------------------------------------------------------------

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
	        cc.putString("SHAPE:" + b.getText());
	        db.setContent(cc);
		}
	}	
	//------------------------------------------
	Tool curTool = Tool.Arrow;
	public Tool getTool() {		return curTool;	}
	boolean sticky = false;
	
	public void setTool(Tool tool)
	{
		if (getTool() == tool) 
		{ 
			sticky = true;
			return;	
		}
		if (getTool() == Tool.Arrow) sticky = false;
		curTool = tool;
		
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
	}	
	
	public void resetTool()		
	{		
		if (sticky) return;
		setArrow();	
	}	
	//-------------------------------------------------------------
	@FXML private void editPalette() 	// TODO raw implementation
	{
		if (allInteractions == null)  return;			// FXML error
		paletteEditing = !paletteEditing;
		boolean startEdit = paletteEditing;
		
		palette.setMinWidth(paletteEditing ? 300 : 150);		// TODO animate
		
		if (startEdit)
		{
			unusedInteractions.getChildren().clear();
			ToggleButton cleaves = new ToggleButton("Cleaves");
			ToggleButton branchLeft = new ToggleButton("Branches Left");
			ToggleButton branchRight = new ToggleButton("Branches Right");
			cleaves.setPrefWidth(120);
			branchLeft.setPrefWidth(120);
			branchRight.setPrefWidth(120);
//			cleaves.setOnDragDetected(event -> { dragInteraction(event); });
//			branchLeft.setOnDragDetected(event -> { dragInteraction(event); });
//			branchRight.setOnDragDetected(event -> { dragInteraction(event); });
			unusedInteractions.getChildren().addAll(cleaves,branchLeft,branchRight  );
			allInteractions.getChildren().add(unusedInteractions);
		}
		else
			allInteractions.getChildren().remove(unusedInteractions);
	} 
	boolean paletteEditing = false;
	VBox unusedInteractions = new VBox(4);
	//------------------------------------------
	@FXML private void dragInteraction(MouseEvent e)		
	{
		if (!paletteEditing) return;
		EventTarget targ  = e.getTarget();
		ButtonBase b = null;
		if (targ instanceof Text)
		{
			Text text = (Text) targ;
			Node parent = text.getParent();
			if (parent instanceof ButtonBase)
				b = (ButtonBase) parent;
		}
		else if (targ instanceof Button)
			b = (ButtonBase) targ;
		else if (targ instanceof ToggleButton)
			b = (ToggleButton) targ;
			
		if (b != null)
		{
			System.out.println("drag " + b.getText());
	        Dragboard db = b.startDragAndDrop(TransferMode.COPY);
	        db.setDragView(b.snapshot(null, null), e.getX(), e.getY());
	        ClipboardContent cc = new ClipboardContent();
	        cc.putString("SHAPE:" + b.getText());
	        db.setContent(cc);
	        Parent p = b.getParent();
	        if (p instanceof VBox)
	        {
	        	int idx = ((VBox)p).getChildren().indexOf(b);
	        	if (idx >= 0)
	        		((VBox)p).getChildren().remove(idx);
	        }
		}
	}

	// **-------------------------------------------------------------------------------
	private ToggleGroup paletteGroup;
	private ToggleGroup lineTypeGroup;
//	private ToggleGroup arrowGroup;
	public ToggleGroup getToolGroup()			{ 	return paletteGroup;	}
//	public ToggleGroup getArrowGroup()			{ 	return arrowGroup;	}
	public ToggleGroup getLineTypeGroup()		{ 	return lineTypeGroup;	}

// **-------------------------------------------------------------------------------
	// Tool palette

	
	@FXML private ToggleButton gene;	
	@FXML private ToggleButton metabolite;	
	@FXML private ToggleButton protein;		
	@FXML private ToggleButton pathway;		
	@FXML private ToggleButton rna;	

	@FXML private void setArrow()		{ setTool(Tool.Arrow);	}
	@FXML private void setRectangle()	{ setTool(Tool.Rectangle);}	// TODO capture double click for stickiness
	@FXML private void setText()		{ setTool(Tool.Text);}
	@FXML private void setOval()		{ setTool(Tool.Circle);		}
	@FXML private void setPolygon()		{ setTool(Tool.Polygon);	}
	@FXML private void setPolyline()	{ setTool(Tool.Polyline);	}
	@FXML private void setLine()		{ setTool(Tool.Line);	}
	@FXML private void setShape1()		{ setTool(Tool.Shape1);	}
	@FXML private void setBrace()		{ setTool(Tool.Brace);	}
	@FXML private void setGene()		{ setTool(Tool.GeneProduct);	}
	@FXML private void setMetabolite()	{ setTool(Tool.Metabolite);	}
	@FXML private void setProtein()		{ setTool(Tool.Protein);	}
	@FXML private void setRNA()			{ setTool(Tool.Rna);	}
	@FXML private void setPathway()		{ setTool(Tool.Pathway);	}

	// **-------------------------------------------------------------------------------
	@FXML public  void zoomIn()			{	pasteboard.zoomIn(); 	}
	@FXML public  void zoomOut()		{	pasteboard.zoomOut();  pasteboard.restoreBackgroundOrder();  	}
	// **------------------------------------------------------------------------------

	public ArrowType getActiveArrowType() {	return activeArrowType;	}
	public String getActiveLineType() {		return activeLineType;	}

	String activeLineType = "Straight";
	ArrowType activeArrowType = ArrowType.mimactivation;

	@FXML private VBox palette;			// root of Palette.fxml
	@FXML private VBox tools;	

	@FXML private ToggleButton arrow;			// selection
	@FXML private ToggleButton rectangle;
	@FXML private ToggleButton circle;
	@FXML private ToggleButton text;
	@FXML private ToggleButton polygon;
	@FXML private ToggleButton polyline;
	@FXML private ToggleButton line;
	@FXML private ToggleButton shape1;
//		@FXML private ToggleButton shape2;
	@FXML private ToggleButton straight;
	@FXML private ToggleButton curved;
	@FXML private ToggleButton elbowed;
	@FXML private ToggleButton segmented;
		
	@FXML private ToggleButton arrow1;
	@FXML private ToggleButton arrow2;
	@FXML private ToggleButton arrow3;
	@FXML private ToggleButton arrow4;
	@FXML private ToggleButton arrow5;
	@FXML private ToggleButton arrow6;

	@FXML private void setStraight()	{		activeLineType = "Straight";	}
	@FXML private void setCurved()		{		activeLineType = "Curved";	}
	@FXML private void setElbowed()		{		activeLineType = "Elbow";	}
	@FXML private void setSegmented()	{		activeLineType = "Segmented";	}
	public EdgeType getCurrentLineBend() { return EdgeType.lookup(activeLineType);		}
	@FXML private void setArrow1()		{		activeArrowType = ArrowType.interacts;	setTool(Tool.ArrowInteracts);}	// TODO get type from id
	@FXML private void setArrow2()		{		activeArrowType = ArrowType.mimconversion;	setTool(Tool.ArrowConverts);}
	@FXML private void setArrow3()		{		activeArrowType = ArrowType.miminhibition;	setTool(Tool.ArrowInhibits);}
	@FXML private void setArrow4()		{		activeArrowType = ArrowType.mimcatalysis;	setTool(Tool.ArrowCatalyzes);}
	@FXML private void setArrow5()		{		activeArrowType = ArrowType.stimulates;	setTool(Tool.ArrowStimulates);}
	@FXML private void setArrow6()		{		activeArrowType = ArrowType.mimbinding;	setTool(Tool.ArrowBinds);}
	@FXML private VBox allInteractions;

	
	//-------------------------------------------------------------


}
