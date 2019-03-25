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

Ext.application({
	name: 'DSS',
	
	init: function() {
		Ext.state.Manager.setProvider(new Ext.state.CookieProvider());
	},	
	launch: function() {
		var normalLogoHeight = 70;
		var olMap = new ol.Map({
			layers: [
				new ol.layer.Tile({
					source: new ol.source.Stamen({
						layer: 'terrain-background' // terrain/ terrain-labels
					})
				}),
				new ol.layer.Tile({
					source: new ol.source.Stamen({
						layer: 'terrain-lines'
					})
				})
			],
			view: new ol.View({
				center: ol.proj.fromLonLat([-89.4, 43.5]),
				zoom: 9
			})
		});
		
		// Sample Radar
		var radarDef = {
			xtype: 'polar',
			itemId: 'DSS-gurf',
			theme: 'custom',
			innerPadding: 20,
			insetPadding: {
				top: 0,
				left: 50,
				right: 50,
				bottom: 0
			},
			animation: {
				duration: 50
			},
			legend: {
				type: 'sprite', // 'sprite' is another possible value
				docked: 'bottom'
			},
			store: {
				fields: ['name', 'data1', 'data2','data3'],
				data: [{
					name: 'Net Income',data1: 90.1,data2: 30.8,data3: 100
				},{
					name: 'Gross Biofuel',data1: 80.5,data2: 20.1,data3: 75.1
				},{
					name: 'Emissions',data1: 90.7,data2: 30.3,data3: 80.1
				},{
					name: 'Soil Retention',data1: 30.1,data2: 98.2,data3: 68.0
				},{
					name: 'Soil Carbon',data1: 20.1,data2: 90.4,data3: 45.0
				},{
					name: 'Bird Habitat',data1: 10.9,data2: 80.2,data3: 96.0
				},{
					name: 'Pest Supression',data1: 15.6,data2: 70.8,data3: 100
				},{
					name: 'Pollinators',data1: 30.4,data2: 86.2,data3: 100
				}]
			},
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
			},{
				type: 'radar',
				title: 'Scenario1',
				angleField: 'name',
				radiusField: 'data2',
				marker: {radius: 4, fillOpacity: 0.7},
				highlight: {fillStyle: '#FFF',strokeStyle: '#000'},
				tooltip: {
					trackMouse: false,
					renderer: function(toolTip, record, ctx) {
						toolTip.setHtml(record.get('name') + ': ' + record.get('data2'));
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
				grid: true
			}, {
				type: 'category',
				position: 'angular',
				fields: 'name',
				style: {
					estStepSize: 1,
					strokeStyle: 'rgba(0,0,0,0)'
				},
				grid: true
			}]
		};
		
		var logoBG = "background: -webkit-linear-gradient(to top, #000, rgb(72,96,32), rgb(210,223,207)) fixed; " + 
		"background: linear-gradient(to top, #000, rgb(72,96,32), rgb(210,223,207)) fixed;";
		
		// animate defs
		var closed = {to:{height:0}};		
		var open = {to:{height:normalLogoHeight}};
		Ext.create('Ext.Viewport', {
			layout: 'border',
			// minimum widest dimension I could find for tablets/desktop (nexus 7 - 2013)
			//	though most tablets/desktops are at least 1024
			minWidth: 960,
			// min shortest dimension for tablets/desktop (again, nexus 7)
			//	most support at least 768...
			minHeight: 600,
			autoScroll: true,
			items: [{
				id: 'DSS_logo',
				xtype: 'container',
				region: 'north',
				style: logoBG,//'background-color: #cfdfcf',//C7CDBA',
				height: normalLogoHeight,
				layout: {
					type: 'hbox',
					pack: 'start',
					align: 'bottom'
				},
				defaults: {
					xtype: 'button',
					margin: 'auto 3',
					scale: 'large',
					toggleGroup: 'DSS-mode',
					allowDepress: false
				},
				items: [{
					xtype: 'container',
					margin: '0 0 0 36',
					width: 310,
					html: '<a href="/assets/wip/landing_bs.html"><img id="ddd" src="assets/images/dss_logo.png" style="width:100%"></a>',
					listeners: {
						afterrender: function(self) {
							Ext.defer(function() {
								self.updateLayout();
							}, 10);
						}	
					}
				},{
					text: 'Explore Landscape',
					margin: '0 2 0 48',
					width: 140,
					pressed: true,
					handler: function(self) {
						Ext.suspendLayouts();
							Ext.getCmp('DSS_findLandByAttr').setCollapsed(false);
							Ext.getCmp('DSS_resultsPanel').setCollapsed(true);
						Ext.resumeLayouts(true);
					}
				},{
					text: 'Create Scenarios',
					width: 140,
					handler: function(self) {
						Ext.suspendLayouts();
							Ext.getCmp('DSS_findLandByAttr').setCollapsed(false);
							Ext.getCmp('DSS_resultsPanel').setCollapsed(true);
						Ext.resumeLayouts(true);
					}
				},{
					id: 'DSS_analyzeButton',
					text: 'Analyze Results',
					width: 140,
					toggleHandler: function(self, state) {
						if (state) {
							Ext.suspendLayouts();
								Ext.getCmp('DSS_findLandByAttr').setCollapsed(true);
								Ext.getCmp('DSS_resultsPanel').setCollapsed(false);
							Ext.resumeLayouts(true);
						}
					}
				},{
					xtype: 'container',
					flex: 1,
				},{
					id: 'DSS_LoginButton',
					text: 'Login',
					minWidth: 96
				},{
					id: 'DSS_ExtraButton',
					text: 'Admin',
					hidden: true
				}]
			},{
				id: 'DSS_findLandByAttr',
				xtype: 'panel',
				title: 'Find Land By Attributes',
				collapsible: 'true',
				scrollable: 'vertical',
				stateful: true,
				stateId: 'DSS_findLandByAttr',
				region: 'west',
				bodyStyle: 'background-color: #bbb',
				width: 380,
			},{
				id: 'DSS_resultsPanel',
				xtype: 'panel',
				title: 'Results',
				collapsible: 'true',
				collapsed: true,
				stateful: true,
				stateId: 'DSS_resultsPanel',
				layout: {
					type:'vbox',
					pack: 'start',
					align: 'stretch'
				},
				region: 'east',
				bodyStyle: 'background-color: #bbb; border-left-width: 0;',
				minWidth: 300,
				maxWidth: 500,
				resizeHandles: 'w',
				resizable: true,
				items: [
					radarDef
				],
				listeners: {
					resize: function(self, width, height, oldWidth, oldHeight, eOpts) {
						self.down('#DSS-gurf').setHeight(width);
					}
				}
			},{
				xtype: 'panel',
				header: {
					style: 'border-right: 1px solid #bcd; border-left: 1px solid #bcd;'
				},
				region: 'center',
				layout: 'fit',
				items: [{
					xtype: 'gx_map',
					map: olMap,
					animate: false
				}],
			}]
		});
		
	}
	
});
