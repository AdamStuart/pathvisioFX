package diagrams.pViz.view;

import gui.Borders;
import javafx.animation.Animation;
import javafx.animation.Transition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class MinWidthAnimator
{
	Animation hider, shower;
	double expandedWidth = 200;
	double narrowWidth = 60;
	Duration d = Duration.millis(350);
	HBox container;
	VBox controlBox = new VBox(2);
	Label colLabel = new Label("Column:");
	Label mapLabel = new Label("Mapping:");
	ChoiceBox<String> colChooser = new ChoiceBox<String>();			// TODO col list
	ChoiceBox<String> mapChooser = new ChoiceBox<String>();			// TODO using mapping enum
	Inspector controller; 
	Slider slider;
// TODO have a hover animation to collapse/expand if no button is sent in
	public MinWidthAnimator(Inspector ctrlr, Pane pane, HBox hbox, Slider inSlider)
	{
		controller = ctrlr;
		container = hbox;
		slider = inSlider;
//		container.setOnMouseClicked(e -> { if (e.getClickCount() == 2) controller.showSettings(container); }); 
		pane.setBorder(Borders.thinEtchedBorder);
		controlBox.getChildren().addAll(colLabel, colChooser, mapLabel, mapChooser);
		container.getChildren().add( controlBox);
//		container.setBorder(Borders.greenBorder);
		controlBox.setVisible(false);
//		pane.getChildren().add(container);
		colChooser.getItems().addAll("Name", "GraphId", "DataValue");
		mapChooser.getItems().addAll("Continous", "Discrete", "Passthrough");
        container.setPrefWidth(narrowWidth);
        container.setMaxWidth(narrowWidth);
        container.setMinWidth(narrowWidth);
		pane.setOnMouseClicked(e -> { if (e.getClickCount() == 2) toggleWidth(pane); }); 
		hider = new Transition() { 
        { setCycleDuration(d); }
        
        protected void interpolate(double frac) 
        {
      	  controlBox.setVisible(false);
            final double curWidth = narrowWidth + (expandedWidth - narrowWidth) * (1 - frac);
            container.setPrefWidth(curWidth);
            container.setMaxWidth(curWidth);
            container.setMinWidth(curWidth);
        }
      };
      hider.onFinishedProperty().set(new EventHandler<ActionEvent>() {
          @Override public void handle(ActionEvent actionEvent) {
        	  container.getStyleClass().remove("wide");
        	  container.getStyleClass().add("narrow");
//        	  container.getChildren().remove(controlBox);
        	  controlBox.setVisible(false);
          }
        });
      // create an animation to increase our width.
      shower = new Transition() {
        { setCycleDuration(d); }
        protected void interpolate(double frac) {
          final double curWidth = narrowWidth + (expandedWidth - narrowWidth) * frac;
          
          container.setPrefWidth(curWidth);  
          container.setMaxWidth(curWidth); 
          container.setMinWidth(curWidth);
        }
      };
      shower.onFinishedProperty().set(new EventHandler<ActionEvent>() {
        @Override public void handle(ActionEvent actionEvent) {
        	container.getStyleClass().add("wide");
        	container.getStyleClass().remove("narrow");
//        	controlBox.getChildren().clear();
//        	controlBox.getChildren().addAll(colLabel, colChooser, mapLabel, mapChooser);
//            String styleString = "-fx-background-color: rgba(0.1, 0.1, 0.1, 0.5);";
//            controlBox.setStyle(styleString);
            controlBox.setVisible(true);
      }
      });			
	}

	public void toggleWidth(Pane pane)
	{
		if (shower.statusProperty().get() == Animation.Status.STOPPED
				&& hider.statusProperty().get() == Animation.Status.STOPPED)
		{
			if (pane.getWidth() >= expandedWidth)
				hider.play();
			else
				shower.play();
		}
	}

}
