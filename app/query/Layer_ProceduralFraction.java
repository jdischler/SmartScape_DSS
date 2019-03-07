package query;

import play.*;
import utils.Json;

import java.util.*;
import java.nio.*;

import com.fasterxml.jackson.databind.*;

//------------------------------------------------------------------------------
public class Layer_ProceduralFraction extends Layer_Base
{
	private static final String NAME = "proceduralFraction";

	// TODO: adding a procedural layer type kind of changes the pattern and needs here...
	//	Consider refactoring the base such that there is a Layer_Data type class that
	//	is the base for disk related layers?
	//--------------------------------------------------------------------------
	protected void allocMemory() {}
	protected void onLoadEnd() {
		Logger.info("+-------------------------------------------------------+");
		Logger.info("| Procedural Layer Added: " + mName);
		Logger.info("+-------------------------------------------------------+");
		Logger.info("");
	}
	protected void processASC_Line(int y, String lineElementsArray[]) {}
	protected void readCopy(ByteBuffer dataBuffer, int width, int atY) {}
	protected void writeCopy(ByteBuffer dataBuffer, int width, int atY) {}

	//--------------------------------------------------------------------------
	public Layer_ProceduralFraction() {
		super(NAME);
	}
	
	//--------------------------------------------------------------------------
	@Override
	public void init() {
		// procedural layers, may need to eventually have some kind of 
		//	ProceduralInit function...however, do not need this at this time...
		onLoadEnd();
	}
	
	/* QueryNode would be an object with the following elements:
		fraction: this.getComponent('DSS_FractionOfLand').getValue(),
		gridCellSize: 30, // 30 x 30 raster cells, or 900 x 900 meters, at 30m resolution
		seed: this.DSS_Seed
	*/		
	//--------------------------------------------------------------------------
	protected Selection query(JsonNode queryNode, Selection selection) {

		Logger.info("Running Procedural Fraction Query");
		
		float fraction = 0.5f;
		int seed = 0;
		int gridSize = 30; // 30 raster cells wide...
		
		try {
			fraction = Json.safeGetOptionalFloat(queryNode, "fraction", 50.0f) / 100.0f;
			seed = Json.safeGetOptionalInteger(queryNode, "seed", 12345);
			gridSize = Json.safeGetOptionalInteger(queryNode, "gridCellSize", 30);
			if (gridSize < 1) gridSize = 1;
			if (gridSize > 100) gridSize = 100;			
		}
		catch(Exception e) {
			
		}

		// Now process it!!
		Random rand = new Random(seed);
		
		int cellY = 0;
		while(cellY < mHeight) {
			int cellX = 0;
			while(cellX < mWidth) {
				
				if (rand.nextFloat() > fraction) {
					
					// calculate how many times to iterate and clamp to grid...
					int toY = Math.min(cellY + gridSize, mHeight);
					int toX = Math.min(cellX + gridSize, mWidth);
					
					for (int y = cellY; y < toY; y++) {
						for (int x = cellX; x < toX; x++) {
							selection.mRasterData[y][x] = 0;
						}
					}
				}
				cellX += gridSize;
			}
			cellY += gridSize;
		}
		
		return selection;
	}
}

