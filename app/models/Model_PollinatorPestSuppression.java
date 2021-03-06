package models;

import play.*;
import query.Layer_CDL;
import query.Layer_Integer;
import query.Scenario;
import utils.PerformanceTimer;

import java.util.*;

import analysis.*;
import analysis.window.*;

//------------------------------------------------------------------------------
// Modeling Process
//
// This program uses landscape proportion to calculate pollinator visitation index (Grass and Forest) 
// using 1500m rectangle buffers 
// Visitation Index can vary from 0 to 18, after normalization it goes 0 to 1
// This model is from unpublished work by Ashley Bennett from Michigan State University
// Inputs are proportion of land cover particularly grass and forest, selected cells in the raster map and crop rotation layer 
// ASCII map of visitation index
// Version 08/20/2013
//
//------------------------------------------------------------------------------
public class Model_PollinatorPestSuppression extends Model_Base {

	private static final boolean SELF_DEBUG_LOGGING = false;

	private static final int mWindowSizeMeters = 1500;
	private static final int mWindowSizeInCells = mWindowSizeMeters / 30; // Number of Cells in Raster Map
	
	private static final String mPollinatorModelFile = "pollinator";
	private static final String mPestModelFile = "pest";
	
	//--------------------------------------------------------------------------
	public List<ModelResult> run(Scenario scenario) {

		int[][] rotationData = scenario.mNewRotation;
		int width = scenario.getWidth(), height = scenario.getHeight();
		
		debugLog(">>> Computing Model Pest/ Pollinator");
		PerformanceTimer timer = new PerformanceTimer();
		
		Layer_Integer cdl = Layer_CDL.get();
		
		int grassMask = cdl.stringToMask("hay","pasture","cool-season grass","warm-season grass");	
		int totalMask = cdl.stringToMask("hay","pasture","cool-season grass","warm-season grass",	
				"continuous corn","cash grain","dairy rotation","other crops");
		
		// full raster save process...
		float [][] pestData = new float[height][width];
		float [][] pollinatorData = new float[height][width];
		
		Moving_CDL_Window win = new Moving_CDL_Window_N(mWindowSizeInCells, rotationData, width, height);
		Moving_Window.WindowPoint point;

		// derived from pollinatorIndex formula. 
		float max = (float)Math.pow(2.8f, 2.0f);
		
		boolean moreCells = true;
		while (moreCells) {
			
			point = win.getPoint();
			if ((rotationData[point.mY][point.mX] & totalMask) > 0 && win.canGetProportions()) {
				
				float proportionForest = win.getProportionForest();
				float proportionGrass = win.getProportionGrass();
				
				// Calculate visitation index and normalize value by max
				float pollinatorIndex = (float)Math.pow((proportionForest * proportionGrass) * 3.0f 
							+ (2.5f * proportionForest) 
							+ (proportionGrass), 2.0f);
				
				pollinatorData[point.mY][point.mX] = pollinatorIndex / max;
				
				// Crop type is zero for Ag, Crop type is 1 for grass
				float cropType = 0.0f;
				if ((rotationData[point.mY][point.mX] & grassMask) > 0) {
					cropType = 1.0f;
				}
					
				// Pest suppression calculation
				float pestSuppression = 0.24f + (0.18f * cropType) + (0.58f * proportionGrass);
	
				pestData[point.mY][point.mX] = pestSuppression;
			}
			else {
				pollinatorData[point.mY][point.mX] = -9999.0f;
				pestData[point.mY][point.mX] = -9999.0f;
			}

			
			moreCells = win.advance();
		}	
	
		List<ModelResult> results = new ArrayList<ModelResult>();
		results.add(new ModelResult(mPestModelFile, scenario.mOutputDir, pestData, width, height));
		results.add(new ModelResult(mPollinatorModelFile, scenario.mOutputDir, pollinatorData, width, height));
		
		debugLog(">>> Model_PollinatorPestSuppression_New is finished - timing (ms): " + timer.stringMilliseconds(2));

		return results;
	}	
	
	//-------------------------------------------------------------------------------------------
	@SuppressWarnings("unused")
	private static final void debugLog(String conditionalLog) {
		
		if (ALL_MODELS_DEBUG_LOGGING || SELF_DEBUG_LOGGING) {
			Logger.debug(conditionalLog);
		}
	}
}
