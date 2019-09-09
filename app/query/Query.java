package query;

import play.*;
import transformData.Downsampler;
import utils.ClientUser;
import utils.Png;

import ar.com.hjg.pngj.chunks.*;

import java.util.concurrent.atomic.AtomicInteger;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;

//------------------------------------------------------------------------------
public class Query {
	
	//--------------------------------------------------------------------------
	private static final boolean DETAILED_DEBUG_LOGGING = false;
	private static final void detailedLog(String detailedMessage) {
		
		if (DETAILED_DEBUG_LOGGING) {
			Logger.debug(detailedMessage);
		}
	}
	
	static AtomicInteger mCounter = new AtomicInteger(1);

	// Users reported that an even alpha gradient makes fractional selections appear more substantial
	//	than they really are. Eyeballed tweaks matched this goofy func
	//------------------------------------------------------------------------------
	private int reweightAlpha(int alpha) {
		
		float nAlpha = alpha / 255.0f;
		double fwAlpha = (1.0 - Math.pow(1.0 - nAlpha, 0.64)) * 255.0;
		
		return (int)fwAlpha;
	}
//	D10046, 209, 0, 70
//	3508A3, 53, 8, 163
	//------------------------------------------------------------------------------
	public JsonNode selection(JsonNode requestBody) throws Exception
	{
		// selection color
		int r1 = 200, g1 = 0, b1 = 255;
		int resampleFactor = 4;
		
		// FIXME: can't base size off of a hardcoded layer? The expectation is that
		//	all layers are of the same size....
		Layer_Base tmp = Layer_Base.getLayer("cdl_2012");
		int width = tmp.getWidth();
		int height = tmp.getHeight();
		
		detailedLog("Called into query");
		detailedLog(requestBody.toString());
		
		int ctr = mCounter.getAndIncrement();
		String selectionFile = "selection" + String.valueOf(ctr) + ".png";
		String partialPath = "/public/dynamicFiles/" + selectionFile;
		String urlPath = "/files/" + selectionFile;
		detailedLog("File write path: " + partialPath);
		
		// 8 bits per pixel, one channel (indexed), file path where the png is saved
		// Since this is the file we are saving, it will be smaller than the actual
		//	width/height by the sample factor.
		int newWidth = width / resampleFactor;
		int newHeight = height / resampleFactor;
		Png png = new Png(newWidth, newHeight, 
				8, 1, 
				"." + partialPath);
		
		PngChunkPLTE palette = png.createPalette(5);
		palette.setEntry(0, 0,0,0); // black
		palette.setEntry(1, r1, g1, b1);
		palette.setEntry(2, r1, g1, b1);
		palette.setEntry(3, r1, g1, b1);
		palette.setEntry(4, r1, g1, b1);
		
		int[] alpha = new int[5];
		alpha[0] = 0;
		alpha[1] = reweightAlpha(64); alpha[2] = reweightAlpha(128); 
		alpha[3] = reweightAlpha(192); alpha[4] = reweightAlpha(255);
		
		png.setTransparentArray(alpha);

		// Set up to run the query...allocate memory...
		Selection selection = execute(requestBody, null);
		
		byte[][] temp = Downsampler.generateSelection(selection.mRasterData, 
								selection.getWidth(), selection.getHeight(),
								5, // transform to 5 colors
								newWidth, newHeight);
		png.writeArray(temp);

		// Get query statistics (number of selected pixels)
		int count = selection.countSelectedPixels();	

		detailedLog("Query Statistics");
		detailedLog("-----------------------");
		detailedLog("Total selected pixels: " + Integer.toString(count));
		detailedLog("Square km: " + Float.toString(count * 0.03f * 0.03f));

		// Data to return to the client		
		ObjectNode ret = JsonNodeFactory.instance.objectNode();
		
		ret.put("url", urlPath);
		ret.put("selectedPixels", count);
		ret.put("totalPixels", height * width);

		return ret;
	}
	
	//NOTE: user can be null
	//--------------------------------------------------------------------------
	public Selection execute(JsonNode requestBody, ClientUser user) throws Exception {
		
		// FIXME: can't base size off of a hardcoded layer? The expectation is that
		// all layers are of the same size....
		Layer_Base tmp = Layer_Base.getLayer("cdl_2012");
		
		Selection selection = new Selection(tmp.getWidth(), tmp.getHeight());

		// Actually run the query...
		JsonNode layerList = requestBody.get("queryLayers");
		Layer_Base.execQuery(layerList, selection, user);
		
		return selection;
	}
	
	//------------------------------------------------------------------------------
	public JsonNode occludedSelection(JsonNode first, JsonNode second) throws Exception
	{
		// effective selection color
		int r1 = 32, g1 = 96, b1 = 255;
		// occluded pixels color
		int r2 = 255, g2 = 0, b2 = 96;
		// affected but not selected pixels
		int r3 = 16, g3 = 32, b3 = 64;
		
		int resampleFactor = 3; // e.g., sampledWidth = realWidth / resampleFactor
		
		// FIXME: need a more robust sizing mechanism
		Layer_Base tmp = Layer_Base.getLayer("cdl_2012");
		int width = tmp.getWidth();
		int height = tmp.getHeight();
		
		int ctr = mCounter.getAndIncrement();
		String selectionFile = "selection" + String.valueOf(ctr) + ".png";
		String partialPath = "./public/dynamicFiles/" + selectionFile;
		String urlPath = "/files/" + selectionFile;
		detailedLog("File write path: " + partialPath);
		
		// 8 bits per pixel, one channel (indexed), file path where the png is saved
		// Image is resampled down to save network bandwtdth
		int newWidth = width / resampleFactor;
		int newHeight = height / resampleFactor;
		Png png = new Png(newWidth, newHeight, 
				8, 1, 
				partialPath);
		
		//		        Selected 
		//      _________________________
		//      |		|		|		|
		//  o   |	0	|	1	|	2	|	alpha 0.0
		//  c   |		|		|		|
		//	c	|-------+-------+-------|
		//  l   |		|		|		|
		//  u   |	3	|	4	|	5	|	alpha 0.5
		//  d   |		|		|		|
		//	e	|-------+-------+-------|
		//  d   |		|		|		|
		//      |	6	|	7	|	8	|	alpha 1.0
		//		|_______|_______|_______|
		//		  a 0.0	  a 0.5   a 1.0
		PngChunkPLTE palette = png.createPalette(12);
		palette.setEntry(0, r1, g1, b1); // fully transparent, color doesn't matter?
			palette.setEntry(1, r1, g1, b1);
				palette.setEntry(2, r1, g1, b1);
		
		palette.setEntry(3, r2, g2, b2);
			palette.setEntry(4, (r1 + r2)/2, (g1 + g2)/2, (b1 + b2)/2);
				palette.setEntry(5, (r1 * 2 + r2) / 3, (g1 * 2 + g2) / 3, (b1 * 2 + b2) / 3);
				
		palette.setEntry(6, r2, g2, b2);
			palette.setEntry(7, (r1 + r2 * 2) / 3, (g1 + g2 * 2) / 3, (b1 + b2 *2) / 3);
				palette.setEntry(8, (r1 + r2)/2, (g1 + g2)/2, (b1 + b2)/2);

		palette.setEntry(9, r3, g3, b3);
		palette.setEntry(10, r3, g3, b3);
		palette.setEntry(11, r3, g3, b3);
		
		int[] alpha = new int[12];
		alpha[0] = 0; 
			alpha[1] = reweightAlpha(128); 
				alpha[2] = reweightAlpha(255); 
		alpha[3] = reweightAlpha(128); 
			alpha[4] = reweightAlpha(128); 
				alpha[5] = reweightAlpha(255); 
		alpha[6] = reweightAlpha(255); 
			alpha[7] = reweightAlpha(255); 
				alpha[8] = reweightAlpha(255);
				
		alpha[9] = 0;
		alpha[10] = reweightAlpha(128);
		alpha[11] = reweightAlpha(255);
		png.setTransparentArray(alpha);

		// Set up to run the query...allocate memory...
		Selection sel1 = new Selection(width, height, (byte)0);
		ArrayNode array = (ArrayNode)first;
		for (int i = 0; i < array.size(); i++) {
			JsonNode next = array.get(i);
			sel1.combineSelection(execute(next, null));
		}
		Selection sel2 = execute(second, null);
		
		byte[][] temp = Downsampler.generateOccludedSelection(sel1.mRasterData, sel2.mRasterData,
								sel1.getWidth(), sel1.getHeight(),
								3, // transform to 3 colors
								newWidth, newHeight);
		png.writeArray(temp);

		// Get query statistics (number of selected pixels)
		int countFirst = sel1.countSelectedPixels();	
		int countSecond = sel2.countSelectedPixels();	
		int occluded = sel1.countOccludedPixels(sel2);
		
		detailedLog("Query Statistics");
		detailedLog("-----------------------");
		detailedLog("Total selected pixels in first: " + Integer.toString(countFirst));
		detailedLog("Square km: " + Float.toString(countFirst * 0.03f * 0.03f));
		detailedLog("Total selected pixels in second: " + Integer.toString(countSecond));
		detailedLog("Square km: " + Float.toString(countSecond * 0.03f * 0.03f));

		// Data to return to the client		
		ObjectNode ret = JsonNodeFactory.instance.objectNode();
		
		ret.put("url", urlPath);
		ret.put("selectedPixelsFirst", countFirst);
		ret.put("selectedPixelsSecond", countSecond);
		ret.put("occludedSecondPixels", occluded);
		ret.put("totalPixels", height * width);

		return ret;
	}

}
