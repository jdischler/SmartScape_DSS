// TODO: set up a proper main and sub-asset folder doodad

Ext.Loader.setConfig({
	enabled: true,
	paths: {
		'GeoExt': '/assets/javascripts/vendor/geo-ext'
	}
});

Ext.require([
    'GeoExt.component.Map',
    'Ext.panel.Panel',
    'Ext.Viewport'
]);

Ext.define('Ext.chart.theme.Custom', {
    extend: 'Ext.chart.theme.Base',
    singleton: true,
    alias: 'chart.theme.custom',
    config: {
		legend: {
			label: {
				fontSize: 13,
				fontWeight: 'default',
				fontFamily: 'default',
				fillStyle: 'black'
			}
		}
    }
});		

Ext.create('Ext.data.Store', {
	storeId: 'dss-values',
	fields: ['name', 'data1', 'location'],
	data: [
	{name: 'Net Income',data1: 90.1, location: 'uc'},
	{name: 'Gross Biofuel',data1: 80.5, location: 'uc'},
	{name: 'Emissions',data1: 90.7, location: 'uc'},
	{name: 'Soil Retention',data1: 30.1, location: 'uc'},
	{name: 'Soil Carbon',data1: 20.1, location: 'uc'},
	{name: 'Bird Habitat',data1: 10.9, location: 'uc'},
	{name: 'Pest Supression',data1: 15.6, location: 'uc'},
	{name: 'Pollinators',data1: 30.4, location: 'uc'},
	
	{name: 'Net Income',data1: 25.1, location: 'cs'},
	{name: 'Gross Biofuel',data1: 70.5, location: 'cs'},
	{name: 'Emissions',data1: 60.7, location: 'cs'},
	{name: 'Soil Retention',data1: 28.1, location: 'cs'},
	{name: 'Soil Carbon',data1: 17.1, location: 'cs'},
	{name: 'Bird Habitat',data1: 28.9, location: 'cs'},
	{name: 'Pest Supression',data1: 19.6, location: 'cs'},
	{name: 'Pollinators',data1: 40.4, location: 'cs'},
	
	{name: 'Net Income',data1: 35.1, location: 'd'},
	{name: 'Gross Biofuel',data1: 20.5, location: 'd'},
	{name: 'Emissions',data1: 30.7, location: 'd'},
	{name: 'Soil Retention',data1: 20.1, location: 'd'},
	{name: 'Soil Carbon',data1: 80.0, location: 'd'},
	{name: 'Bird Habitat',data1: 100.0, location: 'd'},
	{name: 'Pest Supression',data1: 100.0, location: 'd'},
	{name: 'Pollinators',data1: 100.0, location: 'd'},
	
	{name: 'Net Income',data1: 100.0, location: 'frv'},
	{name: 'Gross Biofuel',data1: 100.0, location: 'frv'},
	{name: 'Emissions',data1: 100.0, location: 'frv'},
	{name: 'Soil Retention',data1: 40.1, location: 'frv'},
	{name: 'Soil Carbon',data1: 32.1, location: 'frv'},
	{name: 'Bird Habitat',data1: 34.9, location: 'frv'},
	{name: 'Pest Supression',data1: 30.6, location: 'frv'},
	{name: 'Pollinators',data1: 45.4, location: 'frv'},
	],

	filters: [{
		property: 'location',
		value: /nope/
		
	}]
});

Ext.create('Ext.data.Store', {
	storeId: 'dss-proportions',
	fields: ['name', 'data1', 'location'],
	data: [
	{name: 'Row Crops',		data1: 32.2, 	location: 'uc'},
	{name: 'Woodland',		data1: 24.3, 	location: 'uc'},
	{name: 'Grasses',		data1: 22.4,	location: 'uc'},
	{name: 'Wetlands/Water',data1: 8.8, 	location: 'uc'},
	{name: 'Developed',		data1: 8.3, 	location: 'uc'},
	{name: 'Other',			data1: 4, 		location: 'uc'},

	{name: 'Row Crops',		data1: 26.2, 	location: 'cs'},
	{name: 'Woodland',		data1: 26.3, 	location: 'cs'},
	{name: 'Grasses',		data1: 22.4,	location: 'cs'},
	{name: 'Wetlands/Water',data1: 4.8, 	location: 'cs'},
	{name: 'Developed',		data1: 5.3, 	location: 'cs'},
	{name: 'Other',			data1: 6, 		location: 'cs'},

	{name: 'Row Crops',		data1: 22.2, 	location: 'd'},
	{name: 'Woodland',		data1: 38.3, 	location: 'd'},
	{name: 'Grasses',		data1: 22.4,	location: 'd'},
	{name: 'Wetlands/Water',data1: 4.8, 	location: 'd'},
	{name: 'Developed',		data1: 4.3, 	location: 'd'},
	{name: 'Other',			data1: 4, 		location: 'd'},
	
	{name: 'Row Crops',		data1: 38.2, 	location: 'frv'},
	{name: 'Woodland',		data1: 22.3, 	location: 'frv'},
	{name: 'Grasses',		data1: 22.4,	location: 'frv'},
	{name: 'Wetlands/Water',data1: 9.8, 	location: 'frv'},
	{name: 'Developed',		data1: 7.3, 	location: 'frv'},
	{name: 'Other',			data1: 3, 		location: 'frv'},
	
	],
	
	filters: [{
		property: 'location',
		value: /nope/
	}]

});

//-------------------------------------------------------------
var treeStore = Ext.create('Ext.data.Store', {
	storeId: 'dss-areas',
	fields: ['name', 'data1'],
	data: [{ 
		name: 'Central Sands', value: 'cs', img: 'assets/images/other_5.gif',
		desc: "The remnants of an ancient lake, the area is characterised by sand and p'taters"
	},{ 
		name: 'Driftless', value: 'd', img: 'assets/images/other_6.gif',
		desc: "The driftless area escaped glaciation during the last ice age and is characterized by steep, forested ridges, deeply-carved river valleys, and cold-water trout streams."
	},{ 
		name: 'Fox River Valley', value: 'frv', img: 'assets/images/other_4.gif',
		desc: "Some special risks and opportunities here..."
	},{ 
		name: 'Urban Corridor', value: 'uc', img: 'assets/images/other_2.gif',
		desc: "A mix of comparatively densely populated areas and glaciated bits. It'd be nice to have a train to get us to and fro."
	}]
});

//-------------------------------------------------------------
// Sample Radar
var radarDef = {
	xtype: 'polar',
	itemId: 'DSS-gurf',
	theme: 'custom',
	flex: 3,
	title: 'Current State of Selected Area',
//	bodyStyle: 'border-bottom: 0',
	innerPadding: 20,
	header: {
		style: 'border-left: 1px solid #aaa; border-top: 1px solid #aaa; border-right: 1px solid #aaa'
	},
	insetPadding: {
		top: 25,
		left: 50,
		right: 50,
		bottom: 25
	},
	animation: {
		duration: 250
	},
	store: 'dss-values',
	series: [{
		type: 'radar',
		title: 'Default',
		angleField: 'name',
		radiusField: 'data1',
		marker: {radius: 4, fillOpacity: 0.7},
		highlight: {fillStyle: '#FFF',strokeStyle: '#000'},
		tooltip: {
			trackMouse: false,
			renderer: function(toolTip, record, ctx) {
				toolTip.setHtml(record.get('name') + ': ' + record.get('data1'));
			}
		},			
		style: {fillOpacity: .3}
	}],
	axes: [{
		type: 'numeric',
		position: 'radial',
		fields: 'data1',
		style: {
			estStepSize: 10
		},
		minimum: 0,
		maximum: 100,
		grid: true,
		label: {
			 font: '12px Helvetica',
			 color: '#333'
		}
	}, {
		type: 'category',
		position: 'angular',
		fields: 'name',
		style: {
			estStepSize: 1,
			strokeStyle: 'rgba(0,0,0,0)'
		},
		grid: true,
		label: {
			 font: '12px Helvetica',
			 color: '#333'
		}
	}]
};

var pieDef = {
	xtype: 'polar',
	innerPadding: 20,
//	bodyStyle: 'border-top: 0',
	title: 'Landcover Proportions for Selected Area',
	header: {
		style: 'border-left: 1px solid #aaa; border-right: 1px solid #aaa'
	},
	flex: 2,
	//theme: 'category1',
	insetPadding: {
		top: 15,
		left: 45,
		right: 35,
		bottom: 0
	},
	animation: {
		duration: 300
	},
	store: 'dss-proportions',
	interactions: ['rotate', 'itemhighlight'],
		   series: {
			   colors: ['#f3e45c','#a4b85c','#f3a05a','#6f9fdc','#b7b7b7','#bf8a9a'],
		       type: 'pie3d',
		       highlight: true,
		       angleField: 'data1',
		       label: {
		           field: 'name',
		           display: 'rotate',//'rotate',
		           color: '#333',
					 font: '12px Helvetica'
		       },
		       donut: 40,
		       thickness: 10,
		       distortion: 0.45
		   }		
}
Ext.application({
	name: 'DSS',
	
	init: function() {
		Ext.state.Manager.setProvider(new Ext.state.CookieProvider());
	},	
	launch: function() {
				
		var logoBG = "background: #F0F2F0;background: -webkit-linear-gradient(to top, #cfdfcf, #ffe, #f2f2f0) fixed; background: linear-gradient(to top, #cfdfcf, #ffe,  #f2f2f0) fixed;";
		
		Ext.create('Ext.Viewport', {
			minHeight: 640,
			renderTo: Ext.getBody(),
			style: logoBG,
			autoScroll: true,
			layout: {
				type: 'vbox',
				align: 'middle'
			},
			defaults: {
				xtype: 'container',
			},
			items: [{
				width: 310, height: 70,
				margin: '16 0 0 0',
				html: '<a href="/assets/wip/landing_bs.html"><img src="assets/images/dss_logo.png" style="width:100%"></a>',
			},{
				html: 'Choose an Area of Interest to Explore',
				margin: '24 8 0 8',
				style: 'color: #333; font-size: 18px; font-weight: bold'
			},{
				flex: 20,
				margin: 8,
				maxHeight: 520,
				maxWidth: 860,
				width: '100%',
				layout: {
					type: 'hbox',
					pack: 'start',
					align: 'stretch',
				},
				items: [{
					xtype: 'container',
					margin: '0 8 0 0',
					width: 320,
					layout: {
						type: 'vbox',
						pack: 'start',
						align: 'stretch'
					},
					items: [{
						xtype: 'container',
						style: 'border: 1px solid #ccc; border-radius: 2px; background-color: #fff',
						flex: 1,
						layout: {
							type: 'vbox',
							align: 'middle'
						},
						items: [/*{
							xtype: 'combo',
							width: '100%',
							margin: 8,
							queryMode: 'local',
							store: 'dss-areas',
							fieldLabel: 'Area',
							labelAlign: 'right',
							labelWidth: 40,
							multiSelect: false,
							forceSelection: true,
							displayField: 'name', 
							valueField: 'value',
							//value: 'uc',
							listeners: {
								afterrender: function(self) {
									self.setSelection(self.getStore().findRecord('value','uc'));
								},
								select: function(self, rec) {
									Ext.getCmp('dss-map-logo').update(
										'<img style="background-size: cover; width: 100%" src="' + rec.get('img') + '">'
									);
									var chartData = Ext.data.StoreManager.lookup('dss-values');
									chartData.setFilters(new Ext.util.Filter({
										property: 'location',
										value: rec.get('value')
									}))
								}
							}
						},*/{
							xtype: 'container',
							id: 'dss-map-logo',
							flex: 2,
							padding: '0 0 0 24',
							width: '75%',
							html: '<img style="background-size: cover; width: 100%" src="assets/images/other_4.gif">'
						},{
							xtype: 'container',
							id: 'dss-description',
							padding: '8 16',
							width: '100%',
							style: 'color: #777'
						},{
							xtype: 'grid',
							height: 150, width: '100%',
							store: 'dss-areas',
							header: {
								style: 'border-top: 1px #ccc solid'
							},
							title: 'Available Areas',
							hideHeaders: true,
							columns:[{
								dataIndex: 'name', flex: 1
							}],
							listeners: {
								viewready: function(self) {
									self.getSelectionModel().select(3);
								},
								selectionchange: function(self, recs) {
									Ext.getCmp('dss-map-logo').update(
										'<img style="background-size: contain; width: 100%" src="' + recs[0].get('img') + '">'
									);
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
									Ext.getCmp('dss-description').update('<b>About the ' + recs[0].get('name') + '</b><br/>' + recs[0].get('desc'))
								}
							}
						}]
					},{
						xtype: 'container',
						layout: {
							type: 'hbox',
							pack: 'end',
							align: 'stretch'
						},
						items: [{
							xtype: 'button',
							scale: 'medium',
							text: 'Back',
							margin: '4 6 0 0',
							width: 100
						},{
							xtype: 'button',
							scale: 'medium',
							text: 'Ok',
							margin: '4 8 0 6',
							width: 100,
							handler: function() {
								location.href = '/app';
							}
							
						}]
					}]
				},{
					xtype: 'container',
					flex: 1,
					layout: {
						type: 'vbox',
						align: 'stretch',
						pack: 'start'
					},
					items: [radarDef, pieDef]
				}]
			},{
				flex: 1,
			},{
				padding: 8,
				width: '100%',
				style: 'background-color: #a0b0a0; background: -webkit-linear-gradient(to top, #a0b0a0, 809080);background: linear-gradient(to top, #a0b0a0, #809080); color: #fff; text-shadow: 0 0 1px #00000050; font-size: 18px; border-top: 1px solid #00000050;',
				layout: {
					type: 'vbox',
					align: 'middle'
				},
				defaults: {
					xtype: 'container', margin: 8
				},
				items: [{
					html: 'Footer text and more. And more and more and more',
				},{
					html: '&copy;2019 wei.wisc.edu'
				}]
			}]
		});
		
	}
	
});
