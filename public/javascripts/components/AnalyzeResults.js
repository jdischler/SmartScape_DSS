//-----------------------------------------------------
// DSS.components.AnalyzeResults
//
//-----------------------------------------------------
Ext.define('DSS.components.AnalyzeResults', {
    extend: 'Ext.container.Container',
    alias: 'widget.analyze_results',
 
    requires: [
    ],
    id: 'dss-analyze-results',
    
    padding: 4,
	style: 'background: rgba(48,64,96,0.8); border: 1px solid #256;border-radius: 16px; box-shadow: 0 10px 10px rgba(0,0,0,0.4)' ,
//	style: 'background: rgba(220,230,220,0.8); border: 1px solid #256;border-radius: 16px; box-shadow: 0 10px 10px rgba(0,0,0,0.4)' ,
//	resizable: 'true',
	width: 380,
//	minHeight: 380,
	layout: {
		type: 'vbox',
		align: 'stretch',
		//pack: 'center'
	},
	
	//--------------------------------------------------------------------------
	initComponent: function() {
		var me = this;
		
		function widget(label, value, unit) {
			return {
				xtype: 'container',
				layout: DSS.utils.layout('hbox','start','center'),
				items: [{
					xtype: 'component',
					html: label + ':',
					width: 140,
					style: 'text-align: right; color: #333'
				},{
					xtype: 'component',
					html: Ext.util.Format.number(value, '0,000.##'),
					width: 130,
					padding: 2,
					margin: '2 4',
					style: 'text-align: right; color: #333; background: #fff; border: 1px solid #ccc; border-radius: 4px'
				},{
					xtype: 'component',
					html: unit,
					width: 32,
					margin: '2 4 2 0',
					style: 'color: #888; font-size: 12px'
				},{
					xtype: 'component',
					width: 28, height: 24,
					margin: 2,
					style: 'background: #ff7; border: 1px solid #ccc; border-radius: 4px; box-shadow: 0 2px 4px rgba(0,0,0,0.3)'
						
				}]
			}
		};
		Ext.applyIf(me, {
			items:[{
				xtype: 'component',
				style: 'color: #fff; font-size: 1.1em; font-weight: bold; text-shadow: 1px 1px 1px #000',
				html: 'Analyze Results',
				padding: '2 0 2 8'
			},{
				xtype: 'container',
				style: 'background: #f3f3f3; border-radius: 12px; border: 1px solid #ccc',
				flex: 1,
				layout: {
					type: 'vbox',
					align: 'center'
				},
				items: [{
					xtype: 'graded_radar'
				},{
					xtype: 'container',
					layout: DSS.utils.layout('vbox',undefined, 'stretch'),
					items: [
						widget('Pollinators', 0.3, '0 to 1'),
						widget('Bird Habitat', 0.4, '0 to 1'),
						widget('Pest Supression', 0.23, '0 to 1'),
						widget('Nitrogen Retention', 9494.4,'lb/yr'),
						widget('Soil Retention', 12453850.8,'ton/yr'),
						widget('Phosphorus Retention', 11034.83,'lb/yr'),
						widget('Soil Carbon', 480049.83,'ton/yr'),
						widget('Emissions', 23560.83,'ton/yr'),
					]
				}]

			}]
		});
		
		me.callParent(arguments);
	},
		
});
