//------------------------------------------------------------------------------
Ext.define('DSS.components.OpacitySlider', {
//------------------------------------------------------------------------------
	extend: 'Ext.menu.Menu',
	alias: 'widget.opacity_slider',
	
	OL_Layer: null, // set to the open layers layer to control... can be an array
	
	// MIN value of 0 is not properly supported. Handling for flowlines will need some
	//	work if this is changed due to them being handled as an array where both
	//	opacities are adjusted at the same time...but one layer is normally toggled as not visible.
	//	When sliding up from zero, there'd have to be code to determine which layer should
	//	be made visible...since sliding to zero would've made both layers invisible.
	minValue: 20, 
	maxValue: 100,
	value: 80,
	increment: 10,
	plain: true,
	bodyPadding: 4,
	
	//--------------------------------------------------------------------------
	initComponent: function() {
	
		var me = this;
		me.value = me.OL_Layer.opacity * 100.0;
		
		if (!me.items) me.items = [];
		me.items.push({
			xtype: 'slider',
			itemId: 'slider',
			hideEmptyLabel: true,
			margin: 0,
			width: 140,
			minValue: me.minValue,
			maxValue: me.maxValue,
			value: me.value,
			increment: me.increment,
			listeners: {
				change: function(slider, newVal) {
					me.value = newVal;
					if (newVal == 0) {
						me.OL_Layer.setVisibility(false);
					}
					else {
						if (me.minValue == 0) {
							me.OL_Layer.setVisibility(true);
						}
						me.OL_Layer.setOpacity(newVal / 100.0);
					}
				}
			}
		});

		me.listeners = Ext.applyIf(me.listeners || {}, {
			show: function(me) {
				me.down('#slider').setValue(me.OL_Layer.getOpacity() * 100.0, false);
			}
		});

		me.callParent(arguments);
	},
	
});
	
function getLegendChip(color, text) {
	
	return {
		xtype: 'container',
		margin: '2 8',
		width: 240,
		layout: 'hbox',
		style: 'background-color: #fff; border-radius: 5px',
		items: [{
			xtype: 'container',
			style: 'background-color: ' + color + '; border-radius: 3px', // matched selection - no conflicts
			height: 16, width: 16,
			margin: '3 3',
		},{
			xtype: 'container',
			html: text,
			margin: '2 6'
		}]
	}	
};

//-----------------------------------------------------
// DSS.components.AttributeBrowser
//
//-----------------------------------------------------
Ext.define('DSS.components.AttributeBrowser', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.attributebrowser',
 
    requires: [
        'DSS.components.AccordionLayout',
        'DSS.components.LayerBase',
        'DSS.components.LayerIndexed',
        'DSS.components.LayerFloat',
        'DSS.components.LayerClickSelect',
        'DSS.components.LayerDrawShape',
    ],
    
	id: 'DSS_findLandByAttr',
	title: 'Find Land By Attributes',
	stateful: true,
	stateId: 'DSS_findLandByAttr',
	
	region: 'west',
	width: 380,
	bodyStyle: 'background-color: #bbb',
	collapsible: 'true',
	scrollable: 'vertical',
	
	layout: {
		type: 'DSS_accordion',
		fill: false,
		multi: true,
		allowCollapseAll: true
	},
	
	dockedItems: [{
		xtype: 'container',
		dock: 'bottom',
		margin: '4 12',
		items: [{
			xtype: 'checkbox',
			boxLabel: 'Enable Advanced Finders',
			checked: false,
			handler: function(self, value) {
				//------------------------------------------------------------
				Ext.suspendLayouts();
				//------------------------------------------------------------
				Ext.each(DSS.Layers.layers, function(layer){
					if (layer.DSS_advancedLayer) {
						layer.setVisible(value);
					}
				})
				//------------------------------------------------------------
				Ext.resumeLayouts(true);
				//------------------------------------------------------------
				DSS.Layers.valueChanged();
			}
		},{
			xtype: 'container',
			padding: '4 8',
			layout: {
				type: 'vbox',
				align: 'middle'
			},
			items: [{
				xtype: 'container',
				padding: '4 8',
				items: [{
					xtype: 'button',
					width: 100,
					scale: 'small',
					textAlign: 'left',
					text: 'Subset',
					iconCls: 'transparency-icon',
					tooltip: DSS.utils.tooltip('Adjust layer transparency'),
					arrowAlign: 'right',
					menuAlign: 'l-r?',
					menu: Ext.create('DSS.components.OpacitySlider', {
						OL_Layer: maskLayer
					})
				}]
			},{
				xtype: 'button',
				width: 100,
				scale: 'small',
				textAlign: 'left',
				text: 'Selection',
				iconCls: 'transparency-icon',
				tooltip: DSS.utils.tooltip('Adjust layer transparency'),
				arrowAlign: 'right',
				menuAlign: 'l-r?',
				menu: Ext.create('DSS.components.OpacitySlider', {
					OL_Layer: selectionLayer
				})
			},
			getLegendChip('#5a79ee','Matching selection'),
			getLegendChip('#d73171','Matched but conflicting selection'),
			getLegendChip('#40495a','Not selected but already trxformed'), {
				xtype: 'container',
				id: 'yes-dss-selection-container',
				layout: 'hbox',
				padding: 8,
				margin: '0 -10',
				flex: 1,
				items: [{
					xtype: 'container',
					html: '<b>Selected:<br/>% Area:</b>',
					width: 60,
					style: 'text-align: right'
				},{
					xtype: 'container',
					id: 'yes-dss-selected-stats',
					width: 115,
					padding: '0 0 0 4',
					html: '--<br/>--'
				},{
					xtype: 'container',
					html: '<b>Occluded:<br/>% Occ.:</b>',
					width: 64,
					style: 'text-align: right'
				},{
					xtype: 'container',
					id: 'yes-dss-selected-stats2',
					width: 115,
					padding: '0 0 0 4',
					html: '--<br/>--'
				}]
			}]
		}]
	}],
	
	listeners: {
		afterrender: function() {
			//------------------------------------------------------------
			Ext.suspendLayouts();
			//------------------------------------------------------------
			var lt = Ext.create('DSS.components.LayerIndexed', {
				title: 'Landcover Type',
				DSS_serverLayer: 'cdl_2012',
				DSS_indexConfig: [
					{boxLabel: 'Corn', 		name: "lt", indexValues: [1]},
					{boxLabel: 'Soy', 		name: "lt", indexValues: [16]},
					{boxLabel: 'Alfalfa', 	name: "lt", indexValues: [17]},
					{boxLabel: 'Grass', 	name: "lt", indexValues: [6]},
					{boxLabel: 'Grains', 	name: "lt", indexValues: [2]},
					{boxLabel: 'Wetlands', 	name: "lt", indexValues: [8]},
					{boxLabel: 'Developed', name: "lt", indexValues: [10,11], checked: true},
					{boxLabel: 'Woodland',	name: "lt", indexValues: [7]}
				],
			});
			DSS.Layers.add(lt);

			lt = Ext.create('DSS.components.LayerIndexed', {
				title: 'Wisc Land',
				DSS_serverLayer: 'wisc_land',
				DSS_columns: 3,
				DSS_indexConfig: [
					{boxLabel: 'Cont. Corn', name: "wl", indexValues: [1]},
					{boxLabel: 'Cash Grain', name: "wl", indexValues: [14]},
					{boxLabel: 'Dairy Rotn', name: "wl", indexValues: [15]},
					{boxLabel: 'Other Crops', name: "wl", indexValues: [16]},
					{boxLabel: 'Grass', 	name: "wl", indexValues: [2,3,4,5]},
					{boxLabel: 'Hay', 		name: "wl", indexValues: [2]},
					{boxLabel: 'Pasture', 	name: "wl", indexValues: [3]},
					{boxLabel: 'Warm Grass', name: "wl", indexValues: [5]},
					{boxLabel: 'Cool Grass', name: "wl", indexValues: [4]},
					{boxLabel: 'Wetlands', 	name: "wl", indexValues: [10]},
					{boxLabel: 'Shrubland', name: "wl", indexValues: [17]},
					{boxLabel: 'Developed', name: "wl", indexValues: [12,13], checked: true},
					{boxLabel: 'Woodland',	name: "wl", indexValues: [6,7,8]},
					{boxLabel: 'Conifers',	name: "wl", indexValues: [6]},
					{boxLabel: 'Deciduous',	name: "wl", indexValues: [7]},
				],
			});
			DSS.Layers.add(lt);

			//----------------------------------------------------------
			DSS.Layers.add(Ext.create('DSS.components.LayerFloat', {
				title: 'Slope',
				DSS_shortTitle: 'Slope',
				DSS_serverLayer: 'slope',
				DSS_layerUnit: ' (degrees)',
				DSS_greaterThanValue: 5
			}));

			//----------------------------------------------------------
			DSS.Layers.add(Ext.create('DSS.components.LayerFloat', {
				title: 'Distance to Streams / Surface Water',
				DSS_shortTitle: 'Distance',
				DSS_serverLayer: 'dist_to_water',
				DSS_lessThanValue: 120,
				DSS_greaterThanValue: null,
				DSS_maxValue: 5250,
				DSS_stepSize: 30
			}));

		/*	//----------------------------------------------------------
			DSS.Layers.add(Ext.create('DSS.components.LayerFloat', {
				title: 'Distance to Rivers',
				DSS_shortTitle: 'Distance',
				DSS_serverLayer: 'rivers',
				DSS_greaterThanValue: 90,
				DSS_maxValue: 5250,
				DSS_stepSize: 30
			}));*/
	
			//----------------------------------------------------------
			DSS.Layers.add(Ext.create('DSS.components.LayerIndexed', {
				title: 'Land Capability Class',
				DSS_serverLayer: 'lcc', DSS_columns: 2,
				DSS_indexConfig: [
					{boxLabel: 'Cropland I (Best)', 	name: "lcc", indexValues: [1], checked: true},
					{boxLabel: 'Cropland II', 	name: "lcc", indexValues: [2]},
					{boxLabel: 'Cropland III', 	name: "lcc", indexValues: [3]},
					{boxLabel: 'Cropland IV', 	name: "lcc", indexValues: [4]},
					{boxLabel: 'Marginal I', 	name: "lcc", indexValues: [5]},
					{boxLabel: 'Marginal II', 	name: "lcc", indexValues: [6]},
					{boxLabel: 'Marginal III', 	name: "lcc", indexValues: [7]},
					{boxLabel: 'Marginal IV (Worst)', 	name: "lcc", indexValues: [8]},
				],
			}));
			
			//----------------------------------------------------------
			DSS.Layers.add(Ext.create('DSS.components.LayerIndexed', {
				title: 'Land Capability Subclass',
				DSS_serverLayer: 'lcs', DSS_columns: 1,
				DSS_indexConfig: [
					{boxLabel: 'Erosion Prone', 	name: "lcs", indexValues: [1], checked: true},
					{boxLabel: 'Saturated Soils', 	name: "lcs", indexValues: [2]},
					{boxLabel: 'Poor Soil Texture', name: "lcc", indexValues: [3]},
				],
			}));
			
			//----------------------------------------------------------
			DSS.Layers.add(Ext.create('DSS.components.LayerFloat', {
				title: 'Distance to Public Lands',
				DSS_shortTitle: 'Distance',
				DSS_serverLayer: 'public_land',
				DSS_lessThanValue: 180,
				DSS_maxValue: 5250,
				DSS_stepSize: 30
			}));
			
			DSS.Layers.add(Ext.create('DSS.components.LayerClickSelect', {
				title: 'HUC-10 Watershed',
				DSS_serverLayer: 'huc-10',
				DSS_vectorLayer: watershed
			}));
			
			DSS.Layers.add(Ext.create('DSS.components.LayerClickSelect', {
				title: 'County',
				DSS_serverLayer: 'counties',
				DSS_vectorLayer: county
			}));
			
			DSS.Layers.add(Ext.create('DSS.components.LayerDrawShape'));
			
			//--------------------- ADVANCED ---------------------------
			
			//----------------------------------------------------------
			DSS.Layers.add(Ext.create('DSS.components.LayerFloat', {
				title: 'Soil Depth',
				DSS_shortTitle: 'Depth',
				DSS_serverLayer: 'depth',
				DSS_layerUnit: ' (mm)',
				DSS_greaterThanValue: 80,
				DSS_maxValue: 205,
				DSS_stepSize: 5,
				DSS_advancedLayer: true,
				hidden: true
			}));

			//----------------------------------------------------------
			DSS.Layers.add(Ext.create('DSS.components.LayerFloat', {
				title: 'Cation Exchange Capacity (CEC)',
				DSS_shortTitle: 'CEC',
				DSS_serverLayer: 'cec',
				DSS_layerUnit: ' (cmol<sub>c </sub>/ kg)',
				DSS_greaterThanValue: 60,
				DSS_maxValue: 190,
				DSS_stepSize: 5,
				DSS_advancedLayer: true,
				hidden: true
			}));

			//----------------------------------------------------------
			DSS.Layers.add(Ext.create('DSS.components.LayerFloat', {
				title: 'Soil Organic Carbon (SOC)',
				DSS_shortTitle: 'SOC',
				DSS_serverLayer: 'soc',
				DSS_layerUnit: ' (? Mg / Ha)',
				DSS_greaterThanValue: 250,
				DSS_maxValue: 1275,
				DSS_stepSize: 25,
				DSS_advancedLayer: true,
				hidden: true
			}));

			//----------------------------------------------------------
			DSS.Layers.add(Ext.create('DSS.components.LayerFloat', {
				title: 'Rainfall Erosivity',
				DSS_shortTitle: 'Erosivity',
				DSS_serverLayer: 'rainfall_erosivity',
				DSS_layerUnit: ' (?)',
				DSS_greaterThanValue: 84,
				DSS_maxValue: 1275,
				DSS_stepSize: 2,
				DSS_advancedLayer: true,
				hidden: true
			}));

			//----------------------------------------------------------
			DSS.Layers.add(Ext.create('DSS.components.LayerFloat', {
				title: 'Silt',
				DSS_shortTitle: 'Silt',
				DSS_serverLayer: 'silt',
				DSS_layerUnit: ' (?)',
				DSS_greaterThanValue: 10,
				DSS_maxValue: 1275,
				DSS_stepSize: 5,
				DSS_advancedLayer: true,
				hidden: true
			}));

			//----------------------------------------------------------
			DSS.Layers.add(Ext.create('DSS.components.LayerFloat', {
				title: 'Corn Production (CP)',
				DSS_shortTitle: 'CP',
				DSS_serverLayer: 'corn_p',
				DSS_layerUnit: ' (?tons?)',
				DSS_greaterThanValue: 10,
				DSS_maxValue: 1275,
				DSS_stepSize: 2,
				DSS_advancedLayer: true,
				hidden: true
			}));

			//----------------------------------------------------------
			DSS.Layers.add(Ext.create('DSS.components.LayerFloat', {
				title: 'Soy Production (SP)',
				DSS_shortTitle: 'SP',
				DSS_serverLayer: 'soy_p',
				DSS_layerUnit: ' (?tons?)',
				DSS_greaterThanValue: 10,
				DSS_maxValue: 1275,
				DSS_stepSize: 2,
				DSS_advancedLayer: true,
				hidden: true
			}));

			//----------------------------------------------------------
			DSS.Layers.add(Ext.create('DSS.components.LayerFloat', {
				title: 'Alfalfa Production (AP)',
				DSS_shortTitle: 'AP',
				DSS_serverLayer: 'alfa_p',
				DSS_layerUnit: ' (?tons?)',
				DSS_greaterThanValue: 8,
				DSS_maxValue: 1275,
				DSS_stepSize: 1,
				DSS_advancedLayer: true,
				hidden: true
			}));
			
			//----------------------------------------------------------
			DSS.Layers.add(Ext.create('DSS.components.LayerFloat', {
				title: 'Grass Production (GP)',
				DSS_shortTitle: 'GP',
				DSS_serverLayer: 'grass_p',
				DSS_layerUnit: ' (?tons?)',
				DSS_greaterThanValue: 6,
				DSS_maxValue: 1275,
				DSS_stepSize: 1,
				DSS_advancedLayer: true,
				hidden: true
			}));
			
			//------------------------------------------------------------
			Ext.resumeLayouts(true);
			//------------------------------------------------------------
			Ext.defer(function() {
				lt.expand();
			}, 250);
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