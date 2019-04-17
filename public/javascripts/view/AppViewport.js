
//------------------------------------------------------------------------------
Ext.define('DSS.view.AppViewport', {
//------------------------------------------------------------------------------
	extend: 'Ext.container.Viewport',
	
	requires: [
	    'DSS.components.MainMap', // likes to be first...
	    'DSS.components.d3_nav', // this also likes to be here even if not used directly in this object
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
					xtype: 'container',
				    id: 'dss-scenario-grid',
					style: 'background-color: rgb(245,245,245)',
				    dock: 'bottom',
				    hidden: true,
				  	resizable: {minHeight:172, maxHeight: 246 + 28},
					resizeHandles: 'n',
				    layout: {
				    	type: 'hbox',
				    	align: 'stretch',
				    	pack: 'start'
				    },
				    items: [{
				    	xtype: 'container',
				    	flex: 1
				    },{
				    	xtype: 'scenario_grid',
				    	flex: 20
				    },{
				    	xtype: 'container',
				    	flex: 1
				    }],
				}],
			}]
		});
		
		me.callParent(arguments);
	},
	
});

