package models;

import play.*;
import query.Layer_Base;
import query.Layer_Integer;
import query.Scenario;
import utils.ManagementOptions;
import utils.PerformanceTimer;

import java.util.*;

import analysis.ModelResult;

//------------------------------------------------------------------------------
// Modeling Process
//
// This program calculates soil loss for each pixel
// The inputs are where R is the rainfall erosivity, K is the soil erodibility factor, LS is the slope length factor, C is the crop-management factor and P is the support practice factor
// A = R * K * LS * C * P
// Units are normally ton per Ha year for A, MJ mm per ha hr yr for R, ton ha hr per ha MJ mm for K, 
// R was calculated using Eq from below page
// http://gisedu.colostate.edu/webcontent/nr505/ethiopia/group4/GIS%20Analyses.html#rainfall
// K layer was incorporated from SSURGO data base and we converted it to ton per ha from ton / acre
// Other units are LS, C and P are dimensionless
// Input is crop rotation layer 
// Version 02/15/2014
//
//------------------------------------------------------------------------------
public class Model_Soil_Loss extends Model_Base {
	
	private static final boolean SELF_DEBUG_LOGGING = false;
	
	private static final String mSoilLossModelFile = "soil_loss";
	
	//--------------------------------------------------------------------------
	public List<ModelResult> run(Scenario scenario) {
		
		debugLog(">>> Computing Soil_Loss Model");
		PerformanceTimer timer = new PerformanceTimer();
		
		// Spatial Layers
		int[][] rotationData = scenario.mNewRotation;
		int width = scenario.getWidth(), height = scenario.getHeight();
		
		// Calculate A using this formula A = R * K * LS * C * P
		// Unit (MJ mm/ha hr yr) or R
		float[][] Rainfall_Erosivity = Layer_Base.getLayer("Rainfall_Erosivity").getFloatData();
		// Unit (ton ha hr/ha MJ mm) or K
		float[][] Soil_Erodibility = Layer_Base.getLayer("Soil_Erodibility").getFloatData();
		// LS (Dimensionless)
		float[][] LS = Layer_Base.getLayer("LS").getFloatData();
		
		// Mask
		Layer_Integer wl = (Layer_Integer)Layer_Base.getLayer("wisc_land");
		
		int grassMask = wl.stringToMask("hay","pasture","cool-season grass","warm-season grass");	
		int agMask = wl.stringToMask("continuous corn","cash grain","dairy rotation","other crops");
		int totalMask = agMask | grassMask;
		
		// Arrays to save soil loss at cell base (Mg/ha)
		float[][] Soil_Loss_Data = new float[height][width];
		
		// Rainfall erosivity index for Dane County
		//float Rainfall_Erosivity = 150.0f;
		// Cover management factor (Dimensionless)
		// Both the growing crops and the residue from the crops affect erosion control
		// C factor compares erosion from sites with different crops and residue covers to that of bare, un-cropped site
		// Thus, management decisions about crop rotations and tillage system determine the value of C
		// Undisturbed grassland and forests have very little erosion and the lowest C value, typically below 0.2
		// Soil with no plant or residue cover are most susceptible to erosion and have the highest possible C factor
		float C = 0.0f;
		// Support practice factor (Dimensionless): 1) Contouring, 2) Strip Cropping, 3) Terrace, 4) grassed waterway and 5) No Practice
		// Contouring is tilling and planting across slope rather than with it
		// Strip cropping uses alternate lands of cover and row crops across the slope
		// Terrace are soil embankment that reshape slopes into a series of short slopes that slow water flow and reduce its erosivity
		// Terrace that open to grasses waterways further limit erosion
		// P is 1 for No Practice
		float P = 0.0f;
		// Till Multiplier
		//float T_M = 1.0f;
		// Cover Crop Multiplier
		float CC_M = 1.0f;
		
		// Multipliers from client variables
		//float annualTillage_C1 = 1.0f; //
		float annualCoverCropModifier = 1.0f;		
		float Management_P1 = 1.0f; //
		float Management_P2 = 1.0f;	
		
		// Get user changeable yield scaling values from the client...
		//----------------------------------------------------------------------
		try {
			// values come in as straight multiplier
			annualCoverCropModifier = scenario.mAssumptions.getFloat("sl_cc_annuals");
			Management_P1 = scenario.mAssumptions.getFloat("sl_Contouring_P1");
			Management_P2 = scenario.mAssumptions.getFloat("sl_Terrace_P2");
		}
		catch (Exception e) {
			Logger.warn(e.toString());
		}
		
		debugLog(" Agricultural cover crop from client = " + Float.toString(annualCoverCropModifier) );
		debugLog(" Agricultural Contouring from client = " + Float.toString(Management_P1) );
		debugLog(" Agricultural Terrace from client = " + Float.toString(Management_P2) );
		
		// full raster save process...
		debugLog("  > Allocated memory for Soil_Loss");

		// Soil Loss Model
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				
				int landCover = rotationData[y][x];
				CC_M = 1.0f;
				
				if ((landCover & totalMask) > 0 &&
					Rainfall_Erosivity[y][x] > -9999.0f && 
					Soil_Erodibility[y][x] > -9999.0f && 
					LS[y][x] > -9999.0f) {
					
					// Update C and P factors for different LUCC type
					// C and P are coming from biophysical table (Invest)
					if ((landCover & grassMask) > 0) {
						C = 0.02f;
						P = ManagementOptions.E_Terrace.getIfActive(landCover, Management_P2, 1.0f);
					} 
					/*// NOTE: alfalfa no longer a crop type though it is a component of a rotation?
					 * else if ((landCover & Alfalfa_Mask) > 0) {
						C = 0.02f;
						P = ManagementOptions.E_Terrace.getIfActive(landCover, Management_P2, 1.0f);
					} */
					// Agriculture
					else if ((landCover & agMask) > 0) {
						C = 0.3f;
						CC_M = ManagementOptions.E_CoverCrop.getIfActive(landCover, annualCoverCropModifier, 1.0f);
						P = ManagementOptions.E_Contour.getIfActive(landCover, Management_P1, 1.0f) *
						ManagementOptions.E_Terrace.getIfActive(landCover, Management_P2, 1.0f);
					}

					// Convert Mg per Ha to Mg per cell
					// Calculate Soil Loss for each cell in the landscape (Mg per cell per year)
					Soil_Loss_Data[y][x] = Rainfall_Erosivity[y][x] * Soil_Erodibility[y][x] * LS[y][x] * 
							C * P * CC_M * // apply multipliers based on management options
							900.0f / 10000.0f;
				}
				else {
					Soil_Loss_Data[y][x] = -9999.0f;
				}
			}
		}
		
		List<ModelResult> results = new ArrayList<ModelResult>();
		
		results.add(new ModelResult(mSoilLossModelFile, scenario.mOutputDir, Soil_Loss_Data, width, height));
		
		debugLog(">>> Model_Soil_Loss is finished - timing: " + timer.stringMilliseconds(2));

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
