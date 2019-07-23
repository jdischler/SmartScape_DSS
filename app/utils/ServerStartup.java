package utils;

import java.io.File;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;
import javax.inject.Singleton;

import analysis.ModelResult;
import play.Logger;
import play.inject.ApplicationLifecycle;
import query.*;
import resources.Farm;
import models.*;
import fileHandling.QueuedWriter;

//-----------------------------------------------------------------------
@Singleton
public class ServerStartup {
	
	final Boolean 	LOAD_DEFAULT_DATA = true,
					FORCE_COMPUTE_DEFAULT_DATA = false;
	
	@Inject
	public ServerStartup(ApplicationLifecycle lifecycle) {
		
		QueuedWriter.launchQueuedWriter();
		
		Layer_Base.computeLayers();
		Layer_Base.cacheLayers();

		conditionalCreateDefaultModelOutputs();		
		cacheModelDefaults();

		prepareSubsets();
				
        lifecycle.addStopHook(() -> {
        	Layer_Base.removeAllLayers();
        	QueuedWriter.shutdownQueuedWriter();

            return CompletableFuture.completedFuture(null);
        }); 
     
        Subset.getSubset(0);
        
        testFarmProcessing();
	}
	
	private void prepareSubsets() {

		Subset.loadPresets();
/*		Subset s = new Subset();
		s.mSubsetName = "Greater Yahara Watershed";
		s.mSubsetDescription = "A large portion of area in Southern Wisconsin";
		s.mCornerCoordsX = -10062652.65061; // -90°23'39.648"  // -9878152.65061 // -88°44'13.038"
		s.mCornerCoordsY = 5278060.469521415; // 42°46'36.491" (42.77680307700389) // 5415259.640662575 // 43°40'29.415" (43.67483741601681)
		s.mSubsetArea = new Rectangle(0,0,6150,4557);
		
		Subset.defineSubset(s);
		
		s = new Subset();
		s.mSubsetName = "Lesser Yahara Watershed";
		s.mSubsetDescription = "Sauk City to Stoughton";
		s.mCornerCoordsX = -9991929.51; //-9929770.38 // approx 2358 to 4430
		s.mCornerCoordsY = 5356675.84; // 5299587.26 // approx 1943 to 3847
		s.mSubsetArea = new Rectangle(2358,1943, 4430-2358, 3847-1943);
		
		Subset.defineSubset(s);
*/
	}
	
	//--------------------------------------------------------------------------
	private void cacheModelDefaults() {
		
		if (LOAD_DEFAULT_DATA) {
			PerformanceTimer timer = new PerformanceTimer();
			Logger.info(" ... Server is going to load MODEL DEFAULT files ...");
			Layer_Base layer;
			try {
				layer = new Layer_Float("default/net_income"); layer.init();
				layer = new Layer_Float("default/net_energy"); layer.init();
				layer = new Layer_Float("default/ethanol"); layer.init();
				layer = new Layer_Float("default/habitat_index"); layer.init();
				//layer = new Layer_Float("default/water_quality"); layer.init();
				layer = new Layer_Float("default/p_loss_epic"); layer.init();
				layer = new Layer_Float("default/pest"); layer.init();
				layer = new Layer_Float("default/pollinator"); layer.init();
				layer = new Layer_Float("default/nitrous_oxide"); layer.init();
				layer = new Layer_Float("default/soil_loss"); layer.init();
				layer = new Layer_Float("default/soc"); layer.init();
			}
			catch (Exception e) {
				Logger.error(e.toString());
			}
			Logger.debug(" -Time to cache all model defaults (s): " + timer.stringSeconds(2));
		}
		else {
			Logger.info(" ... The Server is skipping loading MODEL DEFAULT files ...");
		}
	}

	// TODO: potentially check for individual model files vs. just the directory?
	//--------------------------------------------------------------------------
	private void conditionalCreateDefaultModelOutputs() {
		
		// Check for Default Scenario files...to replace them, you need to delete the whole
		//	DEFAULT folder otherwise they will not be recalculated with how this is coded
		//	The default folder also cannot exist for first generation (even if empty...)
		File Output = new File("./layerData/default");
		if(!Output.exists() || FORCE_COMPUTE_DEFAULT_DATA) {

			if (FORCE_COMPUTE_DEFAULT_DATA) {
				Logger.info("Forcing Defaults to be recalculated!");
			}
			else {
				Logger.info("Default scenario folder does not exist, creating it and default model files!");
			}
			
			// Rotation
			Layer_Base layer = Layer_Base.getLayer("wisc_land");
			int width = layer.getWidth();
			int height = layer.getHeight();
			
			Scenario scenario = new Scenario(null); // user can be null
			scenario.mNewRotation = layer.getIntData();
			scenario.mSelection = new Selection(width, height);
			scenario.mAssumptions = new GlobalAssumptions();
			scenario.mOutputDir = "default";
			
			List<ModelResult> results;
			results = new Model_HabitatIndex().run(scenario);
			QueuedWriter.queueResults(results);

			results = new Model_EthanolNetEnergyIncome().run(scenario);
			QueuedWriter.queueResults(results);
			
			results = new Model_PollinatorPestSuppression().run(scenario);
			QueuedWriter.queueResults(results);
			
			results = new Model_NitrousOxideEmissions().run(scenario);
			QueuedWriter.queueResults(results);
			
			results = new Model_SoilCarbon().run(scenario);
			QueuedWriter.queueResults(results);
			
			results = new Model_P_LossEpic().run(scenario);
			QueuedWriter.queueResults(results);

			results = new Model_Soil_Loss().run(scenario);
			QueuedWriter.queueResults(results);
			// NOTE: SOC for the default is not in the model run because it is not a computed data layer like others...
			
			// wait for write queue to dump out the defaults...
			while(QueuedWriter.hasFilesQueued()) {
				Logger.info("Waiting for defaults to be written by the QueuedWriter");
				try {
					Thread.sleep(4000);
				}
				catch(Exception e) {
					// blah, java exception handling...
				}
			}
		}
	}
	
	private void testFarmProcessing() {
		
		// Rotation
		Layer_Base layer = Layer_Base.getLayer("wisc_land");
		int width = layer.getWidth();
		int height = layer.getHeight();
		
		Scenario scenario = new Scenario(null); // user can be null
		scenario.mNewRotation = layer.getIntData();
		scenario.mSelection = new Selection(width, height);
		scenario.mAssumptions = new GlobalAssumptions();
		scenario.mOutputDir = "default";
		
		Logger.info(" ... Farm Init ...");
		Farm.init();
		Logger.info(" ... Farm processing ...");
		Farm.processFarms(scenario);
	}
}


