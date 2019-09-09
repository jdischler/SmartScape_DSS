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
// This program uses corn, soy, grass and alfalfa production to calculate Soil Carbon 
// This model is from unpublished work by Tim University of Wisconsin Madison
// Inputs are corn, soy, grass and alfalfa layers and crop rotation layer 
// Output is ASCII map of SOC
// Version 08/20/2013
//
// TODO: needs updating for supporting rotations in a cell.
//
//------------------------------------------------------------------------------
public class Model_SoilCarbon extends Model_Base {
	
	private static final boolean SELF_DEBUG_LOGGING = false;

	private static final String mModelFile = "soc";
	
	// Raw Soil Carbon Change Factor (RSCCF) 
	static final float RSCCF_Corn_Grass = 0.63f; // Continuous Corn to Grass
	static final float RSCCF_Corn_Alfalfa = 0.37f; // Continuous Corn to Alfalfa
	static final float RSCCF_Soy_Grass = 0.63f; // Continuous Soy to Grass
	static final float RSCCF_Soy_Alfalfa = 0.37f; // Continuous Soy to Alfalfa
	static final float RSCCF_Alfalfa_Grass = 0.59f; // Continuous Grass to Alfalfa
	
	//--------------------------------------------------------------------------
	public List<ModelResult> run(Scenario scenario) {

		int[][] rotationT_Data = scenario.mNewRotation;
		int width = scenario.getWidth(), height = scenario.getHeight();
		
		debugLog(">>> Computing Soil Carbon Index");
		PerformanceTimer timer = new PerformanceTimer();
		
		float [][] soilCarbonData = new float[height][width];
		debugLog("  > Allocated memory for SOC");

		Layer_Integer cdl = (Layer_Integer)Layer_Base.getLayer("cdl_2012"); 
		int Grass_Mask = cdl.stringToMask("grass");
		int Corn_Mask = cdl.stringToMask("corn");
		int Soy_Mask = cdl.stringToMask("soy");
		int Alfalfa_Mask = cdl.stringToMask("Alfalfa");
		
		int TotalMask = Grass_Mask | Corn_Mask | Soy_Mask | Alfalfa_Mask;
		float factor = -1.0f;
		float adjFactor = -1.0f;
		
		// Multipliers from client variables
		float annualNoTillageModifier = 1.0f;
		float annualCoverCropModifier = 1.0f;		
		float annualFertilizerModifier = 1.0f;
		float perennialFertilizerModifier = 1.0f;
		float annualFallFertilizerModifier = 1.0f;
		float perennialFallFertilizerModifier = 1.0f;
		
		// Get user changeable yield scaling values from the client...
		//----------------------------------------------------------------------
		try {	
			// values come in as straight multiplier
			annualNoTillageModifier = 1 + (scenario.mAssumptions.getFloat("soc_nt_annuals") - 1) / 20;
			annualCoverCropModifier = 1 + (scenario.mAssumptions.getFloat("soc_cc_annuals") - 1) / 20;		
			annualFertilizerModifier = 1 + (scenario.mAssumptions.getFloat("soc_m_annuals") - 1) / 20;
			perennialFertilizerModifier = 1 + (scenario.mAssumptions.getFloat("soc_m_perennials") - 1) / 20;	
			annualFallFertilizerModifier = 1 + (scenario.mAssumptions.getFloat("soc_fm_annuals") - 1) / 20;
			perennialFallFertilizerModifier = 1 + (scenario.mAssumptions.getFloat("soc_fm_perennials") - 1) / 20; 	
		}
		catch (Exception e) {
			Logger.warn(e.toString());
		}
		
		debugLog(" Agricultural no till from client = " + Float.toString(annualNoTillageModifier) );
		debugLog(" Agricultural cover crop from client = " + Float.toString(annualCoverCropModifier) );
		debugLog(" Agricultural Fertilizer from client = " + Float.toString(annualFertilizerModifier) );
		debugLog(" Perennial Fertilizer from client = " + Float.toString(perennialFertilizerModifier) );
		debugLog(" Annual Fall Fertilizer from client = " + Float.toString(annualFallFertilizerModifier) );
		debugLog(" Perennial Fall Fertilizer from client = " + Float.toString(perennialFallFertilizerModifier) );
		
		// No Till Multiplier
		float NT_M = 1.0f;
		// Cover Crop Multiplier
		float CC_M = 1.0f;
		// Fertiliezer Multiplier
		float F_M = 1.0f;
		
		int [][] rotationD_Data = cdl.getIntData();
		// Mg per Ha
		float[][] SOC = Layer_Base.getLayer("SOC").getFloatData();
		
		// Soil_Carbon
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if ((rotationD_Data[y][x] & TotalMask) > 0 /*&& scenario.mSelection.mRasterData[y][x] >= 1*/)
				{
					int landCover_D = rotationD_Data[y][x];
					int landCover_T = rotationT_Data[y][x];
					
					// NoData
					if (landCover_D == 0 || landCover_T == 0 || SOC[y][x] < 0.0f) 
					{
						soilCarbonData[y][x] = -9999.0f;
					}
					// Transform
					else if (landCover_D != landCover_T) 
					{
						//factor = 0.0f;
						// ----- CORN to...
						if ((landCover_D & Corn_Mask) > 0) {
							if ((landCover_T & Grass_Mask) > 0) {
								factor = RSCCF_Corn_Grass; // ...GRASS
								// Return tillage modifier if cell is Tilled
								F_M = ManagementOptions.getFertilizerMultiplier(landCover_T, 
											1.0f, 1.0f, // these values correspond to NO Fert multiplier and synthetic multiplier
											perennialFallFertilizerModifier, perennialFertilizerModifier);
							}
							else if ((landCover_T & Alfalfa_Mask) > 0) {
								factor = RSCCF_Corn_Alfalfa; // ...ALFALFA
								// Return tillage modifier if cell is Tilled
								F_M = ManagementOptions.getFertilizerMultiplier(landCover_T, 
											1.0f, 1.0f, // these values correspond to NO Fert multiplier and synthetic multiplier
											perennialFallFertilizerModifier, perennialFertilizerModifier);
							}
						} // ---- SOY to...
						else if ((landCover_D & Soy_Mask) > 0) {
							if ((landCover_T & Grass_Mask) > 0) {
								factor = RSCCF_Soy_Grass; // ...GRASS
								// Return tillage modifier if cell is Tilled
								F_M = ManagementOptions.getFertilizerMultiplier(landCover_T, 
											1.0f, 1.0f, // these values correspond to NO Fert multiplier and synthetic multiplier
											perennialFallFertilizerModifier, perennialFertilizerModifier);
							}
							else if ((landCover_T & Alfalfa_Mask) > 0) {
								factor = RSCCF_Soy_Alfalfa; // ...ALFALFA
								// Return tillage modifier if cell is Tilled
								F_M = ManagementOptions.getFertilizerMultiplier(landCover_T, 
											1.0f, 1.0f, // these values correspond to NO Fert multiplier and synthetic multiplier
											perennialFallFertilizerModifier, perennialFertilizerModifier);
							}
						} // ---- ALFALFA to...
						else if ((landCover_D & Alfalfa_Mask) > 0) {
							if ((landCover_T & Grass_Mask) > 0) {
								factor = RSCCF_Alfalfa_Grass; // ...GRASS
								// Return tillage modifier if cell is Tilled
								F_M = ManagementOptions.getFertilizerMultiplier(landCover_T, 
											1.0f, 1.0f, // these values correspond to NO Fert multiplier and synthetic multiplier
											perennialFallFertilizerModifier, perennialFertilizerModifier);
							} 
							else if ((landCover_T & Corn_Mask) > 0) {
								factor = -RSCCF_Corn_Alfalfa; // ...CORN
								// Return tillage modifier if cell is Tilled
								NT_M = ManagementOptions.E_Till.getIfActive(landCover_T, 1.0f, annualNoTillageModifier);
								CC_M = ManagementOptions.E_CoverCrop.getIfActive(landCover_T, annualCoverCropModifier, 1.0f);
								F_M = ManagementOptions.getFertilizerMultiplier(landCover_T, 
											1.0f, 1.0f, // these values correspond to NO Fert multiplier and synthetic multiplier
											annualFallFertilizerModifier, annualFertilizerModifier);
							}
							else if ((landCover_T & Soy_Mask) > 0) {
								factor = -RSCCF_Soy_Alfalfa; // ...SOY
								// Return tillage modifier if cell is Tilled
								NT_M = ManagementOptions.E_Till.getIfActive(landCover_T, 1.0f, annualNoTillageModifier);
								CC_M = ManagementOptions.E_CoverCrop.getIfActive(landCover_T, annualCoverCropModifier, 1.0f);
								F_M = ManagementOptions.getFertilizerMultiplier(landCover_T, 
											1.0f, 1.0f, // these values correspond to NO Fert multiplier and synthetic multiplier
											annualFallFertilizerModifier, annualFertilizerModifier);
							}
						} // ---- GRASS to...
						else if ((landCover_D & Grass_Mask) > 0) {
							if ((landCover_T & Corn_Mask) > 0) {
								factor = -RSCCF_Corn_Grass; // ...CORN
								// Return tillage modifier if cell is Tilled
								NT_M = ManagementOptions.E_Till.getIfActive(landCover_T, 1.0f, annualNoTillageModifier);
								CC_M = ManagementOptions.E_CoverCrop.getIfActive(landCover_T, annualCoverCropModifier, 1.0f);
								F_M = ManagementOptions.getFertilizerMultiplier(landCover_T, 
											1.0f, 1.0f, // these values correspond to NO Fert multiplier and synthetic multiplier
											annualFallFertilizerModifier, annualFertilizerModifier);
							}
							else if ((landCover_T & Soy_Mask) > 0) {
								factor = -RSCCF_Soy_Grass; // ...SOY
								// Return tillage modififier if cell is Tilled
								NT_M = ManagementOptions.E_Till.getIfActive(landCover_T, 1.0f, annualNoTillageModifier);
								CC_M = ManagementOptions.E_CoverCrop.getIfActive(landCover_T, annualCoverCropModifier, 1.0f);
								F_M = ManagementOptions.getFertilizerMultiplier(landCover_T, 
											1.0f, 1.0f, // these values correspond to NO Fert multiplier and synthetic multiplier
											annualFallFertilizerModifier, annualFertilizerModifier);
							}
							else if ((landCover_T & Alfalfa_Mask) > 0) {
								factor = -RSCCF_Alfalfa_Grass; // ...ALFALFA
								// Return tillage modifier if cell is Tilled
								F_M = ManagementOptions.getFertilizerMultiplier(landCover_T, 
											1.0f, 1.0f, // these values correspond to NO Fert multiplier and synthetic multiplier
											perennialFallFertilizerModifier, perennialFertilizerModifier);
							}
						}
						else
						{
							factor = 1;
						}
						
						// Calculate equation based on calculated factor and SOC layer
						adjFactor = -0.5938f * (float)(Math.log(SOC[y][x] * 0.1f)) + 1.6524f;
						
						if (adjFactor <= 0.2f) {
							adjFactor = 0.2f;
						}
						else if (adjFactor >= 1.2f) {
							adjFactor = 1.2f;
						}
						
						// Mg per Ha and convert to Mg per cell
						soilCarbonData[y][x] = (SOC[y][x] * (1 + (factor * adjFactor) / 20.0f)) * 900.0f / 10000.0f;
						
						// Change value using practice multipliers
						soilCarbonData[y][x] *=  NT_M * CC_M * F_M;
					}
					// Default
					else {
						// Someone though that it didn't make sense to modify default SOC based on practices...
						// however, it seems like it ought to matter?
						/*
						// Corn
						if ((landCover_D & Corn_Mask) > 0) {
							// Return tillage modifier if cell is Tilled
							NT_M = ManagementOptions.E_Till.getIfActiveOn(landCover_D, 1.0f, annualNoTillageModifier);
							CC_M = ManagementOptions.E_CoverCrop.getIfActiveOn(landCover_D, annualCoverCropModifier, 1.0f);
							F_M = ManagementOptions.getFertilizerMultiplier(landCover_D, 
										1.0f, 1.0f, // these values correspond to NO Fert multiplier and synthetic multiplier
										annualFallFertilizerModifier, annualFertilizerModifier);
						}
						// Soy
						else if ((landCover_D & Soy_Mask) > 0) {
							// Return tillage modifier if cell is Tilled
							NT_M = ManagementOptions.E_Till.getIfActiveOn(landCover_D, 1.0f, annualNoTillageModifier);
							CC_M = ManagementOptions.E_CoverCrop.getIfActiveOn(landCover_D, annualCoverCropModifier, 1.0f);
							F_M = ManagementOptions.getFertilizerMultiplier(landCover_D, 
										1.0f, 1.0f, // these values correspond to NO Fert multiplier and synthetic multiplier
										annualFallFertilizerModifier, annualFertilizerModifier);
						}
						else if ((landCover_D & Grass_Mask) > 0) {
							factor = RSCCF_Corn_Grass; // ...GRASS
							// Return tillage modifier if cell is Tilled
							F_M = ManagementOptions.getFertilizerMultiplier(landCover_D, 
										1.0f, 1.0f, // these values correspond to NO Fert multiplier and synthetic multiplier
										perennialFallFertilizerModifier, perennialFertilizerModifier);
						}
						else if ((landCover_D & Alfalfa_Mask) > 0) {
							factor = RSCCF_Corn_Alfalfa; // ...ALFALFA
							// Return tillage modifier if cell is Tilled
							F_M = ManagementOptions.getFertilizerMultiplier(landCover_D, 
										1.0f, 1.0f, // these values correspond to NO Fert multiplier and synthetic multiplier
										perennialFallFertilizerModifier, perennialFertilizerModifier);
						}
						*/
						// Mg per Ha and convert to Mg per cell
						soilCarbonData[y][x] = SOC[y][x] * 900.0f / 10000.0f;
						
						// Change value using multiplier
						//soilCarbonData[y][x] *=  NT_M * CC_M * F_M;
					}
				}
				else {
					soilCarbonData[y][x] = -9999.0f;
				}
			}
		}
		
		List<ModelResult> results = new ArrayList<ModelResult>();
		
		results.add(new ModelResult(mModelFile, scenario.mOutputDir, soilCarbonData, width, height));

		debugLog(">>> Model Soil Carbon is finished - timing (ms): " + timer.stringMilliseconds(2));

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
