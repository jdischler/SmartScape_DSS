//-----------------------------------------------------
// DSS.components.LayerIndexed
//
//-----------------------------------------------------
Ext.define('DSS.components.LayerIndexed', {
    extend: 'DSS.components.LayerBase',
    alias: 'widget.layer_indexed',
    
	title: 'Title',
	
	DSS_columns: 4,			// num checkbox columns
	DSS_serverLayer: false, // 'cdl_2012' for example
	DSS_indexConfig: [{ 	// straight-up checkbox item configs, example
		boxLabel: 'Corn', 		name: "lt", indexValues: [1], checked: true
	},{
		boxLabel: 'Urban', 		name: "lt", indexValues: [10,11]
	}],
	
	//--------------------------------------------------------------------------
	initComponent: function() {
		var me = this;
		
		Ext.applyIf(me, {
			items: [{
				xtype: 'container',
				padding: '0 4 0 32',
				layout: 'fit',
				items: [{
					xtype: 'checkboxgroup',
					vertical: true,
					itemId: 'options',
					columns: me.DSS_columns,
					listeners: {
						change: function(self, newVal, oldVal) {
							DSS.Layers.valueChanged();
						}
					},
					items: me.DSS_indexConfig
				}]
			}]
		});
		
		me.callParent(arguments);
	},
	
	//--------------------------------------------------------------------------
	configureSelection: function() {
		
		var me = this;
		if (me.getCollapsed() || me.isHidden()) return false;
		
		var selectionDef = { 
				name: me.DSS_serverLayer,
				type: 'indexed',
				matchValues: []
			};
			
		var addedElement = false;
        var cont = me.down('#options');
        for (var i = 0; i < cont.items.length; i++) {
        	var item = cont.items.items[i];
        	if (item.getValue()) {
        		addedElement = true;
        		var elements = item.indexValues;
        		selectionDef.matchValues = selectionDef.matchValues.concat(elements);
        	}
        }
        
        if (!addedElement) return false;
        
        return selectionDef;		
	}

});
