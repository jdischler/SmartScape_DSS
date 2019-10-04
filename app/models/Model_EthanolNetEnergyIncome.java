package models;

import play.*;
import query.Layer_Integer;
import query.Layer_CDL;
import query.Scenario;
import utils.PerformanceTimer;

import java.util.*;

import analysis.ModelResult;

//------------------------------------------------------------------------------
// Modeling Process
//
// This program uses corn, soy, grass and alfalfa production to calculate Net Energy 
// This model is from unpublished work by Tim University of Wisconsin Madison
// Inputs are corn, soy, grass and alfalfa layers and crop rotation layer 
// Outputs are ASCII map of Ethanol, Net_Energy and Net_Income
// Version 08/20/2013
//
//------------------------------------------------------------------------------
public class Model_EthanolNetEnergyIncome extends Model_Base
{
	private static final boolean SELF_DEBUG_LOGGING = false;
		
	private static String mEthanolModelFile = "ethanol";
	private static String mNetEnergyModelFile = "net_energy";
	private static String mNetIncomeModelFile = "net_income";
	
	//--------------------------------------------------------------------------
	public List<ModelResult> run(Scenario scenario) {

		int[][] rotationData = scenario.mNewRotation;
		int width = scenario.getWidth(), height = scenario.getHeight();
		
		debugLog(">>> Computing Model Ethanol / Net Energy / Net Income");
		PerformanceTimer timer = new PerformanceTimer();
		
		// Precompute yield....
		Model_CropYield cropYield = new Model_CropYield();
		cropYield.initialize(scenario);
		long[][] packedYield = null;
		try {
			packedYield = cropYield.run();
		}
		catch(Exception e) {
		}

		float [][] netEnergyData = new float[height][width];
		float [][] netIncomeData = new float[height][width];
		float [][] ethanolData = new float[height][width];
		debugLog("  > Allocated memory for NetEnergy, NetIncome, Fuel");
		
		float Net_Energy_C = 0;
		float Net_Energy_S = 0;
		// Mask
		Layer_Integer wl = Layer_CDL.get(); 
		int Grass_Mask = wl.stringToMask("hay","pasture","cool-season grass","warm-season grass");
		int Corn_Mask = wl.stringToMask("continuous corn","dairy rotation","cash grain");
		int Soy_Mask = wl.stringToMask("cash grain");
		int Alfalfa_Mask = wl.stringToMask("dairy rotation");
		int dr = wl.stringToMask("dairy rotation");
		int cg = wl.stringToMask("cash grain");
		
		// Proportion of Stover 
		float Prop_Stover_Harvest = 0.38f;
		
		// Energy Input at Farm (MJ per Ha)
		float EI_CF = 18151f; // HILL
		float EI_CSF = 2121f; // HILL 1/4 fuel use for stover harvest
		float EI_SF = 6096f; // Hill
		float EI_AF = 9075f; // Corn Grain Hill * 1/2
		float EI_GF = 7411f; // EBAMM
		
		// Energy Input in Processing (MJ per L)
		float EI_CP = 13.99f; // HILL
		float EI_CSP = 1.71f; // EBAMM cellulosic
		float EI_GP = 1.71f; // EBAMM cellulosic
		float EI_SP = 10.39f; // Hill
		float EI_AP = 1.71f; // Corn Grain Hill * 1/2
		
		// Energy output (MJ per L)
		float EO_C = 21.26f + 4.31f; // HILL
		float EO_CS = 21.26f + 3.40f; // EBAMM cellulosic
		float EO_G = 21.26f + 3.40f; // EBAMM cellulosic
		float EO_S = 32.93f + 21.94f; // Hill
		float EO_A = 21.26f + 3.40f; // EBAMM cellulosic
		
		// Conversion Efficiency (L per Mg)
		float CEO_C = 400; // HILL
		float CEO_CS = 380; // EBAMM cellulosic
		float CEO_S = 200; // Hill
		float CEO_A = 380; // EBAMM cellulosic
		float CEO_G = 380; // EBAMM cellulosic
				
		float returnAmount;	// Gross return
		
		// Net Income 
		// Price for production
		Float PC_Cost = 1135f; // $ per ha cost for Corn
		Float PCS_Cost = 4120f; // $ per ha cost for Corn Stover
		Float PG_Cost = 412f; // $ per ha cost for Grass
		Float PS_Cost = 627f; // $ per ha cost for Soy
		Float PA_Cost = 620f; // $ per ha cost for Alfalfa
		
		// Price per tonne for sell
		Float P_Per_Corn = 274f;
		Float P_Per_Stover = 70f;
		Float P_Per_Grass = 107f;
		Float P_Per_Soy = 249f;
		Float P_Per_Alfalfa = 230f;

		// Get user changeable values from the client...
		//----------------------------------------------------------------------
		try {
			// Net Income
			// Production
			PC_Cost = scenario.mAssumptions.getFloat("p_corn_p");
			PCS_Cost = scenario.mAssumptions.getFloat("p_stover_p");
			PG_Cost = scenario.mAssumptions.getFloat("p_grass_p");
			PS_Cost = scenario.mAssumptions.getFloat("p_soy_p");
			PA_Cost = scenario.mAssumptions.getFloat("p_alfalfa_p");
			// Sell
			P_Per_Corn = scenario.mAssumptions.getFloat("p_corn_s");
			P_Per_Stover = scenario.mAssumptions.getFloat("p_stover_s");
			P_Per_Grass = scenario.mAssumptions.getFloat("p_grass_s");
			P_Per_Soy = scenario.mAssumptions.getFloat("p_soy_s");
			P_Per_Alfalfa = scenario.mAssumptions.getFloat("p_alfalfa_s");
		
			// Net Energy
			// Energy Input at Farm
			EI_CF = scenario.mAssumptions.getFloat("e_corn");
			EI_CSF = scenario.mAssumptions.getFloat("e_stover");
			EI_GF = scenario.mAssumptions.getFloat("e_grass");
			EI_SF = scenario.mAssumptions.getFloat("e_soy");
			EI_AF = scenario.mAssumptions.getFloat("e_alfalfa");
			// Conversion Efficiency
			CEO_C = scenario.mAssumptions.getFloat("e_corn_ce");
			CEO_CS = scenario.mAssumptions.getFloat("e_stover_ce");
			CEO_G = scenario.mAssumptions.getFloat("e_grass_ce");
			CEO_S = scenario.mAssumptions.getFloat("e_soy_ce");
			CEO_A = scenario.mAssumptions.getFloat("e_alfalfa_ce");
		}
		
		catch (Exception e) {
			Logger.warn(e.toString());
		}
		
		//----------------------------------------------------------------------		

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				
				long pYield = packedYield[y][x];
				if (pYield < 0) {
					ethanolData[y][x] = -9999.0f;
					netEnergyData[y][x] = -9999.0f;
					netIncomeData[y][x] = -9999.0f;
					continue;
				}

				float ethanol = 0, netEnergy = 0, netIncome = 0;
				
				if ((rotationData[y][x] & Corn_Mask) > 0) {
					float yield = Model_CropYield.unpackYield(pYield, 0);
					float cropProp = 1.0f;
					if ((rotationData[y][x] & cg) > 0) {
						cropProp = 0.5f; yield *= cropProp; 
					}
					else if ((rotationData[y][x] & dr) > 0) {
						cropProp = 0.3333f; yield *= cropProp; 
					}
					
					// L per Ha
					ethanol += yield * 0.5f * CEO_C + yield * 0.25f * CEO_CS;
					// Net_Energy - MJ per Ha
					Net_Energy_C = (yield * 0.5f * CEO_C * EO_C) - (EI_CF + EI_CP * yield * 0.5f * CEO_C);
					Net_Energy_S = (yield * Prop_Stover_Harvest * 0.5f * CEO_CS * EO_CS) - (EI_CSF + EI_CSP * yield * Prop_Stover_Harvest * 0.5f * CEO_CS);
					netEnergy = Net_Energy_C + Net_Energy_S;
					// Gross inc return $ per Ha
					returnAmount = P_Per_Corn * 0.5f * yield + P_Per_Stover * Prop_Stover_Harvest * 0.5f * yield;
					// Net Income $ per Ha
					netIncome = returnAmount - (PC_Cost * cropProp) - (PCS_Cost * cropProp);
				}
				
				if ((rotationData[y][x] & Soy_Mask) > 0) {
					float yield = Model_CropYield.unpackYield(pYield, 1);
					float cropProp = 1.0f;
					if ((rotationData[y][x] & cg) > 0) {
						cropProp = 0.5f; yield *= cropProp; 
					}
					
					// L per Ha
					ethanol += yield * CEO_S;
					// MJ per Ha
					netEnergy += (yield * 0.40f * CEO_S * EO_S) - (EI_SF + EI_SP * yield * CEO_S);
					// Soy return $ per Ha
					returnAmount = P_Per_Soy * yield;
					// Net Income $ per Ha
					netIncome += returnAmount - (PS_Cost * cropProp);
				}
				
				if ((rotationData[y][x] & Alfalfa_Mask) > 0) {
					float yield = Model_CropYield.unpackYield(pYield, 2);
					float cropProp = 1.0f;
					if ((rotationData[y][x] & dr) > 0) {
						cropProp = 0.6667f; yield *= cropProp;
					}
					
					// L per Ha
					ethanol += yield * CEO_A;
					// MJ per Ha
					netEnergy += (yield * CEO_A * EO_A) - (EI_AF + EI_AP * yield * CEO_A);
					// Alfalfa return $ per Ha
					returnAmount = P_Per_Alfalfa * yield;
					// Net Income $ per Ha
					netIncome += returnAmount - (PA_Cost * cropProp);
				}
				
				if ((rotationData[y][x] & Grass_Mask) > 0) {
					float yield = Model_CropYield.unpackYield(pYield, 3);
					
					// L per Ha
					ethanol += yield * CEO_G;
					// MJ per Ha
					netEnergy += (yield * CEO_G * EO_G) - (EI_GF + EI_GP * yield * CEO_G);
					// Gross return $ per ha
					returnAmount = P_Per_Grass * yield;
					// Net Income $ per ha
					netIncome += returnAmount  - PG_Cost;
				}
				
				// Convert L per Ha to L per cell
				//ethanolData[y][x] = Math.round(ethanol * 900.0f / 10000.0f * 100.0f) / 100.0f;
				ethanolData[y][x] = ethanol * 900.0f / 10000.0f;
				// Convert MJ per Ha to MJ per cell
				netEnergyData[y][x] = netEnergy * 900.0f / 10000.0f;
				// Convert $ per Ha to $ per cell
				netIncomeData[y][x] = netIncome * 900.0f / 10000.0f;
			}
		}
		
		List<ModelResult> results = new ArrayList<ModelResult>();
		
		results.add(new ModelResult(mEthanolModelFile, scenario.mOutputDir, ethanolData, width, height));
		results.add(new ModelResult(mNetEnergyModelFile, scenario.mOutputDir, netEnergyData, width, height));
		results.add(new ModelResult(mNetIncomeModelFile, scenario.mOutputDir, netIncomeData, width, height));

		debugLog(">>> Model Ethanol / Net Energy / Net Income finished");
		debugLog(" Execution timing (ms): " + timer.stringMilliseconds(2));

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

