package transformData;

// TODO: consider some way to capture the Min and Max value and return that??
//------------------------------------------------------------------------------
public class Downsampler
{
	// FLOAT: Returns a transformed data array of the requested size. Sampling is done via averaging
	// 
	//--------------------------------------------------------------------------
	static public float [][] generateAveraged(float[][] data, 
								int dataWidth, int dataHeight, 
								int newWidth, int newHeight) {
	
		float [][] resampledData = new float[newHeight][newWidth];
		
		float widthFactor = (dataWidth / newWidth);
		float heightFactor = (dataHeight / newHeight);
		float halfWidthFactor = widthFactor * 0.5f;
		float halfHeightFactor = heightFactor * 0.5f;
		
		for (int y = 0; y < newHeight; y++) {
			int upLeftY = Math.round(y * heightFactor - halfHeightFactor);
			int lowRightY = Math.round(y * heightFactor + halfHeightFactor);
			
			// clamp to legal range...
			if (upLeftY < 0) upLeftY = 0;
			if (upLeftY >= dataHeight) upLeftY = dataHeight - 1;
			if (lowRightY < 0) lowRightY = 0;
			if (lowRightY >= dataHeight) lowRightY = dataHeight - 1;
			
			for (int x = 0; x < newWidth; x++) {
				int upLeftX = Math.round(x * widthFactor - halfWidthFactor);
				int lowRightX = Math.round(x * widthFactor + halfWidthFactor);
				
				// clamp to legal range...
				if (upLeftX < 0) upLeftX = 0;
				if (upLeftX >= dataWidth) upLeftX = dataWidth - 1;
				if (lowRightX < 0) lowRightX = 0;
				if (lowRightX >= dataWidth) lowRightX = dataWidth - 1;
				
				// Calculate that ave value and stuff it into resampledData[y][x]
				float sum = 0;
				int ct = 0;
				for (int yy = upLeftY; yy <= lowRightY; yy++) {
					for (int xx = upLeftX; xx <= lowRightX; xx++) {
						if (data[yy][xx] > -9999.0f || data[yy][xx] < -9999.1f) {
							sum += data[yy][xx];
							ct++;
						}
					}
				}
				
				float ave = -9999.0f;
				if (ct > 0) {
					ave = sum / ct;
				}
				resampledData[y][x] = ave;
				upLeftX = lowRightX;
			}
			upLeftY = lowRightY;
		}
		
		return resampledData;
	}
	
	// FLOAT: Returns a transformed data array of the requested size. Sampling is done via 
	//	taking MAX absolute value...
	//--------------------------------------------------------------------------
	static public float [][] generateMax(float[][] data, 
								int dataWidth, int dataHeight,
								int newWidth, int newHeight) {
	
		float [][] resampledData = new float[newHeight][newWidth];
		
		float widthFactor = dataWidth / newWidth;
		float heightFactor = dataHeight / newHeight;
		
		int upLeftX = 0, upLeftY = 0;
		
		for (int y = 0; y < newHeight - 1; y++) {
			int lowRightY = Math.round((y + 1) * heightFactor);
			
			for (int x = 0; x < newWidth - 1; x++) {
				int lowRightX = Math.round((x + 1) * widthFactor);
				
				// Find the max value and stuff it into resampledData[y][x]
				float max = -9999.0f;
				boolean mbHasMax = false;
				for (int yy = upLeftY; yy <= lowRightY; yy++) {
					for (int xx = upLeftX; xx <= lowRightX; xx++) {
						float result = data[yy][xx];
						if (result > -9999.0f || result < -9999.1f) {
							if (!mbHasMax) {
								max = result;
								mbHasMax = true;
							}
							// test absolute value so we capture the largest magnitude, be it pos or neg
							if (Math.abs(result) > Math.abs(max)) {
								// but save the original max result, vs. absolute val.
								max = result;
							}
						}
					}
				}
				
				resampledData[y][x] = max;
				upLeftX = lowRightX;
			}
			upLeftY = lowRightY;
		}
		
		return resampledData;
	}
	
	// FLOAT: Returns a transformed data array of the requested size. Sampler tracks percentage
	//	of cells with a non-zero value...then converts that to the specified output range.
	//	e.g., numOutputValues = 3, generates results values that are 0-2
	//--------------------------------------------------------------------------
	static public byte [][] generateSelection(byte [][] data, 
								int dataWidth, int dataHeight,
								int numOutputValues, 
								int newWidth, int newHeight) {
	
		byte [][] resampledData = new byte[newHeight][newWidth];
		
		float widthFactor = dataWidth / newWidth;
		float heightFactor = dataHeight / newHeight;
		
		int upLeftX = 0, upLeftY = 0;
		
		for (int y = 0; y < newHeight - 1; y++) {
			int lowRightY = Math.round((y + 1) * heightFactor);
			
			for (int x = 0; x < newWidth - 1; x++) {
				int lowRightX = Math.round((x + 1) * widthFactor);
				
				// Calculate a percent of selected cells and stuff it into resampledData[y][x]
				int selectedCells = 0, totalCells = 0;
				
				for (int yy = upLeftY; yy <= lowRightY; yy++) {
					for (int xx = upLeftX; xx <= lowRightX; xx++) {
						if (data[yy][xx] > 0) {
							selectedCells++;
						}
						totalCells++;
					}
				}
				
				float res = 0;
				if (totalCells > 0) {
					// round...
					//res = ((float)selectedCells / totalCells) * numOutputValues + 0.5f;
					res = (float) Math.ceil(((float)selectedCells / totalCells) * numOutputValues);
					// clamp...
					if (res < 0) res = 0;
					else if (res > numOutputValues - 1) res = numOutputValues - 1;
				}
				resampledData[y][x] = (byte)res;
				upLeftX = lowRightX;
			}
			upLeftY = lowRightY;
		}
		
		return resampledData;
	}

	// FLOAT: Returns a transformed data array of the requested size. Sampler tracks percentage
	//	of cells with a non-zero value...then converts that to the specified output range.
	//	e.g., numOutputValues = 3, generates results values that are 0-2
	//--------------------------------------------------------------------------
	public static byte[][] generateOccludedSelection(byte[][] data, byte[][] data2, 
			int width, int height,
			int numOutputColors, int newWidth, int newHeight) {
		byte [][] resampledData = new byte[newHeight][newWidth];
		
		float widthFactor = width / newWidth;
		float heightFactor = height / newHeight;
		
		int upLeftX = 0, upLeftY = 0;
		
		for (int y = 0; y < newHeight - 1; y++) {
			int lowRightY = Math.round((y + 1) * heightFactor);
			
			for (int x = 0; x < newWidth - 1; x++) {
				int lowRightX = Math.round((x + 1) * widthFactor);
				
				// Calculate a percent of selected cells and stuff it into resampledData[y][x]
				int selectedCells = 0, totalCells = 0;
				int occludedCells = 0, otherCells = 0;
				
				for (int yy = upLeftY; yy <= lowRightY; yy++) {
					for (int xx = upLeftX; xx <= lowRightX; xx++) {
						if (data2[yy][xx] > 0) {
							selectedCells++;
							if (data[yy][xx] > 0) {
								occludedCells++;
							}
						}
						else if (data[yy][xx] > 0) {
							otherCells++;
						}
						totalCells++;
					}
				}
				
				long res1 = 0, res2 = 0;
				if (totalCells > 0) {
					// rounding up to nearest INT and avoiding integer decision issues in one step...
					//	Example: (91 + 8 - 1) / 8  =>  100 / 8  =>  12
					// A little weird but works out better than Math.ceil because we don't want a double back
					// 	Math.ceil(91 / 8)  =>  Math.ceil(11.375)  => 12.0
					res1 = (int)Math.ceil((double)occludedCells / totalCells * numOutputColors);
					// clamps for safety...
					if (res1 < 0) res1 = 0;
					else if (res1 >= numOutputColors) res1 = numOutputColors - 1;
					
					res2 = (int)Math.ceil((double)(selectedCells-occludedCells) / totalCells * numOutputColors);
					// clamps for safety...
					if (res2 < 0) res2 = 0;
					else if (res2 >= numOutputColors) res2 = numOutputColors - 1;
				}
				byte color = (byte)(res2 + res1 * numOutputColors);
				if (color == 0 && otherCells > 0) {
					res1 = (int)Math.ceil((double)otherCells / totalCells * numOutputColors);
					if (res1 < 0) res1 = 0;
					else if (res1 >= numOutputColors) res1 = numOutputColors - 1;
					color = (byte)(res1 + 9);
				}
				resampledData[y][x] = color;
				upLeftX = lowRightX;
			}
			upLeftY = lowRightY;
		}
		
		return resampledData;
	}

}

