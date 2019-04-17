//-----------------------------------------------------
// DSS.components.LogoBar
//
//-----------------------------------------------------
Ext.define('DSS.components.LogoBar', {
    extend: 'Ext.container.Container',
    alias: 'widget.logobar',
    
    require: [
    	'DSS.components.Step4',
		'DSS.components.d3_nav'
	],
    
	region: 'north',
	height: 60,
	style: "background: -webkit-linear-gradient(to top, #000, rgb(72,96,32), rgb(210,223,207)) fixed; " + 
		"background: linear-gradient(to top, #000, rgb(72,96,32), rgb(210,223,207)) fixed;",
	
	layout: {
		type: 'hbox',
		pack: 'start',
//		align: 'center'
	},

	items: [{
		xtype: 'component',
		margin: '1 0 0 10',
		width: 260, 
		height: 60, // a fixed height improves page layout responsiveness unfortunately
		html: '<a href="/"><img id="ddd" src="assets/images/dss_logo.png" style="width:100%"></a>',
	},{
		xtype: 'd3_nav',
		width: 640, 
		height: 58,
		padding: '4 0 0 32',
		DSS_tooltipOffset: [-64,-18],
		DSS_align: 'l',
		DSS_duration: 500.0,
		DSS_elements: [{
			text: 'Explore',
			active: true,
			activeText: 'Explore Landscape',
			tooltip: 'Find existing landscape matching selected attributes',
			DSS_selectionChanged: function(selected) {
				if (selected) {
				}
			}
		},{
			text: 'Transform',
			activeText: 'Transform the Landscape',
			tooltip: 'Alter the landcover in selected areas to create a user-scenario',
			DSS_selectionChanged: function(selected) {
				Ext.getCmp('dss-scenario-grid').setVisible(selected);
			}
		},{
			text: 'Analyze',
			activeText: 'Analyze Results',
			tooltip: 'Analyze the modeled outcomes from the user-chosen landscape changes',
			DSS_selectionChanged: function(selected) {
			}
		},{
			text: 'Next?',
			activeText: 'Step 4 (what next?) Mockup',
			tooltip: 'Dig a little deeper and explore meta-model outcomes',
			DSS_selectionChanged: function(selected) {
				if (selected) {
					Ext.create('DSS.components.Step4').show().center();
				}
			}
		}]
	},{
		xtype: 'container',
		flex: 1,
	}]
	
});
