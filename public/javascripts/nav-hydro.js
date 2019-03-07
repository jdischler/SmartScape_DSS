Ext.Loader.setConfig({
	enabled: true,
	paths: {
		'GeoExt': '/assets/javascripts/vendor/geo-ext'
	}
});

Ext.define('Ext.chart.theme.Muted2', {
    extend: 'Ext.chart.theme.Base',
    singleton: true,
    alias: [
        'chart.theme.muted2',
        'chart.theme.Muted2'
    ],
    config: {
        colors: [
            Ext.util.Color.fromString('#856585').createLighter().toHex(),
            Ext.util.Color.fromString('#915f44').createLighter().toHex(),
            Ext.util.Color.fromString('#a1a134').createLighter().toHex(),//4091ba
            Ext.util.Color.fromString('#7ca640').createLighter().toHex(),
            Ext.util.Color.fromString('#3b8d8b').createLighter().toHex(),
            Ext.util.Color.fromString('#8f8f8f').createLighter().toHex(),
            Ext.util.Color.fromString('#8f8f8f').createLighter().toHex(),
            Ext.util.Color.fromString('#6e8852').createLighter().toHex(),
            Ext.util.Color.fromString('#3dcc7e').createLighter().toHex(),
            Ext.util.Color.fromString('#a6bed1').createLighter().toHex(),
            Ext.util.Color.fromString('#cbaa4b').createLighter().toHex(),
            Ext.util.Color.fromString('#998baa').createLighter().toHex()
        ]
    }
});

Ext.define('SmartScapeDSS.view.Main', {
	extend: 'Ext.panel.Panel',
	
	layout: {
		type: 'vbox',
		align: 'stretch'
	},
	minWidth: 800,
	minHeight: 600,
	scrollable: true,
	bodyStyle: 'background: #F0F2F0;background: -webkit-linear-gradient(to top, #cfdfcf, #ffe, #f2f2f0);background: linear-gradient(to top, #cfdfcf, #ffe,  #f2f2f0)',

	dockedItems: [{
		xtype: 'container',
		dock: 'bottom',
		height: 100,
		style: 'background-color: #a0b0a0; background: -webkit-linear-gradient(to top, #a0b0a0, 809080);background: linear-gradient(to top, #a0b0a0, #809080);' 
			+ 'color: #fff; text-shadow: 0 0 1px #00000050; font-size: 16px; border-top: 1px solid #00000050 !important;',
		layout: {
			type: 'vbox',
			align: 'middle'
		},
		items: [{
			xtype: 'container',
			margin: 16,
			html: 'Footer text and more. And more and more and more.'
		},{
			xtype: 'container',
			flex: 1
		},{
			xtype: 'container',
			margin: 16,
			html: '&copy; 2019 wei.wisc.edu'
		}]
		
	}],
	items: [{
		xtype: 'container',
		padding: 8,
		layout: {
			type: 'hbox',
			pack: 'start'
		},
		items: [{
			xtype: 'container',
			width: 350,
			html: '<a href="/nav"><img id="ddd" src="assets/images/dss_logo.png" style="width:100%"></a>',
			listeners: {
				afterrender: function(self) {
					Ext.defer(function() {
						self.updateLayout();
					}, 10);
				}	
			}
		}]
	},{
		xtype: 'container',
		flex: 1
	},{
		xtype: 'container',
		margin: 8,
		style: 'font-size: 18px; font-weight: bold; color: #333; text-align: center;',
		html: 'Select an Area of Interest by Hydrological Unit',
	},{
		xtype: 'container',
		layout: {
			type: 'hbox',
			align: 'stretch'
		},
		//height: 300,
		defaults: {
			xtype: 'container',
		},
		items: [{
			flex: 1
		},{
			width: 300,
			height: 340,
			layout: {
				type: 'vbox',
				align: 'stretch'
			},
			items: [{
				xtype: 'container',
				layout: {
					type: 'vbox',
					align: 'stretch'
				},
				margin: '0 8 8 4',
				height: 290,
				style: 'box-shadow: 0 4px 4px #00000010; color: #333; background-color: #fff;padding: 5px; border: 1px solid #ccc; border-radius: 16px; font-size: 18px;font-weight: normal; text-align: center;',
				items: [{
					xtype: 'container',
					html: 'Available Area Presets',
					style: 'font-size: 14px; font-weight: bold; color: #777; text-align: center;',
					margin: '10 0 10 0'
				},{
					xtype: 'container',
					padding: '1 0 0 0',
					margin: '0 0 8 0',
					style: 'background-color: #ccc;',
				},{
					xtype: 'container',
					html: 'Castle Rock Watershed',
					style: 'font-size: 16px; text-align: left;',
					margin: 2, padding: 6
				},{
					xtype: 'container',
					html: 'Kickapoo River Watershed',
					style: 'font-size: 16px; text-align: left; color: #fff; background-color: #0050ffa0; border-radius: 4px',
					margin: 2, padding: 6
				},{
					xtype: 'container',
					html: 'Lower Fox Watershed',
					style: 'font-size: 16px; text-align: left;',
					margin: 2, padding: 6
				},{
					xtype: 'container',
					html: 'Lower Rock Watershed',
					style: 'font-size: 16px; text-align: left;',
					margin: 2, padding: 6
				},{
					xtype: 'container',
					html: 'Lower Wisconsin Watershed',
					style: 'font-size: 16px; text-align: left;',
					margin: 2, padding: 6
				}]
			},{
				xtype: 'container',
				margin: '4 8',
				layout: {
					type: 'hbox',
					align: 'stretch',
					pack: 'center'
				},
				items: [{
					xtype: 'container',
					margin: '0 4', padding: '8 24',
					html: 'Cancel',
					style: 'text-align: center; background-color: #fff; color: #444; border: 1px solid #ccc; border-radius: 8px; font-size: 14px; font-weight: bold'
				},{
					xtype: 'container',
					margin: '0 4', padding: '8 24',
					html: 'Continue',
					style: 'color: #fff; text-align: center; background-color: #0050ffa0; border: 1px solid #ccc; border-radius: 8px; font-size: 14px; font-weight: bold'
				}]
			}]
		},{
			width: 340,
			height: 340,
			margin: '0 4 8 4',
			style: 'box-shadow: 0 4px 4px #00000010; color: #333; background-color: #fff;padding: 5px; border: 1px solid #ccc; border-radius: 16px; font-size: 18px;font-weight: normal; text-align: center;',
			layout: {
				type: 'vbox',
				align: 'middle'
			},
			items: [{
				xtype: 'container',
				width: 300,
				html: '<a href="/alt"><img id="aaa" src="assets/images/HUC_8.jpg" style="width:100%"></a>',
			}]
		},{
			flex: 3
		}]
	},{
		xtype: 'container',
		flex: 8
	}]
	
});

Ext.application({
    name: 'SmartScapeDSS',
    
    mainView: 'SmartScapeDSS.view.Main'
});


