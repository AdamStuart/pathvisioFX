package diagrams.pViz.app;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.function.Function;

import javax.imageio.ImageIO;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.print.PrinterJob;
import javafx.stage.FileChooser;
import model.bio.Gene;
import model.bio.GeneList;
import util.FileUtil;
/*
 * Document takes care of reading the state from a file, or writing it to one.
 */
public class Document
{
	
	Controller drawController;
	File file = null;
	private int verbose = 0;
	
	public Document(Controller inDC)
	{
		drawController = inDC;
	}
	// **-------------------------------------------------------------------------------
	public void open(String s)		
	{ 	
		try
		{
			org.w3c.dom.Document doc = FileUtil.convertStringToDocument(s);	//  parse string to XML
			if (doc != null)
				drawController.addState(doc);					
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}

	
	public void open(File f)		
	{ 	
		if (FileUtil.isCDT(f))
		{
			try
			{
				GeneList geneList = new GeneList();
				List<String> lines = FileUtil.readFileIntoStringList(f.getAbsolutePath());
				if (lines.size() > 0)
				{
					int skip = 2;
					for (String line : lines)
					{
						if (skip>0) { skip--;  continue; }
						Gene g = new Gene("");
						g.setData(line);
						Gene existing = geneList.find(g);
						if (existing == null)
							geneList.add(g);
					}
				}
				drawController.setGeneList(geneList);
			}
			catch (Exception e) 
			{
				e.printStackTrace();
			}
			return;
		}
			
			
		try
		{
			org.w3c.dom.Document doc = FileUtil.openXML(f);
			if (doc != null)
			{
				drawController.addState(doc);					//  parse XML to sceneGraph
		        new Thread(() ->
		           Platform.runLater(() -> {
		        	   drawController.getModel().resetEdgeTable();	
		        })  ).start();    
			}
			
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		
	}
	// **-------------------------------------------------------------------------------
	public void open()			// ask for the file
	{ 	
		FileChooser chooser = new FileChooser();	
		chooser.setTitle("Open Drawing");
		file = chooser.showOpenDialog(App.getInstance().getStage());
		if (file != null)				// dialog wasn't canceled
			open(file);			
	}
	
	
	// **-------------------------------------------------------------------------------
	public void save()		
	{ 	
		if (file == null)
		{
			FileChooser chooser = new FileChooser();	
			chooser.setTitle("Save Drawing");
			file = chooser.showSaveDialog(App.getInstance().getStage());
			if (file == null) return;
			App.getInstance().getStage().setTitle(file.getName());
		}
		if (verbose > 0) System.out.println("about to do the save traversal");
		String buff =  drawController.getModel().saveState();
		if (verbose > 0) System.out.println(buff);
		 try (FileOutputStream out = new FileOutputStream(file)) 
		 {
		    out.write( buff.getBytes());
			 out.close();
		 } 
		 catch (IOException e) {     e.printStackTrace();  }
	}
	
	public void saveas()		
	{ 	
		file = null;
		save();	
	}
	// **-------------------------------------------------------------------------------
	boolean fileDirty = false;
	
	public void close()		
	{ 	
//		if (fileDirty)	askToSave();
		file = null;
	}
	// **-------------------------------------------------------------------------------
	// TODO:  only prints first page, without scaling it.
	
	public void print()
	{
		PrinterJob job = PrinterJob.createPrinterJob();
		if (job == null) return;
		boolean success = job.printPage(drawController.getPasteboard());
		if (success)
			job.endJob();
	}
	// **-------------------------------------------------------------------------------

}
