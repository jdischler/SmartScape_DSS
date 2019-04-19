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
// This program uses to calculate nitrous oxide emissions(Mg per Ha) 
// This model is from unpublished work by 
// Inputs are layers, selected cells in the raster map and crop rotation layer 
// Outputs are ASCII map of nitrous oxide emissions
// Version 08/20/2013
//
//------------------------------------------------------------------------------
public class Model_NitrousOxideEmissions extends Model_Base
{
	private static final boolean SELF_DEBUG_LOGGING = false;
	
	private static final String mModelFile = "nitrous_oxide";

	//--------------------------------------------------------------------------
	public List<ModelResult> run(Scenario scenario) {

		int[][] rotationData = scenario.mNewRotation;
		int width = scenario.getWidth(), height = scenario.getHeight();
		
		debugLog(">>> Computing Nitrous Oxide Index");
		PerformanceTimer timer = new PerformanceTimer();
		
		float [][] nitrousOxideData = new float[height][width];
		debugLog("  > Allocated memory for N2O");
		
		// Mask
		Layer_Integer cdl = (Layer_Integer)Layer_Base.getLayer("cdl_2012"); 
		int Grass_Mask = cdl.stringToMask("grass");
		int Corn_Mask = cdl.stringToMask("corn");
		int Soy_Mask = cdl.stringToMask("soy");
		int Alfalfa_Mask = cdl.stringToMask("Alfalfa");
		int TotalMask = Grass_Mask | Corn_Mask | Soy_Mask | Alfalfa_Mask;
		
		// Input layers
		float n2o_composite[][] = Layer_Base.getLayer("n2o_composite").getFloatData();
		
		// Multipliers from client variables
		float annualTillageModifier = 1.0f; //
		float annualCoverCropModifier = 1.0f;		
		float annualFertilizerModifier = 1.0f;
		float perennialFertilizerModifier = 1.0f;
		float annualFallFertilizerModifier = 1.0f;
		float perennialFallFertilizerModifier = 1.0f;
		
		// Get user changeable yield scaling values from the client...
		//----------------------------------------------------------------------
		try {	
			// values come in as straight multiplier
			annualTillageModifier = scenario.mAssumptions.getFloat("n_t_annuals");
			annualCoverCropModifier = scenario.mAssumptions.getFloat("n_cc_annuals");		
			annualFertilizerModifier = scenario.mAssumptions.getFloat("n_m_annuals");
			perennialFertilizerModifier = scenario.mAssumptions.getFloat("n_m_perennials");	
			annualFallFertilizerModifier = scenario.mAssumptions.getFloat("n_fm_annuals");
			perennialFallFertilizerModifier = scenario.mAssumptions.getFloat("n_fm_perennials");	
		}
		catch (Exception e) {
			Logger.warn(e.toString());
		}
		
		debugLog(" Agricultural till from client = " + Float.toString(annualTillageModifier) );
		debugLog(" Agricultural cover crop from client = " + Float.toString(annualCoverCropModifier) );
		debugLog(" Agricultural Fertilizer from client = " + Float.toString(annualFertilizerModifier) );
		debugLog(" Perennial Fertilizer from client = " + Float.toString(perennialFertilizerModifier) );
		debugLog(" Annual Fall Fertilizer from client = " + Float.toString(annualFallFertilizerModifier) );
		debugLog(" Perennial Fall Fertilizer from client = " + Float.toString(perennialFallFertilizerModifier) );
		
		// Till SoilLoss Multiplier
		float T_M = 1.0f;
		// Cover Crop Multiplier
		float CC_M = 1.0f;
		// Fertilizer Multiplier
		float F_M = 1.0f;
		
		// Constant for input layers
		float cropRotation = 0;
		float fertRate = 0;
		
		// Calculate Nitrous Oxide Emissions
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				
				int landCover = rotationData[y][x];
				
				// Only process pixels for the landcover types we care about. Skip any cell that would
				//	have a no-data value as any part of the computation
				if ((landCover & TotalMask) > 0 &&
					n2o_composite[y][x] > -9999.0f)
				{
					fertRate = 0.0f;
					
					if ((landCover & Corn_Mask) > 0) { // CORN
						cropRotation = 0.0f;
						fertRate = 168.0f;
						
						// Return tillage modifier if cell is Tilled
						T_M = ManagementOptions.E_Till.getIfActive(landCover, annualTillageModifier, 1.0f);
						CC_M = ManagementOptions.E_CoverCrop.getIfActive(landCover, annualCoverCropModifier, 1.0f);
						F_M = ManagementOptions.getFertilizerMultiplier(landCover, 
									1.0f, 1.0f, // these values correspond to NO Fert multiplier and synthetic multiplier
									annualFallFertilizerModifier, annualFertilizerModifier);
					}
					else if ((landCover & Grass_Mask) > 0) { // GRASS
						cropRotation = -1.268f;
						// Is that right or this belongs to Soy?
						fertRate = 56.0f;
						
						// Return tillage modifier if cell is Tilled
						F_M = ManagementOptions.getFertilizerMultiplier(landCover, 
									1.0f, 1.0f, // these values correspond to NO Fert multiplier and synthetic multiplier
									perennialFallFertilizerModifier, perennialFertilizerModifier);
					}
					else if ((landCover & Soy_Mask) > 0) { // SOY
						cropRotation = -1.023f;
						
						// Return tillage modifier if cell is Tilled
						T_M = ManagementOptions.E_Till.getIfActive(landCover, annualTillageModifier, 1.0f);
						CC_M = ManagementOptions.E_CoverCrop.getIfActive(landCover, annualCoverCropModifier, 1.0f);
						F_M = ManagementOptions.getFertilizerMultiplier(landCover, 
									1.0f, 1.0f, // these values correspond to NO Fert multiplier and synthetic multiplier
									annualFallFertilizerModifier, annualFertilizerModifier);
					}
					else if ((landCover & Alfalfa_Mask) > 0) { // ALFALFA
						cropRotation = -1.023f;
						
						// Return tillage modifier if cell is Tilled
						F_M = ManagementOptions.getFertilizerMultiplier(landCover, 
									1.0f, 1.0f, // these values correspond to NO Fert multiplier and synthetic multiplier
									perennialFallFertilizerModifier, perennialFertilizerModifier);
					}
					
					// Calculate Nitrous Oxide Emissions in Kg per Ha and then convert to Mg per cell per year
					nitrousOxideData[y][x] = ((float)(Math.exp(fertRate * 0.005f + cropRotation +
						n2o_composite[y][x]))) * 900.0f * 0.0001f * 0.001f;
					
					// Change value using multiplier
					nitrousOxideData[y][x] *=  T_M * CC_M * F_M;
					
				}
				else { // else not a landcover type we care about or one of the 4 input cells had no-data
					nitrousOxideData[y][x] = -9999.0f;
				}
			}
		}
	
		
		List<ModelResult> results = new ArrayList<ModelResult>();
		
		results.add(new ModelResult(mModelFile, scenario.mOutputDir, nitrousOxideData, width, height));

		debugLog(">>> Model Nitrous Oxide - timing (ms): " + timer.stringMilliseconds(2));

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
