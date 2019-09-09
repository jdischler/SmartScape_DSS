package resources;

import play.*;
import query.Scenario;
import query.Selection;

import java.awt.Point;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import java.util.Map.Entry;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import analysis.Allocate_Yield;
import analysis.ModelResult;
import models.Model_CropYield;
import utils.*;

public class Farm {
	
	public enum Crop {
		E_CORN,
		E_SOY,
		E_GRASS,
		E_ALFALFA,
	};
	
	private Integer mId;
	private String mType;
	private Double mHaulDistance;
	private Double mAcres;
	private Integer mCount;
	private Double mPx, mPy;
	
	// Crop tracking...
	private Map<Crop, Double> mNeed = new EnumMap<Crop,Double>(Crop.class);
	private Map<Crop, Double> mHave = new EnumMap<Crop,Double>(Crop.class);
	private Map<Crop, Double> mExcess = new EnumMap<Crop,Double>(Crop.class);
	
	private static Map<Integer, Farm> mFarms = new HashMap<Integer, Farm>();
	
	//-----------------------------------------------------------------
	Farm(Integer id, String type, Double haulDist, Double acres, Integer count, Double px, Double py) {
		mId = id;
		mType = type;
		mHaulDistance = haulDist;
		mAcres = acres;
		mCount = count;
		mPx = px;
		mPy = py;
		
		cropMapDefault(mHave, 0.0);
		cropMapDefault(mExcess, 0.0);
		cropMapDefault(mNeed, 0.0);
		
		if (mType.equalsIgnoreCase("beef")) {
			mNeed.put(Crop.E_CORN, 9.0 * count);
			mNeed.put(Crop.E_SOY, 0.2 * count);
			mNeed.put(Crop.E_GRASS, 0.1 * count);
			mNeed.put(Crop.E_ALFALFA, 2.7 * count);
		}
		else {
			mNeed.put(Crop.E_CORN, 9.55 * count);
			mNeed.put(Crop.E_SOY, 0.4 * count);
			mNeed.put(Crop.E_GRASS, 0.1 * count);
			mNeed.put(Crop.E_ALFALFA, 2.75 * count);
		}
		
		mFarms.put(id, this);
	}
	
	// returns farm location defined in 3857 as an x, y index into a 30m raster
	// FIXME: TODO: y in 3857 projection may not be perfectly linear?
	//-----------------------------------------------------------------
	public Point getLocation() {
		int idx = (int)Math.round((-10062652.65061 - mPx) / -30.0);
		int idy = (int)Math.round((5415100.4034 - mPy) / 30.0);
		
		//-10062652.65061,-9878152.65061,5415100.4034,5278380.4034
//		var b = [-10062652.65061,5278380.4034,-9878152.65061,5415100.4034];
//		console.log('y1: ' + (360 * Math.atan(Math.exp(b[1] / RADIUS)) / Math.PI - 90));
/*		Double RADIUS = 6378137.0;
		Double y = (360.0 * Math.atan(Math.exp(mPy / RADIUS)) / Math.PI - 90);
		Double y1 = (360.0 * Math.atan(Math.exp(5415100.4034 / RADIUS)) / Math.PI - 90);
		Double y2 = (360.0 * Math.atan(Math.exp(5278380.4034 / RADIUS)) / Math.PI - 90);
		
		int idy = (int)Math.round((y1 - y) / (y1 - y2) * 4557.0);*/
		return new Point(idx,idy);
	}
	
	//-----------------------------------------------------------------
	public Double getRemainingNeed(Crop c) {
		return mNeed.get(c) - mHave.get(c);
	}
	
	//-----------------------------------------------------------------
	public static Double getRemainingNeed(Crop c, Integer id) {
		Farm f = mFarms.get(id);
		if (f == null) return 0.0;
		return f.mNeed.get(c) - f.mHave.get(c);
	}
	
	//-----------------------------------------------------------------
	public static void receiveCrop(Crop c, Integer id, Double amount) {
		Farm f = mFarms.get(id);
		if (f == null) return;
		f.mHave.put(c, f.mHave.get(c) + amount);
	}
	
	//-----------------------------------------------------------------
	public static void receiveExcessCrop(Crop c, Integer id, Double amount) {
		Farm f = mFarms.get(id);
		if (f == null) return;
		f.mExcess.put(c, f.mExcess.get(c) + amount);
	}
	//-----------------------------------------------------------------
	public static void init() {
		new Farm(1, "beef", 426.72, 0.0, 40, -9952073.7964, 5358970.7118);
		new Farm(2, "dairy", 457.2, 0.0, 10, -9952407.7549, 5359123.6923);
		new Farm(3, "beef", 457.2, 0.0, 10, -9949958.7261, 5362336.8649);
		new Farm(5, "beef", 426.72, 0.0, 40, -9951071.921, 5356982.194);
		new Farm(7, "beef", 792.48, 0.0, 230, -9947732.3363, 5357441.0449);
		new Farm(11, "dairy", 487.68, 0.0, 100, -9947509.6973, 5361724.7464);
		new Farm(12, "beef", 457.2, 0.0, 70, -9947509.6973, 5359429.6609);
		new Farm(13, "beef", 487.68, 0.0, 100, -9947064.4193, 5359276.6753);
		new Farm(14, "beef", 426.72, 0.0, 40, -9947954.9753, 5359429.6609);
		new Farm(15, "beef", 792.48, 0.0, 230, -9948622.8922, 5358970.7118);
		new Farm(17, "dairy", 457.2, 0.0, 84, -9943836.1541, 5359735.6395);
		new Farm(18, "dairy", 2834.64, 0.0, 852, -9944504.071, 5357899.9186);
		new Farm(19, "dairy", 4450.08, 0.0, 999, -9943390.8761, 5356370.4278);
		new Farm(20, "beef", 944.88, 0.0, 480, -9944615.3905, 5355605.7768);
		new Farm(21, "dairy", 457.2, 0.0, 70, -9943279.5566, 5355299.934);
		new Farm(22, "beef", 487.68, 0.0, 150, -9941498.4448, 5356064.5599);
		new Farm(23, "beef", 457.2, 0.0, 80, -9941275.8058, 5356829.2487);
		new Farm(24, "dairy", 487.68, 0.0, 120, -9942389.0007, 5356829.2487);
		new Farm(25, "beef", 274.32, 0.0, 30, -9942277.6812, 5357135.1418);
		new Farm(26, "beef", 457.2, 0.0, 20, -9941275.8058, 5362336.8649);
		new Farm(27, "dairy", 402.336, 170.4, 80, -9936489.0677, 5332847.9466);
		new Farm(28, "beef", 402.336, 118.9, 50, -9932704.205, 5324918.6759);
		new Farm(29, "beef", 402.336, 278.4, 100, -9931368.3711, 5321718.3786);
		new Farm(30, "beef", 402.336, 376.1, 70, -9929587.2593, 5317605.321);
		new Farm(31, "dairy", 402.336, 376.1, 150, -9930255.1762, 5316691.554);
		new Farm(32, "dairy", 402.336, 44.3, 150, -9929364.6203, 5316539.2682);
		new Farm(33, "beef", 402.336, 1068.9, 50, -9926804.272, 5316539.2682);
		new Farm(34, "beef", 402.336, 1068.9, 50, -9926247.6745, 5316082.4256);
		new Farm(35, "dairy", 804.672, 195.3, 100, -9932036.2881, 5313950.7889);
		new Farm(36, "hogs", 804.672, 307.7, 150, -9946507.8219, 5314712.0319);
		new Farm(37, "dairy", 402.336, 209.2, 150, -9927917.4669, 5308928.1383);
		new Farm(38, "beef", 402.336, 394.1, 50, -9940830.5278, 5309080.3002);
		new Farm(39, "beef", 228.6, 189.9, 30, -9952407.7549, 5307254.5206);
		new Farm(40, "dairy", 402.336, 861.7, 200, -9945171.988, 5306950.2586);
		new Farm(41, "beef", 402.336, 152.3, 100, -9930255.1762, 5307254.5206);
		new Farm(42, "dairy", 304.8, 284.9, 150, -9928251.4254, 5306493.8842);
		new Farm(43, "dairy", 402.336, 1444.1, 200, -9925134.4796, 5306493.8842);
		new Farm(44, "dairy", 402.336, 284.9, 100, -9928808.0228, 5306189.6469);
		new Farm(45, "dairy", 804.672, 480.3, 200, -9940830.5278, 5304972.7968);
		new Farm(46, "dairy", 804.672, 1199.7, 200, -9926915.5915, 5297067.1237);
		new Farm(47, "dairy", 804.672, 935.9, 300, -9928362.7449, 5294180.1759);
		new Farm(48, "dairy", 804.672, 513.0, 300, -9926692.9525, 5294028.2559);
		new Farm(49, "dairy", 804.672, 507.3, 400, -9927917.4669, 5292812.9846);
		new Farm(50, "dairy", 457.2, 2159.0, 50, -9970441.5124, 5339715.2596);
		new Farm(51, "dairy", 8046.72, 2159.0, 600, -9969773.5954, 5338799.3256);
		new Farm(52, "dairy", 1524.0, 2159.0, 100, -9969105.6785, 5339867.924);
		new Farm(53, "dairy", 1524.0, 431.5, 400, -9970664.1514, 5337883.4815);
		new Farm(54, "beef", 457.2, 99.3, 50, -9968660.4005, 5337883.4815);
		new Farm(55, "beef", 457.2, 431.5, 50, -9968103.8031, 5337883.4815);
		new Farm(56, "beef", 457.2, 116.2, 50, -9972779.2217, 5342310.895);
		new Farm(57, "dairy", 457.2, 308.0, 100, -9972667.9022, 5343227.1743);
		new Farm(58, "dairy", 457.2, 308.0, 20, -9972667.9022, 5343532.6207);
		new Farm(59, "dairy", 457.2, 529.0, 250, -9971888.6658, 5344296.2807);
		new Farm(60, "beef", 1524.0, 179.2, 200, -9969439.637, 5335441.6704);
		new Farm(61, "dairy", 457.2, 80.1, 70, -9968883.0395, 5335289.0785);
		new Farm(62, "beef", 914.4, 223.1, 100, -9968215.1226, 5335289.0785);
		new Farm(63, "dairy", 1524.0, 300.8, 500, -9968437.7616, 5333458.1697);
		new Farm(64, "dairy", 1524.0, 197.4, 250, -9969105.6785, 5333153.0531);
		new Farm(65, "beef", 1524.0, 519.3, 150, -9970218.8734, 5331170.0389);
		new Farm(66, "dairy", 8046.72, 1045.2, 2045, -9967881.1641, 5331780.1522);
		new Farm(67, "dairy", 8046.72, 4728.0, 3905, -9968771.72, 5331170.0389);
		new Farm(68, "beef", 1524.0, 215.3, 150, -9971443.3878, 5331170.0389);
		new Farm(69, "dairy", 8046.72, 2507.6, 800, -9972333.9437, 5333153.0531);
		new Farm(70, "dairy", 914.4, 304.2, 200, -9967101.9277, 5330864.9972);
		new Farm(71, "beef", 457.2, 304.2, 50, -9966767.9692, 5330712.4801);
		new Farm(72, "dairy", 914.4, 108.7, 100, -9966545.3302, 5330712.4801);
		new Farm(73, "dairy", 457.2, 211.1, 300, -9964986.8573, 5331170.0389);
		new Farm(74, "dairy", 1524.0, 157.8, 550, -9961758.5921, 5331627.6201);
		new Farm(75, "dairy", 1524.0, 215.2, 850, -9961981.2311, 5332695.397);
		new Farm(76, "dairy", 457.2, 711.7, 200, -9963651.0235, 5333305.6101);
		new Farm(77, "dairy", 762.0, 139.0, 200, -9967101.9277, 5333458.1697);
		new Farm(78, "dairy", 457.2, 190.7, 100, -9965988.7328, 5334678.7356);
		new Farm(79, "dairy", 914.4, 686.2, 500, -9966434.0107, 5335594.2649);
		new Farm(80, "dairy", 457.2, 158.0, 150, -9964652.8989, 5335136.489);
		new Farm(81, "dairy", 457.2, 392.8, 50, -9962203.8701, 5334678.7356);
		new Farm(82, "dairy", 914.4, 386.3, 325, -9961535.9531, 5334678.7356);
		new Farm(83, "dairy", 457.2, 140.3, 150, -9962092.5506, 5336052.0633);
		new Farm(84, "beef", 1524.0, 3620.9, 2094, -9959532.2023, 5334526.1561);
		new Farm(85, "beef", 457.2, 152.1, 75, -9959198.2438, 5336509.8841);
		new Farm(86, "beef", 457.2, 271.0, 100, -9958641.6464, 5334526.1561);
		new Farm(87, "beef", 457.2, 589.3, 50, -9957751.0904, 5334678.7356);
		new Farm(88, "dairy", 457.2, 711.7, 25, -9960200.1192, 5332847.9466);
		new Farm(89, "beef", 457.2, 3620.9, 100, -9957973.7294, 5336052.0633);
		new Farm(90, "beef", 457.2, 1045.5, 50, -9959977.4803, 5337425.5932);
		new Farm(91, "dairy", 457.2, 233.4, 325, -9961424.6336, 5336815.1105);
		new Farm(92, "dairy", 914.4, 1045.5, 200, -9961981.2311, 5336815.1105);
		new Farm(93, "dairy", 914.4, 1045.5, 450, -9965320.8158, 5336815.1105);
		new Farm(94, "dairy", 914.4, 161.4, 500, -9964652.8989, 5338799.3256);
		new Farm(95, "dairy", 457.2, 589.3, 75, -9967101.9277, 5336662.496);
		new Farm(96, "dairy", 457.2, 589.3, 75, -9966990.6082, 5337272.9688);
		new Farm(97, "beef", 457.2, 222.3, 100, -9966545.3302, 5337883.4815);
		new Farm(99, "dairy", 609.6, 589.3, 200, -9967658.5251, 5338646.6786);
		new Farm(100, "dairy", 457.2, 237.5, 200, -9965988.7328, 5338951.975);
		new Farm(101, "dairy", 457.2, 237.5, 50, -9966211.3717, 5339409.9383);
		new Farm(102, "dairy", 609.6, 216.4, 200, -9968326.4421, 5339257.2813);
		new Farm(103, "dairy", 914.4, 320.3, 300, -9964652.8989, 5341089.3295);
		new Farm(104, "dairy", 914.4, 322.8, 400, -9963984.9819, 5341089.3295);
		new Farm(105, "dairy", 914.4, 392.8, 400, -9961201.9947, 5338951.975);
		new Farm(106, "dairy", 457.2, 320.8, 300, -9961201.9947, 5339409.9383);
		new Farm(107, "beef", 457.2, 411.2, 150, -9960756.7167, 5339104.6269);
		new Farm(108, "dairy", 457.2, 318.9, 300, -9972667.9022, 5346434.8616);
		new Farm(109, "dairy", 457.2, 355.8, 300, -9970107.5539, 5345060.0032);
		new Farm(110, "dairy", 914.4, 426.1, 500, -9965320.8158, 5345365.5098);
		new Farm(111, "dairy", 1524.0, 848.2, 1500, -9961313.3141, 5345365.5098);
		new Farm(112, "dairy", 1524.0, 156.3, 350, -9960756.7167, 5345365.5098);
		new Farm(113, "dairy", 1524.0, 620.4, 2188, -9960756.7167, 5344601.7622);
		new Farm(114, "dairy", 457.2, 327.4, 200, -9957528.4515, 5344601.7622);
		new Farm(115, "beef", 457.2, 411.2, 200, -9956637.8955, 5345518.2668);
		new Farm(116, "dairy", 609.6, 228.6, 300, -9955858.6591, 5343532.6207);
		new Farm(117, "dairy", 457.2, 174.6, 100, -9954745.4642, 5343685.3477);
		new Farm(118, "dairy", 609.6, 116.4, 500, -9953632.2693, 5343532.6207);
		new Farm(119, "dairy", 457.2, 733.2, 150, -9954522.8252, 5346282.0896);
		new Farm(120, "dairy", 457.2, 79.2, 200, -9956415.2566, 5346587.6362);
		new Farm(121, "dairy", 457.2, 733.2, 700, -9957083.1735, 5346129.32);
		new Farm(122, "dairy", 228.6, 1370.8, 35, -9957862.4099, 5346893.1929);
		new Farm(123, "dairy", 457.2, 148.3, 150, -9958641.6464, 5346434.8616);
		new Farm(124, "dairy", 457.2, 306.6, 300, -9959754.8413, 5347198.7596);
		new Farm(125, "dairy", 914.4, 996.2, 450, -9962092.5506, 5346587.6362);
		new Farm(126, "dairy", 609.6, 996.2, 300, -9962537.8285, 5345976.5529);
		new Farm(127, "dairy", 457.2, 996.2, 100, -9962983.1065, 5346129.32);
		new Farm(128, "dairy", 228.6, 24.5, 40, -9963873.6624, 5345976.5529);
		new Farm(129, "dairy", 457.2, 109.1, 100, -9964764.2184, 5347198.7596);
		new Farm(130, "beef", 457.2, 273.8, 50, -9965098.1768, 5346282.0896);
		new Farm(131, "beef", 457.2, 273.8, 65, -9965320.8158, 5347809.9231);
		new Farm(132, "dairy", 457.2, 414.0, 100, -9965543.4548, 5347504.3363);
		new Farm(133, "dairy", 457.2, 269.0, 50, -9967658.5251, 5347045.975);
		new Farm(134, "dairy", 304.8, 426.1, 100, -9965988.7328, 5346129.32);
		new Farm(135, "dairy", 914.4, 269.0, 350, -9968437.7616, 5346282.0896);
		new Farm(136, "dairy", 457.2, 414.0, 200, -9967435.8861, 5347962.7202);
		new Farm(137, "dairy", 914.4, 734.1, 600, -9966211.3717, 5348879.5557);
		new Farm(138, "dairy", 304.8, 238.0, 100, -9964764.2184, 5348573.9338);
		new Farm(139, "dairy", 457.2, 438.7, 70, -9963873.6624, 5349032.3704);
		new Farm(140, "beef", 228.6, 110.9, 100, -9962760.4675, 5348115.5198);
		new Farm(141, "dairy", 228.6, 996.2, 50, -9961090.6752, 5348879.5557);
		new Farm(142, "dairy", 1524.0, 1370.8, 975, -9960645.3972, 5348726.7435);
		new Farm(143, "dairy", 304.8, 148.3, 100, -9961201.9947, 5348421.1267);
		new Farm(144, "dairy", 457.2, 116.0, 100, -9959086.9243, 5347962.7202);
		new Farm(145, "dairy", 304.8, 76.9, 100, -9959532.2023, 5350102.1435);
		new Farm(146, "dairy", 457.2, 427.1, 100, -9959198.2438, 5350254.9782);
		new Farm(147, "dairy", 228.6, 1370.8, 30, -9960868.0362, 5350713.4976);
		new Farm(148, "dairy", 457.2, 157.5, 150, -9959754.8413, 5352242.0585);
		new Farm(149, "dairy", 457.2, 188.8, 200, -9960088.7997, 5352700.6758);
		new Farm(150, "dairy", 228.6, 156.8, 50, -9958864.2853, 5353465.0881);
		new Farm(151, "dairy", 457.2, 427.1, 50, -9961424.6336, 5350254.9782);
		new Farm(152, "dairy", 304.8, 141.6, 100, -9952964.3523, 5338188.7529);
		new Farm(153, "beef", 457.2, 405.7, 400, -9958752.9659, 5336815.1105);
		new Farm(154, "beef", 457.2, 199.7, 200, -9955969.9786, 5341852.7892);
		new Farm(155, "dairy", 457.2, 353.5, 300, -9962092.5506, 5343379.8962);
		new Farm(156, "beef", 457.2, 683.2, 100, -9963428.3845, 5342005.4886);
		new Farm(157, "dairy", 457.2, 322.8, 50, -9964875.5379, 5342158.1906);
		new Farm(158, "dairy", 457.2, 195.3, 50, -9957528.4515, 5348421.1267);
		new Farm(159, "dairy", 457.2, 427.1, 250, -9958307.6879, 5350102.1435);
		new Farm(161, "dairy", 1524.0, 2507.6, 2974, -9954188.8667, 5352700.6758);
		new Farm(162, "dairy", 457.2, 390.3, 100, -9954522.8252, 5353465.0881);
		new Farm(163, "dairy", 457.2, 405.3, 100, -9953075.6718, 5352853.5532);
		new Farm(164, "beef", 457.2, 137.5, 100, -9950292.6846, 5353312.2006);
		new Farm(165, "beef", 457.2, 751.5, 25, -9949958.7261, 5353617.9781);
		new Farm(166, "beef", 609.6, 247.3, 200, -9952185.1159, 5352394.9285);
		new Farm(167, "beef", 457.2, 896.1, 150, -9949736.0871, 5354229.5632);
		new Farm(168, "beef", 457.2, 751.5, 100, -9948956.8507, 5353923.7657);
		new Farm(169, "beef", 457.2, 2507.6, 50, -9952407.7549, 5355299.934);
		new Farm(170, "dairy", 457.2, 158.4, 200, -9957639.7709, 5355147.0163);
		new Farm(171, "dairy", 914.4, 655.4, 500, -9957639.7709, 5355911.6297);
		new Farm(172, "dairy", 457.2, 323.4, 175, -9957639.7709, 5350713.4976);
		new Farm(173, "dairy", 457.2, 2507.6, 100, -9954522.8252, 5351630.604);
		new Farm(174, "dairy", 457.2, 150.7, 70, -9952296.4354, 5351630.604);
		new Farm(175, "dairy", 762.0, 417.5, 400, -9943724.8346, 5353617.9781);
		new Farm(176, "dairy", 457.2, 490.9, 200, -9941275.8058, 5354229.5632);
		new Farm(177, "dairy", 304.8, 260.7, 50, -9940385.2499, 5352853.5532);
		new Farm(178, "dairy", 457.2, 303.6, 400, -9939272.055, 5354076.6632);
		new Farm(179, "dairy", 457.2, 303.6, 200, -9938938.0965, 5353923.7657);
		new Farm(182, "dairy", 457.2, 505.3, 200, -9951183.2405, 5349796.4815);
		new Farm(183, "dairy", 609.6, 280.3, 350, -9950737.9625, 5341700.0922);
		new Farm(184, "beef", 457.2, 620.4, 100, -9949513.4481, 5343379.8962);
		new Farm(185, "dairy", 1219.2, 272.5, 400, -9952630.3939, 5345365.5098);
		new Farm(186, "dairy", 457.2, 573.7, 25, -9953409.6303, 5345518.2668);
		new Farm(187, "dairy", 457.2, 733.2, 50, -9954522.8252, 5347504.3363);
		new Farm(188, "dairy", 762.0, 573.7, 150, -9952073.7964, 5346282.0896);
		new Farm(189, "beef", 457.2, 848.2, 150, -9950737.9625, 5347809.9231);
		new Farm(190, "beef", 457.2, 405.4, 200, -9943279.5566, 5346740.4133);
		new Farm(191, "dairy", 457.2, 542.4, 300, -9943279.5566, 5347962.7202);
		new Farm(192, "dairy", 457.2, 495.1, 420, -9942945.5982, 5348421.1267);
		new Farm(193, "beef", 304.8, 158.1, 75, -9942055.0422, 5348421.1267);
		new Farm(194, "dairy", 457.2, 194.2, 150, -9942722.9592, 5350713.4976);
		new Farm(195, "dairy", 1524.0, 778.4, 950, -9956081.2981, 5349490.8295);
		new Farm(196, "dairy", 457.2, 1021.6, 150, -9942277.6812, 5344754.5067);
		new Farm(197, "dairy", 228.6, 240.0, 50, -9939049.416, 5338188.7529);
		new Farm(198, "dairy", 457.2, 159.2, 250, -9944058.7931, 5341547.3978);
		new Farm(199, "beef", 457.2, 405.4, 60, -9947954.9753, 5341852.7892);
		new Farm(200, "dairy", 457.2, 135.0, 100, -9947509.6973, 5341700.0922);
		new Farm(201, "dairy", 457.2, 278.6, 100, -9935264.5533, 5346129.32);
		new Farm(202, "dairy", 914.4, 829.5, 400, -9934596.6364, 5346282.0896);
		new Farm(203, "dairy", 457.2, 278.6, 50, -9935821.1508, 5346282.0896);
		new Farm(204, "dairy", 457.2, 614.5, 200, -9937045.6652, 5347351.5467);
		new Farm(205, "dairy", 914.4, 1163.0, 300, -9938381.499, 5345060.0032);
		new Farm(206, "dairy", 914.4, 1163.0, 200, -9938826.777, 5345823.7884);
		new Farm(207, "beef", 304.8, 433.9, 150, -9937936.2211, 5343990.8092);
		new Farm(208, "dairy", 609.6, 495.1, 300, -9940607.8889, 5346434.8616);
		new Farm(209, "beef", 762.0, 537.34, 150, -9940941.8473, 5349185.1876);
		new Farm(210, "dairy", 457.2, 207.4, 200, -9939049.416, 5350866.3424);
		new Farm(211, "dairy", 457.2, 1163.0, 125, -9938270.1796, 5348421.1267);
		new Farm(212, "dairy", 762.0, 463.7, 250, -9936823.0262, 5349949.3112);
		new Farm(213, "dairy", 304.8, 463.7, 50, -9935709.8313, 5350102.1435);
		new Farm(214, "dairy", 457.2, 37.7, 100, -9937824.9016, 5352853.5532);
		new Farm(215, "beef", 457.2, 2159.0, 250, -9968883.0395, 5342921.7378);
		new Farm(216, "beef", 304.8, 4.9, 50, -9968326.4421, 5341242.0164);
		new Farm(217, "dairy", 457.2, 272.8, 100, -9968660.4005, 5341394.7058);
		new Farm(218, "beef", 457.2, 143.4, 70, -9969773.5954, 5341700.0922);
		new Farm(219, "beef", 304.8, 683.2, 50, -9966767.9692, 5341852.7892);
		new Farm(220, "dairy", 457.2, 131.5, 100, -9966545.3302, 5342005.4886);
		new Farm(221, "beef", 1524.0, 0.0, 1281, -9947287.0583, 5346587.6362);
		new Farm(222, "dairy", 1524.0, 0.0, 2145, -9955190.7422, 5361724.7464);
		new Farm(223, "dairy", 457.2, 0.0, 100, -9950960.6015, 5295699.5113);
		new Farm(224, "dairy", 457.2, 0.0, 280, -9945839.9049, 5303451.9566);
		new Farm(225, "dairy", 304.8, 0.0, 50, -9952853.0328, 5307710.9321);
		new Farm(226, "dairy", 457.2, 0.0, 100, -9944170.1126, 5307406.6553);
		new Farm(227, "dairy", 609.6, 0.0, 200, -9943502.1956, 5308015.2188);
		new Farm(228, "dairy", 762.0, 0.0, 300, -9936823.0262, 5298434.9358);
		new Farm(229, "dairy", 762.0, 0.0, 300, -9936489.0677, 5297675.0156);
		new Farm(230, "dairy", 457.2, 0.0, 100, -9946619.1414, 5292509.1914);
		new Farm(231, "dairy", 457.2, 0.0, 100, -9946062.5439, 5290990.373);
		new Farm(232, "dairy", 457.2, 0.0, 100, -9933038.1635, 5319280.7926);
		new Farm(233, "dairy", 762.0, 0.0, 200, -9971666.0268, 5335136.489);
		new Farm(234, "dairy", 457.2, 0.0, 100, -9969662.276, 5333153.0531);
		new Farm(235, "dairy", 457.2, 0.0, 100, -9965098.1768, 5339257.2813);
		new Farm(236, "dairy", 762.0, 0.0, 300, -9962760.4675, 5339104.6269);
		new Farm(237, "dairy", 457.2, 0.0, 150, -9965432.1353, 5343379.8962);
		new Farm(238, "dairy", 457.2, 0.0, 600, -9967658.5251, 5342921.7378);
		new Farm(239, "dairy", 609.6, 0.0, 200, -9972445.2632, 5347351.5467);
		new Farm(240, "dairy", 457.2, 0.0, 100, -9972445.2632, 5345060.0032);
		new Farm(241, "dairy", 457.2, 0.0, 100, -9963205.7455, 5347351.5467);
		new Farm(242, "dairy", 457.2, 0.0, 200, -9949290.8091, 5344754.5067);
		new Farm(243, "dairy", 457.2, 0.0, 50, -9949736.0871, 5348115.5198);
		new Farm(244, "dairy", 228.6, 0.0, 30, -9945951.2244, 5353159.3157);
		new Farm(245, "dairy", 457.2, 0.0, 100, -9939939.9719, 5343074.4548);
		new Farm(246, "dairy", 457.2, 0.0, 200, -9939606.0134, 5358052.8815);
		new Farm(247, "beef", 457.2, 0.0, 50, -9925134.4796, 5293572.5107);
		new Farm(248, "beef", 457.2, 0.0, 25, -9943613.5151, 5290990.373);
		new Farm(249, "dairy", 457.2, 0.0, 100, -9940162.6109, 5290838.5047);
		new Farm(250, "beef", 457.2, 0.0, 50, -9934373.9974, 5289775.4955);
		new Farm(251, "beef", 152.4, 0.0, 25, -9930143.8567, 5288712.6067);
		new Farm(252, "beef", 152.4, 0.0, 25, -9930255.1762, 5290231.0561);
		new Farm(253, "beef", 304.8, 0.0, 25, -9929921.2177, 5292053.5201);
		new Farm(254, "beef", 457.2, 0.0, 25, -9927806.1474, 5290686.6389);
		new Farm(255, "beef", 304.8, 0.0, 25, -9925691.0771, 5291749.7515);
		new Farm(256, "beef", 152.4, 0.0, 15, -9925579.7576, 5292964.8849);
		new Farm(257, "beef", 457.2, 0.0, 50, -9928474.0644, 5293876.3384);
		new Farm(258, "beef", 152.4, 0.0, 15, -9946285.1829, 5303908.1827);
		new Farm(259, "beef", 457.2, 0.0, 25, -9952519.0744, 5304212.3458);
		new Farm(260, "beef", 152.4, 0.0, 15, -9944838.0295, 5291749.7515);
		new Farm(261, "beef", 152.4, 0.0, 15, -9933928.7194, 5309993.3237);
		new Farm(262, "beef", 304.8, 0.0, 50, -9928585.3839, 5311058.6304);
		new Farm(263, "dairy", 457.2, 0.0, 50, -9924577.8822, 5316386.9849);
		new Farm(264, "beef", 457.2, 0.0, 50, -9931924.9686, 5318214.5486);
		new Farm(265, "beef", 457.2, 0.0, 25, -9935598.5118, 5325071.0984);
		new Farm(266, "beef", 457.2, 0.0, 150, -9928808.0228, 5320804.2093);
		new Farm(267, "dairy", 152.4, 0.0, 25, -9960088.7997, 5331475.0906);
		new Farm(268, "dairy", 457.2, 0.0, 200, -9959866.1608, 5335289.0785);
		new Farm(269, "dairy", 609.6, 0.0, 300, -9963094.426, 5336357.2747);
		new Farm(270, "dairy", 304.8, 0.0, 50, -9960645.3972, 5336967.7274);
		new Farm(271, "dairy", 304.8, 0.0, 50, -9962760.4675, 5337883.4815);
		new Farm(272, "beef", 152.4, 0.0, 20, -9961424.6336, 5340936.645);
		new Farm(273, "dairy", 457.2, 0.0, 100, -9971109.4293, 5337272.9688);
		new Farm(274, "dairy", 304.8, 0.0, 25, -9973001.8607, 5339867.924);
		new Farm(275, "dairy", 152.4, 0.0, 20, -9971554.7073, 5340325.9323);
		new Farm(276, "dairy", 457.2, 0.0, 50, -9969328.3175, 5347657.1284);
		new Farm(277, "dairy", 457.2, 0.0, 50, -9970218.8734, 5343532.6207);
		new Farm(278, "dairy", 457.2, 0.0, 100, -9963539.704, 5349643.6542);
		new Farm(279, "beef", 457.2, 0.0, 100, -9960645.3972, 5351783.4639);
		new Farm(280, "beef", 457.2, 0.0, 50, -9960200.1192, 5353006.4332);
		new Farm(281, "beef", 304.8, 0.0, 25, -9958530.3269, 5353617.9781);
		new Farm(282, "dairy", 457.2, 0.0, 100, -9956637.8955, 5351172.0395);
		new Farm(283, "beef", 304.8, 0.0, 25, -9952407.7549, 5348726.7435);
		new Farm(284, "beef", 304.8, 0.0, 25, -9952630.3939, 5346434.8616);
		new Farm(285, "dairy", 304.8, 0.0, 100, -9951628.5184, 5343838.0772);
		new Farm(286, "beef", 304.8, 0.0, 25, -9948066.2947, 5342463.602);
		new Farm(287, "beef", 152.4, 0.0, 20, -9947509.6973, 5343532.6207);
		new Farm(288, "beef", 457.2, 0.0, 50, -9948288.9337, 5340173.2604);
		new Farm(289, "dairy", 457.2, 0.0, 50, -9949624.7676, 5347504.3363);
		new Farm(290, "beef", 304.8, 0.0, 50, -9949736.0871, 5351630.604);
		new Farm(291, "beef", 152.4, 0.0, 15, -9950960.6015, 5354076.6632);
		new Farm(292, "beef", 457.2, 0.0, 100, -9950404.004, 5354841.1886);
		new Farm(293, "beef", 152.4, 0.0, 25, -9952073.7964, 5354841.1886);
		new Farm(294, "dairy", 609.6, 0.0, 300, -9941943.7227, 5354229.5632);
		new Farm(295, "beef", 304.8, 0.0, 25, -9938270.1796, 5352089.1912);
		new Farm(296, "beef", 152.4, 0.0, 15, -9938158.8601, 5352853.5532);
		new Farm(297, "beef", 457.2, 0.0, 100, -9936377.7482, 5352700.6758);
		new Farm(298, "beef", 457.2, 0.0, 50, -9935487.1923, 5352853.5532);
		new Farm(299, "beef", 152.4, 0.0, 15, -9937045.6652, 5350407.8155);
		new Farm(300, "dairy", 457.2, 0.0, 200, -9939828.6524, 5350102.1435);
		new Farm(301, "beef", 152.4, 0.0, 15, -9940830.5278, 5349643.6542);
		new Farm(302, "beef", 457.2, 0.0, 50, -9939939.9719, 5348421.1267);
		new Farm(303, "dairy", 457.2, 0.0, 400, -9940941.8473, 5347809.9231);
		new Farm(304, "dairy", 152.4, 0.0, 25, -9940607.8889, 5345976.5529);
		new Farm(305, "beef", 457.2, 0.0, 100, -9938715.4575, 5344143.5437);
		new Farm(317, "beef", 365.76, 0.0, 100, -9926247.6745, 5302691.6291);
		new Farm(318, "dairy", 457.2, 0.0, 50, -9970330.1929, 5338799.3256);
		new Farm(319, "dairy", 304.8, 0.0, 200, -9953520.9498, 5342921.7378);
		new Farm(320, "dairy", 152.4, 0.0, 50, -9954634.1447, 5361418.7022);
		
	/*	Map<Integer, Farm> cp = new HashMap<Integer, Farm>(mFarms);

		for(Entry<Integer, Farm> d : cp.entrySet()) {
			Farm f = d.getValue();
			new Farm(f.mId + 400, f.mType, f.mHaulDistance, f.mAcres,
					f.mCount + mRnd.nextInt(80),
					f.mPx + mRnd.nextDouble() * 3520 + 50000,
					f.mPy + mRnd.nextDouble() * 1000 - 500);
			
		}
*/		
	}
	
	//-----------------------------------------------------------------
	public static JsonNode toGeoJson() {
	
		ArrayNode array = JsonNodeFactory.instance.arrayNode();

		for (Entry<Integer,Farm> eif : mFarms.entrySet()) {
			Farm f = eif.getValue();
			array.add(Json.pack("type", "Feature", 
				"properties", Json.pack("count", f.mCount, "id", f.mId),
				"geometry", Json.pack("type", "Point", "coordinates", Json.array(f.mPx, f.mPy)))
				);
		}
		
		JsonNode ret = Json.pack("type", "FeatureCollection", "name", "temp",
			"crs", Json.pack("type", "name", "properties", 
				Json.pack("name", "urn:ogc:def:crs:EPSG::3857")),
			"features", array);
				
		return ret;
	}
	
	// selectRadius is in m
	//-----------------------------------------------------------------
	public static Selection select(JsonNode queryNode, Selection selection) {
	
		String lessTest = Json.safeGetOptionalString(queryNode, "lessThanTest", "<");
		String gtrTest = Json.safeGetOptionalString(queryNode, "greaterThanTest", ">");
		
		Integer gtrValNode = Json.safeGetOptionalInteger(queryNode, "greaterThanValue", null);
		Integer lessValNode = Json.safeGetOptionalInteger(queryNode, "lessThanValue", null);

		String type = Json.safeGetOptionalString(queryNode, "type", "any");
		
		Boolean selectAsCircle = Json.safeGetOptionalBoolean(queryNode, "shapeCircle", false);
		Boolean invertSelection = Json.safeGetOptionalBoolean(queryNode, "invert", false);
		Boolean headCountRelative = Json.safeGetOptionalBoolean(queryNode, "headCountRelative", false);
		Float radius = Json.safeGetOptionalFloat(queryNode, "radius", 30.0f);		
		
		float minVal = 0, maxVal = 0;
		boolean isGreaterThan = false, isGreaterThanEqual = false;
		boolean isLessThan = false, isLessThanEqual = false;
		
		if (gtrValNode != null) {
			isGreaterThan = (gtrTest.compareTo(">") == 0);
			isGreaterThanEqual = !isGreaterThan;
			minVal = gtrValNode;
		}
		if (lessValNode != null) {
			isLessThan = (lessTest.compareTo("<") == 0);
			isLessThanEqual = !isLessThan;
			maxVal = lessValNode;
		}		
		int width = 6150, height = 4557;
		Selection farmSel = new Selection(width, height, (byte)0);

		// Overridden per farm if cowRelative is True
		Double selectRadius = radius / 30.0;
		
		for(Entry<Integer, Farm> ef : mFarms.entrySet()) {
			
			Farm f = ef.getValue();
			
			if (headCountRelative) {
				// calc area needed for numCows then compute the width of the selection square or radius of selection circle
				Double acreToM_sqr = 4046.86;
				if (selectAsCircle) {
					// FIXME: treating radius as a measure of acres. Make this more explicit in client interface
					//	additionally, allow units in something like hectares?
					selectRadius = Math.sqrt((radius * f.mCount * acreToM_sqr)
							/ Math.PI);
				}
				else {
					// FIXME: treating radius as a measure of acres. Make this more explicit in client interface
					//	additionally, allow units in something like hectares?
					selectRadius = Math.sqrt(radius * f.mCount * acreToM_sqr);
					// selectRadius is expected to be half of the width of the box
					selectRadius /= 2.0;
				}
				// convert to DST scale units
				selectRadius /= 30.0;
			}
			
			if (type != null && !type.equalsIgnoreCase("any")) {
				if (!f.mType.equalsIgnoreCase(type)) continue;
			}
			
			// TODO: FIXME: simplifying this might be nice...
			if (isGreaterThan) {
				if (isLessThan) {
					// asking for comparison: >  <, transform it to....so we skip farms that DON'T match the request
					if (f.mCount <= minVal || f.mCount >= maxVal) continue;
				}
				else if (isLessThanEqual) {
					// >  <=
					if (f.mCount <= minVal || f.mCount > maxVal) continue;
				}
				else { // >
					if (f.mCount <= minVal) continue;
				}
			}
			else if (isGreaterThanEqual) {
				if (isLessThan) {
					// >= <
					if (f.mCount < minVal || f.mCount >= maxVal) continue;
				}
				else if (isLessThanEqual) {
					// >=  <=
					if (f.mCount < minVal || f.mCount > maxVal) continue;
				}
				else { // >=
					if (f.mCount < minVal) continue;
				}
			}
			else if (isLessThan) {
				// <
				if (f.mCount >= maxVal) continue;
			}
			else if (isLessThanEqual) {
				// <=
				if (f.mCount > maxVal) continue;
			}
			
			Point loc = f.getLocation();
			
			int gsXmin = (int) Math.round(loc.x - selectRadius);
			int gsXmax = (int) Math.round(loc.x + selectRadius);
			int gsYmin = (int) Math.round(loc.y - selectRadius);
			int gsYmax = (int) Math.round(loc.y + selectRadius);

			if (gsXmin < 0) gsXmin = 0;
			if (gsYmin < 0) gsYmin = 0;
			if (gsXmax >= width) gsXmax = width - 1;
			if (gsYmax >= height) gsYmax = height - 1;

			for (int yy = gsYmin; yy <= gsYmax; yy++) {
				for (int xx = gsXmin; xx <= gsXmax; xx++) {

					if (selectAsCircle) {
						Float dist = (float) loc.distanceSq(xx, yy);
						// FIXME: why the additional fudge factor?
						if (dist > (selectRadius + 0.1f)*(selectRadius + 0.1f)) continue;
					}
					farmSel.mRasterData[yy][xx] = 1;
				}
			}

		}
		if (invertSelection) {
			farmSel.invert();
		}
		selection.intersect(farmSel);
		return selection;
	}

	//-----------------------------------------------------------------
	public static void processFarms(Scenario s) {

		PerformanceTimer timer = new PerformanceTimer();
		
		// Calculate yield...
		Model_CropYield cropYield = new Model_CropYield();
		cropYield.initialize(s);
		long[][] packedYield = null;
		try {
			packedYield = cropYield.run();
		}
		catch(Exception e) {
			
		}
		
		Allocate_Yield ay = new Allocate_Yield();
		ay.aggregate(s, packedYield, 510 / 30);

		// Let farms stake claims to nearby feedbuckets
		for(Entry<Integer, Farm> ef : mFarms.entrySet()) {
			
			Farm f = ef.getValue();
			Point loc = f.getLocation();
			int claimRadius = (int)(2000 + (f.mCount * 0.8)) / 30;
			ay.stakeClaim(ef.getKey(), loc, claimRadius);
		}
		
		// Process all claims
		Logger.debug("About to distribute yield");
		ay.distribute();
		ay.claimLeftovers(s)
		;
		// Compute results statistics...
		StringBuilder badSb = new StringBuilder(); badSb.append('\n');
		
		Integer goodCt = 0, badCt = 0;
		Double cornNeed = 0.0, cornGot = 0.0,
			soyNeed = 0.0, soyGot = 0.0,
			grassNeed = 0.0, grassGot = 0.0,
			alfalfaNeed = 0.0, alfalfaGot = 0.0;

		Double excessCorn = 0.0, excessSoy = 0.0, 
				excessGrass = 0.0, excessAlfalfa = 0.0;
		
		Double cp = 100.0, sp = 100.0, ap = 100.0, gp = 100.0;
		int count = 0;
		for(Entry<Integer, Farm> ef : mFarms.entrySet()) {

			Farm f = ef.getValue();
			count++;
			excessCorn 		+= f.mExcess.get(Crop.E_CORN);
			excessSoy 		+= f.mExcess.get(Crop.E_SOY);
			excessGrass 	+= f.mExcess.get(Crop.E_GRASS);
			excessAlfalfa 	+= f.mExcess.get(Crop.E_ALFALFA);
			
			if (f.mNeed.get(Crop.E_CORN) > 0)
				cp = f.mHave.get(Crop.E_CORN) 		/ f.mNeed.get(Crop.E_CORN) * 100.0;
			if (f.mNeed.get(Crop.E_SOY) > 0)
				sp =  f.mHave.get(Crop.E_SOY) 		/ f.mNeed.get(Crop.E_SOY) * 100.0;
			if (f.mNeed.get(Crop.E_GRASS) > 0)
				gp = f.mHave.get(Crop.E_GRASS)		/ f.mNeed.get(Crop.E_GRASS) * 100.0;
			if (f.mNeed.get(Crop.E_ALFALFA) > 0)
				ap = f.mHave.get(Crop.E_ALFALFA)	/ f.mNeed.get(Crop.E_ALFALFA) * 100.0;
			
			cornNeed 	+= f.mNeed.get(Crop.E_CORN);	cornGot 	+= f.mHave.get(Crop.E_CORN); 
			soyNeed 	+= f.mNeed.get(Crop.E_SOY);		soyGot 		+= f.mHave.get(Crop.E_SOY); 
			grassNeed 	+= f.mNeed.get(Crop.E_GRASS);	grassGot 	+= f.mHave.get(Crop.E_GRASS); 
			alfalfaNeed += f.mNeed.get(Crop.E_ALFALFA);	alfalfaGot 	+= f.mHave.get(Crop.E_ALFALFA); 
			
			if (cp >= 98 && gp >= 98 && ap >= 98) {
				goodCt++;
			}
			else {
				String res = String.format("[ cg: %.1f  s: %.1f  g: %.1f  a: %.1f ] Cows: %d\n", cp, sp, gp, ap, f.mCount);
				badSb.append(res);
				badCt++;
			}
		}
		Logger.debug("Good Count:" + goodCt);
		Logger.debug("Bad Count:" + badCt);
		Logger.debug(badSb.toString());
	
		cp = 100.0; sp = 100.0; ap = 100.0; gp = 100.0;
		
		if (cornNeed > 0)
			cp = cornGot 	/ cornNeed * 100.0;
		if (soyNeed > 0)
			sp = soyGot 	/ soyNeed * 100.0;
		if (grassNeed > 0)
			gp = grassGot 	/ grassNeed * 100.0;
		if (alfalfaNeed > 0)
			ap = alfalfaGot / alfalfaNeed * 100.0;
		
		Logger.debug("Overall feed needs met: ");
		Logger.debug(String.format("cg: %.1f  s: %.1f  g: %.1f  a: %.1f", cp, sp, gp, ap));
		
		Logger.debug("Typical per-farm leftover resources for sale: ");
		Logger.debug(String.format("cg: %.2f  s: %.2f  g: %.2f  a: %.2f", 
				excessCorn / count, excessSoy / count, 
				excessGrass / count, excessAlfalfa / count));
		
		Logger.debug(" Execution timing (ms): " + timer.stringMilliseconds(2));
	}

	//-----------------------------------------------------------------
	public static JsonNode getParameterInternal(JsonNode clientRequest) {

		JsonNode ret = null;
		String type = clientRequest.get("type").textValue();
		if (type.equals("layerRange")) {
			Integer min = 9999, max = 0;
			for(Entry<Integer, Farm> ef : mFarms.entrySet()) {
				Farm f = ef.getValue();
				if (f.mCount > max) max = f.mCount;
				if (f.mCount < min) min = f.mCount;
			}
			
			ret = Json.pack("layerMin", min,
					"layerMax", max);
		}
		
		return ret;
	}
	
	//-----------------------------------------------------------------
	private static void cropMapDefault(Map<Crop, Double> map, Double _default) {
		map.put(Crop.E_CORN, _default);
		map.put(Crop.E_SOY, _default);
		map.put(Crop.E_GRASS, _default);
		map.put(Crop.E_ALFALFA, _default);
	}
	
}
