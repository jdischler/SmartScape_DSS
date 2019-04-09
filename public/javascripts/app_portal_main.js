
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

var niText = 'Net\n    Income';
var scText = 'Soil\nCarbon  ';
var bhText = 'Bird  \nHabitat  ';
var srText = 'Soil  \nRetention  ';
var gbText = '  Gross\n  Biofuel';
Ext.create('Ext.data.Store', {
	storeId: 'dss-values',
	fields: ['name', 'data1', 'location'],
	data: [
	{name: niText, data1: 90.1, location: 'uc'},
	{name: gbText, data1: 80.5, location: 'uc'},
	{name: 'Emissions',data1: 90.7, location: 'uc'},
	{name: srText ,data1: 30.1, location: 'uc'},
	{name: scText, data1: 20.1, location: 'uc'},
	{name: bhText, data1: 10.9, location: 'uc'},
	{name: 'Pest Supression',data1: 15.6, location: 'uc'},
	{name: '     Pollinators',data1: 30.4, location: 'uc'},
	
	{name: niText, data1: 25.1, location: 'cs'},
	{name:  gbText, data1: 70.5, location: 'cs'},
	{name: 'Emissions',data1: 60.7, location: 'cs'},
	{name: srText, data1: 28.1, location: 'cs'},
	{name: scText,data1: 17.1, location: 'cs'},
	{name: bhText, data1: 28.9, location: 'cs'},
	{name: 'Pest Supression',data1: 19.6, location: 'cs'},
	{name: '     Pollinators',data1: 40.4, location: 'cs'},
	
	{name: niText,data1: 35.1, location: 'd'},
	{name:  gbText, data1: 20.5, location: 'd'},
	{name: 'Emissions',data1: 30.7, location: 'd'},
	{name: srText, data1: 20.1, location: 'd'},
	{name: scText,data1: 80.0, location: 'd'},
	{name: bhText, data1: 100.0, location: 'd'},
	{name: 'Pest Supression',data1: 100.0, location: 'd'},
	{name: '     Pollinators',data1: 100.0, location: 'd'},
	
	{name: niText,data1: 100.0, location: 'frv'},
	{name:  gbText, data1: 100.0, location: 'frv'},
	{name: 'Emissions',data1: 100.0, location: 'frv'},
	{name: srText, data1: 40.1, location: 'frv'},
	{name: scText,data1: 32.1, location: 'frv'},
	{name: bhText, data1: 34.9, location: 'frv'},
	{name: 'Pest Supression',data1: 30.6, location: 'frv'},
	{name: '     Pollinators',data1: 45.4, location: 'frv'},
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


/*Ext.draw.sprite.Text.prototype.originalUpdatePlainBBox = Ext.draw.sprite.Text.prototype.updatePlainBBox;

Ext.draw.sprite.Text.prototype.updatePlainBBox = function(plain, useOldSize) {
	var me = this;
	me.originalUpdatePlainBBox(plain, useOldSize);
	//if (me.config && me.config.padding) {
		plain.x += 0;//me.config.padding.width;
		plain.height += -0;//me.config.padding.height;
//	}
}*/
//-------------------------------------------------------------
// Sample Radar
var radarDef = {
	xtype: 'polar',
	itemId: 'DSS-gurf',
	theme: 'custom',
	background: 'transparent',
	border: false,
	flex: 3,
	title: 'Current Conditions',
//	bodyStyle: 'border-bottom: 0',
	innerPadding: 4,
	header: {
		style: 'border-left: 1px solid #aaa; border-top: 1px solid #aaa; border-right: 1px solid #aaa'
	},
	insetPadding: {
		top: 25,
		left: 40,
		right: 40,
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
			minStepSize: 10,
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
			 color: '#333',
			 padding: '0px 28px'
		}
	}]
};

var pieDef = {
	xtype: 'polar',
	innerPadding: 20,
//	bodyStyle: 'border-top: 0',
	title: 'Landcover Proportions',
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

Ext.tip.QuickTipManager.init(); // Instantiate the QuickTipManager


Ext.application({
    name: 'DSS',
    views: [
        'PortalViewport'
    ],
    mainView: 'DSS.view.PortalViewport',
    
	init: function() {
		Ext.state.Manager.setProvider(new Ext.state.CookieProvider());
	},	
});
