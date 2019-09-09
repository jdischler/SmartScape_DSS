package query;

import play.*;
import java.util.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;

//------------------------------------------------------------------------------
public class SelectionTransform_CriticalMax {

	// modifies selection in place. sample meters is the uniform height/width. 
	//	critical proportion is a value from 0 to 1. Any proportion >= to the crit causes the sample cell to convert to true
	public static Selection transform(Selection sel, int sampleMeters, float criticalProportion) {
		sampleMeters /= 30;
		
		int width = sel.mWidth, height = sel.mHeight;
		
		int cellY = 0;
		while(cellY < height) {
			int cellX = 0;
			while(cellX < width) {
				
				// calculate how many times to iterate and clamp to grid...
				int toY = Math.min(cellY + sampleMeters, height);
				int toX = Math.min(cellX + sampleMeters, width);
				int count = 0;
				for (int y = cellY; y < toY; y++) {
					for (int x = cellX; x < toX; x++) {
						if (sel.mRasterData[y][x] > 0) count++;
					}
				}
				byte result = (byte) ((count >= (toY - cellY) * (toX - cellX) * criticalProportion) ? 1 : 0);
				for (int y = cellY; y < toY; y++) {
					for (int x = cellX; x < toX; x++) {
						sel.mRasterData[y][x] = result;
					}
				}
				
				cellX += sampleMeters;
			}
			cellY += sampleMeters;
		}		
		return sel;
	}
}
