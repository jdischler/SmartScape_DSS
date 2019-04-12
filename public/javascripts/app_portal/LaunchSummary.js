
//------------------------------------------------------------------------------
Ext.define('DSS.app_portal.LaunchSummary', {
//------------------------------------------------------------------------------
	extend: 'Ext.panel.Panel',
	alias: 'widget.launch_summary',
	
	//height: 140,
	title: 'Start SmartScape Confirmation',
	
	layout: {
		type: 'vbox',
		pack: 'start',
		align: 'stretch'
	},
	bodyPadding: 8,
	width: 380,

	//--------------------------------------------------------------------------
	initComponent: function() {
		var me = this;
		
		Ext.applyIf(me, {
			items: [{
				xtype: 'container',
				html: 'SmartScape will now launch with your settings. Exploring scenarios in a different region will require coming back to this page.'
			},{				
				xtype: 'container',
				flex: 1,
				padding: 4,
				layout: {
					type: 'hbox',
					pack: 'center',
					align: 'end'
				},
				items: [{
					xtype: 'button',
					margin: '0 4',
					scale: 'medium',
					width: 190,
					text: 'Start SmartScape',
					handler: function() {
						location.href ="./app"
					}
				}]
			}]
		});
		
		me.callParent(arguments);
	},
	
	//----------------------------------------------------------
	updateState: function(selected) {
		var me = this;
		if (selected) {
		}
		else {
		}
	}

	
});
