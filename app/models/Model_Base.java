package models;

import play.*;
import java.io.*;

//------------------------------------------------------------------------------
public class Model_Base
{
	private static String mBasePath = "./layerData/";
	
	// MASTER switch to turn on debug logging for all models. If set to false,
	//	any given model itself can opt to turn on logging for just itself;
	protected static final boolean ALL_MODELS_DEBUG_LOGGING = true;	
	
	//--------------------------------------------------------------------------
	protected File getFileForPath(String subPath, String modelFile) {
		
		File testPath = new File(mBasePath + subPath + "/");
		if (!testPath.exists()) {
			Logger.info(testPath.toString() + " does not exist. Attempting to create...");
			testPath.mkdirs();
		}
		return new File(mBasePath + subPath + "/" + modelFile + ".dss");
	}
}

