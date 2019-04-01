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
	height: 60,
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
		padding: '2 0 0 10',
		width: 340, 
		height: 60, // a fixed height improßves page layout responsiveness unfortunately
		html: '<a href="/"><img id="ddd" src="assets/images/dss_logo.png" style="width:75%"></a>',
	},{
		text: 'Explore Landscape',
		margin: '-1 2 0 48',
		width: 130,
		pressed: true
	},{
		text: 'Transform Landscape',
		margin: '-1 2 0 2',
		width: 130,
		toggleHandler: function(self, pressed) {
			Ext.getCmp('dss-scenario-grid').setVisible(pressed);
		}
	},{
		text: 'Analyze Results',
		margin: '-1 2 0 2',
		width: 130
	},{
		text: 'Step 4 Mockup',
		margin: '-1 2 0 2',
		width: 130,
		handler: function() {
			Ext.create('DSS.components.Step4').show().center();
		}
	},{
		xtype: 'container',
		flex: 1,
	}]
	
});
