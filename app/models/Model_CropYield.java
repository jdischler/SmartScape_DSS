package models;

import play.*;
import query.Layer_Base;
import query.Layer_Integer;
import query.Scenario;
import utils.ManagementOptions;
import utils.Utils;

//------------------------------------------------------------------------------
// Modeling Process
//
// This model uses slope, soil depth, silt, and CEC to calculate corn, soy, grass, and alfalfa yield 
// This model is from unpublished work by Tim University of Wisconsin Madison
// Inputs are slope, soil depth, silt, CEC layers, and the selected cells in the raster & rotation map 
// Outputs are an array of corn, soy, grass, and alfalfa yields. The units are in tonnes per hectare
// Output is additionally packed into a long. The model clamped values from 0 - 25 so we exploit the fairly
//	small fixed/known range and pack that into the long as described in packYield...
// Depending on the final packing settings, there will be some loss of yield per cell but the fraction
//	is IMHO so low that it's not worth worrying about. Alternately, packYield could track the error of
//	packed vs. actual and then use that to put a compensation factor on a neighbor.  In aggregate,
//	the total error should end up so small as to not be worth worrying about. ..at the expense of
//	individual cells potentially being slightly less accurate.

// Version 08/20/2013
//
//------------------------------------------------------------------------------
public class Model_CropYield extends Model_Base
{
	private static final boolean SELF_DEBUG_LOGGING = false;
	
	// slot is 0-4, which corresponds to 5 - 12-bit chunks in a 64 bit long
	// The combination of slots vs. bits per slot is somewhat arbitrary, but this gives us
	// accuracy down to about 0.006... with 5 crops
	private final long packYield(float fValue, long lValue, int slot) {
		
		long scaled = (long)Math.round(fValue * (4095.0f / 25.0f));
		scaled = scaled > 4095 ? 4095 : (scaled < 0 ? 0 : scaled);
		return lValue |= (scaled << (slot * 12));
	}
	
	//--------------------------------------------------------------------------
	public long[][] run(Scenario scenario) {

		int[][] rotationData = scenario.mNewRotation;
		int width = scenario.getWidth(), height = scenario.getHeight();
		
		debugLog(" >> Computing Yield");

		// No Till SoilLoss Multiplier
		float NT_M = 1.0f;
		// Cover Crop Multiplier
		float CC_M = 1.0f;
		
		// Multipliers from client variables
		Float annualNoTillageModifier = 1.0f; //
		Float annualCoverCropModifier = 1.0f;		

		// Get user changeable yield scaling values from the client...
		//----------------------------------------------------------------------
		try {	
			// values come in as straight multiplier
			annualNoTillageModifier = scenario.mAssumptions.getFloat("y_nt_annuals");
			annualCoverCropModifier = scenario.mAssumptions.getFloat("y_cc_annuals");		
		}
		catch (Exception e) {
			Logger.warn(e.toString());
		}
		
		debugLog(" Agricultural no till from client = " + annualNoTillageModifier);
		debugLog(" Agricultural cover crop from client = " + annualCoverCropModifier);
		
		// Mask
		Layer_Integer wl = (Layer_Integer)Layer_Base.getLayer("wisc_land"); 
		int Grass_Mask = wl.stringToMask("hay","pasture","cool-season grass","warm-season grass");
		int Corn_Mask = wl.stringToMask("continuous corn","dairy rotation","cash grain");
		int Soy_Mask = wl.stringToMask("cash grain");
		int Alfalfa_Mask = wl.stringToMask("dairy rotation");
		int TotalMask = Grass_Mask | Corn_Mask | Soy_Mask | Alfalfa_Mask;
		
		// Corn and Grass Yield
		float Corn_Y = 0;
		float Grass_Y = 0;
		float Soy_Y = 0;
		float Alfalfa_Y = 0;
		
		float Yield = 0;

		// Yield Modification/Scalar - default to multiply by 1.0, which is NO change
		Float cornYieldModifier = 1.0f; //
		Float soyYieldModifier = 1.0f; //
		Float alfalfaYieldModifier = 1.0f; //
		Float grassYieldModifier = 1.0f; //
		
		// Get user changeable yield scaling values from the client...
		//----------------------------------------------------------------------
		try {	
			// Value comes in as a percent, e.g. -5%...convert to a multiplier
			cornYieldModifier = scenario.mAssumptions.getFloat("ym_corn") / 100.0f + 1.0f;
			soyYieldModifier = scenario.mAssumptions.getFloat("ym_soy") / 100.0f + 1.0f;
			alfalfaYieldModifier = scenario.mAssumptions.getFloat("ym_alfalfa") / 100.0f + 1.0f;
			grassYieldModifier = scenario.mAssumptions.getFloat("ym_grass") / 100.0f + 1.0f;
		}
		catch (Exception e) {
			Logger.warn(e.toString());
		}
		
		long[][] yield = new long[height][width];
		debugLog("  > Allocated memory for Yield");
		
		float slope[][] = Layer_Base.getLayer("Slope").getFloatData();
		float silt[][] = Layer_Base.getLayer("Silt").getFloatData();
		float depth[][] = Layer_Base.getLayer("Depth").getFloatData();
		float cec[][] = Layer_Base.getLayer("CEC").getFloatData();
		
		float cornCoefficient = 1.30f 	// correction for technological advances 
				* 2.0f 					// contribution of stover 
				* 0.053f 				// conversion to Mg per Ha 
				* cornYieldModifier;	// user-set assumption
		
		float grassCoefficient = 1.05f 	// correction for technological advances
				* 1.91f					// conversion to Mg per Ha
				* grassYieldModifier;	// user-set assumption
		
		float soyCoefficient = 1.2f		// Correct for techno advances
				* 0.0585f				// conversion to Mg per Ha
				* 2.5f					// contribution of soy residue
				* soyYieldModifier;		// user-set assumption

		float alfalfaCoefficient = 1.05f // Correction Factor for modern yield
				* 1.905f				// conversion to Mg per Ha
				* alfalfaYieldModifier;	// user-set assumption

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				
				int landCover = rotationData[y][x];
									
				if ((landCover & TotalMask) <= 0 || slope[y][x] < 0 || 
						depth[y][x] < 0 || silt[y][x] < 0 || cec[y][x] < 0) {
					
					yield[y][x] = -1;
					continue;
				}
				long packedYield = 0;
				
				if ((landCover & Corn_Mask) > 0) {
					NT_M = ManagementOptions.E_Till.getIfActive(landCover, 1.0f, annualNoTillageModifier);
					CC_M = ManagementOptions.E_CoverCrop.getIfActive(landCover, annualCoverCropModifier, 1.0f);
					Corn_Y = 22.0f - 1.05f * slope[y][x] + 0.19f * depth[y][x] + 0.817f * silt[y][x] + 1.32f * cec[y][x];
					// Combined corn coefficients & Land management factors
					Corn_Y = Corn_Y * cornCoefficient * NT_M * CC_M;
					packedYield = packYield(Corn_Y, packedYield, 0);
				}
				if ((landCover & Soy_Mask) > 0) {
					NT_M = ManagementOptions.E_Till.getIfActive(landCover, 1.0f, annualNoTillageModifier);
					CC_M = ManagementOptions.E_CoverCrop.getIfActive(landCover, annualCoverCropModifier, 1.0f);
					// Bushels per acre
					Soy_Y = 6.37f - 0.34f * slope[y][x] + 0.065f * depth[y][x] + 0.278f * silt[y][x] + 0.437f * cec[y][x];
					Soy_Y = Soy_Y * soyCoefficient * NT_M * CC_M;
					packedYield = packYield(Soy_Y, packedYield, 1);
				}
				if ((landCover & Alfalfa_Mask) > 0) {
					// Short tons per A\acre
					Alfalfa_Y = 1.26f - 0.045f * slope[y][x] + 0.007f * depth[y][x] + 0.027f * silt[y][x] + 0.041f * cec[y][x];
					Alfalfa_Y = Alfalfa_Y * alfalfaCoefficient;
					packedYield = packYield(Alfalfa_Y, packedYield, 2);
				}
				if ((landCover & Grass_Mask) > 0) {
					// short tons per acre
					Grass_Y = 0.77f - 0.031f * slope[y][x] + 0.008f * depth[y][x] + 0.029f * silt[y][x] + 0.038f * cec[y][x];
					Grass_Y = Grass_Y * grassCoefficient;
					packedYield = packYield(Grass_Y, packedYield, 3);
				}
				
				yield[y][x] = packedYield;
			}
		}
		
		return yield;
	}
	
	//-------------------------------------------------------------------------------------------
	@SuppressWarnings("unused")
	private static final void debugLog(String conditionalLog) {
		
		if (ALL_MODELS_DEBUG_LOGGING || SELF_DEBUG_LOGGING) {
			Logger.debug(conditionalLog);
		}
	}
}

