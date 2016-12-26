package diagrams.pViz.view;

import java.net.URL;
import java.util.ResourceBundle;

import diagrams.pViz.app.Action.ActionType;
import diagrams.pViz.app.Controller;
import diagrams.pViz.app.Selection;
import gui.Borders;
import icon.FontAwesomeIcons;
import icon.GlyphIcon;
import icon.GlyphsDude;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Shape;
import model.AttributeMap;

public class Inspector extends HBox implements Initializable {
	Pasteboard pasteboard;
	Controller controller;
	Selection selection;
	
	@FXML private HBox inspectTop;

	@FXML private ColorPicker fillColor;
	@FXML private ColorPicker lineColor;
	@FXML private Label strokeLabel;
	@FXML private Label fillLabel;
	@FXML private Slider weight;
	@FXML private Slider scale;
	@FXML private Slider rotation;
	@FXML private Slider opacity;
//	@FXML private Label status1;
//	@FXML private Label status2;
//	@FXML private Label status3;
	
	@Override public void initialize(URL location, ResourceBundle resources)
	{
		for (Node n : getChildren())
			System.out.println(n.toString());
		Parent p = strokeLabel.getParent();
		strokeLabel.setGraphic(GlyphsDude.createIcon(FontAwesomeIcons.PENCIL, GlyphIcon.DEFAULT_ICON_SIZE));
		fillLabel.setGraphic(GlyphsDude.createIcon(FontAwesomeIcons.CAR, GlyphIcon.DEFAULT_ICON_SIZE));
		fillLabel.setText("");		fillLabel.setMinWidth(30);		fillLabel.setAlignment(Pos.BASELINE_CENTER);
		strokeLabel.setText(""); 	strokeLabel.setMinWidth(30);	strokeLabel.setAlignment(Pos.BASELINE_CENTER);
		
		fillColor.setOnAction(evt -> apply(true, fillColor));
		lineColor.addEventHandler(ActionEvent.ACTION, evt -> apply(true, lineColor));

		scale.valueProperty().addListener((ov, old, val) ->   {  	pasteboard.setZoom(scale.getValue());        });	
		weight.valueProperty().addListener((ov, old, val) ->    {   apply(false, weight);   });	
		rotation.valueProperty().addListener((ov, old, val) ->  {   apply(false, rotation);  });	
		opacity.valueProperty().addListener((ov, old, val) ->  {   apply(false, opacity);  });	
		
		// sliders don't record undoable events (because they make so many) so snapshot the state on mousePressed
		EventHandler<Event> evH = event -> {	controller.getUndoStack().push(ActionType.Property);  };
//		opacity.setOnMousePressed(evH); 
		weight.setOnMousePressed(evH);
		rotation.setOnMousePressed(evH);
		opacity.setOnMousePressed(evH);
		setMinHeight(100); setPrefHeight(100); setMinWidth(400); 
		getChildren().add (inspectTop);		//new Button("ANYTHING")
//		getChildren().addAll( fillLabel, fillColor, strokeLabel, lineColor);
//		getChildren().addAll( scale, weight, rotation);
//		setBorder(Borders.redBorder);
	}
	// **-------------------------------------------------------------------------------
	public void setParentController(Controller c)	
	{
		controller = c;
		selection = controller.getSelectionManager();
		pasteboard = controller.getPasteboard();
	}
	
	// **------------------------------------------------------------------------------

	// **------------------------------------------------------------------------------

	private String getStyleSettings(Control src)
	{
		Color fill = fillColor.getValue();
		Color stroke = lineColor.getValue();
		if (fill == null) fill = Color.GRAY;
		if (stroke == null) stroke = Color.DARKGRAY;
		String fillHex = "-fx-fill: #" + fill.toString().substring(2, 8) + ";\n";
		String strokeHex = "-fx-stroke: #" + stroke.toString().substring(2, 8) + ";\n";
		String wtStr = String.format("-fx-stroke-width: %.1f;\n", weight.getValue());
		String opacStr = String.format("-fx-opacity: %.2f;\n", opacity.getValue());
		String rotStr = String.format("-fx-rotate: %.1f;\n", rotation.getValue());

		StringBuilder buff = new StringBuilder();
		buff.append(fillHex).append(strokeHex).append(wtStr).append(opacStr).append(rotStr);
		return buff.toString();
	}
	// **-------------------------------------------------------------------------------
	private void apply(boolean undoable, Control src)							
	{ 	 			
		if (undoable) 
			controller.getUndoStack().push(ActionType.Property); 
		selection.applyStyle(getStyleSettings(src));	
	}

	public void syncInspector()		//Inspector
	{
		if(selection.count() == 1)
		{
			VNode firstNode = selection.first();
			Shape n = firstNode.getFigure();
			if  (n != null)
			{
				Paint fill = n.getFill();
				Paint stroke = n.getStroke();
				double wt = n.getStrokeWidth();
				double opac = n.getOpacity();
				double rot = n.getRotate();
				
				fillColor.setValue((Color) fill);
				lineColor.setValue((Color) stroke);
				weight.setValue(wt);
				opacity.setValue(100 * opac);
				rotation.setValue(rot);
			}
			if (firstNode instanceof StackPane)
			{
				StackPane stack = (StackPane) firstNode;
				String style = stack.getStyle();
				AttributeMap attr = new AttributeMap(style, true);
				Color fill = attr.getColor("-fx-background-color");
				Color stroke = attr.getColor("-fx-border-color");
				double opac = attr.getDouble("-fx-opacity", 0.5);
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
				opacity.setValue(100 * opac);
			}
			
		}
	}
	public void start() {
// unused		
	}
}
