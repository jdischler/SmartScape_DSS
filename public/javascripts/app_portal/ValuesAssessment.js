
//------------------------------------------------------------------------------
Ext.define('DSS.app_portal.ValuesAssessment', {
//------------------------------------------------------------------------------
	extend: 'Ext.panel.Panel',
	alias: 'widget.values_assessment',

	floating: true,
	width: 380,
	height: 1,
	layout: 'fit',
	bodyPadding: '16 32',
	bodyStyle: 'background: rgb(240,240,234)',
	title: 'Set Personal Values',
	
	//--------------------------------------------------------------------------
	initComponent: function() {
		var me = this;
		
		Ext.applyIf(me, {
			items: [{
				xtype: 'container',
				layout: {
					type: 'vbox',
					//align: 'center',
				},
				defaults: {
					xtype: 'slider',
					width: 300,
					minValue: 0,
					maxValue: 100,
					margin: '8 32 8 0',
					labelAlign: 'right',
					labelWidth: 120,
					increment: 5,
					value: 10
				},
				items: [{
					xtype: 'component',
					width: '100%',
					margin: '0 0 16 0',
					html: 'Please adjust your preferences regarding how much importance you place to each of the axes shown to the right. ' +
						'The values must add up to 100...yadda yadda'
				},{
					fieldLabel: 'Net Income'
				},{
					fieldLabel: 'Gross Biofuel'
				},{
					fieldLabel: 'Emissions'
				},{
					fieldLabel: 'Soil Retention'
				},{
					fieldLabel: 'Soil Carbon'
				},{
					fieldLabel: 'Bird Habitat'
				},{
					fieldLabel: 'Pest Supression'
				},{
					fieldLabel: 'Pollination Services'
				}]
			}]
		});
		
		me.callParent(arguments);
	},
	
});
