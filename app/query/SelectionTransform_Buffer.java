package query;

import analysis.window.*;

// buffers each selected pixel by the requested amount. Generates a new selection
//------------------------------------------------------------------------------
public class SelectionTransform_Buffer {

	// creates new selection and runs a window transform on the original to populate the copy
	public static Selection transform(Selection sel, int bufferMeters) {
		int pixelBuffer = bufferMeters / 30;
		
		Integer width = sel.mWidth, height = sel.mHeight;
		Selection transformedSel = new Selection(width,height, null);
		
		Moving_SelectionWindow win = new Moving_SelectionWindow_N(pixelBuffer, sel.mRasterData, width, height);
		boolean moreCells = true;
		while (moreCells == true) {
			
			Moving_Window.WindowPoint point = win.getPoint();
			if (win.canGetProportions()) {
				float selectionProp = win.getProportion();
				transformedSel.mRasterData[point.mY][point.mX] = (byte)(selectionProp > 0.0f ? 1 : 0);
			}
			else {
				transformedSel.mRasterData[point.mY][point.mX] = (byte)(0);
			}
			
			moreCells = win.advance();
		}
		
		return transformedSel;
	}
}
