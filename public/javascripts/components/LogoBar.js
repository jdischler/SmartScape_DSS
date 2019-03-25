//-----------------------------------------------------
// DSS.components.LogoBar
//
//-----------------------------------------------------
Ext.define('DSS.components.LogoBar', {
    extend: 'Ext.container.Container',
    alias: 'widget.logobar',
    
    require: [
    	'DSS.components.Step4',
	],
    
	region: 'north',
	height: 71,
	style: "background: -webkit-linear-gradient(to top, #000, rgb(72,96,32), rgb(210,223,207)) fixed; " + 
		"background: linear-gradient(to top, #000, rgb(72,96,32), rgb(210,223,207)) fixed;",
	
	layout: {
		type: 'hbox',
		pack: 'start',
		align: 'bottom'
	},
	defaults: {
		xtype: 'button',
		scale: 'large',
		toggleGroup: 'DSS-mode',
		allowDepress: false
	},

	items: [{
		xtype: 'container',
		margin: '0 0 0 36',
		width: 310, 
		height: 71, // a fixed height improves page layout responsiveness unfortunately
		html: '<a href="/assets/wip/landing_bs.html"><img id="ddd" src="assets/images/dss_logo.png" style="width:100%"></a>',
	},{
		text: 'Explore Landscape',
		margin: '-1 2 0 48',
		width: 140,
		pressed: true
	},{
		text: 'Transform Landscape',
		margin: '-1 2 0 2',
		width: 140,
		toggleHandler: function(self, pressed) {
			Ext.getCmp('dss-scenario-grid').setVisible(pressed);
		}
	},{
		text: 'Analyze Results',
		margin: '-1 2 0 2',
		width: 140, height: 62
	},{
		text: 'Step 4 Mockup',
		margin: '-1 2 0 2',
		width: 140, height: 62,
		handler: function() {
			Ext.create('DSS.components.Step4').show().center();
		}
	},{
		xtype: 'container',
		flex: 1,
	}]
	
});
