package models;

import play.*;
import query.Layer_Base;
import query.Layer_Float;
import query.Layer_Integer;
import query.Scenario;
import java.util.*;
import java.lang.Math.*;

import analysis.ModelResult;
import analysis.Moving_Z_Window;

//------------------------------------------------------------------------------
public class Model_HerdSuitabilityIndex extends Model_Base
{
	private static final boolean SELF_DEBUG_LOGGING = false;
	
	private static final int mWindowSizeMeters = 300;
	private static final int mWindowSizeInCells = mWindowSizeMeters / 30;
	private static final String mModelFile = "cow_index";
	
	
	// Define habitat index function
	//--------------------------------------------------------------------------
	public List<ModelResult> run(Scenario scenario) {

		// Mask
		Layer_Integer cdl = (Layer_Integer)Layer_Base.getLayer("cdl_2012"); 
		int skipMask = cdl.convertStringsToMask("Wetland") | cdl.convertStringsToMask("Water") 
				| cdl.convertStringsToMask("Suburban") | cdl.convertStringsToMask("Urban");

		float slope[][] = ((Layer_Float)Layer_Base.getLayer("slope")).getFloatData();
		float rivers[][] = ((Layer_Float)Layer_Base.getLayer("rivers")).getFloatData();
		float publicLand[][] = ((Layer_Float)Layer_Base.getLayer("public_land")).getFloatData();
		int lcs[][] = ((Layer_Integer)Layer_Base.getLayer("lcs")).getIntData();
/*		1,Erosion Prone,#9c551f
		2,Saturated Soils,#ffffbf
		3,Poor Texture,#218291*/

	
		Integer height = cdl.getHeight();
		Integer width = cdl.getWidth();
		
		int data[][] = cdl.getIntData();
		// --- Model specific code starts here
		Moving_Z_Window zWin = new Moving_Z_Window(mWindowSizeInCells, data, width, height);

		float [][] indexData = new float[height][width];
		
		boolean moreCells = true;
		while (moreCells) {
			Moving_Z_Window.Z_WindowPoint point = zWin.getPoint();
			
			// If proportions are zero, don't try to get them because we'd divide by zero in doing that.
			if (zWin.canGetProportions()) {
				float index = 0;
				float s = slope[point.mY][point.mX];
				if (s <= 10.0f && rivers[point.mY][point.mX] > 1000.0f && (zWin.getWindowCenterValue() & skipMask) == 0) {
					index = (1.0f - zWin.getProportionForest()) * 0.8f + 0.2f;
					index *= (1.0f - zWin.getProportionDeveloped());
					
					index *= Math.pow((1.0f - s / 20.0f) + 0.5f, 0.75f);
					
					if ((lcs[point.mY][point.mX] & 1) > 0) {
						index *= 0.75f;
					}
					if (publicLand[point.mY][point.mX] < 200) index *= 0.8f;
					
					if (zWin.getProportionWater() < 0.5f) index *= 0.05f;
					else if (zWin.getProportionWater() < 0.75f) index *= 0.15f;
					else if (zWin.getProportionWater() < 0.9f) index *= 0.3;
				}

				indexData[point.mY][point.mX] = index;
			}
			else {
				indexData[point.mY][point.mX] = -9999.0f; // NO DATA
			}
			
			moreCells = zWin.advance();
		}		
		
		List<ModelResult> results = new ArrayList<ModelResult>();
		results.add(new ModelResult(mModelFile, "herd", indexData, width, height));
		
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
