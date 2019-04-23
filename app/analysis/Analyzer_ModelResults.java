package analysis;

import query.*;
import utils.Json;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;

import play.Logger;

//------------------------------------------------------------------------------
public class Analyzer_ModelResults
{
	//--------------------------------------------------------------------------
	public static JsonNode get(JsonNode types) {

		JsonNode counties = types.get("counties"),
				watersheds = types.get("watersheds");
		
		Layer_Integer county = (Layer_Integer)Layer_Base.getLayer("counties");
		Layer_Integer huc = (Layer_Integer)Layer_Base.getLayer("huc-10");
		int countyData[][] = county.getIntData();
		int watershedData[][] = huc.getIntData();

		Layer_Integer wl = (Layer_Integer)Layer_Base.getLayer("wisc_land"); 
		
		int width = wl.getWidth(), height = wl.getHeight();
		int data[][] = wl.getIntData();
		int totalMask = wl.stringToMask("hay","pasture","cool-season grass","warm-season grass",
				"continuous corn","dairy rotation","cash grain",
				"urban","suburban",
				"wetland","open water",
				"coniferous","deciduous","mixed woodland");
		
		Layer_Float pollinators = (Layer_Float)Layer_Base.getLayer("default/pollinator");
		float polData[][] = pollinators.getFloatData();
		double polSum = 0, polCt = 0;

		Layer_Float bird = (Layer_Float)Layer_Base.getLayer("default/habitat_index");
		float birdData[][] = bird.getFloatData();
		double birdSum = 0, birdCt = 0;

		Layer_Float pest = (Layer_Float)Layer_Base.getLayer("default/pest");
		float pestData[][] = pest.getFloatData();
		double pestSum = 0, pestCt = 0;

		Layer_Float netInc = (Layer_Float)Layer_Base.getLayer("default/net_income");
		float netIncData[][] = netInc.getFloatData();
		double netIncSum = 0, netIncCt = 0;

		Layer_Float soilLoss = (Layer_Float)Layer_Base.getLayer("default/soil_loss");
		float soilLossData[][] = soilLoss.getFloatData();
		double soilLossSum = 0, soilLossCt = 0;

		Layer_Float soilCarbon = (Layer_Float)Layer_Base.getLayer("default/soc");
		float soilCarbonData[][] = soilCarbon.getFloatData();
		double soilCarbonSum = 0, soilCarbonCt = 0;
		
		Layer_Float ethanol = (Layer_Float)Layer_Base.getLayer("default/ethanol");
		float ethanolData[][] = ethanol.getFloatData();
		double ethanolSum = 0, ethanolCt = 0;
		
		Layer_Float n20 = (Layer_Float)Layer_Base.getLayer("default/nitrous_oxide");
		float n20Data[][] = n20.getFloatData();
		double n20Sum = 0, n20Ct = 0;
		
		Boolean hasCounties = false;
		Set<Integer> countyId = new HashSet<Integer>();
		if (counties != null && counties.isArray()) {
			ArrayNode ar = (ArrayNode)counties;
			for (int i = 0; i < ar.size(); i++) {
				JsonNode nd = ar.get(i);
				if (nd.canConvertToInt()) {
					hasCounties = true;
					countyId.add(nd.asInt());
				}
			}
		}

		Boolean hasWatersheds = false;
		Set<Integer> watershedId = new HashSet<Integer>();
		if (watersheds != null && watersheds.isArray()) {
			ArrayNode ar = (ArrayNode)watersheds;
			for (int i = 0; i < ar.size(); i++) {
				JsonNode nd = ar.get(i);
				if (nd.canConvertToInt()) {
					hasWatersheds = true;
					watershedId.add(nd.asInt());
				}
			}
		}
		
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				
				int landCover = data[y][x];
				if ((landCover & totalMask) <= 0) continue;
				if (hasCounties && !countyId.contains(countyData[y][x])) continue;
				if (hasWatersheds && !watershedId.contains(watershedData[y][x])) continue;
				if (polData[y][x] > -9999.0f) {
					polSum += polData[y][x]; polCt++;
				}
				if (birdData[y][x] > -9999.0f) {
					birdSum += birdData[y][x]; birdCt++;
				}
				if (pestData[y][x] > -9999.0f) {
					pestSum += pestData[y][x]; pestCt++;
				}
				if (netIncData[y][x] > -9999.0f) {
					netIncSum += netIncData[y][x]; netIncCt++;
				}
				if (soilLossData[y][x] > -9999.0f) {
					soilLossSum += soilLossData[y][x]; soilLossCt++;
				}
				if (soilCarbonData[y][x] > -9999.0f) {
					soilCarbonSum += soilCarbonData[y][x]; soilCarbonCt++;
				}
				if (ethanolData[y][x] > -9999.0f) {
					ethanolSum += ethanolData[y][x]; ethanolCt++;
				}
				if (n20Data[y][x] > -9999.0f) {
					n20Sum += n20Data[y][x]; n20Ct++;
				}
			}
		}
		
		ObjectNode ob = JsonNodeFactory.instance.objectNode();
		ob.put("pl", polSum / polCt);
		ob.put("bh", birdSum / birdCt);
		ob.put("ps", pestSum / pestCt);
		ob.put("ni", netIncSum / netIncCt);
		ob.put("sl", soilLossSum / soilLossCt);
		ob.put("sc", soilCarbonSum / soilCarbonCt);
		ob.put("gb", ethanolSum / ethanolCt);
		ob.put("em", n20Sum / n20Ct);

		return ob;
	}
}

