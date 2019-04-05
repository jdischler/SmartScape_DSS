// AOI Refinement - Area of Interest Refinement

//------------------------------------------------------------------------------
Ext.define('DSS.app_portal.AOI_Refinement', {
//------------------------------------------------------------------------------
	extend: 'Ext.panel.Panel',
	alias: 'widget.aoi_refinement',
	
	height: 140,
	title: 'Region Refinement Tools',
	
	// MAP Interaction Styles...
	DSS_layerStyle: new ol.style.Style({
	    stroke: new ol.style.Stroke({
	        color: 'rgba(0, 0, 0, 0.3)',
	        width: 1
	    }),
	    fill: new ol.style.Fill({
		    color: 'rgba(128, 32, 255, 0.3)'
		})
	}),
	DSS_hoverStyle: new ol.style.Style({
	    stroke: new ol.style.Stroke({
	        color: 'rgba(0, 0, 0, 0.5)',
	        width: 2
	    }),
	    fill: new ol.style.Fill({
		    color: 'rgba(128, 32, 255, 0.5)'
		})
	}),
	DSS_clickStyle: new ol.style.Style({
	    stroke: new ol.style.Stroke({
	        color: 'rgba(128, 100, 16, 0.9)',
	        width: 2
	    }),
	    fill: new ol.style.Fill({
		    color: 'rgba(255, 200, 16, 0.6)'
		})
	}),
	
	layout: {
		type: 'vbox',
		pack: 'start',
		align: 'stretch'
	},
	bodyPadding: 8,
	width: 380,

	//--------------------------------------------------------------------------
	initComponent: function() {
		var me = this;
		
		Ext.applyIf(me, {
			items: [{
				xtype: 'container',
				html: 'The Area of Interest can be further restricted by choosing a combination of counties or watersheds touching this Region.'
			},{				
				xtype: 'container',
				flex: 1,
				padding: 8,
				layout: {
					type: 'hbox',
					pack: 'center',
					align: 'end'
				},
				items: [{
					xtype: 'button',
					margin: '0 4',
					scale: 'medium',
					width: 140,
					toggleGroup: 'rbc',
					text: 'Restrict by County',
					toggleHandler: function(btn, state) {
						if (state) {
							btn.setText('Done Choosing')
						} else {
							btn.setText('Restrict by County')
						}
					}
				},{
					xtype: 'button',
					margin: '0 4',
					scale: 'medium',
					width: 170,
					toggleGroup: 'rbc',
					text: 'Restrict by Watershed',
					toggleHandler: function(btn, state) {
						if (state) {
							btn.setText('Done Choosing')
						} else {
							btn.setText('Restrict by Watershed')
						}
					}
				}]
			}]
		});
		
		//me.addMapInterface();
		me.callParent(arguments);
	},
	
	//--------------------------------------------------------------------------
	addMapInterface: function() {
		var me = this;
		
		var source = me['DSS_vectorSource'] = new ol.source.Vector({
			url: './assets/portal-counties.geojson',
			format: new ol.format.GeoJSON()
		});
		
		var vectorLayer = me['DSS_vectorLayer'] = new ol.layer.Vector({
			style: me.DSS_layerStyle,
			opacity: 0.1,
			source: source,
			// these potentially reduce performance but looks better
			updateWhileAnimating: true, 
			updateWhileInteracting: true
		})		
		
		globalMap.addLayer(vectorLayer);

		var hoverInteraction = me['DSS_hoverTool'] = new ol.interaction.Select({
			condition: ol.events.condition.pointerMove,
			layers: [vectorLayer],
			style: me.DSS_hoverStyle
		});
		hoverInteraction.setActive(false);
		globalMap.addInteraction(hoverInteraction);

		var clickInteraction = me['DSS_clickTool'] = new ol.interaction.Select({
			condition: ol.events.condition.click,
			layers: [vectorLayer],
			style: me.DSS_clickStyle,
		});
		clickInteraction.setActive(false);
		globalMap.addInteraction(clickInteraction);
		
		clickInteraction.on('select', function(feature) {
			if (feature.selected.length > 0) {
				var sel = feature.selected[0].get("OBJECTID");
				//me.getSelectionModel().select(sel);
			}
		})
	},

	//----------------------------------------------------------
	updateState: function(selected) {
		var me = this;
		if (selected) {
			if (Ext.getCmp('dss-portal-auto-zoom').getValue()) {
				var fs = Ext.getCmp('dss-region-grid').DSS_clickTool.getFeatures();
				if (fs.getLength() > 0) {
					globalView.cancelAnimations();
					globalView.fit(fs.item(0).getGeometry(), {
						padding: [10,10,10,10],
						duration: 750,
						easing: ol.easing.easeOut
					});
				}
			}

			var tmp = new ol.source.Vector();
			me.DSS_vectorLayer.getSource().forEachFeature(function(feature) {
				if (feature.get('REGION') == 0) {
					tmp.addFeature(feature);
				}
			});
			me.DSS_vectorLayer.setSource(tmp);
			me.DSS_vectorLayer.setOpacity(1)
			me.DSS_hoverTool.setActive(true);
			me.DSS_clickTool.setActive(true);
		}
		else {
			if (Ext.getCmp('dss-portal-auto-zoom').getValue()) {
				var fs = Ext.getCmp('dss-region-grid').DSS_vectorLayer.getSource().getFeatures();
				if (fs.length > 0) {
					var e1 = false;
					for (var i=0; i < fs.length; i++) {
						if (i == 0) {
							e1 = fs[i].getGeometry().getExtent();
						} else {
							e1 = ol.extent.extend(e1, fs[i].getGeometry().getExtent());
						}
					}
					
					if (e1) {
						globalView.cancelAnimations();
						globalView.fit(e1, {
							padding: [15,15,15,15],
							duration: 750,
							easing: ol.easing.easeOut
						});
					}
				}
			}
			me.DSS_vectorLayer.setOpacity(0.2)
			me.DSS_hoverTool.setActive(false);
			me.DSS_clickTool.setActive(false);
		}
	}

	
});
