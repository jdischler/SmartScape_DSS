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
public class Model_HabitatIndex extends Model_Base
{
	private static final boolean SELF_DEBUG_LOGGING = false;
	
	private static final int mWindowSizeMeters = 390;
	private static final int mWindowSizeInCells = mWindowSizeMeters / 30;
	private static final String mModelFile = "habitat_index";
	
	
	// Define habitat index function
	//--------------------------------------------------------------------------
	public List<ModelResult> run(Scenario scenario) {

		int[][] rotationData = scenario.mNewRotation;
		int width = scenario.getWidth(), height = scenario.getHeight();
		
		debugLog(">>> Computing Model Habitat Index");
		PerformanceTimer timer = new PerformanceTimer();
		
		float [][] habitatData = new float[height][width];
		debugLog("  > Allocated memory for Habitat Index");
		
		Layer_Integer wl = (Layer_Integer)Layer_Base.getLayer("wisc_land");
		
		int totalMask = wl.stringToMask("hay","pasture","cool-season grass","warm-season grass",	
				"continuous corn","cash grain","dairy rotation","other crops");

		// --- Model specific code starts here
		Moving_Z_Window zWin = new Moving_Z_Window(mWindowSizeInCells, rotationData, width, height);
		
		boolean moreCells = true;
		while (moreCells) {
			Moving_Z_Window.WindowPoint point = zWin.getPoint();
			
			// If proportions are zero, don't try to get them because we'd divide by zero in doing that.
			if ((rotationData[point.mY][point.mX] & totalMask) > 0 && zWin.canGetProportions()) {
				float proportionAg = zWin.getProportionAg();
				float proportionGrass = zWin.getProportionGrass();
				
				// Habitat Index
				float lambda = -4.47f + (2.95f * proportionAg) + (5.17f * proportionGrass); 
				float habitatIndex = (float)((1.0f / (1.0f / Math.exp(lambda) + 1.0f )) / 0.67f);

				habitatData[point.mY][point.mX] = habitatIndex;
			}
			else {
				habitatData[point.mY][point.mX] = -9999.0f; // NO DATA
			}
			
			moreCells = zWin.advance();
		}		
		
		List<ModelResult> results = new ArrayList<ModelResult>();
		results.add(new ModelResult(mModelFile, scenario.mOutputDir, habitatData, width, height));
		
		debugLog(">>> Model_Habitat_Index is finished - timing (ms): " + timer.stringMilliseconds(2));

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
