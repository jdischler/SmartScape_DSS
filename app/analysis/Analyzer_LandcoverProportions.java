package analysis;

import query.*;
import utils.Json;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;

import play.Logger;

//------------------------------------------------------------------------------
public class Analyzer_LandcoverProportions
{
	//--------------------------------------------------------------------------
	public static JsonNode get(JsonNode types) {
	
		JsonNode counties = types.get("counties"),
			watersheds = types.get("watersheds");
		
		Layer_Integer wl = (Layer_Integer)Layer_Base.getLayer("wisc_land"); 
		Layer_Integer county = (Layer_Integer)Layer_Base.getLayer("counties");
		Layer_Integer huc = (Layer_Integer)Layer_Base.getLayer("huc-10");
		
		int width = wl.getWidth(), height = wl.getHeight();
		int data[][] = wl.getIntData();
		int countyData[][] = county.getIntData();
		int watershedData[][] = huc.getIntData();
		
		int hayCt = 0, pastureCt = 0, coolCt = 0, warmCt = 0;
		int hayMask = wl.stringToMask("hay");
		int pastureMask = wl.stringToMask("pasture");
		int coolGrassMask = wl.stringToMask("cool-season grass");
		int warmGrassMask = wl.stringToMask("warm-season grass");
		
		int contCornCt = 0, dairyCt = 0, cashGrainCt = 0;
		int contCornMask = wl.stringToMask("continuous corn");
		int dairyMask = wl.stringToMask("dairy rotation");
		int cashGrainMask = wl.stringToMask("cash grain");
		
		int urbanCt = 0, suburbanCt = 0;
		int urbanMask = wl.stringToMask("urban");
		int suburbanMask = wl.stringToMask("suburban");
		
		int wetlandCt = 0, waterCt = 0;
		int wetlandsMask = wl.stringToMask("wetland");
		int waterMask = wl.stringToMask("open water");
		
		int coniferCt = 0, deciduousCt = 0, mixedCount = 0;
		int coniferMask = wl.stringToMask("coniferous");
		int deciduousMask = wl.stringToMask("deciduous");
		int mixedWoolandMask = wl.stringToMask("mixed woodland");

		int totalCt = 0;
		int totalMask = hayMask | pastureMask | coolGrassMask | warmGrassMask |
				contCornMask | dairyMask | cashGrainMask |
				urbanMask | suburbanMask |
				wetlandsMask | waterMask |
				coniferMask | deciduousMask | mixedWoolandMask;
		
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
				
				totalCt++;

				if ((landCover & hayMask) > 0) 				hayCt++;
				else if ((landCover & pastureMask) > 0) 	pastureCt++;
				else if ((landCover & coolGrassMask) > 0) 	coolCt++;
				else if ((landCover & warmGrassMask) > 0) 	warmCt++;

				else if ((landCover & contCornMask) > 0) 	contCornCt++;
				else if ((landCover & dairyMask) > 0) 		dairyCt++;
				else if ((landCover & cashGrainMask) > 0) 	cashGrainCt++;

				else if ((landCover & urbanMask) > 0) 		urbanCt++;
				else if ((landCover & suburbanMask) > 0) 	suburbanCt++;

				else if ((landCover & wetlandsMask) > 0) 	wetlandCt++;
				else if ((landCover & waterMask) > 0) 		waterCt++;

				else if ((landCover & coniferMask) > 0) 	coniferCt++;
				else if ((landCover & deciduousMask) > 0) 	deciduousCt++;
				else if ((landCover & mixedWoolandMask) > 0)mixedCount++;
			}
		}
		float frac = totalCt / 100.0f;
		
		ArrayNode ar = JsonNodeFactory.instance.arrayNode();
		ar.add(Json.pack(
				"type", "Grasses",
				"val", (hayCt + pastureCt + coolCt + warmCt) / frac,
				"t","Grasses",
				"v", (hayCt + pastureCt + coolCt + warmCt) / frac,
				"sub", Json.pack(
					"Hay", hayCt / frac,
					"Pasture", pastureCt / frac,
					"Cool-Season Grass", coolCt / frac,
					"Warm-Season Grass", warmCt / frac
				)
			));
		ar.add(Json.pack(
				"type", "Row Crops",
				"val", (contCornCt + dairyCt + cashGrainCt) / frac,
				"t", "Row Crops",
				"v", (contCornCt + dairyCt + cashGrainCt) / frac,
				"sub", Json.pack(
					"Continuous Corn", contCornCt / frac,
					"Dairy Rotation", dairyCt / frac,
					"Cash Grain", cashGrainCt / frac
				)
			));
		ar.add(Json.pack(
				"type", "Developed",
				"val", (urbanCt + suburbanCt) / frac,
				"t", "Developed",
				"v", (urbanCt + suburbanCt) / frac,
				"sub", Json.pack(
					"Urban", urbanCt / frac,
					"Other", suburbanCt / frac
				)
			));
		ar.add(Json.pack(
				"type", "Wetlands / Water",
				"val", (waterCt + wetlandCt) / frac,
				"t", "Wetlands / Water",
				"v", (waterCt + wetlandCt) / frac,
				"sub", Json.pack(
					"Open Water", waterCt / frac,
					"Wetlands", wetlandCt / frac
				)
			));
		ar.add(Json.pack(
				"type", "Woodland",
				"val", (coniferCt + deciduousCt + mixedCount) / frac,
				"t", "Woodland",
				"v", (coniferCt + deciduousCt + mixedCount) / frac,
				"sub", Json.pack(
					"Conifers", coniferCt / frac,
					"Deciduous", deciduousCt / frac
				//	"Mixed Conifers/Deciduous", mixedCount / frac // effectively zero?
				)
			));
					
		return ar;
	}
}

