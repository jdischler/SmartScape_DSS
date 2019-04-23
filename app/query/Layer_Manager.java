package query;


import play.*;
import query.Layer_Integer.EType;
import utils.PerformanceTimer;

//------------------------------------------------------------------------------
public class Layer_Manager
{

	//--------------------------------------------------------------------------
	public static Layer_Float newFloatLayer(String name) {
		Layer_Float layer = new Layer_Float(name);
		return layer;
	}
	
	//--------------------------------------------------------------------------
	public static Layer_Integer newIntegerLayer(String name, EType layerType) {
		Layer_Integer layer = new Layer_Integer(name, layerType);
		return layer;
	}
	
	//--------------------------------------------------------------------------
	public static Layer_Integer newIntegerLayer(String name) {
		Layer_Integer layer = new Layer_Integer(name, EType.EPreShiftedIndex);
		return layer;
	}

	//--------------------------------------------------------------------------
	public static void computeLayers() {

		Logger.info("Computing layers if needed");
		/* // Uncomment if need to recalculate and output crop rotation
		CropRotation cr = new CropRotation();
		cr.computeRotation();
		*/

	}
	
	//--------------------------------------------------------------------------
	public static void cacheLayers() 
	{
		PerformanceTimer timer = new PerformanceTimer();
		Layer_Base layer;
		try {
			Logger.info("Caching all data layers");
			
			// Queryable layers...though some of these are also used by model computations..
			layer = new Layer_ProceduralFraction(); layer.init();// really has no data...init may not also be needed?
			newIntegerLayer("cdl_2012").init();
			newIntegerLayer("wisc_land").init();
			newFloatLayer("slope").init();
			newFloatLayer("dist_to_water").init();
			newFloatLayer("rivers").init();
			newIntegerLayer("watersheds", Layer_Integer.EType.ERaw).init();
			newIntegerLayer("huc-10", Layer_Integer.EType.ERaw).init();
			newIntegerLayer("counties", Layer_Integer.EType.ERaw).init();
		//	newFloatLayer("cow_index").init();
			
			// Layers for model computation
			newFloatLayer("cec").init();
			newFloatLayer("depth").init();
			newFloatLayer("silt").init();
			newFloatLayer("soc").init();
			newFloatLayer("texture").init();
			newFloatLayer("om_soc").init();
			newFloatLayer("drainage").init();
			newFloatLayer("ph").init();
			newFloatLayer("ls").init();
			newFloatLayer("rainfall_erosivity").init();
			newFloatLayer("soil_erodibility").init();
			newFloatLayer("n2o_composite").init();
			
			// Epic computed data...
			newFloatLayer("alfa_p").init();
			newFloatLayer("corn_p").init();
			newFloatLayer("soy_p").init();
			newFloatLayer("grass_p").init();
			
			newIntegerLayer("ag_lands", Layer_Integer.EType.ERaw).init();
			newIntegerLayer("crp", Layer_Integer.EType.ERaw) // don't do fancy shift/match tricks...there are only two values possible here...
				.setNoDataConversion(0)// work around a data issue - conversion -9999 to zeros
				.init();

			newIntegerLayer("lcc").init();
			newIntegerLayer("lcs").init();
			newFloatLayer("dairy").init();
			newFloatLayer("public_land").init();
		}
		catch (Exception e) {
			Logger.error(e.toString());
		}
		
		Logger.debug(" -Time to cache all layers (s): " + timer.stringSeconds(2));
	}
	
}

