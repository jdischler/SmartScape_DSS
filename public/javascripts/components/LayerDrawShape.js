//-----------------------------------------------------
// DSS.components.LayerDrawShape
//
//-----------------------------------------------------
Ext.define('DSS.components.LayerDrawShape', {
    extend: 'DSS.components.LayerBase',
    alias: 'widget.layer_draw_shape',
    
	title: 'Draw Custom Selection',
	
	DSS_serverLayer: false, // 'cdl_2012' for example
	
	//--------------------------------------------------------------------------
	initComponent: function() {
		var me = this;
		
		Ext.applyIf(me, {
			items: [{
				xtype: 'container',
				padding: '0 32 4 32',
				html: 'TODO'
			}]
		});
		
		me.callParent(arguments);
	},
	
	//--------------------------------------------------------------------------
	configureSelection: function() {

		// TODO: 
		
        return false;
	}

});
