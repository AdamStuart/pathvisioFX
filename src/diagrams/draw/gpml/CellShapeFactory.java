package diagrams.draw.gpml;

import diagrams.draw.app.Tool;
import javafx.scene.paint.Color;
import javafx.scene.shape.CubicCurveTo;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.QuadCurveTo;
import javafx.scene.shape.Shape;

public class CellShapeFactory {
	
	static public Shape makeCustomShape(String s)
	{
		if ("Brace".equals(s))	  		return getBracePath();
		if ("Mitochondria".equals(s))	return getMitochondrialPath();
		if ("SR".equals(s))	  			return getSarcoplasmicReticulumPath();
		if ("ER".equals(s))	  			return getEndoplasmicReticulumPath();
		if ("Golgi".equals(s))	  		return getGolgiPath();
		return null;
	}	
	//-------------------------------------------------------------------------
	
	static private Path getBracePath()
	{
		Path path = new Path();
		MoveTo moveTo = new MoveTo(0,40);
		QuadCurveTo quadCurveTo1 = new QuadCurveTo(0,20,30,20);
		QuadCurveTo quadCurveTo2 = new QuadCurveTo(60,20,60,0);
		QuadCurveTo quadCurveTo3 = new QuadCurveTo(60,20,90,20);
		QuadCurveTo quadCurveTo4 = new QuadCurveTo(120,20,120,40);
		path.getElements().addAll(moveTo, quadCurveTo1, quadCurveTo2, quadCurveTo3, quadCurveTo4);
		path.setStroke(Color.RED);
		path.setStrokeWidth(5);
		return path;
	}
	static private Path getMitochondrialPath()
	{
		Path path = new Path();
		path.getElements().add (new MoveTo(72.81, 85.70));
		path.getElements().add(new CubicCurveTo (97.59, 83.01, 94.55, 147.38, 119.28, 144.29));
		path.getElements().add(new CubicCurveTo (166.27, 144.40, 136.22, 42.38, 175.51, 41.70));
		path.getElements().add(new CubicCurveTo (215.08, 41.02, 188.27, 150.12, 227.79, 148.28));
		path.getElements().add(new CubicCurveTo (271.14, 146.25, 230.67, 29.04, 274.00, 26.55));
		path.getElements().add(new CubicCurveTo (317.72, 24.05, 290.58, 142.55, 334.36, 143.22));
		path.getElements().add(new CubicCurveTo (371.55, 143.80, 351.55, 43.14, 388.66, 45.75));
		path.getElements().add(new CubicCurveTo (429.51, 48.62, 392.43, 153.80, 432.85, 160.40));
		path.getElements().add(new CubicCurveTo (459.82, 164.80, 457.96, 94.30, 485.13, 97.26));
		path.getElements().add(new CubicCurveTo (548.33, 124.69, 534.13, 233.75, 472.75, 258.89));
		path.getElements().add(new CubicCurveTo (454.92, 261.42, 450.22, 220.87, 432.35, 223.03));
		path.getElements().add(new CubicCurveTo (400.60, 226.86, 409.73, 303.71, 377.80, 301.95));
		path.getElements().add(new CubicCurveTo (348.05, 300.30, 365.16, 223.61, 335.37, 223.28));
		path.getElements().add(new CubicCurveTo (295.83, 222.85, 316.30, 327.99, 276.78, 326.44));
		path.getElements().add(new CubicCurveTo (241.90, 325.08, 266.95, 236.11, 232.34, 231.61));
		path.getElements().add(new CubicCurveTo (200.07, 227.42, 201.79, 311.88, 169.71, 306.49));
		path.getElements().add(new CubicCurveTo (134.22, 300.53, 167.04, 209.92, 131.32, 205.60));
		path.getElements().add(new CubicCurveTo (110.14, 203.04, 116.28, 257.74, 94.95, 258.26));
		path.getElements().add(new CubicCurveTo (15.35, 236.77, 5.51, 114.51, 72.81, 85.70));
		path.getElements().add (new MoveTo(272.82, 0.84));
		path.getElements().add(new CubicCurveTo (378.97, 1.13, 542.51, 62.39, 543.54, 168.53));
		path.getElements().add(new CubicCurveTo (544.58, 275.18, 381.50, 342.19, 274.84, 342.28));
		path.getElements().add(new CubicCurveTo (166.69, 342.36, 0.84, 274.66, 2.10, 166.51));
		path.getElements().add(new CubicCurveTo (3.33, 60.72, 167.03, 0.56, 272.82, 0.84));
		return path;
		// **-------------------------------------------------------------------------------
	}
	static private Path getSarcoplasmicReticulumPath()
	{
		Path path = new Path();
		path.getElements().add (new MoveTo(118.53, 16.63));
		path.getElements().add(new CubicCurveTo (34.13, 22.00, 23.84 , 107.76, 49.44 , 169.22));
		path.getElements().add(new CubicCurveTo (73.73, 242.63, 0.51 , 289.88, 56.13 , 366.83));
		path.getElements().add(new CubicCurveTo (99.99, 419.32, 176.93 , 391.26, 192.04 , 332.54));
		path.getElements().add(new CubicCurveTo (207.42, 271.52, 163.49 , 228.38, 183.45 , 168.61));
		path.getElements().add(new CubicCurveTo (211.75, 89.03, 181.43 , 16.01, 118.53 , 16.63));
		path.getElements().add(new LineTo (118.53, 16.63));		
		return path;
	}
	// **-------------------------------------------------------------------------------
	
	static private Path getEndoplasmicReticulumPath()
	{
		Path path = new Path();
		path.getElements().add (new MoveTo(115.62, 170.76));
		path.getElements().add(new CubicCurveTo (106.85, 115.66, 152.29 , 74.72, 152.11 , 37.31));
		path.getElements().add(new CubicCurveTo (151.57, 22.91, 135.75 , 10.96, 123.59 , 21.51));
		path.getElements().add(new CubicCurveTo (97.02, 44.83, 99.19 , 108.29, 90.52 , 146.58));
		path.getElements().add(new CubicCurveTo (89.97, 157.27, 79.04 , 153.89, 78.44 , 145.14));
		path.getElements().add(new CubicCurveTo (69.32, 111.41, 105.16 , 72.62, 87.74 , 58.00));
		path.getElements().add(new CubicCurveTo (57.12, 33.80, 42.90 , 120.64, 53.32 , 143.34));
		path.getElements().add(new CubicCurveTo (65.01, 185.32, 49.93 , 215.62, 42.80 , 189.23));
		path.getElements().add(new CubicCurveTo (39.00, 173.52, 52.26 , 156.40, 41.55 , 141.32));
		path.getElements().add(new CubicCurveTo (34.82, 133.03, 23.22 , 139.41, 16.36 , 150.49));
		path.getElements().add(new CubicCurveTo (0.00, 182.29, 23.74 , 271.85, 49.05 , 257.53));
		path.getElements().add(new CubicCurveTo (56.38, 251.73, 44.01 , 231.76, 55.14 , 229.10));
		path.getElements().add(new CubicCurveTo (66.52, 226.70, 63.22 , 247.43, 67.13 , 256.43));
		path.getElements().add(new CubicCurveTo (70.73, 268.42, 74.67 , 281.17, 83.91 , 290.85));
		path.getElements().add(new CubicCurveTo (91.38, 298.36, 107.76 , 297.10, 110.06 , 285.05));
		path.getElements().add(new CubicCurveTo (113.23, 257.62, 69.35 , 201.07, 93.40 , 192.41));
		path.getElements().add(new CubicCurveTo (122.33, 184.37, 100.80 , 263.03, 131.30 , 280.35));
		path.getElements().add(new CubicCurveTo (146.12, 286.36, 155.69 , 278.51, 154.40 , 268.41));
		path.getElements().add(new CubicCurveTo (150.12, 235.05, 115.21 , 201.24, 115.47 , 170.24));
		path.getElements().add(new LineTo (115.62, 170.76));
		return path;
	}
// **-------------------------------------------------------------------------------
	static private Path getGolgiPath()
	{
		Path path = new Path();
		path.getElements().add (new MoveTo(148.89, 77.62));
		path.getElements().add(new CubicCurveTo (100.07, 3.50, 234.06 , 7.65, 207.78 , 62.66));
		path.getElements().add(new CubicCurveTo (187.00, 106.50, 171.09 , 190.54, 209.13 , 287.47));
		path.getElements().add(new CubicCurveTo (240.55, 351.33, 111.35 , 353.69, 144.36 , 284.72));
		path.getElements().add(new CubicCurveTo (171.13, 215.31, 165.77 , 107.32, 148.89 , 77.62));
		path.getElements().add(new LineTo (148.89, 77.62));
		path.getElements().add (new MoveTo(88.16, 91.24));
		path.getElements().add(new CubicCurveTo (62.70, 40.69, 158.70 , 44.41, 131.59 , 92.83));
		path.getElements().add(new CubicCurveTo (116.28, 128.91, 117.95 , 238.10, 134.33 , 269.85));
		path.getElements().add(new CubicCurveTo (154.45, 313.72, 56.82 , 315.51, 85.96 , 264.54));
		path.getElements().add(new CubicCurveTo (102.37, 223.58, 110.67 , 141.16, 88.16 , 91.24));
		path.getElements().add(new LineTo (88.16, 91.24));
		path.getElements().add (new MoveTo(83.40, 133.15));
		path.getElements().add(new CubicCurveTo (86.43, 160.23, 86.72 , 203.15, 82.05 , 220.09));
		path.getElements().add(new CubicCurveTo (73.24, 250.74, 69.98 , 262.93, 50.80 , 265.89));
		path.getElements().add(new CubicCurveTo (32.17, 265.52, 22.80 , 242.80, 39.49 , 227.87));
		path.getElements().add(new CubicCurveTo (50.94, 214.61, 53.98 , 202.20, 55.20 , 173.72));
		path.getElements().add(new CubicCurveTo (54.63, 152.16, 56.07 , 133.57, 43.25 , 126.63));
		path.getElements().add(new CubicCurveTo (25.26, 121.45, 30.31 , 86.90, 56.06 , 93.20));
		path.getElements().add(new CubicCurveTo (69.86, 95.63, 79.23 , 109.03, 83.40 , 133.15));
		path.getElements().add(new LineTo (83.40, 133.15));
		return path;
	}
}
