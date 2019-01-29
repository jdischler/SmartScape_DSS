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
			},{
				type: 'radar',
				title: 'Scenario2',
				angleField: 'name',
				radiusField: 'data3',
				marker: {radius: 4, fillOpacity: 0.7},
				highlight: {fillStyle: '#FFF',strokeStyle: '#000'},
				tooltip: {
					trackMouse: false,
					renderer: function(toolTip, record, ctx) {
						toolTip.setHtml(record.get('name') + ': ' + record.get('data3'));
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
		
		// animate defs
		var closed = {to:{height:0}};		
		var open = {to:{height:normalLogoHeight}};
		Ext.create('Ext.Viewport', {
			layout: 'border',
			minWidth: 800,
			autoScroll: true,
			items: [{
				id: 'DSS_logo',
				xtype: 'container',
				region: 'north',
				style: 'background-color: #C7CDBA',
				height: normalLogoHeight,
				layout: {
					type: 'hbox',
					pack: 'start',
					align: 'middle'
				},
				defaults: {
					xtype: 'button',
					margin: 'auto 3',
					scale: 'large',
				},
				items: [{
					xtype: 'container',
					margin: '-1 2 0 0',
					flex: 1,
					minWidth: 220,
					maxWidth: 320,
					html: '<a href="http://www.glbrc.org"><img id="ddd" src="assets/images/dss_logo.png" style="width:100%"></a>',
					listeners: {
						afterrender: function(self) {
							Ext.defer(function() {
								self.updateLayout();
							}, 10);
						}	
					}
				},{
					text: 'SmartScape&#8482 Help', // tm = &#8482;
					aURL: 'http://youtu.be/XxZvzqFZTU8',
					handler: function(self) {
						javascript:window.open(self.aURL,'_blank');return false;
					}
				},{
					text: 'Gratton Lab',
					aURL: 'http://gratton.entomology.wisc.edu',
					handler: function(self) {
						javascript:window.open(self.aURL,'_blank');return false;
					}
				},{
					text: 'WEI Homepage',
					aURL: 'https://energy.wisc.edu',
					handler: function(self) {
						javascript:window.open(self.aURL,'_blank');return false;
					}
				},{
					xtype: 'container',
					flex: 1,
				},{
					id: 'DSS_LoginButton',
					text: 'Login'
				},{
					id: 'DSS_ExtraButton',
					text: 'Admin',
					hidden: true
				}]
			},{
				xtype: 'panel',
				title: 'Configure',
				collapsible: 'true',
				region: 'west',
				bodyStyle: 'background-color: #bbb',
				minWidth: 300,
				maxWidth: 400,
				resizeHandles: 'e',
				resizable: true
			},{
				xtype: 'panel',
				title: 'Results',
				collapsible: 'true',
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
				title: 'Landscape Viewer',
				header: {
					style: 'border-right: 1px solid #bcd; border-left: 1px solid #bcd;'
				},
				region: 'center',
				layout: 'fit',
				tools: [{
					type: 'up',
					tooltip: 'Collapse panel',
					callback: function(owner, self, evt) {
						if (self.type === 'up') {
							Ext.getCmp('DSS_logo').animate(closed);
							self.setType('down');
						}
						else {
							Ext.getCmp('DSS_logo').animate(open);
							self.setType('up');
						}
					}
				}],
				items: [{
					xtype: 'gx_map',
					map: olMap
				}]
			}]
		});
	}
	
});
