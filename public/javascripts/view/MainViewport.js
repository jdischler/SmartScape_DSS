
//------------------------------------------------------------------------------
Ext.define('DSS.view.MainViewport', {
//------------------------------------------------------------------------------
	extend: 'Ext.container.Viewport',
	
	requires: [
	    'DSS.components.MainMap', // likes to be first...
	    'DSS.components.LogoBar',
	    'DSS.components.AttributeBrowser',
	    'DSS.components.ScenarioGrid',
	],

	layout: 'border',
	// minimum widest dimension I could find for tablets/desktop (nexus 7 - 2013)
	//	though most tablets/desktops are at least 1024
	minWidth: 960,
	// min shortest dimension for tablets/desktop (again, nexus 7)
	//	most support at least 768...
	minHeight: 600,
	autoScroll: true,
	
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
					hidden: false,
				}],
			}]
		});
		
		me.callParent(arguments);
	},
	
});

