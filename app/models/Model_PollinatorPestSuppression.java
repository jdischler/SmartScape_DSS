package models;

import play.*;
import query.Layer_Base;
import query.Layer_Integer;
import query.Scenario;
import utils.PerformanceTimer;

import java.util.*;

import analysis.ModelResult;
import analysis.Moving_Z_Window;

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

	private static final int mWindowSizeMeters = 990;
	private static final int mWindowSizeInCells = mWindowSizeMeters / 30; // Number of Cells in Raster Map
	
	private static final String mPollinatorModelFile = "pollinator";
	private static final String mPestModelFile = "pest";
	
	//--------------------------------------------------------------------------
	public List<ModelResult> run(Scenario scenario) {

		int[][] rotationData = scenario.mNewRotation;
		int width = scenario.getWidth(), height = scenario.getHeight();
		
		debugLog(">>> Computing Model Pest/ Pollinator");
		PerformanceTimer timer = new PerformanceTimer();
		
		// Mask
		Layer_Integer cdl = (Layer_Integer)Layer_Base.getLayer("cdl_2012"); 
		int Grass_Mask = cdl.convertStringsToMask("grass");
		int Alfalfa_Mask = cdl.convertStringsToMask("alfalfa");
		int Corn_Mask = cdl.convertStringsToMask("corn");
		int Soy_Mask = cdl.convertStringsToMask("soy");
		int TotalMask = Grass_Mask | Corn_Mask | Soy_Mask | Alfalfa_Mask;
		
		// full raster save process...
		float [][] pestData = new float[height][width];
		float [][] pollinatorData = new float[height][width];
		
		Moving_Z_Window zWin = new Moving_Z_Window(mWindowSizeInCells, rotationData, width, height);
		Moving_Z_Window.Z_WindowPoint point = zWin.getPoint();

		float max = (float)Math.pow(0.75f + 2.5f + 1.0f, 2.0f);
		
		boolean moreCells = true;
		while (moreCells) {
			
			point = zWin.getPoint();
			if ((rotationData[point.mY][point.mX] & TotalMask) > 0 && zWin.canGetProportions()) {
				
				float proportionForest = zWin.getProportionForest();
				float proportionGrass = zWin.getProportionGrass();
				
				// Calculate visitation index and normalize value by max
				float pollinatorIndex = (float)Math.pow((proportionForest * proportionGrass) * 3.0f 
							+ (2.5f * proportionForest) 
							+ (proportionGrass), 2.0f);
				
				pollinatorData[point.mY][point.mX] = pollinatorIndex / max;
				
				// Crop type is zero for Ag, Crop type is 1 for grass
				float cropType = 0.0f;
				if ((rotationData[point.mY][point.mX] & Grass_Mask) > 0) {
					cropType = 1.0f;
				}
					
				// Pest suppression calculation
				float pestSuppression = 0.25f + (0.19f * cropType) + (0.62f * proportionGrass);
	
				pestData[point.mY][point.mX] = pestSuppression;
			}
			else {
				pollinatorData[point.mY][point.mX] = -9999.0f;
				pestData[point.mY][point.mX] = -9999.0f;
			}

			
			moreCells = zWin.advance();
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
