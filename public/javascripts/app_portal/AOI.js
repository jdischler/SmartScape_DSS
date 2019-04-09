// AOI - Area of Interest

//-------------------------------------------------------------
Ext.create('Ext.data.Store', {
	storeId: 'dss-areas',
	fields: ['name', 'value', 'desc', 'feature'],
	data: [{ 
		name: 'Central Sands', value: 'cs', objectid: 0,
		desc: "The remnants of an ancient lake, the area is characterised by sand and p'taters",
	},{ 
		name: 'Driftless', value: 'd', objectid: 1,
		desc: "The driftless area escaped glaciation during the last ice age and is characterized by steep, forested ridges, deeply-carved river valleys, and cold-water trout streams.",
	},{ 
		name: 'Fox River Valley', value: 'frv', objectid: 2,
		desc: "Some special risks and opportunities here...",
	},{ 
		name: 'Urban Corridor', value: 'uc', objectid: 3,
		desc: "A mix of comparatively densely populated areas and glaciated bits. It'd be nice to have a train to get us to and fro.",
	}]
});

//------------------------------------------------------------------------------
Ext.define('DSS.app_portal.AOI', {
//------------------------------------------------------------------------------
	extend: 'Ext.grid.Panel',
	alias: 'widget.aoi',
	
	height: 140,
//	width: 380,
	store: 'dss-areas',
	title: 'Available Regions',
	
	hideHeaders: true,
	columns:[{
		dataIndex: 'name', flex: 1
	}],
	listeners: {
		viewready: function(self) {
			Ext.defer(function() {
				self.getSelectionModel().select(3);
			}, 500);
		},
		selectionchange: function(sel, recs, e) {
			var me = this;
			var chartData = Ext.data.StoreManager.lookup('dss-values');
			chartData.setFilters(new Ext.util.Filter({
				property: 'location',
				value: recs[0].get('value')
			}))
			chartData = Ext.data.StoreManager.lookup('dss-proportions');
			chartData.setFilters(new Ext.util.Filter({
				property: 'location',
				value: recs[0].get('value')
			}));
			DSS_PortalMap.selectFeature('region', recs[0].get('objectid'), true);
		}
	},

	//--------------------------------------------------------------------------
	initComponent: function() {
		var me = this;
		
		Ext.applyIf(me, {
		});
		
		me.callParent(arguments);
	},
	
	//----------------------------------------------------------
	updateState: function(selected) {
		var me = this;
		if (selected) {
		}
		else {
		}
	}

	
});
