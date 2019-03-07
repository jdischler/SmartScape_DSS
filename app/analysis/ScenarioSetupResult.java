package analysis;

//------------------------------------------------------------------------------
public class ScenarioSetupResult
{
	public String mDestinationFolder; // for saving
	public int [][] mLandscapeData;
	public byte [][] mSelectionData;
	public int mWidth, mHeight;

	//----------------------------------------------------------------------
	public ScenarioSetupResult(String folder, 
				int [][] landscapeData, byte [][] selectionData, 
				int width, int height) {

		mDestinationFolder = folder;
		mLandscapeData = landscapeData;
		mSelectionData = selectionData;
		mWidth = width;
		mHeight = height;
	}
}
	
