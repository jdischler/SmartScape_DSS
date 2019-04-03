// AOI - Area of Interest

var centralSandsCoords = [[
	[-9974444.68, 5525984.17],
	[-10005576.9, 5501770.16],
	[-10012495.2, 5449882.98],
	[-9988281.26, 5408373.24],
	[-9943312.37, 5397995.81],
	[-9908720.92, 5397995.81],
	[-9887966.05, 5432587.26],
	[-9887966.05, 5477556.14],
	[-9905261.78, 5515606.74],
	[-9943312.37, 5529443.32]
]];
var centralSandsFeature = new ol.Feature(new ol.geom.Polygon(centralSandsCoords));

var urbanCorridorCoords = [[ 
	[-9967999.98, 5370159.85],
	[-9988790.85, 5357929.93],
	[-9996128.81, 5333470.08],
	[-9993682.82, 5309010.23],
	[-9971668.96, 5295557.31],
	[-9933756.19, 5288219.36],
	[-9914188.31, 5284550.38],
	[-9889728.46, 5291888.34],
	[-9873829.56, 5301672.28],
	[-9835916.79, 5300449.28],
	[-9807787.97, 5296780.31],
	[-9785774.10, 5305341.25],
	[-9779659.14, 5337139.06],
	[-9798004.03, 5353037.96],
	[-9837139.79, 5367713.87],
	[-9873829.56, 5370159.85],
	[-9917857.29, 5375051.82]
]];
var urbanCorridorFeature = new ol.Feature(new ol.geom.Polygon(urbanCorridorCoords));

var foxValleyCoords = [[ 
	[-9805341.98842241, 5555443.215766609],
	[-9828578.845021103, 5540767.306335855],
	[-9861599.641240299, 5523645.411999975],
	[-9876275.550671054, 5493070.600685905],
	[-9883613.50538643, 5464941.77427696],
	[-9883613.50538643, 5434366.96296289],
	[-9861599.641240299, 5412353.098816759],
	[-9832247.822378792, 5408684.121459071],
	[-9806564.980874972, 5413576.091269322],
	[-9773544.184655776, 5427029.008247512],
	[-9764983.237487836, 5480840.676160277],
	[-9749084.33560452, 5499185.562948719],
	[-9751530.320509646, 5533429.351620478],
	[-9738077.403531455, 5561558.178029423],
	[-9727070.471458388, 5589687.004438368],
	[-9736854.411078893, 5597024.959153744],
	[-9753976.305414772, 5593355.981796056],
	[-9771098.19975065, 5557889.200671734],
]];
var foxValleyFeature = new ol.Feature(new ol.geom.Polygon(foxValleyCoords));

var DriftlessCoords = [[ 
	[-10155117.829855375, 5433143.970510328],
	[-10158786.807213064, 5405015.144101383],
	[-10157563.8147605, 5378109.310145001],
	[-10144110.89778231, 5363433.4007142475],
	[-10141664.912877185, 5356095.445998871],
	[-10149002.867592562, 5330412.604495051],
	[-10145333.890234873, 5314513.702611734],
	[-10129434.988351557, 5303506.770538669],
	[-10084184.267606732, 5304729.762991232],
	[-10032818.584599094, 5305952.755443795],
	[-10004689.75819015, 5314513.702611734],
	[-9985121.878949143, 5332858.589400177],
	[-9966776.992160702, 5363433.4007142475],
	[-9966776.992160702, 5387893.249765504],
	[-9992459.833664522, 5401346.166743695],
	[-10015696.690263214, 5416022.076174448],
	[-10053609.45629266, 5427029.008247514],
	[-10078069.305343919, 5441704.917678268],
	[-10115982.071373366, 5444150.902583393],
]];
var driftlessFeature = new ol.Feature(new ol.geom.Polygon(DriftlessCoords));

//-------------------------------------------------------------
Ext.create('Ext.data.Store', {
	storeId: 'dss-areas',
	fields: ['name', 'value', 'desc', 'feature'],
	data: [{ 
		name: 'Central Sands', value: 'cs',
		desc: "The remnants of an ancient lake, the area is characterised by sand and p'taters",
		feature: centralSandsFeature
	},{ 
		name: 'Driftless', value: 'd',
		desc: "The driftless area escaped glaciation during the last ice age and is characterized by steep, forested ridges, deeply-carved river valleys, and cold-water trout streams.",
		feature: driftlessFeature
	},{ 
		name: 'Fox River Valley', value: 'frv',
		desc: "Some special risks and opportunities here...",
		feature: foxValleyFeature
	},{ 
		name: 'Urban Corridor', value: 'uc',
		desc: "A mix of comparatively densely populated areas and glaciated bits. It'd be nice to have a train to get us to and fro.",
		feature: urbanCorridorFeature
	}]
});

//------------------------------------------------------------------------------
Ext.define('DSS.app_portal.AOI', {
//------------------------------------------------------------------------------
	extend: 'Ext.grid.Panel',
	alias: 'widget.aoi',
	
	height: 140, width: '100%',
	store: 'dss-areas',
	//style: 'opacity: 0.15',
	title: 'Choose an Area of Interest to Explore',//'Available Areas',
	hideHeaders: true,
	columns:[{
		dataIndex: 'name', flex: 1
	}],
	listeners: {
		viewready: function(self) {
			self.getSelectionModel().select(3);
		},
		selectionchange: function(self, recs) {
			var chartData = Ext.data.StoreManager.lookup('dss-values');
			chartData.setFilters(new Ext.util.Filter({
				property: 'location',
				value: recs[0].get('value')
			}))
			chartData = Ext.data.StoreManager.lookup('dss-proportions');
			chartData.setFilters(new Ext.util.Filter({
				property: 'location',
				value: recs[0].get('value')
			}))
		    maskLayer.getSource().clear();
		    maskLayer.getSource().addFeature(recs[0].get('feature'));
		}
	},

	//--------------------------------------------------------------------------
	initComponent: function() {
		var me = this;
		
		Ext.applyIf(me, {
		});
		
		me.callParent(arguments);
	},
	
});
