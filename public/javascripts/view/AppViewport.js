
//------------------------------------------------------------------------------
Ext.define('DSS.view.AppViewport', {
//------------------------------------------------------------------------------
	extend: 'Ext.container.Viewport',
	
	requires: [
	    'DSS.components.MainMap', // likes to be first...
	    'DSS.components.LogoBar',
	    'DSS.components.AttributeBrowser',
	    'DSS.components.ScenarioGrid',
	],

	// most desktops/tablets support 1024x768 but Nexus 7 (2013) is a bit smaller so target that if at all possible
	minWidth: 960,
	minHeight: 600,
	
	autoScroll: true,
    renderTo: Ext.getBody(),
	layout: 'border',
    
	//--------------------------------------------------------------------------
	initComponent: function() {
		var me = this;
		
		Ext.applyIf(me, {
			items: [{
				xtype: 'logobar'
			},{
				xtype: 'attributebrowser'
			},{
				xtype: 'mainmap',
				dockedItems: [{
				    id: 'dss-scenario-grid',
					xtype: 'scenario_grid',
					dock: 'bottom',
					hidden: true,
				}],
			}]
		});
		
		me.callParent(arguments);
	},
	
});

