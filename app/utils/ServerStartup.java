package utils;

import java.awt.Rectangle;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;
import javax.inject.Singleton;

import fileHandling.QueuedWriter;
import play.inject.ApplicationLifecycle;
import query.Layer_Base;
import query.Layer_Manager;

//-----------------------------------------------------------------------
@Singleton
public class ServerStartup {
	
	@Inject
	public ServerStartup(ApplicationLifecycle lifecycle) {
		
		Layer_Manager.computeLayers();
		Layer_Manager.cacheLayers();

		prepareSubsets();
		
		QueuedWriter.launchQueuedWriter();
		
        lifecycle.addStopHook(() -> {
        	Layer_Base.removeAllLayers();
        	QueuedWriter.shutdownQueuedWriter();

            return CompletableFuture.completedFuture(null);
        }); 
     
        Subset.getSubset(0);
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
}


