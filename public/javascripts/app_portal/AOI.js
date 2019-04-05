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
	
	// MAP Interaction Styles...
	DSS_layerStyle: new ol.style.Style({
	    stroke: new ol.style.Stroke({
	        color: 'rgba(0, 0, 0, 0.3)', width: 1
	    }),
	    fill: new ol.style.Fill({
		    color: 'rgba(128, 32, 255, 0.3)'
		})
	}),
	DSS_hoverStyle: new ol.style.Style({
	    stroke: new ol.style.Stroke({
	        color: 'rgba(0, 0, 0, 0.5)', width: 2
	    }),
	    fill: new ol.style.Fill({
		    color: 'rgba(128, 32, 255, 0.5)'
		})
	}),
	DSS_clickStyle: new ol.style.Style({
	    stroke: new ol.style.Stroke({
	        color: 'rgba(255, 255, 64, 0.9)', width: 2
	    }),
	    fill: new ol.style.Fill({
		    color: 'rgba(255, 255, 32, 0.6)'
		})
	}),
	
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
		 //   maskLayer.getSource().clear();
		  //  maskLayer.getSource().addFeature(recs[0].get('feature'));
		}
	},

	//--------------------------------------------------------------------------
	initComponent: function() {
		var me = this;
		
		Ext.applyIf(me, {
		});
		
		me.addMapInterface();
		me.callParent(arguments);
	},
	
	//--------------------------------------------------------------------------
	addMapInterface: function() {
		var me = this;
		
		var vectorLayer = me['DSS_vectorLayer'] = new ol.layer.Vector({
			style: me.DSS_layerStyle,
			opacity: 0.75,
			source: new ol.source.Vector({
				url: './assets/regions.geojson',
				format: new ol.format.GeoJSON()
			}),
			// these potentially reduce performance but looks better
			updateWhileAnimating: true, 
			updateWhileInteracting: true
		});
		globalMap.addLayer(vectorLayer);

		var hoverInteraction = me['DSS_hoverTool'] = new ol.interaction.Select({
			condition: ol.events.condition.pointerMove,
			layers: [vectorLayer],
			style: me.DSS_hoverStyle
		});
		globalMap.addInteraction(hoverInteraction);

		var clickInteraction = me['DSS_clickTool'] = new ol.interaction.Select({
			condition: ol.events.condition.click,
			toggleCondition: ol.events.condition.never,
			layers: [vectorLayer],
			style: me.DSS_clickStyle,
		});
		
		globalMap.addInteraction(clickInteraction);
		clickInteraction.on('select', function(feature) {
			if (feature.selected.length > 0) {
				var sel = feature.selected[0].get("OBJECTID");
				me.getSelectionModel().select(sel);
			}
		})
	},

	//----------------------------------------------------------
	updateState: function(selected) {
		var me = this;
		if (selected) {
			me.DSS_vectorLayer.setOpacity(0.75)
			me.DSS_hoverTool.setActive(true);
			me.DSS_clickTool.setActive(true);
		}
		else {
			me.DSS_vectorLayer.setOpacity(0.2)
			me.DSS_hoverTool.setActive(false);
			me.DSS_clickTool.setActive(false);
		}
	}

	
});
