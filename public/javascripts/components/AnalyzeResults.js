//-----------------------------------------------------
// DSS.components.AnalyzeResults
//
//-----------------------------------------------------
Ext.define('DSS.components.AnalyzeResults', {
    extend: 'Ext.container.Container',
    alias: 'widget.analyze_results',
 
    requires: [
    ],
    
    padding: 4,
//	style: 'background: rgba(48,64,96,0.8); border: 1px solid #256;border-radius: 16px; box-shadow: 0 10px 10px rgba(0,0,0,0.4)' ,
	style: 'background: rgba(220,230,220,0.8); border: 1px solid #256;border-radius: 16px; box-shadow: 0 10px 10px rgba(0,0,0,0.4)' ,
	resizable: 'true',
	minWidth: 480,
	minHeight: 380,
	layout: {
		type: 'vbox',
		align: 'center',
		//pack: 'center'
	},
	
	//--------------------------------------------------------------------------
	initComponent: function() {
		var me = this;
		
		Ext.applyIf(me, {
			items:[]
		});
		
		me.callParent(arguments);
	},
		
});
