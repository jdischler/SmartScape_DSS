package analysis;

//------------------------------------------------------------------------------
public class ModelResult
{
	public String mName;
	public String mDestinationFolder; // for saving
	public float [][] mRasterData;
	public int mWidth, mHeight;

	//----------------------------------------------------------------------
	public ModelResult(String name, String folder, float [][] data, int width, int height) {

		mName = name;
		mDestinationFolder = folder;
		mRasterData = data;
		mWidth = width;
		mHeight = height;
	}
}
	
