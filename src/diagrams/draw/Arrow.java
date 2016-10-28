package diagrams.draw;

import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.CubicCurve;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Shape;
import javafx.scene.transform.Rotate;
import util.LineUtil;

public class Arrow extends Polygon {

	private float t;			// 0-1 parameter along the curve
    private CubicCurve curve;
    private Line line;
    private Rotate rotZ;

    public Arrow( CubicCurve acurve, float d, double... arg0) {
        super(arg0);
        curve = acurve;
        t = d;
        init();
    }
    public Arrow(  Line aline, float d, Color c, Shape sh) {
        super();
        line = aline;
        t = d;
        sh.setFill(c);
        sh.setStroke(c);
        init();
        setPosition(line.getEndX(), line.getEndY());
    }

    public Arrow(  Line aline, float d, Color c, double... arg0) {
        super(arg0);
        line = aline;
        t = d;
        setFill(c);
        setStroke(c);
        init();
        setPosition(line.getEndX(), line.getEndY());
    }

    public void setPosition(double x, double y)
    {
        setTranslateX(x);
        setTranslateY(y);
    }
//--------------------------------------------------------------------
    private void init() {
//        setFill(Color.web("#4f0970"));
        rotZ = new Rotate();
        rotZ.setAxis(Rotate.Z_AXIS);
        getTransforms().addAll(rotZ);
        updateLine();
        updateCurve();
    }
  //--------------------------------------------------------------------
    boolean useElbowConnection = false;
    public void updateLine() {
        if (line == null) return;
    	double ang2 = (useElbowConnection) ? 90 : LineUtil.getEndAngle(line);
//    	System.out.println(String.format("updateLine: (%.2f, %.2f) %.2f", line.getEndX(), line.getEndY(), Math.toDegrees(ang2)));
    	Point2D midpt = LineUtil.midPoint(line, 0.90);
        setTranslateX(midpt.getX());
        setTranslateY(midpt.getY());	
           // arrow origin is top => apply offset
//        translateXProperty().bind(line.endXProperty());
//        translateYProperty().bind(line.endYProperty());
        double offset = ( t > 0.5) ? 90 : -90;
        rotZ.setAngle(Math.toDegrees(-ang2) + offset);
   }
    
	//--------------------------------------------------------------------
   public void updateCurve() {
	   if (curve == null) return;
      	double size = Math.max(curve.getBoundsInLocal().getWidth(), curve.getBoundsInLocal().getHeight());
		double scale = size / 4d;
		
		Point2D ori = LineUtil.eval(curve, t);
		Point2D tan = LineUtil.evalDt(curve, t).normalize().multiply(scale);
		
		setTranslateX(ori.getX());
		setTranslateY(ori.getY());
		
		double angle = Math.atan2( tan.getY(), tan.getX());
		angle = Math.toDegrees(angle);
		double offset = ( t > 0.5) ? 90 : -90;		// arrow origin is top => apply offset
		rotZ.setAngle(angle + offset);
    }
}


