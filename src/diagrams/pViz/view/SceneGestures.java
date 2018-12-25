package diagrams.pViz.view;

import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.transform.NonInvertibleTransformException;

/**
 * Listeners for making the scene's canvas draggable and zoomable
 */
public class SceneGestures {

    private static final double MAX_SCALE = 10.0d;
    private static final double MIN_SCALE = .5d;

    private DragContext sceneDragContext = new DragContext();

    PanningCanvas canvas;

    public SceneGestures(PanningCanvas canvas) {
        this.canvas = canvas;
    }

    public EventHandler<MouseEvent> getOnMousePressedEventHandler() {  return onMousePressedEventHandler;   }
    public EventHandler<MouseEvent> getOnMouseDraggedEventHandler() {  return onMouseDraggedEventHandler;     }
    public EventHandler<ScrollEvent> getOnScrollEventHandler() 		{  return onScrollEventHandler;    }

    private EventHandler<MouseEvent> onMousePressedEventHandler = new EventHandler<MouseEvent>() {

        public void handle(MouseEvent event) {
        }

    };

    private EventHandler<MouseEvent> onMouseDraggedEventHandler = new EventHandler<MouseEvent>() {
        public void handle(MouseEvent event) {
        }
    };

    /**
     * Mouse wheel handler: zoom to pivot point
     */
    private EventHandler<ScrollEvent> onScrollEventHandler = new EventHandler<ScrollEvent>() {

        @Override
        public void handle(ScrollEvent event) {

            double delta = 1.1;

            double scale = canvas.getScale(); // currently we only use Y, same value is used for X
            double oldScale = scale;

            if (event.getDeltaY() < 0)
                scale /= delta;
            else
                scale *= delta;

            scale = pin( scale, MIN_SCALE, MAX_SCALE);
            double f = (scale / oldScale)-1;
            
            
            ScrollPane scrollPane = canvas.getController().getScrollPane();
            double scrollHvalue = scrollPane.getHvalue();
            double scrollVvalue = scrollPane.getVvalue();
            
//            double dx = (event.getSceneX() - (canvas.getBoundsInParent().getWidth()/2 + canvas.getBoundsInParent().getMinX()));
//            double dy = (event.getSceneY() - (canvas.getBoundsInParent().getHeight()/2 + canvas.getBoundsInParent().getMinY()));
            
            
            try {
            	Point2D pt = new Point2D(event.getSceneX(), event.getSceneY());
            	
				Point2D ptInCanvas = canvas.getLocalToSceneTransform().inverseTransform(pt);
				
				double canvasHvalue = ptInCanvas.getX() / canvas.getWidth();
				double canvasVvalue = ptInCanvas.getY() / canvas.getHeight();
				
//				Point2D ptInScrollPane = scrollPane.getLocalToSceneTransform().inverseTransform(pt);
				
				scrollHvalue += (canvasHvalue - scrollHvalue) * f;
				scrollVvalue += (canvasVvalue - scrollVvalue) * f;
				
				
//				System.out.println("Mouse x within canvas: " + pointInCanvas.getX());
			} catch (NonInvertibleTransformException e) {
				System.err.println("Error in " + getClass().getName() + ": " + e);
			}
            
            
            canvas.setScale( scale);
            scrollPane.setHvalue(scrollHvalue);
            scrollPane.setVvalue(scrollVvalue);
            
//System.out.println(scale);
            // note: pivot value must be untransformed, i. e. without scaling
//            canvas.setPivot(f*dx, f*dy);
            
            event.consume();

        }

    };


    public static double pin( double value, double min, double max) {

        if( Double.compare(value, min) < 0)
            return min;

        if( Double.compare(value, max) > 0)
            return max;

        return value;
    }
}