// AOI Refinement - Area of Interest Refinement

//------------------------------------------------------------------------------
Ext.define('DSS.app_portal.AOI_Refinement', {
//------------------------------------------------------------------------------
	extend: 'Ext.panel.Panel',
	alias: 'widget.aoi_refinement',
	
//	height: 140,
	title: 'Region Refinement Tools',
	
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
				html: 'The Area of Interest can optionally be further restricted by choosing counties or watersheds that overlap this Region.'
			},{				
				xtype: 'container',
				flex: 1,
				padding: '16 8 9 8',
				layout: {
					type: 'hbox',
					pack: 'center',
					align: 'end'
				},
				items: [{
					xtype: 'button',
					margin: '0 4',
					scale: 'medium',
					width: 150,
					toggleGroup: 'rbc',
					text: 'Restrict by County',
					toggleHandler: function(btn, state) {
						if (state) {
							btn.setText('Done Choosing')
							DSS_PortalMap.setMode('county');
						} else {
							btn.setText('Restrict by County')
							DSS_PortalMap.setMode('refine');
						}
					}
				},{
					xtype: 'button',
					margin: '0 4',
					scale: 'medium',
					width: 170,
					toggleGroup: 'rbc',
					text: 'Restrict by Watershed',
					toggleHandler: function(btn, state) {
						if (state) {
							btn.setText('Done Choosing')
							DSS_PortalMap.setMode('watershed');
						} else {
							btn.setText('Restrict by Watershed')
							DSS_PortalMap.setMode('refine');
						}
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
			Ext.each(Ext.ComponentQuery.query('button', me), function(item) {
				if (item['pressed']) {
					item.toggle(false);
				}
			});
		}
	}

	
});