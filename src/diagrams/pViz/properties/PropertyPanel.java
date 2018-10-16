package diagrams.pViz.properties;

import java.net.URL;
import java.util.ResourceBundle;

import diagrams.pViz.app.Controller;
import diagrams.pViz.app.Selection;
import diagrams.pViz.view.VNode;
import javafx.fxml.Initializable;
import javafx.scene.input.DragEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import model.AttributeMap;

public class PropertyPanel extends AnchorPane implements Initializable{
	Controller controller;
	public PropertyPanel(Controller c)
	{
		controller = c;
	}
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// TODO Auto-generated method stub
		
	}
	public void doDrag(DragEvent e) {
		// TODO Auto-generated method stub
		
	}
	public void syncProperties() {
		Selection selection = controller.getSelectionManager();
		if(selection.count() == 1)
		{
			VNode firstNode = selection.first();
			
			double x = firstNode.getLayoutX();
			double y = firstNode.getLayoutY();
			double width = firstNode.getWidth();
			double height = firstNode.getHeight();
			setRect(x, y, width, height);
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

			}
			
		}
	}
	void setRect(double x, double y, double w, double h)
	{
		
	}
}
